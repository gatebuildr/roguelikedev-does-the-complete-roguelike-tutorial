package graphics

import asciiPanel.AsciiPanel

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