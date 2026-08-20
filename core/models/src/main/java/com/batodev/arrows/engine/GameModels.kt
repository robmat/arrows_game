package com.batodev.arrows.engine

data class Point(
    val x: Int,
    val y: Int,
) {
    operator fun plus(dir: Direction) = Point(x + dir.dx, y + dir.dy)
}

enum class Direction(
    val dx: Int,
    val dy: Int,
) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
}

data class Snake(
    val id: Int,
    val body: List<Point>, // Ordered list: Head -> ... -> Tail
    val headDirection: Direction,
)

data class GameLevel(
    val width: Int,
    val height: Int,
    val snakes: List<Snake>,
)
