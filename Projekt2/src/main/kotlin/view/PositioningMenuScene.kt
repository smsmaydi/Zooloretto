package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.event.KeyCode
import tools.aqua.bgw.event.KeyEvent
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

/** Helper Scene for positioning objects
 *
 * How this class works: You have a menu scene with a single blue button on it.
 * Once you click on the button you can start to reposition it by using WASD on the keyboard
 *
 * You can also resize the button by holding shift while you press WASD
 *
 * If you hold shift while moving or resizing the button, all the actions happen faster
 *
 * Once you are happy with the position
 */
class PositioningMenuScene: MenuScene(960, 540), Refreshable {

    private val startingPosX = 300
    private val startingPosY = 300

    private val startingWidth = 50
    private val startingHeight = 50

    private var tutorialProgress = 0
        set(value) {
            clickHereLabel.text = labelTexts[value]
            field = value
        }




    /** Manually created buttons with space */
    private val addedButtons = mutableListOf<Button>()

    private val labelTexts = listOf(
        "Click on the button then move with WASD",
        "Hold down shift and press WASD to resize the button",
        "Hold down Ctrl to do the actions faster",
        "Press Enter to print out the current position of the button",
        "Press Space to create a permanent button at the current position",
        "Press F5 to print out code to create the buttons on the field",
        "Press Backspace or click on one of the buttons to delete it",
        "Press Esc to disable this label"
    )

    private val clickHereLabel = Label(
        text = labelTexts[0],
        posX = -80, posY = 0,
        height = 20, width = 600
    )

    init {
        addComponents(clickHereLabel)
    }


    private var positioningToken = Button(
        posX = startingPosX, posY = startingPosY,
        width = startingWidth, height = startingHeight,
        visual = ColorVisual(Color.blue)
    ).apply {
        onKeyPressed = { keyEvent ->
            buttonKeyLogic(this, keyEvent)
            updateTutorialLabel(keyEvent)
        }
    }

    init {
        addComponents(positioningToken)
    }


    private fun buttonKeyLogic(button: Button, keyEvent: KeyEvent) {
        val baseMovementPixels = 5
        val baseSizeModifierPixels = 2

        var multiplier = 1
        if(keyEvent.controlDown) {
            multiplier = 4
        }

        if(keyEvent.keyCode == KeyCode.SPACE) {
            val tempButton = Button(
                posX = button.posX, posY = button.posY,
                width = button.width, height = button.height,
                visual = ColorVisual(Color.gray)
            ).apply {
                onMouseClicked = {
                    addedButtons.remove(this)
                    removeComponents(this)
                }
            }

            addComponents(tempButton)
            addedButtons.add(tempButton)
            println("Adding new Button at")
            println(getBtnPosAsString(button))
        } else if(keyEvent.keyCode == KeyCode.ENTER) {
            println("Printing Position")
            println(getBtnPosAsString(button))
        } else if(keyEvent.keyCode == KeyCode.F5) {
            printAllComponents()
        } else if(keyEvent.keyCode == KeyCode.BACK_SPACE) {
            // Remove latest added component
            if(addedButtons.size > 0) {
                val removedButton = addedButtons.removeLast()
                removeComponents(removedButton)
            }
        } else if(!keyEvent.shiftDown) {
            if (keyEvent.keyCode == KeyCode.D) {
                button.posX += baseMovementPixels * multiplier
            }
            if (keyEvent.keyCode == KeyCode.A) {
                button.posX -= baseMovementPixels * multiplier
            }
            if (keyEvent.keyCode == KeyCode.S) {
                button.posY += baseMovementPixels * multiplier
            }
            if (keyEvent.keyCode == KeyCode.W) {
                button.posY -= baseMovementPixels * multiplier
            }
        } else {
            if(keyEvent.keyCode == KeyCode.D) {
                button.width += baseSizeModifierPixels * multiplier
            }
            if(keyEvent.keyCode == KeyCode.A) {
                button.width -= baseSizeModifierPixels * multiplier
            }
            if(keyEvent.keyCode == KeyCode.S) {
                button.height += baseSizeModifierPixels * multiplier
            }
            if(keyEvent.keyCode == KeyCode.W) {
                button.height -= baseSizeModifierPixels * multiplier
            }
        }
    }

    /** returns the position as well as the width and height of the button as a string */
    private fun getBtnPosAsString(button: UIComponent): String {
        return "posX = ${button.posX}, posY = ${button.posY}, width = ${button.width}, height = ${button.height}"
    }

    /** print out code needed to create all the buttons in the scene (except the blue one) */
    private fun printAllComponents() {
        println()
        println()
        println("##### Code for creating these components #####")
        println()
        println()
        println("private val createdComponents = listOf(")
        addedButtons.forEach {
            println(
                "\tButton(" + getBtnPosAsString(it) + ", visual = ColorVisual(Color.darkGray)),"
            )
        }
        println(")")
        println("")
        println("init {\n" +
                "\tcreatedComponents.forEach { addComponents(it) }\n" +
                "}")
    }

    private fun updateTutorialLabel(keyEvent: KeyEvent) {
        val movementKeys = listOf(KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D)

        if(keyEvent.keyCode == KeyCode.ESCAPE) clickHereLabel.isVisible = false

        when(tutorialProgress) {
            0 -> {
                if(keyEvent.keyCode in movementKeys) tutorialProgress = 1
            }
            1 -> {
                if(keyEvent.shiftDown && keyEvent.keyCode in movementKeys) tutorialProgress = 2
            }
            2 -> {
                if(keyEvent.controlDown && keyEvent.keyCode in movementKeys) tutorialProgress = 3
            }
            3 -> {
                if(keyEvent.keyCode == KeyCode.ENTER) tutorialProgress = 4
            }
            4 -> {
                if(keyEvent.keyCode == KeyCode.SPACE) tutorialProgress = 5
            }
            5 -> {
                if(keyEvent.keyCode == KeyCode.F5) tutorialProgress = 6
            }
            6 -> {
                if(keyEvent.keyCode == KeyCode.BACK_SPACE) tutorialProgress = 7
            }
        }
    }

}