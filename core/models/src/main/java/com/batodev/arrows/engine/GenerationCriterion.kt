package com.batodev.arrows.engine

data class CriterionParams(
    val body: List<Point>,
    val point: Point,
    val snakes: List<Snake>,
    val width: Int,
    val height: Int,
    val forbiddenPoints: Set<Point>,
    val occupied: Array<BooleanArray>,
)

interface Criterion {
    fun isSatisfied(params: CriterionParams): Boolean
}

class NextToExistingSnakeCriterion : Criterion {
    private val allDirections =
        listOf(
            Pair(-1, -1),
            Pair(0, -1),
            Pair(1, -1),
            Pair(-1, 0),
            Pair(1, 0),
            Pair(-1, 1),
            Pair(0, 1),
            Pair(1, 1),
        )

    override fun isSatisfied(params: CriterionParams): Boolean {
        for ((dx, dy) in allDirections) {
            val nx = params.point.x + dx
            val ny = params.point.y + dy
            if (nx in 0 until params.width && ny in 0 until params.height && params.occupied[nx][ny]) {
                return true
            }
        }
        val bodyWithoutLast = if (params.body.size > 1) params.body.dropLast(1) else emptyList()
        return bodyWithoutLast.any { segment ->
            allDirections.any { (dx, dy) ->
                params.point.x + dx == segment.x && params.point.y + dy == segment.y
            }
        }
    }
}

class AlwaysTrueCriterion : Criterion {
    override fun isSatisfied(params: CriterionParams): Boolean = true
}
