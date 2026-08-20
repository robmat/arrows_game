package com.batodev.arrows.engine

import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale

class AndroidNativeImage(
    val bitmap: Bitmap,
) : NativeImage {
    override val width: Int get() = bitmap.width
    override val height: Int get() = bitmap.height

    override fun getPixel(
        x: Int,
        y: Int,
    ): Int = bitmap[x, y]

    override fun scale(
        targetWidth: Int,
        targetHeight: Int,
    ): NativeImage = AndroidNativeImage(bitmap.scale(targetWidth, targetHeight, false))

    override fun clip(
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
    ): NativeImage {
        val width = maxX - minX + 1
        val height = maxY - minY + 1
        return AndroidNativeImage(Bitmap.createBitmap(bitmap, minX, minY, width, height))
    }
}

class AndroidBoardShape(
    val bitmap: Bitmap,
) : BoardShape {
    private val processor = BoardImageProcessor()

    override fun getWalls(
        targetWidth: Int,
        targetHeight: Int,
    ): Array<BooleanArray> = processor.createWallsFromImage(AndroidNativeImage(bitmap), targetWidth, targetHeight)
}
