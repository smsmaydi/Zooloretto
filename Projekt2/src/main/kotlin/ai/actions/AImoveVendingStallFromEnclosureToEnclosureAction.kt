package ai.actions

import entity.Enclosure
import entity.VendingStallTile
import service.RootService

/**
 * Wrapper for AImoveVendingStallFromEnclosureToEnclosureAction
 */
class AImoveVendingStallFromEnclosureToEnclosureAction(
    private val srcEnclosureIndex: Int,
    private val dstEnclosureIndex: Int,
    private val vendingStallinSrcEnclosure: VendingStallTile
) : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {

        val currentGame = aiRootService.game ?: return
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards)

        for (vendingStall in enclosures[srcEnclosureIndex].vendingStalls) {
            if (checkSameClass(vendingStall, vendingStallinSrcEnclosure)) {
                aiRootService.playerActionService.moveVendingStallFromEnclosureToEnclosure(
                    enclosures[srcEnclosureIndex], enclosures[dstEnclosureIndex], vendingStall
                )
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
        return "Move $vendingStallinSrcEnclosure from ${enclosures[srcEnclosureIndex]} to ${enclosures[dstEnclosureIndex]}"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return ""
        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(currentGame.getActivePlayer().enclosures)
        enclosures.addAll(currentGame.getActivePlayer().expansionBoards.filter { it.isUsed })
        return "Move $vendingStallinSrcEnclosure between enclosures: " +
                "Enc with Points ${enclosures[srcEnclosureIndex].biggerPoints} " +
                "to Enc with Points ${enclosures[dstEnclosureIndex].biggerPoints}"
    }
}