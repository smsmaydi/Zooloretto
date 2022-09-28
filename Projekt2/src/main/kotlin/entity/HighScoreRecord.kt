package entity

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * A high score record is a record that gets created on game end.
 */
@Serializable
class HighScoreRecord(
    /**  */
    val playerName: String,
    /**  */
    val playerPoints: Int,
    private val timeStampOfGameEnd: Long
) {

    /**
     * - returns the date when the high score record was generated
     */
    fun getDateAsString(): String {
        val simpleDate = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return simpleDate.format(Date(timeStampOfGameEnd))
    }
}
