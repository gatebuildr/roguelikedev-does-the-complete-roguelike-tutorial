package input

/**
 * Enum of all the classified inputs that the game reacts to. Every user input in every mode (except plain-text input)
 * should be accounted for here. This should allow for changeable key mappings later.
 */
enum class Input {
    Left,
    Right,
    Up,
    Down,
    Exit
}