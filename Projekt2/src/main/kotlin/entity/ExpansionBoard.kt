package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * The expansion board
 */
@Serializable
class ExpansionBoard : Enclosure(), Cloneable<Enclosure> {

    /** If the expansion board is used / bas been bought by the player */
    var isUsed: Boolean = false

    init {
        // these are set based on the game rules
        this.biggerPoints = 9
        this.smallerPoints = 5
        this.animalsMax = 5
        this.vendingStallsMax = 1
        this.coinsToGetWhenFull = 1
    }

    override fun clone(): ExpansionBoard {
        val newExpansionBoard = ExpansionBoard()
        newExpansionBoard.biggerPoints = this.biggerPoints
        newExpansionBoard.smallerPoints = this.smallerPoints
        newExpansionBoard.coinsToGetWhenFull = this.coinsToGetWhenFull
        newExpansionBoard.vendingStallsMax = this.vendingStallsMax
        newExpansionBoard.animalsMax = this.animalsMax
        newExpansionBoard.animals = this.animals.map { it.clone() }.toMutableList()
        newExpansionBoard.vendingStalls = this.vendingStalls.map { it.clone() }.toMutableList()
        newExpansionBoard.isUsed = this.isUsed
        return newExpansionBoard
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "ExpansionBoard(points=$biggerPoints/$smallerPoints coins=$coinsToGetWhenFull " +
                "vendingStallCapacity=$vendingStallsMax) animalCapacity=$animalsMax " +
                "animals=(${animals.joinToString(" ")}) vendingStalls=(${vendingStalls.joinToString(" ")}))"
    }
}
