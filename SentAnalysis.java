/*
 * Students: Alivia Kliesen and Cheko Mkocheko
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

public class SentAnalysis {

	final static File TRAINFOLDER = new File("./train");
	public static HashMap<String, Integer> posWordDic = new HashMap<String, Integer>();
	public static HashMap<String, Integer> negWordDic = new HashMap<String, Integer>();
	
	public static double numPosReviews = 0.0; // number of positive reviews
	public static double numNegReviews = 0.0; // number of negative reviews
	public static double probPosReview = 0.0; // probability a review is positive
	public static double probNegReview = 0.0; // probability a review is negative
	public static double uniquePosWords = 0.0; // number of unique words across all positive reviews
	public static double uniqueNegWords = 0.0; // number of unique words across all neagtive reviews
		
	public static void main(String[] args) throws FileNotFoundException, IOException
	{		
		train();
		
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
	public static void train() throws FileNotFoundException, IOException
	{
	String target_dir = "./train";
        File dir = new File(target_dir);
        File[] filess = dir.listFiles();
	System.out.println("Populating files...");
        for(File n: filess){
            boolean type = wordType(n.getName());
            BufferedReader br = new BufferedReader (new FileReader(n));
            String st;
            while ((st = br.readLine()) != null){
                String[] splited = st.replaceAll("[^a-zA-Z ]","").toLowerCase().split("\\s+");
                if(type){ // if positive
					numPosReviews++;
                    for(String a : splited){
                        posWordDic.put(a, posWordDic.getOrDefault(a,0)+1); // update dictionary count
                    }

                }
                else{ // if negative
					numNegReviews++;
                    for(String a : splited){
                        negWordDic.put(a, negWordDic.getOrDefault(a,0)+1); // update dictionary count
                    }

                }
            }
			br.close(); 
        }
		double totalReviews = numNegReviews + numPosReviews; // total number of reviews in training set
		probPosReview = numPosReviews / totalReviews; // probability a review from training set is positive ie P(Positive)
		probNegReview = numNegReviews / totalReviews; // probability a review from training set is negative ie P(negative)
		uniquePosWords = posWordDic.size(); // total number of unique words in positive dictionary
		uniqueNegWords = negWordDic.size(); // total number of unique words in negative dictionary
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
				if(k==1){
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

		String[] words = text.replaceAll("[^a-zA-Z ]","").toLowerCase().split("\\s+");
		double textProbPos = log2(probPosReview); // initialize the probability a text is positive log2 P(positive)
		double textProbNeg = log2(probNegReview); // initialize the probability a text is negative with log2 P(negative)
		
		double posSmooth = .0001 / (uniquePosWords + (uniqueNegWords + uniquePosWords) * .0001); // smoothing value for positive word
		posSmooth = log2(posSmooth);
		double negSmooth = .0001 / (uniqueNegWords + (uniqueNegWords + uniquePosWords) * .0001); // smoothing value for negative word
		negSmooth = log2(negSmooth);

		for(String word: words){
			if(posWordDic.containsKey(word)) { //charGram in positive dictionary, calculate normal log2 probability 
				double wordProbPos = log2(posWordDic.get(word) / uniquePosWords); 
				textProbPos += wordProbPos;
			}
			else{ // charGram not in positive dictionary, apply smoothing 
				textProbPos += posSmooth;
			}
			if(negWordDic.containsKey(word)){ //charGram in negative dictionary, calculate normal log2 probability 
				double wordProbNeg = log2(negWordDic.get(word) / uniqueNegWords);
				textProbNeg  += wordProbNeg;
			}
			else{ // charGram not in negative dictionary, apply smoothing 
				textProbNeg  += negSmooth;
			}

		}	

		if(textProbPos > textProbNeg){  // higher probability the text is positive
			result = "positive";
		}
		else{ // higher probability the text is negative
			result = "negative";
		}
		
		return result;
		
	}
	
	/*
	 *  Log2 Calcuation: Calculate the base 2 logarithm of a double x 
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

		double totalClassified = 0.0; // total number of files
		double totalPosClassified = 0.0; // total number of positive files
		double totalNegClassified= 0.0; // total number of negative files
		double corrPosClassified = 0.0; // total number of correctly classified positive files
		double corrNegClassified = 0.0; // total number of correctly classified negative files 
		
		System.out.print("Enter folder name of files to classify: ");
		String foldername = scan.nextLine();

		String targetFolder = "./" + foldername;
		File folder = new File(targetFolder);
		File[] testFiles = folder.listFiles();
        for(File n: testFiles){
			totalClassified++;
            boolean type = wordType(n.getName());  // get actual classification of a file (positive or negative)
            BufferedReader br = new BufferedReader (new FileReader(n));
            String st;
			while ((st = br.readLine()) != null){
				String result = classify(st);
				if(type){ // should be positively classified
					totalPosClassified++;
					if(result.equals("positive")){ // correct classification by algorithm
						corrPosClassified++;
					}
				}
				else{ // should be negatively classified
					totalNegClassified++;
					if(result.equals("negative")){ // correct classification by algorithm
						corrNegClassified++;
					}
				}
			}
			br.close();
			
		}

		double accuracy = ((corrPosClassified + corrNegClassified) / totalClassified) *100;
		double posPrecision = (corrPosClassified / totalPosClassified)*100; // positive precision
		double negPrecision = (corrNegClassified / totalNegClassified)*100; // negative precision

		System.out.println("Accuracy: " + accuracy);
		System.out.println("Precision (positive): " + posPrecision);
		System.out.println("Precision (negative): " + negPrecision);
	}
	
	
	
}
