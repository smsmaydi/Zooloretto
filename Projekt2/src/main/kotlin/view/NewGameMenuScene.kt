package view

import entity.AIDifficulty
import entity.Player
import entity.PlayerAI
import service.GameLoaderService
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

/**
 * MenuScene to start a new Game
 *
 * Contains [playerOrderComboBoxes] to define the playerorder,
 * [nameTextfields] to set playernames,
 * [difficultiesComboBoxes] to set the AI Difficulty,
 * [backButton], [importDrawpileButton] and [startButton]
 */
class NewGameMenuScene (private val rootService: RootService) : MenuScene(960, 540), Refreshable{

    //offsets to position the GUI elements in the scene (distance from upper left corner
    private val offsetX = 210
    private val offsetY = 82

    /** The imported config file of the draw pile (can be selected in the Load Draw Pile Menu Scene
     *
     * Has a setter that automatically updates the view depending on whether this is null or not
     */
    var importedDrawpile: GameLoaderService.ConfigurationFileParsingResult? = null
        set(value) {
            importDrawpileButton.isVisible = value == null
            deselectDrawPileButton.isVisible = value != null

            field = value
        }

    /*
    Size of all GUI elements:
        width = 540
        height = 375
     */

    private val playerOrderComboBoxes = arrayOf(
        ComboBox<Int>(posX = 0 + offsetX, posY = 0   + offsetY, width = 100, height = 20),
        ComboBox<Int>(posX = 0 + offsetX, posY = 70  + offsetY, width = 100, height = 20),
        ComboBox<Int>(posX = 0 + offsetX, posY = 140 + offsetY, width = 100, height = 20),
        ComboBox<Int>(posX = 0 + offsetX, posY = 220 + offsetY, width = 100, height = 20),
        ComboBox<Int>(posX = 0 + offsetX, posY = 280 + offsetY, width = 100, height = 20)
    )

    //used for the swap order logic
    private var lastSelectedPlayerorders = arrayOf(1, 2, 3, 4, 5)

    private val nameTextfields = arrayOf(
        TextField(posX = 120 + offsetX, posY = 0   + offsetY, width = 300, height = 20, prompt = "Player1"),
        TextField(posX = 120 + offsetX, posY = 70  + offsetY, width = 300, height = 20, prompt = "Player2"),
        TextField(posX = 120 + offsetX, posY = 140 + offsetY, width = 300, height = 20, prompt = "Player3"),
        TextField(posX = 120 + offsetX, posY = 210 + offsetY, width = 300, height = 20, prompt = "Player4"),
        TextField(posX = 120 + offsetX, posY = 280 + offsetY, width = 300, height = 20, prompt = "Player5")
    )

    private val difficultiesComboBoxes = arrayOf(
        ComboBox<String>(posX = 440 + offsetX, posY = 0   + offsetY, width = 100, height = 20),
        ComboBox<String>(posX = 440 + offsetX, posY = 70  + offsetY, width = 100, height = 20),
        ComboBox<String>(posX = 440 + offsetX, posY = 140 + offsetY, width = 100, height = 20),
        ComboBox<String>(posX = 440 + offsetX, posY = 210 + offsetY, width = 100, height = 20),
        ComboBox<String>(posX = 440 + offsetX, posY = 280 + offsetY, width = 100, height = 20)
    )

    val backButton = Button(posX = 0 + offsetX, posY = 350 + offsetY, width = 150, height = 25, text = "Back",
        visual = ColorVisual( Color.RED ))

    /**  */
    val importDrawpileButton = Button(posX = 195 + offsetX, posY = 350 + offsetY, width = 150, height = 25,
        text = "Import Drawpile", visual = ColorVisual( Color.GRAY ))

    /** Button to not use imported draw pile for game (only shown when a draw pile has been imported) */
    val deselectDrawPileButton = Button(posX = 195 + offsetX, posY = 350 + offsetY, width = 150, height = 25,
        text = "Reset Drawpile", visual = ColorVisual( Color.RED )).apply {
        isVisible = false
        onMouseClicked = {
            importedDrawpile = null
        }
    }

