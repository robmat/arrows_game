package com.batodev.arrows.navigation.transitions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TransitionPickerTest {
    @Test
    fun `pick returns a type from the available list`() {
        val picker = TransitionPicker(random = Random(seed = 0))
        val result = picker.pick()
        assertTrue(result in NavTransitionType.entries)
    }

    @Test
    fun `pick returns different types over many calls`() {
        val picker = TransitionPicker(random = Random(seed = 42))
        val results = (1..50).map { picker.pick() }.toSet()
        assertTrue("Expected more than one unique type in 50 picks", results.size > 1)
    }

    @Test
    fun `same seed produces same sequence`() {
        val picker1 = TransitionPicker(random = Random(seed = 100))
        val picker2 = TransitionPicker(random = Random(seed = 100))
        val results1 = (1..10).map { picker1.pick() }
        val results2 = (1..10).map { picker2.pick() }
        assertEquals(results1, results2)
    }

    @Test
    fun `different seeds produce different sequences`() {
        val picker1 = TransitionPicker(random = Random(seed = 1))
        val picker2 = TransitionPicker(random = Random(seed = 2))
        val results1 = (1..20).map { picker1.pick() }
        val results2 = (1..20).map { picker2.pick() }
        assertTrue("Different seeds should produce different sequences", results1 != results2)
    }

    @Test
    fun `picks only from custom type list`() {
        val custom = listOf(NavTransitionType.FADE, NavTransitionType.SCALE_FADE)
        val picker = TransitionPicker(types = custom, random = Random(seed = 0))
        repeat(30) {
            assertTrue(picker.pick() in custom)
        }
    }

    @Test
    fun `single type list always returns that type`() {
        val picker =
            TransitionPicker(
                types = listOf(NavTransitionType.FADE),
                random = Random(seed = 0),
            )
        repeat(10) {
            assertEquals(NavTransitionType.FADE, picker.pick())
        }
    }

    @Test
    fun `all five transition types appear with enough samples`() {
        val picker = TransitionPicker(random = Random(seed = 777))
        val seen = mutableSetOf<NavTransitionType>()
        repeat(200) { seen.add(picker.pick()) }
        NavTransitionType.entries.forEach { type ->
            assertTrue("$type should appear in 200 picks", type in seen)
        }
    }

    @Test
    fun `types list is accessible and matches entries by default`() {
        val picker = TransitionPicker()
        assertEquals(NavTransitionType.entries, picker.types)
    }
}
