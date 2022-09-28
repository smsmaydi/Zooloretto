package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * The delivery truck
 */
@Serializable
class DeliveryTruck : Cloneable<DeliveryTruck> {

    /** The tiles present in the delivery truck */
    var tiles: MutableList<Tile> = mutableListOf()

    /** How many tiles this delivery truck can hold */
    var maxSize: Int = 3

    /** If this delivery truck is alrady taken by a player */
    var isTaken: Boolean = false

    override fun clone(): DeliveryTruck {
        val newTruck = DeliveryTruck()
        newTruck.tiles = this.tiles.map { it -> (it as Cloneable<Tile>).clone() }.toMutableList()
        newTruck.maxSize = this.maxSize
        newTruck.isTaken = this.isTaken
        return newTruck
    }

    /** Add a tile to this delivery truck */
    fun addTile(tile: Tile) {
        check(this.hasSpace()) { "error the truck has no enough space" }
        this.tiles.add(tile)
    }

    /** If this delivery truck has space for another tile */
    fun hasSpace(): Boolean {
        return this.tiles.size < maxSize
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "DeliveryTruck(maxSize=$maxSize tiles=(${tiles.joinToString(",")}))"
    }
}

