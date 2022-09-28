package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * A marker tile
 */
@Serializable
class MarkerTile : Tile(), Cloneable<MarkerTile> {

    override fun clone(): MarkerTile {
        return MarkerTile()
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "MarkerTile"
    }
}
