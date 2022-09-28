package entity

import kotlinx.serialization.Serializable

/**
 * The vending stall type
 *
 */
@Serializable
enum class VendingStallType {
    /** */
    V1,

    /** */
    V2,

    /** */
    V3,

    /** */
    V4,
    ;

    override fun toString() =
        when (this) {
            V1 -> "V1"
            V2 -> "V2"
            V3 -> "V3"
            V4 -> "V4"
        }
}
