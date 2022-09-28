package entity

import kotlinx.serialization.Serializable

/**
 * The game state
 *
 */
@Serializable
enum class GameState {
    /** Is the game running */
    ACTIVE,

    /** Is the game over */
    FINISHED,

    /** Is the game paused */
    PAUSED,
    ;

    override fun toString() =
        when (this) {
            ACTIVE -> "ACTIVE"
            FINISHED -> "FINISHED"
            PAUSED -> "PAUSED"
        }
}
