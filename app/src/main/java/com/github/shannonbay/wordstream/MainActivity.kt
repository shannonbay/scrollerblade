package com.github.shannonbay.wordstream;

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.ui.AppBarConfiguration
import com.github.shannonbay.wordstream.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var paragraphTextView: TextView
    lateinit var firstClauseTextBox: TextView

    private lateinit var secondClauseTextBox: TextView
    private lateinit var listView: LinearLayoutCompat
    private lateinit var resultTextBox: TextView
    private lateinit var progressTextBox: TextView
    private lateinit var paragraphScrollView: ScrollView

    private val book = Book(Companion.paragraphs.map { p -> splitSentenceIntoPhrases(p) })

    class MyViewModel(private val firstClauseIdx: IntField,
                      private val secondClauseIdx: IntField,
                      private val currentLevel: IntField,
                      private val currentStage: IntField,
                      private val book: Book) : ViewModel() {
        val firstClause: LiveData<String>
            get() = firstClauseIdx.valueLiveData.map { a -> book.clauses[a!!] }

        val secondClause: LiveData<String>
            get() = secondClauseIdx.valueLiveData.map { a -> book.clauses[a!!] }
        val currentParagraph: LiveData<String>
            get() = currentStage.valueLiveData.map { a -> paragraphs[currentLevel.value] }

        val currentLevelString: LiveData<String> // when we can listen to currentStage.or.currentLevel - that's an event engine!
            get() = currentStage.valueLiveData.map { a -> "Level ${currentLevel.value + 1} Stage ${currentStage.value + 1}"}
    }

    private val viewModel:  MyViewModel by lazy {
        MyViewModel(firstClauseIdx, secondClauseIdx, currentLevel, currentStage, book)
    }

    private val firstClauseIdx: IntField by lazy {
        createIntField("first", 0)
    }
    private val secondClauseIdx : IntField by lazy {
        createIntField("second", 1)
    }
    private val currentLevel : IntField by lazy {
        createIntField("level", 0)
    }
    private val currentStage : IntField by lazy {
        createIntField("stage", 0)
    }

    private var startY = 0f

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

