package  service

import AbstractRefreshingService
import Cloneable
import Logger
import entity.*
import javafx.application.Platform
import java.lang.Integer.max
import java.util.*


/**
 * The player action service
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    private val logger: Logger = Logger("PlayerActionService")

    /**
     * is used to check if an AI Player made a move
     */
    var hasAIPlayerMadeAMove: Boolean = false

    /**
     * - move a single animal tile from the barn to an enclosure
     * - the tile should be coming from the barn
     * - the action costs 1 coin
     */
    fun moveAnimalTileFromBarnToEnclosure(animalTile: AnimalTile, dstEnclosure: Enclosure) {
        this.logIfNotSimulating("Got request to move animal tile from barn to enclosure. animal tile =${animalTile}")
        val game = rootService.game

        checkNotNull(game) { "game is null. cannot move a tile" }

        val numCoins = game.getActivePlayer().numCoins
        check(numCoins >= 1) {
            "the player cannot choose this action. the action requires 1 coin and the player" +
                    " has $numCoins"
        }


        check(game.getActivePlayer().barn.animals.isNotEmpty()) { "the barn is empty" }

        check(dstEnclosure.hasSpaceForAnimal()) { "the enclosure does not have enough space for animals" }

        // trying to add an animal of a different type to the enclosure
        if (dstEnclosure.animals.isNotEmpty() && dstEnclosure.getAnimalType() != animalTile.type) {
            throw Exception(
                "the destination enclosure can only contain animals of the " +
                        "type ${dstEnclosure.getAnimalType()}"
            )
        }

        val isExpansionBoard = dstEnclosure is ExpansionBoard
        val dstEnclosureIndex = if (isExpansionBoard) {
            val usedExpansionBoards = game.getActivePlayer().getUsedExpansionBoards()
            check(usedExpansionBoards.isNotEmpty()) {
                "the player did not expand the zoo yet but an " +
                        "expansion board was given"
            }
            usedExpansionBoards.indexOf(dstEnclosure)
        } else {
            game.getActivePlayer().enclosures.indexOf(dstEnclosure)
        }

        check(dstEnclosureIndex != -1) {
            "could not find the given enclosure in the player enclosures or " +
                    "expansion boards"
        }

        val tileIndex = game.getActivePlayer().barn.animals.indexOf(animalTile)

        check(tileIndex != -1) { "the given tile does not exist in the barn" }

        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val tileInNewGame = animalTile.clone()

        // add tile to enclosure animals
        if (isExpansionBoard) {
            activePlayerInNewGame.expansionBoards[dstEnclosureIndex].animals.add(tileInNewGame)
        } else {
            activePlayerInNewGame.enclosures[dstEnclosureIndex].animals.add(tileInNewGame)
        }

        // rm tile from the barn
        activePlayerInNewGame.barn.animals.removeAt(tileIndex)

        // update bank and coin
        newGame.coinsInBank += 1
        newGame.getActivePlayer().numCoins -= 1

        val newDstEnclosure = if (isExpansionBoard) {
            activePlayerInNewGame.expansionBoards[dstEnclosureIndex]
        } else {
            activePlayerInNewGame.enclosures[dstEnclosureIndex]
        }
        // if the tile was added to the last space. try to give the player bonus points
        this.handleCaseWhenTileWasAddedToLastEmptySpace(newGame, newDstEnclosure)

        //  try to produce a new offspring
        this.handleCaseWhenAnimalTileMayProduceAnOffspring(newGame, newDstEnclosure)

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterTileMoved() }

        this.switchToNextPossiblePlayerOrEndRound()

    }


    /**
     * - move a vending stall from the barn to the enclosure
     * - the action costs 1 coin
     */
    fun moveVendingStallFromBarnToEnclosure(
        enclosure: Enclosure,
        vendingStall: VendingStallTile
    ) {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot move a tile" }

        this.logIfNotSimulating("Got request to move vending stall tile from barn to enclosure. vending stall tile =${vendingStall}")

        val player = game.getActivePlayer()
        check(player.numCoins >= 1) {
            "the player cannot choose this action. the action requires 1 coin " +
                    "and the player has ${player.numCoins}"
        }
        check(
            player.getEnclosuresWithUsedExpansionBoards().contains(enclosure)
        ) { "the given player does not have the given enclosure" }


        check(enclosure.hasSpaceForVendingStall()) { "the enclosure does not have enough space for vending stalls" }
        check(player.barn.vendingStalls.contains(vendingStall)) { "the barn does not contain the given vending stall" }

        val isExpansionBoard = enclosure is ExpansionBoard

        val enclosureIndex = if (isExpansionBoard) {
            player.getUsedExpansionBoards().indexOf(enclosure)
        } else {
            player.enclosures.indexOf(enclosure)
        }

        check(enclosureIndex != -1) {
            "could not find the enclosures in the player's enclosures" +
                    " or expansion boards"
        }


        val vendingStallIndex = player.barn.vendingStalls.indexOf(vendingStall)

        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val vendingStallInNewGame = player.barn.vendingStalls[vendingStallIndex]

        // add vending stall to the enclosure
        if (isExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[enclosureIndex].vendingStalls.add(vendingStallInNewGame)
        } else {
            activePlayerInNewGame.enclosures[enclosureIndex].vendingStalls.add(vendingStallInNewGame)
        }

        // rm vending stall from the barn
        activePlayerInNewGame.barn.vendingStalls.removeAt(vendingStallIndex)

        // update bank and coin
        newGame.coinsInBank += 1
        newGame.getActivePlayer().numCoins -= 1

        val enc = if (isExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[enclosureIndex]
        } else {
            activePlayerInNewGame.enclosures[enclosureIndex]
        }

        // if the vending stall was added to the last space. try to give the player bonus points
        this.handleCaseWhenTileWasAddedToLastEmptySpace(newGame, enc)

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterTileMoved() }

        this.switchToNextPossiblePlayerOrEndRound()

    }


    /**
     * - move a vending stall from one space to another in the zoo
     * - the srcEnclosure is the one that contains the vending stall
     * - the dstEnclosure is the one that will contain the moved vending stall
     * - the vendingStallInSrcEnclosure is the vending stall to move
     * - the action costs 1 coin
     */
    fun moveVendingStallFromEnclosureToEnclosure(
        srcEnclosure: Enclosure,
        dstEnclosure: Enclosure,
        vendingStallInSrcEnclosure: VendingStallTile
    ) {
        val game = rootService.game

        checkNotNull(game) { "game is null. cannot move a tile" }

        this.logIfNotSimulating("Got request to move vending stall tile from enclosure to enclosure. vending stall tile =${vendingStallInSrcEnclosure}")


        val numCoins = game.getActivePlayer().numCoins
        check(numCoins >= 1) {
            "the player cannot choose this action. the action requires 1 coin and the" +
                    " player has $numCoins"
        }


        check(dstEnclosure.hasSpaceForVendingStall()) { "the enclosure does not have enough space for vending stalls" }

        val isSrcExpansionBoard = srcEnclosure is ExpansionBoard
        val isDstExpansionBoard = dstEnclosure is ExpansionBoard

        val srcEnclosureIndex = if (isSrcExpansionBoard) {
            game.getActivePlayer().getUsedExpansionBoards().indexOf(srcEnclosure)
        } else {
            game.getActivePlayer().enclosures.indexOf(srcEnclosure)
        }

        val dstEnclosureIndex = if (isDstExpansionBoard) {
            game.getActivePlayer().getUsedExpansionBoards().indexOf(dstEnclosure)
        } else {
            game.getActivePlayer().enclosures.indexOf(dstEnclosure)
        }

        check(srcEnclosureIndex != -1 && dstEnclosureIndex != -1) {
            "could not find either the src enclosure" +
                    " or the dst enclosure in the player's enclosures or expansion boards"
        }


        val vendingStallIndex = if (isSrcExpansionBoard) {
            game.getActivePlayer().getUsedExpansionBoards()[srcEnclosureIndex].vendingStalls.indexOf(
                vendingStallInSrcEnclosure
            )
        } else {
            game.getActivePlayer().enclosures[srcEnclosureIndex].vendingStalls.indexOf(vendingStallInSrcEnclosure)
        }

        check(vendingStallIndex != -1) { "the given vending stall does not exist in the src enclosure" }

        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val vendingStallInNewGame = vendingStallInSrcEnclosure.clone()

        // add vending stall to the dist enclosure
        if (isDstExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[dstEnclosureIndex].vendingStalls.add(vendingStallInNewGame)
        } else {
            activePlayerInNewGame.enclosures[dstEnclosureIndex].vendingStalls.add(vendingStallInNewGame)
        }

        // rm vending stall from the src enclosure
        if (isSrcExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[srcEnclosureIndex].vendingStalls.removeAt(vendingStallIndex)
        } else {
            activePlayerInNewGame.enclosures[srcEnclosureIndex].vendingStalls.removeAt(vendingStallIndex)
        }

        // update bank and coin
        newGame.coinsInBank += 1
        newGame.getActivePlayer().numCoins -= 1


        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterTileMoved() }

        this.switchToNextPossiblePlayerOrEndRound()

    }


    /**
     * - takes all tiles of one animal type in  one of his zoo's enclosures
     *   and exchanges them with all tiles of another animal type in another of his zoo's locations.
     *   the action costs 1 coin
     */
    fun exchangeTiles(source: Enclosure, destination: Enclosure) {

        val game = rootService.game

        checkNotNull(game) { "game is null. cannot exchange tiles" }

        this.logIfNotSimulating("Got request to exchange tiles between enclosures.")

        val numCoins = game.getActivePlayer().numCoins

        check(numCoins >= 1) {
            "the player cannot choose this action. the action requires 1 coin and " +
                    "the player has $numCoins"
        }

        check(source.animals.isNotEmpty() && destination.animals.isNotEmpty()) {
            "cannot exchange tiles when " +
                    "an enclosure has no animals"
        }

        check(source.getAnimalType() != destination.getAnimalType()) { "cannot exchange the same animal type" }

        check(
            source.animals.size <= destination.animalsMax
                    && destination.animals.size <= source.animalsMax
        ) {
            "An enclosure do " +
                    "not have enough animals space for this exchange"
        }

        val isSrcExpansionBoard = source is ExpansionBoard
        val isDstExpansionBoard = destination is ExpansionBoard
        val sourceEncIndex = if (isSrcExpansionBoard) {
            game.getActivePlayer().getUsedExpansionBoards().indexOf(source)
        } else {
            game.getActivePlayer().enclosures.indexOf(source)
        }
        val destinationEncIndex = if (isDstExpansionBoard) {
            game.getActivePlayer().getUsedExpansionBoards().indexOf(destination)
        } else {
            game.getActivePlayer().enclosures.indexOf(destination)
        }

        check(sourceEncIndex != -1 && destinationEncIndex != -1) {
            "could not find either the source " +
                    "enclosure or the destination enclosure in the player"
        }

        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val srcEnclosureInNewGame = if (isSrcExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[sourceEncIndex]
        } else {
            activePlayerInNewGame.enclosures[sourceEncIndex]
        }

        val dstEnclosureInNewGame = if (isDstExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[destinationEncIndex]
        } else {
            activePlayerInNewGame.enclosures[destinationEncIndex]
        }

        val srcAnimals = srcEnclosureInNewGame.animals.toMutableList()
        val dstAnimals = dstEnclosureInNewGame.animals.toMutableList()


        // clear the animals
        srcEnclosureInNewGame.animals.clear()
        dstEnclosureInNewGame.animals.clear()

        // add the animals
        srcEnclosureInNewGame.animals.addAll(dstAnimals)
        dstEnclosureInNewGame.animals.addAll(srcAnimals)


        // update coins
        newGame.coinsInBank += 1
        activePlayerInNewGame.numCoins -= 1

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterTileMoved() }

        this.switchToNextPossiblePlayerOrEndRound()
    }

    /**
     * - takes tiles of one animal type in  his barn
     *   and exchanges them with all tiles of another animal type in another of his zoo's locations.
     */
    fun exchangeTiles(enclosure: Enclosure, barn: Barn, animalTypeInBarnToExchange: AnimalType) {

        val game = rootService.game
        checkNotNull(game) { "game is null. cannot add tile to delivery truck" }

        this.logIfNotSimulating("Got request to exchange tiles between enclosures and barn. the animal type to exchange=${animalTypeInBarnToExchange}")

        val numCoins = game.getActivePlayer().numCoins

        check(numCoins >= 1) {
            "the player cannot choose this action. the action requires 1 coin and the " +
                    "player has $numCoins"
        }

        check(enclosure.animals.isNotEmpty() && barn.animals.isNotEmpty()) {
            "cannot exchange tiles when an " +
                    "enclosure or barn has no animals"
        }

        check(enclosure.getAnimalType() != animalTypeInBarnToExchange) { "cannot exchange the same animal type" }

        val barnAnimalsThatCanBeExchanged = barn.animals.filter { animal -> animal.type == animalTypeInBarnToExchange }

        check(barnAnimalsThatCanBeExchanged.isNotEmpty()) {
            "The barn does not contain " +
                    "this animal type $barnAnimalsThatCanBeExchanged"
        }

        check(barnAnimalsThatCanBeExchanged.size <= enclosure.animalsMax) {
            "The enclosure does not have" +
                    " enough space for all animals of type $animalTypeInBarnToExchange in the barn"
        }


        val isExpansionBoard = enclosure is ExpansionBoard
        val encIndex = if (isExpansionBoard) {
            game.getActivePlayer().getUsedExpansionBoards().indexOf(enclosure)
        } else {
            game.getActivePlayer().enclosures.indexOf(enclosure)
        }
        check(encIndex != -1) {
            "could not find the enclosure in the player's enclosures or used" +
                    " expansion boards"
        }

        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val enclosureInNewGame = if (isExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[encIndex]
        } else {
            activePlayerInNewGame.enclosures[encIndex]
        }

        val enclosureAnimals = enclosureInNewGame.animals.toMutableList()
        val barnAnimalsToExchange =
            activePlayerInNewGame.barn.animals.filter { animal -> animal.type == animalTypeInBarnToExchange }


        // add the animals to the enclosure from the barn
        enclosureInNewGame.animals.clear()
        enclosureInNewGame.animals.addAll(barnAnimalsToExchange)

        // add the animals to the barn from the enclosure
        activePlayerInNewGame.barn.animals.removeAll(barnAnimalsToExchange.toSet())
        activePlayerInNewGame.barn.animals.addAll(enclosureAnimals)

        // update coins
        newGame.coinsInBank += 1
        activePlayerInNewGame.numCoins -= 1

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterTileMoved() }

        this.switchToNextPossiblePlayerOrEndRound()
    }

    /**
     * - the player discards either an (animalTile or vendingStallTile) of his choice from his barn
     * - the tile should come from the barn
     * - the action costs 2 coins
     */
    fun discardTile(tile: Tile) {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot add tile to delivery truck" }

        this.logIfNotSimulating("Got request to discard the tile $tile")

        val numCoins = game.getActivePlayer().numCoins
        check(numCoins >= 2) {
            "the player cannot choose this action. the action requires 2 coins and" +
                    " the player has $numCoins"
        }

        check(tile is AnimalTile || tile is VendingStallTile) {
            "the tile should be either an animal tile" +
                    " or vending stall tile. got another tile type"
        }


        val tileIndex = if (tile is AnimalTile) {
            game.getActivePlayer().barn.animals.indexOf(tile)
        } else {
            game.getActivePlayer().barn.vendingStalls.indexOf(tile)
        }

        check(tileIndex != -1) { "the provided tile does not exist in the barn" }

        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()

        // remove tile from barn
        if (tile is AnimalTile) {
            activePlayerInNewGame.barn.animals.removeAt(tileIndex)
        } else {
            activePlayerInNewGame.barn.vendingStalls.removeAt(tileIndex)
        }

        //update coin and bank
        newGame.coinsInBank += 2
        activePlayerInNewGame.numCoins -= 2

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterDiscardTile() }

        this.switchToNextPossiblePlayerOrEndRound()
    }

    /**
     * - returns if the tile stack is empty
     */
    fun isTileStackEmpty(): Boolean {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot draw" }
        val tiles = game.tileStack.tiles.filterNot { tile -> tile is AnimalTile && tile.variant == AnimalVariant.CHILD }
        return tiles.isEmpty()
    }

    /**
     * - draw returns the top tile of the stack if there is space in a truck
     * - else it throws an error
     */
    fun draw(): Tile {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot draw" }


        check(isThereTruckWithFreePlace()) { "all trucks are full. cannot draw" }


        val nonChildTiles =
            game.tileStack.tiles.filterNot { tile -> tile is AnimalTile && tile.variant == AnimalVariant.CHILD }

        return if (nonChildTiles.isEmpty()) {
            check(game.endStack.tiles.isNotEmpty()) { "the end stack is empty. cannot draw" }
            val newGame = this.createNewGameState(game)
            rootService.game = newGame
            val tile = newGame.endStack.tiles[0]
            this.logIfNotSimulating("Draw tile from end stack. tile=${tile}")
            newGame.endStack.tiles.remove(tile)
            tile
        } else {
            val newGame = this.createNewGameState(game)
            rootService.game = newGame
            val nonChildTilesInNewGame =
                newGame.tileStack.tiles.filterNot { tile -> tile is AnimalTile && tile.variant == AnimalVariant.CHILD }
            val tile = nonChildTilesInNewGame[0]
            this.logIfNotSimulating("Draw tile from tile stack. tile=${tile}")
            newGame.tileStack.tiles.remove(tile)
            tile
        }
    }


    /**
     * - adds the given tile to a delivery truck
     * - the tile either comes from the tile stack or from the end stack
     */
    fun addTileToTruck(tile: Tile, truck: DeliveryTruck) {

        val game = rootService.game
        checkNotNull(game) { "game is null. cannot add tile to delivery truck" }

        this.logIfNotSimulating("Got request to add a tile to a truck. tile=${tile}")

        // check if truck is full
        check(truck.hasSpace()) { "cannot add a tile to this delivery truck. it's full" }

        // get the truck index in the game
        val truckIndex = game.deliveryTrucks.indexOf(truck)
        check(truckIndex != -1) { "the given delivery truck does not exist in the game" }


        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val tileInNewGame = (tile as Cloneable<Tile>).clone()
        val truckInNewGame = newGame.deliveryTrucks[truckIndex]

        // add the tile to the truck
        truckInNewGame.addTile(tileInNewGame)


        // needed for **AI**
        // Remove tile from tileCount tileStack (because the the tile was removed from the normal tile stack)
        newGame.tileCount!!.draw(tile)
        // end for **AI**

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterUndoRedo() }

        this.switchToNextPossiblePlayerOrEndRound()

    }


    /**
     * - take a delivery truck and pass for the rest of the round.
     */
    fun takeTruck(truck: DeliveryTruck) {

        val game = rootService.game
        checkNotNull(game) { "game is null. cannot take truck" }

        this.logIfNotSimulating("Got request to take the truck $truck")

        // check if truck is empty
        check(truck.tiles.isNotEmpty()) { "cannot take a truck with no tiles" }


        // get the truck index in the game
        val truckIndex = game.deliveryTrucks.indexOf(truck)
        check(truckIndex != -1) { "the given truck does not exist in the game" }


        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame
        val truckInNewGame = newGame.deliveryTrucks[truckIndex]

        // get the tiles to add
        val tilesToAdd: MutableList<Tile> = truckInNewGame.tiles.toMutableList()

        val activePlayerInNewGame = newGame.getActivePlayer()
        // set the truck to the player
        activePlayerInNewGame.takeTruck(truckInNewGame)

        truckInNewGame.tiles.clear()

        // add the tiles to the zoo
        for (tile in tilesToAdd) {
            this.addTileToZoo(tile, activePlayerInNewGame, newGame)
        }

        // save the last player to take a truck
        newGame.lastPlayerToTakeDeliveryTruckIndex = newGame.players.indexOf(activePlayerInNewGame)

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterTakeTruck(truckInNewGame) }

        this.switchToNextPossiblePlayerOrEndRound()
    }


    /**
     * - adds the given tile from the truck to the player's barn and calls a refreshable after it's done
     */
    fun addTileFromTruckToPlayerBarn(tile: Tile, truck: DeliveryTruck) {
        val game = rootService.game
        checkNotNull(game) { "game is null" }

        this.logIfNotSimulating("Got request to add a tile from a truck to a player's barn. tile=${tile} truck=${truck}")

        check(tile is AnimalTile || tile is VendingStallTile) {
            "the given tile should be either" +
                    " an animal or vending stall."
        }
        check(game.deliveryTrucks.contains(truck)) { "the given game does not contain the given truck" }
        check(truck.tiles.contains(tile)) { "the given truck does not contain the given coin tile" }


        val truckIndex = game.deliveryTrucks.indexOf(truck)
        val tileIndex = truck.tiles.indexOf(tile)


        val newGame = this.createNewGameState(game)
        rootService.game = newGame
        val activePlayerInNewGame = newGame.getActivePlayer()
        val truckInNewGame = newGame.deliveryTrucks[truckIndex]
        val tileInNewGame = truckInNewGame.tiles[tileIndex]

        // rm the tile from the truck
        truckInNewGame.tiles.removeAt(tileIndex)

        // add the tile to the barn
        if (tileInNewGame is AnimalTile) {
            activePlayerInNewGame.barn.animals.add(tileInNewGame)
        } else if (tileInNewGame is VendingStallTile) {
            activePlayerInNewGame.barn.vendingStalls.add(tileInNewGame)
        }

        this.saveThatPlayerTookTruck(newGame, activePlayerInNewGame, truckInNewGame)

        if (!rootService.simulate)
            this.onAllRefreshables {
                refreshAfterTileWasAddedFromTruckToPlayerBarn(
                    activePlayerInNewGame as Player,
                    truckInNewGame
                )
            }

        if (truckInNewGame.tiles.isEmpty()) {
            // the tile was the last one on the truck. end the turn
            this.switchToNextPossiblePlayerOrEndRound()
        }
    }

    /**
     * - adds all coin tiles from the truck to the player
     * - if the truck does not contain any coin tiles. an error is thrown
     */
    fun addAllCoinTilesFromTruckToPlayer(truck: DeliveryTruck) {
        val game = rootService.game
        checkNotNull(game) { "game is null" }

        this.logIfNotSimulating("Got request to add all coin tiles from truck to the player. truck=${truck}")

        check(game.deliveryTrucks.contains(truck)) { "the given game does not contain the given truck" }
        check(truck.tiles.isNotEmpty()) { "the given truck does not have any tile" }

        val coinTiles = truck.tiles.filterIsInstance<CoinTile>()

        check(coinTiles.isNotEmpty()) { "the given truck does not have any coin tile" }


        val truckIndex = game.deliveryTrucks.indexOf(truck)

        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val truckInNewGame = newGame.deliveryTrucks[truckIndex]

        val coinTilesInNewGame = truckInNewGame.tiles.filterIsInstance<CoinTile>()
        for (coinTile in coinTilesInNewGame) {
            activePlayerInNewGame.numCoins += 1
        }

        truckInNewGame.tiles.removeAll(coinTilesInNewGame.toSet())


        this.saveThatPlayerTookTruck(newGame, activePlayerInNewGame, truckInNewGame)

        if (!rootService.simulate)
            this.onAllRefreshables {
                refreshAfterCoinTileWadAddedFromTruckToPlayer(
                    activePlayerInNewGame as Player,
                    truckInNewGame
                )
            }

        if (truckInNewGame.tiles.isEmpty()) {
            // the tile was the last one on the truck. end the turn
            this.switchToNextPossiblePlayerOrEndRound()
        }
    }


    /**
     * - adds the given coin tile from the truck to the player and calls a refreshable after it's done
     */
    fun addCoinTileFromTruckToPlayer(tile: CoinTile, truck: DeliveryTruck) {
        val game = rootService.game
        checkNotNull(game) { "game is null" }

        this.logIfNotSimulating("Got request to add a coin tile from truck to the player. truck=${truck}")

        check(game.deliveryTrucks.contains(truck)) { "the given game does not contain the given truck" }
        check(truck.tiles.contains(tile)) { "the given truck does not contain the given coin tile" }


        val truckIndex = game.deliveryTrucks.indexOf(truck)
        val tileIndex = truck.tiles.indexOf(tile)

        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val truckInNewGame = newGame.deliveryTrucks[truckIndex]
        truckInNewGame.tiles.removeAt(tileIndex)

        activePlayerInNewGame.numCoins += 1

        this.saveThatPlayerTookTruck(newGame, activePlayerInNewGame, truckInNewGame)

        if (!rootService.simulate)
            this.onAllRefreshables {
                refreshAfterCoinTileWadAddedFromTruckToPlayer(
                    activePlayerInNewGame as Player,
                    truckInNewGame
                )
            }

        if (truckInNewGame.tiles.isEmpty()) {
            // the tile was the last one on the truck. end the turn
            this.switchToNextPossiblePlayerOrEndRound()
        }
    }

    /**
     * - adds the given animal or vending stall tile from the truck to the player's
     *  enclosure and calls a refreshable when it's done
     */
    fun addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
        tile: Tile,
        truck: DeliveryTruck,
        enclosure: Enclosure
    ) {

        // validation
        val game = rootService.game
        checkNotNull(game) { "game is null" }
        val player = game.getActivePlayer()

        this.logIfNotSimulating("Got request to add animal or vending stall tile from a truck to a player's enclosure. tile=${tile} truck=${truck}")

        check(game.deliveryTrucks.contains(truck)) { "the given game does not contain the given truck" }
        if (enclosure is ExpansionBoard) {
            check(
                player.getUsedExpansionBoards().contains(enclosure)
            ) { "the given player does not have the given expansion board" }

        } else {
            check(player.enclosures.contains(enclosure)) { "the given player does not have the given enclosure" }
        }
        check(truck.tiles.contains(tile)) { "the given truck does not contain the given coin tile" }
        check(tile is AnimalTile || tile is VendingStallTile) {
            "the given tile should be either" +
                    " an animal or vending stall"
        }


        val truckIndex = game.deliveryTrucks.indexOf(truck)
        val tileIndex = truck.tiles.indexOf(tile)


        if (tile is AnimalTile) {
            check(enclosure.hasSpaceForAnimal()) { "the enclosure does not have enough space for animals" }
        }
        if (tile is VendingStallTile) {
            check(enclosure.hasSpaceForVendingStall()) { "the enclosure does not have enough space for vending stalls" }
        }


        val isExpansionBoard = enclosure is ExpansionBoard
        val enclosureIndex = if (isExpansionBoard) {
            player.getUsedExpansionBoards().indexOf(enclosure)
        } else {
            player.enclosures.indexOf(enclosure)
        }

        val newGame = this.createNewGameState(game)
        rootService.game = newGame
        val activePlayerInNewGame = newGame.getActivePlayer()
        val truckInNewGame = newGame.deliveryTrucks[truckIndex]
        val tileInNewGame = truckInNewGame.tiles[tileIndex]
        val enclosureInNewGame = if (isExpansionBoard) {
            activePlayerInNewGame.getUsedExpansionBoards()[enclosureIndex]
        } else {
            activePlayerInNewGame.enclosures[enclosureIndex]
        }

        if (tileInNewGame is AnimalTile) {
            enclosureInNewGame.animals.add(tileInNewGame)
            this.handleCaseWhenTileWasAddedToLastEmptySpace(
                newGame,
                enclosureInNewGame,
            )
            this.handleCaseWhenAnimalTileMayProduceAnOffspring(
                newGame,
                enclosureInNewGame
            )
        } else if (tileInNewGame is VendingStallTile) {
            enclosureInNewGame.vendingStalls.add(tileInNewGame)
            this.handleCaseWhenTileWasAddedToLastEmptySpace(
                newGame,
                enclosureInNewGame,
            )
        }

        truckInNewGame.tiles.removeAt(tileIndex)
        this.saveThatPlayerTookTruck(newGame, activePlayerInNewGame, truckInNewGame)

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterEnclosureChanged(enclosureInNewGame) }

        if (truckInNewGame.tiles.isEmpty()) {
            // the tile was the last one on the truck. end the turn
            this.switchToNextPossiblePlayerOrEndRound()
        }
    }


    /**
     * saves that a player took the given truck
     */
    private fun saveThatPlayerTookTruck(game: Game, player: PlayerBase, truck: DeliveryTruck) {
        this.logIfNotSimulating("Saving that the player ${player.name} took the truck $truck")
        player.takeTruck(truck)
        game.lastPlayerToTakeDeliveryTruckIndex = game.players.indexOf(player)
    }


    /**
     *
     */
    fun showHint() {
        this.logIfNotSimulating("Showing a hint to a player")
        val currentAIActionService = this.rootService.aiActionService ?: return
        val currentGame = this.rootService.game ?: return

        // set receivedHint
        currentGame.getActivePlayer().receivedHint = true
        currentAIActionService.hint(currentGame.getActivePlayer() as Player)
    }

    /**
     * - the player turns his expansion board face up and pays 3 coins
     *   to the bank.
     * - this expands his zoo by one enclosure and one stall space.
     */
    fun expandZoo() {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot add tile to delivery truck" }

        this.logIfNotSimulating("Got request to expand the zoo")

        val numCoins = game.getActivePlayer().numCoins
        check(numCoins >= 3) {
            "the player cannot choose this action. the action requires 3 coins" +
                    " and the player has $numCoins"
        }

        val allExpansionBoardsUsed =
            game.getActivePlayer().expansionBoards.none { board -> !board.isUsed }

        check(!allExpansionBoardsUsed) { "the player has already used all of the expansion boards" }

        // update the linked list of saved game states (this action creates a new state)
        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val activePlayerInNewGame = newGame.getActivePlayer()
        val board = activePlayerInNewGame.expansionBoards.filter { board -> !board.isUsed }[0]
        board.isUsed = true


        //update coin and bank
        newGame.coinsInBank += 3
        activePlayerInNewGame.numCoins -= 3

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterUsingANewExpansionBoard(activePlayerInNewGame as Player) }

        this.switchToNextPossiblePlayerOrEndRound()
    }

    /**
     * - the player takes the animal tile of his choice from the barn of another player and adds it to his own zoo.
     * - the tile should come from the barn of the other player
     * - the action costs 2 coins
     */
    fun purchaseTile(otherPlayer: PlayerBase, tile: Tile) {
        val game = rootService.game
        checkNotNull(game) { "game is null. cannot purchase a tile" }

        this.logIfNotSimulating("Got request purchase the tile $tile from the barn of the player ${otherPlayer.name}")

        if (tile is AnimalTile) {
            check(otherPlayer.barn.animals.isNotEmpty()) {
                "the barn of the other player has no animals." +
                        " cannot purchase a tile from it"
            }
            check(otherPlayer.barn.animals.contains(tile)) {
                "the provided animal tile does not exist" +
                        " in the barn of the other player"
            }
        } else if (tile is VendingStallTile) {
            check(otherPlayer.barn.vendingStalls.size > 0) {
                "the barn of the other player has" +
                        " no vending stalls. cannot purchase a tile from it"
            }
            check(otherPlayer.barn.vendingStalls.contains(tile)) {
                "the provided vending stall " +
                        "tile does not exist in the barn of the other player"
            }
        }

        val newGame = this.createNewGameState(game)
        rootService.game = newGame

        val otherPlayerIndex = game.players.indexOf(otherPlayer)

        val activePlayerInNewGame = newGame.getActivePlayer()
        val otherPlayerInNewGame = newGame.players[otherPlayerIndex]

        if (tile is AnimalTile) {
            val animalTileIndex = otherPlayer.barn.animals.indexOf(tile)

            val animalTileInNewGame = otherPlayerInNewGame.barn.animals[animalTileIndex]

            // rm the animal tile from the other player's barn
            otherPlayerInNewGame.barn.animals.remove(animalTileInNewGame)

            // add the animal tile to the barn of the buying player
            activePlayerInNewGame.barn.animals.add(animalTileInNewGame)

            if (!rootService.simulate)
                this.onAllRefreshables { refreshAfterBuying(animalTileInNewGame) }

        } else {
            val vendingStallTileIndex = otherPlayer.barn.vendingStalls.indexOf(tile)

            val vendingStallTileInNewGame = otherPlayerInNewGame.barn.vendingStalls[vendingStallTileIndex]

            // rm the vending stall tile from the other player's barn
            otherPlayerInNewGame.barn.vendingStalls.remove(vendingStallTileInNewGame)

            // add the vending stall tile to the barn of the buying player
            activePlayerInNewGame.barn.vendingStalls.add(vendingStallTileInNewGame)

            if (!rootService.simulate)
                this.onAllRefreshables { refreshAfterBuying(vendingStallTileInNewGame) }

        }


        //update coin and bank
        newGame.coinsInBank += 1
        otherPlayerInNewGame.numCoins += 1
        activePlayerInNewGame.numCoins -= 2

        this.switchToNextPossiblePlayerOrEndRound()
    }


    /**
     * - switches the active player to the next possible player (i.e. another player which has not yet
     * taken a delivery truck)
     * - or ends the round if all players have already takes delivery trucks
     * - or ends the game if all players have taken delivery trucks and a tile was drawn from the end game pile
     */
    private fun switchToNextPossiblePlayerOrEndRound() {
        this.logIfNotSimulating("Switching to the next possible player or ending the round")

        //wait for aiPlayer to make move
        if (hasAIPlayerMadeAMove)
            return

        val game = rootService.game
        checkNotNull(game) { "game is null. cannot switch to next possible player" }

        // all players have taken a truck. end the round
        if (game.haveAllPlayersTakenDeliveryTrucks()) {
            this.logIfNotSimulating("All players have passed. Either ending the game or starting a new round")
            if (rootService.gameService.hasLastPileStarted()) {
                // all players have taken trucks. end the game
                this.rootService.gameService.handleEndGame()
            } else {
                rootService.gameService.startNewRound()
            }
            return
        }


        // there are still players who have not yet taken trucks. switch the active player
        game.switchActivePlayer()
        var newActivePlayer = game.getActivePlayer()
        while (newActivePlayer.tookDeliveryTruck) {
            game.switchActivePlayer()
            newActivePlayer = game.getActivePlayer()
        }
        game.activePlayerIndex = game.players.indexOf(newActivePlayer)

        this.logIfNotSimulating("Switched the active player. New active player is ${newActivePlayer.name}")

        // execute AI move
        preRoundChecksAndExecuteAIMove()

        if (!rootService.simulate)
            this.onAllRefreshables { refreshAfterNewTurn() }
    }

    /**
     * - creates a new node of gameState in the linked list of game states
     */
    private fun createNewGameState(game: Game): Game {
        this.logIfNotSimulating("Creating a new game state")
        val newGame = game.clone()
        game.connect(newGame)
        return newGame
    }

    /**
     *
     * Whenever a player places a tile on the last empty space in one of his enclosures, he receives
     * bonus coins from the bank equal to the number shown in the enclosure.
     * The player may also take coin tiles instead, should there be any in the bank. If the bank is
     * empty, then the player receives nothing.
     */
    private fun handleCaseWhenTileWasAddedToLastEmptySpace(
        game: Game,
        dstEnclosure: Enclosure,
    ) {
        this.logIfNotSimulating("Handling the case when a tile is added to last empty space")
        val activePlayer = game.getActivePlayer()
        if (!dstEnclosure.hasSpaceForAnimal()) {
            this.logIfNotSimulating("Trying to give a player coins from the bank because a tile was added to last empty space")
            // the destination enclosure does not have empty spaces which means the tile was added to the last space
            if (game.coinsInBank >= dstEnclosure.coinsToGetWhenFull) {
                game.coinsInBank -= dstEnclosure.coinsToGetWhenFull
                activePlayer.numCoins += dstEnclosure.coinsToGetWhenFull
            }
        } else {
            this.logIfNotSimulating("Tile was not added to last empty space so do nothing")
        }
    }


    /**
     * There are 2 fertile males and females of each animal type, distinguishable
     * by the small symbols on their tiles.
     * When the appropriate partner for a fertile male or female is added to the same enclosure,
     * the two immediately produce an offspring. The player takes one of the appropriate offspring
     * tiles from the supply and places it onto an empty space in the enclosure. Once placed, the
     * offspring tile is treated like any other animal tile.
     * If there isn’t any space left in the enclosure, then the player must place the offspring in his
     * barn instead.
     * Note: All that is required is for the male and female to be in the same enclosure –
     * they do not need to be next to each other.
     * Each male and female can produce an offspring only once. So, for example, no offspring
     * is produced when a third fertile animal is added to an enclosure that already has a fertile
     * pair. Another offspring would only be produced when another partner, creating a second
     * fertile pair, is added to the enclosure.
     * Important: Pairs only produce offspring in enclosures, not in barns or on delivery trucks.
     */
    private fun handleCaseWhenAnimalTileMayProduceAnOffspring(
        game: Game,
        dstEnclosure: Enclosure
    ) {
        val activePlayer = game.getActivePlayer()

        this.logIfNotSimulating("Handling the case when an animal tile may produce an offspring in enclosure")


        val malesWhichNotMatedYet =
            dstEnclosure.animals.filter { animal -> animal.variant == AnimalVariant.MALE && !animal.hasMated }
        val femalesWhichNotMatedYet =
            dstEnclosure.animals.filter { animal -> animal.variant == AnimalVariant.FEMALE && !animal.hasMated }

        if (malesWhichNotMatedYet.isNotEmpty() && femalesWhichNotMatedYet.isNotEmpty()) {

            this.logIfNotSimulating("An offspring is produced")
            // produce a baby lol
            malesWhichNotMatedYet[0].hasMated = true
            femalesWhichNotMatedYet[0].hasMated = true

            // get the child from the offspring
            val offsprings =
                game.tileStack.getOffsprings().filter { offspring -> offspring.type == dstEnclosure.getAnimalType() }
            check(offsprings.isNotEmpty()) {
                "there are no offsprings of type ${dstEnclosure.getAnimalType()} in the" +
                        " tile stack. cannot give a player an " +
                        "offspring as a result of mating."
            } // should not happen right ?

            // add the child to the enclosure or the barn.
            if (dstEnclosure.hasSpaceForAnimal()) {
                dstEnclosure.animals.add(offsprings[0])
            } else {
                activePlayer.barn.animals.add(offsprings[0])
            }
            // remove the child from the tile stack
            game.tileStack.tiles.remove(offsprings[0])
        } else {

            this.logIfNotSimulating("An offspring is not produced")
        }

    }


    /**
     * - adds the given tile to the zoo of the given player
     */
    private fun addTileToZoo(tile: Tile, player: PlayerBase, game: Game) {
        this.logIfNotSimulating("Adding the tile $tile to the zoo")
        // A coin tile is added to any other coins the player has.
        // A coin tile is worth one coin. Both are equivalent and may be used interchangeably.
        if (tile is CoinTile) {
            player.numCoins += 1
        }

        // A vending stall may be placed on an empty stall space.
        // If there aren’t any stall spaces left, then the player must place the tile in his barn instead.
        if (tile is VendingStallTile) {
            var didAddToEnc = false
            for (enc in player.getEnclosuresWithUsedExpansionBoards()) {
                if (enc.hasSpaceForVendingStall()) {
                    enc.vendingStalls.add(tile)
                    this.handleCaseWhenTileWasAddedToLastEmptySpace(
                        game,
                        enc
                    )
                    didAddToEnc = true
                    break
                }
            }
            if (!didAddToEnc) {
                player.barn.vendingStalls.add(tile)
            }
        }


        // An animal tile may be placed either on an empty enclosure space or in the barn.
        // Important: No enclosure may contain more than one type of animal tile. However, a player
        // may have multiple enclosures containing the same animal type.
        // If there aren’t any legal spaces for an animal tile left in the enclosures, then the player
        // must place the animal in his barn instead.
        if (tile is AnimalTile) {
            var didAddToEnc = false
            for (enc in player.getEnclosuresWithUsedExpansionBoards()) {
                if (enc.animals.isEmpty() || (enc.getAnimalType() == tile.type && enc.hasSpaceForAnimal())) {
                    enc.animals.add(tile)
                    this.handleCaseWhenTileWasAddedToLastEmptySpace(
                        game,
                        enc
                    )
                    this.handleCaseWhenAnimalTileMayProduceAnOffspring(
                        game,
                        enc
                    )
                    didAddToEnc = true
                    break
                }
            }
            if (!didAddToEnc) {
                player.barn.animals.add(tile)
            }

        }
    }


    /**
     * this method is called before every round and
     * executes the AIMove if player = PLayerAI
     * the simulationSpeed in given in unit seconds
     * this is not executed for the simulated rootService
     */
    fun preRoundChecksAndExecuteAIMove() {
        if (rootService.simulate || this.hasAIPlayerMadeAMove)
            return

        this.logIfNotSimulating("Doing pre round checks and executing an ai move")

        val currentGame = rootService.game ?: return
        val aiActionService = rootService.aiActionService ?: return
        val currentPlayer = currentGame.getActivePlayer()

        if (currentGame.gameState == GameState.FINISHED)
            return

        // execute AIMove
        if (currentPlayer is PlayerAI) {
            this.hasAIPlayerMadeAMove = true
            this.onAllRefreshables { refreshAfterUndoRedo() }

            val start = System.currentTimeMillis()
            val moveString = aiActionService.executeAIMove(currentPlayer, currentPlayer.difficulty)
            this.onAllRefreshables { refreshAfterAIMadeAMove(moveString) }

            val calculationDurationInMs = (System.currentTimeMillis() - start).toInt()
            val durationString = String.format("%.2f", calculationDurationInMs / 1000.0)
            logger.log("AI move calculation took $durationString seconds")
            val sleepDuration = max(0, currentGame.simulationSpeed * 1000 - calculationDurationInMs)
            Timer().schedule(SimulationTimer(this.rootService), sleepDuration.toLong())
        }
    }

    /**
     * this class is used to execute
     * switchPLayer after simulation timer has ended
     */
    class SimulationTimer(val rootService: RootService) : TimerTask() {
        override fun run() {
            try {
                Platform.runLater {
                    rootService.playerActionService.hasAIPlayerMadeAMove = false
                    rootService.playerActionService.switchToNextPossiblePlayerOrEndRound()
                    rootService.playerActionService.onAllRefreshables { refreshAfterUndoRedo() }
                }
            } catch (_: java.lang.IllegalStateException) {
                // Some tests may cause this exception because they run without a gui, if this is
                // the case, we can ignore it.
            }
        }
    }


    /**
     * check if there is a place in Truck
     */
    private fun isThereTruckWithFreePlace(): Boolean {
        val game = rootService.game
        checkNotNull(game) { "game is null" }
        this.logIfNotSimulating("Checking if there is a truck with free space")
        for (truck in game.deliveryTrucks) {
            if (truck.tiles.size < truck.maxSize) {
                return true
            }
        }
        return false
    }

    fun logIfNotSimulating(message: String) {
        if (!rootService.simulate)
            this.logger.log(message)
    }

}

