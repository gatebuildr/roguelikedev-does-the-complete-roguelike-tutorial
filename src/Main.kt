import asciiPanel.AsciiPanel
import javax.swing.JFrame

/**
 * Created by Matt on 7/8/2017.
 */

fun main(args: Array<String>) {
    val asciiPanel = AsciiPanel(80, 24)

    val frame = JFrame("Kotlin Roguelike Tutorial")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    frame.add(asciiPanel)

    frame.pack()
    frame.isVisible = true

    println("main")

    asciiPanel.write("hello world")
}