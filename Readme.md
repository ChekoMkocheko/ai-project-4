
Group Members: Cheko Mkocheko and Alivia Kliesen

Name of Files:
	SentAnalysis.java
	SentAnalysisBest.java
stop_words_english.txt

Outside Sources of Help:
https://countwordsfree.com/stopwords (original txt file list of stop words,
modified by Alivia and Cheko for this assignment)
https://aclanthology.org/W18-0541.pdf (ngrams inspiration)

Known Bugs:
	Our N-gram algorithm improves the accuracy of our SentimentAnalysis program
  by 5.33% which is still less than the 7.2% improvement suggested by the
  benchmarking solution in the homework. This difference in accuracy is big in
  the precision of classifying negative words. Our program achieves a negative
  precision of 80.78 compared to 87.2% of the benchmark. We think that our negative
  precision is still low because our algorithm does not capture sarcasm and use of
  negation clauses that would cause a sentence that is mostly positive to be negative
  and vice versa.

Part 2:

“Great job to the director who did nothing innovative with this movie!”
This sentence is classified as positive, yet it is negative.
The classifier fails to detect that this is a negative sentence because it is
sarcastic. The word “nothing” negates “innovative” which is a positive word,
making the entire sentence negative although there are two positive words “great” and
“innovative” and only one negative word “nothing”. It also fails to detect that the
exclamation mark contributes to the sentence sounding sarcastic, and therefore
negative..

“I haven't eaten pizza this good before!”
This sentence is classified as negative yet it is positive. The classifier fails
to detect that “haven’t” does not negate the sentence, but is used to enhance the
positivity of the sentence “I have eaten pizza this good before!” which could be a
neutral sentence without the word “haven’t”.

“His speeches are so motivating but they are too long”
This sentence is classified as positive yet it is negative. The classifier fails
to detect that the sentence is made of two clauses “His speeches are so motivating”
which is positive and “ they are too long” which is negative,  joined by the
conjunction “but”. While the former clause is classified correctly, the latter
clause is misclassified because the classifier does not know that when “too” and
“long” follow each other the phrase becomes negative, and when we use “but” in a
sentence, we change the entire sentiment of a sentence to align with the sentiment
of the clause that follows “but”.

Generally, our sentiment analysis fails in situations where words are not explicitly
positive or negative but a certain combination and arrangement of positive,
negative or neutral words could change the sentiment of a sentence from positive
to negative and vice versa.

Accuracy: 76.83
Precision (positive): 78.73
Precision (negative): 74.82

Part 3: Improved Sentiment Analysis

Accuracy: 82.16
Precision (positive): 83.46
Precision (negative): 80.78

We used n-grams to break down words from the input to create new smaller sized
sub-strings that would account for misspelled words and different variations of
the same word (ex. Happily, happy, happiness, etc.). We first removed neutral stop
words such as “you”, “about”, “various”, “gotten”, “many”, “when” etc. which do not
have any impact on the positivity or negativity of a sentence. Our n-gram algorithm
then takes a list of words, and generates all n-grams of sizes MINNGRAMSIZE (3) to
MAXNGRAMSIZE (5) for each word.

For example:
	Sentence: “This movie was insanely terribl.”
	Sentence after removing stop words: “movie insanely terribl”

Size 3 ngrams generated: mov, ovi, vie, ins, nsa, san, ane, nel, ely, ter, err, rri, rib, ibl
Size 4 ngrams generated: movi, ovie, insa, nsan, sane, anel, nelly, terr, erri, rrib, ribl
Size 5 ngrams generated: movie, insan, nsane, sanel, anely, terri, errib, rribl

All ngrams generated: mov, ovi, vie, ins, nsa, san, ane, nel, ely, ter, err, rri,
rib, ibl, movi, ovie, insa, nsan, sane, anel, nelly, terr, erri, rrib, ribl, movie,
insan, nsane, sanel, anely, terri, errib, rribl

Now “insanely” shares more frequency counts in common with other variations
such as “insane” and “insanity” since there are more overlapping character grams
between these 3 words. In our previous unigram model, “insanely” would never have
been linked to “insane” and “insanity” since they are technically separate words
(despite all sharing similar meanings and connotations). Additionally, “terribl”
is misspelled (the correct spelling should be “terrible”). In our previous unigram
model,  “terribl” would have stood as its own separate word (with its own
frequency count) distinct from “terrible” despite it only being one letter off,
but with our n-gram model, “terribl” still shares several n-grams with “terrible”
such as: ter, err, rri, rib, ibl, terr, erri, rrib, ribl,  terri, errib, and rribl
(and these frequency counts are considered in our calculations). 
