package view

import entity.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private const val CARDS_FILE = "/tts/New Project.png"
private const val TRUCK_FILE = "/tts/httpsiimgurcomxup83g7jpg.jpg"
private const val UNDO_BUTTON_FILE = "/tts/httpsiimgurcomawpLtfHpng.png"
private const val BG_FILE = "/tts/httpsiimgurcomLjNeorbjpg.jpg"
private const val COVER_FILE = "/tts/1645737660.png"
private const val BOARD_FILE = "/tts/httpsiimgurcomRhLtlP9jpg.jpg"
private const val EXPANSIONBOARD_FILE_1 = "/tts/httpsiimgurcom7rWvGx1jpg.jpg"
private const val EXPANSIONBOARD_FILE_2 = "/tts/httpsiimgurcom7rWvGx12jpg.jpg"

private const val IMG_HEIGHT = 292
private const val IMG_WIDTH = 292

/**
 * provides access to the tts/New Project file that contains all the tiles images.
 * The returned [BufferedImage] objects of [frontImageFor]
 * and [backImage] are 292x292 pixels.

 */

class PileImageLoader {

    /**
     * The full raster image containing the suits as rows (plus one special row for blank/back)
     * and values as columns (starting with the ace). As the ordering does not correctly reflect
     * the order in which the suits are declared in [AnimalType], mappings via [row] and [column]
     * are required.
     */

    private val image : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(CARDS_FILE))

    val truckImage : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(TRUCK_FILE))
    val undoButtonImage : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(UNDO_BUTTON_FILE))
    val backgroundImage : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(BG_FILE))
    val coverImage : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(COVER_FILE))
    val boardImage : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(BOARD_FILE))
    val expansionBoardImage_1 : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(EXPANSIONBOARD_FILE_1))
    val expansionBoardImage_2 : BufferedImage = ImageIO.read(PileImageLoader::class.java.getResource(EXPANSIONBOARD_FILE_2))

    /**
     * Provides the card image for the given [VendingStallType]
     */
    fun frontImageFor(tile: Tile): BufferedImage {
        if (tile is AnimalTile) {
            return frontImageForAnimal(tile.type, tile.variant)
        } else if (tile is VendingStallTile) {
            return when (tile.type) {
                VendingStallType.V1 -> typeOneMarket
                VendingStallType.V2 -> typeTwoMarket
                VendingStallType.V3 -> typeThreeMarket
                VendingStallType.V4 -> typeFourMarket
            }
        }
        return coinImage
    }

    /**
     * The frontImageForAnimal fun
     */
    fun frontImageForAnimal(type: AnimalType, variant: AnimalVariant) =
        getImageByCoordinates(variant.column, type.row)

    // coin and background image of the piles
    val coinImage: BufferedImage get() = getImageByCoordinates(0, 9)
    val backImage: BufferedImage get() = getImageByCoordinates(1, 9)

    // Market types
    val typeOneMarket: BufferedImage get() = getImageByCoordinates(0, 8)
    val typeTwoMarket: BufferedImage get() = getImageByCoordinates(1, 8)
    val typeThreeMarket: BufferedImage get() = getImageByCoordinates(2, 8)
    val typeFourMarket: BufferedImage get() = getImageByCoordinates(3, 8)


    /**
     * retrieves from the full raster image [image] the corresponding sub-image
     * for the given column [x] and row [y]
     *
     * @param x column in the raster image, starting at 0
     * @param y row in the raster image, starting at 0
     *
     */


    private fun getImageByCoordinates(x: Int, y: Int): BufferedImage =
        image.getSubimage(
            x * IMG_WIDTH,
            y * IMG_HEIGHT,
            IMG_WIDTH,
            IMG_HEIGHT
        )

    /**
     * As the [CARDS_FILE] does not have the same ordering of suits
     * as they are in [AnimalType], this extension property provides
     * a corresponding mapping to be used when addressing the row.
     *
     */

    private val AnimalType.row
        get() = when (this) {
            AnimalType.FLAMINGO -> 0
            AnimalType.LEOPARD -> 1
            AnimalType.PANDA -> 2
            AnimalType.ZEBRA -> 3
            AnimalType.CAMEL -> 4
            AnimalType.ELEPHANT -> 5
            AnimalType.CHIMPANZEE -> 6
            AnimalType.KANGAROO -> 7
        }

    /**
     * As the [CARDS_FILE] does not have the same ordering of suits
     * as they are in [AnimalVariant], this extension property provides
     * a corresponding mapping to be used when addressing the row.
     *
     */
    private val AnimalVariant.column
        get() = when (this) {
            AnimalVariant.NEUTRAL -> 0
            AnimalVariant.MALE -> 1
            AnimalVariant.FEMALE -> 2
            AnimalVariant.CHILD -> 3
        }
}
