package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual

/**
 * MenuScene that gets shown after starting the Game
 *
 * Contains [newGameButton], [loadGameButton] and [highscoreButton]
 */
class StartMenuScene : MenuScene(960, 540){

    private val offsetX = 280
    private val offsetY = 170

    val newGameButton = Button(
        posX = 0 + offsetX, posY = 0 + offsetY, width = 400, height = 50, text = "New Game",
        visual = ColorVisual(206, 229, 208)
    )

    val loadGameButton = Button(
        posX = 0 + offsetX, posY = 75 + offsetY, width = 400, height = 50, text = "load Game",
        visual = ColorVisual(243, 240, 215)
    )

    val highscoreButton = Button(
        posX = 0 + offsetX, posY = 150 + offsetY, width = 400, height = 50, text = "Highscore",
        visual = ColorVisual(255, 120, 120)
    )

    init {
        background = ColorVisual(255,192,215)
        addComponents(newGameButton, loadGameButton, highscoreButton)
    }
}