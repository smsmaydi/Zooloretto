package  service

import entity.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull


/**
 * a shuffler that does nothing.  just returns the content as is
 */
class NoopShuffler<T> : Shuffler<T> {
    /**
     * - don't shuffle. in the tests we don't want random stuff. we need to be able to control what happens
     */
    override fun shuffle(things: MutableList<T>): MutableList<T> {
        return things
    }

}

// creates a player based on the game rules
// the game rules differ based on the number of players
private fun createPlayer(name: String, numPlayers: Int): Player {
    val player = Player.createPlayer(name, numPlayers)
    return player
}

/**
 * get Animal Tile With Type
 */
fun getAnimalTileWithType(tiles: MutableList<Tile>, type: AnimalType): AnimalTile {
    return tiles.filter { tile -> tile is AnimalTile && tile.type == type }[0] as AnimalTile
}

/**
 * get Animal Tile with Type
 */
fun getAnimalTilesWithType(tiles: MutableList<Tile>, type: AnimalType): MutableList<AnimalTile> {
    return tiles.filter { tile -> tile is AnimalTile && tile.type == type }.toMutableList() as MutableList<AnimalTile>
}

/**
 * get vending stall Tile with type
 */
fun getVendingStallTileWithType(tiles: MutableList<Tile>, type: VendingStallType): VendingStallTile {
    return tiles.filter { it -> it is VendingStallTile && it.type == type }[0] as VendingStallTile
}

/**
 * create variable amount of Players
 */
fun createFromNumPlayers(numPlayers: Int): RootService {
    val root = RootService()
    root.gameComponentsService.setTilesShuffler(NoopShuffler())
    root.gameService.startGame(List(numPlayers) { index -> createPlayer("p${index + 1}", numPlayers) })
    return root
}


/**
 * - Test cases for the player action service
 */
class PlayerActionServiceTest {

