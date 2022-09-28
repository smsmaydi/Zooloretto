package entity

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test the [Player] class, especially the [Player.calculatePoints] method
 * */
class PlayerTest {
    private var player = Player.createPlayer("Test", 1)

    /**
     * set up a Player
     */
    @BeforeEach
    fun setupPlayer() {
        player = Player.createPlayer("Test", 1)
    }

    /**
     * set up enclosures for testing
     */
    private fun setupEnclosure(enclosure: Enclosure): Enclosure {
        enclosure.biggerPoints = 10
        enclosure.smallerPoints = 6
        enclosure.vendingStallsMax = 1
        enclosure.animalsMax = 6
        enclosure.animals.add(AnimalTile(AnimalVariant.MALE, AnimalType.ELEPHANT))
        enclosure.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))
        enclosure.animals.add(AnimalTile(AnimalVariant.CHILD, AnimalType.ELEPHANT))
        enclosure.animals.add(AnimalTile(AnimalVariant.MALE, AnimalType.ELEPHANT))
        return enclosure
    }

    /**
     * test Calc Points not full
     */
    @Test
    fun testCalculatePointsNotFull() {
        val enclosure = setupEnclosure(Enclosure())
        player.enclosures.add(enclosure)

        // No VendingStall so no points
        assertEquals(
            0, player.calculatePoints(),
            "Wrong points for when enclosure is not full and there is no VendingStall"
        )

        enclosure.vendingStalls.add(VendingStallTile(VendingStallType.V1))

        // 4 Animals + 1 VendingStall --> 6 Points
        assertEquals(
            6, player.calculatePoints(),
            "Wrong points for when enclosure is not full and there is a VendingStall"
        )
    }

    /**
     * test Calc Points nearly full
     */
    @Test
    fun testCalculatePointsNearlyFull() {
        val enclosure = setupEnclosure(Enclosure())
        player.enclosures.add(enclosure)
        enclosure.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))

        // Only one tile missing
        assertEquals(
            enclosure.smallerPoints,
            player.calculatePoints(),
            "Wrong points for when enclosure is nearly full"
        )
    }

    /**
     * test Calc Points full
     */
    @Test
    fun testCalculatePointsFull() {
        val enclosure = setupEnclosure(Enclosure())
        player.enclosures.add(enclosure)
        enclosure.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))
        enclosure.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))

        assertEquals(
            enclosure.biggerPoints, player.calculatePoints(), "Wrong points for when enclosure " +
                    "is full"
        )
    }

    /**
     * test Calc Points penalties
     */
    @Test
    fun testCalculatePointsBarnPenalties() {
        val enclosure = setupEnclosure(Enclosure())
        player.enclosures.add(enclosure)
        enclosure.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))
        enclosure.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))

        player.barn.animals.add(AnimalTile(AnimalVariant.NEUTRAL, AnimalType.FLAMINGO))
        player.barn.animals.add(AnimalTile(AnimalVariant.NEUTRAL, AnimalType.LEOPARD))
        player.barn.vendingStalls.add(VendingStallTile(VendingStallType.V1))

        // Each animal type and each vending stall type results in 2 points penalty --> 6 points penalty
        assertEquals(enclosure.biggerPoints - 6, player.calculatePoints(), "Barn penalty calculation is wrong")
    }

    /**
     * test Calc Points expansionBoards
     */
    @Test
    fun testCalculatePointsExpansionBoard() {
        val expansionBoard = setupEnclosure(ExpansionBoard()) as ExpansionBoard
        expansionBoard.animals.add(AnimalTile(AnimalVariant.FEMALE, AnimalType.ELEPHANT))
        player.expansionBoards.add(expansionBoard)

        // Expansion Board is disabled so the board should not count
        expansionBoard.isUsed = false
        assertEquals(0, player.calculatePoints(), "Expansion Board is evaluated although is is disabled")

        // Enable Expansion Board
        expansionBoard.isUsed = true
        assertEquals(
            expansionBoard.smallerPoints,
            player.calculatePoints(),
            "Expansion Board is not evaluated although it is enabled"
        )
    }
}