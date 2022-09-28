package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * A coin tile
 */
@Serializable
class CoinTile : Tile(), Cloneable<CoinTile> {

    /** Return a clone (deep copy) of this object */
    override fun clone(): CoinTile {
        return CoinTile()
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "CoinTile"
    }
}
