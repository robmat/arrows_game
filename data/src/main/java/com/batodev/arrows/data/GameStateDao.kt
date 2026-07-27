package com.batodev.arrows.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.batodev.arrows.data.GameLevelData
import com.batodev.arrows.data.PointData
import com.batodev.arrows.data.SnakeData
import com.batodev.arrows.data.SnakeSaveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class GameStateDao {

    @Insert
    abstract suspend fun insertBoard(board: GameBoardEntity): Long

    @Insert
    abstract suspend fun insertSnake(snake: SnakeEntity): Long

    @Insert
    abstract suspend fun insertBodyPoints(points: List<SnakeBodyPointEntity>)

    @Query("SELECT * FROM game_boards WHERE stateType = :stateType LIMIT 1")
    abstract suspend fun getBoard(stateType: String): GameBoardEntity?

    @Query("SELECT * FROM snakes WHERE boardId = :boardId")
    abstract suspend fun getSnakes(boardId: Long): List<SnakeEntity>

    @Query("SELECT * FROM snake_body_points WHERE snakeRowId = :snakeRowId ORDER BY orderIndex")
    abstract suspend fun getBodyPoints(snakeRowId: Long): List<SnakeBodyPointEntity>

    @Query("DELETE FROM game_boards WHERE stateType = :stateType")
    abstract suspend fun deleteByStateType(stateType: String)

    @Query("DELETE FROM game_boards")
    abstract suspend fun deleteAllBoards()

    @Query("SELECT COUNT(*) FROM game_boards WHERE stateType = 'CURRENT'")
    abstract fun getCurrentBoardCount(): Flow<Int>

    @Transaction
    open suspend fun saveGameLevel(stateType: String, width: Int, height: Int, snakes: List<SnakeSaveData>) {
        deleteByStateType(stateType)
        val boardId = insertBoard(GameBoardEntity(stateType = stateType, width = width, height = height))
        for (snake in snakes) {
            val snakeRowId = insertSnake(
                SnakeEntity(boardId = boardId, snakeId = snake.id, headDirection = snake.headDirection)
            )
            insertBodyPoints(
                snake.bodyPoints.mapIndexed { index, point ->
                    SnakeBodyPointEntity(snakeRowId = snakeRowId, orderIndex = index, x = point.x, y = point.y)
                }
            )
        }
    }

    @Transaction
    open suspend fun loadGameLevel(stateType: String): GameLevelData? {
        val board = getBoard(stateType) ?: return null
        val snakeEntities = getSnakes(board.boardId)
        val snakes = snakeEntities.map { snakeEntity ->
            val points = getBodyPoints(snakeEntity.snakeRowId)
            SnakeData(
                id = snakeEntity.snakeId,
                headDirection = snakeEntity.headDirection,
                bodyPoints = points.map { PointData(it.x, it.y) }
            )
        }
        return GameLevelData(board.width, board.height, snakes)
    }

    @Transaction
    open suspend fun clearAllSavedLevels() {
        deleteAllBoards()
    }
}

fun GameStateDao.hasSavedLevel(): Flow<Boolean> = getCurrentBoardCount().map { it > 0 }
