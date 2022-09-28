package view

import service.GameLoaderService
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.SelectionMode
import tools.aqua.bgw.components.uicomponents.TableColumn
import tools.aqua.bgw.components.uicomponents.TableView
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color
import java.io.File

/**
 * drawPilePath
 */
const val DRAW_PILE_FOLDER_PATH = "src/main/resources/draw_piles"

/**
 * Class used for importing a draw pile into a game
 *
 * Shows a list of draw pile files in the folder resources/draw_piles. When one of them is selected
 * and you click load draw pile, the game will load that draw pile, and you will go back to the new
 * game menu scene.
 */
class LoadDrawPileMenuScene(
    /** RootService */
    val rs: RootService
) : MenuScene(960, 540), Refreshable {

    private val offsetX = 180
    private val offsetY = 80

    /** Button to go back to the new game menu scene */
    val backButton = Button(posX = 0 + offsetX, posY = 0 + offsetY, width = 200, height = 70, text = "Back to menu")

    /** Button to load the currently selected draw pile */
    val importDrawPileButton =
        Button(posX = 200 + offsetX, posY = 0 + offsetY, width = 200, height = 70, text = "Import Draw Pile")


    /** Refresh button. Refreshes the draw pile file list */
    val refreshButton = Button(
        posX = 900 + offsetX, posY = 0 + offsetY,
        width = 200, height = 70,
        text = "Refresh", visual = ColorVisual(Color.GREEN),
    ).apply {
        onMouseClicked = {
            updateDrawPileTable()
        }
    }


    /** Table showing the list of draw piles in the folder resources/draw_piles
     * Rows consist of a Pair of a String of the file path along with a parsed config file
     */
    val drawPileTable = TableView<TableData>(
        posX = 0 + offsetX, posY = 50 + offsetY,
        width = 600, height = 280,
        selectionMode = SelectionMode.SINGLE
    )


    /** Initialize the draw pile list */
    init {

        // Adding Columns to the draw pile table
        val drawPileColumns: List<TableColumn<TableData>> = listOf(
            TableColumn(
                title = "Name", width = 200, font = Font(size = 15),
                formatFunction = {
                    "${it.fileName}"
                }
            ),
            TableColumn(
                title = "Num Players", width = 120, font = Font(size = 15),
                formatFunction = {
                    "${it.configParseResult.numPlayers}"
                }
            )
        )
        drawPileTable.columns.addAll(drawPileColumns)

        addComponents(
            backButton, importDrawPileButton, drawPileTable, refreshButton
        )

        updateDrawPileTable()

    }


    /** Loads all the draw piles from the specified folder and updates the draw pile table */
    private fun updateDrawPileTable() {

        val gameLoader = rs.gameLoaderService
        checkNotNull(gameLoader) { "game loader is null" }
        // Clear all items in the table
        drawPileTable.items.clear()

        // create the resource path if it does not exist
        gameLoader.fs.mkdir(DRAW_PILE_FOLDER_PATH)

        // Try to parse each file in the resources/draw_piles directory and add it to the table
        gameLoader.fs.listDir(DRAW_PILE_FOLDER_PATH).forEach { fileName ->
            val relativeFilePath = DRAW_PILE_FOLDER_PATH + File.separator + fileName
            val loadedConfigFile = gameLoader.parseConfigurationFile(relativeFilePath)
            drawPileTable.items.add(TableData(fileName, loadedConfigFile))
        }
    }
}


/** Simple class to store name of a draw pile file and the parsing result */
class TableData(val fileName: String, val configParseResult: GameLoaderService.ConfigurationFileParsingResult)