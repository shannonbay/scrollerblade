package com.github.shannonbay.wordstream;

import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.color.MaterialColors
import java.util.LinkedList
import java.util.Random


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var paragraphTextView: TextView
    private lateinit var firstClauseTextBox: TextView
    private lateinit var secondClauseTextBox: TextView
    private lateinit var resultTextBox: TextView
    private lateinit var progressTextBox: TextView
    private lateinit var paragraphScrollView: ScrollView
    private val paragraphs: Array<String> = arrayOf(
        "Paul, an apostle of Christ Jesus through the will of God, to the saints who are in Ephesus and are faithful in Christ Jesus:",
        "Grace to you and peace from God our Father and the Lord Jesus Christ.",
        "Blessed be the God and Father of our Lord Jesus Christ, who has blessed us with every spiritual blessing in the heavenlies in Christ,",
        "Even as He chose us in Him before the foundation of the world to be holy and without blemish before Him in love,",
        "Predestinating us unto sonship through Jesus Christ to Himself, according to the good pleasure of His will,",
        "To the praise of the glory of His grace, with which He graced us in the Beloved;",
        "In whom we have redemption through His blood, the forgiveness of offenses, according to the riches of His grace,",
        "Which He caused to abound to us in all wisdom and prudence,",
        "Making known to us the mystery of His will according to His good pleasure, which He purposed in Himself,",
        "Unto the economy of the fullness of the times, to head up all things in Christ, the things in the heavens and the things on the earth, in Him;",
        "In whom also we were designated as an inheritance, having been predestinated according to the purpose of the One who works all things according to the counsel of His will,",
        "That we would be to the praise of His glory who have first hoped in Christ,",
        "In whom you also, having heard the word of the truth, the gospel of your salvation, in Him also believing, you were sealed with the Holy Spirit of the promise,",
        "Who is the pledge of our inheritance, unto the redemption of the acquired possession, to the praise of His glory.",
        "Therefore I also, having heard of the faith in the Lord Jesus which is among you and your love to all the saints,",
        "Do not cease giving thanks for you, making mention of you in my prayers,",
        "That the God of our Lord Jesus Christ, the Father of glory, may give to you a spirit of wisdom and revelation in the full knowledge of Him,",
        "The eyes of your heart having been enlightened, that you may know what is the hope of His calling, and what are the riches of the glory of His inheritance in the saints,",
        "And what is the surpassing greatness of His power toward us who believe, according to the operation of the might of His strength,",
        "Which He caused to operate in Christ in raising Him from the dead and seating Him at His right hand in the heavenlies,",
        "Far above all rule and authority and power and lordship and every name that is named not only in this age but also in that which is to come;",
        "And He subjected all things under His feet and gave Him to be Head over all things to the church,",
        "Which is His Body, the fullness of the One who fills all in all."
        // Add more paragraphs as needed
    )

    private val levels = paragraphs.map { p -> splitSentenceIntoPhrases(p) }
    private var currentLevel = 0
    private var currentStage = 0

    private var startY = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        paragraphTextView = findViewById(R.id.paragraphTextView)
        firstClauseTextBox = findViewById(R.id.firstClauseTextBox)
        secondClauseTextBox = findViewById(R.id.secondClauseTextBox)
        resultTextBox = findViewById(R.id.resultTextBox)
        resultTextBox.isVisible = false

        progressTextBox = findViewById(R.id.progressTextBox)

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        initialiseClauses()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        Log.e("WORD", "TOUCH")
        return super.onGenericMotionEvent(event)
    }

    private fun checkResult(guessBefore: Boolean) {
        val up = if(guessBefore) "down" else "up"
        Log.e("LVL", "first Idx: $firstClauseIdx second Idx:$secondClauseIdx guess: $up")
        if(guessBefore) {
            if(firstClauseIdx > secondClauseIdx) {
                results.set(secondClauseIdx, false)
                resultTextBox.text = "Mmmm"
                resultTextBox.setBackgroundColor(MaterialColors.getColor(resultTextBox, com.google.android.material.R.attr.colorError))
            } else {
                results.set(secondClauseIdx, true)
                resultTextBox.text = "Amen!"
                resultTextBox.setBackgroundColor(MaterialColors.getColor(resultTextBox, com.google.android.material.R.attr.colorTertiary))
            }
        } else {
            if(firstClauseIdx < secondClauseIdx) {
                results.set(secondClauseIdx, false)
                resultTextBox.text = "Mmmm"

                resultTextBox.setBackgroundColor(MaterialColors.getColor(resultTextBox, com.google.android.material.R.attr.colorError))
            } else {
                Log.e("LVL", "current clause Idx: " + secondClauseIdx + " results size:" + results.size)
                results.set(secondClauseIdx, true)
                resultTextBox.text = "Amen!"
                resultTextBox.setBackgroundColor(MaterialColors.getColor(resultTextBox, com.google.android.material.R.attr.colorTertiary))
            }
        }
        resultTextBox.isVisible = true
        if(!results.contains(false)) nextLevel()
    }

    override fun onResume() {
        super.onResume()
        //val preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val value = sharedPreferences.getString("key", "default value")
        currentLevel = sharedPreferences.getInt("currentLevel", 0)
        currentStage = sharedPreferences.getInt("currentStage", 0)
        firstClauseIdx = sharedPreferences.getInt("firstClauseIdx", 0)
        secondClauseIdx = sharedPreferences.getInt("secondClauseIdx", 1)
        initialiseClauses()
        Log.d("STATE",
            "Load lvl: $currentLevel stage: $currentStage firstClauseIdx: $firstClauseIdx secondClauseIdx: $secondClauseIdx"
        )
        setFirstClause(firstClauseIdx)
        setSecondClause(secondClauseIdx)
    }
    override fun onPause(){
        super.onPause()
        //val preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("currentLevel", currentLevel)
        editor.putInt("currentStage", currentStage)
        editor.putInt("firstClauseIdx", firstClauseIdx)
        editor.putInt("secondClauseIdx", secondClauseIdx)
        editor.apply()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("STATE", "Save lvl: " + currentLevel + " stage: " + currentStage + " firstClauseIdx: " + firstClauseIdx + " secondClauseIdx: " + secondClauseIdx)
        outState.putInt("currentLevel", currentLevel)
        outState.putInt("currentStage", currentStage)
        outState.putInt("firstClauseIdx", firstClauseIdx)
        outState.putInt("secondClauseIdx", secondClauseIdx)
        super.onSaveInstanceState(outState)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Log.e("LVL", "Save2 1: " + firstClauseIdx + " 2: " + secondClauseIdx)
        outState.putInt("currentLevel", currentLevel)
        outState.putInt("currentStage", currentStage)
        outState.putInt("firstClauseIdx", firstClauseIdx)
        outState.putInt("secondClauseIdx", secondClauseIdx)
        super.onSaveInstanceState(outState)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startY = event.y
            MotionEvent.ACTION_UP -> {
                Log.e("WORD", "TOUCH")
                val endY = event.y
                if (endY > startY) {
                    // Swipe down
                    checkResult(true)
                } else if (endY < startY) {
                    // Swipe up
                    checkResult(false)
                }

                showRandomClause()
            }
        }
        return true
    }

    override fun setFinishOnTouchOutside(finish: Boolean) {
        super.setFinishOnTouchOutside(finish)
    }
    val debouncer = Debouncer()

    fun splitSentenceIntoPhrases(sentence: String): List<String> {
        // Define the words that should split the sentence into phrases
        val splitWords = setOf("and", "or")

        // Construct a regular expression pattern to match whole words
        val pattern = "\\b(?:${splitWords.joinToString("|")})\\b|[,.?!]"

        // Split the sentence into phrases using the pattern
        val phrases = sentence.split(Regex(pattern))

        // Remove any empty or whitespace-only phrases
        return phrases.filter { it.trim().isNotEmpty() }
    }


    private val clauses = LinkedList<String>()

    private var firstClauseIdx = 0;
    private var secondClauseIdx = 1;
    /**
     * Levels consist of level and stage (current verse, and how many preceding verses respectively)
     */
    private fun nextLevel() {

        currentStage++
        if(currentStage > currentLevel) {
            currentStage = 0
            currentLevel++
            if(currentLevel > paragraphs.size) {
                clauses.clear()
                clauses.add("End of Game"); // TODO signal end of game some other way or go into loop mode
            }
        }

        setFirstClause(0)
        setSecondClause(1)
        initialiseClauses()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentLevel = savedInstanceState.getInt("currentLevel")
        currentStage = Math.max(0,savedInstanceState.getInt("currentStage"))
        firstClauseIdx = savedInstanceState.getInt("firstClauseIdx")
        secondClauseIdx = savedInstanceState.getInt("secondClauseIdx")
        Log.d("STATE", "Load lvl: " + currentLevel + " stage: " + currentStage + " firstClauseIdx: " + firstClauseIdx + " secondClauseIdx: " + secondClauseIdx)
        initialiseClauses()
    }

    private fun initialiseClauses() {
        // Check if game over and assign current paragraph
        if (currentLevel < paragraphs.size) {
            paragraphTextView.text = paragraphs[currentLevel]
        } else {
            paragraphTextView.text = "End of the game."
            return
        }

        // Initialise clauses for current stage
        clauses.clear()
        for (stage in currentStage downTo 0) {
            val stageClauses = levels.get(currentLevel - stage)
            clauses.addAll(stageClauses)
        }
        val sb = StringBuilder()
        for (clause in clauses) {
            sb.append(clause).append(":")
        }
        Log.e("LVL", sb.toString())
        results = ArrayList(List(clauses.size) { false })

        setFirstClause(firstClauseIdx)
        setSecondClause(secondClauseIdx)

        progressTextBox.text = "Level $currentLevel Stage $currentStage"
    }

    private fun setFirstClause(idx: Int){
        firstClauseIdx = idx
        firstClauseTextBox.text = clauses.get(firstClauseIdx)
    }

    private fun setSecondClause(idx: Int){
        secondClauseIdx = idx
        secondClauseTextBox.text = clauses.get(secondClauseIdx)
    }

    private var results = ArrayList<Boolean>()
    private val random = Random()

    private fun showRandomClause() {
        setFirstClause(secondClauseIdx)

        // Choose a random clause excluding the prevClause
        // -1 since the prevClause is excluded
        var randomIndex = random.nextInt(clauses.size-1)
        secondClauseIdx = if(randomIndex >= firstClauseIdx) randomIndex+1 else randomIndex

        setSecondClause(secondClauseIdx)
    }
}
