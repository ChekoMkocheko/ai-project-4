/*
 * Students: Alivia Kliesen and Cheko Mkocheko
 * Outside Sources of Help: https://countwordsfree.com/stopwords (original txt file list of stop words, modified by Alivia and Cheko for this assignment)
 * https://aclanthology.org/W18-0541.pdf (ngrams inspiration)
 */

import java.io.*;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SentAnalysisBest {

	final static String TRAINDIR = "./train"; // directory where training data is located
    	final static int MAXNGRAMSIZE = 5; // maximum size of a nGram
	final static int MINNGRAMSIZE = 3; // minimum size of a nGram
	final static double SMOOTH = .01; 
	final static String STOPWORDSFILE = "stop_words_english.txt";

	public static HashMap<String, Integer> posWordDic = new HashMap<String, Integer>(); // positive
	public static HashMap<String, Integer> negWordDic = new HashMap<String, Integer>(); // negative
	
	public static double numPosReviews = 0.0; // number of positive reviews
	public static double numNegReviews = 0.0; // number of negative reviews
	public static double probPosReview = 0.0; // probability a review is positive (numPosReviews / totalReviews)
	public static double probNegReview = 0.0; // probability a review is negative (numNegReviews / totalReviews)
	public static double uniquePosWords = 0.0; // number of unique words across all positive reviews
	public static double uniqueNegWords = 0.0; // number of unique words across all negative reviews

	public static ArrayList<String> stopWords = new ArrayList<String>(); // neutral words to be excluded from analysis 
		
	public static void main(String[] args) throws FileNotFoundException, IOException
	{			
        trainGrams();
		
		//if command line argument is "evaluate", runs evaluation mode
		if (args.length==1 && args[0].equals("evaluate")){
			evaluate();
		}
		else{//otherwise, runs interactive mode
			@SuppressWarnings("resource")
			Scanner scan = new Scanner(System.in);
			System.out.print("Text to classify>> ");
			String textToClassify = scan.nextLine();
			System.out.println("Result: "+classify(textToClassify));
		}	
	}

	/*
	* Trainer: Reads text from data files in folder datafolder and stores counts 
	* to be used to compute probabilities for the Bayesian formula.
	*/
    public static void trainGrams() throws FileNotFoundException, IOException
	{
        File dir = new File(TRAINDIR);
        File[] dirFiles = dir.listFiles();
	makeStopWords(); // generates list of all stop words 
	System.out.println("Populating files...");

        for(File n: dirFiles){ // iterates through each file from training directory
            boolean type = wordType(n.getName()); // gets whether file is positive or negative review
            BufferedReader br = new BufferedReader (new FileReader(n));
            String st;
    
            while ((st = br.readLine()) != null){ 
               String[] splited = st.replaceAll("[^a-zA-Z ]","").toLowerCase().split("\\s+");
               ArrayList<String> charGramsList = nGramList(splited, MAXNGRAMSIZE);
                if(type){ // if positive review
					numPosReviews++;
                    for(String a : charGramsList){ // iterates through each charGram
                        posWordDic.put(a, posWordDic.getOrDefault(a,0)+1); // update dictionary count
                    }
                }
                else{ // if negative review
					numNegReviews++;
                    for(String a : charGramsList){  
                        negWordDic.put(a, negWordDic.getOrDefault(a,0)+1); // update dictionary count
                    }

                }
            } 
        }

		double totalReviews = numNegReviews + numPosReviews; // total number of reviews in training set
		probPosReview = numPosReviews / totalReviews; // probability a review from training set is positive ie P(Positive)
		probNegReview = numNegReviews / totalReviews; // probability a review from training set is negative ie P(negative)
		uniquePosWords = posWordDic.size(); // total number of unique words in positive dictionary
		uniqueNegWords = negWordDic.size(); // total number of unique words in negative dictionary
	}

	/*
	* Adds all stop words read from a file to a global ArrayList 
	*/
	public static void makeStopWords() throws FileNotFoundException, IOException{
		File n = new File(STOPWORDSFILE);
		BufferedReader br = new BufferedReader (new FileReader(n));
		String st = "";
		while((st=br.readLine()) !=null){ // there is a new stop word to be added 
			stopWords.add(st);
		}
	}

	/* 
	* Given a list of words in a review, removes the stop words and generates a final list of ngrams created from the remaining words 
	* NOTE: all ngrams of size x where 2 <= x <= n are created and added to the final ArrayList of ngrams 
	*/
    public static ArrayList<String> nGramList(String[] review, int n){
        ArrayList<String> nGrams = new ArrayList<String>();
       
        while(n>=MINNGRAMSIZE){ // ngrams are never shorter than the minimum ngram size
            for(int i=0; i<review.length; i++){ // iterates through each word in the review 
				String currWord = review[i];
				if(stopWords.contains(currWord) == false){ // word is not a stop word 
                	nGrams = charGramList(currWord, n, nGrams); // generate charGrams and add to nGrams ArrayList
				}
            }
            n--;
        }
        return nGrams;
    }

	/*
	*Given n and a single word, creates as many n-length chargrams as possible from the word and adds them to an ArrayList of nGrams
	* Ex. if the word was "restuarant" and n=5, then "restu", "estua", "stuar", "tuara", "uaran", and "arant" would be added
	*/
    public static ArrayList<String> charGramList(String word, int n, ArrayList<String> nGrams){
        
        if(word.length() >= n){ // the length of the word is at least as long as the size of the nGram to be created
            for(int i=0; i<word.length()-n+1; i++){ // word.length()-n+1 represents the largest possible start index of a nGram from the given word
                String nGram = word.substring(i, i+n); // creates a single n-length ngram 
                nGrams.add(nGram); 
            }
        }
    
        return nGrams;
    }

	/*
	* wordType: Looks at the name of a file and returns True if the file is a positive review and False if the file is a negative review
	* positive reviews have the format of "subject-1-idNum.txt" 
	*	ex."movies-1-36.txt"
	* negative reviews have the format of "subject-5-idNum.txt" 
	*	ex. "restaurants-5-1909.txt"
	*/
	public static boolean wordType(String fileName){
		boolean ans = true; 

		for(int i = 0; i < fileName.length(); i++){
			if(fileName.charAt(i)=='-'){
				int k = Character.getNumericValue(fileName.charAt(i+1));
				if(k==1){ // negative review 
					ans = false;
				}
				break;
			}
		}
		return ans;
	}

	/*
	 * Classifier: Classifies the input text (type: String) as positive or negative
	 */
	public static String classify(String text)
	{
		String result=""; 

		String[] splited = text.replaceAll("[^a-zA-Z ]","").toLowerCase().split("\\s+");
        ArrayList<String> charGramsList = nGramList(splited, MAXNGRAMSIZE);

        double textProbPos = log2(probPosReview); // initialize the probability a text is positive log2 P(positive)
		double textProbNeg = log2(probNegReview); // initialize the probability a text is negative with log2 P(negative)
		
		double posSmooth = SMOOTH / (uniquePosWords + (uniqueNegWords + uniquePosWords) * SMOOTH); // smoothing value for positive word not in posWordDic
		posSmooth = log2(posSmooth);
		double negSmooth = SMOOTH / (uniqueNegWords + (uniqueNegWords + uniquePosWords) * SMOOTH); // smoothing value for negative word not in negWordDic
		negSmooth = log2(negSmooth);

		for(String word: charGramsList){ // iterates through each charGram from the text
			if(posWordDic.containsKey(word)) { //charGram in dictionary, calculate normal log2 probability 
				double wordProbPos = log2(posWordDic.get(word) / uniquePosWords); 
				textProbPos += wordProbPos;
			}
			else{ //charGram not in dictionary, apply smoothing 
				textProbPos += posSmooth;
			}
			if(negWordDic.containsKey(word)){ // charGram in dictionary, calculate normal log2 probability 
				double wordProbNeg = log2(negWordDic.get(word) / uniqueNegWords);
				textProbNeg  += wordProbNeg;
			}
			else{ // charGram not in dictionary, apply smoothing 
				textProbNeg  += negSmooth;
			}
		}	

		if(textProbPos > textProbNeg){ // higher probability the text is positive
			result = "positive";
		}
		else{ // higher probability the text is negative
			result = "negative";
		}
		
		return result;
	}
	
	/*
	* Log2 Calcuation: Calculate the base 2 logarithm of a double x 
	*/
	public static double log2(double x){
		return Math.log(x) / Math.log(2.0);
	}

	/*
	 * Classifier: Classifies all of the files in the input folder (type: File) as positive or negative
	 */
	public static void evaluate() throws FileNotFoundException , IOException
	{
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		
		System.out.print("Enter folder name of files to classify: ");
		String foldername = scan.nextLine();

		double totalClassified = 0.0; // total number of files
		double totalPosClassified = 0.0; // total number of positive files
		double totalNegClassified= 0.0; // total number of negative files
		double corrPosClassified = 0.0; // total number of correctly classified positive files
		double corrNegClassified = 0.0; // total number of correctly classified negative files 

		String targetFolder = "./" + foldername;
		File folder = new File(targetFolder);
		File[] testFiles = folder.listFiles();

        for(File n: testFiles){
			totalClassified++; // increment for each new file classified 
            boolean type = wordType(n.getName()); // get actual classification of a file (positive or negative)
            BufferedReader br = new BufferedReader (new FileReader(n));
            String st;
			while ((st = br.readLine()) != null){
				String result = classify(st);
				if(type){ // should be positively classified
					totalPosClassified++; // increment for each file that should be positively classified 
					if(result.equals("positive")){ // correct classification by algorithm
						corrPosClassified++;
					}
				}
				else{ // should be negatively classified
					totalNegClassified++; // increment for each file that should be negatively classified
					if(result.equals("negative")){ // correct classification by algorithm
						corrNegClassified++;
					}
				}
			}
		}

		double accuracy = ((corrPosClassified + corrNegClassified) / totalClassified) *100;
		double posPrecision = (corrPosClassified / totalPosClassified)*100; // positive precision
		double negPrecision = (corrNegClassified / totalNegClassified)*100; // negative precision

		System.out.println("Accuracy: " + accuracy);
		System.out.println("Precision (positive): " + posPrecision);
		System.out.println("Precision (negative): " + negPrecision);
	}
}