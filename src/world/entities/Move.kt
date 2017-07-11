package world.entities

import world.Direction

data class Move(val direction: Direction) : Action<PlayerCharacter> {
    override fun perform(actor: PlayerCharacter) {
        actor.coords+=direction
    }
}