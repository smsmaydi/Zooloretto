package ai.actions

import entity.AnimalTile
import entity.Enclosure
import service.RootService

/**
 * Wrapper for AImoveAnimalTileFromBarnToEnclosureAction
 */
class AImoveAnimalTileFromBarnToEnclosureAction(
    private val animalTile: AnimalTile,
    private val dstEnclosureIndex: Int
) : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {

        val currentGame = aiRootService.game ?: return

        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards)

        for( animal in currentGame.getActivePlayer().barn.animals){
            if(checkSameClass(animal , animalTile )){
                aiRootService.playerActionService.moveAnimalTileFromBarnToEnclosure(animal, enclosures[dstEnclosureIndex])
                return
            }
        }
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
        return "Move $animalTile from ${currentGame.getActivePlayer().barn} to ${enclosures[dstEnclosureIndex]}"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return ""
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards.filter { it.isUsed })
        return "Move $animalTile from barn to enclosure with type ${enclosures[dstEnclosureIndex].getAnimalType()}"
    }
}