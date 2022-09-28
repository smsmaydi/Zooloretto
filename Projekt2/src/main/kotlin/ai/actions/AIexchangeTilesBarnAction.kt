package ai.actions

import entity.AnimalType
import entity.Enclosure
import service.RootService

/**
 * Wrapper for AIexchangeTilesBarnAction
 */
class AIexchangeTilesBarnAction(
    /** Enclosure with which the tiles are to be exchanged */
    val enclosure: Enclosure,
    private val animalTypeInBarnToExchange: AnimalType,
    private val enclosureIndex: Int
) : AIAction() {

    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {
        val currentGame = aiRootService.game ?: return
        val activePlayer = currentGame.getActivePlayer()
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(activePlayer.enclosures)
        enclosures.addAll(activePlayer.expansionBoards.filter { it.isUsed })

        aiRootService.playerActionService.exchangeTiles(
            enclosures[enclosureIndex],
            currentGame.getActivePlayer().barn,
            animalTypeInBarnToExchange
        )
    }

    /**
     * executes action
     */
    override fun execute(rootService: RootService) {
        executeSimulation(rootService)
    }

    /** Implement a custom toString method, the RootService is needed to access the current game objects */
    override fun toString(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return "no Game"
        return "Exchange ${animalTypeInBarnToExchange}-tiles from ${currentGame.getActivePlayer().barn} with $enclosure"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return ""
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards.filter { it.isUsed })
        return "Exchange between barn and enclosure: ${animalTypeInBarnToExchange} with ${enclosure.getAnimalType()}"
    }
}