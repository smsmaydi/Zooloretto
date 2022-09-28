package ai.actions

import entity.Tile
import service.RootService

/**
 * Wrapper for AIdiscardTileAction
 */
class AIdiscardTileAction(
    /** The tile to discard */
    var tile: Tile
) : AIAction() {

    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {
        val currenGame = aiRootService.game ?: return

        // find correct tile in barn
        for (tile in currenGame.getActivePlayer().barn.animals) {
            if (checkSameClass(this.tile, tile)) {
                aiRootService.playerActionService.discardTile(tile)
                return
            }
        }

        // find correct tile in barn
        for (tile in currenGame.getActivePlayer().barn.vendingStalls){
            if(checkSameClass(this.tile, tile)){
                aiRootService.playerActionService.discardTile(tile)
                return
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
        return "DiscardTile $tile"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        return "DiscardTile $tile"
    }
}