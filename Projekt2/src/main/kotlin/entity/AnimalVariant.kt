package entity

import kotlinx.serialization.Serializable

/**
 * The animal variant
 *
 */
@Serializable
enum class AnimalVariant {
    /** */
    MALE,

    /** */
    FEMALE,

    /** */
    CHILD,

    /** */
    NEUTRAL,
    ;

    override fun toString() =
        when (this) {
            MALE -> "MALE"
            FEMALE -> "FEMALE"
            CHILD -> "CHILD"
            NEUTRAL -> "NEUTRAL"
        }
}
