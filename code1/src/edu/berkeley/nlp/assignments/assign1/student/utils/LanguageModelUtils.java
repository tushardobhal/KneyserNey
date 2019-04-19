package edu.berkeley.nlp.assignments.assign1.student.utils;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;

public class LanguageModelUtils {
	
	public final static double LOAD_FACTOR = 0.7;
	public final static int INIT_CAPACITY = 10;
	public final static String UNK = "<unk>";
	public final static double DISCOUNT_FACTOR = 0.65;
	
	private final static int BIT_LENGTH = 20;
	private final static long MAX_VAL = (1 << BIT_LENGTH) - 1;
	private final static int BASE = 2;
	
	public static long getIndexesToLong(int[] index) {
		long key = 0;
		for(int i=0; i<index.length; i++) {
			key = key | ((((long) index[i]) & MAX_VAL) << BIT_LENGTH*i);
		}
		return key;
	}
	
	public static int[] getLongToIndexes(long value) {
		String binary = Long.toBinaryString(value);
		int indexesLength = (binary.length() / BIT_LENGTH) + 1;
		int[] indexes = new int[indexesLength];
		
		for(int i=0; i<indexesLength; i++) {
			if(i != indexesLength - 1) {
				String subBinary = binary.substring(binary.length()-BIT_LENGTH*(i + 1), binary.length()-BIT_LENGTH*i);
				indexes[indexesLength-i-1] = Integer.valueOf(subBinary, BASE);
			} else {
				String subBinary = binary.substring(0, binary.length()-BIT_LENGTH*i);
				indexes[indexesLength-i-1] = Integer.valueOf(subBinary, BASE);
			}
		}
		return indexes;
	}
	
	public static int[] index(String... arr) {
		int[] indexedArr = new int[arr.length];
		for (int i = 0; i < indexedArr.length; i++) {
			indexedArr[i] = EnglishWordIndexer.getIndexer().addAndGetIndex(arr[i]);
		}
		return indexedArr;
	}
	
}
