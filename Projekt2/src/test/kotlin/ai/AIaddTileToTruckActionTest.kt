package ai

import ai.actionGroups.AIaddTileToTruckActionGroup
import ai.actionGroups.MAX_TILES_TO_CHOOSE
import ai.actions.AIaddTileToTruckAction
import entity.AIDifficulty
import entity.Player
import entity.PlayerAI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import service.RootService
import kotlin.test.assertEquals

/**
 * Test for [AIaddTileToTruckActionGroup] and [AIaddTileToTruckAction]
 */
class AIaddTileToTruckActionTest {

    private val rootService = RootService()

    /**
     * init Game
     */
    @BeforeEach
    fun initGame() {
        val p1 = PlayerAI.createPlayer("Test", 3, AIDifficulty.HARD)
        val p2 = Player.createPlayer("Tom", 3)
        val p3 = Player.createPlayer("Timon", 3)
        rootService.gameService.startGame(listOf(p1, p2, p3))
    }

    /**
     *  test AIaddTileToTruckActionTest
     */
    @Disabled
    @Test
    fun testAIaddTileToTruckActionTest() {

        val currentGame = rootService.game ?: return

        val group = AIaddTileToTruckActionGroup()
        val action = group.generateCombinations(rootService, currentGame.players[0] as Player)

        val sizeGeneratedAction = currentGame.deliveryTrucks.size * MAX_TILES_TO_CHOOSE

        assertEquals(sizeGeneratedAction, action.size)

        //at start this is the  tile with the highest value
        val highestProbabilityTile = currentGame.tileCount!!.getProbabilities().first().first
        assertEquals(highestProbabilityTile::class, (action[0] as AIaddTileToTruckAction).tile::class)

    }
}