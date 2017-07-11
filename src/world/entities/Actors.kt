package world.entities

interface Actor

interface Action<in T: Actor> {
    fun perform(actor: T)
}