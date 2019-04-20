package edu.berkeley.nlp.assignments.assign1.student.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.assign1.student.utils.LanguageModelUtils;
import edu.berkeley.nlp.assignments.assign1.student.utils.OpenHashMap;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;

public class TrigramLanguageModel implements NgramLanguageModel {
	private OpenHashMap unigramMap;
	private OpenHashMap bigramMap;
	private OpenHashMap trigramMap;
	private OpenHashMap preBigramFertilityMap;
	private OpenHashMap postBigramFertilityMap;
	private OpenHashMap prePostUnigramFertilityMap;
	
//	private OpenHashSet unkTokensKeys;
	
	private long totalUnigramCount;
		
	public TrigramLanguageModel(Iterable<List<String>> trainingData) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm:ss");
		
		System.out.println("Building TrigramLanguageModel at time " + df.format(LocalDateTime.now()));
		
		unigramMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		bigramMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		trigramMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		preBigramFertilityMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		postBigramFertilityMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		prePostUnigramFertilityMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		
/*		unkTokensKeys = new OpenHashSet(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		preprocessTokens(trainingData);*/
		
		int sent = 0;
		for (List<String> sentence : trainingData) {
			if (sent % 1000000 == 0) 
				System.out.println("On sentence " + sent + " at time " + df.format(LocalDateTime.now()));
			sent++;
			
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);
			
			int index1 = 0, index2 = 0, index3 = 0;
			for (int i=0; i<stoppedSentence.size(); i++) {
				++totalUnigramCount;
				if(i == 0)
					index1 = LanguageModelUtils.index(stoppedSentence.get(i));
				unigramMap.increment(LanguageModelUtils.getIndexesToLong(index1), 1);
				
				if(i < stoppedSentence.size() - 1) {
					if(i == 0)
						index2 = LanguageModelUtils.index(stoppedSentence.get(i+1));
					long bigramKeyIndex = LanguageModelUtils.getIndexesToLong(index1, index2);
					bigramMap.increment(bigramKeyIndex, 1);
					
					if(i < stoppedSentence.size() - 2) {
						index3 = LanguageModelUtils.index(stoppedSentence.get(i+2));
						long trigramKeyIndex = LanguageModelUtils.getIndexesToLong(index1, index2, index3);
						int currCount = trigramMap.increment(trigramKeyIndex, 1);

						if(currCount == 1) {
							preBigramFertilityMap.increment(LanguageModelUtils.getIndexesToLong(index2, index3), 1);
							postBigramFertilityMap.increment(bigramKeyIndex, 1);
							prePostUnigramFertilityMap.increment(LanguageModelUtils.getIndexesToLong(index2), 1);
						}	
					}
				}
				index1 = index2;
				index2 = index3;
			}
		}
		System.out.println("unigramMap - " + unigramMap.size() + ", bigramMap - " + bigramMap.size()
				+ ", preBigram - " + preBigramFertilityMap.size() + ", postBigram - " + postBigramFertilityMap.size()
				+ ", prePost - " + prePostUnigramFertilityMap.size() + ", trigramMap - " + trigramMap.size());
		System.out.println("Done building TrigramLanguageModel . . .");
	}
	
	/*private void preprocessTokens(Iterable<List<String>> trainingData) {
		for (List<String> sentence : trainingData) {
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);
			
			for (String word : stoppedSentence) {
				unigramMap.increment(LanguageModelUtils.getIndexesToLong(LanguageModelUtils.index(word)), 1);
			}
		}
		for()
	}*/
		
	@Override
	public int getOrder() {
		return 3;
	}
	
	@Override
	public double getNgramLogProbability(int[] ngram, int from, int to) {
		if(to - from > 3) {
			System.out.println("WARNING - (to - from) exceeds 3 for TrigramLanguageModel");
			return 0.0;
		} else if(to - from == 3) {
			int totalCount = Math.max(1, bigramMap.get(LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1])));
			return Math.log( 
						(Math.max(0.0, trigramMap.get(LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1], ngram[from+2])) - 
								LanguageModelUtils.DISCOUNT_FACTOR)/totalCount)
						+ (
							(LanguageModelUtils.DISCOUNT_FACTOR/(totalCount)) * 
							Math.max(LanguageModelUtils.EPSILON, postBigramFertilityMap.get(LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1]))) * 
							Math.max(LanguageModelUtils.EPSILON, preBigramFertilityMap.get(LanguageModelUtils.getIndexesToLong(ngram[from+1], ngram[from+2]))) / 
							Math.max(1, prePostUnigramFertilityMap.get(LanguageModelUtils.getIndexesToLong(ngram[from+1])))
						  )
					);
		} else if(to - from == 2) {
			return Math.log(
						Math.max(LanguageModelUtils.EPSILON, preBigramFertilityMap.get(LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1]))) / 
						Math.max(1, prePostUnigramFertilityMap.get(LanguageModelUtils.getIndexesToLong(ngram[from])))
					);
		} else {
			return Math.log(unigramMap.get(LanguageModelUtils.getIndexesToLong(ngram))/(totalUnigramCount));
		}
	}

	@Override
	public long getCount(int[] ngram) {
		if(ngram.length > 3)
			return 0;
		if(ngram.length == 3)
			return trigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
		else if(ngram.length == 2) 
			return bigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
		else
			return unigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
	}

}
