package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AIexpandZooAction
import entity.Player
import service.RootService

/**
 * Wrapper for all possible actions for [AIexpandZooAction]
 */
class AIexpandZooActionGroup: AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val currentGame = aiRootService.game ?: return mutableListOf()

        if(aiPlayer.numCoins >= 3){
            val allExpansionBoardsUsed =
                currentGame.getActivePlayer().expansionBoards.none { board -> !board.isUsed }
            if(!allExpansionBoardsUsed){
                return mutableListOf(AIexpandZooAction())
            }
        }

        return mutableListOf()
    }
}
