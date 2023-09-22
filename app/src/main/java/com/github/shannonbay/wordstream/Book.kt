package com.github.shannonbay.wordstream

import kotlin.random.Random
import kotlin.random.nextUInt

class Book(private val pages: List<List<String>>) {
    operator fun get(pageIndex: Int): List<String>? {
        return if (pageIndex in pages.indices) {
            pages[pageIndex]
        } else {
            null
        }
    }

    fun flattenWithIndexes(list: List<List<String>>): Pair<List<String>, List<UInt>> {
        val flattenedList = mutableListOf<String>()
        val indexes = mutableListOf<UInt>()

        for (i in list.indices) {
            indexes.add(flattenedList.size.toUInt())
            flattenedList.addAll(list[i])
        }

        return Pair(flattenedList, indexes)
    }


//    private val pageIndexes: List<UInt> = pages.map { it.size.toUInt() }.scan(0u) { acc, size -> acc + size }
    private val book = flattenWithIndexes(pages)
    val clauses = book.first
    private val pageIndexes = book.second

    fun getRandomLine(pageRange: UIntRange): String? {
        val startIndex = pageRange.coerceIn(0u until pageIndexes.last().toUInt())

        val randomIndex = Random.nextUInt(startIndex)
        return clauses[randomIndex.toInt()]
    }

    val totalPages: UInt
        get() = pages.size.toUInt()

    private fun UIntRange.coerceIn(range: UIntRange): UIntRange {
        val start = this.first.coerceIn(range)
        val endInclusive = this.last.coerceIn(range)
        return start..endInclusive
    }

    fun getPages(pageRange: UIntRange): List<List<String>> {
        val validRange = pageRange.coerceIn(0u until totalPages)
        return validRange.map { pages[it.toInt()] }
    }
}