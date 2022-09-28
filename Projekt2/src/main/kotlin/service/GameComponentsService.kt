package  service

import entity.*


/**
 * - a shuffler shuffles stuff (mixes them around)
 */
interface Shuffler<T> {
    /**
     * shuffle stuff
     */
    fun shuffle(things: MutableList<T>): MutableList<T>
}


/**
 * A default shuffler
 */
class DefaultShuffler<T> : Shuffler<T> {
    override fun shuffle(things: MutableList<T>): MutableList<T> {
        things.shuffle()
        return things
    }

}

/**
 * - An Interface for the game components service
 */
interface GameComponents {

    /**
     * - initializes the game components for the given number of players
     * - this has to be called before using the other methods in this interface
     */
    fun initForNumPlayers(numPlayers: Int): Unit

    /**
     * - gets the delivery trucks
     */
    fun getDeliveryTrucks(): MutableList<DeliveryTruck>

    /**
     * - gets the tiles for the tile stack
     */
    fun getTileStackTiles(): MutableList<Tile>

    /**
     * - gets the tiles for the end stack
     */
    fun getEndStackTiles(): MutableList<Tile>

    /**
     * - sets the class used to shuffle the tiles (mix them around)
     */
    fun setTilesShuffler(shuffler: Shuffler<Tile>): Unit

    /**
     * - gets the class used to shuffle the tiles (mix them around)
     */
    fun getTilesShuffler(): Shuffler<Tile>

    /**
     * - gets all available types in current Game
     */
    fun getAvailableTileTypes(): MutableList<Tile>
}


/**
 * - GameComponents contains the components of the game that are used by the service to fill the game properties
 * - It creates tiles and trucks based on the game specification
 * - The game service can use it to create the game on start
 */
class GameComponentsService : GameComponents {

    private var numPlayers: Int = 5
    private var didInitForNumPlayers = false
    private var animalTiles: MutableList<AnimalTile> = mutableListOf()
    private var coinTiles: MutableList<CoinTile> = mutableListOf()
    private var vendingStallsTiles: MutableList<VendingStallTile> = mutableListOf()
    private var offSprings: MutableList<AnimalTile> = mutableListOf()
    private var deliveryTrucks: MutableList<DeliveryTruck> = mutableListOf()
    private var removedAnimalTiles: MutableList<AnimalTile> = mutableListOf()

    private var tileStackTiles: MutableList<Tile> = mutableListOf()
    private var endStackTiles: MutableList<Tile> = mutableListOf()
    private var tilesShuffler: Shuffler<Tile> = DefaultShuffler()

    // needed for AI to calc statistics
    private var tileTypes: MutableList<Tile> = mutableListOf()


    private fun reset() {
        this.numPlayers = 5
        this.didInitForNumPlayers = false
        this.animalTiles = mutableListOf()
        this.coinTiles = mutableListOf()
        this.vendingStallsTiles = mutableListOf()
        this.offSprings = mutableListOf()
        this.deliveryTrucks = mutableListOf()
        this.removedAnimalTiles = mutableListOf()
        this.tileStackTiles = mutableListOf()
        this.endStackTiles = mutableListOf()
        // needed for AI to calc statistics
        this.tileTypes = mutableListOf()
    }

    /**
     * - sets up the components based on the number of players
     */
    override fun initForNumPlayers(numPlayers: Int) {
        this.reset()
        check(numPlayers in 2..5) { "the number of players should be between 2,5" }
        this.numPlayers = numPlayers
        this.didInitForNumPlayers = true
        this.setup()
    }


    /**
     * - returns the delivery trucks for the game
     */
    override fun getDeliveryTrucks(): MutableList<DeliveryTruck> {
        check(this.didInitForNumPlayers) { "make sure to call initForNumPlayers first to setup the components" }
        return this.deliveryTrucks
    }

    /**
     * - returns the tile stack tiles for the game
     */
    override fun getTileStackTiles(): MutableList<Tile> {
        check(this.didInitForNumPlayers) { "make sure to call initForNumPlayers first to setup the components" }
        return this.tileStackTiles
    }


    /**
     * - returns the end stack tiles for the game
     */
    override fun getEndStackTiles(): MutableList<Tile> {
        check(this.didInitForNumPlayers) { "make sure to call initForNumPlayers first to setup the components" }
        return this.endStackTiles
    }

    /**
     * - sets the tiles shuffler
     */
    override fun setTilesShuffler(shuffler: Shuffler<Tile>) {
        this.tilesShuffler = shuffler
    }

    /**
     * - gets the tiles shuffler
     */
    override fun getTilesShuffler(): Shuffler<Tile> {
        return this.tilesShuffler
    }


