import asciiPanel.AsciiPanel
import graphics.AsciiTerminalDisplay
import graphics.Terminal
import input.Input
import input.MoveKeys
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import world.Coords
import world.entities.Action
import world.entities.Move
import world.entities.PlayerCharacter
import kotlin.system.exitProcess

val SCREEN_WIDTH = 80
val SCREEN_HEIGHT = 24

fun main(args: Array<String>) {

    val swingIO = AsciiTerminalDisplay(AsciiPanel(SCREEN_WIDTH, SCREEN_HEIGHT))

    val mainPanel = swingIO.terminal

    val inputChannel = swingIO.inputChannel

    val game = Game(mainPanel, inputChannel)
    game.start()
}

class Game(val mainPanel: Terminal, val inputChannel: Channel<Input>) {
    val playerIntelligenceChannel = Channel<Action<PlayerCharacter>>()
    fun start() {

        val inputReadCoroutine = launch(newSingleThreadContext("Input processing")) {
            inputChannel.consumeEach { input ->
                println("input.Input received: $input")
                when (input) {
                    Input.Exit -> exitProcess(0)
                    in MoveKeys -> playerIntelligenceChannel.send(Move(MoveKeys[input]!!))
                    else -> {
                    }
                }
            }
        }
        val player = PlayerCharacter()
        player.coords = Coords(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2)

        fun updateMapDisplay() {
            mainPanel.clear()
            mainPanel[player.coords.x, SCREEN_HEIGHT - player.coords.y] = '@'
            mainPanel.flush()
        }

        updateMapDisplay()

        val gameLoopCoroutine = launch(newSingleThreadContext("Game Loop")) {
            playerIntelligenceChannel.consumeEach { action ->
                println("Player move: $action")
                action.perform(player)
                updateMapDisplay()
            }
        }
    }
}