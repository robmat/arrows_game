package com.batodev.arrows.data

import com.batodev.arrows.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapeRegistryTest {
    @Test
    fun `registry contains nineteen shapes`() {
        assertEquals(19, ShapeRegistry.shapes.size)
    }

    @Test
    fun `registry does not contain rectangular - handled separately in UI`() {
        assertNull(ShapeRegistry.shapes[GameConstants.SHAPE_TYPE_RECTANGULAR])
    }

    @Test
    fun `all resource IDs are positive`() {
        ShapeRegistry.shapes.forEach { (name, resId) ->
            assertTrue("$name has non-positive resource id", resId > 0)
        }
    }

    @Test
    fun `all shape names are non-blank`() {
        ShapeRegistry.shapes.keys.forEach { name ->
            assertTrue("shape name must not be blank", name.isNotBlank())
        }
    }

    @Test
    fun `registry has no duplicate resource IDs`() {
        val resIds = ShapeRegistry.shapes.values.toList()
        assertEquals("resource IDs must be unique", resIds.size, resIds.toSet().size)
    }

    @Test
    fun `expected shape names are present`() {
        val expected =
            listOf(
                "bolt",
                "brick",
                "build",
                "cannabis",
                "chess_queen",
                "chess_rook",
                "delete",
                "disabled",
                "favorite",
                "home",
                "humerus",
                "key",
                "star_kid",
                "mood_bad",
                "satisfied",
                "settings",
                "star",
                "tibia",
                "water_bottle",
            )
        expected.forEach { name ->
            assertNotNull("$name must be in ShapeRegistry", ShapeRegistry.shapes[name])
        }
    }

    @Test
    fun `shape names do not contain whitespace`() {
        ShapeRegistry.shapes.keys.forEach { name ->
            assertTrue(
                "'$name' must not contain whitespace — used as nav param",
                name.none { it.isWhitespace() },
            )
        }
    }
}
