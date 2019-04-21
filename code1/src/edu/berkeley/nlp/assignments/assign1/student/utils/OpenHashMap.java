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
public class OpenHashMap
{

	private long[] keys;

	private int[] values;

	private int size = 0;

	private final double MAX_LOAD_FACTOR;

	public boolean put(long k, int v) {
		if (size / (double) keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		return putHelp(k, v, keys, values);

	}

	public OpenHashMap() {
		this(10);
	}

	public OpenHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.7);
	}

	public OpenHashMap(int initialCapacity_, double loadFactor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		MAX_LOAD_FACTOR = loadFactor;
		values = new int[cap];
		Arrays.fill(values, -1);
		keys = new long[cap];
		Arrays.fill(keys, -1);
	}

	/**
	 * 
	 */
	private void rehash() {
		long[] newKeys = new long[keys.length * 3 / 2];
		int[] newValues = new int[values.length * 3 / 2];
		Arrays.fill(newValues, -1);
		Arrays.fill(newKeys, -1);
		size = 0;
		for (int i = 0; i < keys.length; ++i) {
			long curr = keys[i];
			if (curr != -1) {
				int val = values[i];
				putHelp(curr, val, newKeys, newValues);
			}
		}
		keys = newKeys;
		values = newValues;
	}

	/**
	 * @param k
	 * @param v
	 */
	private boolean putHelp(long k, int v, long[] keyArray, int[] valueArray) {
		int pos = getInitialPos(k, keyArray);
		long curr = keyArray[pos];
		while (curr != -1 && curr != k) {
			pos++;
			if (pos == keyArray.length) pos = 0;
			curr = keyArray[pos];
		}

		valueArray[pos] = v;
		if (curr == -1) {
			size++;
			keyArray[pos] = k;
			return true;
		}
		return false;
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

	public int get(long k) {
		int pos = find(k);

		return values[pos];
	}

	/**
	 * @param k
	 * @return
	 */
	private int find(long k) {
		int pos = getInitialPos(k, keys);
		long curr = keys[pos];
		while (curr != -1 && curr != k) {
			pos++;
			if (pos == keys.length) pos = 0;
			curr = keys[pos];
		}
		return pos;
	}

	public int increment(long k, int c) {
		int pos = find(k);
		long currKey = keys[pos];
		if (currKey == -1) {
			put(k, c);
		} else
			values[pos]++;
		return values[pos];
	}

	public int size() {
		return size;
	}

}