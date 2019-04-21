package edu.berkeley.nlp.assignments.assign1.student.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.assign1.student.utils.BigramTokenIndexer;
import edu.berkeley.nlp.assignments.assign1.student.utils.DirectHashMap;
import edu.berkeley.nlp.assignments.assign1.student.utils.LanguageModelUtils;
import edu.berkeley.nlp.assignments.assign1.student.utils.OpenHashMap;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.TIntOpenHashMap;
import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;

public class TrigramLanguageModel implements NgramLanguageModel {
	
	private BigramTokenIndexer bigramIndexer;
	private int[] unigramMap;
	private int[] bigramMap;
	private OpenHashMap trigramMap;
	
	private int[] preBigramFertilityMap;
	private int[] postBigramFertilityMap;
	private int[] prePostUnigramFertilityMap;
	
	private DirectHashMap cacheMap;
	
	private int totalUnigramCount;
	private int unkTokenIndex;
	private long indexerSize;
	
	DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm:ss");
		
	public TrigramLanguageModel(Iterable<List<String>> trainingData) {
		System.out.println("Building TrigramLanguageModel at time " + df.format(LocalDateTime.now()));
		
		unigramMap = new int[LanguageModelUtils.UNIGRAM_INIT_CAPACITY];
		bigramMap = new int[LanguageModelUtils.BIGRAM_INIT_CAPACITY];
		trigramMap = new OpenHashMap(LanguageModelUtils.TRIGRAM_INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		bigramIndexer = new BigramTokenIndexer(LanguageModelUtils.BIGRAM_INIT_CAPACITY, LanguageModelUtils.LOAD_FACTOR);
		preBigramFertilityMap = new int[LanguageModelUtils.BIGRAM_INIT_CAPACITY];
		postBigramFertilityMap = new int[LanguageModelUtils.BIGRAM_INIT_CAPACITY];
		prePostUnigramFertilityMap = new int[LanguageModelUtils.UNIGRAM_INIT_CAPACITY];
		
		preprocessTokens(trainingData);
		
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
				if(i == 0) {
					index1 = EnglishWordIndexer.getIndexer().indexOf(stoppedSentence.get(i));
					if(index1 == -1)
						index1 = unkTokenIndex;
				}
				unigramMap[index1]++;
				
				if(i < stoppedSentence.size() - 1) {
					if(i == 0) {
						index2 = EnglishWordIndexer.getIndexer().indexOf(stoppedSentence.get(i+1));
						if(index2 == -1)
							index2 = unkTokenIndex;
					}
					long bigramKeyIndex = LanguageModelUtils.getIndexesToLong(index1, index2);
					int bigramIndex = bigramIndexer.addAndGetIndex(bigramKeyIndex);
					bigramMap[bigramIndex]++;
					
					if(i < stoppedSentence.size() - 2) {
						index3 = EnglishWordIndexer.getIndexer().indexOf(stoppedSentence.get(i+2));
						if(index3 == -1)
							index3 = unkTokenIndex;
						
						long trigramKeyIndex = LanguageModelUtils.getIndexesToLong(index1, index2, index3);
						int currCount = trigramMap.increment(trigramKeyIndex, 1);

						if(currCount == 1) {
							int preBigramIndex = bigramIndexer.addAndGetIndex(LanguageModelUtils.getIndexesToLong(index2, index3));
							preBigramFertilityMap[preBigramIndex]++;
							postBigramFertilityMap[bigramIndex]++;
							prePostUnigramFertilityMap[index2]++;
						}	
					}
				}
				index1 = index2;
				index2 = index3;
			}
		}
		System.out.println("unigramMap - " + unigramMap.length + ", bigramMap - " + bigramMap.length
				+ ", preBigram - " + preBigramFertilityMap.length + ", postBigram - " + postBigramFertilityMap.length
				+ ", prePost - " + prePostUnigramFertilityMap.length+ ", trigramMap - " + trigramMap.size());
		
