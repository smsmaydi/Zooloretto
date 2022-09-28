package  service

import FileSystem
import Logger
import entity.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic


/**
 * A game loader service
 */
class GameLoaderService(val fs: FileSystem, private val rootService: RootService) {

    private val logger: Logger = Logger("GameLoaderService")

    // the data is saved in <path to project root>/gameState directory
    private val stateDirPath = "gameState"

    // the filename of the active game is saved in this file.
    // we use this file when loading the game later to set the rootService.game to point to the right game in the linked list
    private val activeGameFileName = "activeGame.txt"

    // the high score data is saved in gameState/highscore.json
    private val highScoreFileName = "highscore.json"

    // this module is used for (serialization,deserialization)
    private var jsonModule: Json

    init {
        // create a json module that can (serialize,deserialize) data
        val serializerModule = SerializersModule {
            fun PolymorphicModuleBuilder<Tile>.registerProjectSubclasses() {
                subclass(AnimalTile::class, AnimalTile.serializer())
                subclass(CoinTile::class, CoinTile.serializer())
                subclass(MarkerTile::class, MarkerTile.serializer())
                subclass(VendingStallTile::class, VendingStallTile.serializer())
            }

            fun PolymorphicModuleBuilder<PlayerBase>.registerPlayerBaseSubclasses() {
                subclass(Player::class, Player.serializer())
                subclass(PlayerAI::class, PlayerAI.serializer())
            }
            polymorphic(Tile::class) { registerProjectSubclasses() }
            polymorphic(PlayerBase::class) { registerPlayerBaseSubclasses() }
            polymorphic(Any::class) { registerProjectSubclasses() }
        }
        this.jsonModule = Json {
            serializersModule = serializerModule
            classDiscriminator = "type_"
            prettyPrint = true
        }

        // create the stateDir if it does not exist
        this.fs.mkdir(this.stateDirPath)

    }


    private fun getGameAsString(game: Game): String {
        return this.jsonModule.encodeToString(game)
    }

    private fun getHighscoreAsString(highScore: HighScore): String {
        return this.jsonModule.encodeToString(highScore)
    }

    private fun loadHighscoreFromString(highScoreAsString: String): HighScore {
        return this.jsonModule.decodeFromString(highScoreAsString)
    }

    private fun loadGameFromString(gameAsString: String): Game {
        return this.jsonModule.decodeFromString(gameAsString)
    }


    /**
     * - returns a list of the names of all saved games
     */
    fun getSavedGames(): List<String> {
        val games = this.fs.listDir(
            this.stateDirPath
        ).filter { name -> name != this.highScoreFileName }

        this.logger.log("Getting all saved games. They are $games")

        return games
    }

    /**
     * - saves the given game in the file system
     */
    fun save(game: Game) {

        this.logger.log("Saving a game with the name ${game.name}")

        // save the current game file name to a separate file (when loading the game later.
        // use the data here to set the rootService.game to point to the right game in the linked list of state)
        this.fs.writeFile(
            this.fs.joinPath(this.stateDirPath, game.name, this.activeGameFileName),
            game.fileName
        )

        // get the root
        var root: Game? = game
        while (root?.prevGame !== null) {
            root = root.prevGame!!
        }

        // save each game instance in the linked list as a json file
        while (root !== null) {
            val gameAsString = this.getGameAsString(root)
            this.fs.writeFile(
                this.fs.joinPath(this.stateDirPath, root.name, "${root.fileName}.json"),
                gameAsString
            )
            root = root.nextGame
        }

    }


    /**
     * - loads the game from the file system using the game name
     * - returns the root game
     */
    fun load(gameName: String): Game {

        this.logger.log("Loading a game with the name $gameName")

        val gameStateFiles = this.fs.listDir(this.fs.joinPath(this.stateDirPath, gameName))
            .filter { fileName -> fileName != this.activeGameFileName }

        val indexes = gameStateFiles.map { it.removeSuffix(".json").toInt() }.sorted()
        val jsonTexts =
            indexes.map { this.fs.readFile(this.fs.joinPath(stateDirPath, gameName, "${it}.json")) }
        val games = jsonTexts.map { this.loadGameFromString(it) }
        // connect the games with each other to build the linked list of games again
        games.forEachIndexed { index, game ->
            if (index + 1 < games.size) {
                game.connect(games[index + 1])
            }
        }

        val activeGameFileName = this.fs.readFile(this.fs.joinPath(stateDirPath, gameName, activeGameFileName))
        val activeGameIndex = games.indexOfFirst { game -> game.fileName == activeGameFileName }
        if (activeGameIndex == -1) {
            throw Exception("an active game could not be found. did you change something in the gameState directory ?")
        }
        return games[activeGameIndex]
    }

