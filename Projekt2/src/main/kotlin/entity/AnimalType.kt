package entity

import kotlinx.serialization.Serializable

/**
 * The animal type
 *
 */
@Serializable
enum class AnimalType {
    /** */
    FLAMINGO,

    /** */
    CAMEL,

    /** */
    LEOPARD,

    /** */
    ELEPHANT,

    /** */
    PANDA,

    /** */
    CHIMPANZEE,

    /** */
    ZEBRA,

    /** */
    KANGAROO,
    ;

    override fun toString() =
        when (this) {
            FLAMINGO -> "FLAMINGO"
            CAMEL -> "CAMEL"
            LEOPARD -> "LEOPARD"
            ELEPHANT -> "ELEPHANT"
            PANDA -> "PANDA"
            CHIMPANZEE -> "CHIMPANZEE"
            ZEBRA -> "ZEBRA"
            KANGAROO -> "KANGAROO"
        }
}
