package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AImoveAnimalTileFromBarnToEnclosureAction
import entity.Enclosure
import entity.Player
import service.RootService

/**
 * Wrapper for all possible actions for [AImoveAnimalTileFromBarnToEnclosureAction]
 */
class AImoveAnimalTileFromBarnToEnclosureActionGroup : AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()

        // Cannot move anything if the player has not enough coins
        if (aiPlayer.numCoins < 1) return actions

        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(aiPlayer.enclosures)
        enclosures.addAll(aiPlayer.expansionBoards)

        val animalTypesInBarn = aiPlayer.barn.animals

        for (animalTile in animalTypesInBarn) {
            for (dstEnclosure in enclosures) {
                if (dstEnclosure.hasSpaceForAnimal() && dstEnclosure.animals.isNotEmpty()
                    && dstEnclosure.getAnimalType() == animalTile.type
                )
                    actions.add(AImoveAnimalTileFromBarnToEnclosureAction(animalTile, enclosures.indexOf(dstEnclosure)))
            }
        }

        return actions
    }
}
