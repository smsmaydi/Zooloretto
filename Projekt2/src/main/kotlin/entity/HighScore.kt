package entity

import kotlinx.serialization.Serializable

/**
 * The high score list
 */
@Serializable
class HighScore {
    /** The scores of the high score list */
    var scores: MutableList<HighScoreRecord> = mutableListOf()

    /**
     * - sorts the score records and keeps only the first 10 records with the highest player points
     */
    fun sortAndKeepFirst10() {
        // limit the high score list to the highest 10 result
        this.scores.sortByDescending { record -> record.playerPoints }
        if (this.scores.size > 10) {
            this.scores = this.scores.subList(0, 10)
        }
    }
}
