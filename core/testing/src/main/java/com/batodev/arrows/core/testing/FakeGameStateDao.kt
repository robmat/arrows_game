package com.batodev.arrows.core.testing

import com.batodev.arrows.data.GameBoardEntity
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.SnakeBodyPointEntity
import com.batodev.arrows.data.SnakeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Shared test double for [GameStateDao].
 * Backed by in-memory maps — no database required in unit tests.
 */
class FakeGameStateDao : GameStateDao() {
    private val boards = mutableMapOf<Long, GameBoardEntity>()
    private val snakes = mutableMapOf<Long, SnakeEntity>()
    private val points = mutableMapOf<Long, SnakeBodyPointEntity>()
    private var nextBoardId = 1L
    private var nextSnakeId = 1L
    private var nextPointId = 1L
    private val boardCountFlow = MutableStateFlow(0)

    private fun updateBoardCount() {
        boardCountFlow.value = boards.values.count { it.stateType == "CURRENT" }
    }

    override suspend fun insertBoard(board: GameBoardEntity): Long {
        val id = nextBoardId++
        boards[id] = board.copy(boardId = id)
        updateBoardCount()
        return id
    }

    override suspend fun insertSnake(snake: SnakeEntity): Long {
        val id = nextSnakeId++
        snakes[id] = snake.copy(snakeRowId = id)
        return id
    }

    override suspend fun insertBodyPoints(points: List<SnakeBodyPointEntity>) {
        points.forEach {
            val id = nextPointId++
            this.points[id] = it.copy(pointId = id)
        }
    }

    override suspend fun getBoard(stateType: String): GameBoardEntity? = boards.values.find { it.stateType == stateType }

    override suspend fun getSnakes(boardId: Long): List<SnakeEntity> = snakes.values.filter { it.boardId == boardId }

    override suspend fun getBodyPoints(snakeRowId: Long): List<SnakeBodyPointEntity> =
        points.values.filter { it.snakeRowId == snakeRowId }.sortedBy { it.orderIndex }

    override suspend fun deleteByStateType(stateType: String) {
        val boardIds =
            boards.values
                .filter { it.stateType == stateType }
                .map { it.boardId }
                .toSet()
        val snakeIds =
            snakes.values
                .filter { it.boardId in boardIds }
                .map { it.snakeRowId }
                .toSet()
        points.entries.removeAll { it.value.snakeRowId in snakeIds }
        snakes.entries.removeAll { it.value.boardId in boardIds }
        boards.entries.removeAll { it.value.stateType == stateType }
        updateBoardCount()
    }

    override suspend fun deleteAllBoards() {
        boards.clear()
        snakes.clear()
        points.clear()
        updateBoardCount()
    }

    override fun getCurrentBoardCount(): Flow<Int> = boardCountFlow
}