    /**
     * - returns the saved high score
     */
    fun getHighScore(): HighScore {
        this.logger.log("Getting the highscore list")
        return try {
            val highScore = this.loadHighscoreFromString(
                this.fs.readFile(
                    this.fs.joinPath(
                        stateDirPath,
                        highScoreFileName
                    )
                )
            )
            highScore.sortAndKeepFirst10()
            highScore
        } catch (_: Exception) {
            this.logger.log("Highscore list was not found in the file system. Creating a new empty one")
            HighScore() // no high score list was generated yet
        }
    }

    /**
     * - saves the high score to the fs
     */
    fun saveHighScore() {
        val highScore = rootService.highScore
        checkNotNull(highScore) { "high score is null" }

        this.logger.log("Saving the high score list")

        this.fs.writeFile(
            this.fs.joinPath(stateDirPath, highScoreFileName),
            this.getHighscoreAsString(highScore)
        )
    }


    /**
     * A ConfigurationFileParsingResult
     */
    class ConfigurationFileParsingResult(
        val numPlayers: Int,
        val tileStackTiles: MutableList<Tile>,
        val endStackTiles: MutableList<Tile>
    )


    /**
     * - parses the configuration file with the path filePath (the path is relative to the project root dir)
     * - returns a result containing the data encoded in the file
     * - the function does not assume the file is messed up. so don't change the file by yourself
     */
    fun parseConfigurationFile(filePath: String): ConfigurationFileParsingResult {

        this.logger.log("Parsing the configuration file with the path $filePath")

        val content = this.fs.readFile(filePath)
        val lines = content.split("\r?\n|\r".toRegex()).toTypedArray().filter { line -> line.trim().isNotEmpty() }

        val numPlayers = lines[0].toInt()

        val tileStackLines = lines.subList(1, lines.size - 15)
        val endStackLines = lines.subList(lines.size - 15, lines.size)

        val tileStackTiles: MutableList<Tile> = mutableListOf()
        val endStackTiles: MutableList<Tile> = mutableListOf()

        for (pattern in tileStackLines) {
            tileStackTiles.add(parsePatternToTile(pattern))
        }

        for (pattern in endStackLines) {
            endStackTiles.add(parsePatternToTile(pattern))
        }

        // the tile stack does not contain any offsprings when it's imported from a file.
        val offsprings = mutableListOf<Tile>()
        // 16 offsprings (2 of each animal type)
        AnimalType.values().forEach { type ->
            offsprings.addAll(List(2) { AnimalTile(AnimalVariant.CHILD, type) })
        }

        tileStackTiles.addAll(offsprings)

        return ConfigurationFileParsingResult(numPlayers, tileStackTiles, endStackTiles)
    }


    /**
     * parses a pattern like 'Kw' to a tile based on the specs
     */
    private fun parsePatternToTile(pattern: String): Tile {

        val vendingStallTypeMap: MutableMap<Char, VendingStallType> = mutableMapOf()
        vendingStallTypeMap['1'] = VendingStallType.V1
        vendingStallTypeMap['2'] = VendingStallType.V2
        vendingStallTypeMap['3'] = VendingStallType.V3
        vendingStallTypeMap['4'] = VendingStallType.V4


        val animalTypeMap: MutableMap<Char, AnimalType> = mutableMapOf()
        animalTypeMap['F'] = AnimalType.FLAMINGO
        animalTypeMap['P'] = AnimalType.PANDA
        animalTypeMap['K'] = AnimalType.CAMEL
        animalTypeMap['S'] = AnimalType.CHIMPANZEE
        animalTypeMap['L'] = AnimalType.LEOPARD
        animalTypeMap['Z'] = AnimalType.ZEBRA
        animalTypeMap['E'] = AnimalType.ELEPHANT
        animalTypeMap['U'] = AnimalType.KANGAROO

        val animalVariantMap: MutableMap<Char, AnimalVariant> = mutableMapOf()
        animalVariantMap['w'] = AnimalVariant.FEMALE
        animalVariantMap['m'] = AnimalVariant.MALE
        animalVariantMap['-'] = AnimalVariant.NEUTRAL


        return if (pattern[0] == 'C') {
            CoinTile()
        } else if (pattern[0] == 'v') {
            VendingStallTile(vendingStallTypeMap[pattern[1]] as VendingStallType)
        } else {
            val animalType = animalTypeMap[pattern[0]] as AnimalType
            val animalVariant = animalVariantMap[pattern[1]] as AnimalVariant
            AnimalTile(animalVariant, animalType)
        }
    }


}


