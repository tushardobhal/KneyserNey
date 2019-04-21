package edu.berkeley.nlp.assignments.assign1.student.utils;

import java.util.Arrays;

/**
 * Open address hash map with linear probing. Maps Strings to int's. Note that
 * int's are assumed to be non-negative, and -1 is returned when a key is not
 * present.
 * 
 * @author adampauls
 * 
 */
public class DirectHashMap
{

	private long[] keys;

	private double[] values;
	
	private static final long NULL_KEY = -1;

	private int size = 0;

	public DirectHashMap() {
		this(10);
	}

	public DirectHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.7);
	}

	public DirectHashMap(int initialCapacity_, double loadFactor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		size = cap;
		values = new double[cap];
		Arrays.fill(values, -1.0);
		keys = new long[cap];
		Arrays.fill(keys, NULL_KEY);
	}
	
	public void put(long k, double v) {
		putHelp(k, v, keys, values);

	}

	private void putHelp(long k, double v, long[] keyArray, double[] valueArray) {
		int pos = getInitialPos(k, keyArray);
		values[pos] = v;
		keys[pos] = k;
	}

	/**
	 * @param k
	 * @param keyArray
	 * @return
	 */
	private int getInitialPos(long k, long[] keyArray) {
		int hash = Long.hashCode(k);
		int pos = hash % keyArray.length;
		if (pos < 0) pos += keyArray.length;
    // N.B. Doing it this old way causes Integer.MIN_VALUE to be
		// handled incorrect since -Integer.MIN_VALUE is still
		// Integer.MIN_VALUE
//		if (hash < 0) hash = -hash;
//		int pos = hash % keyArray.length;
		return pos;
	}

	public double get(long k) {
		return find(k);
	}

	/**
	 * @param k
	 * @return
	 */
	private double find(long k) {
		int pos = getInitialPos(k, keys);
		long curr = keys[pos];
		if (curr != NULL_KEY && curr == k) {
			return values[pos];
		}
		return -1.0;
	}

	public int size() {
		return size;
	}

}