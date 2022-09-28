package ai

import ai.actionGroups.*
import ai.actions.AIAction
import entity.AIDifficulty
import entity.Game
import entity.Player
import service.RootService
import java.util.concurrent.ForkJoinPool

/**
 * Holds implementation of AI
 */
class AIActionService(private val rootService: RootService) {
    /**
     * calculates the best move for the given difficultly
     * and executes it
     *
     * @param difficulty either EASY, NORMAL, HARD
     */
    fun executeAIMove(playerAI: Player, difficulty: AIDifficulty): String {
        val currentGame = rootService.game ?: error("Game cannot be null")
        val aiPlayerIndex = currentGame.players.indexOf(playerAI)

        val maxTimePanic = System.currentTimeMillis() + 10000 // 10s

        val bestMove = when (difficulty) {
            AIDifficulty.EASY -> maxn(currentGame, 3, aiPlayerIndex, maxTimePanic)
            AIDifficulty.NORMAL -> maxn(currentGame, 5, aiPlayerIndex, maxTimePanic)
            AIDifficulty.HARD -> maxn(currentGame, 6, aiPlayerIndex, maxTimePanic)
        }

        //println(bestMove.toString(rootService))
        val returnString = bestMove.toStringShort(rootService)

        bestMove.execute(rootService)

        return returnString
    }

    /** Top level maxn function */
    private fun maxn(oldGame: Game, depth: Int, playerAI: Int, maxTimePanic: Long): AIAction {
        val aiRootService = RootService(true)
        aiRootService.game = oldGame

        val actions = generateActions(
            aiRootService,
            aiRootService.game!!.players.indexOf(aiRootService.game!!.getActivePlayer()) != playerAI,
        )

        check(actions.size != 0) { "Actions can not be empty" }

        val tasks = ArrayList<MaxnRecursiveRunnable>()
        for (action in actions) {
            val task = MaxnRecursiveRunnable(aiRootService.game!!.clone(), depth - 1, 0, playerAI, maxTimePanic, action)
            forkJoinPool.submit(task)
            tasks.add(task)
        }

        var maxAction = actions[0]
        var maxActionPoints = Int.MIN_VALUE
        for (task in tasks) {
            val points = task.join()
            if (points > maxActionPoints) {
                maxAction = task.action
                maxActionPoints = points
            }
        }

        return maxAction
    }

    /**
     * if a Player uses the hint the best move will be
     * calculated and executed.
     * In Player receivedHint is set to true
     */
    fun hint(player: Player) {
        executeAIMove(player, AIDifficulty.HARD)
    }

    companion object {
        /** A thread-pool on which the AI calculation tasks are executed */
        val forkJoinPool: ForkJoinPool = ForkJoinPool.commonPool()

        private val limitedActionGroups = mutableListOf(AIaddTileToTruckActionGroup(), AItakeTruckActionGroup())

        private val allActionGroups = mutableListOf(
            AIaddTileToTruckActionGroup(),
            AIdiscardTileActionGroup(),
            AIexchangeTilesActionGroup(),
            AIexpandZooActionGroup(),
            AImoveAnimalTileFromBarnToEnclosureActionGroup(),
            AImoveVendingStallFromEnclosureToEnclosureActionGroup(),
            AIpurchaseTileActionGroup(),
            AItakeTruckActionGroup()
        )

        /** Evaluate a game state using a hybrid evaluation function */
        fun evaluateState(currentGame: Game, playerAI: Int): Int {
            // Use hybrid evaluation function
            val otherPlayerPoints =
                currentGame.players.filter { it != currentGame.getActivePlayer() }.sumOf { it.calculatePoints() }
            return 2 * currentGame.players[playerAI].calculatePoints() * currentGame.players.size - otherPlayerPoints
        }

        /** Generate a list of possible AIAction for the current game state */
        fun generateActions(
            aiRootService: RootService,
            isLimitedPlayer: Boolean,
        ): MutableList<AIAction> {
            var actionGroups = allActionGroups
            if (isLimitedPlayer)
                actionGroups = limitedActionGroups

            val actions: MutableList<AIAction> = mutableListOf()
            val currentGame = aiRootService.game ?: error("Game cannot be null")
            for (actionGroup in actionGroups)
                actions.addAll(actionGroup.generateCombinations(aiRootService, currentGame.getActivePlayer() as Player))

            return actions
        }
    }
}