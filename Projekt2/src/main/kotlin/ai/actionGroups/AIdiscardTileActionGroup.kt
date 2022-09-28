package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AIdiscardTileAction
import entity.Player
import service.RootService


/**
 * Wrapper for all possible actions for [AIdiscardTileAction]
 */
class AIdiscardTileActionGroup : AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()

        if (aiPlayer.numCoins >= 2) {
            for (animal in aiPlayer.barn.animals)
                actions.add(AIdiscardTileAction(animal))
            for (vendingStall in aiPlayer.barn.vendingStalls)
                actions.add(AIdiscardTileAction(vendingStall))
        }

        return actions
    }
}
