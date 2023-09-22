package com.github.shannonbay.wordstream

import android.util.Log
import kotlin.random.Random
import kotlin.random.nextUInt

class Book(private val sections: List<List<String>>) {
    operator fun get(sectionIndex: Int): List<String>? {
        return if (sectionIndex in sections.indices) {
            sections[sectionIndex]
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
    private val book = flattenWithIndexes(sections)
    val clauses = book.first
    private val sectionIndexes = book.second

    val stats = UIntArray(clauses.size) { 0u }

    fun checkStats(currentLevel: Int, currentStage: Int) : Boolean {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        val range = sectionIndexes[first] until       sectionIndexes[last] - 1u
        for (i in range) {
            if(stats[i.toInt()] < 2u) return false
        }
        return true
    }

    fun getWeightedRandomLine(currentLevel: Int, currentStage: Int, exclude: UInt): UInt {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        val range = sectionIndexes[first] until sectionIndexes[last] - 1u

        Log.d("STATE", "Eligible Range: ${range}")
        // Create a list of eligible line indices based on stats and exclusion
        val eligibleIndices = mutableListOf<UInt>()
        for (i in range) {
            if(i != exclude)
                eligibleIndices.add(i)
        }

        // Check if there are any eligible indices
        if (eligibleIndices.isEmpty()) {
            return getRandomLine(currentLevel, currentStage, exclude)  // No eligible lines
        }

        // Calculate weights based on stats (you can adjust the weight calculation)
        val eligibleStats = eligibleIndices.map { stats[it.toInt()]+1u }
        val total = eligibleStats.sum()
        val weights = eligibleIndices.map { total - stats[it.toInt()] }
        Log.d("STATE", "Eligible stats: ${eligibleStats} Weights: ${weights}")
        val totalWeight = weights.sum()
        Log.d("STATE", "Eligible indices: ${eligibleIndices}")
        Log.d("STATE", "Weights         : ${weights}")
        // Generate a random value within the total weight range
        val randomValue = Random.nextUInt(0u, totalWeight)

        Log.d("STATE", "Random Line is ${randomValue} ${totalWeight}")

        // Select a line based on weighted random value
        var cumulativeWeight = 0u
        for (i in eligibleIndices.indices) {
            cumulativeWeight += weights[i]
            if (randomValue <= cumulativeWeight) {
                return eligibleIndices[i]
            }
        }
        Log.e("STATE", "Falling back to getRandomLine!!!!!")
        // This should not happen, but return null if something goes wrong
        return getRandomLine(currentLevel, currentStage, exclude)
    }

    fun getWeightedRandomLine2(currentLevel: Int, currentStage: Int, exclude: UInt): UInt {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        val range = sectionIndexes[first] until sectionIndexes[last] - 1u

        // Create a list of eligible line indices based on stats and exclusion

        // Calculate weights based on stats (you can adjust the weight calculation)
        val totalWeight = stats.sum() + stats.size.toUInt()
        val weights = stats.map { totalWeight - (stats[it.toInt()]+1u) }

        // Generate a random value within the total weight range
        val randomValue = Random.nextUInt(0u, totalWeight)

        // Select a line based on weighted random value
        Log.e("STATE", "${randomValue} ${totalWeight}")

        var cumulativeWeight = 0u
        for (i in clauses.indices) {
            cumulativeWeight += weights[i]
            if (randomValue <= cumulativeWeight) {
                Log.e("STATE", "Returning ${i}")
                return i.toUInt()
            }
        }
        Log.e("STATE", "Falling back to getRandomLine!!!!!")
        return getRandomLine(currentLevel, currentStage, exclude)
    }

    fun getRandomLine(currentLevel: Int, currentStage: Int, exclude: UInt): UInt {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        var r = Random.nextUInt((sectionIndexes[last] - 1u) - sectionIndexes[first]) + sectionIndexes[first]
        if(r >= exclude) r++
        return r
    }

    val totalSections: UInt
        get() = sections.size.toUInt()

    private fun UIntRange.coerceIn(range: UIntRange): UIntRange {
        val start = this.first.coerceIn(range)
        val endInclusive = this.last.coerceIn(range)
        return start..endInclusive
    }

    fun getSections(sectionRange: UIntRange): List<List<String>> {
        val validRange = sectionRange.coerceIn(0u until totalSections)
        return validRange.map { sections[it.toInt()] }
    }
}