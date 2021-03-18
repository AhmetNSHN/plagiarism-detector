package com.CMP3005;

import java.io.File;
import java.text.BreakIterator;
import java.util.*;
import java.util.stream.Collectors;
import java.io.FilenameFilter;


public class plagiarism_detector {
	static File fileLocation = null;
    public static int computeEditDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        Map<Character,Character> stringList = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        }else {
                                stringList.put(s1.charAt(i-1),s2.charAt(j-1));
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                       }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        Set<Character> keySet = stringList.keySet();
       List<Character> getValues  = keySet.stream().filter(character -> !character.equals(' ')).collect(Collectors.toList());
       for (Character character : getValues){
           stringBuilder.append(character.toString());
       }       
        return costs[s2.length()];
    }
    

    public static double printDistance(String s1, String s2) {
        double similarityOfStrings = 0.0;
        int editDistance = 0;
        if (s1.length() < s2.length()) { // s1 should always be bigger
            String swap = s1;
            s1 = s2;
            s2 = swap;
        }
        int bigLen = s1.length();
        editDistance = computeEditDistance(s1, s2);
        if (bigLen == 0) {
            similarityOfStrings = 1.0; /* both strings are zero length */
        } else {
            similarityOfStrings = (bigLen - editDistance) / (double) bigLen;
        }
        return similarityOfStrings*100;
    }
    

    public static String fileToString(String filePath){
        Scanner sc = null;
        String input = null;
        StringBuffer sb = null;
        try {
            sc = new Scanner(new File(filePath));
            sb = new StringBuffer();
            while (sc.hasNextLine()) {
                input = sc.nextLine();
                sb.append(" "+input);
            }
        }
        catch(Exception ex) {
            ex.toString();
        }
        return sb.toString();
    }
    
    
    public static void main(String[] args) {
    	
    	String folder = "/Users/ibr.sahin/eclipse-workspace/CMP3005Project/src/Files";// enter path that folder locate
    	String main_path = "main_doc.txt";// name of documant that will be compared with others
    	File path = new File(folder);
        File[] files = path.listFiles(new FilenameFilter() {// take all files except main doc
			public boolean accept(File dir, String name) {
				boolean result;
				if(name.startsWith(main_path)){
					result=false;
				}
				else{
					result=true;
				}
				return result;
			}
		});
        int number_of_files = files.length+1;
        ArrayList<String>[] array_of_arraylist = new ArrayList[number_of_files]; //array to seperate sentences of files 
        double similarity_rate = 0;
		double max_similarity_rate = 0;
		double sentence_index_main = 0;
		double sentence_index_other = 0;
		int start;
		int index_main;
    	int index_other;
    	double sum = 0;

        for (int i = 0; i < number_of_files; i++){// loop for all files
        	array_of_arraylist[i] = new ArrayList<String>();// initializing array list with arrays
        	BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.forLanguageTag("tr-TR"));
        	if (i == 0) {// take all sentences of main_doc
        		String source = fileToString(folder + "/" + main_path);
        		iterator.setText(source);
            	start = iterator.first();
            	for (int end = iterator.next();
                        end != BreakIterator.DONE;
                        start = end, end = iterator.next()) {
            		array_of_arraylist[0].add(source.substring(start,end));// and place them in first index of array
        	    }
        	}
        	else {// compare sentences of main_doc with others
        		double[][] top_similar_sentences = new double[5][3];// array to keep similarity rate of sentences and location of them
        		similarity_rate = 0;
        		max_similarity_rate = 0;
        		sentence_index_main = 0;
        		sentence_index_other = 0;
        		sum = 0;
        		
        		String source = fileToString(folder + "/" + files[i-1].getName());
            	iterator.setText(source);
            	start = iterator.first();
            	for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {//split document into sentences
            		similarity_rate = 0;
        			max_similarity_rate = 0;
        			array_of_arraylist[i].add(source.substring(start,end));// add semtences to array to access them to print result
        			for(int j = 0; j < array_of_arraylist[0].size(); j++) {// compare one sentence taken from other doc with all sentences of main_doc
        				similarity_rate = printDistance(array_of_arraylist[0].get(j), source.substring(start,end));// compare one sentence taken from other doc with all sentences of main_doc
            			if (max_similarity_rate < similarity_rate) {// choose best similar sentence to current sentence
        					max_similarity_rate = similarity_rate;
        					sentence_index_main = j;
        				}
        			}
        			if(top_similar_sentences[0][0] < max_similarity_rate)// keep record of top 5
        			{
        				int x;
        				for(x = 0;  x < 4 && top_similar_sentences[x+1][0] < max_similarity_rate; x++) {
        					top_similar_sentences[x][0] = top_similar_sentences[x+1][0];
        					top_similar_sentences[x][1] = top_similar_sentences[x+1][1];
        					top_similar_sentences[x][2] = top_similar_sentences[x+1][2];
        				}
        				
        				top_similar_sentences[x][0] = max_similarity_rate;
        				top_similar_sentences[x][1] = sentence_index_main;
        				top_similar_sentences[x][2] = sentence_index_other;
        			}
        			sentence_index_other++;
        			sum = sum + max_similarity_rate;// calculate overall similarity by selecting best similar sentence to current sentence (using highest similarity rate for a sentence)

            	}
            	// print results for current document
        		System.out.println("for " + i + ". document ("+ files[i-1].getName() + ") top 5 similar sentences with overall similarity %" + (sum/(array_of_arraylist[i].size())));
        		int x = 1;
        		for(int y = 4; y>=0; y--) {
        			index_main = (int) top_similar_sentences[y][1];
        		    index_other = (int) top_similar_sentences[y][2];
        			System.out.println((x++) + ". Most similar sentences with %"+ top_similar_sentences[y][0]+" rate " + " is " + array_of_arraylist[0].get(index_main)+ "    AND    " + array_of_arraylist[i].get(index_other));
            		
        		}
        	}
        	System.out.println("");
        }
    }
}
        
    
    		
    	     	
            	        	
        
        
        
        

        
    		
    		
    			
    		
    	           	

        	
        	
        				
        		
        			
        				
        				
        			

        			
        		
        		
       
        	

  
