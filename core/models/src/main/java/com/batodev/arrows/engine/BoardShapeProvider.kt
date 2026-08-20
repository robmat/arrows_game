package com.batodev.arrows.engine

interface BoardShapeProvider {
    fun getRandomShape(): BoardShape?

    fun getAllShapeNames(): List<String>

    fun getShapeByName(name: String): BoardShape?
}
