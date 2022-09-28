package view

import AbstractRefreshingService
import entity.DeliveryTruck
import entity.Enclosure
import entity.Player
import entity.Tile

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {


    /**
     * Perform refreshes necessary after a new game started
     */
    fun refreshAfterGameStart() {}

    /**
     * perform refreshes after a tile has moved
     *
     * TODO pass the tiles that moves as arguments to this function
     */
    fun refreshAfterTileMoved() {}

    /**
     * Perform refreshes after a turn has ended and the next player starts his turn
     */
    fun refreshAfterNewTurn() {}

    /**
     * Perform the necessary refreshes after the current player has expanded their zoo
     */
    fun refreshAfterExpanding() {}

    /**
     * Refresh the GUI after a player bought an animal/shop tile from another player
     *
     */
    fun refreshAfterBuying(boughtTile: Tile) {}

    /**
     * Perform the necessary refreshes after a player has taken a truck
     *
     */
    fun refreshAfterTakeTruck(truck: DeliveryTruck) {}

    /**
     * Perform the necessary refreshes after the game has ended and a winner has been decided
     */
    fun refreshAfterGameEnd() {}

    /**
     * - Is called after the game state changes (on redo,undo)
     */
    fun refreshAfterGameStateChanged() {}

    /**
     * Refresh the GUI after the button has been pressed to view another players board or after the view switched back
     * to the players own board
     */
    fun refreshAfterViewOtherBoard() {}

    /**
     * is called after discard tile
     */
    fun refreshAfterDiscardTile() {}

    /**
     * Is called after a new round starts.
     */
    fun refreshAfterNewRoundStarted() {}

    /**
     * Is called after a player uses an expansion board
     */
    fun refreshAfterUsingANewExpansionBoard(player: Player) {}

    /**
     * Is called after a coin tile is added to the player from the delivery truck
     */
    fun refreshAfterCoinTileWadAddedFromTruckToPlayer(player: Player, truck: DeliveryTruck) {}

    /**
     * Is called after a tile is added to the player barn from the delivery truck
     */
    fun refreshAfterTileWasAddedFromTruckToPlayerBarn(player: Player, truck: DeliveryTruck) {}

    /**
     * - Is called after the state of the enclosure has changed
     */
    fun refreshAfterEnclosureChanged(enclosure: Enclosure) {}

    /**
     * is called after each turn
     */
    fun refreshAfterTurn() {}

    /**
     * is called whenever undo or redo is done
     */
    fun refreshAfterUndoRedo() {}

    /**
     * is called whenever a AI made a Move
     */
    fun refreshAfterAIMadeAMove(move: String) {}
}
