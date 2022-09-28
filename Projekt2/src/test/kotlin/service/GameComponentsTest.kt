package service

import entity.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * - Test cases for the game components
 */
class GameComponentsTest {


    /**
     * - should read the game state directory to return a list of saved games
     * */
    @Test
    fun didGameCurrentlyGroupTiles() {

        // coin
        val vendingStallsTiles = mutableListOf<VendingStallTile>()
        val animalTiles = mutableListOf<AnimalTile>()

        // 4 vending stalls types
        VendingStallType.values().forEach { type ->
            vendingStallsTiles.add(VendingStallTile(type))
        }

        AnimalType.values().forEach { type ->
            animalTiles.add(AnimalTile(AnimalVariant.MALE, type))
            animalTiles.add(AnimalTile(AnimalVariant.FEMALE, type))
            animalTiles.add(AnimalTile(AnimalVariant.NEUTRAL, type))
        }

        val tiles = mutableListOf<Tile>()
        tiles.add(CoinTile())
        tiles.addAll(vendingStallsTiles)
        tiles.addAll(animalTiles)


        val gameComponents = GameComponentsService()
        gameComponents.initForNumPlayers(5)
        val availableTiles = gameComponents.getAvailableTileTypes()
        val result = listsEqual(tiles, availableTiles)

        assertEquals(result, true)

        //for 2 player remove corresponding Tiles like in GameComponentService
        var getTile = removeTileType(AnimalType.ELEPHANT, tiles)
        tiles.removeAll(getTile.toSet())

        getTile = removeTileType(AnimalType.KANGAROO, tiles)
        tiles.removeAll(getTile.toSet())

        getTile = removeTileType(AnimalType.FLAMINGO, tiles)
        tiles.removeAll(getTile.toSet())

        gameComponents.initForNumPlayers(2)
        val availableTiles2 = gameComponents.getAvailableTileTypes()
        val result2 = listsEqual(tiles, availableTiles2)

        assertEquals(result2, true)
    }

    // removes the tile from [tileTypes]
    private fun removeTileType(type: AnimalType, tiles: MutableList<Tile>): MutableList<Tile> {
        val tilesToRemove = mutableListOf<Tile>()

        for (tile in tiles) {
            if (tile is AnimalTile) {
                if (tile.type === type) {
                    tilesToRemove.add(tile)
                }
            }
        }
        return tilesToRemove
    }

    //compare if two list hold same types
    private fun listsEqual(list1: List<Any>, list2: List<Any>): Boolean {
        if (list1.size != list2.size)
            return false

        val pairList = list1.zip(list2)

        return pairList.all { (elt1, elt2) ->
            elt1::class == elt2::class
        }
    }
}