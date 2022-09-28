package ai.actions

import entity.Enclosure
import service.RootService

/**
 * Wrapper for AIexchangeTilesEnclosureAction
 */
class AIexchangeTilesEnclosureAction(private val sourceIndex: Int, private val destinationIndex: Int) : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {

        val currentGame = aiRootService.game ?: return

        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards.filter { it.isUsed })

        aiRootService.playerActionService.exchangeTiles(enclosures[sourceIndex], enclosures[destinationIndex])
    }
    /**
     * executes action
     */
    override fun execute(rootService: RootService) {
        executeSimulation(rootService)
    }

    /** Implement a custom toString method, the RootService is needed to access the current game objects */
    override fun toString(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return ""
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards.filter { it.isUsed })
        return "Exchange tiles from ${enclosures[sourceIndex]} with tiles from ${enclosures[destinationIndex]}"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return ""
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards.filter { it.isUsed })
        val src = enclosures[sourceIndex]
        val dst = enclosures[destinationIndex]
        return "Exchange between enclosures: ${src.getAnimalType()} with ${dst.getAnimalType()}"
    }
}