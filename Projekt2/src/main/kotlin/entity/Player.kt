package entity

import Cloneable

import kotlinx.serialization.Serializable


/**
 * A base player
 */
@Serializable
sealed class PlayerBase {
    /** The name of the player */
    abstract var name: String

    /** Has the player already received a hint */
    abstract var receivedHint: Boolean

    /** Has the player already taken a delivery truck (in this round) */
    abstract var tookDeliveryTruck: Boolean

    /** The amount of coins the player has */
    abstract var numCoins: Int

    /** The enclosures the player has */
    abstract var enclosures: MutableList<Enclosure>

    /** The expansion boards the player has */
    abstract var expansionBoards: MutableList<ExpansionBoard>

    /** When a player has taken a delivery truck it is referenced here */
    abstract var deliveryTruck: DeliveryTruck?

    /** The barn of the player */
    abstract var barn: Barn

    /**
     * - calculate the points for this player
     */
    abstract fun calculatePoints(): Int

    /**
     * - gets the active expansion boards
     */
    abstract fun getUsedExpansionBoards(): MutableList<ExpansionBoard>

    /**
     * - gets the enclosures and any used expansion boards
     */
    abstract fun getEnclosuresWithUsedExpansionBoards(): MutableList<Enclosure>

    /** Take a truck */
    abstract fun takeTruck(truck: DeliveryTruck): Unit

}


/**
 * The player
 */
@Serializable
open class Player() : PlayerBase(), Cloneable<Player> {

    /** The name of the player */
    override var name: String = "PLayer"

    /** Has the player received a hint */
    override var receivedHint: Boolean = false

    /** Has the player taken a delivery truck */
    override var tookDeliveryTruck: Boolean = false

    /** How many coins the player */
    override var numCoins: Int = 2 // each player gets 2 coins at the beginning

    /** The enclosures the player has */
    override var enclosures: MutableList<Enclosure> = mutableListOf()

    /** The expansion boards the player has */
    override var expansionBoards: MutableList<ExpansionBoard> = mutableListOf()

    /** The delivery truck the player has */
    override var deliveryTruck: DeliveryTruck? = null

    /** The barn the player has */
    override var barn: Barn = Barn()


    /**
     * - returns the number of different vending stall types that this player has
     */
    private fun getNumberOfVendingStallTypes(vendingStalls: List<VendingStallTile>): Int {
        return vendingStalls.groupBy { it.type }.size
    }

    /**
     * - returns the number of different animal tiles that this player has
     */
    private fun getNumberOfAnimalTypes(animalTiles: List<AnimalTile>): Int {
        return animalTiles.groupBy { it.type }.size
    }

    /**
     * - returns the number of different animal tiles that this player has
     */
    override fun takeTruck(truck: DeliveryTruck) {
        truck.isTaken = true
        this.tookDeliveryTruck = true
        this.deliveryTruck = truck
    }


    /**
     * - calculate the points for this player
     */
    override fun calculatePoints(): Int {
        var points = 0

        // points of the enclosures
        for (enc in this.enclosures) {
            points += enc.calculatePoints()
        }

        // points of the expansion boards
        for (board in this.expansionBoards.filter { board -> board.isUsed }) {
            points += board.calculatePoints()
        }


        //  For each vending stall type on stall spaces, the player receives 2 points.
        val vendingStallsInEnclosures =
            this.getEnclosuresWithUsedExpansionBoards().map { enc -> enc.vendingStalls }.flatten()
        points += this.getNumberOfVendingStallTypes(vendingStallsInEnclosures) * 2

        // For each vending stall type in his barn, the player receives minus 2 points.
        points -= this.getNumberOfVendingStallTypes(this.barn.vendingStalls) * 2

        // For each animal type in his barn, the player receives minus 2 points.
        points -= this.getNumberOfAnimalTypes(this.barn.animals) * 2

        return points
    }


    /**
     * - gets the active expansion boards
     */
    override fun getUsedExpansionBoards(): MutableList<ExpansionBoard> {
        return this.expansionBoards.filter { it.isUsed }.toMutableList()
    }

    /**
     * - gets the enclosures and any used expansion boards
     */
    override fun getEnclosuresWithUsedExpansionBoards(): MutableList<Enclosure> {
        return (this.enclosures + this.expansionBoards.filter { it.isUsed }).toMutableList()
    }


    override fun clone(): Player {
        val newPlayer = Player()
        newPlayer.name = this.name
        newPlayer.receivedHint = this.receivedHint
        newPlayer.tookDeliveryTruck = this.tookDeliveryTruck
        newPlayer.numCoins = this.numCoins
        newPlayer.enclosures = this.enclosures.map { it.clone() }.toMutableList()
        newPlayer.expansionBoards = this.expansionBoards.map { it.clone() }.toMutableList()
        newPlayer.deliveryTruck = this.deliveryTruck?.clone()
        newPlayer.barn = this.barn.clone()
        return newPlayer
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "Player(name=$name receivedHint=$receivedHint tookDeliveryTruck=$tookDeliveryTruck coins=$numCoins " +
                "enclosures=(${enclosures.joinToString(",")}) expansionBoards=" +
                "(${expansionBoards.joinToString(",")}) barn=$barn deliveryTruck=$deliveryTruck)"
    }


    /**
     * A factory for the player
     */
    companion object Factory {

        /**
         * - creates a player based on the game rules
         * - the game rules differ based on the number of players
         * - name is the name of the player
         * - numPlayersInGame is the number of players in the game where the player is used
         */
        fun createPlayer(name: String, numPlayersInGame: Int): Player {
            val player = Player()
            player.name = name

            // Add the expansion boards
            player.expansionBoards.add(ExpansionBoard())
            if (numPlayersInGame == 2) {
                player.expansionBoards.add(ExpansionBoard())
            }

            // Add the enclosures
            player.enclosures.add(Enclosure.createBasedOnNumAnimals(4))
            player.enclosures.add(Enclosure.createBasedOnNumAnimals(5))
            player.enclosures.add(Enclosure.createBasedOnNumAnimals(6))

            return player
        }

    }
}