/*    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_graph)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_reset -> {
                currentLevel.value = 0
                currentStage.value = 0
                secondClauseIdx.value = 0
                showRandomClause()
                book.stats.iterator().forEach { _ -> 0u }
                return true
            }
           // Handle other menu items as needed
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSessionState(applicationContext)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_graph)
  //      appBarConfiguration = AppBarConfiguration(navController.graph)
 //       setupActionBarWithNavController(navController, appBarConfiguration)


        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /*viewModel = ViewModelProvider(this, MyViewModelFactory(firstClauseIdx, secondClauseIdx, book))
            .get(MyViewModel::class.java)*/
        binding.setVariable(BR.viewModel, viewModel)
        binding.lifecycleOwner = this // Set the lifecycle owner for LiveData updates

        Log.d("STATE", "${viewModel} ${viewModel.secondClause.hasActiveObservers()}")

        logState(SaveLoadOption.Debug)
        firstClauseIdx.value
        secondClauseIdx.value

        Log.d("STATE","firstClauseIdx init? ${firstClauseIdx.valueLiveData.isInitialized} ${firstClauseIdx.valueLiveData.value} ")

        paragraphTextView = findViewById(R.id.paragraphTextView)
        listView = findViewById(R.id.listView) as LinearLayoutCompat
        firstClauseTextBox = findViewById(R.id.firstClauseTextBox)
        secondClauseTextBox = findViewById(R.id.secondClauseTextBox)
        resultTextBox = findViewById(R.id.resultTextBox)
        resultTextBox.isVisible = false

        progressTextBox = findViewById(R.id.progressTextBox)

        results = ArrayList(List(book.clauses.size){ false })

        progressTextBox.text = "Level $currentLevel Stage $currentStage"

        firstClauseIdx.value = firstClauseIdx.value.toInt()
        secondClauseIdx.value = secondClauseIdx.value.toInt()

        // Just to trigger UI
        currentStage.value = currentStage.value
   }

    class MyViewModelFactory(val firstClauseIdx: IntField, val secondClauseIdx: IntField, val currentLevel: IntField, val currentStage: IntField, val book: Book) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return MyViewModel(firstClauseIdx, secondClauseIdx, currentLevel, currentStage, book) as T
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        Log.e("WORD", "TOUCH")
        return super.onGenericMotionEvent(event)
    }

    private fun checkResult(guessBefore: Boolean) {
        val up = if (guessBefore) "down" else "up"
        Log.e("LVL", "first Idx: $firstClauseIdx second Idx:$secondClauseIdx guess: $up")
        if (guessBefore) {
            if (firstClauseIdx.value > secondClauseIdx.value) {
                results.set(secondClauseIdx.value, false)
                book.stats[firstClauseIdx.value] = 0u
                book.stats[secondClauseIdx.value] = 0u
                resultTextBox.text = "Mmmm"
                resultTextBox.setBackgroundColor(
                    MaterialColors.getColor(
                        resultTextBox,
                        com.google.android.material.R.attr.colorError
                    )
                )
            } else {
                results.set(secondClauseIdx.value, true)
                book.stats[secondClauseIdx.value] += 1u
                book.stats[firstClauseIdx.value] += 1u
                resultTextBox.text = "Amen!"
                resultTextBox.setBackgroundColor(
                    MaterialColors.getColor(
                        resultTextBox,
                        com.google.android.material.R.attr.colorTertiary
                    )
                )
            }
        } else {
            if (firstClauseIdx.value < secondClauseIdx.value) {
                results.set(secondClauseIdx.value, false)
                resultTextBox.text = "Mmmm"
                book.stats[firstClauseIdx.value] = 0u
                book.stats[secondClauseIdx.value] = 0u
                resultTextBox.setBackgroundColor(
                    MaterialColors.getColor(
                        resultTextBox,
                        com.google.android.material.R.attr.colorError
                    )
                )
            } else {
                Log.e(
                    "LVL",
                    "current clause Idx: " + secondClauseIdx + " results size:" + results.size
                )
                book.stats[secondClauseIdx.value] += 1u
                book.stats[firstClauseIdx.value] += 1u
                resultTextBox.text = "Amen!"
                resultTextBox.setBackgroundColor(
                    MaterialColors.getColor(
                        resultTextBox,
                        com.google.android.material.R.attr.colorTertiary
                    )
                )
            }
        }
        resultTextBox.isVisible = true
        if (book.checkStats(currentLevel.value, currentStage.value)) nextLevel()
    }


    override fun onResume() {
        super.onResume()
        //val preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        logState(SaveLoadOption.Save)
    }

    private fun logState(save: SaveLoadOption) {
        Log.d(
            "STATE",
            "$save lvl: ${currentLevel} ${currentLevel.value} " +
                    "stage: ${currentStage.value} " +
                    "firstClauseIdx: ${firstClauseIdx} " +
                    "secondClauseIdx: $secondClauseIdx"
        )
    }

    override fun onPause() {
        super.onPause()
        //val preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)

        firstClauseIdx.apply()
        secondClauseIdx.apply()
        currentLevel.apply()
        currentStage.apply()
    }

    private enum class SaveLoadOption {
        Save,
        Load,
        Debug
    }

    override fun onSaveInstanceState(outState: Bundle) {
        logState(SaveLoadOption.Save)
        firstClauseIdx.apply()
        secondClauseIdx.apply()
        currentLevel.apply()
        currentStage.apply()

        super.onSaveInstanceState(outState)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("ANIMATE", "Touch down")
                startY = event.y
            }
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
                unswap()
                showRandomClause()
            }
            MotionEvent.ACTION_MOVE -> {
                Log.e("WORD", "TOUCH")
                val endY = event.y
                if (endY > startY) {
                    // Swipe down
                    unswap()
                } else if (endY < startY) {
                    // Swipe up
                    swap()
                }
            }
        }
        return false
    }
    fun unswap() {
        if (listView.get(1) == firstClauseTextBox) {
            val a = listView.get(1)
            listView.removeViewAt(1)
            listView.addView(a, 0)
        }
    }
    fun swap() {
        if (listView.get(0) == firstClauseTextBox) {
            Log.d("SWAP", "swapping")
            val a = listView.get(1)
            listView.removeViewAt(1)
            listView.addView(a, 0)
        }
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

    /**
     * Levels consist of level and stage (current verse, and how many preceding verses respectively)
     */
    private fun nextLevel() {

        Log.d("STATE", "Increment stage")
        currentStage.inc()
        if(currentStage.value > currentLevel.value) {
            currentStage.value = 0
            currentLevel.inc()
            if(currentLevel.value > Companion.paragraphs.size) {
                // TODO game over
            } else {
                val dialog = LevelCompletionDialog(currentLevel.value, currentStage.value, paragraphs[currentLevel.value] )
                dialog.show(supportFragmentManager, "LevelCompletionDialog")
            }
        } else {
            val dialog = StageCompletionDialog(currentLevel.value, currentStage.value, paragraphs[currentLevel.value] )
            dialog.show(supportFragmentManager, "StageCompletionDialog")
        }
        // Ensure initial clauses are both from current stage
        showRandomClause()
    }

    fun weightedRandomSelection(chanceArray: Array<Double>): Int {
        val cdfArray = DoubleArray(chanceArray.size)
        var sum = 0.0
        for (i in chanceArray.indices) {
            sum += chanceArray[i]
            cdfArray[i] = sum
        }
        val randNum = Math.random() * sum
        var low = 0
        var high = cdfArray.size - 1
        while (low < high) {
            val mid = (low + high) / 2
            if (randNum < cdfArray[mid]) {
                high = mid
            } else {
                low = mid + 1
            }
        }
        return low
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        logState(SaveLoadOption.Load)
    }

    private var results = ArrayList<Boolean>()
    private val random = Random()

    private fun showRandomClause() {
        firstClauseIdx.value = secondClauseIdx.value

        // Choose a random clause excluding the prevClause
        // -1 since the prevClause is excluded
        Log.d("STATE", "Current Level ${currentLevel} toUint ${currentLevel.value.toUInt()}")
        Log.d("STATE", "Current Stage ${currentStage} toUint ${currentStage.value.toUInt()}")
        Log.d("STATE", "Level - Stage ${currentLevel.value.toUInt() - currentStage.value.toUInt()}")
        book.getWeightedRandomLine( currentLevel.value, currentStage.value, firstClauseIdx.value.toUInt())
            .toInt().also { secondClauseIdx.value = it }
    }

    companion object {
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
        )
    }
}
