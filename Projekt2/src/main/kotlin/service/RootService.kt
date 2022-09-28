package service

import FileSystemImpl
import ai.AIActionService
import entity.Game
import entity.HighScore
import view.Refreshable

/**
 * - the root service is a wrapper for all other services.
 * - other services can reach each other through this class
 *  @param simulate this is an optional parameter used for the AI layer, to
 *  avoid create infinite rootServices because the AIActionService uses internally
 *  its own RootService for simulating actions.
 */
class RootService(val simulate: Boolean = false) {

    var gameComponentsService: GameComponents = GameComponentsService()
    var gameService = GameService(this, this.gameComponentsService)
    var gameLoaderService = if (!simulate) GameLoaderService(FileSystemImpl(), this) else null
    var playerActionService = PlayerActionService(this)
    var highScore: HighScore? = if (!simulate) this.gameLoaderService?.getHighScore() else null
    var aiActionService: AIActionService? = if (!simulate) AIActionService(this) else null


    /** The current game. Can be `null`, if no game has started yet. */
    var game: Game? = null

    /**
     * Adds the provided [newRefreshable] to all services connected to this root service
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }

}
