package entity

import Cloneable

import kotlinx.serialization.Serializable

/**
 * A tile stack
 */
@Serializable
data class TileStack(
    /** The tiles currently in this tile stack */
    var tiles: MutableList<Tile>
) : Cloneable<TileStack> {

    override fun clone(): TileStack {
        val newTiles = this.tiles.map { it -> (it as Cloneable<Tile>).clone() }.toMutableList()
        return TileStack(newTiles)
    }

    /** Get the coin tiles in this tile stack */
    fun getCoinTiles(): List<CoinTile> {
        return this.tiles.filterIsInstance<CoinTile>()
    }

    /** Get the animal tiles in this tile stack */
    fun getAnimalTiles(): List<AnimalTile> {
        return this.tiles.filterIsInstance<AnimalTile>()
    }

    /** Get the vending stall tiles in this tile stack */
    fun getVendingStalls(): List<VendingStallTile> {
        return this.tiles.filterIsInstance<VendingStallTile>()
    }

    /** Get the offspring (child) animal tiles in this tile stack */
    fun getOffsprings(): List<AnimalTile> {
        return this.getAnimalTiles().filter { animal -> animal.variant == AnimalVariant.CHILD }
    }
}