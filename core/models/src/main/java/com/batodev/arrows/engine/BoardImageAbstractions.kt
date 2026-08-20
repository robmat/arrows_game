package com.batodev.arrows.engine

interface NativeImage {
    val width: Int
    val height: Int

    fun getPixel(
        x: Int,
        y: Int,
    ): Int

    fun scale(
        targetWidth: Int,
        targetHeight: Int,
    ): NativeImage

    fun clip(
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
    ): NativeImage
}

interface BoardShape {
    fun getWalls(
        targetWidth: Int,
        targetHeight: Int,
    ): Array<BooleanArray>
}
