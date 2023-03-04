# Lab: Text indexing

In this lab, you will implement a very fast search for large text files.

## About the labs

* This lab is part of the examination of the course.
  Therefore, you must not copy code from or show code to other students.
  You are welcome to discuss general ideas with one another, but anything you do must be **your own work**.
* Further info on Canvas:
  - [general information](https://chalmers.instructure.com/courses/23356/pages/labs-general-information)
  - [the lab system](https://chalmers.instructure.com/courses/23356/pages/the-lab-system)
  - [running Java labs](https://chalmers.instructure.com/courses/23356/pages/running-java-labs)

## Background

If we want to search for a string in a text file, we usually iterate through the file from the start until we find an occurrence of the string.
This works fine for small-to-medium sized texts,
but when the files contain millions of words this simple idea becomes too slow.

To solve this, we can calculate a *search index* in advance.
Much like an index in a book, a search index is a data structure that allows us to quickly find all the places where a given string appears.

Search indexes are used in many applications -- for example, database engines use them to be able to search quickly.

In this lab, you will build a search index using a data structure called a *suffix array*.
Suffix arrays build on ideas from *sorting algorithms* and *binary search* to search efficiently in large texts.

Later in the course, you will learn about two more data structures, called *hash tables* and *search trees*, which can be used to implement a search index that can be updated quickly.

The lab has four parts (but some of them are rather short):

* In part 1, you will implement versions of insertion sort and quicksort.
* In part 2, you will create a search index for a text file, using your sorting algorithms from part 1.
* In part 3, you will use the search index to find strings in the text file quickly.
* In part 4, you will implement multi-key quicksort, a specialized sorting algorithm for strings.

By the end, you will be able to search through large texts in a millisecond!
(Building the index will take some time, but the search will be instantaneous.)

## Getting started

The lab directory contains several Java files, some organized into packages:

- **util**:
  This is a utility package.
  You don't have to look at it (except when we tell you that something is relevant).

- **sorting**:
  This package deals with sorting.
  It is missing implementations of several sorting algorithms (see Parts 1 and 4).

- **texts**:
  This directory contains excerpts from the British National Corpus (BNC), a large collection of English-language text collected from all kinds of spoken and written sources.
  We shall use these to test your text indexing algorithm (see Part 2).

- **Text.java**:
  This class represents a text file that we want to index (see Part 2).
  It is already implemented for you.

- **SuffixArray.java**:
  This is the central class of the project.
  It is responsible for building the search index and searching through it.
  It contains skeleton code that you will have to complete for Parts 2 and 3.

- **BuildSuffixArray.java** and **RunSearchPrompt.java**:
  These are executable programs that use `SuffixArray` to build a search index and run search prompts.
  They will become usable once you finish Parts 2 and 3, respectively.

- **answers.txt**:
  Here, you will write down answers to questions in this lab.

## Part 1: implementing some sorting algorithms

This part is solely concerned with the **sorting** package.
You can ignore the rest of the project here.

### Package overview

We represent each sorting algorithm as subclasses of the abstract class `SortingAlgorithm` (in **SortingAlgorithm.java**).
This class takes a type parameter `E` for the type of items to be sorted (and another type parameter `C` for specialized comparator support, but this will become relevant only in Part 4).
Its constructor takes a comparator and stores it as attribute `comparator`, for use by the sorting algorithm.
The only abstract method (to be implemented in each subclass) is:
```java
sort(List<E> list, int from, int to)
```
This should sort the range of the given list specified by `from` and `to`.
The convenience method `sort(List<E> list)` sorts the entire list and is implemented by calling the above method with range from 0 to `list.size()`.

**Note:**
In this lab, we follow the standard convention in computer science that the index range from x to y consists of the indices x, x+1, …, y-1.
It is *inclusive* at the start and *exclusive* at the end.
This convention makes it convenient to work with arrays: the valid indices of an array of size n go from 0 to n.

**Note:**
All sorting in this lab is done on *lists* instead of arrays.
So you have to use `list.get` to read from the list and `list.set` to write to it.
Since `List` is an interface, this allows us to plug in other implementations of a list than just an array of type E.

`SortingAlgorithm` will eventually have three implementations:
- **InsertionSort.java**: Task 1a
- **Quicksort.java**: Task 1b
- **MultiKeyQuicksort.java** Part 4

The remaining files in the package are helper classes:

- **QuicksortPivotSelector.java**:
  This class abstracts over the pivot selection strategy for quicksort (see Part 1b below).

- **Test.java**
  This program tests and benchmarks your sorting algorithms.

- **LexicographicComparator.java** and **CharSequenceComparator.java**:
  Ignore these for now.
  They will become relevant in Part 4.

### Task 1a: Insertion sort

Your first task is to implement the missing method in the class `InsertionSort`:
```java
public void sort(List<E> list, int from, int to) {
    throw new UnsupportedOperationException(); // TODO: implement
}
```
You should implement the in-place version of insertion sort, not allocating any additional memory.
To perform swaps, you can use the helper method `swap(List<E> list, int i, int j)` inherited from `SortingAlgorithm`.

Reminders:
* To compare list values, use the stored comparator (`comparator.compare`).
* You should only sort the range of the list starting at `from` and ending at `to`.
  The first index in this range is `from` and the last index is `to - 1`.

<details>
<summary>
Spoiler 1
</summary>

Don't use recursion.
Two nested loops will do.
</details>

<details>
<summary>
Spoiler 2
</summary>

The outer loop should have an index `i` range from `from` to `to`.
The inner loop should move the new element (at index `i`) backwards to its correct place according to the ordering.
For example, this can be done by repeatedly swapping with the element before it.
</details>

<details>
<summary>
Spoiler 3
</summary>

The course book has pseudocode for insertion sort.
</details>

The class `InsertionSort` has a main method with some basic tests (alphabetical sorting of strings).
If you run with assertions enabled, an exception will be thrown if sorting results in an unsorted list.
Feel free to add your own tests!

### Task 1b: Quicksort

Your second task is to complete the implementation of quicksort in `Quicksort.java`.
To help you structure your code, we have created a skeleton with a separate partition method:
```java
// Sort the given range of the list in-place.
public void sort(List<E> list, int from, int to) {
    // Base case
    int size = to - from;
    if (size == 0)
        return;

    int pivotIndex = partition(list, from, to);
    throw new UnsupportedOperationException(); // TODO: implement
}

// Partition the given range of a list.
// Return the final position of the pivot.
private int partition(List<E> list, int from, int to) {
    // The index of the element that should be used as the pivot.
    int pivotIndex = selector.pivotIndex(list, from, to, comparator);
    E pivot = list.get(pivotIndex);

    throw new UnsupportedOperationException(); // TODO: implement
}
```
You have to finish both methods.

You can choose which partitioning scheme to use in `partition`.
We recommend the one taught in the course (see the course material and Spoiler 2 below).

Note the call to `selector.pivotIndex` in `partition`.
The class `Quicksort` is parameterized over a *pivot selection strategy* (interface `QuicksortPivotSelector`) that is specified in the constructor.
We have already implemented a variety of pivot selection strategies:
* **QuicksortPivotSelector.FIRST**: always pick the first element as pivot,
* **QuicksortPivotSelector.RANDOM**: pick a random pivot,
* **QuicksortPivotSelector.MEDIAN_OF_THREE**: take the median of the first, middle, and last element,
* **QuicksortPivotSelector.ADAPTIVE**: adaptive strategy that takes the size of the range into account.

The same reminders as for insertion sort apply:
* To compare list values, use the stored comparator (`comparator.compare`).
* The first index in the given range is `from` and the last index is `to - 1`.

<details>
<summary>
Spoiler 1
</summary>

Use recursion to sort the left and right parts of the partition in `sort`.
</details>

<details>
<summary>
Spoiler 2
</summary>

The partition scheme of the course works as follows.
First swap the pivot with the first element.
Initialize `lo = from + 1` and `hi = to - 1`.
Advance `lo` forward and `hi` backward while their elements are in the correct position.
Once we reach a conflict on both sides, we swap and advance.
Eventually, `lo` and `hi` cross.
Finally, where should the pivot go?
</details>

<details>
<summary>
Spoiler 3
</summary>

The course book has pseudocode for quicksort.
</details>

Just like `InsertionSort`, the class `QuickSort` has a main method with some basic tests.
Feel free to add your own tests!

### Testing

To test your implementations more fully, run the class `Test` in the `sorting` package.
When it prompts you for parameters, for most of them you can press ENTER — that will make it use the default parameters.
But you should change the following parameter, to enable testing mode:

* *-t: Test the sorting algorithm on lots of small arrays (default: false)?* Type in **true**

In this mode, the program will call the chosen sorting algorithm on random lists of between 0 and 30 elements and check that the lists get sorted correctly.
If it fails, that means you have a bug in your code!
In that case, you will get an error message like this:
```
Building list:  13.4 μs    -->  [0, 1]
Shuffling list: 17.1 μs    -->  [1, 0]
Sorting list:   1.83 μs    -->  [1, 0]
ERROR: not sorted (at position 1): "1" > "0"
ERROR: list is NOT sorted!
```
In the first line above, the program has created the list of integers `[0, 1]`.
In the second line, it shuffles the list randomly, to get `[1, 0]`.
The third line shows the result of calling the chosen sorting algorithm on the shuffled list.
In this case, the result was the list `[1, 0]`, which is not correctly sorted.

Make sure to fix any bugs found by the testing before moving on.

### Benchmarking

Next, you can benchmark your implementation by re-running `Test` without enabling testing mode:

* *-t: Test the sorting algorithm on lots of small arrays (default: false)?*
  Press ENTER here

This will run your chosen sorting algorithm on a list of your chosen size.
Afterwards, the program prints some statistics — the sorting time, the number of comparisons, and the comparison speed.
Mostly, we are interested in the sorting time.

**Explanation of randomness parameter:**
The randomness parameter controls how unordered the array is.
Randomness=0 uses a sorted array of numbers from 0 to N (in order).
Randomness=1 shuffles the array before sorting.
A value in between `0` and `1` partly shuffles the array.
So randomness=0.0001 uses an array where only every 10,000th element has been shuffled.

**Note:**
The `TestSortingAlgorithm` class also accepts parameters as command-line arguments.
For example, you can try the case above by passing the arguments `-n 1,000,000 -r 0.0001`.
Use whichever way you prefer!

### Asymptotic complexity

Finally, use the benchmarking program to answer some questions about the growth rates of your algorithms.
See **answers.txt**, "Part 1".

## Part 2: Building the suffix array

Now that you have played around with sorting algorithms for a while, it's time to build the search index.
Here, you will modify `SuffixArray.java`.

### Background: Suffix arrays

We want to search for arbitrary strings of characters in a text.
A very nice data structure called a *suffix array* can be used as a search index for this problem.
In this section, imagine that we want to build a search index for the text "then the three horses are through there".
A *suffix* is a substring of the text that starts at some position and goes all the way to the end of the text.
For example, "are through there" is a suffix of the text above.

Conceptually, a suffix array consists of *all suffixes* of the text, sorted alphabetically.
Here are some suffixes of our example text, together with the position (in characters) where each one starts.
(There are also suffixes that start at other characters, for example the middle of a word or a blank, but we ignore those just for this example.)
We write it as an array of pairs of the form `(position, suffix)`:

```
{( 0, "then the three horses are through there"),
 ( 5, "the three horses are through there"),
 ( 9, "three horses are through there"),
 (15, "horses are through there"),
 (22, "are through there"),
 (26, "through there"),
 (34, "there")}
```

The suffix array for the text is conceptually just this list of suffixes sorted alphabetically:

```
{(22, "are through there"),
 (15, "horses are through there"),
 (5,  "the three horses are through there"),
 (0,  "then the three horses are through there"),
 (34, "there"),
 (9,  "three horses are through there"),
 (26, "through there")}
```

Now, how can we find a specific string in the text?
It turns out that *we can use binary search on the suffix array!*
For example, suppose we want to find all occurrences of "the" (including words such as "then" and "there" which start with "the").
Can you see how to do this?

Here is the idea:

* There are three occurrences of "the" ("then...", "the three..." and "there").
  Put another way, there are three suffixes that *start with* "the" (positions 0, 5, and 34).
* In alphabetical order, these suffixes must all be `>= "the"` and e.g. `< "thf"`.
  So they must come together in the suffix array (as you can see above), in one "block": `(5, "the three...")`, `(0, "then the...")`, `(34, "there")`.
* In fact, the suffix array remains sorted when we restrict each suffix to just the first three characters (same length as "the").
* We can use binary search for "the" in this restricted suffix array!
  The first occurrence will be the start of the "block" and the last occurrence will be the end of the "block".
  Everything in between is a match.

Make sure you understand this before going on!

There is **one more important detail**.
If the text consists of N characters, the suffix array will consist of N strings, ranging from the whole text to a single character at the end.
If we would store the array like that, it would use up an enormous amount of memory (quadratic in N) — but we don't have to store it like that.
Instead the suffix array is an array of integers, which are positions in the text.
This array is then sorted — not numerically by position, but alphabetically by the substring starting at that position.
So, for the text above, we start with the array of positions `{0, 5, 9, 15, 22, 26, 34}`, and sort it to get `{22, 15, 5, 0, 34, 9, 26}`.

In Java, we achieve the custom sorting of suffix start positions by defining our own comparator.
Below, we use suffixes that start **at any character**, not just at the start of the word.
For example, this makes it possible to search for character sequences in the middle of a word.

### Example texts

The `texts` directory has example text files for you to index.
They are generated from the British National Corpus (BNC), a large collection of English-language text collected from all kinds of spoken and written sources:

| Text file                 | Sentences | Words      | Characters  |
|---------------------------|-----------| -----------|-------------|
| `texts/bnc-miniscule.txt` |       200 |     ≈1,000 |      ≈5,000 |
| `texts/bnc-tiny.txt`      |     2,000 |    ≈21,000 |    ≈100,000 |
| `texts/bnc-mini.txt`      |    20,000 |   ≈400,000 |  ≈2,100,000 |
| `texts/bnc-small.txt`     |    60,000 | ≈1,000,000 |  ≈5,700,000 |
| `texts/bnc-medium.txt`    |   200,000 | ≈3,500,000 | ≈20,000,000 |
| `texts/bnc-large.txt`     |   600,000 | ≈9,700,000 | ≈57,000,000 |

If you want to test your code on even larger files, you can [download them here](https://github.com/ChalmersGU-data-structure-courses/big-data/tree/main/plain-text-corpora/bnc):

| Text file                | Sentences | Words       | Characters   |
|--------------------------|-----------| ------------|--------------|
| `texts/bnc-huge.txt`     | 2,000,000 |  ≈3,500,000 | ≈210,000,000 |
| `texts/bnc-full.txt`     | 6,071,889 | ≈97,000,000 | ≈570,000,000 |

Those are stored compressed in `.gz` format.
You have to decompress them to be able to search them using your implementation.

### The class `Text`

The class `Text` represents the text we want to index.
It is already implemented for you.
Its constructor takes a path to a text file to read in.
Useful methods:
* `suffix(int start)` returns the suffix starting at `start`.
* `substring(int start, int end)` returns the substring from `start` (inclusive) to `end` (exclusive).
  This constructs a new string, so should only be used for small substrings.
* `suffixComparator(boolean ignoreCase)` returns a comparator that compares suffix start positions by looking at the suffix string.
* `printKeywordInContext` for nicely printing a found match.
  This will be useful in Part 3.

### The class `SuffixArray`

The class `SuffixArray` represents the search index.
Apart from the attribute `text` for the text to search, it has a list `sortedSuffixStarts` of integers that represents the sorted suffix array.

**Note:**
You may have noticed the strange type `IOIntArray` of `sortedSuffixStarts`.
That's a custom implementation of `List<integer>` tuned for writing to and reading from disk.
We cache the suffix array on disk so that we do not have to reconstruct the suffix array every time we want to make a query.
Note that `IOIntArray` only supports `get` and `set`, not `add` or `remove`.
(That's all you need to know about it.)

The core methods of `SuffixArray` are as follows:
* `build()`:
  Builds the search index.
  To be implemented in this part.
* `writeToDisk()` (after callling `build()`):
  Writes the built search index to disk.
  The filename used is that of the text file, but with suffix `.txt` replaced by `.ix`.
* `readFromDisk()` (instead of `build()`):
  Reads a previously built search index from disk.
  Much faster  than building it from scratch.
* `sortedSuffixes` and `searchForKey`: see Part 3.

There also is a main method.
Feel free to experiment with the `SuffixArray` class there!

**Note:**
The class `SuffixArray` has a static constant `IS_CASE_INSENSITIVE` that determines if capitalization of letters should matter when looking for search matches.
A case sensitive search index will only work correctly for case sensitive search queries.
So if you change this constant, remember to rebuild your suffix arrays!

### Implementing `build()`

This method is responsible for filling in the list `sortedSuffixStarts`.
The two main parts are missing.

First, all suffix start positions need to be filled in `sortedSuffixStarts`:
```java
Util.printTiming("initializing suffix array", () -> {
    sortedSuffixStarts = new IOIntArray(text.size());

    // Write all the possible suffix starts into `sortedSuffixStarts` (not yet sorted).
    throw new UnsupportedOperationException(); // TODO: implement
});
```
These are just all positions of characters in the text (of which there are `text.size()` many).

Second, the suffix start positions need to be sorted according to the encoded suffix:
```java
double time = Util.printTiming("Sorting suffix array", () -> {

    // Construct and call one of your sorting algorithms.
    throw new UnsupportedOperationException(); // TODO: implement

});
```
Here, you should choose one of the sorting algorithms you have implemented.
Preferably, one with good order of growth!
Construct an instance and use it to sort the suffix array.
Use `counting` as the comparator; it wraps the suffix comparator of the `Text` instance using a comparison counter.

You can experiment with using different sorting algorithms.
(In fact, there are a few questions in **answers.txt** about that.)

### Testing and benchmarking

`BuildSuffixArray` is a simple wrapper program to build a search index for a given text file and write it to disk.
It will report an error if your sorting algorithm for some reason does not sort the suffix array correctly.
It also prints some useful statistics, most importantly the time it took to sort the suffix array.

### Questions to answer

See **answers.txt**, "Parts 2 and 4".
Answer only the part of the questions that deal with insertion sort and quicksort.
You will implement Multi-key quicksort in Part 4.

## Part 3: Searching using the suffix array

Here, you will implement the method
```java
void searchForKey(String searchKey, int maxNumMatches, int context, boolean trimLines)
```
of `SuffixArray`.
It is used to perform search queries using the search index built in the previous part.

The arguments have the following meaning:
 * `searchKey`:
   The string key to search for.
   Can be anything between a character, a word, a sequence of words, and a whole sentence.
 * `maxNumMatches`:
   Report at most these many matches.
 * `context`:
   How many characters to print to the left and right of each match.
 * `trimLines`:
   Whether to restrict the context to the same line(s) as the match.

Do not worry about all these parameters!
Mostly, you are going to pass them off to existing helper methods such as the method `printKeywordInContext` of `text`.
The ones you need to think about are `searchKey` and `maxNumMatches`.

The strategy for the search is as follows.
First of all, we only consider as many characters of each suffix as in the search key.
That's the puropose of this line constructs:
```
final List<String> sortedSuffixes = sortedSuffixes(searchKey.length());
```
Here, `sortedSuffixes` is the sorted suffix array, but with each suffix restricted to `searchKey.length()` characters.
Note that the list returned by `sortedSuffixes(int maxLength)` constructs these restricted strings on-demand in the `get` method — that saves spaces.
This is called a *view pattern*.

Second, we use binary search to determine the interval of matching suffices.
For this, make use of `findIntervalStart` and `findIntervalEnd` in `util.BinarySearch`.
(Alternatively, you can reuse your work from the binary search lab.)
The comparator to use is provided already in the method:
```
final Comparator<String> c = IS_CASE_INSENSITIVE ? String.CASE_INSENSITIVE_ORDER : Comparator.naturalOrder();
```

Remember that the list `sortedSuffixStarts` has the starting position for each suffix.

Requirements are as follows:
* Report each match using `text.printKeywordInContext`.
  The `start` and `end` arguments are positions in the text file specifying where the match it.
* If there are no matches, print the line "[no matches found]".
* If there are more matches than `maxNumMatches`, stop printing them with a line like "[17 matches omitted]".
* Do not use linear search.

### Testing and benchmarking

Now you can finally reap the fruits of your labor.
Execute `RunSearchPrompt` with a text file for which you have previously built an index.

If everything went well, you should now have a blazingly fast text search interface to the corpus you selected.
If it takes longer than a hundredth of a second, you probably have some bug in your code.

Here are some concrete test cases for **`texts/bnc-medium.txt`**.

There should be exactly 9 matches for the string "Chalmers":
```
Enter search key (CTRL-C/D/Z to quit): Chalmers
Searching for "Chalmers":
 6672970: ottish Business Biography , 1986.] Neil |Chalmers| , Sir Sidney Frederic (1862–1950), zool
 8594918:  the Treasury, Sir Robert (later Baron) |Chalmers| [q.v.], and he countermanded on his own
 8986636: the religious and social work of Thomas |Chalmers| [q.v.]in Glasgow and then moved to Lond
17487174: . It makes me jump every time. Kind Mrs |Chalmers| holds the prayer book high, as if for F
17486677: dly and late. And Pa keeps glaring. Mrs |Chalmers| is standing beside me, tutting and shif
17488286: urns and shouts, ‘Name this child.’ Mrs |Chalmers| lashes back with her heel, simultaneous
 8989078: ade in Great Britain, 1829–1860 , 1991; |Chalmers| papers, New College, Edinburgh; informa
 9500437:  The Gold Mines of the Rand (with J. A. |Chalmers|, 1895);The Geology of South Africa (wit
17323609: disappears inside. Although neither Mrs |Chalmers|, the light-bulb lady, nor old Frank who
2.54 ms
```
The order of the results may be different for you.

You can also search for multiple-word strings:
```
Enter search key (CTRL-C/D/Z to quit): computer science
Searching for "computer science":
 6908411:  logic, but also within linguistics and |computer science|. He had apparently endless energy, prod
17857095: grows. Not only in biotechnology and in |computer science|; even in archaeology and fine art, we s
 3581811: gy but, drawing on the then equally new |computer science|s,information was what mattered. Control
977 μs
```

Or for parts of words:
```
Enter search key (CTRL-C/D/Z to quit): hat
Searching for "hat":
 2585834: o not know it, and they are like me in t|hat| (I know it, and do not know it) and lik
  127555: hm. But you had to take all that worry t|hat| But there was  somebody wasn't going to
   73880: out  carseckies ? Oh aye I've heard    t|hat| Er and  yes. the grazer jacket. That's
 2561003: tariat: The hungry in their long lines t|hat| Gangling around two sides of city block
 4510967: ’ I don't know what he meant but after t|hat| He had a different look, much more rela
    5363: imes D U by D X. Well if U is equal to t|hat| Mhm. we can differentiate that with res
    3259: s  like the the log erm and what  like t|hat| Mm. each of the two X multiplied by sin
   14083: h. Right  Okay one way differentiating t|hat| Mm. is multiply it out. You've got five
    2137: se  Erm  because that is a function of t|hat| Right. I've differentiated that. Well I
 4838564: rican development. Michael Löwy argues t|hat| [As a]professional leadership group, or
[40186 matches omitted]
1.10 ms
```

Wonderful, isn't it?
Note how fast the query is!
You could do almost a thousand queries per second.
And most of the query time is probably just used for printing the results.

* The first number that is printed on each line is the position of each search result.
  As you can see these numbers are not ordered, but they seem to be random.
  Why do you think the results are not shown in increasing order of position?

### Questions to answer

See `answers.txt`, "Part 3".

## Part 4: Multi-key quicksort

Using quicksort to build the search index is already quite fast, but we are going to make it even faster.
You will implement a version of quicksort that is particularly fast at sorting a list of sequences *in lexicographic order*.
Our use case for this is sorting a list of strings (which are sequences of characters) in alphabetic order.

### Lexicographic order

Suppose we have a type T with an ordering.
This induces an ordering on the sequences of type T: given sequences x and y, we just look for the first difference between them.
For example, if x is [4, 1, 7, 6] and y is [4, 1, 6, 8], then x > y because the first position where the sequences differ is index 2, and x[2] = 7 is bigger than y[2] = 6.

The abstract class **LexicographicComparator** in the package **sorting** implements a Java comparator based on this principle.
It has the following abstract method:
```java
// Compare x and y at position k.
public abstract int compare(E x, E y, int k);
```
The comparator method `compare(x, y)` is then defined in terms of the above method: we loop over all positions k, starting from 0, until `compare(x, y, k)` tells us there is a difference (a non-zero comparison value, which we return).
We call this a *lexicographic comparator*.

Note that we do not actually assume that E is a sequence type in this presentation.
In particular, when we have x of type E, we cannot actually access position k.
But that won't be needed.
Comparing elements of type E at position k will be enough, whatever "at position k" means.

**Note:**
We make two simplifying asumptions:
* All positions are valid (i.e., we never get an out-of-bounds error).
* If `x` and `y` compare equal at all positions, they are equal.

The class **CharSequenceComparator** implements **CharSequenceComparator** with the position-based comparison method expected from strings: we simply compare the k-th characters.
In fact, this is exactly what the predefined comparator for strings does.
So this implements the alphabetic ordering.

**Note:**
The constructor of **CharSequenceComparator** can be given a boolean flag that decides if capitalization of characters should be ignored.
We do not need this for this part.

### Multi-key quicksort

Multi-key quicksort is a version of quicksort optimized for types with a lexicographic ordering.
You will implement it in **MultiKeyQuicksort**.
Of note, the `comparator` attribute is now an instance of `LexicographicComparator`.
So given elements x and y, we can compare them at any position k of our choosing.

There are two main differences over plain quicksort:

* We never compare elements fully.
  Instead, we only ever compare them at some position.
  Initially, we compare elements in their first positions.
  But as the algorithm goes on, we will start comparing elements also in their second positions, third positions, and so on.

* Instead of partitioning into a left part and a right part, we partition into *three parts*.
  The middle part will contain all the elements that compare equal to the pivot (at the chosen comparison position).

The pivot selection step works the same as in quicksort.

Initially, we partition the array by comparing their first positions (position 0).
This gives us three parts:
* the left part has elements with first position smaller than the pivot,
* the middle part has elements with same first position as the pivot.
* the right part has elements with first position larger than the pivot.

Just like in quicksort, we handle the left and right part recursively, calling the same method with a smaller range.
But for the middle part, we can do something more efficient.
Since we already know that all elements in the middle part have the same first position, we can move on to comparing the *second positions*.

To be able to recurse also in this case, we need to add a *comparison position* parameter (called `position` in `MultiKeyQuicksort`) to the sorting method.
Then the general pattern is:
* For the left and right part, we keep the same comparison position in the recursive call.
* For the middle part, we increase the comparison position by one.

That leaves just one quetion: how do we partition into three parts?
This should be in-place, i.e., not use a helper array.
Feel free to look at some of the below spoilers if you are out of ideas.

<details>
<summary>
Spoiler 1
</summary>

Start as usual by swapping the pivot with the first element of the range.
</details>

<details>
<summary>
Spoiler 2
</summary>

Let's call the current range `from` and `to`.
Eventually, we want a range `middleFrom` and `middleTo` for the middle part of the partition.
This should include the pivot.
The range from `from` and to `middleFrom` will be the left part and the range from `middleTo` to `to` will be the right part.
</details>

<details>
<summary>
Spoiler 3
</summary>

Initialize `middleFrom = from` and `middleTo = to`.
You need to traverse all the elements from `middleFrom` and `middleTo`, compare each to the pivot in the position under consideration, and depending on the result swap it into its parts of the partition.
We will update to update some of the variables such as `middleFrom` and `middleTo` to account for changes in the partition sizes.
</details>

<details>
<summary>
Spoiler 4
</summary>

Say we use `i` as the index to start at `middleFrom`.
(Actually, we can start at `middleFrom + 1`. Why?)
We process the element at `i` until we reach `i == middleTo`.
Inside the loop, the ranges have to following meaning:
- from `from` to `middleFrom`: the left part so far
- from `middleFrom` to `i`: the middle part so far
- from `i` to `middleTo`: still to be processed
- from `middleTo` to `to`: the right part so far
</details>

<details>
<summary>
Spoiler 5
</summary>

Suppose we process the element at `i`.
Let `c = compare(list.get(i), pivot, position)`.
What we should do depends on `c`:
* If `c < 0`, the element belongs in the left part.
* If `c == 0`, the element belongs to the middle part.
* If `c > 0`, the element belongs to the right part.

In each case, we may use a swap to update the three parts of the partition so far with the new element.
How do the variables `middleFrom`, `middleTo`, `i` change?
</details>

### Testing

Again, the main method in `MultiKeyQuicksort` has some small test cases you can experiment with.
You can test and benchmark multi-key quicksort with the program `sorting.Test`, in the same way as you did for quicksort and insertion sort.
Make sure to fix any bugs found by the testing before moving on.

### Faster index building?

Modify the `build()` method of `SuffixArray` to use multi-key quicksort.
Is building a search index now any faster than with regular quicksort?
Again, `BuildSuffixArray` provides you with useful timing data.

### Questions to answer

Finish the remaining questions in **answers.txt**, "Parts 2 and 4".

## Submission

Double check:
* Have you answered the questions in `answers.txt`?
* Have you tested your code with [Robograder](https://chalmers.instructure.com/courses/23356/pages/the-lab-system#robograder)?

[How to submit](https://chalmers.instructure.com/courses/23356/pages/the-lab-system#submit-a-lab).

### Optional tasks

* When you run multi-key quicksort on `bnc-large.txt` or larger, you may experience as stack overflow error.
  Which code path do you think a high level of nested calls comes from?
  How can you fix this?

* When you run multi-key quicksort on a list that contains duplicates, your code will run forever or cause a stack overflow error.
  Fortunately, this can never happen for a suffix array (why?).
  How can you fix your implementation so that it also works with duplicates?

## Literature

- Wikipedia explains [suffix arrays](https://en.wikipedia.org/wiki/Suffix_array)

- Sedgewick & Wayne (2011) has a full chapter on string algorithms (chapter 5), including text searching.
  They even have a section about suffix arrays (in chapter 6 "Context").

- Wikipedia explains [multi-key quicksort](https://en.wikipedia.org/wiki/Multi-key_quicksort).
  But it gives away the pseudocode.
  Try to first build your own version.
