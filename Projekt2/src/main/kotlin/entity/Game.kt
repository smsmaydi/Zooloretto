package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * The game
 */
@Serializable
class Game(
    /** The name of the game (used for save game) */
    val name: String
) : Cloneable<Game> {
    /**
     * coins in Bank
     */
    var coinsInBank: Int = 0

    /**
     * simulation speed
     */
    var simulationSpeed: Int = 3

    /**
     *used undo
     */
    var usedUndo: Boolean = false

    /**
     * index of active PLayer
     */
    var activePlayerIndex: Int = 0

    /**
     * index of LastPlayerToTakeDeliveryTruck
     */
    var lastPlayerToTakeDeliveryTruckIndex: Int = 0

    /**
     * gameState
     */
    var gameState: GameState = GameState.ACTIVE

    /**
     * deliveryTrucks
     */
    var deliveryTrucks: MutableList<DeliveryTruck> = mutableListOf()

    /**
     * tileStack
     */
    var tileStack: TileStack = TileStack(mutableListOf())

    /**
     * endStack
     */
    var endStack: TileStack = TileStack(mutableListOf())

    /**
     * players
     */
    var players: MutableList<PlayerBase> = mutableListOf()

    /**
     * ref to next Game
     */
    @kotlinx.serialization.Transient
    var nextGame: Game? = null

    /**
     * ref to prev Game
     */
    @kotlinx.serialization.Transient
    var prevGame: Game? = null

    /**
     * needed for AI to calc tile on stack
     */
    var tileCount: TileCount? = null


    override fun clone(): Game {
        val newGame = Game(this.name)
        newGame.coinsInBank = this.coinsInBank
        newGame.usedUndo = this.usedUndo
        newGame.gameState = this.gameState
        newGame.activePlayerIndex = this.activePlayerIndex
        newGame.lastPlayerToTakeDeliveryTruckIndex = this.lastPlayerToTakeDeliveryTruckIndex
        newGame.deliveryTrucks = this.deliveryTrucks.map { it.clone() }.toMutableList()
        newGame.players = this.players.toList().map { (it as Cloneable<PlayerBase>).clone() }.toMutableList()
        newGame.tileStack = this.tileStack.clone()
        newGame.endStack = this.endStack.clone()
        newGame.nextGame = null
        newGame.prevGame = null
        newGame.simulationSpeed = this.simulationSpeed
        newGame.tileCount = if (this.tileCount != null) this.tileCount!!.clone() else null
        return newGame

    }

    // has to do with saving state to the file system
    /** The filename for the save */
    val fileName: String
        get() {
            if (this.prevGame == null) {
                // we are root
                return "0"
            }
            return (this.prevGame!!.fileName.toInt() + 1).toString()
        }

    /**
     *  - connects the child with the current game to build a linked list of game states (useful for undo,redo later)
     */
    fun connect(child: Game) {
        this.nextGame = child
        child.prevGame = this
    }

    /**
     * - will switch active player
     */
    fun switchActivePlayer() {
        this.activePlayerIndex = (this.activePlayerIndex + 1) % this.players.size
    }


    /**
     * - gets the active player
     */
    fun getActivePlayer(): PlayerBase {
        check(this.players.isNotEmpty()) { "game has no players" }
        return this.players[this.activePlayerIndex]
    }

    /**
     * - returns true if all players have taken delivery trucks
     */
    fun haveAllPlayersTakenDeliveryTrucks(): Boolean {
        check(this.players.isNotEmpty()) { "game has no players" }
        return this.players.none { player -> !player.tookDeliveryTruck }

    }

    /**
     * - returns true if the game used the undo functionality. the usedUndo can be set to any node of the linked list of states
     */
    fun didUseUndo(): Boolean {
        var root: Game? = this

        while (root?.prevGame != null) {
            root = root.prevGame
        }

        while (root != null) {
            if (root.usedUndo) {
                return true
            }
            root = root.nextGame
        }
        return false
    }

}
