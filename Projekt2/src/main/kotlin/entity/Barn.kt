package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * The barn
 */
@Serializable
class Barn : Cloneable<Barn> {

    /** The animal tiles currently in the barn */
    var animals: MutableList<AnimalTile> = mutableListOf()

    /** The vending stall tiles currently in the barn */
    var vendingStalls: MutableList<VendingStallTile> = mutableListOf()

    /**
     * Returns the amount on a specific animal type in currently in the barn
     */
    fun getNumberOfTypeInBarn(type: AnimalType): Int {
        return animals.filter { it.type == type }.size
    }

    /** Return a clone (deep copy) of this object */
    override fun clone(): Barn {
        val newBarn = Barn()
        newBarn.animals = this.animals.map { it.clone() }.toMutableList()
        newBarn.vendingStalls = this.vendingStalls.map { it.clone() }.toMutableList()
        return newBarn
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "Barn(animals=(${animals.joinToString(",")}) vendingStalls=(${vendingStalls.joinToString(",")}))"
    }
}
