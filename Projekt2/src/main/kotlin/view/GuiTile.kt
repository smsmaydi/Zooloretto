package view

import entity.Tile
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ImageVisual

/**
 * The GuiTile
 */
class GuiTile(posX: Number, posY: Number, tile: Tile, visual: ImageVisual) :
    TokenView(height = 66, width = 66, posX = posX, posY = posY, visual = visual) {
    val tile = tile
}