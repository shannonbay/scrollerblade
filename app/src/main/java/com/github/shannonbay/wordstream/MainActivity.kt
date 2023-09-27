package com.github.shannonbay.wordstream;

import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.HtmlCompat
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

    lateinit var firstClauseTextBox: TextView

    private lateinit var secondClauseTextBox: TextView
    private lateinit var listView: LinearLayoutCompat
    private lateinit var resultTextBox: TextView
    private lateinit var progressTextBox: TextView

    private val book = Book(Companion.verses.map { p -> splitVerseIntoPhrases(p) })

    class MyViewModel(private val firstClauseIdx: IntField,
                      private val secondClauseIdx: IntField,
                      private val currentLevel: IntField,
                      private val currentStage: IntField,
                      private val book: Book) : ViewModel() {
        val firstClause: LiveData<Spanned>
            get() = firstClauseIdx.valueLiveData.map { a ->
                HtmlCompat.fromHtml(
                    if(book.stats[a!!] == 0u) {
                        Log.d("STATS", "Include context")
                        ("<b>" + book.clauses[a!!] + "</b>" + trimJoiners(book.clauses[a!! + 1]))
                    } else {
                        Log.d("STATS", "No context")
                        trimJoiners( book.clauses[a!!])
                    }, 0)
            }

        val secondClause: LiveData<String>
            get() = secondClauseIdx.valueLiveData.map { a -> trimJoiners(book.clauses[a!!]) }
        val currentParagraph: LiveData<String>
            get() = currentStage.valueLiveData.map { a -> verses[currentLevel.value] }

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
            R.id.menu_item_about -> {
                val dialog = AboutDialog(packageManager, packageName)
                dialog.show(supportFragmentManager, "LevelCompletionDialog")
                return true
            }
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

        listView = findViewById(R.id.listView) as LinearLayoutCompat
        firstClauseTextBox = findViewById(R.id.firstClauseTextBox)
        secondClauseTextBox = findViewById(R.id.secondClauseTextBox)
        resultTextBox = findViewById(R.id.resultTextBox)
        resultTextBox.isVisible = false

        progressTextBox = findViewById(R.id.progressTextBox)

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

    private fun checkResult(guessBefore: Boolean) : Boolean {
        val up = if (guessBefore) "down" else "up"
        Log.e("LVL", "first Idx: $firstClauseIdx second Idx:$secondClauseIdx guess: $up")
        try {
            if (guessBefore) {
                if (firstClauseIdx.value > secondClauseIdx.value) {
                    return handleMistake()
                } else {
                    return handleCorrect()
                }
            } else {
                if (firstClauseIdx.value < secondClauseIdx.value) {
                    return handleMistake()
                } else {
                    return handleCorrect()
                }
            }
        } finally {
           if (book.checkStats(currentLevel.value, currentStage.value)) nextLevel()
        }
    }

    private fun shakeThenFadeView(view: View, repeatCount: Int) {
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)

        // Create an AnimationSet to run animations sequentially
        val animationSet = AnimationSet(true)

        // Add the shake animation
        animationSet.addAnimation(shakeAnimation)

        // Set a listener for the shake animation to start the fade animation
        shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // Animation started
            }

            override fun onAnimationEnd(animation: Animation) {
                // Animation ended, start the fade animation
                fadeOutView(view)
            }

            override fun onAnimationRepeat(animation: Animation) {
                // Animation repeated (not used in this example)
            }
        })

        // Set animation duration for the combined animation

        // Start the animation
        view.startAnimation(animationSet)
    }

    private fun fadeOutView(view: View) {
        val blah = AnimationUtils.loadAnimation(this,R.anim.fade_out)
        blah.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        resultTextBox.startAnimation(blah);

    }

    private fun handleMistake() : Boolean {
        resultTextBox.text = "Mmmm"
        if(!retry) {
            Log.d("STATS", "Reset count for both")
            book.stats[firstClauseIdx.value] = 0u
            book.stats[secondClauseIdx.value] = 0u
        }
        retry = true
        resultTextBox.setBackgroundColor(
            MaterialColors.getColor(
                resultTextBox,
                com.google.android.material.R.attr.colorError
            )
        )

        resultTextBox.isVisible = true
        shakeThenFadeView(resultTextBox, 3)

        return false
    }

    private fun handleCorrect() : Boolean {
        if(!retry) {
            book.stats[secondClauseIdx.value] += 1u
            book.stats[firstClauseIdx.value] += 1u
        }
        resultTextBox.text = "Amen!"
        resultTextBox.setBackgroundColor(
            MaterialColors.getColor(
                resultTextBox,
                com.google.android.material.R.attr.colorTertiary
            )
        )

        resultTextBox.isVisible = true
        fadeOutView(resultTextBox)


        return true
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

    var retry = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("ANIMATE", "Touch down")
                startY = event.y
            }
            MotionEvent.ACTION_UP -> {
                Log.e("WORD", "TOUCH")
                val endY = event.y
                val result : Boolean =
                    if (endY > startY) {
                        // Swipe down
                        checkResult(true)
                    } else if (endY < startY) {
                        checkResult(false)
                    } else true
                        // Swipe up
                unswap()
                if(result)
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


    /**
     * Levels consist of level and stage (current verse, and how many preceding verses respectively)
     */
    private fun nextLevel() {

        Log.d("STATE", "Increment stage")
        currentStage.inc()
        if(currentStage.value > currentLevel.value) {
            currentStage.value = 0
            currentLevel.inc()
            if(currentLevel.value > Companion.verses.size) {
                // TODO game over
            } else {
                val dialog = LevelCompletionDialog(currentLevel.value, currentStage.value, verses[currentLevel.value] )
                dialog.show(supportFragmentManager, "LevelCompletionDialog")
            }
        } else {
            val dialog = StageCompletionDialog(currentLevel.value, currentStage.value, verses[currentLevel.value] )
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

    private val random = Random()

    // Define an array of colors to cycle through
    private val colorArray = arrayOf(
        com.google.android.material.R.attr.colorPrimaryDark,
        com.google.android.material.R.attr.colorSecondary
    )

    // Initialize an index to keep track of the current color in the array
    private var currentColorIndex = 0

    fun setBackgroundColour(textView: TextView) {
        // Store the background drawable of textView1
        textView.setBackgroundColor(            MaterialColors.getColor(
            textView,
            colorArray[currentColorIndex]
        ))
    }

    fun highlight(textView: TextView) {
        // Store the background drawable of textView1
        textView.setBackgroundColor(            MaterialColors.getColor(
            textView,
            androidx.appcompat.R.attr.colorPrimary
        ))
    }

    fun rotateBackgroundColours(textView: TextView) {
        // Store the background drawable of textView1
        currentColorIndex = (currentColorIndex + 1) % colorArray.size
   }

    private fun showRandomClause() {
        retry = false
        firstClauseIdx.value = secondClauseIdx.value

        setBackgroundColour(firstClauseTextBox)
        rotateBackgroundColours(secondClauseTextBox)
        setBackgroundColour(secondClauseTextBox)

        // Choose a random clause excluding the prevClause
        // -1 since the prevClause is excluded
        Log.d("STATE", "Current Level ${currentLevel} toUint ${currentLevel.value.toUInt()}")
        Log.d("STATE", "Current Stage ${currentStage} toUint ${currentStage.value.toUInt()}")
        Log.d("STATE", "Level - Stage ${currentLevel.value.toUInt() - currentStage.value.toUInt()}")
        try {
            if (currentStage.value > 1) {
                book.getWeightedRandomLineExcludeLevel(
                    currentLevel.value, currentStage.value,
                    book.clausesToVerse[firstClauseIdx.value].toUInt()
                ).toInt().also {
                    secondClauseIdx.value = it
                }
                return
            }

            book.getWeightedRandomLine(
                currentLevel.value,
                currentStage.value,
                firstClauseIdx.value.toUInt()
            )
                .toInt().also { secondClauseIdx.value = it }
            secondClauseTextBox.isVisible = secondClauseIdx.value != firstClauseIdx.value
        } finally {
            if(book.stats[secondClauseIdx.value] == 0u) { // we messed up last time we saw this one
                highlight(secondClauseTextBox)
            }
        }
    }

    companion object {
        fun trimJoiners(sentence: String): String {
            // Define the words that should split the sentence into phrases

            // Construct a regular expression pattern to match whole words
//        val pattern = "\\b(?:${splitWords.joinToString("|")})\\b|[,.?!]"
            return sentence.replace(pattern, "")
        }
        fun splitWithDelimiter(input: String, delimiter: Regex): List<String> {
            val matches = delimiter.findAll(input)
            val result = mutableListOf<String>()
            var startIndex = 0

            for (match in matches) {
                val endIndex = match.range.endInclusive + 1
                result.add(input.substring(startIndex, endIndex))
                startIndex = endIndex
            }

            if (startIndex < input.length) {
                result.add(input.substring(startIndex))
            }

            return result
        }

        fun splitVerseIntoPhrases(sentence: String): List<String> {
            // Split the sentence into phrases using the pattern
            val phrases = splitWithDelimiter(sentence, pattern)

            // Remove any empty or whitespace-only phrases
            return phrases.filter { it.trim().isNotEmpty() }
        }

        private val splitWords = setOf("and", "or", "who")
        private val pattern = Regex("(?<=\\w\\s)\\b(?:${splitWords.joinToString("|")})\\b|([,.?!;:]\n?)")

        private val verses: Array<String> = arrayOf(
            "Paul, an apostle of Christ Jesus through the will of God, to the saints who are in Ephesus and are faithful in Christ Jesus:\n",
            "Grace to you and peace from God our Father and the Lord Jesus Christ.\n",
            "Blessed be the God and Father of our Lord Jesus Christ, who has blessed us with every spiritual blessing in the heavenlies in Christ,\n",
            "Even as He chose us in Him before the foundation of the world to be holy and without blemish before Him in love,\n",
            "Predestinating us unto sonship through Jesus Christ to Himself, according to the good pleasure of His will,\n",
            "To the praise of the glory of His grace, with which He graced us in the Beloved;\n",
            "In whom we have redemption through His blood, the forgiveness of offenses, according to the riches of His grace,\n",
            "Which He caused to abound to us in all wisdom and prudence,\n",
            "Making known to us the mystery of His will according to His good pleasure, which He purposed in Himself,\n",
            "Unto the economy of the fullness of the times, to head up all things in Christ, the things in the heavens and the things on the earth, in Him;\n",
            "In whom also we were designated as an inheritance, having been predestinated according to the purpose of the One who works all things according to the counsel of His will,\n",
            "That we would be to the praise of His glory who have first hoped in Christ,\n",
            "In whom you also, having heard the word of the truth, the gospel of your salvation, in Him also believing, you were sealed with the Holy Spirit of the promise,\n",
            "Who is the pledge of our inheritance, unto the redemption of the acquired possession, to the praise of His glory.\n",
            "Therefore I also, having heard of the faith in the Lord Jesus which is among you and your love to all the saints,\n",
            "Do not cease giving thanks for you, making mention of you in my prayers,\n",
            "That the God of our Lord Jesus Christ, the Father of glory, may give to you a spirit of wisdom and revelation in the full knowledge of Him,\n",
            "The eyes of your heart having been enlightened, that you may know what is the hope of His calling, and what are the riches of the glory of His inheritance in the saints,\n",
            "And what is the surpassing greatness of His power toward us who believe, according to the operation of the might of His strength,\n",
            "Which He caused to operate in Christ in raising Him from the dead and seating Him at His right hand in the heavenlies,\n",
            "Far above all rule and authority and power and lordship and every name that is named not only in this age but also in that which is to come;\n",
            "And He subjected all things under His feet and gave Him to be Head over all things to the church,\n",
            "Which is His Body, the fullness of the One who fills all in all."
        )
    }
}
