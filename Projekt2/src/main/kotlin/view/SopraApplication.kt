package view

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import kotlin.system.exitProcess

/**
 * The SopraApplication
 */
class SopraApplication : BoardGameApplication("SoPra Game"), Refreshable {

    val rootService = RootService()
    val barnScene = BarnMenuScene(rootService)
    val startMenuScene = StartMenuScene()
    val newGameMenuScene = NewGameMenuScene(rootService)
    val loadGameMenuScene = LoadGameMenuScene(rootService)
    val highscoreMenuScene = HighscoreMenuScene(rootService)
    val gameScene = ZoolorettoGameScene(rootService, barnScene)
    val endGameMenuScene = EndGameMenuScene(rootService)
    val gameOptionMenuScene = GameOptionMenu(rootService)
    val importDrawPileScene = LoadDrawPileMenuScene(rootService)

    init {

        initiateButtonlogic()

        rootService.addRefreshables(
            this,
            newGameMenuScene,
            loadGameMenuScene,
            gameScene,
            endGameMenuScene,
            gameOptionMenuScene,
            highscoreMenuScene
        )
        this.showMenuScene(loadGameMenuScene)

    }

    private fun initiateButtonlogic() {
        ///////////////Buttons in startMenuScene ///////////////
        startMenuScene.newGameButton.apply {
            onMouseClicked = {
                showMenuScene(newGameMenuScene)
            }
        }
        startMenuScene.loadGameButton.apply {
            onMouseClicked = {
                showMenuScene(loadGameMenuScene)
            }
        }
        startMenuScene.highscoreButton.apply {
            onMouseClicked = {
                showMenuScene(highscoreMenuScene)
            }
        }

        ////////////Buttons in newGameMenuScene //////////////
        newGameMenuScene.backButton.apply {
            onMouseClicked = {
                showMenuScene(startMenuScene)
            }
        }
        newGameMenuScene.importDrawpileButton.apply {
            onMouseClicked = {
                showMenuScene(importDrawPileScene)
            }
        }

        ////////////Buttons in highscoreMenuScene ///////////
        highscoreMenuScene.backButton.apply {
            onMouseClicked = {
                showMenuScene(startMenuScene)
            }
        }

        ///////////Buttons in loadGameMenuScene /////////////
        loadGameMenuScene.backButton.apply {
            onMouseClicked = {
                showMenuScene(startMenuScene)
            }
        }
        loadGameMenuScene.loadButton.apply {
            onMouseClicked = {
                loadGameMenuScene.startGame()
                showGameScene(gameScene)
                hideMenuScene()
            }
        }

        gameScene.barnHole.apply {
            onMouseClicked = {
                showMenuScene(barnScene)
            }
        }
        gameScene.menuButton.apply {
            onMouseClicked = {
                showMenuScene(gameOptionMenuScene)
            }
        }
        ////////////Buttons in gameOptioneMenuScene
        gameOptionMenuScene.saveAndQuitButton.apply {
            onMouseClicked = {
                val game = rootService.game
                checkNotNull(game) { "game is null" }
                try {
                    rootService.gameLoaderService?.save(game)
                    exitProcess(0)
                } catch (_: Exception) {
                    exitProcess(180)
                }
            }
        }
        gameOptionMenuScene.resumeButton.apply {
            onMouseClicked = {
                hideMenuScene()
                showGameScene(gameScene)
            }
        }

        gameOptionMenuScene.quitWithoutSavingButton.apply {
            onMouseClicked = {
                exitProcess(0)
            }
        }


        gameOptionMenuScene.startANewGameButton.apply {
            onMouseClicked = {
                showMenuScene(startMenuScene)
            }
        }

        ////////////Buttons in barnScene////////////////
        barnScene.backButton.apply {
            onMouseClicked = {
                gameScene.setIsDisabledForDiscardButton(false)
                gameScene.setIsDisabledForMoveButton(false)
                gameScene.setIsDisabledForPurchaseButton(false)
                gameScene.setIsDisabledForApplyExchangeButton(false)
                hideMenuScene()
            }
        }

        ////////////Buttons in LoadDrawPileMenuScene
        importDrawPileScene.backButton.apply {
            onMouseClicked = { this@SopraApplication.showMenuScene(newGameMenuScene) }
        }
        importDrawPileScene.importDrawPileButton.apply {
            // If the import draw pile button is pressed in the load draw pile menu scene
            // and a config file is selected then switch back to the new game menu scene
            // and update the imported draw pile
            // TODO add the option to remove the selected draw pile again
            onMouseClicked = {
                if (importDrawPileScene.drawPileTable.selectedIndices.size > 0) {
                    newGameMenuScene.importedDrawpile =
                        importDrawPileScene.drawPileTable.selectedItems[0].configParseResult

                    showMenuScene(newGameMenuScene)
                }
            }
        }


        //TODO Implement Buttonlogic for EndGameMenu and GameOptionMenu when merged
        //////////Buttons in EndGameMenu/////////////////////////////
        endGameMenuScene.playAgainButton.apply {
            onMouseClicked = {
                showMenuScene(startMenuScene)
            }
        }
        endGameMenuScene.quitButton.apply {
            onMouseClicked = { exitProcess(0) }
        }

        //TODO Implement Buttonlogic for GameOptionMenu when merged
    }

    override fun refreshAfterGameStart() {
        this.hideMenuScene()
    }

    override fun refreshAfterGameEnd() {
        this.showMenuScene(endGameMenuScene)
    }
}

