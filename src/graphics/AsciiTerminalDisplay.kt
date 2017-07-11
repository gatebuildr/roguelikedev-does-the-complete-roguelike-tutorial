package graphics

import asciiPanel.AsciiPanel
import input.Input
import input.InputSource
import input.KeyListenerAdapter
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.swing.Swing
import java.awt.event.KeyEvent
import javax.swing.JFrame

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