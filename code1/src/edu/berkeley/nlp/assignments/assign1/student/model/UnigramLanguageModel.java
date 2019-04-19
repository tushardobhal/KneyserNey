package edu.berkeley.nlp.assignments.assign1.student.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.assignments.assign1.student.utils.LanguageModelUtils;
import edu.berkeley.nlp.assignments.assign1.student.utils.OpenHashMap;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;

public class UnigramLanguageModel implements NgramLanguageModel {

	private OpenHashMap unigramMap;
	private int totalCount;
	
	public UnigramLanguageModel(Iterable<List<String>> trainingData) {
		System.out.println("Building UnigramLanguageModel . . .");
		
		unigramMap = new OpenHashMap(LanguageModelUtils.INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		
		int sent = 0;
		for (List<String> sentence : trainingData) {
			sent++;
			if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);
			
			for (String word : stoppedSentence) {
				++totalCount;
				unigramMap.increment(LanguageModelUtils.getIndexesToLong(LanguageModelUtils.index(word)), 1);
			}
		}		
		System.out.println("Done building UnigramLanguageModel . . .");
	}
		
	@Override
	public int getOrder() {
		return 1;
	}

	@Override
	public double getNgramLogProbability(int[] ngram, int from, int to) {
		if(to - from > 1) {
			System.out.println("WARNING - (to - from) exceeds 1 for UnigramLanguageModel");
			return 0.0;
		} else {
			long unigramKey = LanguageModelUtils.getIndexesToLong(Arrays.copyOfRange(ngram, from, to));
			return unigramMap.get(unigramKey)/totalCount;
		}
	}

	@Override
	public long getCount(int[] ngram) {
		if(ngram.length > 1)
			return 0;
		else 
			return unigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
	}
	
}
