package com.batodev.arrows.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.batodev.arrows.engine.AndroidBoardShape
import com.batodev.arrows.engine.BoardShape
import com.batodev.arrows.engine.BoardShapeProvider

class AndroidResourceBoardShapeProvider(
    private val context: Context,
) : BoardShapeProvider {
    override fun getRandomShape(): BoardShape? {
        val resId = ShapeRegistry.shapes.values.random()
        return decodeResource(resId)?.let { AndroidBoardShape(it) }
    }

    override fun getAllShapeNames(): List<String> =
        ShapeRegistry.shapes.keys
            .toList()
            .sorted()

    override fun getShapeByName(name: String): BoardShape? {
        val resId = ShapeRegistry.shapes[name] ?: return null
        return decodeResource(resId)?.let { AndroidBoardShape(it) }
    }

    private fun decodeResource(resId: Int): Bitmap? =
        BitmapFactory.decodeResource(
            context.resources,
            resId,
            BitmapFactory.Options().apply {
                inScaled = false
            },
        )
}
