package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants

class BoardImageProcessor {
    fun createWallsFromImage(
        original: NativeImage,
        targetWidth: Int,
        targetHeight: Int,
    ): Array<BooleanArray> {
        val clipped = clipToBlackContent(original)
        val scaled = clipped.scale(targetWidth, targetHeight)
        val walls = Array(targetWidth) { BooleanArray(targetHeight) }

        for (x in 0 until targetWidth) {
            for (y in 0 until targetHeight) {
                val pixel = scaled.getPixel(x, y)
                walls[x][y] = !isBlackPixel(pixel)
            }
        }
        return walls
    }

    private fun clipToBlackContent(image: NativeImage): NativeImage {
        var minX = image.width
        var minY = image.height
        var maxX = 0
        var maxY = 0
        var found = false

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                if (isBlackPixel(image.getPixel(x, y))) {
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                    found = true
                }
            }
        }

        if (!found) return image

        return image.clip(minX, minY, maxX, maxY)
    }

    private fun isBlackPixel(pixel: Int): Boolean {
        val alpha = (pixel shr GameConstants.ALPHA_SHIFT) and GameConstants.COLOR_MASK
        val red = (pixel shr GameConstants.RED_SHIFT) and GameConstants.COLOR_MASK
        val green = (pixel shr GameConstants.GREEN_SHIFT) and GameConstants.COLOR_MASK
        val blue = pixel and GameConstants.COLOR_MASK

        return alpha > GameConstants.COLOR_THRESHOLD &&
            red < GameConstants.COLOR_THRESHOLD &&
            green < GameConstants.COLOR_THRESHOLD &&
            blue < GameConstants.COLOR_THRESHOLD
    }
}
