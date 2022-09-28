package  service


import FileSystem
import entity.Game
import entity.Player
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Filesystem Test class
 */
class FileSystemMockImpl : FileSystem {

    // these attributes are used to record the calls of the methods in the class
    public var mkdirArgs: MutableList<String> = mutableListOf()
    public var listDirArgs: MutableList<String> = mutableListOf()
    public var readFileArgs: MutableList<String> = mutableListOf()
    public var writeFileArgs: MutableList<List<String>> = mutableListOf()

    // these attributes are used to control the return values of the methods
    public var listDirReturn: List<String> = listOf()
    public var readFileReturn: String = ""


    override fun mkdir(path: String) {
        this.mkdirArgs.add(path)
    }

    override fun listDir(path: String): List<String> {
        this.listDirArgs.add(path)
        return this.listDirReturn
    }

    override fun readFile(path: String): String {
        this.readFileArgs.add(path)
        return this.readFileReturn
    }

    override fun writeFile(path: String, content: String) {
        this.writeFileArgs.add(listOf(path, content))
    }

    override fun joinPath(vararg paths: String): String {
        return paths.joinToString(File.separator)
    }

}


/**
 * - Test cases for the game loader service
 */
class GameLoaderServiceTest {

    /**
     * - should read the game state directory to return a list of saved games
     * */
    @Test
    fun shouldReturnAListOfSavedGames() {
        val fs = FileSystemMockImpl()
        val loader = GameLoaderService(fs, RootService())
        val games = listOf("game1", "game2", "game3")
        fs.listDirReturn = games
        assertEquals(loader.getSavedGames(), fs.listDirReturn)
    }

    /**
     * - should read the saved state from the gameState directory
     * */
    @Test
    fun shouldReadStateFromTheGameStateDirectory() {
        val fs = FileSystemMockImpl()
        val loader = GameLoaderService(fs, RootService())
        loader.getSavedGames()
        assertEquals(fs.listDirArgs[0], "gameState")
    }

    /**
     * - should save game state as json files to the file system
     * */
    @Test
    fun shouldSaveGameStateAsJsonFilesToTheFs() {
        val fs = FileSystemMockImpl()
        val loader = GameLoaderService(fs, RootService())

        // build a linked list of game states
        val rootGame = Game("coolGame")
        rootGame.coinsInBank = 30
        rootGame.players.add(Player.createPlayer("p1", 1))
        val childGame1 = rootGame.clone()
        rootGame.connect(childGame1)
        val childGame2 = childGame1.clone()
        childGame1.connect(childGame2)


        loader.save(childGame1)

        // we should save the active game name to a file activeGame.txt
        assertEquals(fs.writeFileArgs[0][0], fs.joinPath("gameState", "coolGame", "activeGame.txt"))
        assertEquals(fs.writeFileArgs[0][1], childGame1.fileName)

        // we should save the game states as json files. (here 3 json files)

        assertEquals(fs.writeFileArgs[1][0], fs.joinPath("gameState", "coolGame", "0.json"))
        assertEquals(fs.writeFileArgs[2][0], fs.joinPath("gameState", "coolGame", "1.json"))
        assertEquals(fs.writeFileArgs[3][0], fs.joinPath("gameState", "coolGame", "2.json"))

        assertEquals(
            fs.writeFileArgs[1][1],
            "{\n" +
                    "    \"name\": \"coolGame\",\n" +
                    "    \"coinsInBank\": 30,\n" +
                    "    \"players\": [\n" +
                    "        {\n" +
                    "            \"type_\": \"entity.Player\",\n" +
                    "            \"name\": \"p1\",\n" +
                    "            \"enclosures\": [\n" +
                    "                {\n" +
                    "                    \"biggerPoints\": 5,\n" +
                    "                    \"smallerPoints\": 4,\n" +
                    "                    \"coinsToGetWhenFull\": 1,\n" +
                    "                    \"vendingStallsMax\": 2,\n" +
                    "                    \"animalsMax\": 4\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"biggerPoints\": 8,\n" +
                    "                    \"smallerPoints\": 5,\n" +
                    "                    \"coinsToGetWhenFull\": 2,\n" +
                    "                    \"vendingStallsMax\": 1,\n" +
                    "                    \"animalsMax\": 5\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"biggerPoints\": 10,\n" +
                    "                    \"smallerPoints\": 6,\n" +
                    "                    \"vendingStallsMax\": 1,\n" +
                    "                    \"animalsMax\": 6\n" +
                    "                }\n" +
                    "            ],\n" +
                    "            \"expansionBoards\": [\n" +
                    "                {\n" +
                    "                    \"biggerPoints\": 9,\n" +
                    "                    \"smallerPoints\": 5,\n" +
                    "                    \"coinsToGetWhenFull\": 1,\n" +
                    "                    \"vendingStallsMax\": 1,\n" +
                    "                    \"animalsMax\": 5\n" +
                    "                }\n" +
                    "            ],\n" +
                    "            \"barn\": {\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}"
        )

    }

}
