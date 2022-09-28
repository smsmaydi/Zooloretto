package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AIaddTileToTruckAction
import entity.Player
import service.RootService

/** Max tiles to simulate */
const val MAX_TILES_TO_CHOOSE = 5

/**
 * Wrapper for all possible actions for [AIaddTileToTruckAction]
 */
class AIaddTileToTruckActionGroup : AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()
        val currentGame = aiRootService.game ?: return actions

        // tileCount
        val tileCount = currentGame.tileCount ?: return actions
        // useed to get only firs MAX_TILES_TO_CHOOSE tiles
        var count = 0

        for (pair in tileCount.getProbabilities()) {
            count++
            if (count > MAX_TILES_TO_CHOOSE)
                break

            for (truck in currentGame.deliveryTrucks) {
                if (truck.hasSpace() && !truck.isTaken)
                    actions.add(AIaddTileToTruckAction(pair.first, truck, currentGame.deliveryTrucks.indexOf(truck)))
            }
        }

        return actions
    }

}
