package service

import AbstractRefreshingService
import Logger
import entity.*
import java.time.LocalDateTime


/**
 * - the max number of coins that is there on game state
 */
const val MAX_NUM_COINS_ON_GAME_START = 30

/**
 * - the num of coins a player gets on game state
 */
const val NUM_COINS_A_PLAYER_GETS_ON_GAME_START = 2

/**
 * - the max number of tiles that are put on the last pile on game start
 */
const val MAX_TILES_ON_LAST_PILE = 15

/**
 * The game service
 */
class GameService(private val rootService: RootService, private val gameComponents: GameComponents) :
    AbstractRefreshingService() {

    private val logger: Logger = Logger("GameService")

    /**
     * it starts the game or throws an error if there is a problem with validation
     */
    fun startGame(players: List<Player>) {
        this.logIfNotSimulating("Starting game with num Players ${players.size}")
        val numPlayers = players.size

        // validate num of players
        check(numPlayers in 2..5) { "a game can contain 2,3,4 or 5 players. players count: $numPlayers" }

        this.gameComponents.initForNumPlayers(numPlayers)
        val game = Game(this.generateGameName())
        game.endStack = TileStack(this.gameComponents.getEndStackTiles())
        game.tileStack = TileStack(this.gameComponents.getTileStackTiles())
        game.deliveryTrucks = gameComponents.getDeliveryTrucks()
        game.players = players.toMutableList()
        game.coinsInBank = MAX_NUM_COINS_ON_GAME_START - numPlayers * NUM_COINS_A_PLAYER_GETS_ON_GAME_START

        // init game.tileCount use in **AI**
        game.tileCount = TileCount()

        game.tileCount!!.add(game.tileStack.tiles.filterNot { tile ->
            tile is AnimalTile && tile.variant == AnimalVariant.CHILD
        }
            .toMutableList())
        game.tileCount!!.add(game.endStack.tiles)
        // end init for **AI**

        rootService.game = game
        // execute AI move at beginning of Game
        rootService.playerActionService.preRoundChecksAndExecuteAIMove()

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterGameStart() }
    }


    /**
     * The startGameFromConfigurationFile fun
     */
    fun startGameFromConfigurationFile(
        config: GameLoaderService.ConfigurationFileParsingResult,
        players: List<Player>
    ) {

        this.logIfNotSimulating("Starting game from configuration file result with num players ${players.size}")
        val numPlayers = config.numPlayers

        // validate num of players
        check(numPlayers in 2..5) { "a game can contain 2,3,4 or 5 players. players count: $numPlayers" }

        this.gameComponents.initForNumPlayers(numPlayers)
        val game = Game(this.generateGameName())
        game.endStack = TileStack(config.endStackTiles)
        game.tileStack = TileStack(config.tileStackTiles)
        game.deliveryTrucks = gameComponents.getDeliveryTrucks()
        game.players = players.toMutableList()
        game.coinsInBank = MAX_NUM_COINS_ON_GAME_START - numPlayers * NUM_COINS_A_PLAYER_GETS_ON_GAME_START
        rootService.game = game

        // init game.tileCount use in **AI**
        game.tileCount = TileCount()
        game.tileCount!!.add(game.tileStack.tiles)
        game.tileCount!!.add(game.endStack.tiles)

        // execute AI move at beginning of Game
        rootService.playerActionService.preRoundChecksAndExecuteAIMove()
        // end init for **AI**

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterGameStart() }
    }


    /**
     * - is called after a new round starts
     */
    fun startNewRound() {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot start a new round" }

        this.logIfNotSimulating("Starting a new round with active player index = ${game.lastPlayerToTakeDeliveryTruckIndex}")

        // on a new round. the last player who took a truck in the prev round begins
        game.activePlayerIndex =
            game.lastPlayerToTakeDeliveryTruckIndex

        game.players.forEach { player ->
            player.deliveryTruck = null
            player.tookDeliveryTruck = false
        }


        // clear because of the rules in 2 players game (the last truck with tiles should be empty)
        game.deliveryTrucks.forEach { truck ->
            truck.isTaken = false
            truck.tiles.clear()
        }

        // execute AI move at beginning of new Round
        rootService.playerActionService.preRoundChecksAndExecuteAIMove()

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterNewRoundStarted() }
    }


    /**
     *  it starts a saved game using the game name
     */
    fun startSavedGame(gameName: String) {
        val gameLoader = rootService.gameLoaderService
        checkNotNull(gameLoader) { "game loader is null" }
        this.logIfNotSimulating("Starting saved game with the name $gameName")
        var game = gameLoader.load(gameName)
        rootService.game = game
        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterGameStart() }
    }

    /**
     *  it calculates the points of the given player
     */
    fun calculatePlayerPoints(player: Player): Int {
        this.logIfNotSimulating("Calculating the player points")
        return player.calculatePoints()
    }

    /**
     *  undo undoes the last action the player has made
     *  it uses the linked list of games and sets the rootService game to the previous pointer
     */
    fun undo() {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot undo" }
        if (game.prevGame == null) {
            return // nothing to be undone (we are the root game)
        }
        this.logIfNotSimulating("Game undo")
        game.usedUndo = true
        rootService.game = game.prevGame!!
        if (!rootService.simulate) {
            this.onAllRefreshables { refreshAfterGameStateChanged() }
            this.onAllRefreshables { refreshAfterUndoRedo() }
        }
    }

    /**
     *  redo redoes the last undone action
     *  it uses the linked list of games and sets the rootService game to the next pointer
     */
    fun redo() {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot redo" }
        if (game.nextGame == null) {
            return // nothing to be redone (we are the last game)
        }
        this.logIfNotSimulating("Game redo")
        rootService.game = game.nextGame!!
        if (!rootService.simulate) {
            this.onAllRefreshables { refreshAfterGameStateChanged() }
            this.onAllRefreshables { refreshAfterUndoRedo() }
        }
    }


    /**
     *  creates a game name from the current time
     */
    fun setSimulationSpeed(simulationSpeed: Int) {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot set simulation speed" }
        this.logIfNotSimulating("Setting the simulation speed to $simulationSpeed")
        game.simulationSpeed = simulationSpeed
    }

    /**
     * - returns true if the last pile was used
     */
    fun hasLastPileStarted(): Boolean {
        val game = rootService.game
        checkNotNull(game) { "game is null" }
        return game.endStack.tiles.size < MAX_TILES_ON_LAST_PILE
    }

    /**
     * - calculates the points of all players and return a list of the points
     */
    fun calculateEndGame(): List<Int> {
        this.logIfNotSimulating("Calculating game end")
        val game = rootService.game
        checkNotNull(game) { "game is null" }
        return game.players.map { player -> player.calculatePoints() }
    }

    /**
     * - updates the high score list and saves it to the fs
     * - should be called after game end
     */
    fun updateHighScore() {
        val game = rootService.game
        checkNotNull(game) { "game is null" }

        this.logIfNotSimulating("Updating the highscore list")

        val highScore = rootService.highScore
        checkNotNull(highScore) { "high score is null" }

        if (game.didUseUndo()) {
            return // the game used undo. don't consider it
        }

        val playersToConsider = game.players.filter { player -> player !is PlayerAI && !player.receivedHint }
        for (player in playersToConsider) {
            highScore.scores.add(
                HighScoreRecord(
                    player.name,
                    player.calculatePoints(),
                    System.currentTimeMillis()
                )
            )
        }
        // limit the high score list to the highest 10 result
        highScore.sortAndKeepFirst10()
        rootService.gameLoaderService?.saveHighScore()
    }


    /**
     *  creates a game name from the current time
     */
    private fun generateGameName(): String {
        return LocalDateTime.now().toString().replace(":", "-")
    }

    /**
     *  is called after the game has ended
     */
    fun handleEndGame() {
        this.logIfNotSimulating("Handling game end")
        val game = rootService.game
        checkNotNull(game) { "game is null" }
        game.gameState = GameState.FINISHED

        if (!rootService.simulate) {
            this.updateHighScore()
            this.onAllRefreshables { refreshAfterGameEnd() }
        }
    }

    fun logIfNotSimulating(message: String) {
        if (!rootService.simulate)
            this.logger.log(message)
    }
}

