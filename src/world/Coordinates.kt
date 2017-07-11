package world

interface ICoords{
    val x:Int
    val y:Int
    operator fun plus(other: ICoords) = Coords(x + other.x, y + other.y)
}

data class Coords(override val x: Int, override val y: Int): ICoords

enum class Direction(val dx:Int, val dy:Int): ICoords by Coords(dx, dy) {
    North(0,1),
    NorthEast(1,1),
    East(1,0),
    SouthEast(1,-1),
    South(0,-1),
    SouthWest(-1,-1),
    West(-1,0),
    NorthWest(-1,1)
}