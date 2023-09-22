g scrollerblade

# TODO
* [ ] Text summary editor
	- [ ] Ask user to write summaries by allowing them to gray out text.
	- [ ] Then ask use for summary by highlighting key clauses ( Clauses start with to, in, of, or end with is, so)

* [ ] highlighting keywords in phrases
* [ ] stats based probability of selecting a clause
* [ ] stats review page: highlight intensity of paragraphs to show recency-weighted accuracy (errors should fade over time)
* [ ] include joining words
* [ ] customizable joining words/characters
* [ ] customizable texts
* [ ] export of texts with stats
* [ ] persistent stats
* [x] display current level and stage
* [ ] Current stage/layout needs improvement on screen rotation
* [ ] Do a little celebratory animation/sound-effect on each new stage/level
	- [ ] Make the next phrase pop up over the screen and encourage the user to read it somehow
* [ ] Randomize the success message

* [ ] Challenge mode variants:
	- [ ] Generate mix and match quizzes from paragraphs
	- [ ] Use user summaries to test the user
	- [ ] Multi choice: for a given clause, Selecting from a set of possible next clauses
	- [ ] Three way compare to find first

# Design
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