    /**
     * sets up the game components
     */
    private fun setup() {

        //for AI
        generateAllTypes()

        // 12 coin tiles
        this.coinTiles = List(12) { CoinTile() }.toMutableList()

        // 12 vending stalls (3 for each type)
        VendingStallType.values().forEach { type ->
            this.vendingStallsTiles.addAll(List(3) { VendingStallTile(type) })
        }

        // 16 offsprings (2 of each animal type)
        AnimalType.values().forEach { type ->
            this.offSprings.addAll(List(2) { AnimalTile(AnimalVariant.CHILD, type) })
        }

        // 88 animal tiles
        AnimalType.values().forEach { type ->
            // 11 tiles for one animal type (2 males, 2 females, 7 neutral)
            this.animalTiles.addAll(List(2) { 0 }.map { AnimalTile(AnimalVariant.MALE, type) })
            this.animalTiles.addAll(List(2) { 0 }.map { AnimalTile(AnimalVariant.FEMALE, type) })
            this.animalTiles.addAll(List(7) { 0 }.map { AnimalTile(AnimalVariant.NEUTRAL, type) })
        }

        // 5 delivery trucks
        this.deliveryTrucks = List(5) { DeliveryTruck() }.toMutableList()

        // shuffle the content
        this.tilesShuffler.shuffle(this.animalTiles as MutableList<Tile>)
        this.tilesShuffler.shuffle(this.offSprings as MutableList<Tile>)
        this.tilesShuffler.shuffle(this.vendingStallsTiles as MutableList<Tile>)

        this.updateForNumPlayers(this.numPlayers)

        // set the tile stack and end stack
        val squareTiles =
            (this.animalTiles + this.coinTiles + this.vendingStallsTiles).toMutableList() as MutableList<Tile>
        this.tilesShuffler.shuffle(squareTiles)


        this.endStackTiles = squareTiles.subList(0, MAX_TILES_ON_LAST_PILE)
        this.tilesShuffler.shuffle(this.endStackTiles)

        this.tileStackTiles =
            (squareTiles.subList(MAX_TILES_ON_LAST_PILE, squareTiles.size) + this.offSprings).toMutableList()
        this.tilesShuffler.shuffle(this.tileStackTiles)
    }


    // updates the components based on the number of players (like removing some tiles or filling the trucks with
    // fixed positioned tiles for 2 players variant)
    private fun updateForNumPlayers(numPlayers: Int) {
        when (numPlayers) {
            3 -> {

                // all animal and offspring tiles of two types are removed from the game
                this.removeAnimalType(AnimalType.ELEPHANT)
                this.removeOffspringType(AnimalType.ELEPHANT)
                this.removeTileType(AnimalType.ELEPHANT)

                this.removeAnimalType(AnimalType.KANGAROO)
                this.removeOffspringType(AnimalType.KANGAROO)
                this.removeTileType(AnimalType.KANGAROO)

                // use only 3 trucks
                this.deliveryTrucks.removeAt(0)
                this.deliveryTrucks.removeAt(1)

            }
            4 -> {

                // all animal and offspring tiles of one type are removed from the game
                this.removeAnimalType(AnimalType.KANGAROO)
                this.removeOffspringType(AnimalType.KANGAROO)
                this.removeTileType(AnimalType.KANGAROO)

                // use only 4 trucks
                this.deliveryTrucks.removeAt(0)
            }
            2 -> {

                // all animal and offspring tiles of three type are removed from the game
                this.removeAnimalType(AnimalType.ELEPHANT)
                this.removeOffspringType(AnimalType.ELEPHANT)
                this.removeTileType(AnimalType.ELEPHANT)

                this.removeAnimalType(AnimalType.KANGAROO)
                this.removeOffspringType(AnimalType.KANGAROO)
                this.removeTileType(AnimalType.KANGAROO)

                this.removeAnimalType(AnimalType.FLAMINGO)
                this.removeOffspringType(AnimalType.FLAMINGO)
                this.removeTileType(AnimalType.FLAMINGO)

                // use only 3 trucks
                this.deliveryTrucks.removeAt(0)
                this.deliveryTrucks.removeAt(1)

                // Three of the tiles removed from the game are drawn at random and used to block certain delivery
                // truck spaces for the entire game:
                // 1 face-down tile is placed onto one of the trucks. 2 face-down tiles are placed onto another truck.

                // first truck is filled with two tiles
                this.deliveryTrucks[0].maxSize = 1
                // first truck is filled with one tile
                this.deliveryTrucks[1].maxSize = 2
            }
        }
    }


    // removes the animal tiles of the given animal type
    private fun removeAnimalType(type: AnimalType) {
        val tilesToRemove = this.animalTiles.filter { tile -> tile.type === type }
        this.animalTiles.removeAll(tilesToRemove.toSet())
        this.removedAnimalTiles.addAll(tilesToRemove)
    }

    // removes the offspring tiles of the given animal type
    private fun removeOffspringType(type: AnimalType) {
        val offspringsToRemove = this.offSprings.filter { offspring -> offspring.type === type }
        this.offSprings.removeAll(offspringsToRemove.toSet())
        this.removedAnimalTiles.addAll(offspringsToRemove)
    }


    // removes the tile from [tileTypes]
    private fun removeTileType(type: AnimalType) {
        val tilesToRemove = mutableListOf<Tile>()

        for (tile in this.tileTypes) {
            if (tile is AnimalTile) {
                if (tile.type === type) {
                    tilesToRemove.add(tile)
                }
            }
        }
        this.tileTypes.removeAll(tilesToRemove.toSet())
    }

    // get all available tile types in current Game
    override fun getAvailableTileTypes(): MutableList<Tile> {
        return this.tileTypes
    }

    // generates a List with all types used in Game (used in AI)
    private fun generateAllTypes() {
        this.tileTypes.clear()

        this.tileTypes.add(CoinTile())

        VendingStallType.values().forEach { type ->
            this.tileTypes.add(VendingStallTile(type))
        }

        AnimalType.values().forEach { type ->
            this.tileTypes.add(AnimalTile(AnimalVariant.MALE, type))
            this.tileTypes.add(AnimalTile(AnimalVariant.FEMALE, type))
            this.tileTypes.add(AnimalTile(AnimalVariant.NEUTRAL, type))
        }

    }

}
