package com.batodev.arrows

internal object GenerateScreenLogic {
    fun resolveMaxSize(isFillBoardEnabled: Boolean): Float =
        if (isFillBoardEnabled) {
            GameConstants.GENERATOR_MAX_SIZE_FILL_BOARD
        } else {
            GameConstants.GENERATOR_MAX_SIZE
        }

    fun clampDimension(
        value: Float,
        maxSize: Float,
    ): Float = value.coerceAtMost(maxSize)

    fun buildCustomGameParams(
        width: Float,
        height: Float,
        selectedShape: String,
    ): CustomGameParams {
        val shapeName = if (selectedShape == GameConstants.SHAPE_TYPE_RECTANGULAR) null else selectedShape
        return CustomGameParams(
            isCustom = true,
            customWidth = width.toInt(),
            customHeight = height.toInt(),
            customShape = shapeName,
        )
    }

    fun buildShapeList(allShapeNames: List<String>): List<String> = listOf(GameConstants.SHAPE_TYPE_RECTANGULAR) + allShapeNames

    fun shapeFlatIndex(
        rowIndex: Int,
        indexInRow: Int,
        shapesPerRow: Int,
    ): Int = rowIndex * shapesPerRow + indexInRow

    fun shapePopInDelayMs(shapeIndex: Int): Long = shapeIndex * GameConstants.GENERATOR_SHAPE_POP_IN_STAGGER_MS
}
