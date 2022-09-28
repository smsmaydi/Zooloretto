package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AIpurchaseTileAction
import entity.AnimalTile
import entity.Player
import entity.Tile
import entity.VendingStallTile
import service.RootService

/**
 * Wrapper for all possible actions for [AIpurchaseTileAction]
 */
class AIpurchaseTileActionGroup: AIActionGroup() {

    private fun checkSameClass(tileA: Tile, tileB: Tile): Boolean {
        return (tileA is AnimalTile && tileB is AnimalTile && tileA.type == tileB.type) ||
            (tileA is VendingStallTile && tileB is VendingStallTile && tileA.type == tileB.type)
    }

    private fun removeDuplicates(tiles: MutableList<Tile>): MutableList<Tile> {
        val uniqueTiles = mutableListOf<Tile>()
        for(tile in tiles) {
            if (uniqueTiles.none { checkSameClass(it, tile) })
                uniqueTiles.add(tile)
        }
        return uniqueTiles
    }

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()
        val currentGame = aiRootService.game ?: return actions

        if(aiPlayer.numCoins < 2) return actions

        currentGame.players.forEachIndexed { index, otherPlayer ->
            if(aiPlayer != otherPlayer){
                for(animal in removeDuplicates(otherPlayer.barn.animals as MutableList<Tile>))
                    actions.add(AIpurchaseTileAction(index, animal))
                for(vendingStall in removeDuplicates(otherPlayer.barn.vendingStalls as MutableList<Tile>))
                    actions.add(AIpurchaseTileAction(index, vendingStall))
            }
        }

        return actions
    }

}
