package view

import tools.aqua.bgw.components.uicomponents.Button
import service.RootService
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * MenuScene that player can the game save and quit
 * it contains four different options.
 * resumeButton is to go back to the current game
 * startANewGameButton is to start a new game
 * saveAndQuitButton is to save and quit the game
 * quitWithoutSavingButton is to quit the game without saving the game
 */
class GameOptionMenu(rootService: RootService): MenuScene(1920, 1080), Refreshable {

    val resumeButton = Button(
        width = 900,
        height = 100,
        posX = 500,
        posY = 250,
        text = "Resume",
        font = Font(size = 60),
        visual = ColorVisual(206, 229, 208)
    )
    val startANewGameButton = Button(
        width = 900,
        height = 100,
        posX = 500,
        posY = 400,
        text = "Start a new game",
        font = Font(size = 60),
        visual = ColorVisual(243, 240, 215)
    )

    val saveAndQuitButton = Button(
        width = 900,
        height = 100,
        posX = 500,
        posY = 550,
        text = "Save and quit",
        font = Font(size = 60,family="Arial"),
        visual = ColorVisual(224, 192, 151)
    )
    val quitWithoutSavingButton = Button(
        width = 900,
        height = 100,
        posX = 500,
        posY = 700,
        text = "Quit without saving",
        font = Font(size = 60),
        visual = ColorVisual(255, 120, 120)
    )

    private val simulationSpeedComboBox = ComboBox<Int>(posX = 100, posY = 100, width = 100, height = 20).apply {
        items = listOf(1, 5, 10, 15, 20, 25)
        selectedItem = 10
        selectedItemProperty.addListener { _, newValue ->
            if (newValue != null){
                rootService.gameService.setSimulationSpeed(newValue)
            }
        }
    }

    init {
        background = ColorVisual(255,192,215)
        addComponents(
            resumeButton,saveAndQuitButton,startANewGameButton,quitWithoutSavingButton, simulationSpeedComboBox
        )
    }
}