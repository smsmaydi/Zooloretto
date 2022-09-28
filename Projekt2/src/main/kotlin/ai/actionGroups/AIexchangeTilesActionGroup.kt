package ai.actionGroups

import ai.actions.AIAction
import ai.actions.AIexchangeTilesBarnAction
import ai.actions.AIexchangeTilesEnclosureAction
import entity.Enclosure
import entity.Player
import service.RootService


/**
 * Wrapper for all possible actions for [AIexchangeTilesBarnAction] and [AIexchangeTilesEnclosureAction]
 */
class AIexchangeTilesActionGroup : AIActionGroup() {

    /**
     * generate actions
     */
    override fun generateCombinations(aiRootService: RootService, aiPlayer: Player): MutableList<AIAction> {
        val actions: MutableList<AIAction> = mutableListOf()

        if (aiPlayer.numCoins < 1) return actions

        val enclosures = mutableListOf<Enclosure>()
        enclosures.addAll(aiPlayer.enclosures)
        enclosures.addAll(aiPlayer.expansionBoards.filter { it.isUsed })

        val animalTypesInBarn = aiPlayer.barn.animals.map { it.type }.toMutableSet()

        if (animalTypesInBarn.isEmpty())
            return actions

        // AIexchangeTiles-BARN-Action
        for (enc in enclosures) {
            if (enc.animals.isNotEmpty()) {
                for (animalType in animalTypesInBarn) {
                    if (enc.getAnimalType() != animalType
                        && aiPlayer.barn.getNumberOfTypeInBarn(animalType) <= enc.animalsMax
                    ) {
                        actions.add(
                            AIexchangeTilesBarnAction(
                                enc,
                                animalType,
                                enclosures.indexOf(enc)
                            )
                        )
                    }
                }
            }
        }

        for (srcEnclosure in enclosures) {
            for (dstEnclosure in enclosures) {
                if (srcEnclosure !== dstEnclosure) {
                    if (srcEnclosure.animals.isNotEmpty() && dstEnclosure.animals.isNotEmpty()
                        && srcEnclosure.getAnimalType() != dstEnclosure.getAnimalType()
                        && srcEnclosure.animals.size <= dstEnclosure.animalsMax
                        && dstEnclosure.animals.size <= srcEnclosure.animalsMax
                    ) {

                        actions.add(
                            AIexchangeTilesEnclosureAction(
                                enclosures.indexOf(srcEnclosure),
                                enclosures.indexOf(dstEnclosure)
                            )
                        )
                    }
                }
            }
        }

        return actions
    }

}