    /**
     * - should switch the active player after a player makes an action
     * */
    @Test
    fun shouldSwitchActivePlayerAfterAction() {
        val root = createFromNumPlayers(3)
        assertEquals("p1", root.game!!.getActivePlayer().name)
        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])
        assertEquals("p2", root.game!!.getActivePlayer().name)
    }

    /**
     * - should skip a player if the player took a truck
     * */
    @Test
    fun shouldSkipAPlayerIfTookATruck() {
        val root = createFromNumPlayers(3)
        assertEquals("p1", root.game!!.getActivePlayer().name)
        root.game!!.players[1].tookDeliveryTruck = true
        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])
        assertEquals("p3", root.game!!.getActivePlayer().name)
    }

    /**
     * - should have the same player as the active player if all other players took delivery trucks
     * */
    @Test
    fun shouldHaveSamePlayerAsActivePlayerIfAllOthersTookTrucks() {
        val root = createFromNumPlayers(3)
        assertEquals("p1", root.game!!.getActivePlayer().name)
        assertEquals("p1", root.game!!.getActivePlayer().name)
        root.game!!.players[1].tookDeliveryTruck = true
        root.game!!.players[2].tookDeliveryTruck = true
        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])
        assertEquals("p1", root.game!!.getActivePlayer().name)
    }

    /**
     * - should take a truck and put all of its tiles in the zoo
     * */
    @Test
    fun shouldTakeTruckAndPutAllTilesOnZoo() {
        val root = createFromNumPlayers(3)
        assertEquals("p1", root.game!!.getActivePlayer().name)

        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])
        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])
        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])

        assertEquals(3, root.game!!.deliveryTrucks[0].tiles.size)

        root.playerActionService.takeTruck(root.game!!.deliveryTrucks[0])

        assertNotNull(root.game!!.players[0].deliveryTruck)
        assertEquals(true, root.game!!.players[0].tookDeliveryTruck)
        assertEquals(true, root.game!!.deliveryTrucks[0].isTaken)
    }


    /**
     * - should not take an empty truck
     * */
    @Test
    fun shouldNotTakEmptyTruck() {
        val root = createFromNumPlayers(3)

        assertEquals(0, root.game!!.deliveryTrucks[0].tiles.size)


        val exception =
            assertFailsWith<Exception> {
                root.playerActionService.takeTruck(root.game!!.deliveryTrucks[0])
            }

        assertEquals(
            "cannot take a truck with no tiles",
            exception.message
        )

    }


    /**
     * - should set the last player to take a truck in the game
     * */
    @Test
    fun shouldSetThatLastPlayerToTakeATruckInTheGame() {
        val root = createFromNumPlayers(3)
        root.playerActionService.addTileToTruck(root.game!!.tileStack.tiles[0], root.game!!.deliveryTrucks[0])
        root.playerActionService.takeTruck(root.game!!.deliveryTrucks[0])
        assertEquals(
            1,
            root.game!!.lastPlayerToTakeDeliveryTruckIndex
        )

    }

    /**
     * - should not move animal tile from barn to enclosure if the barn is empty
     * */
    @Test
    fun shouldNotMoveAnimalTileFromBarnToEnclosureIfTheBarnIsEmpty() {
        val root = createFromNumPlayers(5)
        val tileToMove = getAnimalTileWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)

        val exception = assertFailsWith<Exception> {
            root.playerActionService.moveAnimalTileFromBarnToEnclosure(tileToMove, root.game!!.players[0].enclosures[0])
        }
        assertEquals("the barn is empty", exception.message)


    }


    /**
     * - shouldNotMoveAnimalTileFromBarnToEnclosureIfHaveDifferentTypes
     * */
    @Test
    fun shouldNotMoveAnimalTileFromBarnToEnclosureIfHaveDifferentTypes() {
        val root = createFromNumPlayers(5)

        val kangarooTile = getAnimalTileWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val flamingoTile = getAnimalTileWithType(root.game!!.tileStack.tiles, AnimalType.FLAMINGO)

        root.game!!.players[0].enclosures[0].animals.add(flamingoTile) // enc 0 has flamingos
        root.game!!.players[0].barn.animals.add(kangarooTile) // barn has elephants


        val exception = assertFailsWith<Exception> {
            root.playerActionService.moveAnimalTileFromBarnToEnclosure(
                kangarooTile,
                root.game!!.players[0].enclosures[0]
            )
        }
        assertEquals("the destination enclosure can only contain animals of the type FLAMINGO", exception.message)


    }


    /**
     * - shouldMoveAnimalTileFromBarnToEnclosureWhenValidConditions
     * */
    @Test
    fun shouldMoveAnimalTileFromBarnToEnclosureWhenValidConditions() {
        val root = createFromNumPlayers(5)

        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)

        root.game!!.players[0].enclosures[0].animals.add(
            elephants[0]
        ) // enc 0 has one elephant
        root.game!!.players[0].barn.animals.add(
            elephants[1]
        )

        root.playerActionService.moveAnimalTileFromBarnToEnclosure(
            elephants[1],
            root.game!!.players[0].enclosures[0]
        )

        assertEquals(2, root.game!!.players[0].enclosures[0].animals.size)
        assertEquals(AnimalType.ELEPHANT, root.game!!.players[0].enclosures[0].getAnimalType())

    }


    /**
     * - shouldProduceAnOffspringWhenAFemaleIsAddedToAnEnclosureWithMaleThatHasNotMatedYet
     * */
    @Test
    fun shouldProduceAnOffspringWhenAFemaleIsAddedToAnEnclosureWithMaleThatHasNotMatedYet() {
        val root = createFromNumPlayers(5)

        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val males = elephants
            .filter { k -> k.variant == AnimalVariant.MALE }
        val females = elephants
            .filter { k -> k.variant == AnimalVariant.FEMALE }

        root.game!!.players[0].enclosures[0].animals.add(males[0]) // enc has one male
        root.game!!.players[0].barn.animals.add(females[0]) // barn has one female


        root.playerActionService.moveAnimalTileFromBarnToEnclosure(
            females[0],
            root.game!!.players[0].enclosures[0]
        ) // a female is added to the enclosure with a male. an offspring should be produced

        assertEquals(3, root.game!!.players[0].enclosures[0].animals.size)
        assertEquals(AnimalVariant.CHILD, root.game!!.players[0].enclosures[0].animals[2].variant)
        assertEquals(true, root.game!!.players[0].enclosures[0].animals[0].hasMated) // male has mated
        assertEquals(true, root.game!!.players[0].enclosures[0].animals[1].hasMated) // female has mated

    }

    /**
     * - shouldNotProduceAnOffspringWhenAFemaleIsAddedToAnEnclosureWithMaleThatHasMatedAlready
     * */
    @Test
    fun shouldNotProduceAnOffspringWhenAFemaleIsAddedToAnEnclosureWithMaleThatHasMatedAlready() {
        val root = createFromNumPlayers(5)

        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val males = elephants
            .filter { k -> k.variant == AnimalVariant.MALE }
        val females = elephants
            .filter { k -> k.variant == AnimalVariant.FEMALE }

        males[0].hasMated = true // the male has mated
        root.game!!.players[0].enclosures[0].animals.add(males[0]) // enc has one male which has mated
        root.game!!.players[0].barn.animals.add(females[0]) // barn has one female which has not yet mated


        root.playerActionService.moveAnimalTileFromBarnToEnclosure(
            females[0],
            root.game!!.players[0].enclosures[0]
        ) // a female is added to the enclosure but no offspring is created.

        assertEquals(2, root.game!!.players[0].enclosures[0].animals.size)
        assertEquals(true, root.game!!.players[0].enclosures[0].animals[0].hasMated) // male has mated
        assertEquals(false, root.game!!.players[0].enclosures[0].animals[1].hasMated) // female has not mated

    }

    /**
     * - shouldGivePlayerBonusPointsIfAnimalTileWasAddedToLastSpaceInEnclosure
     * */
    @Test
    fun shouldGivePlayerBonusPointsIfAnimalTileWasAddedToLastSpaceInEnclosure() {
        val root = createFromNumPlayers(5)

        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val neutrals = elephants
            .filter { k -> k.variant == AnimalVariant.NEUTRAL }

        val enc = root.game!!.players[0].enclosures[0] // enc with animalsMax=4 vendingStallMax=2

        enc.animals.add(neutrals[0])
        enc.animals.add(neutrals[1])
        enc.animals.add(neutrals[3]) // one free space for animal


        root.game!!.players[0].barn.animals.add(neutrals[4]) // the last tile that would be moved to the enc

        val vendingStalls = root.game!!.tileStack.getVendingStalls()
        enc.vendingStalls.add(vendingStalls[0])
        enc.vendingStalls.add(vendingStalls[1])

        root.playerActionService.moveAnimalTileFromBarnToEnclosure(
            neutrals[4],
            root.game!!.players[0].enclosures[0]
        )

        assertEquals(
            root.game!!.players[0].enclosures[0].animalsMax,
            root.game!!.players[0].enclosures[0].animals.size
        )
        assertEquals(
            root.game!!.players[0].enclosures[0].vendingStallsMax,
            root.game!!.players[0].enclosures[0].vendingStalls.size
        )

        assertEquals(
            2 - 1 + root.game!!.players[0].enclosures[0].coinsToGetWhenFull,
            root.game!!.players[0].numCoins
        ) // (2 -> first coins a player gets) (1 -> action cost)
    }

    /**
     * - shouldNotExpandZooWhenHaveNoEnoughMoney
     * */
    @Test
    fun shouldNotExpandZooWhenHaveNoEnoughMoney() {
        val root = createFromNumPlayers(3)

        val exception = assertFailsWith<Exception> {
            root.playerActionService.expandZoo()
        }
        assertEquals(
            "the player cannot choose this action. the action requires 3 coins and the player has 2",
            exception.message
        )


    }

    /**
     * - shouldExpandZooWhenHavingMoney
     * */
    @Test
    fun shouldExpandZooWhenHavingMoney() {
        val root = createFromNumPlayers(3)
        root.game!!.players[0].numCoins = 4
        root.playerActionService.expandZoo()
        assertEquals(true, root.game!!.players[0].expansionBoards[0].isUsed)
        assertEquals(1, root.game!!.players[0].numCoins)

    }

    /**
     * - shouldNotExpandAZooWhenAlreadyExpanded
     * */
    @Test
    fun shouldNotExpandAZooWhenAlreadyExpanded() {
        val root = createFromNumPlayers(3)
        root.game!!.players[0].numCoins = 10

        root.playerActionService.expandZoo()
        root.game!!.activePlayerIndex = 0

        assertEquals(true, root.game!!.players[0].expansionBoards[0].isUsed)
        assertEquals(7, root.game!!.players[0].numCoins)


        val exception = assertFailsWith<Exception> {
            root.playerActionService.expandZoo() // expand again
        }
        assertEquals("the player has already used all of the expansion boards", exception.message)


    }

    /**
     * - shouldBeAbleToExpandZooTwiceInTwoMode
     * */
    @Test
    fun shouldBeAbleToExpandZooTwiceInTwoMode() {
        val root = createFromNumPlayers(2)
        root.game!!.players[0].numCoins = 10

        root.playerActionService.expandZoo()

        root.game!!.activePlayerIndex = 0 // make first player play again

        root.playerActionService.expandZoo()

        assertEquals(true, root.game!!.players[0].expansionBoards[0].isUsed)
        assertEquals(true, root.game!!.players[0].expansionBoards[1].isUsed)

        assertEquals(4, root.game!!.players[0].numCoins)

    }


    /**
     * - shouldDiscardTileFromBarn
     * */
    @Test
    fun shouldDiscardAnimalTileFromBarn() {
        val root = createFromNumPlayers(5)


        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val males = elephants
            .filter { k -> k.variant == AnimalVariant.MALE }
        val females = elephants
            .filter { k -> k.variant == AnimalVariant.FEMALE }

        // barn has two elephants

        root.game!!.players[0].barn.animals.add(males[0])
        root.game!!.players[0].barn.animals.add(females[0])


        assertEquals(males[0], root.game!!.players[0].barn.animals[0])
        assertEquals(females[0], root.game!!.players[0].barn.animals[1])

        root.playerActionService.discardTile(males[0]) // discard the male from the barn

        assertEquals(1, root.game!!.players[0].barn.animals.size)
        assertEquals(females[0].variant, root.game!!.players[0].barn.animals[0].variant)
        assertEquals(females[0].type, root.game!!.players[0].barn.animals[0].type)

        assertEquals(0, root.game!!.players[0].numCoins)

    }

    /**
     * - shouldDiscardVendingStallTileFromBarn
     * */
    @Test
    fun shouldDiscardVendingStallTileFromBarn() {
        val root = createFromNumPlayers(5)


        val vendingStalls = root.game!!.tileStack.getVendingStalls()
        val v1VendingStalls = vendingStalls.filter { vs -> vs.type == VendingStallType.V1 }
        val v2VendingStalls = vendingStalls.filter { vs -> vs.type == VendingStallType.V2 }

        // barn has two vending Stalls of types v1,v2
        root.game!!.players[0].barn.vendingStalls.add(v1VendingStalls[0])
        root.game!!.players[0].barn.vendingStalls.add(v2VendingStalls[0])


        assertEquals(v1VendingStalls[0], root.game!!.players[0].barn.vendingStalls[0])
        assertEquals(v2VendingStalls[0], root.game!!.players[0].barn.vendingStalls[1])
        assertEquals(2, root.game!!.players[0].barn.vendingStalls.size)

        root.playerActionService.discardTile(v2VendingStalls[0]) // discard the v2 vending stall from the barn

        assertEquals(1, root.game!!.players[0].barn.vendingStalls.size)
        assertEquals(VendingStallType.V1, root.game!!.players[0].barn.vendingStalls[0].type)

        assertEquals(0, root.game!!.players[0].numCoins)

    }

    /**
     * - shouldNotDiscardATileWhichIsNotAnimalOrVendingStall
     * */
    @Test
    fun shouldNotDiscardATileWhichIsNotAnimalOrVendingStall() {
        val root = createFromNumPlayers(5)
        val coins = root.game!!.tileStack.getCoinTiles()
        val exception = assertFailsWith<Exception> {
            root.playerActionService.discardTile(coins[0])
        }
        assertEquals(
            "the tile should be either an animal tile or vending stall tile. got another tile type",
            exception.message
        )

        assertEquals(2, root.game!!.players[0].numCoins)

    }

    /**
     * DesardTile test
     */
    @Test
    fun shouldDisCard() {

        val root = createFromNumPlayers(5)
        val flamingos = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.FLAMINGO)

        root.game!!.players[0].barn.animals.addAll(flamingos)
        val countAnimalInBarnBeforDescard = root.game!!.players[0].barn.animals.size
        // anzahl coins in Bank
        val countCoinBeforDiscard = root.game!!.coinsInBank

        root.playerActionService.discardTile(root.game!!.players[0].barn.animals[0])

        val countAnimalInBarnAfterDescard = root.game!!.players[0].barn.animals.size

        assertEquals(countAnimalInBarnAfterDescard, countAnimalInBarnBeforDescard - 1)
        assertNotEquals(root.game!!.coinsInBank, countCoinBeforDiscard)
        assertEquals(root.game!!.coinsInBank, countCoinBeforDiscard + 2)

    }

    /**
     * purchaseTile Test
     */
    @Test
    fun shouldPurchaseTile() {
        val root = createFromNumPlayers(5)

        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)


        assertEquals(2, root.game!!.players[0].numCoins)
        assertEquals(2, root.game!!.players[1].numCoins)
        assertEquals(20, root.game!!.coinsInBank)

        root.game!!.players[1].barn.animals.add(pandas[0])
        root.game!!.players[1].barn.animals.add(pandas[1])

        assertEquals(0, root.game!!.players[0].barn.animals.size)
        assertEquals(2, root.game!!.players[1].barn.animals.size)

        root.playerActionService.purchaseTile(root.game!!.players[1], pandas[0])

        assertEquals(1, root.game!!.players[0].barn.animals.size)
        assertEquals(1, root.game!!.players[1].barn.animals.size)

        assertEquals(0, root.game!!.players[0].numCoins)
        assertEquals(3, root.game!!.players[1].numCoins)
        assertEquals(21, root.game!!.coinsInBank)

    }

    /**
     * shouldNotPurchaseTileWhenBarnOfThePlayerHasNoAnimals
     */
    @Test
    fun shouldNotPurchaseTileWhenBarnOfThePlayerHasNoAnimals() {

        val root = createFromNumPlayers(5)

        val zebra = getAnimalTileWithType(root.game!!.tileStack.tiles, AnimalType.ZEBRA)

        val exception = assertFailsWith<Exception> {
            root.playerActionService.purchaseTile(root.game!!.players[1], zebra)
        }
        assertEquals("the barn of the other player has no animals. cannot purchase a tile from it", exception.message)


    }

    /**
     *purchaseTile Test When the barn of the other player has no animals with specific animal type
     */
    @Test
    fun shouldNotPurchaseTileWhenTheBarnOfTheOtherPlayerDoesNotContainAnimalType() {
        val root = createFromNumPlayers(5)

        val zebras = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ZEBRA)
        val camel = getAnimalTileWithType(root.game!!.tileStack.tiles, AnimalType.CAMEL)

        root.game!!.players[1].barn.animals.addAll(zebras)

        val exception = assertFailsWith<Exception> {
            root.playerActionService.purchaseTile(root.game!!.players[1], camel)
        }
        assertEquals("the provided animal tile does not exist in the barn of the other player", exception.message)


    }

    /**
     * addTileToTruck test
     */

    @Test
    fun shouldAddTileTOTruck() {

        val root = createFromNumPlayers(5)


        val zebra = getAnimalTileWithType(root.game!!.tileStack.tiles, AnimalType.ZEBRA)

        // add AnimalTile to truck
        val truck = root.game!!.deliveryTrucks[0]
        val countTileInTruckBeforAddTile = truck.tiles.size
        root.playerActionService.addTileToTruck(zebra, root.game!!.deliveryTrucks[0])
        val countTileInTruckAfterAddTile = root.game!!.deliveryTrucks[0].tiles.size

        assertEquals(countTileInTruckBeforAddTile + 1, countTileInTruckAfterAddTile)
        // add coinTile to Truck
        val coinTile = root.game!!.tileStack.tiles.filterIsInstance<CoinTile>()[0]
        root.playerActionService.addTileToTruck(coinTile, root.game!!.deliveryTrucks[0])
        val countTileInTruckAfterAddCoinTile = root.game!!.deliveryTrucks[0].tiles.size
        assertEquals(countTileInTruckAfterAddTile, countTileInTruckAfterAddCoinTile - 1)

        //add VendingStallTile to truck
        val vendingStallTile = root.game!!.tileStack.tiles.filterIsInstance<VendingStallTile>()[0]
        root.playerActionService.addTileToTruck(vendingStallTile, root.game!!.deliveryTrucks[0])
        val countTileInTruckAfterAddVendingStallTile = root.game!!.deliveryTrucks[0].tiles.size

        assertEquals(countTileInTruckAfterAddVendingStallTile - 3, countTileInTruckBeforAddTile)

        val exception = assertFailsWith<Exception> {
            root.playerActionService.addTileToTruck(zebra, root.game!!.deliveryTrucks[0])
        }
        assertEquals("cannot add a tile to this delivery truck. it's full", exception.message)


    }

    /**
     * exchangeTile (sourceEnc,swatinationEnc) Test
     */

    @Test
    fun shouldExchangeTilesEncWithOtherEnc() {
        val root = createFromNumPlayers(5)

        val zebras = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ZEBRA).take(3)
        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA).take(3)

        root.game!!.players[0].enclosures[1].animals.addAll(zebras)
        root.game!!.players[0].enclosures[2].animals.addAll(pandas)

        val coinInBankBeforeExchangeTiles = root.game!!.coinsInBank
        val coinForPlayerBeforeExchange = root.game!!.players[0].numCoins


        root.playerActionService.exchangeTiles(
            root.game!!.players[0].enclosures[1],
            root.game!!.players[0].enclosures[2]
        )

        assertEquals(AnimalType.PANDA, root.game!!.players[0].enclosures[1].getAnimalType())
        assertEquals(AnimalType.ZEBRA, root.game!!.players[0].enclosures[2].getAnimalType())

        assertEquals(3, root.game!!.players[0].enclosures[1].animals.size)
        assertEquals(3, root.game!!.players[0].enclosures[2].animals.size)

        // Test Coin
        assertEquals(coinInBankBeforeExchangeTiles + 1, root.game!!.coinsInBank)
        assertEquals(coinForPlayerBeforeExchange - 1, root.game!!.players[0].numCoins)
    }

    /**
     * exchangeTile (sourceEnc,destinationEnc) Test when one of Enc empty
     */
    @Test
    fun shouldExchangeTilesEncWithOtherEncWhenOneOfEncsIsEmpty() {
        val root = createFromNumPlayers(5)

        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA).take(3)

        root.game!!.players[1].enclosures[1].animals.addAll(pandas)
        val player1NumCoin = root.game!!.players[1].numCoins

        val exception = assertFailsWith<Exception> {
            root.playerActionService.exchangeTiles(
                root.game!!.players[1].enclosures[1],
                root.game!!.players[1].enclosures[2]
            )
        }
        assertEquals("cannot exchange tiles when an enclosure has no animals", exception.message)


        assertEquals(player1NumCoin, root.game!!.players[1].numCoins)

    }

    /**
     * exchangeTile (sourceEnc,destinationEnc) Test when animalstyp in both of Encs are same
     */
    @Test
    fun shouldExchangeTilesEncWithOtherEncWhenAnimalTypeAreSame() {
        val root = createFromNumPlayers(5)

        val camels = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.CAMEL).take(3)

        root.game!!.players[1].enclosures[0].animals.addAll(camels)
        root.game!!.players[1].enclosures[1].animals.addAll(camels)

        val exception = assertFailsWith<Exception> {
            root.playerActionService.exchangeTiles(
                root.game!!.players[1].enclosures[0],
                root.game!!.players[1].enclosures[1]
            )
        }
        assertEquals("cannot exchange the same animal type", exception.message)


    }

    /**
     * exchangeTile (sourceEnc,barn,AnimalType) Test
     */
    @Test
    fun shouldExchangeTilesBarnWithEnc() {
        val root = createFromNumPlayers(5)

        val zebras = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ZEBRA).take(3)
        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA).take(3)


        root.game!!.players[0].barn.animals.addAll(zebras)
        root.game!!.players[0].enclosures[0].animals.addAll(pandas)

        val coinInBankBeforeExchangeTiles = root.game!!.coinsInBank
        val coinForPlayerBeforeExchange = root.game!!.players[0].numCoins

        root.playerActionService.exchangeTiles(
            root.game!!.players[0].enclosures[0],
            root.game!!.players[0].barn, AnimalType.ZEBRA
        )

        assertEquals(3, root.game!!.players[0].enclosures[0].animals.size)
        assertEquals(3, root.game!!.players[0].barn.animals.size)

        assertEquals(AnimalType.ZEBRA, root.game!!.players[0].enclosures[0].getAnimalType())
        assertEquals(AnimalType.PANDA, root.game!!.players[0].barn.animals[0].type)

        assertEquals(coinInBankBeforeExchangeTiles + 1, root.game!!.coinsInBank)
        assertEquals(coinForPlayerBeforeExchange - 1, root.game!!.players[0].numCoins)
    }

    /**
     * -MoveVendingStallFromEnclosureToEnclosure Test
     */
    @Test
    fun shouldMoveVendingStallFromEnclosureToEnclosure() {
        val root = createFromNumPlayers(5)

        val vendingStallTileV1 = getVendingStallTileWithType(root.game!!.tileStack.tiles, VendingStallType.V1)

        root.game!!.players[0].enclosures[0].vendingStalls.add(vendingStallTileV1)


        root.playerActionService.moveVendingStallFromEnclosureToEnclosure(
            root.game!!.players[0].enclosures[0],
            root.game!!.players[0].enclosures[1], vendingStallTileV1
        )

        assertEquals(root.game!!.players[0].enclosures[1].vendingStalls[0].type, VendingStallType.V1)

        assertEquals(root.game!!.players[0].enclosures[1].vendingStalls.size, 1)

    }

    /**
     * -shouldMoveVendingStallFromEnclosureToExpansionBoard Test
     */
    @Test
    fun shouldMoveVendingStallFromEnclosureToExpansionBoard() {
        val root = createFromNumPlayers(5)

        val vendingStallTileV1 = getVendingStallTileWithType(root.game!!.tileStack.tiles, VendingStallType.V1)

        root.game!!.players[0].numCoins = 10
        root.playerActionService.expandZoo()

        root.game!!.activePlayerIndex = 0

        root.game!!.players[0].enclosures[0].vendingStalls.add(vendingStallTileV1)


        root.playerActionService.moveVendingStallFromEnclosureToEnclosure(
            root.game!!.players[0].enclosures[0],
            root.game!!.players[0].expansionBoards[0], vendingStallTileV1
        )

        assertEquals(root.game!!.players[0].expansionBoards[0].vendingStalls[0].type, VendingStallType.V1)
        assertEquals(root.game!!.players[0].expansionBoards[0].vendingStalls.size, 1)

    }

    /**
     * -shouldMoveVendingStallFromEnclosureToExpansionBoard Test
     */
    @Test
    fun shouldErrorWhenTryingToUseExpansionBoardWithoutExpandingTheZoo() {
        val root = createFromNumPlayers(5)

        val vendingStallTileV1 = getVendingStallTileWithType(root.game!!.tileStack.tiles, VendingStallType.V1)

        root.game!!.players[0].numCoins = 10

        root.game!!.players[0].enclosures[0].vendingStalls.add(vendingStallTileV1)


        val exception = assertFailsWith<Exception> {

            root.playerActionService.moveVendingStallFromEnclosureToEnclosure(
                root.game!!.players[0].enclosures[0],
                root.game!!.players[0].expansionBoards[0], vendingStallTileV1
            )

        }

        assertEquals(
            exception.message,
            "could not find either the src enclosure or the dst enclosure in the player's enclosures or expansion boards"
        )

    }


    /**
     * -shouldSetLastPlayerToTakeTruckInGameSecondTest Test
     */
    @Test
    fun shouldSetLastPlayerToTakeTruckInGameSecondTest() {
        val root = createFromNumPlayers(4)
        // first player plays
        root.game!!.players[0].tookDeliveryTruck = true
        root.game!!.players[1].tookDeliveryTruck = true
        root.game!!.players[2].tookDeliveryTruck = true

        root.game!!.activePlayerIndex = 3 // third player will play and take a truck and as a such the round is ended
        root.game!!.deliveryTrucks[0].tiles.add(root.game!!.tileStack.tiles[0])
        root.game!!.deliveryTrucks[0].tiles.add(root.game!!.tileStack.tiles[1])

        assertEquals(
            0,
            root.game!!.lastPlayerToTakeDeliveryTruckIndex
        )

        root.playerActionService.takeTruck(root.game!!.deliveryTrucks[0])

        assertEquals(
            3,
            root.game!!.lastPlayerToTakeDeliveryTruckIndex
        )

    }

    /**
     * -shouldAddAnimalTileToBarnWhenTakingATruckIfAllOtherEncsHaveAnotherAnimalTypes
     */
    @Test
    fun shouldAddAnimalTileToBarnWhenTakingATruckIfAllOtherEncsHaveAnotherAnimalTypes() {
        val root = createFromNumPlayers(5)


        val leopards = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.LEOPARD)
        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)
        val flamingos = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.FLAMINGO)
        val camels = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.CAMEL)

        root.game!!.players[0].enclosures[0].animals.add(leopards[0])  // first enc has leopards
        root.game!!.players[0].enclosures[1].animals.add(elephants[0]) // second enc has elephants
        root.game!!.players[0].enclosures[2].animals.add(pandas[0]) // third enc has pandas

        root.game!!.deliveryTrucks[0].tiles.add(pandas[1])
        root.game!!.deliveryTrucks[0].tiles.add(flamingos[0]) // should go to barn as there is no enc with this animal type
        root.game!!.deliveryTrucks[0].tiles.add(camels[0]) // should go to barn as there is no enc with this animal type

        root.playerActionService.takeTruck(root.game!!.deliveryTrucks[0])


        assertEquals(
            1,
            root.game!!.players[0].enclosures[0].animals.size
        )

        assertEquals(
            1,
            root.game!!.players[0].enclosures[1].animals.size
        )

        assertEquals(
            2,
            root.game!!.players[0].enclosures[2].animals.size
        ) // the second pandas was added

        assertEquals(
            2,
            root.game!!.players[0].barn.animals.size
        ) // the second pandas was added

    }

    /**
     * -shouldAddAnimalTileToBarnWhenTakingATruckIfAllOtherEncsHaveAnotherAnimalTypes
     */
    @Test
    fun shouldAddOffspringToTheBarnIfTheEncHasNoSpace() {
        val root = createFromNumPlayers(5)


        val leopards = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.LEOPARD)
        val maleLeopards = leopards.filter { l -> l.variant == AnimalVariant.MALE }
        val femaleLeopards = leopards.filter { l -> l.variant == AnimalVariant.FEMALE }
        val elephants = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ELEPHANT)
        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)

        root.game!!.players[0].enclosures[0].animals.addAll(
            listOf(
                maleLeopards[0],
                maleLeopards[1],
                femaleLeopards[0],
            )
        )  // first enc has 2 males, 1 female

        root.game!!.players[0].enclosures[1].animals.add(elephants[0]) // second enc has elephants
        root.game!!.players[0].enclosures[2].animals.add(pandas[0]) // third enc has pandas


        root.game!!.deliveryTrucks[0].tiles.add(femaleLeopards[1])
        root.playerActionService.takeTruck(root.game!!.deliveryTrucks[0])


        assertEquals(
            4,
            root.game!!.players[0].enclosures[0].animals.size
        )

        assertEquals(
            1,
            root.game!!.players[0].barn.animals.size
        ) // the offspring is in the barn

        assertEquals(
            AnimalVariant.CHILD,
            root.game!!.players[0].barn.animals[0].variant
        ) // the offspring is in the barn

    }


    /**
     * -shouldMoveVendingStallFromBarnToEnclosure
     */
    @Test
    fun shouldMoveVendingStallFromBarnToEnclosure() {
        val root = createFromNumPlayers(5)

        val vendingStalls = root.game!!.tileStack.tiles.filter { tile -> tile is VendingStallTile }
        root.game!!.players[0].barn.vendingStalls.add(vendingStalls[0] as VendingStallTile)

        assertEquals(
            0,
            root.game!!.players[0].enclosures[0].vendingStalls.size
        )

        assertEquals(
            1,
            root.game!!.players[0].barn.vendingStalls.size
        )


        root.playerActionService.moveVendingStallFromBarnToEnclosure(
            root.game!!.players[0].enclosures[0],
            vendingStalls[0] as VendingStallTile
        )


        assertEquals(
            1,
            root.game!!.players[0].enclosures[0].vendingStalls.size
        )

        assertEquals(
            0,
            root.game!!.players[0].barn.vendingStalls.size
        )


    }


    /**
     * -shouldRemoveTheCoinTileFromTruckAfterTakingIt
     */
    @Test
    fun shouldRemoveTheCoinTileFromTruckAfterTakingIt() {
        val root = createFromNumPlayers(5)

        val coins = root.game!!.tileStack.tiles.filterIsInstance<CoinTile>()
        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)

        root.game!!.deliveryTrucks[0].tiles.add(coins[0])
        root.game!!.deliveryTrucks[0].tiles.add(pandas[0])
        root.game!!.deliveryTrucks[0].tiles.add(pandas[0])


        assertEquals(
            2,
            root.game!!.players[0].numCoins
        )

        assertEquals(
            3,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        root.playerActionService.addCoinTileFromTruckToPlayer(
            coins[0],
            root.game!!.deliveryTrucks[0]
        )

        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        ) // we took the coin tile

        assertEquals(
            3,
            root.game!!.players[0].numCoins
        ) // we took the coin tile

        assertEquals(
            "p1",
            root.game!!.getActivePlayer().name
        ) // player is still active because the truck is not empty

    }

    /**
     * -shouldSwitchActivePlayerOnlyWhenTruckIsEmpty
     */
    @Test
    fun shouldSwitchActivePlayerOnlyWhenTruckIsEmpty() {
        val root = createFromNumPlayers(5)

        val coins = root.game!!.tileStack.tiles.filterIsInstance<CoinTile>()

        root.game!!.deliveryTrucks[0].tiles.add(coins[0])
        root.game!!.deliveryTrucks[0].tiles.add(coins[1])
        root.game!!.deliveryTrucks[0].tiles.add(coins[2])


        assertEquals(
            2,
            root.game!!.players[0].numCoins
        )

        assertEquals(
            3,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        root.playerActionService.addCoinTileFromTruckToPlayer(
            root.game!!.deliveryTrucks[0].tiles[0] as CoinTile,
            root.game!!.deliveryTrucks[0]
        )

        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        root.playerActionService.addCoinTileFromTruckToPlayer(
            root.game!!.deliveryTrucks[0].tiles[0] as CoinTile,
            root.game!!.deliveryTrucks[0]
        )

        assertEquals(
            1,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        root.playerActionService.addCoinTileFromTruckToPlayer(
            root.game!!.deliveryTrucks[0].tiles[0] as CoinTile,
            root.game!!.deliveryTrucks[0]
        )

        assertEquals(
            0,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        assertEquals(
            5,
            root.game!!.players[0].numCoins
        )
        assertEquals(
            "p2",
            root.game!!.getActivePlayer().name
        ) // player switches

    }


    /**
     * -shouldBeginNewRoundAfterAllPlayersPlayed
     */
    @Test
    fun shouldBeginNewRoundAfterAllPlayersPlayed() {
        val root = createFromNumPlayers(3)


        root.game!!.players[0].numCoins = 10 // every player has enough coins to do actions
        root.game!!.players[1].numCoins = 10
        root.game!!.players[2].numCoins = 10


        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)

        root.game!!.deliveryTrucks[0].tiles.add(pandas[0])
        root.game!!.deliveryTrucks[1].tiles.add(pandas[1])
        root.game!!.deliveryTrucks[2].tiles.add(pandas[2])


        assertEquals(
            "p1",
            root.game!!.getActivePlayer().name
        )

        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[0].tiles[0],
            root.game!!.deliveryTrucks[0],
            root.game!!.getActivePlayer().enclosures[0]
        )

        assertEquals(
            "p2",
            root.game!!.getActivePlayer().name
        )

        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[1].tiles[0],
            root.game!!.deliveryTrucks[1],
            root.game!!.getActivePlayer().enclosures[0]
        )


        assertEquals(
            "p3",
            root.game!!.getActivePlayer().name
        )

        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[2].tiles[0],
            root.game!!.deliveryTrucks[2],
            root.game!!.getActivePlayer().enclosures[0]
        )


        assertEquals(
            "p3",
            root.game!!.getActivePlayer().name
        )

        assertEquals(false, root.game!!.deliveryTrucks[0].isTaken)
        assertEquals(false, root.game!!.deliveryTrucks[1].isTaken)
        assertEquals(false, root.game!!.deliveryTrucks[2].isTaken)

    }

    /**
     * -shouldStartNewRounedWithActivePlayerAsLastPlayerToTakeTruck
     */
    @Test
    fun shouldStartNewRounedWithActivePlayerAsLastPlayerToTakeTruck() {
        val root = createFromNumPlayers(3)

        root.game!!.players[0].numCoins = 10 // every player has enough coins to do actions
        root.game!!.players[1].numCoins = 10
        root.game!!.players[2].numCoins = 10


        assertEquals(
            "p1",
            root.game!!.getActivePlayer().name
        )

        root.playerActionService.expandZoo()

        assertEquals(
            "p2",
            root.game!!.getActivePlayer().name
        )

        root.playerActionService.expandZoo()

        assertEquals(
            "p3",
            root.game!!.getActivePlayer().name
        )

        root.game!!.lastPlayerToTakeDeliveryTruckIndex = 1 // make second player to start new round
        root.game!!.players[0].tookDeliveryTruck = true
        root.game!!.players[1].tookDeliveryTruck = true
        root.game!!.players[2].tookDeliveryTruck = true

        root.playerActionService.expandZoo()

        assertEquals(
            "p2",
            root.game!!.getActivePlayer().name
        )

        assertEquals(
            false,
            root.game!!.players[0].tookDeliveryTruck
        )

        assertEquals(
            false,
            root.game!!.players[1].tookDeliveryTruck
        )

        assertEquals(
            false,
            root.game!!.players[2].tookDeliveryTruck
        )

    }

    /**
     * -shouldAddAnimalOrVendingStallTileFromTruckToPlayerEnclosure
     */
    @Test
    fun shouldAddAnimalOrVendingStallTileFromTruckToPlayerEnclosure() {
        val root = createFromNumPlayers(5)

        val vendingStalls = root.game!!.tileStack.tiles.filterIsInstance<VendingStallTile>()

        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[0])
        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[1])


        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[0].tiles[0] as VendingStallTile,
            root.game!!.deliveryTrucks[0],
            root.game!!.players[0].enclosures[0]
        )

        assertEquals(
            1,
            root.game!!.players[0].enclosures[0].vendingStalls.size
        )

        assertEquals(
            1,
            root.game!!.deliveryTrucks[0].tiles.size
        )

    }


    /**
     * -shouldAddAnimalOrVendingStallTileFromTruckToPlayerEnclosure
     */
    @Test
    fun shouldThrowExceptionWhenEnclosureDoesNotHaveSpaceForVendingStallTile() {
        val root = createFromNumPlayers(5)

        val vendingStalls = root.game!!.tileStack.tiles.filterIsInstance<VendingStallTile>()


        root.game!!.players[0].enclosures[0].vendingStalls.add(vendingStalls[0])
        root.game!!.players[0].enclosures[0].vendingStalls.add(vendingStalls[1]) // vending stalls spaces are full

        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[2])
        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[3])


        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        val exception = assertFailsWith<Exception> {
            root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
                root.game!!.deliveryTrucks[0].tiles[0] as VendingStallTile,
                root.game!!.deliveryTrucks[0],
                root.game!!.players[0].enclosures[0]
            )

        }

        assertEquals(
            "the enclosure does not have enough space for vending stalls",
            exception.message,
        )

    }


    /**
     * -shouldUseTileAsNonPlaceHolderTileIfValid
     */
    @Test
    fun shouldUseTileAsNonPlaceHolderTileIfValid() {
        val root = createFromNumPlayers(2)

        val vendingStalls = root.game!!.tileStack.tiles.filterIsInstance<VendingStallTile>()
        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[0])

        assertEquals(
            1,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        assertEquals(
            0,
            root.game!!.players[0].enclosures[0].vendingStalls.size
        )

        assertEquals(
            "p1",
            root.game!!.getActivePlayer().name
        )

        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[0].tiles[0] as VendingStallTile,
            root.game!!.deliveryTrucks[0],
            root.game!!.players[0].enclosures[0]
        )

        assertEquals(
            0,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        assertEquals(
            1,
            root.game!!.players[0].enclosures[0].vendingStalls.size
        )

        assertEquals(
            "p2",
            root.game!!.getActivePlayer().name
        )

    }


    /**
     * -shouldUseTileAsNonPlaceHolderTileIfValid
     */
    @Test
    fun shouldThrowWhenAddingATileToATruckThatIsFull() {
        val root = createFromNumPlayers(2)

        val vendingStalls = root.game!!.tileStack.tiles.filterIsInstance<VendingStallTile>()
        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[0])


        val exception = assertFailsWith<Exception> {
            root.playerActionService.addTileToTruck(
                vendingStalls[1],
                root.game!!.deliveryTrucks[0],
            )

        }
        assertEquals(
            "cannot add a tile to this delivery truck. it's full",
            exception.message
        )

    }

    /**
     * -shouldTakeAnimalOrVendingStallTileFromTruckToPlayerEnclosureWithExpansioBoard
     */
    @Test
    fun shouldTakeAnimalOrVendingStallTileFromTruckToPlayerEnclosureWithExpansioBoard() {
        val root = createFromNumPlayers(5)


        val vendingStalls = root.game!!.tileStack.tiles.filterIsInstance<VendingStallTile>()

        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[0])
        root.game!!.deliveryTrucks[0].tiles.add(vendingStalls[1])


        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        )


        root.game!!.players[0].numCoins = 10

        root.playerActionService.expandZoo()

        assertEquals(
            true,
            root.game!!.players[0].expansionBoards[0].isUsed
        )

        root.game!!.activePlayerIndex = 0
        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[0].tiles[0] as VendingStallTile,
            root.game!!.deliveryTrucks[0],
            root.game!!.players[0].expansionBoards[0]
        )

        assertEquals(
            1,
            root.game!!.players[0].expansionBoards[0].vendingStalls.size,
        )


        assertEquals(
            1,
            root.game!!.deliveryTrucks[0].tiles.size
        )

    }


    /**
     * -shouldRemoveTheCoinTileFromTruckAfterTakingIt
     */
    @Test
    fun shouldTakeAllCoinTilesFromTruckToPlayer() {
        val root = createFromNumPlayers(5)

        val coins = root.game!!.tileStack.tiles.filterIsInstance<CoinTile>()
        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)

        // truck has 2 coins
        root.game!!.deliveryTrucks[0].tiles.add(coins[0])
        root.game!!.deliveryTrucks[0].tiles.add(coins[1])
        root.game!!.deliveryTrucks[0].tiles.add(pandas[0])


        assertEquals(
            2,
            root.game!!.players[0].numCoins
        )

        assertEquals(
            3,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        root.playerActionService.addAllCoinTilesFromTruckToPlayer(
            root.game!!.deliveryTrucks[0]
        )

        assertEquals(
            1,
            root.game!!.deliveryTrucks[0].tiles.size
        ) // we took 2 coin tiles

        assertEquals(
            4,
            root.game!!.players[0].numCoins
        ) // we took 2 coin tiles

        assertEquals(
            "p1",
            root.game!!.getActivePlayer().name
        ) // player is still active because the truck is not empty

    }


    /**
     * -shouldErrorWhenTakingAllCoinTilesFromTruckIfHasNoCoinTiles
     */
    @Test
    fun shouldErrorWhenTakingAllCoinTilesFromTruckIfHasNoCoinTiles() {
        val root = createFromNumPlayers(5)

        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)

        // truck has 0 coins
        root.game!!.deliveryTrucks[0].tiles.add(pandas[0])
        root.game!!.deliveryTrucks[0].tiles.add(pandas[1])


        assertEquals(
            2,
            root.game!!.players[0].numCoins
        )

        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        )

        val exception = assertFailsWith<Exception> {
            root.playerActionService.addAllCoinTilesFromTruckToPlayer(
                root.game!!.deliveryTrucks[0]
            )
        }

        assertEquals(
            "the given truck does not have any coin tile",
            exception.message
        )

        assertEquals(
            2,
            root.game!!.players[0].numCoins
        ) // we took 2 coin tiles

        assertEquals(
            2,
            root.game!!.deliveryTrucks[0].tiles.size
        )

    }

    /**
     * -removesTileOnDraw
     */
    @Test
    fun removesTileOnDraw() {
        val root = createFromNumPlayers(5)

        val beforeDrawTileCount = root.game!!.tileStack.tiles.size

        root.playerActionService.draw()

        assertEquals(
            beforeDrawTileCount - 1,
            root.game!!.tileStack.tiles.size
        )


    }

    /**
     * -akenFlagWithTruckmovesTileOnDr
     */
    @Test
    fun shouldKeppIsTakenFlagWithTruck() {
        val root = createFromNumPlayers(5)

        val pandas = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.PANDA)
        val zebras = getAnimalTilesWithType(root.game!!.tileStack.tiles, AnimalType.ZEBRA)
        val coins = root.game!!.tileStack.tiles.filterIsInstance<CoinTile>()

        root.game!!.deliveryTrucks[0].tiles.add(pandas[0])
        root.game!!.deliveryTrucks[0].tiles.add(pandas[1])

        root.game!!.deliveryTrucks[1].tiles.add(coins[0])
        root.game!!.deliveryTrucks[1].tiles.add(zebras[0])


        assertEquals(
            false,
            root.game!!.deliveryTrucks[0].isTaken
        )

        assertEquals(
            false,
            root.game!!.deliveryTrucks[1].isTaken
        )


        root.playerActionService.addAllCoinTilesFromTruckToPlayer(root.game!!.deliveryTrucks[1])

        assertEquals(
            true,
            root.game!!.deliveryTrucks[1].isTaken
        )

        root.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
            root.game!!.deliveryTrucks[0].tiles[0],
            root.game!!.deliveryTrucks[0],
            root.game!!.getActivePlayer().enclosures[0]
        )

        assertEquals(
            true,
            root.game!!.deliveryTrucks[0].isTaken
        )

        assertEquals(
            true,
            root.game!!.deliveryTrucks[1].isTaken
        )


    }

}
