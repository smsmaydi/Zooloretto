package entity

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/** Test cases for the [TileCount] */
class TileCountTest {
    /**
     * The [TileCount] that is tested with this test class
     * 3 * 12 Animals (Camel, Elephant, Leopard)
     * 12 CoinTiles
     * 2 * 4 Vendingstalls
     * == 56
     */
    private var tileCount: TileCount = TileCount()

    /** resets tileCount for every test */
    @BeforeEach
    fun loadCompareImage() {
        tileCount = TileCount()
        for(i in 1..12) {
            tileCount.add(AnimalTile(type = AnimalType.CAMEL, variant = AnimalVariant.NEUTRAL))
            tileCount.add(AnimalTile(type = AnimalType.ELEPHANT, variant = AnimalVariant.NEUTRAL))
            tileCount.add(AnimalTile(type = AnimalType.LEOPARD, variant = AnimalVariant.NEUTRAL))
            tileCount.add(CoinTile())
        }
        for(i in 1..4) {
            tileCount.add(VendingStallTile(type = VendingStallType.V1))
            tileCount.add(VendingStallTile(type = VendingStallType.V2))
        }
        assertEquals(56, tileCount.numberOfTiles)
    }

    /** Tests if statistics get calculated correctly */
    @Test
    fun testCalcStat() {
        // 2/13 Camel, 1/13 Elephant, 1/13 Leopard, 3/13 CoinTile, 1/3 VendingStall V1, 1/3 VendingStall V2
        tileCount.draw(AnimalTile(type = AnimalType.CAMEL, variant = AnimalVariant.NEUTRAL))
        tileCount.draw(AnimalTile(type = AnimalType.CAMEL, variant = AnimalVariant.NEUTRAL))
        tileCount.draw(AnimalTile(type = AnimalType.ELEPHANT, variant = AnimalVariant.NEUTRAL))
        tileCount.draw(AnimalTile(type = AnimalType.LEOPARD, variant = AnimalVariant.NEUTRAL))
        tileCount.draw(CoinTile())
        tileCount.draw(CoinTile())
        tileCount.draw(CoinTile())
        tileCount.draw(VendingStallTile(type = VendingStallType.V1))
        tileCount.draw(VendingStallTile(type = VendingStallType.V2))

        // +1 CoinTile => 4/12 CoinTile
        tileCount.draw(CoinTile())

        // there has to be 12-4= 8 Coins left
        val statOfCoin: Double = 8 / tileCount.numberOfTiles.toDouble()
        assertEquals(statOfCoin, tileCount.getProbability(CoinTile()))
    }


    /** Tests if statistics get calculated correctly */
    @Test
    fun testAddMoreThanAreAvailableError() {
        //draw 12 coinTiles
        for (i in 1..12)
            tileCount.draw(CoinTile())

        // there should be 0 Coins left in Stack a probability should be 0
        assertEquals(0.0, tileCount.getProbability(CoinTile()))

        // remove one Coin that is not in the Stack
        // assertFailsWith<IllegalStateException>{ tileCount.add(CoinTile())}
    }


}