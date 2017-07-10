import asciiPanel.AsciiPanel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.swing.Swing
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.concurrent.TimeUnit
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

fun main(args: Array<String>) {

    val asciiPanel = AsciiPanel(SCREEN_WIDTH, SCREEN_HEIGHT)

    val frame = JFrame("Kotlin Roguelike Tutorial")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    frame.add(asciiPanel)

    val inputChannel = Channel<Input>()

    frame.addKeyListener(object : KeyListenerAdapter() {
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

    val playerIntelligenceChannel = Channel<Action<Player>>()

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

    fun refreshScreen() {
        asciiPanel.clear()
        asciiPanel.write('@', player.coords.x, SCREEN_HEIGHT-player.coords.y)
        asciiPanel.repaint()
    }

    refreshScreen()

    val gameLoopCoroutine = launch(newSingleThreadContext("Game Loop")) {
        playerIntelligenceChannel.consumeEach { action ->
            println("Player move: $action")
            action.perform(player)
            refreshScreen()
        }
    }

    frame.pack()
    frame.isVisible = true
}