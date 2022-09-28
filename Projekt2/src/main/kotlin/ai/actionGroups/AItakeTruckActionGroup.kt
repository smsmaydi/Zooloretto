package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AItakeTruckAction
import entity.Player
import service.RootService

/**
 * Wrapper for all possible actions for [AItakeTruckAction]
 */
class AItakeTruckActionGroup: AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()

        val currentGame = aiRootService.game ?: return actions

        for(deliveryTruck in currentGame.deliveryTrucks) {
            if (deliveryTruck.tiles.isNotEmpty() && !deliveryTruck.isTaken) {
                actions.add(AItakeTruckAction(currentGame.deliveryTrucks.indexOf(deliveryTruck)))
            }
        }

        return actions
    }

}
