package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * The EndGameMenuScene
 */
class EndGameMenuScene(val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    private val offsetX = 535
    private val offsetY = 200

    private val titelLabel = Label(
        posX = 760,
        posY = 0,
        height = 120,
        width = 400,
        text = "Scores",
        font = Font(size = 100, color = Color.yellow)
    )

    private val playerNameLabel = arrayOf(
        Label(
            posX = -400 + offsetX,
            posY = 300 + offsetY,
            height = 80,
            width = 400,
            text = "Name",
            font = Font(size = 60),
            visual = ColorVisual(
                50, 222, 138
            )
        ),
        Label(
            posX = -400 + offsetX,
            posY = 400 + offsetY,
            height = 80,
            width = 400,
            text = "Name",
            font = Font(size = 60),
            visual = ColorVisual(
                162, 232, 221
            )
        ),
        Label(
            posX = -400 + offsetX,
            posY = 500 + offsetY,
            height = 80,
            width = 400,
            text = "Name",
            font = Font(size = 60),
            visual = ColorVisual(
                119, 156, 171
            )
        ),
        Label(
            posX = -400 + offsetX,
            posY = 600 + offsetY,
            height = 80,
            width = 400,
            text = "Name",
            font = Font(size = 60),
            visual = ColorVisual(
                98, 124, 133
            )
        ),
        Label(
            posX = -400 + offsetX,
            posY = 700 + offsetY,
            height = 80,
            width = 400,
            text = "Name",
            font = Font(size = 60),
            visual = ColorVisual(
                53, 82, 74
            )
        )
    )

    private val playerPointLabel = arrayOf(
        Label(
            posX = 850 + offsetX,
            posY = 300 + offsetY,
            height = 80,
            width = 400,
            text = "50",
            font = Font(size = 60),
            visual = ColorVisual(
                50, 222, 138
            )
        ),
        Label(
            posX = 850 + offsetX,
            posY = 400 + offsetY,
            height = 80,
            width = 400,
            text = "50",
            font = Font(size = 60),
            visual = ColorVisual(
                162, 232, 221
            )
        ),
        Label(
            posX = 850 + offsetX,
            posY = 500 + offsetY,
            height = 80,
            width = 400,
            text = "50",
            font = Font(size = 60),
            visual = ColorVisual(
                119, 156, 171
            )
        ),
        Label(
            posX = 850 + offsetX,
            posY = 600 + offsetY,
            height = 80,
            width = 400,
            text = "50",
            font = Font(size = 60),
            visual = ColorVisual(
                98, 124, 133
            )
        ),
        Label(
            posX = 850 + offsetX,
            posY = 700 + offsetY,
            height = 80,
            width = 400,
            text = "50",
            font = Font(size = 60),
            visual = ColorVisual(
                53, 82, 74
            )
        )
    )

    private val winnerLabel = Label(
        posX = 0 + offsetX,
        posY = 200 + offsetY,
        height = 80,
        width = 850,
        text = "Winner: Player1",
        alignment = Alignment.CENTER,
        font = Font(size = 60, fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(250, 194, 19)
    )

    val playAgainButton = Button(
        posX = 450 + offsetX, posY = 780 + offsetY, height = 80, width = 400, text = "PLAY AGAIN",
        font = Font(size = 50, color = Color.white), visual = ColorVisual(83, 191, 157)
    )
    val quitButton = Button(
        posX = 0 + offsetX, posY = 780 + offsetY, height = 80, width = 400, text = "QUIT",
        font = Font(size = 50, color = Color.white), visual = ColorVisual(249, 76, 102)
    )

    init {
        background = ImageVisual("tts/zooloretto.jpeg")
        for (label in playerNameLabel) {
            this.addComponents(label)
        }
        for (label in playerPointLabel) {
            this.addComponents(label)
        }
        this.addComponents(
            titelLabel, winnerLabel, quitButton, playAgainButton
        )
    }

    override fun refreshAfterGameEnd() {
        val game = rootService.game
        checkNotNull(game)

        val results = rootService.gameService.calculateEndGame()
        var indexWithHighestResult = 0
        println(results.size)
        for (playerIndex in results.indices) {
            playerNameLabel[playerIndex].isVisible = true
            playerPointLabel[playerIndex].isVisible = true
            playerNameLabel[playerIndex].text = game.players[playerIndex].name
            playerPointLabel[playerIndex].text = "${results[playerIndex]}"
            if (results[playerIndex] > results[indexWithHighestResult]) {
                indexWithHighestResult = playerIndex
            }
        }
        for (playerIndex in results.size..4) {
            playerNameLabel[playerIndex].isVisible = false
            playerPointLabel[playerIndex].isVisible = false
        }

        winnerLabel.text = "Winner: ${game.players[indexWithHighestResult].name}"
    }
}