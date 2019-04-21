package edu.berkeley.nlp.assignments.assign1.student.utils;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;

public class LanguageModelUtils {
	public final static String UNK = "<unk>";
	
	public final static double LOAD_FACTOR = 0.65;
	public final static int UNIGRAM_INIT_CAPACITY = 260_000;
	public final static int BIGRAM_INIT_CAPACITY = 8_000_000;
	public final static int TRIGRAM_INIT_CAPACITY = 41_300_000;
	
	public final static double DISCOUNT_FACTOR = 0.75;
	public final static double EPSILON = 0.000001;
	
	public static long getIndexesToLong(long index1, long index2, long index3) {
		long key = ((index1 << 20 | index2) << 20 | index3) << 4;
		return key;
	}
	
	public static long getIndexesToLong(long index1, long index2) {
		long key = (index1 << 20 | index2) << 24;
		return key;
	}
	
	public static int[] index(String... arr) {
		int[] indexedArr = new int[arr.length];
		for (int i = 0; i < indexedArr.length; i++) {
			indexedArr[i] = EnglishWordIndexer.getIndexer().addAndGetIndex(arr[i]);
		}
		return indexedArr;
	}
	
	public static int index(String str) {
		return EnglishWordIndexer.getIndexer().addAndGetIndex(str);
	}
	
}
