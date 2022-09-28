package ai

import ai.actions.AIAction
import entity.Game
import entity.GameState
import service.RootService
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveTask

/** Calculate maxn recursively using the ForkJoinPool framework */
class MaxnRecursiveRunnable(
    private val oldGame: Game,
    private val depth: Int,
    private val points: Int,
    private val playerAI: Int,
    private val maxTimePanic: Long,
    val action: AIAction,
) : RecursiveTask<Int>() {

    /** Calculate maxn recursively using the ForkJoinPool framework */
    override fun compute(): Int {
        val aiRootService = RootService(true)
        aiRootService.game = oldGame

        action.executeSimulation(aiRootService)

        if (depth == 0 || aiRootService.game!!.gameState == GameState.FINISHED) {
            //aiRootService.gameService.undo()
            return AIActionService.evaluateState(aiRootService.game!!, playerAI)
        }

        if (System.currentTimeMillis() > maxTimePanic)
            return Int.MIN_VALUE

        val actions = AIActionService.generateActions(
            aiRootService,
            aiRootService.game!!.players.indexOf(aiRootService.game!!.getActivePlayer()) != playerAI,
        )

        val tasks = ArrayList<MaxnRecursiveRunnable>()
        for (subAction in actions) {
            tasks.add(
                MaxnRecursiveRunnable(
                    aiRootService.game!!.clone(),
                    depth - 1,
                    points,
                    playerAI,
                    maxTimePanic,
                    subAction
                )
            )
        }

        val maxEval = ForkJoinTask.invokeAll(tasks).stream().mapToInt { it.join().toInt() }.max().orElse(Int.MIN_VALUE)

        //aiRootService.gameService.undo()

        return maxEval
    }
}