package ai.actions

import entity.AnimalTile
import entity.Tile
import entity.VendingStallTile
import service.RootService

/**
 * Wrapper for AIpurchaseTileAction
 */
class AIpurchaseTileAction(
    private val otherPlayerIndex: Int,
    /** The tile to purchase */
    val tile: Tile
) : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {
        // This purchases the Tile from the other player and adds the tile to a free place in the players Zoo

        val currrentGame = aiRootService.game ?: return

        val barn = currrentGame.players[otherPlayerIndex].barn

        if (tile is AnimalTile) {
            for (animal in barn.animals) {
                if (checkSameClass(animal, tile)) {
                    aiRootService.playerActionService.purchaseTile(currrentGame.players[otherPlayerIndex], animal)
                    return
                }
            }
        }

        if (tile is VendingStallTile) {
            for (vendingStall in barn.vendingStalls) {
                if (checkSameClass(vendingStall, tile)) {
                    aiRootService.playerActionService.purchaseTile(currrentGame.players[otherPlayerIndex], vendingStall)
                    return
                }
            }
        }
        return
    }

    /**
     * executes action
     */
    override fun execute(rootService: RootService) {
        executeSimulation(rootService)
    }

    /** Implement a custom toString method, the RootService is needed to access the current game objects */
    override fun toString(aiRootService: RootService): String {
        val currrentGame = aiRootService.game ?: return ""
        return "Purchase $tile from ${currrentGame.players[otherPlayerIndex].name} barn"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        val currrentGame = aiRootService.game ?: return ""
        return "Purchase $tile from ${currrentGame.players[otherPlayerIndex].name} barn"
    }
}