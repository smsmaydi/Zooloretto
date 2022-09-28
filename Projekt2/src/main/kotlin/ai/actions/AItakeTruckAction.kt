package ai.actions

import service.RootService

/**
 * Wrapper for AItakeTruckAction
 */
class AItakeTruckAction(private val truckIndex: Int) : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {
        val currentGame = aiRootService.game ?: return

        //find truck in rootService
        val currentTruck = currentGame.deliveryTrucks[truckIndex]

        aiRootService.playerActionService.takeTruck(currentTruck)
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
        val currentTruck = currentGame.deliveryTrucks[truckIndex]
        return "Take truck $currentTruck"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        val currentGame = aiRootService.game ?: return ""
        val currentTruck = currentGame.deliveryTrucks[truckIndex]
        return "Take truck at index $truckIndex (${currentTruck.tiles})"
    }
}