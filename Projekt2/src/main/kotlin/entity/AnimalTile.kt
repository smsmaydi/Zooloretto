package entity

import Cloneable
import kotlinx.serialization.Serializable

/**
 * An animal tile
 */
@Serializable
class AnimalTile(
    /** The variant (female, male, ...) of the animal */
    val variant: AnimalVariant,
    /** The type of the animal (zebra, ...) */
    val type: AnimalType,
    /** If the animal has already mated */
    var hasMated: Boolean = false
) :
    Tile(), Cloneable<AnimalTile> {

    /** Return a clone (deep copy) of this object */
    override fun clone(): AnimalTile {
        return AnimalTile(this.variant, this.type, this.hasMated)
    }

    /** Implement a custom toString method */
    override fun toString(): String {
        return "AnimalTile(variant=$variant type=$type hasMated=$hasMated)"
    }
}
