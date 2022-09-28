package entity

import kotlinx.serialization.Serializable

/**
 * The AI difficulty
 *
 */
@Serializable
enum class AIDifficulty {
    /** Difficulty easy */
    EASY,

    /** Difficulty normal */
    NORMAL,

    /** Difficulty hard */
    HARD,
    ;

    override fun toString() =
        when (this) {
            EASY -> "EASY"
            NORMAL -> "NORMAL"
            HARD -> "HARD"
        }
}
