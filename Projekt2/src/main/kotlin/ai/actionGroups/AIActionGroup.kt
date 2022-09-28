package ai.actionGroups

import ai.actions.AIAction
import entity.Player
import service.RootService

/**
 * Wrapper for all possible actions
 */
abstract class AIActionGroup {

    /**
     * generate actions
     */
    abstract fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction>
}