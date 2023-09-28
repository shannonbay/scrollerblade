# scrollerblade

## TODO

Bugs:
- [ ] Something is wrong with the weightings - it takes too many goes to get through some stages and too few on others.
	- [ ] Is it being reset too often - should we switch to ratios of errors/successes?

Gameplay:
 [ ] Certain pairs are often the problem- need to identify tricky pairs and focus on context

Logic/Algorithms:

* [ ] Choose only from mistake clauses, make everything start at 0, and mistakes get reset to 0, and to advance you need 1
- [ ] \bwho\b should be a good phrase boundary 
* [ ] stats review page: highlight intensity of paragraphs to show recency-weighted accuracy (errors should fade over time)
* [ ] customizable joining words/characters
* [ ] customizable texts
* [ ] export of texts with stats
* [ ] persistent stats

UI Enhancements:
* [ ] Use stats field liveValue as the actual live value, so we don't rely on checkResult to trigger UI updates
* [ ] change the level and stage indicator to actually just say "Verses 2-5"
* [ ] On each new level, force the reader to read the verse - perhaps by 'one word' and by touching through the clauses
* [ ] Randomize the success message
* [ ] highlighting keywords in phrases
* [ ] Show a counter for how many mistakes need to be cleared to advance to next stage
* [ ] Remove stage dialogs
* [ ] About section, with citations
* [ ] Touch a phrase to set a custom color for that phrase which the game will use from now on
* [?] Double touch a phrase to expand the clause as a hint - may not be needed as we now expand clauses on mistakes
* [ ] Visually show how many wins in a row, overall, and per clause
* [ ] Learn Names of the Books of the Bible
* [ ] Learn subjects of the Books of the Bible
* [ ] Months, weeks, alphabet learning
* [ ] Allow user assigned colours per clause
* [ ] Tutorial
* [ ] Setting to disable StageCompletionDialog
* [ ] Different colours for each level - possibly a rotation and permanently associated with a paragraph
* [ ] Reset a book
* [ ] Basic UI elements
	- [x] Level and Stage ViewModel
	- [x] Paragraph ViewModel
* [ ] Stats and feedback
	- [ ] Progress Meter as a gradient loading bar (weighted by currentStage/currentLevel) on the currentParagraph TextBox
	- [ ] Show total error count and guesses in a row without error for this session/book
	- [ ] Do a little celebratory animation/sound-effect on each new stage/level
* [ ] Text summary editor
	- [ ] Ask user to write summaries by allowing them to gray out text.
	- [ ] Then ask use for summary by highlighting key clauses ( Clauses start with to, in, of, or end with is, so)
* [ ] Speed read current passage or hardest verses when app opens - cancel with a touch - if it automatically does this on mistakes and chooses hardest, done

Reader mode:
- [ ] Bible reading mode
- [ ] Options to show one word, phrase, verse or section at a time
- [ ] Filter to remove verse numbering and superscripts.

## Challenge mode variants:
	- [ ] Generate mix and match quizzes from paragraphs
	- [ ] Use user summaries to test the user
	- [ ] Multi choice: for a given clause, Selecting from a set of possible next clauses
	- [ ] Three way compare to find first
	- [ ] Choose next word, next phrase, next verse - can be converted into various Arcade games:
		- [ ] Arcade games like side-scroller like the Chrome browser game, snake, platforms like HamsterBall
		- [ ] For next verse mode, make it a choice between the next verse, next but one, and preceding verse... 
					Might even work for words, take first word from each of those... 
		- [ ] Learn which wrong choices trip players up through stats and use them more often :)
	- [ ] 

## Ideas
Dim background of organic shapes (abstract misty hills, cloud scape, or stars/galaxy effect?) with a low contrast dark, posterized abstract scenes

Maybe a combo of blurry and sharp shapes - Make it look layered with depth and very subtle contrast, and
Accelerometer texture movement and holographic shadow projection, like you're looking at a hologram

Animate the switching of the two clauses as player swipes
Move line processing to book constructor
Maybe weights should be based on all time stats, ratio of errors to successes

Eg lord jesus vs lord Christ, shows that jesus follows lord, and Christ follows lord, so Christ and lord are potentially synonyms. This is a single level of context and distance, but could be deeper. Kind of like degrees of separation

