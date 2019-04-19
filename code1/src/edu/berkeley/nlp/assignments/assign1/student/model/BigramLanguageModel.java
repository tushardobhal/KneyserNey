/*package edu.berkeley.nlp.assignments.assign1.student.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.assignments.assign1.student.utils.LanguageModelUtils;
import edu.berkeley.nlp.assignments.assign1.student.utils.OpenHashMap;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;

public class BigramLanguageModel implements NgramLanguageModel {

	private UnigramLanguageModel unigramLanguageModel;
	private OpenHashMap bigramMap;
	private OpenHashMap unigramFertilityMap;
	private int totalCount;

	public BigramLanguageModel(Iterable<List<String>> trainingData) {
		unigramLanguageModel = new UnigramLanguageModel(trainingData);
		
		System.out.println("Building BigramLanguageModel . . .");

		bigramMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		unigramFertilityMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);

		int sent = 0;
		for (List<String> sentence : trainingData) {
			sent++;
			if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);

			for (int i=0; i<stoppedSentence.size()-1; i++) {
				++totalCount;
				int[] index = LanguageModelUtils.index(stoppedSentence.get(i), stoppedSentence.get(i+1));
				long keyIndex = LanguageModelUtils.getIndexesToLong(index);
				bigramMap.increment(keyIndex, 1);
				
				if(bigramMap.get(keyIndex) == 1) {
					unigramFertilityMap.increment(LanguageModelUtils.getIndexesToLong(
							LanguageModelUtils.index(stoppedSentence.get(i))), 1);
				}
			}
		}		

		System.out.println("Done building BigramLanguageModel . . .");
	}

	@Override
	public int getOrder() {
		return 2;
	}

	@Override
	public double getNgramLogProbability(int[] ngram, int from, int to) {
		if (to - from > 2) {
			System.out.println("WARNING - (to - from) exceeds 2 for BigramLanguageModel");
			return 0.0;
		} else if(to - from == 2) {
			long bigramKey = LanguageModelUtils.getIndexesToLong(Arrays.copyOfRange(ngram, from, to));
			long unigramKey = LanguageModelUtils.getIndexesToLong(Arrays.copyOfRange(ngram, from, to-1));
			return (Math.max(0, bigramMap.get(bigramKey) - LanguageModelUtils.DISCOUNT_FACTOR)/unigramLanguageModel.getTotalCount()) 
					+ ((LanguageModelUtils.DISCOUNT_FACTOR/unigramLanguageModel.getTotalCount()) * unigramFertilityMap.get(unigramKey) 
							* unigramLanguageModel.getNgramLogProbability(ngram, from, to-1));
		} else {
			return unigramLanguageModel.getNgramLogProbability(ngram, from, to);
		}
	}

	@Override
	public long getCount(int[] ngram) {
		if (ngram.length > 2)
			return 0;
		else if (ngram.length == 2)
			return bigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
		else
			return unigramLanguageModel.getCount(ngram);
	}

	public int getTotalCount() {
		return totalCount;
	}

}
*/