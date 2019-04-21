package edu.berkeley.nlp.assignments.assign1.student.utils;

/**
 * Maintains a two-way map between a set of objects and contiguous integers from
 * 0 to the number of objects. Use get(i) to look up object i, and
 * indexOf(object) to look up the index of an object.
 * 
 * @author Dan Klein
 */
public class BigramTokenIndexer
{
	OpenHashMap indexes;

	/**
	 * Returns the number of objects indexed.
	 */
	public int size() {
		return indexes.size();
	}

	/**
	 * Returns the index of the given object, or -1 if the object is not present
	 * in the indexer.
	 * 
	 * @param o
	 * @return
	 */
	public int indexOf(long o) {
		int index = indexes.get(o);

		return index;
	}

	/**
	 * Add an element to the indexer if not already present. In either case,
	 * returns the index of the given object.
	 * 
	 * @param e
	 * @return
	 */
	public int addAndGetIndex(long e) {
		int index = indexes.get(e);
		if (index >= 0) { return index; }
		//  Else, add
		int newIndex = size();
		indexes.put(e, newIndex);
		return newIndex;
	}

	/**
	 * Add an element to the indexer. If the element is already in the indexer,
	 * the indexer is unchanged (and false is returned).
	 * 
	 * @param e
	 * @return
	 */
	public boolean add(long e) {
		return addAndGetIndex(e) == size() - 1;
	}
	
	public BigramTokenIndexer() {
		this(10);
	}
	
	public BigramTokenIndexer(int initialCapacity) {
		indexes = new OpenHashMap(initialCapacity, 0.7);
	}
	
	public BigramTokenIndexer(int initialCapacity, double loadFactor) {
		indexes = new OpenHashMap(initialCapacity, loadFactor);
	}

}
