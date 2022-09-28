package  service


import entity.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * - Test cases for the game service
 */
class GameServiceTest {

    /**
     * - should throw when given a wrong number of players
     * */
    @Test
    fun shouldThrowErrorWhenGivenWrongNumberOfPlayers() {
        val root = RootService()
        root.game = Game("someName")
        val service = GameService(root, GameComponentsService())


        assertFailsWith<Exception>("a game can contain 2,3,4 or 5 players. players count: 0") {
            service.startGame(listOf())
        }

    }

    /**
     * - should return false if the last pile has not been modified
     * */
    @Test
    fun shouldReturnThatLastPileHasNotStartedIfThatIsTheCase() {
        val root = RootService()
        root.game = Game("someName")
        val service = GameService(root, GameComponentsService())
        service.startGame(
            listOf(
                Player.createPlayer("p1", 3),
                Player.createPlayer("p2", 3),
                Player.createPlayer("p3", 3)
            )
        )
        assertEquals(service.hasLastPileStarted(), false)
    }

    /**
     * - should return true if the last pile has been modified
     * */
    @Test
    fun shouldReturnThatLastPileHasStartedIfThatIsTheCase() {
        val root = RootService()
        val service = GameService(root, GameComponentsService())
        service.startGame(
            listOf(
                Player.createPlayer("p1", 3),
                Player.createPlayer("p2", 3),
                Player.createPlayer("p3", 3)
            )
        )
        root.game!!.endStack.tiles.removeAt(4)
        assertEquals(service.hasLastPileStarted(), true)
    }

    /**
     * - should set the rootService.game attribute to the prev node of the linked list of game states
     * */
    @Test
    fun shouldUndo() {
        val root = RootService()

        val service = GameService(root, GameComponentsService())
        service.startGame(
            listOf(
                Player.createPlayer("p1", 3),
                Player.createPlayer("p2", 3),
                Player.createPlayer("p3", 3)
            )
        )

        // build a linked list
        val rootGame = root.game!!
        val nextGame = rootGame.clone()
        rootGame.connect(nextGame)


        root.game = nextGame // the root service points to nextGame

        assertEquals(root.game, nextGame)

        service.undo() // the rootService.game should now point to rootGame

        assertEquals(root.game!!, rootGame)


    }

    /**
     * - should redo
     * */
    @Test
    fun shouldRedo() {
        val root = RootService()

        val service = GameService(root, GameComponentsService())
        service.startGame(
            listOf(
                Player.createPlayer("p1", 3),
                Player.createPlayer("p2", 3),
                Player.createPlayer("p3", 3)
            )
        )

        // build a linked list
        val rootGame = root.game!!
        val nextGame = rootGame.clone()
        rootGame.connect(nextGame)


        assertEquals(root.game, rootGame)

        service.redo()

        assertEquals(root.game!!, nextGame)


    }

    /**
     * - should return the points for a player
     * */
    @Test
    fun shouldCalculatePointsForAPlayer() {
        val root = RootService()
        val service = GameService(root, GameComponentsService())
        service.startGame(
            listOf(
                Player.createPlayer("p1", 3),
                Player.createPlayer("p2", 3),
                Player.createPlayer("p3", 3)
            )
        )
        val game = root.game!!
        game.players[0].barn.animals.add(AnimalTile(AnimalVariant.MALE, AnimalType.KANGAROO))
        assertEquals(service.calculatePlayerPoints(game.players[0] as Player), -2)
    }


    /**
     * - should return the points for all players on game end
     * */
    @Test
    fun shouldCalculatePointsForAllPlayersOnGameEnd() {
        val root = RootService()
        val service = GameService(root, GameComponentsService())
        service.startGame(
            listOf(
                Player.createPlayer("p1", 3),
                Player.createPlayer("p2", 3),
                Player.createPlayer("p3", 3)
            )
        )
        val game = root.game!!

        game.players[0].barn.animals.add(AnimalTile(AnimalVariant.MALE, AnimalType.KANGAROO)) // gets -2 poins

        game.players[1].barn.vendingStalls.add(VendingStallTile(VendingStallType.V1)) // gets -2 points
        game.players[1].barn.vendingStalls.add(VendingStallTile(VendingStallType.V2)) // gets -2 points

        assertEquals(service.calculateEndGame(), listOf(-2, -4, 0))
    }

}
