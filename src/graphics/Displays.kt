package graphics

interface GameDisplay {
    val terminal: Terminal
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