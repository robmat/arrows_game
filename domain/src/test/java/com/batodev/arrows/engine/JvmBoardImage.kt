package com.batodev.arrows.engine

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class JvmNativeImage(
    val image: BufferedImage,
) : NativeImage {
    override val width: Int get() = image.width
    override val height: Int get() = image.height

    override fun getPixel(
        x: Int,
        y: Int,
    ): Int = image.getRGB(x, y)

    override fun scale(
        targetWidth: Int,
        targetHeight: Int,
    ): NativeImage {
        val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g = scaled.createGraphics()
        g.drawImage(image, 0, 0, targetWidth, targetHeight, null)
        g.dispose()
        return JvmNativeImage(scaled)
    }

    override fun clip(
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
    ): NativeImage {
        val width = maxX - minX + 1
        val height = maxY - minY + 1
        return JvmNativeImage(image.getSubimage(minX, minY, width, height))
    }
}

class JvmBoardShape(
    val image: BufferedImage,
) : BoardShape {
    private val processor = BoardImageProcessor()

    override fun getWalls(
        targetWidth: Int,
        targetHeight: Int,
    ): Array<BooleanArray> = processor.createWallsFromImage(JvmNativeImage(image), targetWidth, targetHeight)

    companion object {
        fun fromFile(path: String): JvmBoardShape {
            val file = File(path)
            val image = ImageIO.read(file) ?: throw IllegalArgumentException("Could not read image: $path")
            return JvmBoardShape(image)
        }
    }
}
