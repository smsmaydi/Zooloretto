package view

import entity.HighScoreRecord
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.SelectionMode
import tools.aqua.bgw.components.uicomponents.TableColumn
import tools.aqua.bgw.components.uicomponents.TableView
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font

/** MenuScene that shows th all time Highscores
 *
 * Contains a Table showing Rank Name Points and Date of each Player in the list
 * and the [backButton]
 */
class HighscoreMenuScene(val rs: RootService) : MenuScene(960, 540), Refreshable {

    private val offsetX = 180
    private val offsetY = 80

    val backButton = Button(posX = 0 + offsetX, posY = 0 + offsetY, width = 200, height = 25, text = "Back to menu")

    private val highscoreTable = TableView<Pair<Int, HighScoreRecord>>(
        posX = 0 + offsetX, posY = 50 + offsetY, width = 600, height = 280,
        selectionMode = SelectionMode.NONE
    )

    init {

        highscoreTable.columns.add(TableColumn(title = "Rank", width = 70, font = Font(size = 15)) { "${it.first}" })
        highscoreTable.columns.add(
            TableColumn(
                title = "Name",
                width = 298,
                font = Font(size = 15)
            ) { it.second.playerName })
        highscoreTable.columns.add(
            TableColumn(
                title = "Points",
                width = 80,
                font = Font(size = 15)
            ) { "${it.second.playerPoints}" })
        highscoreTable.columns.add(
            TableColumn(
                title = "Date",
                width = 150,
                font = Font(size = 15)
            ) { it.second.getDateAsString() })


        addComponents(highscoreTable, backButton)

        updateHighScores()

    }

    /**
     * The updateHighScores fun
     */
    fun updateHighScores() {
        checkNotNull(rs.highScore) { "highscore is null" }
        val scores = rs.highScore!!.scores

        /** Example Highscores for testing
        val scores = mutableListOf<HighScoreRecord>(
        HighScoreRecord("Tom", 3, Calendar.getInstance().timeInMillis),
        HighScoreRecord("Tim", 5, Calendar.getInstance().timeInMillis),
        HighScoreRecord("Lukas", 1, Calendar.getInstance().timeInMillis),
        HighScoreRecord("Lisa", 2, Calendar.getInstance().timeInMillis),
        )
         */

        scores.sortByDescending { it.playerPoints }

        var i = 0
        val scoresWithRank = scores.map {
            i += 1
            Pair(i, it)
        }


        highscoreTable.items.addAll(scoresWithRank)
    }

    override fun refreshAfterGameEnd() {
        updateHighScores()
    }
}