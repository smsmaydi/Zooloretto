package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color


/**
 * The ZoolorettoGameScene
 */
class ZoolorettoGameScene(private val rs: RootService, private val barnScene: BarnMenuScene) :
    BoardGameScene(1920, 1080), Refreshable {

    private val imageLoader = PileImageLoader()

    private var selectedTruck = -1
    private var truckSelected = false
    private var selectedVendingStallHole = -1
    private var vendingStallHoleSelected = false

    private var lastDrawnTile: Tile = AnimalTile(AnimalVariant.NEUTRAL, AnimalType.CAMEL, false)

    private var addingTilesToZoo = false
    private var moving = false

    private var truckTaken = false

    private var selectedEnclosure1 = -1
    private var selectedEnclosure2 = -1

    /** variable to represent whether move has been clicked and the player now has to select which enclosures to swap */
    private var currentlySelectingEnclosures = false

    /** Label that shows what the AI is doing */
    private val aiLabel = Label(posX = 460, posY = 222, width = 700, height = 200, text = "", font = Font(size = 24))


    /** Buttons to view other players boards */
    private val playerButtons = listOf<Button>(
        Button(posX = 50, posY = 300, height = 100, width = 200, text = "Player1", visual = ColorVisual(118, 186, 153)),
        Button(posX = 50, posY = 450, height = 100, width = 200, text = "Player2", visual = ColorVisual(173, 207, 159)),
        Button(posX = 50, posY = 600, height = 100, width = 200, text = "Player3", visual = ColorVisual(206, 216, 158)),
        Button(posX = 50, posY = 750, height = 100, width = 200, text = "Player4", visual = ColorVisual(255, 220, 174)),
        Button(posX = 50, posY = 900, height = 100, width = 200, text = "Player5", visual = ColorVisual(118, 186, 160))
    )

    /** Further initialize other player buttons */
    init {
        playerButtons.forEachIndexed { index, button ->
            button.apply {
                isVisible = false
                onMouseClicked = {
                    val game = rs.game
                    checkNotNull(game)

                    showOtherPlayersBoard(index)
                }
            }
        }
    }

    private val backToActivePlayerButton = Button(
        posX = 1600, posY = 300, width = 200, height = 700,
        text = "back to active Player", visual = ColorVisual(118, 186, 153)
    ).apply {
        isVisible = false
        onMouseClicked = {
            val game = rs.game
            checkNotNull(game)

            showOtherPlayersBoard(game.activePlayerIndex)
        }
    }

    //Action Buttons
    private val drawATileButton = Button(
        posX = 1600,
        posY = 300,
        height = 100,
        width = 200,
        text = "Draw Tile",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {

            lastDrawnTile = rs.playerActionService.draw()
            showDrawnTile()
            setIsDisabledForAllAktionButtons(true)

            /*
            if (lastDrawnTile is AnimalTile) {
                println("Animal Drawn ${(lastDrawnTile as AnimalTile).type}")
                println("${(lastDrawnTile as AnimalTile).variant}")
                println("Mating state: ${(lastDrawnTile as AnimalTile).hasMated}")
            } else if (lastDrawnTile is VendingStallTile) {
                println("Vendingstall drawn ${(lastDrawnTile as VendingStallTile).type}")
            } else if (lastDrawnTile is CoinTile) {
                println("Coin drawn")
            }
             */

            updateTileCount()
        }
    }

    private val takeADeliveryButton = Button(
        posX = 1600,
        posY = 420,
        height = 100,
        width = 200,
        text = "Take Truck",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {
            if (truckSelected && !addingTilesToZoo) {
                val game = rs.game
                checkNotNull(game)

                addingTilesToZoo = true
                truckTaken = true
                selectTruck(selectedTruck, false)
                //println("truckSelected: $truckSelected")
                setIsDisabledForAllAktionButtons(true)
                for (truck in truckButtons) {
                    truck.isDisabled = true
                }
                takeTruck()
            }
        }
    }

    private val moveButton = Button(
        posX = 1600,
        posY = 540,
        height = 100,
        width = 200,
        text = "Move",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {

            if (vendingStallHoleSelected || barnScene.tileSelected) {
                val game = rs.game
                checkNotNull(game)

                moving = true
                setIsDisabledForAllAktionButtons(true)
                if (vendingStallHoleSelected) {
                    vendingStallHoles[selectedVendingStallHole].components[0].isDraggable = true
                } else {
                    val newTile = GuiTile(
                        posX = 1200,
                        posY = 100,
                        barnScene.getSelectedTile(),
                        visual = ImageVisual(imageLoader.frontImageFor(barnScene.getSelectedTile()))
                    )
                    newTile.isDraggable = true
                    addComponents(newTile)
                    barnScene.unselectTile()
                }
            }

        }
    }
    private val exchangeButton = Button(
        posX = 1600,
        posY = 660,
        height = 100,
        width = 200,
        text = "Exchange",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {

            if (!currentlySelectingEnclosures) {
                currentlySelectingEnclosures = true
            }

            val game = rs.game
            checkNotNull(game) { "Error: Game null" }

            showExchangeButtons()
            setIsDisabledForAllAktionButtons(true)
            setIsDisabledForApplyExchangeButton(true)

            // Show only the selection for the enclosures that are not empty and that the player has access to if they
            // are expansions
            enclosureSelectionButtons.forEachIndexed { index, button ->
                if (!currentlySelectingEnclosures) {
                    // If currently not selecting enclosures then hide the selection buttons
                    button.isVisible = false
                } else {
                    // Otherwise show the available
                    var canSeeButton = true
                    val activePlayer = game.getActivePlayer()
                    val enclosure = if (index < 3) {
                        activePlayer.enclosures[index]
                    } else if (index < 4 || game.players.size == 2) {
                        activePlayer.expansionBoards[index - 3]
                    } else {
                        null
                    }

                    // Can't swap expansion without having expanded first
                    if (enclosure is ExpansionBoard) {
                        canSeeButton = enclosure.isUsed
                    }

                    // Can't swap empty enclosure
                    if (enclosure == null || enclosure.animals.size == 0) {
                        canSeeButton = false
                    }

                    button.isVisible = canSeeButton
                }
            }
        }
    }

    private val applyExchangeButton = Button(
        posX = 1705,
        posY = 660,
        height = 100,
        width = 95,
        text = "apply",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        isVisible = false

        onMouseClicked = {
            if (currentlySelectingEnclosures) {

                // Apply the exchanges (as long as 2 enclosures or 1 enclosure and a barn animal are selected)
                if (selectedEnclosure1 == -1 && selectedEnclosure2 == -1) {
                    // Do not apply the exchange
                } else {
                    // Apply the exchange in the service layer

                    val game = rs.game
                    checkNotNull(game) { "Error: Game not found!" }

                    val selectedEnclosures = mutableListOf<Enclosure>()
                    if (selectedEnclosure1 != -1) {
                        if (selectedEnclosure1 < 3) {
                            selectedEnclosures.add(game.getActivePlayer().enclosures[selectedEnclosure1])
                        } else {
                            selectedEnclosures.add(game.getActivePlayer().expansionBoards[selectedEnclosure1 - 3])
                        }
                    }
                    if (selectedEnclosure2 != -1) {
                        if (selectedEnclosure2 < 3) {
                            selectedEnclosures.add(game.getActivePlayer().enclosures[selectedEnclosure2])
                        } else {
                            selectedEnclosures.add(game.getActivePlayer().expansionBoards[selectedEnclosure2 - 3])
                        }
                    }

                    if (selectedEnclosures.size == 1) {
                        // Only one enclosure selected. Exchange animals from this enclosure with the selected animal
                        // type from the barn
                        val selectedBarnTile = barnScene.getSelectedTile() as AnimalTile
                        rs.playerActionService.exchangeTiles(
                            selectedEnclosures[0],
                            game.getActivePlayer().barn,
                            selectedBarnTile.type
                        )
                    } else {
                        // Two enclosures selected. Swap animals from these two enclosures
                        rs.playerActionService.exchangeTiles(
                            selectedEnclosures[0],
                            selectedEnclosures[1]
                        )
                    }
                }

                resetExchange()
                showExchangeButtons()
            }
        }
    }

    private val cancelExchangeButton = Button(
        posX = 1600,
        posY = 660,
        height = 100,
        width = 95,
        text = "cancel",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        isVisible = false
        onMouseClicked = {
            if (currentlySelectingEnclosures) {
                resetExchange()
                this.isVisible = false
                applyExchangeButton.isVisible = false
                showExchangeButtons()
                setIsDisabledForAllAktionButtons(false)
            }
        }
    }

    private val purchaseButton = Button(
        posX = 1600,
        posY = 780,
        height = 100,
        width = 95,
        text = "Purchase",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {
            if (barnScene.otherPlayersTileSelected) {
                val game = rs.game
                checkNotNull(game)

                rs.playerActionService.purchaseTile(
                    game.players[barnScene.playerOfSelectedTileIndex], barnScene.getSelectedTile()
                )

                barnScene.updateValues()
                barnScene.unselectTile()
            }
        }
    }

    private val discardButton = Button(
        posX = 1705,
        posY = 780,
        height = 100,
        width = 95,
        text = "Discard",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {
            if (barnScene.tileSelected) {
                //println("x: ${barnScene.selectedTileX} y: ${barnScene.selectedTileY}")
                rs.playerActionService.discardTile(barnScene.getSelectedTile())
                barnScene.animalCountLabel[barnScene.selectedTileX][barnScene.selectedTileY].visual =
                    ColorVisual(0, 0, 0, 0)
                barnScene.unselectTile()
            }
        }
    }

    private val expandZooButton = Button(
        posX = 1600,
        posY = 900,
        height = 100,
        width = 200,
        text = "Expand",
        visual = ColorVisual(118, 186, 153)
    ).apply {
        onMouseClicked = {
            val game = rs.game
            checkNotNull(game)

            var canExpand = false
            for (expansionBoard in game.getActivePlayer().expansionBoards) {
                if (!expansionBoard.isUsed) {
                    canExpand = true
                }
            }
            if (canExpand) {
                rs.playerActionService.expandZoo()
                //setIsDisabledForAllAktionButtons(true)
            }
        }
    }


    //private val truckXPositions = arrayOf(600.0, 720.0, 840.0, 960.0, 180.0)

    private val takenTruckVisual =
        TokenView(posX = 1200, posY = 600, height = 300, width = 100, visual = ImageVisual(imageLoader.truckImage))

    //Delivery Truck button
    private val truckButtons = arrayOf(
        Button(posX = 600, posY = 20, height = 300, width = 100, visual = ImageVisual(imageLoader.truckImage)),
        Button(posX = 720, posY = 20, height = 300, width = 100, visual = ImageVisual(imageLoader.truckImage)),
        Button(posX = 840, posY = 20, height = 300, width = 100, visual = ImageVisual(imageLoader.truckImage)),
        Button(posX = 960, posY = 20, height = 300, width = 100, visual = ImageVisual(imageLoader.truckImage)),
        Button(posX = 1080, posY = 20, height = 300, width = 100, visual = ImageVisual(imageLoader.truckImage))
    )

    private val takenTruckHoles = arrayOf(
        Area<TokenView>(posX = 1218, posY = 671, height = 66, width = 66),
        Area<TokenView>(posX = 1218, posY = 743, height = 66, width = 66),
        Area<TokenView>(posX = 1218, posY = 814, height = 66, width = 66)
    )

    //private val truckHoleXPositions = arrayOf(618.0, 738.0, 858.0, 978.0, 1098.0)
    // Dragable position for the trucks
    private val truckHoles = arrayOf<Area<TokenView>>(
        //Truck 1a
        Area<TokenView>(posX = 618, posY = 91, height = 66, width = 66, visual = ColorVisual(0, 255, 0, 0)),
        Area<TokenView>(posX = 618, posY = 163, height = 66, width = 66, visual = ColorVisual(0, 255, 0, 0)),
        Area<TokenView>(posX = 618, posY = 234, height = 66, width = 66, visual = ColorVisual(0, 255, 0, 0)),

        //Truck 2
        Area<TokenView>(posX = 738, posY = 91, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 738, posY = 163, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 738, posY = 234, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Truck 3
        Area<TokenView>(posX = 858, posY = 91, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 858, posY = 163, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 858, posY = 234, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Truck 4
        Area<TokenView>(posX = 978, posY = 91, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 978, posY = 163, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 978, posY = 234, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Truck 5
        Area<TokenView>(posX = 1098, posY = 91, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 1098, posY = 163, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 1098, posY = 234, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0))
    )

    private val animalHoles = arrayOf(
        //Enclosure 1
        Area<TokenView>(posX = 946, posY = 390, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 967, posY = 467, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 932, posY = 544, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 1018, posY = 587, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Enclosure 2
        Area<TokenView>(posX = 770, posY = 410, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 664, posY = 507, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 770, posY = 490, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 644, posY = 584, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 749, posY = 566, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Enclosure 3
        Area<TokenView>(posX = 962, posY = 710, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 1085, posY = 735, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 986, posY = 788, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 1069, posY = 812, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 962, posY = 863, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 972, posY = 942, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //bonus Enclosure 4
        Area<TokenView>(posX = 491, posY = 446, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 510, posY = 531, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 495, posY = 616, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 510, posY = 701, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 495, posY = 787, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //bonus Enclosure 5
        Area<TokenView>(posX = 352, posY = 446, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 371, posY = 531, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 357, posY = 616, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 371, posY = 701, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 357, posY = 787, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0))
    )

    private val vendingStallHoles = arrayOf(
        //Enclosure 1
        Area<TokenView>(posX = 1102, posY = 390, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),
        Area<TokenView>(posX = 1102, posY = 466, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Enclosure 2
        Area<TokenView>(posX = 635, posY = 390, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Enclosure 3
        Area<TokenView>(posX = 1102, posY = 941, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //Bonus Enclosure 4
        Area<TokenView>(posX = 511, posY = 941, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0)),

        //bonus Enclosure 5
        Area<TokenView>(posX = 372, posY = 941, height = 66, width = 66, visual = ColorVisual(255, 0, 0, 0))
    )

    val barnHole =
        Area<TokenView>(posX = 621, posY = 686, height = 323, width = 284, visual = ColorVisual(255, 0, 0, 0))


    //Menu Button
    val menuButton = Button(
        posX = 1600,
        posY = 20,
        height = 100,
        width = 200,
        text = "Menu",
        visual = ColorVisual(250, 194, 19)
    ).apply {
        onMouseClicked = {
            //val game = rootService.game
            //checkNotNull(game)

            //for (tile in game.getActivePlayer().barn.animals){
            //    println(tile.type)
            //}
            //game.getActivePlayer().numCoins += 1

            //updateCoins(game.activePlayerIndex)
        }
    }

    //Hint Button
    private val hintButton = Button(
        posX = 1600,
        posY = 140,
        height = 100,
        width = 200,
        text = "Hint",
        visual = ColorVisual(250, 194, 19)
    ).apply {
        onMouseClicked = {
            rs.game.let {
                rs.playerActionService.showHint()
            }
        }
    }

    /** opaque buttons to select the different enclosures (for exchange action) */
    val enclosureSelectionButtons = listOf(
        Button(visual = ColorVisual(Color.red), posX = 905, posY = 368, width = 273, height = 295),
        Button(visual = ColorVisual(Color.red), posX = 618, posY = 367, width = 248, height = 305),
        Button(visual = ColorVisual(Color.red), posX = 932, posY = 680, width = 247, height = 358),
        Button(visual = ColorVisual(Color.red), posX = 470, posY = 372, width = 134, height = 668),
        Button(visual = ColorVisual(Color.red), posX = 327, posY = 372, width = 134, height = 678),
    )

    // further initialize the enclosure selection buttons
    init {
        enclosureSelectionButtons.forEachIndexed { index, button ->
            button.apply {
                isVisible = false
                opacity = 0.65
                // This is the code that will get executed when the enclosure was clicked after the exchange function
                // was selected
                onMouseClicked = {
                    this.visual = ColorVisual(Color.blue)

                    // If this enclosure was already selected then deselect it
                    if (selectedEnclosure1 == index) {
                        selectedEnclosure1 = -1
                        enclosureSelectionButtons[index].visual = ColorVisual(Color.red)
                    } else if (selectedEnclosure2 == index) {
                        selectedEnclosure2 = -1
                        enclosureSelectionButtons[index].visual = ColorVisual(Color.red)
                    }
                    // Otherwise select this enclosure and if there already were two enclosures selected then remove the
                    // first selected one
                    else {
                        // Otherwise push one of the already selected enclosures back into their normal state
                        if (selectedEnclosure1 == -1) selectedEnclosure1 = index
                        else if (selectedEnclosure2 == -1) selectedEnclosure2 = index
                        else {
                            val tempIndex = selectedEnclosure1
                            selectedEnclosure1 = selectedEnclosure2
                            selectedEnclosure2 = index

                            // Reset color of the now unselected enclosure
                            enclosureSelectionButtons[tempIndex].visual = ColorVisual(Color.red)
                        }
                    }
                }
            }
        }
    }


    //Undo - Redo Button
    private val undoButton = Button(
        posX = 50,
        posY = 20,
        height = 200,
        width = 100,
        visual = ImageVisual(imageLoader.undoButtonImage)
    ).apply {
        onMouseClicked = {
            rs.gameService.undo()
        }
    }
    private val redoButton = Button(
        posX = 200,
        posY = 20,
        height = 200,
        width = 100,
        visual = ImageVisual(imageLoader.undoButtonImage)
    ).apply {
        onMouseClicked = {
            rs.gameService.redo()
        }
    }

    //Labels
    private val coinsValueLabel =
        Label(posX = 1470, posY = 900, height = 100, width = 100, visual = ImageVisual(imageLoader.backgroundImage))
    private val tileValueLabel =
        Label(posX = 1470, posY = 300, height = 100, width = 100, visual = ImageVisual(imageLoader.coverImage))

    //Labels Visual
    private val coinsValueLabelVisual =
        Label(posX = 1470, posY = 900, height = 100, width = 100, text = "2", font = Font(size = 32))
    private val tileValueLabelVisual =
        Label(posX = 1470, posY = 300, height = 100, width = 100, text = "31", font = Font(size = 32))

    //Board Image
    private val boardVisual =
        Label(posX = 600, posY = 350, width = 600, height = 700, visual = ImageVisual(imageLoader.boardImage))

    private val expansionBoardVisuals = arrayOf(
        CardView(
            posX = 462, posY = 350, width = 138, height = 700,
            back = ImageVisual(imageLoader.expansionBoardImage_1, height = 2854, width = 562, offsetX = 17, offsetY = 0),
            front = ImageVisual(imageLoader.expansionBoardImage_1, height = 2854, width = 562, offsetX = 605, offsetY = 0)
        ),
        CardView(
            posX = 324, posY = 350, width = 138, height = 700,
            back = ImageVisual(imageLoader.expansionBoardImage_2, height = 2854, width = 562, offsetX = 17, offsetY = 0),
            front = ImageVisual(imageLoader.expansionBoardImage_2, height = 2854, width = 562, offsetX = 605, offsetY = 0)
        )
    )

    init {
        background = ImageVisual(imageLoader.backgroundImage)
        redoButton.rotation = 180.0
        tileValueLabel.opacity = 0.5

        takenTruckVisual.isVisible = false
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        addDropLogicToTruckHoles()
        addDropLogicToAnimalHoles()
        addDropLogicToVendingStallHoles()
        addDropLogicToBarnHole()
        addEnclosureSelectionLogic()
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        for (truckButton in truckButtons) {
            addTruckSelection(truckButton)
            this.addComponents(truckButton)
        }

        playerButtons.forEach { addComponents(it) }

        this.addComponents(
            drawATileButton, takeADeliveryButton, moveButton, exchangeButton, expandZooButton, purchaseButton,
            menuButton, hintButton, undoButton, redoButton, applyExchangeButton, cancelExchangeButton,
            coinsValueLabel, tileValueLabel,
            coinsValueLabelVisual, tileValueLabelVisual, boardVisual, barnHole, discardButton, backToActivePlayerButton,
            takenTruckVisual, aiLabel
        )
        for (hole in truckHoles) {
            this.addComponents(hole)
        }
        for (hole in takenTruckHoles) {
            this.addComponents(hole)
        }
        for (expansionBoard in expansionBoardVisuals) {
            expansionBoard.showBack()
            this.addComponents(expansionBoard)
        }
        for (i in 0..24) {
            this.addComponents(animalHoles[i])
        }
        for (hole in vendingStallHoles) {
            this.addComponents(hole)
        }
        for (it in enclosureSelectionButtons) addComponents(it)

    }

    private fun addTruckSelection(truckButton: Button) {
        takeADeliveryButton.isDisabled = true

        truckButton.apply {
            onMouseClicked = {
                if (!addingTilesToZoo) {
                    selectTruck(truckButtons.indexOf(truckButton), true)
                }
            }
        }

    }

    /** Updates the tile count on top of the draw pile */
    private fun updateTileCount() {
        val game = rs.game
        checkNotNull(game)

        var remainingCardsNum = -1

        // While the draw pile has cards left render the draw pile size
        if (!rs.playerActionService.isTileStackEmpty()) {
            val filteredTiles = game.tileStack.tiles.filterNot {
                it is AnimalTile && it.variant == AnimalVariant.CHILD
            }
            remainingCardsNum = filteredTiles.size
        } else {
            remainingCardsNum = game.endStack.tiles.size
        }
        tileValueLabelVisual.text = "$remainingCardsNum"
    }


    //adds the logic to select enclosures via the enclosureSelectionButtons
    private fun addEnclosureSelectionLogic() {
        enclosureSelectionButtons.forEachIndexed { index, button ->
            button.apply {

                isVisible = false
                opacity = 0.65
                // This is the code that will get executed when the enclosure was clicked after the exchange function
                // was selected
                onMouseClicked = {
                    //println("pressed")
                    this.visual = ColorVisual(Color.blue)

                    // If this enclosure was already selected then deselect it
                    if (selectedEnclosure1 == index) {
                        selectedEnclosure1 = -1
                        enclosureSelectionButtons[index].visual = ColorVisual(Color.red)
                    } else if (selectedEnclosure2 == index) {
                        selectedEnclosure2 = -1
                        enclosureSelectionButtons[index].visual = ColorVisual(Color.red)
                    }
                    // Otherwise select this enclosure and if there already were two enclosures selected then remove the
                    // first selected one
                    else {
                        // Otherwise push one of the already selected enclosures back into their normal state
                        if (selectedEnclosure1 == -1) selectedEnclosure1 = index
                        else if (selectedEnclosure2 == -1) selectedEnclosure2 = index
                        else {
                            val tempIndex = selectedEnclosure1
                            selectedEnclosure1 = selectedEnclosure2
                            selectedEnclosure2 = index

                            // Reset color of the now unselected enclosure
                            enclosureSelectionButtons[tempIndex].visual = ColorVisual(Color.red)
                        }
                    }
                    setIsDisabledForApplyExchangeButton(false)
                }
            }
        }
    }

    private fun addDropLogicToTruckHoles() {
        for (hole in truckHoles) {
            hole.dropAcceptor = { dragEvent ->
                //println((dragEvent.draggedComponent is GuiTile) && hole.isEmpty() && !addingTilesToZoo && !moving && truckButtons[truckHoles.indexOf(hole) / 3].isVisible)
                (dragEvent.draggedComponent is GuiTile)
                        && hole.isEmpty()
                        && !addingTilesToZoo
                        && !moving
                        && truckButtons[truckHoles.indexOf(hole) / 3].isVisible
            }
            hole.onDragDropped = { dragEvent ->
                hole.add((dragEvent.draggedComponent as GuiTile).apply { reposition(0, 0) })
                (dragEvent.draggedComponent as GuiTile).isDraggable = false
                val game = rs.game
                checkNotNull(game)
                rs.playerActionService.addTileToTruck(
                    (dragEvent.draggedComponent as GuiTile).tile,
                    game.deliveryTrucks[truckHoles.indexOf(hole) / 3]
                )
                //updateSingleTruck(selectedTruck)
                setIsDisabledForAllAktionButtons(false)
            }
        }
    }

    private fun addDropLogicToAnimalHoles() {
        animalHoles.forEachIndexed { index, hole ->

            hole.dropAcceptor = { dragEvent ->
                val game = rs.game
                checkNotNull(game)

                (dragEvent.draggedComponent is GuiTile) && ((dragEvent.draggedComponent as GuiTile).tile is AnimalTile)
                        && hole.isEmpty() && (addingTilesToZoo || moving) &&
                        if (getEnclosureByAnimalHoleIndex(animalHoles.indexOf(hole)) <= 2) {
                            !(game.getActivePlayer().enclosures[getEnclosureByAnimalHoleIndex(index)]
                                .animals.isNotEmpty() &&
                                    game.getActivePlayer().enclosures[getEnclosureByAnimalHoleIndex(index)]
                                        .getAnimalType() != ((dragEvent.draggedComponent as GuiTile).tile as AnimalTile)
                                .type)
                        } else {
                            !(game.players.size != 2 && getEnclosureByAnimalHoleIndex(index) == 4) &&
                                    game.getActivePlayer().expansionBoards[getEnclosureByAnimalHoleIndex(index) - 3]
                                        .isUsed &&
                                    !(game.getActivePlayer().expansionBoards[getEnclosureByAnimalHoleIndex(index) - 3]
                                        .animals.isNotEmpty() &&
                                            game.getActivePlayer()
                                                .expansionBoards[getEnclosureByAnimalHoleIndex(index) - 3]
                                                .getAnimalType() != ((dragEvent.draggedComponent as GuiTile)
                                        .tile as AnimalTile).type)
                        }
            }
            hole.onDragDropped = { dragEvent ->
                hole.add((dragEvent.draggedComponent as GuiTile).apply { reposition(0, 0) })
                (dragEvent.draggedComponent as GuiTile).isDraggable = false
                val game = rs.game
                checkNotNull(game)
                val enclosure = getEnclosureByAnimalHoleIndex(index)

                //println("addingTilesToZoo : $addingTilesToZoo")
                //println("moving : $moving")
                if (addingTilesToZoo) {
                    if (enclosure <= 2) {
                        // println(((dragEvent.draggedComponent as GuiTile).tile as AnimalTile).type.toString())
                        rs.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
                            (dragEvent.draggedComponent as GuiTile).tile, game.deliveryTrucks[selectedTruck],
                            game.getActivePlayer().enclosures[enclosure]
                        )
                    } else {
                        if (game.getActivePlayer().expansionBoards[enclosure - 3].isUsed) {
                            rs.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
                                (dragEvent.draggedComponent as GuiTile).tile, game.deliveryTrucks[selectedTruck],
                                game.getActivePlayer().expansionBoards[enclosure - 3]
                            )
                        }
                    }
                    updateTakenTruck()
                    //addingTilesToZoo = false
                } else if (moving) {
                    val dst = if (getEnclosureByAnimalHoleIndex(index) <= 2) {
                        game.getActivePlayer().enclosures[
                                getEnclosureByAnimalHoleIndex(index)
                        ]
                    } else {
                        game.getActivePlayer().expansionBoards[
                                getEnclosureByAnimalHoleIndex(index)
                        ]
                    }
                    rs.playerActionService.moveAnimalTileFromBarnToEnclosure(
                        (dragEvent.draggedComponent as GuiTile).tile as AnimalTile,
                        dst
                    )
                    barnScene.updateValues()
                }
                updateSingleEnclosureAnimals(getEnclosureByAnimalHoleIndex(animalHoles.indexOf(hole)))
            }
        }
    }

    /** Adds drop logic to the vending stall holes */
    private fun addDropLogicToVendingStallHoles() {
        vendingStallHoles.forEachIndexed { indexOfHole, hole ->
            // Defines when it is okay to drop tiles in the vending tile holes
            hole.dropAcceptor = { dragEvent ->
                val game = rs.game
                checkNotNull(game)

                val canAdd = (dragEvent.draggedComponent is GuiTile)
                        && ((dragEvent.draggedComponent as GuiTile).tile is VendingStallTile)
                        && hole.isEmpty() && (addingTilesToZoo || moving) &&
                        if (getEnclosureByVendingStallHoleIndex(indexOfHole) <= 2) {
                            true
                        } else {
                            //println("test")
                            !(game.players.size != 2 && getEnclosureByVendingStallHoleIndex(indexOfHole) == 4) &&
                                    game.getActivePlayer().expansionBoards[getEnclosureByVendingStallHoleIndex(
                                        indexOfHole
                                    ) - 3].isUsed
                        }

                canAdd
            }

            // Defines what happens when a tile is dropped into a vending tile hole
            hole.onDragDropped = { dragEvent ->

                val game = rs.game
                checkNotNull(game)


                val guiTile = dragEvent.draggedComponent as GuiTile


                val enclosureID = getEnclosureByVendingStallHoleIndex(indexOfHole)

                // Destination enclosure (might be expansion board)
                val enclosureDst = if (enclosureID <= 2) {
                    game.getActivePlayer().enclosures[enclosureID]
                } else {
                    game.getActivePlayer().expansionBoards[enclosureID - 3]
                }

                // If Destination enclosure is expansion board then make sure that the player has expanded
                if (enclosureDst is ExpansionBoard) {
                    check(enclosureDst.isUsed) { "Error: Trying to add vending stall tile to expansion board without " +
                            "having expanded first" }
                }

                hole.add(guiTile.apply { reposition(0, 0) })

                if (addingTilesToZoo) {
                    // Right now in the process of taking tiles from a selected truck

                    // Add vending stall from truck to enclosure destination
                    rs.playerActionService.addAnimalOrVendingStallTileFromTruckToPlayerEnclosure(
                        guiTile.tile, game.deliveryTrucks[selectedTruck], enclosureDst
                    )
                    updateTakenTruck()

                } else if (moving) {
                    // You moved the vending stall via the move action

                    if (vendingStallHoleSelected) {
                        // Vending stall on the board was selected as the source

                        // id of the selected enclosure
                        val srcEnclosureID = getEnclosureByVendingStallHoleIndex(selectedVendingStallHole)

                        // selected enclosure (might be expansion board)
                        val enclosureSrc = if (srcEnclosureID <= 2) {
                            game.getActivePlayer().enclosures[srcEnclosureID]
                        } else {
                            game.getActivePlayer().expansionBoards[srcEnclosureID - 3]
                        }

                        // If the source is an expansion board, make sure the player has already expanded.
                        // Though if the rest of the code is correct this case should never happen
                        if (enclosureSrc is ExpansionBoard) {
                            check(enclosureSrc.isUsed) { "Error: Trying to take vending stall from expansion board " +
                                    "somewhere else but you have not expanded yet (How did you get here?)" }
                        }

                        // Move vending stall from enclosure source to enclosure destination
                        rs.playerActionService.moveVendingStallFromEnclosureToEnclosure(
                            enclosureSrc, enclosureDst, guiTile.tile as VendingStallTile
                        )


                    } else {
                        rs.playerActionService.moveVendingStallFromBarnToEnclosure(
                            enclosureDst, guiTile.tile as VendingStallTile
                        )
                    }
                }

            }
            hole.apply {
                onMouseClicked = {
                    if (!hole.isEmpty() && !moving) {
                        if (vendingStallHoles.indexOf(hole) == selectedVendingStallHole) {
                            if (vendingStallHoleSelected) {
                                vendingStallHoleSelected = false
                                hole.rotation = 0.0
                                setIsDisabledForMoveButton(true)
                            } else {
                                vendingStallHoleSelected = true
                                selectedVendingStallHole = vendingStallHoles.indexOf(hole)
                                hole.rotation = -20.0
                                setIsDisabledForMoveButton(false)
                            }
                        } else {
                            if (vendingStallHoleSelected) {
                                vendingStallHoles[selectedVendingStallHole].rotation = 0.0
                            }
                            vendingStallHoleSelected = true
                            selectedVendingStallHole = vendingStallHoles.indexOf(hole)
                            hole.rotation = -20.0
                            setIsDisabledForMoveButton(false)
                        }
                    }
                }
            }
        }
    }

    private fun addDropLogicToBarnHole() {
        barnHole.dropAcceptor = { dragEvent ->
            (dragEvent.draggedComponent is GuiTile) && addingTilesToZoo && !moving
        }
        barnHole.onDragDropped = { dragEvent ->
            val game = rs.game
            checkNotNull(game)

            rs.playerActionService.addTileFromTruckToPlayerBarn(
                (dragEvent.draggedComponent as GuiTile).tile, game.deliveryTrucks[selectedTruck]
            )
            barnHole.add(
                (dragEvent.draggedComponent as GuiTile)
                //.apply { reposition(0, 0)  }
            )
            barnHole.remove(dragEvent.draggedComponent as GuiTile)
            barnScene.updateValues()
            updateTakenTruck()
        }
    }

    /** Disable the Areas that are not in use for the current game.
     *
     * For example in a three player game there is no use for the areas on truck 4 and 5 so they get disabled.
     */
    private fun disableHoles() {
        val game = rs.game
        checkNotNull(game)

        // enable all areas
        vendingStallHoles.forEach { it.isDisabled = false }
        animalHoles.forEach { it.isDisabled = false }
        truckHoles.forEach { truckHole ->
            truckHole.visual = Visual.EMPTY
            truckHole.isDisabled = false
        }


        // Disable the holes that can not be used with the cur amount of players
        if (game.players.size == 2) {

            // Rules for 2 players
            for (i in truckHoles.indices) {
                truckHoles[i].isDisabled = !(i == 0 || i in 3..4 || (i in 6..8))
                if (i in 1..2 || i == 5) {
                    truckHoles[i].visual = ColorVisual(0, 0, 255, 100)
                }
            }

        } else {

            // Rules for > 2 players
            truckHoles.forEachIndexed { index, truckHole ->
                truckHole.isDisabled = index > game.players.size * 3 - 1
            }
            for (i in 20..24) {
                animalHoles[i].isDisabled = true
            }
            vendingStallHoles[5].isDisabled = true

        }
    }

    /** enables and shows only the trucks that are appropriate for the player count */
    private fun disableTrucks() {
        // TODO does this function need to be called after every turn or is it enough to call it once?
        val game = rs.game
        checkNotNull(game)
        if (game.players.size == 2) {
            for (i in truckButtons.indices) {
                truckButtons[i].isDisabled = i >= 3
                truckButtons[i].isVisible = i < 3
            }

        } else {
            for (i in truckButtons.indices) {
                truckButtons[i].isDisabled = i >= game.players.size
                truckButtons[i].isVisible = i < game.players.size
            }
        }
    }

    /**
     * The setIsDisabledForAllAktionButtons fun
     */
    private fun setIsDisabledForAllAktionButtons(isDisabled: Boolean) {
        val game = rs.game
        checkNotNull(game) { "Error game null" }

        var disable = isDisabled
        if (game.getActivePlayer() is PlayerAI) {
            disable = true
        }
        hintButton.isDisabled = disable
        setIsDisabledForTakeADeliveryButton(disable)
        setIsDisabledForMoveButton(disable)
        exchangeButton.isDisabled = disable
        setIsDisabledForPurchaseButton(disable)
        setIsDisabledForDiscardButton(disable)
        setIsDisabledForExpandZooButton(disable)
        setIsDisabledForDrawButton(disable)
    }

    /**
     * The setIsDisabledForTakeADeliveryButton fun
     */
    private fun setIsDisabledForTakeADeliveryButton(isDisabled: Boolean) {
        if (isDisabled) {
            takeADeliveryButton.isDisabled = true
        } else {
            takeADeliveryButton.isDisabled = !truckSelected
        }
    }

    private fun setIsDisabledForDrawButton(isDisabled: Boolean) {
        val game = rs.game
        checkNotNull(game)

        drawATileButton.isDisabled = if (isDisabled) {
            true
        } else {
            var spaceInTrucks = false
            if (game.players.size > 2) {
                for (i in 0 until game.players.size) {
                    if (game.deliveryTrucks[i].tiles.size < game.deliveryTrucks[i].maxSize
                        && truckButtons[i].isVisible
                    ) spaceInTrucks = true
                }
            } else {
                for (i in 0 until 3) {
                    if (game.deliveryTrucks[i].tiles.size < i + 1) spaceInTrucks = true
                }
            }
            !spaceInTrucks
        }
    }

    /**
     * The setIsDisabledForDiscardButton fun
     */
    fun setIsDisabledForDiscardButton(isDisabled: Boolean) {
        discardButton.isDisabled = if (isDisabled) {
            true
        } else {
            !barnScene.tileSelected
        }
    }

    /**
     * Thek setIsDisabledForMoveButton fun
     */
    fun setIsDisabledForMoveButton(isDisabled: Boolean) {
        moveButton.isDisabled = if (isDisabled) {
            true
        } else {
            !barnScene.tileSelected && !vendingStallHoleSelected
        }
    }

    /**
     * The setIsDisabledForPurchaseButton fun
     */
    fun setIsDisabledForPurchaseButton(isDisabled: Boolean) {
        purchaseButton.isDisabled = if (isDisabled) {
            true
        } else {
            !barnScene.otherPlayersTileSelected
        }
    }

    /**
     * The setIsDisabledForExpandZooButton fun
     */
    private fun setIsDisabledForExpandZooButton(isDisabled: Boolean) {
        val game = rs.game
        checkNotNull(game)
        if (game.getActivePlayer().numCoins < 3 || isDisabled) {

            expandZooButton.isDisabled = true
        } else {
            expandZooButton.isDisabled = game.getActivePlayer().expansionBoards.none { board -> !board.isUsed }
        }
    }

    /**
     * The setIsDisabledForApplyExchangeButton fun
     */
    fun setIsDisabledForApplyExchangeButton(isDisabled: Boolean) {
        // println("disable Buttons: $isDisabled")
        var disable: Boolean
        if (isDisabled) {
            disable = true
        } else {
            if (selectedEnclosure1 != -1 && selectedEnclosure2 != -1) {
                val game = rs.game
                checkNotNull(game)

                val enclosure1 = if (selectedEnclosure1 <= 2) {
                    game.getActivePlayer().enclosures[selectedEnclosure1]
                } else {
                    game.getActivePlayer().expansionBoards[selectedEnclosure1 - 3]
                }

                val enclosure2 = if (selectedEnclosure2 <= 2) {
                    game.getActivePlayer().enclosures[selectedEnclosure2]
                } else {
                    game.getActivePlayer().expansionBoards[selectedEnclosure2 - 3]
                }

                disable = !(enclosure1.animals.isNotEmpty() && enclosure2.animals.isNotEmpty() &&
                        enclosure1.animals[0].type != enclosure2.animals[0].type &&
                        enclosure1.animals.size <= enclosure2.animalsMax &&
                        enclosure2.animals.size <= enclosure1.animalsMax)

            } else if ((selectedEnclosure1 != -1 || selectedEnclosure2 != -1) && barnScene.tileSelected) {
                val game = rs.game
                checkNotNull(game)

                val enclosure = if (selectedEnclosure1 != -1) {
                    if (selectedEnclosure1 <= 2) {
                        game.getActivePlayer().enclosures[selectedEnclosure1]
                    } else {
                        game.getActivePlayer().expansionBoards[selectedEnclosure1 - 3]
                    }
                } else {
                    if (selectedEnclosure2 <= 2) {
                        game.getActivePlayer().enclosures[selectedEnclosure2]
                    } else {
                        game.getActivePlayer().expansionBoards[selectedEnclosure2 - 3]
                    }
                }

                val animalsOfSelectionInBarn = barnScene.getNumberOfAnimalsOfSelectedTileInBarn()

                disable =
                    !(enclosure.animals.isNotEmpty() && animalsOfSelectionInBarn != 0 &&
                            animalsOfSelectionInBarn <= enclosure.animalsMax &&
                            barnScene.getSelectedTile() is AnimalTile &&
                            (barnScene.getSelectedTile() as AnimalTile).type != enclosure.animals[0].type)
            } else {
                disable = true
            }
        }
        // println(disable)
        applyExchangeButton.isDisabled = disable
    }

    /** Maps animal hole ID's to player enclosure ID's */
    private fun getEnclosureByAnimalHoleIndex(holeIndex: Int): Int {
        return when (holeIndex) {
            0, 1, 2, 3 -> 0
            4, 5, 6, 7, 8 -> 1
            9, 10, 11, 12, 13, 14 -> 2
            15, 16, 17, 18, 19 -> 3
            20, 21, 22, 23, 24 -> 4
            else -> throw IndexOutOfBoundsException("the index is out of bound")
        }
    }

    /** Maps vending stall hole ID's to enclosure ID's */
    private fun getEnclosureByVendingStallHoleIndex(holeIndex: Int): Int {
        return when (holeIndex) {
            0, 1 -> 0
            2 -> 1
            3 -> 2
            4 -> 3
            5 -> 4
            else -> throw IndexOutOfBoundsException("the index is out of bound")
        }
    }

    // TODO document this function
    private fun getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex: Int): Int {
        return when (enclosureIndex) {
            0 -> 0
            1 -> 4
            2 -> 9
            3 -> 15
            4 -> 20
            else -> throw IndexOutOfBoundsException("the index is out of bound")
        }
    }

    // TODO document this function
    private fun getLowestVendingstallHoleIndexByEnclosureIndex(enclosureIndex: Int): Int {
        return when (enclosureIndex) {
            0 -> 0
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            else -> throw IndexOutOfBoundsException("the index is out of bound")
        }
    }


    /** Render the correct amount of coins for the current player */
    private fun updateCoins(playerIndex: Int) {
        val game = rs.game
        checkNotNull(game)

        coinsValueLabelVisual.text = "${game.players[playerIndex].numCoins}"
    }

    /** show drawn tile */
    private fun showDrawnTile() {

        val newTile = GuiTile(
            posX = 1200,
            posY = 100,
            lastDrawnTile,
            visual = ImageVisual(imageLoader.frontImageFor(lastDrawnTile))
        )
        newTile.isDraggable = true
        this.addComponents(newTile)
    }

    /** Show 1 or 2 expansion boards depending on whether it is a 2 player game.
     *
     * Only needs to be called once, since the amount of times you can expand doesn't
     * change over the course of a single game.
     *
     * Expansion boards are always visible but the back of the expansion board is shown
     * for players that have not yet expanded.
     */
    private fun showExpansionBoardsForPlayercount() {
        val game = rs.game
        checkNotNull(game)
        expansionBoardVisuals[1].isVisible = game.players.size == 2
    }

    /** Render board of the player with index playerIndex. Disables all buttons */
    private fun showOtherPlayersBoard(playerIndex: Int) {
        val game = rs.game
        checkNotNull(game)

        clearBoard()
        fillBoard(playerIndex)
        updateCoins(playerIndex)
        for (expansionBoardIndex in game.players[playerIndex].expansionBoards.indices) {
            if (game.players[playerIndex].expansionBoards[expansionBoardIndex].isUsed) {
                expansionBoardVisuals[expansionBoardIndex].showFront()
            } else {
                expansionBoardVisuals[expansionBoardIndex].showBack()
            }
        }
        barnScene.unselectTile()
        barnScene.showOtherPlayersBarn(playerIndex)
        barnScene.playerIndex = playerIndex
        hideActionButtons(playerIndex != game.activePlayerIndex)
    }


    /** Updates the board so all information about the current player is shown there
     *
     * Updates the following things (visually)
     * - Num coins the player has
     * - Animal tiles in enclosures
     * - Vending stall tiles of the player
     * - tiles in barn
     * - whether the player has expanded or not
     */
    private fun updateBoard() {
        val game = rs.game
        checkNotNull(game)

        // Update all
        clearBoard()
        fillBoard(game.activePlayerIndex)
        updateCoins(game.activePlayerIndex)
        barnScene.updateValues()

        // Show expansion boards the player has purchased
        for (index in game.getActivePlayer().expansionBoards.indices) {
            val curExpansionBoard = game.getActivePlayer().expansionBoards[index]
            val curExpansionBoardVisual = expansionBoardVisuals[index]

            if (curExpansionBoard.isUsed) {
                curExpansionBoardVisual.showFront()
            } else {
                curExpansionBoardVisual.showBack()
            }
        }
    }

    /** Clears all tiles from the player board (visually) */
    private fun clearBoard() {
        for (hole in animalHoles) {
            if (!hole.isEmpty()) {
                hole.clear()
            }
        }
        // println("clearing holes")
        for (hole in vendingStallHoles) {
            if (!hole.isEmpty()) {
                hole.clear()
                // println("Hole Nr. ${vendingStallHoles.indexOf(hole)} cleared")
            }
        }
        /*
        for (hole in truckHoles){
            if (!hole.isEmpty()){
                hole.remove(hole.components[0])
            }
        }
        */
    }

    /** Updates the tiles on the board to match the entity layer */
    private fun fillBoard(playerIndex: Int) {
        val game = rs.game
        checkNotNull(game)

        for (enclosureIndex in game.players[playerIndex].enclosures.indices) {
            for (tileIndex in game.players[playerIndex].enclosures[enclosureIndex].animals.indices) {
                val newTile = game.players[playerIndex].enclosures[enclosureIndex].animals[tileIndex]
                animalHoles[getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex) + tileIndex].add(
                    GuiTile(
                        posX = 0, posY = 0, newTile,
                        ImageVisual(imageLoader.frontImageFor(newTile))
                    )
                )
            }
            for (tileIndex in game.players[playerIndex].enclosures[enclosureIndex].vendingStalls.indices) {
                val newTile = game.players[playerIndex].enclosures[enclosureIndex].vendingStalls[tileIndex]
                vendingStallHoles[getLowestVendingstallHoleIndexByEnclosureIndex(enclosureIndex) + tileIndex].add(
                    GuiTile(
                        posX = 0, posY = 0, newTile,
                        ImageVisual(imageLoader.frontImageFor(newTile))
                    )
                )
            }
        }
        for (enclosureIndex in game.players[playerIndex].expansionBoards.indices) {
            for (tileIndex in game.players[playerIndex].expansionBoards[enclosureIndex].animals.indices) {
                val newTile = game.players[playerIndex].expansionBoards[enclosureIndex].animals[tileIndex]
                animalHoles[getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex + 3) + tileIndex].add(
                    GuiTile(
                        posX = 0, posY = 0, newTile,
                        ImageVisual(imageLoader.frontImageFor(newTile))
                    )
                )
            }
            for (tileIndex in game.players[playerIndex].expansionBoards[enclosureIndex].vendingStalls.indices) {
                val newTile = game.players[playerIndex].expansionBoards[enclosureIndex].vendingStalls[tileIndex]
                vendingStallHoles[
                        getLowestVendingstallHoleIndexByEnclosureIndex(enclosureIndex + 3) + tileIndex
                ].add(
                    GuiTile(
                        posX = 0, posY = 0, newTile,
                        ImageVisual(imageLoader.frontImageFor(newTile))
                    )
                )
            }
        }
    }

    private fun hideActionButtons(hide: Boolean) {
        drawATileButton.isVisible = !hide
        takeADeliveryButton.isVisible = !hide
        moveButton.isVisible = !hide
        exchangeButton.isVisible = !hide
        purchaseButton.isVisible = !hide
        discardButton.isVisible = !hide
        expandZooButton.isVisible = !hide

        backToActivePlayerButton.isVisible = hide
    }

    /** Hide the player buttons of players that exceed the amount of players in the game currently */
    private fun hidePlayerButtons() {
        val game = rs.game
        checkNotNull(game)

        for (i in 0..4) {
            playerButtons[i].isVisible = i < game.players.size
        }
    }

    private fun takeTruck() {
        var game = rs.game
        checkNotNull(game)

        for (i in 0..2) {
            truckHoles[i + 3 * selectedTruck].clear()
        }
        takenTruckVisual.isVisible = true
        truckButtons[selectedTruck].isVisible = false
        for (tile in game.deliveryTrucks[selectedTruck].tiles) {
            if (tile is CoinTile) {
                rs.playerActionService.addAllCoinTilesFromTruckToPlayer(game.deliveryTrucks[selectedTruck])
                break
            }
        }

        game = rs.game
        checkNotNull(game)

        for (tileIndex in game.deliveryTrucks[selectedTruck].tiles.indices) {
            val newTile = game.deliveryTrucks[selectedTruck].tiles[tileIndex]
            takenTruckHoles[tileIndex].add(GuiTile(0, 0, newTile, ImageVisual(
                imageLoader.frontImageFor(newTile)
            )))
            takenTruckHoles[tileIndex].components[0].isDraggable = true
        }
    }

    /** Render all trucks that have not been taken this round at the top */
    private fun showTrucksNotTaken() {
        val game = rs.game
        checkNotNull(game)

        for (truckIndex in game.deliveryTrucks.indices) {
            val truckIsVisible = !game.deliveryTrucks[truckIndex].isTaken
            truckButtons[truckIndex].isVisible = truckIsVisible
        }
    }

    private fun resetExchange() {

        currentlySelectingEnclosures = false
        selectedEnclosure1 = -1
        selectedEnclosure2 = -1
        enclosureSelectionButtons.forEach {
            it.apply {
                visual = ColorVisual(Color.red)
                isVisible = false
            }
        }
        showExchangeButtons()
    }

    /*
    private fun showAllTrucksAndResetPosition(){
        val game = rootService.game
        checkNotNull(game)

        for (truck in game.deliveryTrucks){
            truckButtons[game.deliveryTrucks.indexOf(truck)].isVisible = true
        }

        truckHoles[1].isVisible = true
        truckHoles[2].isVisible = true
        truckHoles[5].isVisible = true
    }
    */

    private fun selectTruck(truckIndex: Int, select: Boolean) {
        if (select) {
            if (truckSelected) {
                truckButtons[selectedTruck].posY = 20.0
                truckHoles[0 + 3 * selectedTruck].posY = 91.0
                truckHoles[1 + 3 * selectedTruck].posY = 163.0
                truckHoles[2 + 3 * selectedTruck].posY = 234.0
            }
            selectedTruck = truckIndex
            truckButtons[selectedTruck].posY = 10.0
            truckHoles[0 + 3 * selectedTruck].posY = 81.0
            truckHoles[1 + 3 * selectedTruck].posY = 153.0
            truckHoles[2 + 3 * selectedTruck].posY = 224.0
            truckSelected = true

            val game = rs.game
            checkNotNull(game)

            takeADeliveryButton.isDisabled = game.deliveryTrucks[selectedTruck].tiles.isEmpty()
        } else {
            truckButtons[selectedTruck].posY = 20.0
            truckHoles[0 + 3 * selectedTruck].posY = 91.0
            truckHoles[1 + 3 * selectedTruck].posY = 163.0
            truckHoles[2 + 3 * selectedTruck].posY = 234.0
            truckSelected = false
        }
    }

    private fun updateSingleTruck(truckIndex: Int) {
        val game = rs.game
        checkNotNull(game)

        for (i in 0..2) {
            truckHoles[i + 3 * truckIndex].clear()
        }
        for (i in game.deliveryTrucks[truckIndex].tiles.indices) {
            val newTile = game.deliveryTrucks[truckIndex].tiles[i]
            val newGuiTile = GuiTile(posX = 0, posY = 0, newTile, ImageVisual(imageLoader.frontImageFor(newTile)))
            truckHoles[i + 3 * truckIndex].add(newGuiTile)
        }
    }

    //updates the animals of a single enclosure (called everytime an animal gets added to an enclosure)
    private fun updateSingleEnclosureAnimals(enclosureIndex: Int) {
        val game = rs.game
        checkNotNull(game)

        val upperEnclosureBound = if (enclosureIndex < 5) {
            getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex + 1) - 1
        } else {
            24
        }
        for (holeIndex in getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex)..upperEnclosureBound) {
            animalHoles[holeIndex].clear()
        }
        if (enclosureIndex <= 2) {
            for (tileIndex in game.getActivePlayer().enclosures[enclosureIndex].animals.indices) {
                val newTile = game.getActivePlayer().enclosures[enclosureIndex].animals[tileIndex]
                animalHoles[getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex) + tileIndex].add(
                    GuiTile(
                        posX = 0, posY = 0, newTile,
                        ImageVisual(imageLoader.frontImageFor(newTile))
                    )
                )
            }
        } else {
            for (tileIndex in game.getActivePlayer().expansionBoards[enclosureIndex - 3].animals.indices) {
                val newTile = game.getActivePlayer().expansionBoards[enclosureIndex - 3].animals[tileIndex]
                animalHoles[getLowestAnimalHoleIndexByEnclosureIndex(enclosureIndex) + tileIndex].add(
                    GuiTile(
                        posX = 0, posY = 0, newTile,
                        ImageVisual(imageLoader.frontImageFor(newTile))
                    )
                )
            }
        }
    }

    /** Renders the taken delivery truck if there is one */
    private fun updateTakenTruck() {
        // TODO combine some of these functions for rendering the delivery trucks into a bigger one
        val game = rs.game
        checkNotNull(game)
        val deliveryTruck = game.getActivePlayer().deliveryTruck
        if (deliveryTruck != null) {
            for (hole in takenTruckHoles) {
                hole.clear()
            }

            for (i in deliveryTruck.tiles.indices) {
                val newTile = deliveryTruck.tiles[i]
                val newGuiTile = GuiTile(posX = 0, posY = 0, newTile, ImageVisual(imageLoader.frontImageFor(newTile)))
                newGuiTile.isDraggable = true
                takenTruckHoles[i].add(newGuiTile)
            }
        }
    }

    private fun showExchangeButtons() {
        exchangeButton.isVisible = !currentlySelectingEnclosures
        applyExchangeButton.isVisible = currentlySelectingEnclosures
        cancelExchangeButton.isVisible = currentlySelectingEnclosures
    }

    override fun refreshAfterDiscardTile() {
        refreshAllVisuals()
    }

    override fun refreshAfterExpanding() {
        refreshAllVisuals()
    }

    override fun refreshAfterGameStart() {
        val game = rs.game
        checkNotNull(game)

        // Show 1 or 2 expansion boards depending on player count
        showExpansionBoardsForPlayercount()

        // Disable trucks that are not in use because of player count
        disableTrucks()

        // Disable the holes that are not in play
        disableHoles()

        // Hide unneccesary player buttons
        hidePlayerButtons()

        // Refresh all visuals
        refreshAllVisuals()
    }

    override fun refreshAfterTakeTruck(truck: DeliveryTruck) {
        refreshAllVisuals()
    }

    override fun refreshAfterUsingANewExpansionBoard(player: Player) {
        refreshAllVisuals()
    }

    override fun refreshAfterNewTurn() {
        val game = rs.game
        checkNotNull(game)

        if (game.getActivePlayer() !is PlayerAI) {
            aiLabel.text = ""
        }

        updateAfterNewState()
        refreshAllVisuals()

    }

    override fun refreshAfterBuying(boughtTile: Tile) {
        updateAfterNewState()
        refreshAllVisuals()
    }

    override fun refreshAfterUndoRedo() {
        updateAfterNewState()
        refreshAllVisuals()
    }

    override fun refreshAfterNewRoundStarted() {
        updateAfterNewState()
        refreshAllVisuals()
    }

    /** testing function that gets called on every refresh. Updates all of the GUI */
    private fun refreshAllVisuals() {

        val game = rs.game
        checkNotNull(game)

        // Update the shown values in the barn
        barnScene.updateValues()

        // Updates the player board with the newest information
        updateBoard()

        // Render all untaken trucks at the top
        showTrucksNotTaken()

        // Update adding tiles status
        addingTilesToZoo = game.getActivePlayer().tookDeliveryTruck


        // Update the barnScene player index (does nothing on it's own)
        barnScene.playerIndex = game.activePlayerIndex

        // Disable all buttons if cur player is ai
        setIsDisabledForAllAktionButtons(game.getActivePlayer() is PlayerAI)
        for (truck in truckButtons) {
            truck.isDisabled = (game.getActivePlayer() is PlayerAI)
        }


        // Clear tiles off all the trucks (visually)
        for (hole in truckHoles) {
            hole.clear()
        }
        for (hole in takenTruckHoles) {
            hole.clear()
        }

        // Update all delivery trucks on the top and then hide the ones that were taken
        takenTruckVisual.isVisible = false
        for (truckIndex in game.deliveryTrucks.indices) {
            truckButtons[truckIndex].isDisabled = false
            if (game.deliveryTrucks[truckIndex].isTaken) {
                truckButtons[truckIndex].isVisible = false
            } else {
                truckButtons[truckIndex].isVisible = true
                updateSingleTruck(truckIndex)
            }
        }

        // Update the player buttons
        // Current player in green. other players in gray except when they took a truck then they are shown in black
        for (i in 0 until game.players.size) {
            val curPlayer = game.players[i]
            val curPlayerButton = playerButtons[i]
            curPlayerButton.isDisabled = false
            curPlayerButton.text = curPlayer.name

            if (curPlayer is PlayerAI) {
                curPlayerButton.text = "Ai " + curPlayerButton.text
            }

            if (!curPlayer.tookDeliveryTruck) {
                curPlayerButton.visual = ColorVisual(Color.gray)
                curPlayerButton.font = Font(color = Color.black)
            } else {
                curPlayerButton.visual = ColorVisual(Color.black)
                curPlayerButton.font = Font(color = Color.white)
                curPlayerButton.text += " (passed)"
            }
        }

        playerButtons[game.activePlayerIndex].visual = ColorVisual(Color.green)


        // Update shown tile count on top of the draw pile
        updateTileCount()

    }

    override fun refreshAfterAIMadeAMove(move: String) {
        aiLabel.font = Font(size = 16)
        aiLabel.alignment = Alignment.CENTER_LEFT
        aiLabel.text = "Berechneter Zug:\n$move"
    }

    /** Function to reset all the temporary actions that might have happened during a players turn like which token
     * they had selected or what buttons they pressed.
     *
     * Called on every new turn and after actions that change the whole game state like undo/redo
     */
    private fun updateAfterNewState() {
        val game = rs.game
        checkNotNull(game) { "Error: Game not found" }

        // reset the moving boolean
        moving = false

        // unselect the currently selected barn tile
        barnScene.unselectTile()

        // Resets exchange functionality if the button has been pressed
        resetExchange()

        // Reset vending stall selection
        if (vendingStallHoleSelected) {
            vendingStallHoles[selectedVendingStallHole].rotation = 0.0
            vendingStallHoleSelected = false
            selectedVendingStallHole = -1
        }
    }
}
