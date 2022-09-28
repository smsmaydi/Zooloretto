package ai

import entity.*
import org.junit.jupiter.api.Test
import service.RootService
import java.io.File
import kotlin.test.assertEquals

/**
 * Test for [AIActionService]
 */
class AIActionServiceTest {
    private val rootService = RootService()

    /**
     * test AI
     */
    @Test
    fun testAIMaxN() {
        val p1 = Player.createPlayer("Tom", 3)
        val p2 = PlayerAI.createPlayer("Test", 3, AIDifficulty.HARD)
        val p3 = Player.createPlayer("Timon", 3)

        rootService.gameService.startGame(listOf(p1, p2, p3))

        val tile = rootService.game!!.tileStack.tiles.last()
        rootService.playerActionService.addTileToTruck(tile, rootService.game!!.deliveryTrucks.first())

        val aiActionService = AIActionService(rootService)
        val currentGame = rootService.game ?: error("cannot be null")

        //is activePlayer AI Player
        // assert(currentGame.getActivePlayer() is PlayerAI)
        assertEquals(1, rootService.game!!.activePlayerIndex)
    }

    /**
     * test AI twoPlayer
     */
    @Test
    fun testAIMaxNTwoPlayer() {
        val p1 = Player.createPlayer("Tom", 2)
        val p2 = PlayerAI.createPlayer("Test", 2, AIDifficulty.HARD)
        rootService.gameService.startGame(listOf(p1, p2))

        val tile = rootService.game!!.tileStack.tiles.last()
        rootService.playerActionService.addTileToTruck(tile, rootService.game!!.deliveryTrucks.first())

        val aiActionService = AIActionService(rootService)
        val currentGame = rootService.game ?: error("cannot be null")

        //is activePlayer AI Player
        // assert(currentGame.getActivePlayer() is PlayerAI)

        //val activePlayer = currentGame.getActivePlayer()
        //aiActionService.executeAIMove(activePlayer, AIDifficulty.HARD)
        assertEquals(1, rootService.game!!.activePlayerIndex)
    }

    /**
     * This is a modified version of the showHint in PlayerActionService so that the difficulty can be manually set
     */
    private fun showHint(aiDifficulty: AIDifficulty) {
        val currentAIActionService = this.rootService.aiActionService ?: return
        val currentGame = this.rootService.game ?: return

        currentGame.getActivePlayer().receivedHint = true
        currentAIActionService.executeAIMove(currentGame.getActivePlayer() as Player, aiDifficulty)
    }

    /**
     * test AI hint
     * run hints for each player until the game is over
     */
    @Test
    fun testHint() {
        // Use static draw pile
        val DRAW_PILE_PATH = "src/main/resources/draw_piles/zooloretto2players.txt".replace("/", File.separator)

        // Set the AI difficulty
        val AI_DIFFICULTY = AIDifficulty.EASY

        // Create the game
        val loadedConfigFile = rootService.gameLoaderService!!.parseConfigurationFile(DRAW_PILE_PATH)
        rootService.gameService.startGameFromConfigurationFile(
            loadedConfigFile, listOf(
                Player.createPlayer("Timon", 2),
                Player.createPlayer("Tom", 2)
            )
        )
        var currentGame = rootService.game ?: error("cannot be null")

        // Calculate the number of tiles in the draw pile (including endstack but excluding child tiles)
        val sizeBeginning = currentGame.tileStack.tiles.filterNot { tile ->
            tile is AnimalTile && tile.variant == AnimalVariant.CHILD
        }.size + currentGame.endStack.tiles.size

        // Simulate the game until the game is over
        var i = 0
        while (currentGame.gameState != GameState.FINISHED) {

            // Calculate the percentage of tiles already drawn from the draw pile
            if (i % 10 == 0) {
                val size = currentGame.tileStack.tiles.filterNot { tile ->
                    tile is AnimalTile && tile.variant == AnimalVariant.CHILD
                }.size + currentGame.endStack.tiles.size
                val percent = String.format("%.3f", (1 - (size / sizeBeginning.toDouble())) * 100)
                println("Number of Tiles in drawStack ${size}: (${percent}%)")
            }

            //val start = System.nanoTime();
            // Execute the hint AI move
            showHint(AI_DIFFICULTY)
            ///val time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
            //println("calc move took TIME: ${time / 1000.000} s")

            // Update the current game reference
            currentGame = rootService.game ?: error("cannot be null")

            i++
        }

        // Output players' scores and print the winner
        currentGame = rootService.game ?: error("cannot be null")
        for (player in currentGame.players)
            println("Player ${player.name} has ${player.calculatePoints()} points")
        val winner = currentGame.players.maxByOrNull { it.calculatePoints() }
        if (winner != null) {
            println("Winner is ${winner.name} with ${winner.calculatePoints()} points")
        } else {
            error("No winner")
        }
    }
}