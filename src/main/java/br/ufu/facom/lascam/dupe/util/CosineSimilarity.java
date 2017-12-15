package br.ufu.facom.lascam.dupe.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

public class CosineSimilarity {
	public class values {
		int val1;
		int val2;

		values(int v1, int v2) {
			this.val1 = v1;
			this.val2 = v2;
		}

		public void updateValues(int v1, int v2) {
			this.val1 = v1;
			this.val2 = v2;
		}
	}// end of class values

	public double getCosineSimilarityScore(String word1, String word2) {
		double cosScore = 0.0000000;
		// 1. Identify distinct words from both documents
		String[] word_seq_text1 = word1.split(" ");
		String[] word_seq_text2 = word2.split(" ");
		Hashtable<String, values> wordFreqVector = new Hashtable<String, values>();
		LinkedList<String> bagOfWords = new LinkedList<String>();

		// prepare word frequency vector by using Text1
		for (int i = 0; i < word_seq_text1.length; i++) {
			String tmp_wd = word_seq_text1[i].trim();
			if (tmp_wd.length() > 0) {
				if (wordFreqVector.containsKey(tmp_wd)) {
					values vals1 = wordFreqVector.get(tmp_wd);
					int freq1 = vals1.val1 + 1;
					int freq2 = vals1.val2;
					vals1.updateValues(freq1, freq2);
					wordFreqVector.put(tmp_wd, vals1);
				} else {
					values vals1 = new values(1, 0);
					wordFreqVector.put(tmp_wd, vals1);
					bagOfWords.add(tmp_wd);
				}
			}
		}

		// prepare word frequency vector by using Text2
		for (int i = 0; i < word_seq_text2.length; i++) {
			String tmp_wd = word_seq_text2[i].trim();
			if (tmp_wd.length() > 0) {
				if (wordFreqVector.containsKey(tmp_wd)) {
					values vals1 = wordFreqVector.get(tmp_wd);
					int freq1 = vals1.val1;
					int freq2 = vals1.val2 + 1;
					vals1.updateValues(freq1, freq2);
					wordFreqVector.put(tmp_wd, vals1);
				} else {
					values vals1 = new values(0, 1);
					wordFreqVector.put(tmp_wd, vals1);
					bagOfWords.add(tmp_wd);
				}
			}
		}

		// calculate the cosine similarity score.
		double vectorAB = 0.0000000;
		double vectA_Sq = 0.0000000;
		double vectB_Sq = 0.0000000;

		for (int i = 0; i < bagOfWords.size(); i++) {
			values vals12 = wordFreqVector.get(bagOfWords.get(i));

			double freq1 = (double) vals12.val1;
			double freq2 = (double) vals12.val2;
			//System.out.println(distinct_words_text_1_2.get(i) + "#" + freq1 + "#" + freq2);

			vectorAB = vectorAB + (freq1 * freq2);

			vectA_Sq = vectA_Sq + freq1 * freq1;
			vectB_Sq = vectB_Sq + freq2 * freq2;
		}
		//System.out.println("VectAB " + VectAB + " VectA_Sq " + VectA_Sq + " VectB_Sq " + VectB_Sq);
		cosScore = ((vectorAB) / (Math.sqrt(vectA_Sq) * Math.sqrt(vectB_Sq)));
		wordFreqVector = null;
		bagOfWords = null;
		return (cosScore);
	}

	public static void main(String[] args) throws IOException, ParseException {
				
		//System.out.println("Cosine similarity score = " + getCosineSimilarity("As Julia points out Sujit here example is very useful", "As aa points out Sujit Pal example is very asgardddddd"));
		
		//System.out.println(cosineSimilarity("Java comparison with == of two strings is false?", "How do I compare strings in Java?"));
		
		String s1 = "can anyon objective-c mona 3333";
		String s2 = "can anyon sss mona 3333";
		
		
		String tag1 = "objective-c ios iphone";
		String tag2 = "objective-c"; 
		
		
		
		System.out.println(getCosineSimilarity(tag1,tag2));
		
		
	}
	
	public static double getCosineSimilarity(String string1, String string2) throws IOException, ParseException {
		CosineSimilarity cs1 = new CosineSimilarity();
		double sim_score = cs1.getCosineSimilarityScore(string1,string2);				
		cs1 = null;		
		return sim_score;
	}
	
	
	
	public static double cosineSimilarity(double[] vectorA, double[] vectorB) throws Exception {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    if(vectorA.length!=vectorB.length){
	    	throw new Exception("Tamanho de vetor A:"+vectorA.length+" é diferente de tamanho de vetor B: "+vectorB.length);
	    }
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    if(normA==0d || normB== 0d){
	    	return 0d;
	    }
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	 public static Map<String, Integer> getTermFrequencyMap(String[] terms) {
	        Map<String, Integer> termFrequencyMap = new HashMap<>();
	        for (String term : terms) {
	            Integer n = termFrequencyMap.get(term);
	            n = (n == null) ? 1 : ++n;
	            termFrequencyMap.put(term, n);
	        }
	        return termFrequencyMap;
	    }

	    /**
	     * @param text1 
	     * @param text2 
	     * @return cosine similarity of text1 and text2
	     */
	    public static double cosineSimilarity(String text1, String text2) {
	        //Get vectors
	        Map<String, Integer> a = getTermFrequencyMap(text1.split("\\W+"));
	        Map<String, Integer> b = getTermFrequencyMap(text2.split("\\W+"));

	        //Get unique words from both sequences
	        HashSet<String> intersection = new HashSet<>(a.keySet());
	        intersection.retainAll(b.keySet());

	        double dotProduct = 0, magnitudeA = 0, magnitudeB = 0;

	        //Calculate dot product
	        for (String item : intersection) {
	            dotProduct += a.get(item) * b.get(item);
	        }

	        //Calculate magnitude a
	        for (String k : a.keySet()) {
	            magnitudeA += Math.pow(a.get(k), 2);
	        }

	        //Calculate magnitude b
	        for (String k : b.keySet()) {
	            magnitudeB += Math.pow(b.get(k), 2);
	        }

	        //return cosine similarity
	        return dotProduct / Math.sqrt(magnitudeA * magnitudeB);
	    }
	    
		
		
	
}