    private val startButton = Button(posX = 390 + offsetX, posY = 350 + offsetY, width = 150, height = 25,
        text = "Start", visual = ColorVisual( Color.GREEN )).apply {
        onMouseClicked = {


            val curPlayerNumber: Int = if (nameTextfields[2].text.isBlank()) {
                2
            } else if (nameTextfields[3].text.isBlank()) {
                3
            } else if (nameTextfields[4].text.isBlank()) {
                4
            } else {
                5
            }


            val playernames = getPlayernamesInOrder(curPlayerNumber)
            val playerDifficulties = getPlayerDifficultiesInOrder(curPlayerNumber)
            val players = mutableListOf<Player>()
            for (playerIndex in playernames.indices) {
                if (playerDifficulties[playerIndex] == "Player"){
                    players.add(Player.createPlayer(playernames[playerIndex], curPlayerNumber))
                }else if (playerDifficulties[playerIndex] == "AI Easy"){
                    players.add(PlayerAI.createPlayer(playernames[playerIndex], curPlayerNumber, AIDifficulty.EASY))
                }else if(playerDifficulties[playerIndex] == "AI Medium"){
                    players.add(PlayerAI.createPlayer(playernames[playerIndex], curPlayerNumber, AIDifficulty.NORMAL))
                }else if (playerDifficulties[playerIndex] == "AI Hard"){
                    players.add(PlayerAI.createPlayer(playernames[playerIndex], curPlayerNumber, AIDifficulty.HARD))
                }
            }




            // If no draw pile is imported then start normally. Otherwise start with the imported draw pile
            if (importedDrawpile == null) {
                rootService.gameService.startGame(players)
            } else {
                // For some reason we can't just use the variable imported draw pile here, so we use temp instead
                val temp = importedDrawpile
                checkNotNull(temp)
                val configPlayerNum = temp.numPlayers

                if(curPlayerNumber == configPlayerNum) {
                    rootService.gameService.startGameFromConfigurationFile(temp, players)
                } else {
                    // If not enough names were entered then throw an exception
                    throw java.lang.IllegalArgumentException()
                }

            }
        }
    }

    private val aiDificulties = mutableListOf("Player", "AI Easy", "AI Medium", "AI Hard")

    init {
        opacity = 0.5

        difficultiesComboBoxes[0].selectedItemProperty.addListener { _, newValue ->
            println(newValue)
        }

        for (boxIndex in 0 .. 4){
            playerOrderComboBoxes[boxIndex].items = mutableListOf(1, 2, 3, 4, 5)
            playerOrderComboBoxes[boxIndex].selectedItem = boxIndex + 1
            addSwapOrderLogic(boxIndex)
            addComponents(playerOrderComboBoxes[boxIndex])
        }

        for (box in difficultiesComboBoxes){
            box.items = aiDificulties
            box.selectedItem = aiDificulties[0]
            addComponents(box)
        }

        for (field in nameTextfields){
            addDisableStartButtonLogic(field)
            addComponents(field)
        }
        startButton.isDisabled = true
        addComponents(
            backButton, startButton,
            importDrawpileButton, deselectDrawPileButton
        )
    }

    //logic to disable the Startbutton if an invalid playercount is set
    private fun addDisableStartButtonLogic (textField: TextField){
        textField.apply {
            onKeyTyped = {
                startButton.isDisabled = (
                        nameTextfields[0].text.isBlank() || nameTextfields[1].text.isBlank() ||
                                (nameTextfields[3].text.isNotBlank() && nameTextfields[2].text.isBlank()) ||
                                (nameTextfields[4].text.isNotBlank() && (nameTextfields[2].text.isBlank() ||
                                        nameTextfields[3].text.isBlank()))
                        )
            }
        }
    }

    //used in addSwapOrderLogic
    private var valueChanged = false

    //logic to ensure a valid playerorder
    private fun addSwapOrderLogic (indexInArray: Int){
        playerOrderComboBoxes[indexInArray].selectedItemProperty.addListener { _, newValue ->
            run{
                if (!valueChanged) {
                    checkNotNull(newValue)
                    val otherBoxIndex = getPlayerOrderBoxByValue(newValue)
                    valueChanged = true
                    playerOrderComboBoxes[otherBoxIndex].selectedItem = lastSelectedPlayerorders[indexInArray]
                    valueChanged = false
                    lastSelectedPlayerorders[otherBoxIndex] = lastSelectedPlayerorders[indexInArray]
                    lastSelectedPlayerorders[indexInArray] = newValue
                }
            }
        }
    }

    //returns the index of the playerorderBox that had the given value before
    private fun getPlayerOrderBoxByValue(value : Int): Int {
        for (selected in 0 .. 4){
            if (lastSelectedPlayerorders[selected] == value){
                return selected
            }
        }
        return 0
    }

    //returns Playernames in the correct Order in which they take their Turns
    private fun getPlayernamesInOrder(playernumber : Int) : List<String>{
        val playernames = mutableListOf<String>()
        for(player in 1 .. 5){
            for(i in 0 until playernumber){
                if (playerOrderComboBoxes[i].selectedItem == player){
                    playernames.add(nameTextfields[i].text)
                }
            }
        }
        return playernames
    }

    //returns the difficulties of the PlayerAI in the order they take their turns
    //if a player is not AI difficulty is player
    private fun getPlayerDifficultiesInOrder(playernumber: Int) : List<String>{
        val playerDifficulties = mutableListOf<String>()
        for(player in 1 .. 5){
            for(i in 0 until playernumber){
                if (playerOrderComboBoxes[i].selectedItem == player){
                    playerDifficulties.add(difficultiesComboBoxes[i].selectedItem!!)
                }
            }
        }
        return playerDifficulties
    }
}