- [ ] Perform probabilistic analysis of word successions and use to generate feasible alternatives for word by word choice mode
			There are two dimensions here, one is the distance in text, the second is the distance in degrees of separation. 
			A third dimension is how many such relationships exist above the first degree of separation. Ie how many of my friends do you know? 
			A fourth is how many times it was NOT a given word but instead another, ie a reduction in likelihood 
			Top down sort - ie start with two paragraphs, then split into three, then four, so phrases get shorter and shorter
			Use grammar and text frequency analysis to generate reasonable choices for quizzes, along with more randomly chosen words, but ultimately, learn stats for which words trip people up! 

## Fixes and Enhancements
* [x] Make sure app title is displayed in icon
* [x] no longer display App title in MainActivity
* [x] Basic gamerules
	- [x] Track and check stage accuracy in results list
	- [x] Level and stage progression
	- [x] Select clauses relevant to stage
* [x] Remove the verse at top
* [x] 8dp padding
* [x] Consider presenting a whole verse for one of the options - this gives more opportunity to read and memorize
	- [x] Perhaps the mode should be enabled only after a mistake - of the two clauses, the one with the highest error ratio will become the whole verse
* [ ] Improve colour theme for colourblindness with earth and sky theme
* [x] Dim the orange 'level' indicator brightness (orange is too bright) 
* [x] Current stage/layout needs improvement on screen rotation
* [x] display current level and stage
- [x] Make the next phrase pop up over the screen as a Dialog
- [x] After a mistake, expand the hardest clause with it's succeeding clause
* [x] include joining words
* [x] Original phrase sorting mode should rotate through colours
* [x] Indicate harder phrases somehow to alert the player it's a phrase they often get wrong - brightness perhaps?
- [x] Game crashes at Verse 11 for some reason, but you can get past it by closing and re-opening shortly before verse 11, which forces a save.
	- caused by verse with one clause - fix was to allow same clause, and just hide the secondClauseTextView
* [x] Feedback element in uppder screen - Amen and Mmm are fading animations; Mmmm shakes. 
- [x] Only advance on correct answers
	- [x] Stop counting errors after first retry - so frustrated players don't ruin their stats and muck up their algorithm
* [x] stats based probability of selecting a clause

## Design principles
- Each mode of game-play has a specific goal - eg Reading, Memorisation, Comprehension, Musing.  Do not compromise on that goal.
	- Thus any new features introduced in a mode should specifically enhance that goal, or improve the aesthetics and streamline the interaction
- Minimalism, highly responsive UI, minimal interactions required (Currently we have too many dialog clicks? - dialog could go away on touch rather than release)
- the current game-play is fast, responsive, minimal and effective - any variations should be introduced as optional modes

## Code Design
Requirement:
Store stats per clause, such as answersSinceLastError per UUID/row
- We have an ordered, indexed List<String> representing the clauses
- each clause should be stored as a row with its stats (we have previously decided to use a file per row/UUID since that groups related values in a timely fashion)
- Thus we have clause:stat-name:value with types UUID:String:Int
- However, to get from the List<String> to a UUID, we need another mapping of indexes to UUID (or we could generate the UUID from the String, but that may lead to clashes?)
- We could substitute the UUID with the book/index/clause in a type 3 UUID, with a namespace_uuid unique to the book
	namespace_uuid = '6ba7b810-9dad-11d1-80b4-00c04fd430c8'  # Example UUID
	name = 'example.com'
	name_based_uuid = generate_name_based_uuid(namespace_uuid, name)
	print(name_based_uuid)
- For cleanup purposes, if a clause is removed from a book, that UUID will no longer be referenced by anything - we need a GC!  This is why the JStatePool style of GC is good - in that case, UUIDs come from a pool of allocated UUIDs.  When a UUID is assigned from the pool, there must be two references - one in each direction.  In JStatePool, that two-way reference is in working memory. We can do two maps in persistent storage as well.  From time to time, we can clean up.
- then the book needs to be stored with it's namespace UUID - or we generate unique namespace UUID as a type 3 from the book name (no duplicate book names)
- if the user re-uses a bookname, or renames a book, that's a problem, so we should store the UUID against it's bookname in the root
