package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

/**
 * Scene that gets shown when the User selects Load game. There are a maximum of 6 Game slots. Some (or all) of which
 * may be empty. When the user presses on a specific game state (That has saved data) they can then press the "Load"
 * button to load the specific game state and get into the loaded game.
 * Alternatively the user can press the "Back" button to get back into the menu
 */
class LoadGameMenuScene(val rootService: RootService) : MenuScene(1920, 1080), Refreshable {


    /** Button to refresh the list of saved games */
    private val refreshButton = Button(
        posX = 1000, posY = 200,
        width = 200, height = 70,
        visual = ColorVisual(Color.gray),
        text = "Refresh list"
    ).apply {
        onMouseClicked = {
            updateGameSaves()
        }
    }

    /** Label */
    private val gameSavesLabel = Label(
        width = 469, height = 97, posX = 744, posY = 75,
        text = "Games",
        font = Font(size = 80, color = Color.WHITE)
    )


    /** Button to go back to the start menu */
    val backButton = Button(
        width = 317, height = 102, posX = 618, posY = 926,
        text = "Back",
        font = Font(size = 60)
    )

    /** Button to load the selected save and start the game
     *
     * Logic for this Button implemented in SoPra Application
     */
    val loadButton = Button(
        width = 317, height = 102, posX = 997, posY = 926,
        text = "Load",
        font = Font(size = 60)
    )

    /** Returns the selected save from the table if a save was selected otherwise returns null */
    fun startGame() {
        if (tableView.selectedIndices.size > 0) {
            val gameName = tableView.items[tableView.selectedIndices[0]]
            return rootService.gameService.startSavedGame(gameName)
        }
    }

    /** Table showing a list of all the files in the resources/game_saves folder */
    private val tableView = TableView<String>(
        posX = 630, posY = 229,
        width = 700, height = 550,
        selectionMode = SelectionMode.SINGLE
    ).apply {
        this.columns.add(
            TableColumn(
                title = "Games", width = 698,
                font = Font(size = 24)
            )
            { it }
        )
    }

    /** Updates the shown list of game saves by going through the folder resources/game_saves */
    private fun updateGameSaves() {
        val gameLoader = rootService.gameLoaderService
        checkNotNull(gameLoader) { "game loader is null" }
        tableView.items.clear()
        val savedGames = gameLoader.getSavedGames()
        for (game in savedGames) {
            tableView.items.add(game)
        }
    }

    init {
        this.background = ColorVisual(Color.BLUE)
        addComponents(
            gameSavesLabel,
            backButton, loadButton, refreshButton,
            tableView
        )
        updateGameSaves()
    }

}
