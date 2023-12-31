package com.github.shannonbay.wordstream

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlin.random.Random
import kotlin.random.nextUInt

class Book(private val sections: List<List<String>>) {
    operator fun get(verseIndex: Int): List<String>? {
        return if (verseIndex in sections.indices) {
            sections[verseIndex]
        } else {
            null
        }
    }

    private fun flattenWithIndexes(list: List<List<String>>): Triple<List<String>, List<UInt>, IntArray> {
        val flattenedList = mutableListOf<String>()
        val indexes = mutableListOf<UInt>()

        for (i in list.indices) {
            indexes.add(flattenedList.size.toUInt())
            flattenedList.addAll(list[i])
        }
        val phraseToVerse = list.flatMapIndexed { index, phrases -> phrases.map { index } }.toIntArray()

        Log.d("BOOK", "My array ${phraseToVerse.joinToString ( " " )}")
        return Triple(flattenedList, indexes, phraseToVerse)
    }

//    private val pageIndexes: List<UInt> = pages.map { it.size.toUInt() }.scan(0u) { acc, size -> acc + size }
    private val book = flattenWithIndexes(sections)
    val clauses = book.first
    private val verseIndex = book.second
    val clausesToVerse = book.third

    val stats: IntArrayField by lazy {
        createIntArrayField("stats", IntArray(clauses.size) { 0 })
    }

    fun countZeros(currentLevel: Int, currentStage: Int): Int {
        val first = currentLevel - currentStage
        val last = currentLevel + 1

        // Ensure the range is within the bounds of the stats array
        val rangeStart = verseIndex.getOrElse(first) { 0u }
        val rangeEnd = verseIndex.getOrElse(last) { stats.value.size.toUInt() } -1u

        // Calculate the count of zeros within the specified range

        return stats.value.slice((rangeStart.toInt() until rangeEnd.toInt())).count { it == 0 }
    }

    var zeros : MutableLiveData<Int> = MutableLiveData(0)

    fun checkStats(currentLevel: Int, currentStage: Int) : Boolean {
        val x = countZeros(currentLevel, currentStage)
        zeros.value = x
        return x == 0
    }
    fun getWeightedRandomLineExcludeLevel(currentLevel: Int, currentStage: Int, exclude: UInt): UInt {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        val range = verseIndex[first] until verseIndex[last]

//        val excludes = verseIndex[exclude.toInt()] until verseIndex[(exclude+1u).toInt()]

        Log.d("STATE", "Eligible Range: ${range}")
        // Create a list of eligible line indices based on stats and exclusion
        val eligibleIndices = mutableListOf<UInt>()
        for (i in range) {
            if(clausesToVerse[i.toInt()].toUInt() != exclude && stats.value[i.toInt()] <= 1)
                eligibleIndices.add(i)
        }

        // Check if there are any eligible indices
        if (eligibleIndices.isEmpty()) {
            return getRandomLine(currentLevel, currentStage, exclude)  // No eligible lines
        }

        // Calculate weights based on stats (you can adjust the weight calculation)
        val eligibleStats = eligibleIndices.map { stats.value[it.toInt()]+1 }
        val total = eligibleStats.sum()
        val max = eligibleStats.max()
        val weights = eligibleIndices.map { max - stats.value[it.toInt()] }
        Log.d("STATE", "Eligible stats: ${eligibleStats} Weights: ${weights}")
        val totalWeight = weights.sum()
        Log.d("STATE", "Eligible indices: ${eligibleIndices}")
        Log.d("STATE", "Weights         : ${weights}")
        // Generate a random value within the total weight range
        val randomValue = Random.nextInt(0, totalWeight)

        Log.d("STATE", "Random Line is ${randomValue} ${totalWeight}")

        // Select a line based on weighted random value
        var cumulativeWeight = 0
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

    fun getWeightedRandomLine(currentLevel: Int, currentStage: Int, exclude: UInt): UInt {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        val range = verseIndex[first] until verseIndex[last]

        Log.d("STATE", "Eligible Range: ${range}")
        // Create a list of eligible line indices based on stats and exclusion
        val eligibleIndices = mutableListOf<UInt>()
        for (i in range) {
            if(i != exclude)
                eligibleIndices.add(i)
        }

        // Check if there are any eligible indices
        if (eligibleIndices.isEmpty()) {
            return exclude
        }

        // Calculate weights based on stats (you can adjust the weight calculation)
        val eligibleStats = eligibleIndices.map { stats.value[it.toInt()]+1 }
        val total = eligibleStats.sum()
        val max = eligibleStats.max()
        val weights = eligibleIndices.map { max - stats.value[it.toInt()] }
        Log.d("STATE", "Eligible stats: ${eligibleStats} Weights: ${weights}")
        val totalWeight = weights.sum()
        Log.d("STATE", "Eligible indices: ${eligibleIndices}")
        Log.d("STATE", "Weights         : ${weights}")
        // Generate a random value within the total weight range
        val randomValue = Random.nextInt(0, totalWeight)

        Log.d("STATE", "Random Line is ${randomValue} ${totalWeight}")

        // Select a line based on weighted random value
        var cumulativeWeight = 0
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

    fun getRandomLine(currentLevel: Int, currentStage: Int, exclude: UInt): UInt {
        val first = currentLevel - currentStage
        val last = currentLevel + 1
        var r = Random.nextUInt((verseIndex[last] - 1u) - verseIndex[first]) + verseIndex[first]
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