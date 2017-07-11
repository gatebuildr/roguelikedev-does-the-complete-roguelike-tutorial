import asciiPanel.AsciiPanel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.swing.Swing
import java.awt.Component
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.JFrame
import kotlin.system.exitProcess

/**
 * Created by Matt on 7/8/2017.
 */

open class KeyListenerAdapter : KeyListener {
    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {}
    override fun keyReleased(e: KeyEvent) {}
}

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

enum class Direction(val dx:Int, val dy:Int): ICoords by Coords(dx,dy) {
    North(0,1),
    NorthEast(1,1),
    East(1,0),
    SouthEast(1,-1),
    South(0,-1),
    SouthWest(-1,-1),
    West(-1,0),
    NorthWest(-1,1)
}

val MoveKeys = mapOf(
        Input.Left to Direction.West,
        Input.Up to Direction.North,
        Input.Right to Direction.East,
        Input.Down to Direction.South)

interface ICoords{
    val x:Int
    val y:Int
    operator fun plus(other:ICoords) = Coords(x+other.x,y+other.y)
}

data class Coords(override val x: Int, override val y: Int): ICoords

interface Actor

interface Action<T: Actor> {
    fun perform(actor: T)
}

data class Move(val direction: Direction) : Action<Player> {
    override fun perform(actor: Player) {
        actor.coords+=direction
    }
}

val SCREEN_WIDTH = 80
val SCREEN_HEIGHT = 24

class Player : Actor {
    var coords = Coords(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2)
}

interface Terminal {
    val width:Int
    val height:Int
    operator fun set(x:Int, y:Int, char: Char)
    operator fun set(x0:Int, y0:Int, text: String) {
        var x = x0
        var y = y0
        for(c in text) {
            this[x,y]=c
            x++
            if(x >= width) {
                x = 0
                y++
            }
        }
    }
    fun clear()
    fun flush()
}

class AsciiTerminal(val asciiPanel: AsciiPanel): Terminal {
    override fun flush() {
        asciiPanel.repaint()
    }

    override val width: Int
        get() = asciiPanel.charWidth

    override val height: Int
        get() = asciiPanel.charHeight

    override fun set(x: Int, y: Int, char: Char) {
        asciiPanel.write(char, x, y)
    }

    override fun clear() {
        asciiPanel.clear()
    }
}

interface InputSource {
    val inputChannel: Channel<Input>
}

interface GameDisplay {
    val terminal:Terminal
}

class AsciiTerminalDisplay(val asciiPanel: AsciiPanel): GameDisplay, InputSource {
    override val inputChannel = Channel<Input>()
    override val terminal = AsciiTerminal(asciiPanel)

    val frame = JFrame("Kotlin Roguelike Tutorial").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        addKeyListener(object : KeyListenerAdapter() {
            override fun keyPressed(e: KeyEvent) {
                println("Key pressed: ${e.keyCode}")
                val input = when (e.keyCode) {
                    KeyEvent.VK_LEFT -> Input.Left
                    KeyEvent.VK_RIGHT -> Input.Right
                    KeyEvent.VK_UP -> Input.Up
                    KeyEvent.VK_DOWN -> Input.Down
                    KeyEvent.VK_ESCAPE -> Input.Exit
                    else -> null
                }
                if (input != null) {
                    launch(Swing) {
                        inputChannel.send(input)
                    }
                }
            }
        })

        add(asciiPanel)
        pack()
        isVisible = true
    }
}

fun main(args: Array<String>) {

    val swingIO = AsciiTerminalDisplay(AsciiPanel(SCREEN_WIDTH, SCREEN_HEIGHT))

    val mainPanel = swingIO.terminal

    val inputChannel = swingIO.inputChannel

    val game = Game(mainPanel, inputChannel)
    game.start()

    game(mainPanel, inputChannel)
}

class Game(val mainPanel: Terminal, val inputChannel: Channel<Input>) {
    val playerIntelligenceChannel = Channel<Action<Player>>()
    fun start() {

        val inputReadCoroutine = launch(newSingleThreadContext("Input processing")) {
            inputChannel.consumeEach { input ->
                println("Input received: $input")
                when (input) {
                    Input.Exit -> exitProcess(0)
                    in MoveKeys -> playerIntelligenceChannel.send(Move(MoveKeys[input]!!))
                    else -> {
                    }
                }
                //pretend input takes a while to process
                delay(500, TimeUnit.MILLISECONDS)
            }
        }
        val player = Player()

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

private fun game(mainPanel: Terminal, inputChannel: Channel<Input>) {

}