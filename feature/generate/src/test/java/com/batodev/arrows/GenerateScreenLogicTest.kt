package com.batodev.arrows

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateScreenLogicTest {
    // resolveMaxSize

    @Test
    fun `resolveMaxSize returns fill board max when fill enabled`() {
        assertEquals(GameConstants.GENERATOR_MAX_SIZE_FILL_BOARD, GenerateScreenLogic.resolveMaxSize(true))
    }

    @Test
    fun `resolveMaxSize returns regular max when fill disabled`() {
        assertEquals(GameConstants.GENERATOR_MAX_SIZE, GenerateScreenLogic.resolveMaxSize(false))
    }

    @Test
    fun `resolveMaxSize fill board max is smaller than regular max`() {
        assertTrue(
            GenerateScreenLogic.resolveMaxSize(true) < GenerateScreenLogic.resolveMaxSize(false),
        )
    }

    // clampDimension

    @Test
    fun `clampDimension clamps value exceeding max`() {
        assertEquals(50f, GenerateScreenLogic.clampDimension(60f, 50f))
    }

    @Test
    fun `clampDimension keeps value below max unchanged`() {
        assertEquals(40f, GenerateScreenLogic.clampDimension(40f, 50f))
    }

    @Test
    fun `clampDimension keeps value equal to max unchanged`() {
        assertEquals(50f, GenerateScreenLogic.clampDimension(50f, 50f))
    }

    // buildCustomGameParams

    @Test
    fun `buildCustomGameParams maps rectangular shape to null`() {
        val params = GenerateScreenLogic.buildCustomGameParams(30f, 25f, GameConstants.SHAPE_TYPE_RECTANGULAR)
        assertNull(params.customShape)
    }

    @Test
    fun `buildCustomGameParams sets isCustom true`() {
        val params = GenerateScreenLogic.buildCustomGameParams(30f, 25f, GameConstants.SHAPE_TYPE_RECTANGULAR)
        assertTrue(params.isCustom)
    }

    @Test
    fun `buildCustomGameParams converts float width to int`() {
        val params = GenerateScreenLogic.buildCustomGameParams(30.9f, 25f, GameConstants.SHAPE_TYPE_RECTANGULAR)
        assertEquals(30, params.customWidth)
    }

    @Test
    fun `buildCustomGameParams converts float height to int`() {
        val params = GenerateScreenLogic.buildCustomGameParams(30f, 25.7f, GameConstants.SHAPE_TYPE_RECTANGULAR)
        assertEquals(25, params.customHeight)
    }

    @Test
    fun `buildCustomGameParams preserves non-rectangular shape name`() {
        val params = GenerateScreenLogic.buildCustomGameParams(30f, 25f, "star")
        assertEquals("star", params.customShape)
    }

    @Test
    fun `buildCustomGameParams preserves any named shape`() {
        val params = GenerateScreenLogic.buildCustomGameParams(50f, 40f, "bolt")
        assertEquals("bolt", params.customShape)
        assertFalse(params.isCustom.not())
    }

    // buildShapeList

    @Test
    fun `buildShapeList prepends rectangular to shape names`() {
        val result = GenerateScreenLogic.buildShapeList(listOf("star", "bolt"))
        assertEquals(GameConstants.SHAPE_TYPE_RECTANGULAR, result.first())
    }

    @Test
    fun `buildShapeList includes all provided shape names`() {
        val names = listOf("star", "bolt", "key")
        val result = GenerateScreenLogic.buildShapeList(names)
        assertTrue(result.containsAll(names))
    }

    @Test
    fun `buildShapeList size is input size plus one`() {
        val names = listOf("star", "bolt")
        assertEquals(names.size + 1, GenerateScreenLogic.buildShapeList(names).size)
    }

    @Test
    fun `buildShapeList with empty input returns only rectangular`() {
        val result = GenerateScreenLogic.buildShapeList(emptyList())
        assertEquals(listOf(GameConstants.SHAPE_TYPE_RECTANGULAR), result)
    }

    // shapeFlatIndex

    @Test
    fun `shapeFlatIndex first row first item is zero`() {
        assertEquals(0, GenerateScreenLogic.shapeFlatIndex(rowIndex = 0, indexInRow = 0, shapesPerRow = 4))
    }

    @Test
    fun `shapeFlatIndex first row last item equals shapesPerRow minus one`() {
        assertEquals(3, GenerateScreenLogic.shapeFlatIndex(rowIndex = 0, indexInRow = 3, shapesPerRow = 4))
    }

    @Test
    fun `shapeFlatIndex second row first item equals shapesPerRow`() {
        assertEquals(4, GenerateScreenLogic.shapeFlatIndex(rowIndex = 1, indexInRow = 0, shapesPerRow = 4))
    }

    @Test
    fun `shapeFlatIndex increases linearly across rows`() {
        val row2col1 = GenerateScreenLogic.shapeFlatIndex(rowIndex = 2, indexInRow = 1, shapesPerRow = 4)
        assertEquals(9, row2col1)
    }

    @Test
    fun `shapeFlatIndex respects custom shapesPerRow`() {
        assertEquals(5, GenerateScreenLogic.shapeFlatIndex(rowIndex = 1, indexInRow = 2, shapesPerRow = 3))
    }

    // shapePopInDelayMs

    @Test
    fun `shapePopInDelayMs returns zero for first shape`() {
        assertEquals(0L, GenerateScreenLogic.shapePopInDelayMs(0))
    }

    @Test
    fun `shapePopInDelayMs increases by stagger constant per shape`() {
        val stagger = GameConstants.GENERATOR_SHAPE_POP_IN_STAGGER_MS
        assertEquals(stagger, GenerateScreenLogic.shapePopInDelayMs(1))
        assertEquals(stagger * 2, GenerateScreenLogic.shapePopInDelayMs(2))
    }

    @Test
    fun `shapePopInDelayMs scales linearly with index`() {
        val index = 5
        val expected = index * GameConstants.GENERATOR_SHAPE_POP_IN_STAGGER_MS
        assertEquals(expected, GenerateScreenLogic.shapePopInDelayMs(index))
    }
}
