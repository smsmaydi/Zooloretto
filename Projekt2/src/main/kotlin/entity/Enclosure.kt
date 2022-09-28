package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * The enclosure
 */
@Serializable
open class Enclosure : Cloneable<Enclosure> {

    /** The bigger points value on an enclosure board */
    var biggerPoints: Int = 0

    /** The smaller points value on an enclosure board */
    var smallerPoints: Int = 0

    /** The amount of coins a player gets if he fills the enclosure */
    var coinsToGetWhenFull: Int = 0

    /** The maximum amount of vending stall spaces in this enclosure */
    var vendingStallsMax: Int = 0

    /** The maximum amount of animals spaces in this enclosure */
    var animalsMax: Int = 0

    /** The animals currently in this enclosure */
    var animals: MutableList<AnimalTile> = mutableListOf()

    /** The vending stalls currently in this enclosure */
    var vendingStalls: MutableList<VendingStallTile> = mutableListOf()

    override fun clone(): Enclosure {
        val newEnclosure = Enclosure()
        newEnclosure.biggerPoints = this.biggerPoints
        newEnclosure.smallerPoints = this.smallerPoints
        newEnclosure.coinsToGetWhenFull = this.coinsToGetWhenFull
        newEnclosure.vendingStallsMax = this.vendingStallsMax
        newEnclosure.animalsMax = this.animalsMax
        newEnclosure.animals = this.animals.map { it.clone() }.toMutableList()
        newEnclosure.vendingStalls = this.vendingStalls.map { it.clone() }.toMutableList()
        return newEnclosure
    }


    /**
     * - returns the number of empty spaces in that enclosure
     */
    private fun getNumberOfEmptySpaces(): Int {
        return (animalsMax + vendingStallsMax) - (this.animals.size + this.vendingStalls.size)
    }


    /**
     * - returns true if there are empty spaces
     */
    fun hasEmptySpaces(): Boolean {
        return this.getNumberOfEmptySpaces() > 0
    }

    /**
     * - returns true if there is space for an animal
     */
    fun hasSpaceForAnimal(): Boolean {
        return this.animals.size < this.animalsMax
    }


    /**
     * - returns true if there is space for a vending stall
     */
    fun hasSpaceForVendingStall(): Boolean {
        return this.vendingStalls.size < this.vendingStallsMax
    }

    /**
     * - returns the animal type that this enclosure has. An enclosure can have one animal type
     * - throws error if there are no animals yet in the enclosure
     */
    fun getAnimalType(): AnimalType {
        check(this.animals.size > 0) { "there are no animals in this enclosure" }
        return this.animals[0].type
    }

    /**
     *  returns the number of Animal empty spaces in that enclosure
     */
    private fun getNumberOfEmptyAnimalSpaces(): Int {
        return animalsMax - this.animals.size
    }

    /**
     * returns true if there are Animal empty spaces
     */
    private fun hasEmptyAnimalSpaces(): Boolean {
        return getNumberOfEmptyAnimalSpaces() > 0
    }

    /**
     * - calculate the points for this enclosure
     */
    fun calculatePoints(): Int {
        var points = 0

        // Two point values are shown in each enclosure. For an enclosure with no
        //empty spaces, the player receives the larger of the two values.
        //For an enclosure with one empty space, the player receives the smaller of
        //the two values.
        if (!this.hasEmptyAnimalSpaces()) {
            points += this.biggerPoints
        } else if (this.getNumberOfEmptyAnimalSpaces() == 1) {
            points += this.smallerPoints
        } else if (this.getNumberOfEmptyAnimalSpaces() > 1) {
            //For an enclosure with two or more empty spaces, the player only receives points
            //if he has a vending stall on the stall space associated with the enclosure.
            //In this case, the player receives 1 point for each animal in the enclosure.
            // Note: Even if both stall spaces next to a 4-space enclosure are occupied, a player still
            //only receives 1 point per tile
            if (this.vendingStalls.size > 0) {
                points += this.animals.size
            }

        }

        return points
    }


    /**
     * A factory for the enclosure
     */
    companion object Factory {

        /**
         *  - creates an Enclosure and sets the variables based on the number of animals
         *  - these initial variables are set based on the game rules
         */
        fun createBasedOnNumAnimals(numAnimals: Int): Enclosure {
            check(numAnimals in 4..6) { "an enclosure can have either 4,5 or 6 animals. animals count: $numAnimals" }

            val enclosure = Enclosure()
            when (numAnimals) {
                4 -> {
                    enclosure.biggerPoints = 5
                    enclosure.smallerPoints = 4
                    enclosure.coinsToGetWhenFull = 1
                    enclosure.animalsMax = 4
                    enclosure.vendingStallsMax = 2
                }
                5 -> {
                    enclosure.biggerPoints = 8
                    enclosure.smallerPoints = 5
                    enclosure.coinsToGetWhenFull = 2
                    enclosure.animalsMax = 5
                    enclosure.vendingStallsMax = 1
                }
                else -> {
                    enclosure.biggerPoints = 10
                    enclosure.smallerPoints = 6
                    enclosure.coinsToGetWhenFull = 0
                    enclosure.animalsMax = 6
                    enclosure.vendingStallsMax = 1
                }
            }
            return enclosure
        }
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "Enclosure(points=$biggerPoints/$smallerPoints coins=$coinsToGetWhenFull " +
                "vendingStallCapacity=$vendingStallsMax animalCapacity=$animalsMax " +
                "animals=(${animals.joinToString(",")}) vendingStalls=(${vendingStalls.joinToString(",")}))"
    }
}
