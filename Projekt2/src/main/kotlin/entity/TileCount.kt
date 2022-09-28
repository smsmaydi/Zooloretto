package entity

import Cloneable
import kotlinx.serialization.Serializable
import kotlin.math.max

/**
 * This class keeps a statistic of all drawn cards and
 * calculates the probability of the next [Tile] on the drawStack
 * this is used in the AIActionService to prioritize the decisions
 */
@Serializable
class TileCount : Cloneable<TileCount> {
    /** Inner data class to store information about the Tile the amount of the tiles (total and currently) */
    @Serializable
    class TileCountEntry(
        /** The tile of which its amount is tracked */
        var tile: Tile,
        /** The amount of the tile that was in the stack at the start */
        var totalCount: Int,
        /** The amount of the tile currently in the stack */
        var currentCount: Int
    ) : Cloneable<TileCountEntry> {

        /** Clone this TileCountEntry and return it */
        override fun clone(): TileCountEntry {
            return TileCountEntry(this.tile, this.totalCount, this.currentCount)
        }
    }

    /** The amount of cards currently in the card stack, this is the same as summing the currentCount attribute
     * of all TileCountEntry in entries */
    var numberOfTiles = 0

    /** The cards currently in the card stack*/
    private var entries: MutableList<TileCountEntry> = mutableListOf()

    private fun checkSameClass(tileA: Tile, tileB: Tile): Boolean {
        return (tileA is AnimalTile && tileB is AnimalTile && tileA.type == tileB.type) ||
                (tileA is VendingStallTile && tileB is VendingStallTile && tileA.type == tileB.type) ||
                (tileA is CoinTile && tileB is CoinTile)
    }

    private fun getProbability(entry: TileCountEntry): Double {
        return entry.currentCount / numberOfTiles.toDouble()
    }

    private fun sortEntries() {
        entries.sortedByDescending { getProbability(it) }
    }

    /** draws a card and removes it from the draw stack */
    fun draw(tile: Tile) {
        val entry = entries.firstNotNullOfOrNull { if (checkSameClass(it.tile, tile)) it else null }
        if (entry != null) {
            entry.currentCount = max(0, entry.currentCount - 1)
            numberOfTiles--
            assert(numberOfTiles >= 0) { "numberOfTiles is negative" }
            sortEntries()
        } else {
            // this should never happen
        }
    }

    /** adds a card to the draw stack, this will only be needed at the start of the game */
    fun add(tile: Tile) {
        val clonedTile = (tile as Cloneable<Tile>).clone()
        val entry = entries.firstNotNullOfOrNull { if (checkSameClass(it.tile, clonedTile)) it else null }
        if (entry != null) {
            entry.totalCount++
            entry.currentCount++
        } else {
            entries.add(TileCountEntry(clonedTile, 1, 1))
        }
        numberOfTiles++
        sortEntries()
    }

    /** adds multiple cards to the draw stack, this will only be needed at the start of the game */
    fun add(tiles: MutableList<Tile>) {
        for (tile in tiles)
            add(tile)
    }

    /** returns the probability of a given tile type to be currently in the draw stack */
    fun getProbability(tile: Tile): Double {
        val entry = entries.firstNotNullOfOrNull { if (checkSameClass(it.tile, tile)) it else null }
        return if (entry != null)
            getProbability(entry)
        else
            0.0
    }

    /** Get the probabilities of all the tiles in descending order as a map */
    fun getProbabilities(): MutableList<Pair<Tile, Double>> {
        val probabilities = mutableListOf<Pair<Tile, Double>>()
        for (entry in entries)
            probabilities.add(entry.tile to getProbability(entry))

        return probabilities
    }

    /** Clear the managed entries */
    fun clear() {
        entries.clear()
        numberOfTiles = 0
    }

    /** create a clone of this object which includes a deep copy of its attributes and return it */
    override fun clone(): TileCount {
        val newTileCount = TileCount()
        newTileCount.numberOfTiles = numberOfTiles
        newTileCount.entries = mutableListOf()
        for (entry in this.entries)
            newTileCount.entries.add(entry.clone())

        return newTileCount
    }
}