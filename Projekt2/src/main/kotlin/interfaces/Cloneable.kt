/**
 * - The Cloneable interface represents an entity that can be cloned.
 * - We use this with the entity model objects to clone them.
 * - The implementation should make sure that cloning is deep and not shallow (which means that if there was a list
 * for e.g then every object of that list should be cloned as well)
 */
interface Cloneable<T> {
    /** Return a clone (deep copy) */
    fun clone(): T
}

