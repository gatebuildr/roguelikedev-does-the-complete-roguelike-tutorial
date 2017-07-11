package input

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

open class KeyListenerAdapter : KeyListener {
    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {}
    override fun keyReleased(e: KeyEvent) {}
}