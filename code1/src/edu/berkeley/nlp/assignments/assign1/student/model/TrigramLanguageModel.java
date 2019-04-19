package edu.berkeley.nlp.assignments.assign1.student.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.assignments.assign1.student.utils.LanguageModelUtils;
import edu.berkeley.nlp.assignments.assign1.student.utils.OpenHashMap;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;

public class TrigramLanguageModel implements NgramLanguageModel {
	
	private BigramLanguageModel bigramLanguageModel;
	
	private OpenHashMap trigramMap;
	private OpenHashMap bigramFertilityMap;
	
	private long totalCount;
	
	public TrigramLanguageModel(Iterable<List<String>> trainingData) {
		bigramLanguageModel = new BigramLanguageModel(trainingData);
		
		System.out.println("Building TrigramLanguageModel . . .");
		
		trigramMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		bigramFertilityMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		
		int sent = 0;
		for (List<String> sentence : trainingData) {
			sent++;
			if (sent % 1000000 == 0) 
				System.out.println("On sentence " + sent);
			
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);

			for (int i=0; i<stoppedSentence.size()-2; i++) {
				int[] index = LanguageModelUtils.index(stoppedSentence.get(i), 
						stoppedSentence.get(i+1), stoppedSentence.get(i+2));
				long keyIndex = LanguageModelUtils.getIndexesToLong(index);
				trigramMap.increment(keyIndex, 1);
				
				if(trigramMap.get(keyIndex) == 1) {
					bigramFertilityMap.increment(LanguageModelUtils.getIndexesToLong(
							LanguageModelUtils.index(stoppedSentence.get(i), stoppedSentence.get(i+1))), 1);
				}
				++totalCount;	
			}	
		}
		System.out.println("Done building TrigramLanguageModel . . .");
	}
		
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
			long trigramKey = LanguageModelUtils.getIndexesToLong(Arrays.copyOfRange(ngram, from, to));
			long bigramKey = LanguageModelUtils.getIndexesToLong(Arrays.copyOfRange(ngram, from, to-1));
			
			return (Math.max(0.0, trigramMap.get(trigramKey) - LanguageModelUtils.DISCOUNT_FACTOR)/totalCount) 
					+ ((LanguageModelUtils.DISCOUNT_FACTOR/totalCount) * bigramFertilityMap.get(bigramKey) 
							* bigramLanguageModel.getNgramLogProbability(ngram, from, to-1));
		} else {
			return bigramLanguageModel.getNgramLogProbability(ngram, from, to);
		}
		
	}

	@Override
	public long getCount(int[] ngram) {
		if(ngram.length > 3)
			return 0;
		if(ngram.length == 3)
			return trigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
		else 
			return bigramLanguageModel.getCount(ngram);
	}

}
