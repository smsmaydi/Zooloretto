package entity

import Cloneable
import kotlinx.serialization.Serializable

/**
 * The player AI
 */
@Serializable
class PlayerAI(
    /** The difficulty of the AI  */
    val difficulty: AIDifficulty
) : Player(), Cloneable<Player> {


    /**
     * A factory for the player AI
     */
    companion object Factory {

        /**
         * - creates a PlayerAI based on the game rules
         * - the game rules differ based on the number of players
         * - name is the name of the player
         * - numPlayersInGame is the number of players in the game where the player is used
         */
        fun createPlayer(name: String, numPlayersInGame: Int, difficulty: AIDifficulty): PlayerAI {
            val player = PlayerAI(difficulty)
            player.name = name

            // Add the expansion boards
            player.expansionBoards.add(ExpansionBoard())
            if (numPlayersInGame == 2) {
                player.expansionBoards.add(ExpansionBoard())
            }

            // Add the enclosures
            player.enclosures.add(Enclosure.createBasedOnNumAnimals(4))
            player.enclosures.add(Enclosure.createBasedOnNumAnimals(5))
            player.enclosures.add(Enclosure.createBasedOnNumAnimals(6))

            return player
        }
    }

    override fun clone(): PlayerAI {
        val newPlayer = PlayerAI(this.difficulty)
        newPlayer.name = this.name
        newPlayer.receivedHint = this.receivedHint
        newPlayer.tookDeliveryTruck = this.tookDeliveryTruck
        newPlayer.numCoins = this.numCoins
        newPlayer.enclosures = this.enclosures.map { it.clone() }.toMutableList()
        newPlayer.expansionBoards = this.expansionBoards.map { it.clone() }.toMutableList()
        newPlayer.deliveryTruck = this.deliveryTruck?.clone()
        newPlayer.barn = this.barn.clone()
        return newPlayer
    }
}
