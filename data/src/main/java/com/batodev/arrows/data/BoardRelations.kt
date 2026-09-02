package com.batodev.arrows.data

import androidx.room.Embedded
import androidx.room.Relation

/** Room relation POJOs for [GameStateDao.getBoardWithSnakes] - lets one `@Transaction @Query`
 * fetch a board's snakes and each snake's body points in a single DAO method, instead of
 * three separate abstract query methods (kept GameStateDao's declared-function count under
 * detekt's TooManyFunctions threshold without splitting the DAO). */
data class SnakeWithPoints(
    @Embedded val snake: SnakeEntity,
    @Relation(parentColumn = "snakeRowId", entityColumn = "snakeRowId")
    val points: List<SnakeBodyPointEntity>,
)

data class BoardWithSnakes(
    @Embedded val board: GameBoardEntity,
    @Relation(entity = SnakeEntity::class, parentColumn = "boardId", entityColumn = "boardId")
    val snakes: List<SnakeWithPoints>,
)