		System.out.println("Done building TrigramLanguageModel at time " + df.format(LocalDateTime.now()));
	}
	
	private void preprocessTokens(Iterable<List<String>> trainingData) {
		TIntOpenHashMap<String> tempCountMap = new TIntOpenHashMap<>();
		
		for (List<String> sentence : trainingData) {
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);
			
			for (String word : stoppedSentence) {
				tempCountMap.increment(word, 1);
			}
		}
		
		unkTokenIndex = EnglishWordIndexer.getIndexer().addAndGetIndex(LanguageModelUtils.UNK);
		for(Entry<String> entry : tempCountMap.entrySet()) {
			if(entry.value > 1) {
				EnglishWordIndexer.getIndexer().addAndGetIndex(entry.key);
			}
		}
		indexerSize = EnglishWordIndexer.getIndexer().size();
		System.out.println("Preprocessing done at time " + df.format(LocalDateTime.now()));
	}
		
	@Override
	public int getOrder() {
		return 3;
	}
	
	@Override
	public double getNgramLogProbability(int[] ngram, int from, int to) {
		if(cacheMap == null)
			cacheMap = new DirectHashMap((int) (LanguageModelUtils.TRIGRAM_INIT_CAPACITY * 0.1));
		
		for(int i=from; i<to; i++) {
			if(ngram[i] >= indexerSize)
				ngram[i] = unkTokenIndex;
		}
		
		if(to - from > 3) {
			System.out.println("WARNING - (to - from) exceeds 3 for TrigramLanguageModel");
			return 0.0;
		} else if(to - from == 3) {
			long trigramKey = LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1], ngram[from+2]);
			double currProbability = cacheMap.get(trigramKey);
			if(currProbability == -1.0) {
				long bigramKey = LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1]);
				int bigramIndex = bigramIndexer.indexOf(bigramKey);
				int totalCount = 1;
				int preBigramIndex = bigramIndexer.indexOf(LanguageModelUtils.getIndexesToLong(ngram[from+1], ngram[from+2]));
				double postBigramFertility = 0.0;
				if(bigramIndex != -1) {
					totalCount = Math.max(1, bigramMap[bigramIndex]);
					postBigramFertility = postBigramFertilityMap[bigramIndex];
				}
				double preBigramFertility = 0.0;
				if(preBigramIndex != -1)
					preBigramFertility = preBigramFertilityMap[preBigramIndex];
				double probability =  Math.log(  
						(Math.max(0.0, trigramMap.get(trigramKey) - 
								LanguageModelUtils.DISCOUNT_FACTOR)/totalCount)
						+ (
							(LanguageModelUtils.DISCOUNT_FACTOR/(totalCount)) * 
							Math.max(LanguageModelUtils.EPSILON, postBigramFertility) * 
							Math.max(LanguageModelUtils.EPSILON, preBigramFertility) / 
							Math.max(1, prePostUnigramFertilityMap[ngram[from+1]])
						  )
					);
				cacheMap.put(trigramKey, probability);
				return probability;
			} else {
				return currProbability;
			}
				
		} else if(to - from == 2) {
			long bigramKey = LanguageModelUtils.getIndexesToLong(ngram[from], ngram[from+1]);
			double currProbability = cacheMap.get(bigramKey);
			if(currProbability == -1.0) {
				int bigramIndex = bigramIndexer.indexOf(bigramKey);
				double preBigramFertility = 0.0;
				if(bigramIndex != -1)
					preBigramFertility = preBigramFertilityMap[bigramIndex];
				double probability = Math.log(
						Math.max(LanguageModelUtils.EPSILON, preBigramFertility) / 
						Math.max(1, prePostUnigramFertilityMap[ngram[from]])
					);
				cacheMap.put(bigramKey, probability);
				return probability;
			} else {
				return currProbability;
			}
		} else {
			return Math.log(unigramMap[bigramIndexer.indexOf(ngram[from])] /(totalUnigramCount));
		}
	}

	@Override
	public long getCount(int[] ngram) {
		if(ngram.length > 3)
			return 0;

		long count = 0;
		if(ngram.length == 3)
			count = trigramMap.get(LanguageModelUtils.getIndexesToLong(ngram));
		else if(ngram.length == 2) 
			count = bigramMap[bigramIndexer.indexOf(LanguageModelUtils.getIndexesToLong(ngram))];
		else
			count = unigramMap[ngram[0]];
		
		return count == -1 ? 0 : count;
	}

}