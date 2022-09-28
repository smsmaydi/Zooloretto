package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AImoveVendingStallFromEnclosureToEnclosureAction
import entity.Enclosure
import entity.Player
import service.RootService


/**
 * Wrapper for all possible actions for [AImoveVendingStallFromEnclosureToEnclosureAction]
 */
class AImoveVendingStallFromEnclosureToEnclosureActionGroup : AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()

        // Cannot move anything if the player has not enough coins
        if (aiPlayer.numCoins < 1) return actions

        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(aiPlayer.enclosures)
        enclosures.addAll(aiPlayer.expansionBoards)

        enclosures.forEachIndexed { sourceIndex, sourceEnclosure ->
            for (vendingStall in sourceEnclosure.vendingStalls) {
                enclosures.forEachIndexed { destIndes, destEnclosure ->
                    if (sourceIndex != destIndes && destEnclosure.hasSpaceForVendingStall()) {
                        if (aiPlayer.expansionBoards.contains(destEnclosure)) {
                            val index = aiPlayer.expansionBoards.indexOf(destEnclosure)
                            if (aiPlayer.expansionBoards[index].isUsed)
                                actions.add(
                                    AImoveVendingStallFromEnclosureToEnclosureAction(
                                        sourceIndex,
                                        destIndes,
                                        vendingStall
                                    )
                                )
                        } else {
                            actions.add(
                                AImoveVendingStallFromEnclosureToEnclosureAction(
                                    sourceIndex,
                                    destIndes,
                                    vendingStall
                                )
                            )
                        }
                    }
                }
            }
        }

        return actions
    }
}