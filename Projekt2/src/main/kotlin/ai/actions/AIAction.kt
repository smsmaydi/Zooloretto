package ai.actions

import entity.AnimalTile
import entity.CoinTile
import entity.Tile
import entity.VendingStallTile
import service.RootService

/**
 * Wrapper for AIaction
 */
abstract class AIAction {
    /**
     * executes simulation of action
     */
    abstract fun executeSimulation(aiRootService: RootService)

    /** Execute the action for real */
    abstract  fun execute(rootService: RootService)

    /**
     * compares two tiles if they are the same Class
     */
    fun checkSameClass(tileA: Tile, tileB: Tile): Boolean {
        return (tileA is AnimalTile && tileB is AnimalTile && tileA.type == tileB.type && tileA.variant == tileB.variant) ||
                (tileA is VendingStallTile && tileB is VendingStallTile && tileA.type == tileB.type) ||
                (tileA is CoinTile && tileB is CoinTile)
    }

    /**
     * to String methode
     */
    abstract fun toString(aiRootService: RootService): String

    /**
     * to short String methode
     */
    abstract fun toStringShort(aiRootService: RootService): String
}