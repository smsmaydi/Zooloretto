package ai.actions

import entity.DeliveryTruck
import entity.Tile
import service.RootService

/**
 * Wrapper for AIaddTileToTruckAction
 */
class AIaddTileToTruckAction(
    /** The tile to add to the truck */
    val tile: Tile,
    /** The truck the tile is to be added to */
    val truck: DeliveryTruck,
    private val truckIndex: Int
) : AIAction() {
    /**
     * executes simulation of action
     */
    override fun executeSimulation(aiRootService: RootService) {
        val currentGame = aiRootService.game ?: return

        /* TODO decrement count of tile type in TileCount which has the same tile type as the tile parameter
        this could currently fail because of the checks implemented in PlayerActionService, as solution would be either
        removing these checks or implementing the equals method for various tiles */

        //add tile to stack, so addTileToTruck always works
        currentGame.tileStack.tiles.add(tile)

        //find truck in rootService
        val currentTruck = currentGame.deliveryTrucks[truckIndex]

        aiRootService.playerActionService.addTileToTruck(tile, currentTruck)
    }

    /**
     * executes action
     */
    override fun execute(rootService: RootService) {
        val getTile = rootService.playerActionService.draw()

        // Get the current game after draw because game a new gamestate was created
        val currentGame = rootService.game ?: return

        //find truck in rootService
        val currentTruck = currentGame.deliveryTrucks[truckIndex]

        rootService.playerActionService.addTileToTruck(getTile, currentTruck)
    }

    /** Implement a custom toString method, the RootService is needed to access the current game objects */
    override fun toString(aiRootService: RootService): String {
        return "Add $tile to $truck at index $truckIndex"
    }

    /** Implement a custom short toString method, the RootService is needed to access the current game objects */
    override fun toStringShort(aiRootService: RootService): String {
        return "Draw a Tile and add it to truck at index $truckIndex (${truck.tiles})"
    }
}