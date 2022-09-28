package ai.actions

import service.RootService

/**
 * Wrapper for AIexpandZooAction
 */
class AIexpandZooAction : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {
        aiRootService.playerActionService.expandZoo()
    }

    /**
     * executes action
     */
    override fun execute(rootService: RootService) {
        executeSimulation(rootService)
    }

    /** Implement a custom toString method, the RootService is needed to access the current game objects */
    override fun toString(aiRootService: RootService): String {
        return "Expand zoo"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        return "Expand zoo"
    }
}