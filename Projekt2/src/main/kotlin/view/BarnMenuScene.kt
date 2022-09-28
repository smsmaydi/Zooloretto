package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual


/**
 * The barn menu scene
 */
class BarnMenuScene(val rootService: RootService) : MenuScene(1920, 1080), Refreshable {
    private val imageLoader = PileImageLoader()
    val offsetX = 750
    val offsetY = 100
    var animalCount = arrayOf(
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
    )

    /** Index of the playerID of the currently shown barn */
    var playerIndex = 0

    var tileSelected = false
    var otherPlayersTileSelected = false
    var selectedTileX = 0
    var selectedTileY = 0
    var playerOfSelectedTileIndex = 0

    val backButton = Button(height = 50, width = 100, posY = 0, posX = 0, text = "back")
    private val nameLabels = arrayOf(
        Label(
            posX = 86 + offsetX,
            posY = 0 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual("tts/neutral.png")
        ),
        Label(posX = 172 + offsetX, posY = 0 + offsetY, height = 66, width = 66, visual = ImageVisual("tts/male.png")),
        Label(
            posX = 258 + offsetX,
            posY = 0 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual("tts/female.png")
        ),
        Label(
            posX = 344 + offsetX,
            posY = 0 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual("tts/kindpng_743417.png")
        ),

        Label(
            posX = 0 + offsetX,
            posY = 86 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.FLAMINGO, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 172 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.LEOPARD, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 258 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.PANDA, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 344 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.ZEBRA, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 430 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.CAMEL, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 516 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.ELEPHANT, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 602 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.CHIMPANZEE, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 688 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.frontImageForAnimal(AnimalType.KANGAROO, AnimalVariant.NEUTRAL))
        ),
        Label(
            posX = 0 + offsetX,
            posY = 774 + offsetY,
            height = 66,
            width = 66,
            visual = ImageVisual(imageLoader.typeOneMarket)
        ),
    )
    val animalCountLabel = arrayOf(
        //Flamingo
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 86 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 86 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 86 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 86 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Leopard
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 172 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 172 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 172 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 172 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Panda
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 258 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 258 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 258 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 258 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Zebra
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 344 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 344 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 344 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 344 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Camel
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 430 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 430 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 430 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 430 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Elephant
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 516 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 516 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 516 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 516 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Chimpanzee
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 602 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 602 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 602 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 602 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Kangaroo
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 688 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 688 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 688 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 688 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        ),

        //Vendingstalls
        arrayOf(
            Label(
                posX = 86 + offsetX,
                posY = 774 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 172 + offsetX,
                posY = 774 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 258 + offsetX,
                posY = 774 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            ),
            Label(
                posX = 344 + offsetX,
                posY = 774 + offsetY,
                height = 66,
                width = 66,
                text = "0",
                font = Font(size = 32)
            )
        )
    )

    init {
        addSelectionLogic()
        for (label in nameLabels) {
            this.addComponents(label)
        }
        for (i in 0..8) {
            for (i2 in 0..3) {
                this.addComponents(animalCountLabel[i][i2])
            }
        }
        this.addComponents(backButton)
    }

    /** Updates the shown quantities of animals and vending stalls in the players barn */
    fun updateValues() {
        val game = rootService.game
        checkNotNull(game)

        animalCount = Array(9) { Array(4) { 0 } }

        for (animal in game.players[game.activePlayerIndex].barn.animals) {
            animalCount[getAnimalIndex(animal)][getVariantIndex(animal)] += 1
        }
        for (vendingStall in game.players[game.activePlayerIndex].barn.vendingStalls) {
            animalCount[8][getVendingstallIndex(vendingStall)] += 1
        }
        updateBarnLabels()
    }

    /** updates this BarnScene to show the values of another players barn animal and vending stall tiles */
    fun showOtherPlayersBarn(playerIndex: Int) {
        val game = rootService.game
        checkNotNull(game)

        animalCount = Array(9) { Array(4) { 0 } }

        for (animal in game.players[playerIndex].barn.animals) {
            animalCount[getAnimalIndex(animal)][getVariantIndex(animal)] += 1
        }
        for (vendingStall in game.players[playerIndex].barn.vendingStalls) {
            animalCount[8][getVendingstallIndex(vendingStall)] += 1
        }
        updateBarnLabels()
    }

    /** Updates all Labels in the barn */
    private fun updateBarnLabels() {
        for (type in 0..8) {
            for (variant in 0..3) {
                animalCountLabel[type][variant].text = "${animalCount[type][variant]}"
            }
        }
    }

    /** maps animal types to int */
    private fun getAnimalIndex(tile: AnimalTile): Int {
        return when (tile.type) {
            AnimalType.FLAMINGO -> 0
            AnimalType.LEOPARD -> 1
            AnimalType.PANDA -> 2
            AnimalType.ZEBRA -> 3
            AnimalType.CAMEL -> 4
            AnimalType.ELEPHANT -> 5
            AnimalType.CHIMPANZEE -> 6
            AnimalType.KANGAROO -> 7
        }
    }

    /** maps animal variants to int */
    private fun getVariantIndex(tile: AnimalTile): Int {
        return when (tile.variant) {
            AnimalVariant.NEUTRAL -> 0
            AnimalVariant.MALE -> 1
            AnimalVariant.FEMALE -> 2
            AnimalVariant.CHILD -> 3
        }
    }

    /** Maps Vending stall types to int */
    private fun getVendingstallIndex(tile: VendingStallTile): Int {
        return when (tile.type) {
            VendingStallType.V1 -> 0
            VendingStallType.V2 -> 1
            VendingStallType.V3 -> 2
            VendingStallType.V4 -> 3
        }

    }

    /** returns the currently selected tile in the barn */
    fun getSelectedTile(): Tile {
        val game = rootService.game
        checkNotNull(game)

        val playerOfBarn = if (tileSelected) {
            game.getActivePlayer()
        } else {
            game.players[playerOfSelectedTileIndex]
        }
        if (selectedTileX == 8) {
            val type = when (selectedTileY) {
                0 -> VendingStallType.V1
                1 -> VendingStallType.V2
                2 -> VendingStallType.V3
                3 -> VendingStallType.V4
                else -> {
                    throw java.lang.IndexOutOfBoundsException("index is out of bounds")
                }
            }
            for (tile in playerOfBarn.barn.vendingStalls) {
                if (tile.type == type) {
                    return tile
                }
            }
        } else {
            var type = AnimalType.KANGAROO
            var variant = AnimalVariant.FEMALE

            type = when (selectedTileX) {
                0 -> AnimalType.FLAMINGO
                1 -> AnimalType.LEOPARD
                2 -> AnimalType.PANDA
                3 -> AnimalType.ZEBRA
                4 -> AnimalType.CAMEL
                5 -> AnimalType.ELEPHANT
                6 -> AnimalType.CHIMPANZEE
                7 -> AnimalType.KANGAROO
                else -> throw java.lang.IndexOutOfBoundsException("index is out of bounds")
            }
            variant = when (selectedTileY) {
                0 -> AnimalVariant.NEUTRAL
                1 -> AnimalVariant.MALE
                2 -> AnimalVariant.FEMALE
                3 -> AnimalVariant.CHILD
                else -> throw java.lang.IndexOutOfBoundsException("index is out of bounds")
            }

            for (tile in playerOfBarn.barn.animals) {
                if (tile.type == type && tile.variant == variant) {
                    return tile
                }
            }

        }
        return AnimalTile(AnimalVariant.CHILD, AnimalType.KANGAROO, false)
    }

    /**
     * The getNumberOfAnimalsOfSelectedTileInBarn fun
     */
    fun getNumberOfAnimalsOfSelectedTileInBarn(): Int {
        updateValues()

        var animalCountOfSelected = 0
        for (version in 0..3) {
            animalCountOfSelected += animalCount[selectedTileX][version]
        }

        return animalCountOfSelected
    }

    /** deselects the tile in the barn */
    fun unselectTile() {
        if (tileSelected) {
            animalCountLabel[selectedTileX][selectedTileY].visual = ColorVisual(0, 0, 0, 0)
            tileSelected = false
        }
    }

    /** Adds the selection logic when you click on the animals in your barn */
    private fun addSelectionLogic() {
        for (type in 0..8) {
            for (variant in 0..3) {
                animalCountLabel[type][variant].apply {
                    onMouseClicked = {
                        val game = rootService.game
                        checkNotNull(game)

                        if (animalCount[type][variant] >= 1) {
                            //if (tileSelected) {
                            animalCountLabel[selectedTileX][selectedTileY].visual = ColorVisual(0, 0, 0, 0)
                            //}
                            selectedTileX = type
                            selectedTileY = variant
                            animalCountLabel[type][variant].visual = ColorVisual(0, 255, 0)
                            playerOfSelectedTileIndex = playerIndex

                            tileSelected = playerIndex == game.activePlayerIndex
                            otherPlayersTileSelected = playerIndex != game.activePlayerIndex
                        }

                        tileSelected = true

                    }
                }
            }
        }
    }
}