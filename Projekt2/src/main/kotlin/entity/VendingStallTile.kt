package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * A vending stall tile
 */
@Serializable
class VendingStallTile(
    /** The type of this vending stall */
    val type: VendingStallType
) : Tile(), Cloneable<VendingStallTile> {

    override fun clone(): VendingStallTile {
        return VendingStallTile(this.type)
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "VendingStallTile(type=$type)"
    }
}
