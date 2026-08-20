package com.batodev.arrows.navigation

import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BackStackNavigationTest {
    private lateinit var backStack: BackStack<NavTarget>

    @Before
    fun setUp() {
        backStack =
            BackStack(
                initialElement = NavTarget.Home,
                savedStateMap = null,
            )
    }

    @Test
    fun `initial active element is home`() {
        assertEquals(NavTarget.Home, backStack.activeElement)
    }

    @Test
    fun `push settings makes settings active`() {
        backStack.push(NavTarget.Settings)
        assertEquals(NavTarget.Settings, backStack.activeElement)
    }

    @Test
    fun `push generate makes generate active`() {
        backStack.push(NavTarget.Generate)
        assertEquals(NavTarget.Generate, backStack.activeElement)
    }

    @Test
    fun `pop after push restores home`() {
        backStack.push(NavTarget.Settings)
        backStack.pop()
        assertEquals(NavTarget.Home, backStack.activeElement)
    }

    @Test
    fun `replace swaps active element without growing history`() {
        backStack.push(NavTarget.Generate)
        backStack.replace(NavTarget.Settings)
        assertEquals(NavTarget.Settings, backStack.activeElement)
    }

    @Test
    fun `newRoot sets home as only active element`() {
        backStack.push(NavTarget.Settings)
        backStack.push(NavTarget.Generate)
        backStack.newRoot(NavTarget.Home)
        assertEquals(NavTarget.Home, backStack.activeElement)
    }

    @Test
    fun `push game target with custom params keeps params intact`() {
        val gameTarget = NavTarget.Game(isCustom = true, customWidth = 10, customHeight = 8)
        backStack.push(gameTarget)
        assertEquals(gameTarget, backStack.activeElement)
    }

    @Test
    fun `multiple pushes keep last element active`() {
        backStack.push(NavTarget.Generate)
        backStack.push(NavTarget.Settings)
        assertEquals(NavTarget.Settings, backStack.activeElement)
    }

    @Test
    fun `pop after multiple pushes goes one step back`() {
        backStack.push(NavTarget.Generate)
        backStack.push(NavTarget.Settings)
        backStack.pop()
        assertEquals(NavTarget.Generate, backStack.activeElement)
    }
}
