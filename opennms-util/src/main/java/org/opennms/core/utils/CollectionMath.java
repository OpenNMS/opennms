package org.opennms.core.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CollectionMath {

	/**
	 * Get the number of null entries in a collection
	 * @param collection the collection
	 * @return the number of null entries
	 */
	public static long countNull(Collection collection) {
		long count = 0;
		for (Object entry : collection) {
			if (entry == null) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Get the number of non-null entries in a collection
	 * @param collection the collection
	 * @return the number of non-null entries
	 */
	public static long countNotNull(Collection collection) {
		return (collection.size() - countNull(collection));
	}

	/**
	 * Get the percentage of null entries in a collection of {@link Long} values
	 * @param collection the collection of {@link Long} values
	 * @return the percentage of null values as a {@link Long} value
	 */
	public static Long percentNull(Collection<Long> collection) {
		return percentNull(convertLongToBigDecimal(collection)).longValue();
	}

	/**
	 * Get the percentage of null entries in a collection of {@link BigDecimal} values
	 * @param collection the collection of {@link BigDecimal} values
	 * @return the percentage of null values as a {@link BigDecimal} value
	 */
	public static BigDecimal percentNull(Collection<BigDecimal> collection) {
		if (collection.size() > 0) {
			return new BigDecimal(countNull(collection)).divide(new BigDecimal(collection.size())).multiply(new BigDecimal(100));
		} else {
			return null;
		}
	}
	
	/**
	 * Get the percentage of not-null entries in a collection of {@link Long} values
	 * @param collection the collection of {@link Long} values
	 * @return the percentage of not-null values as a {@link Long} value
	 */
	public static Long percentNotNull(Collection<Long> collection) {
		return percentNotNull(convertLongToBigDecimal(collection)).longValue();
	}
	
	/**
	 * Get the percentage of not-null entries in a collection of {@link BigDecimal} values
	 * @param collection the collection of {@link BigDecimal} values
	 * @return the percentage of not-null values as a {@link BigDecimal} value
	 */
	public static BigDecimal percentNotNull(Collection<BigDecimal> collection) {
		if (collection.size() > 0) {
			return new BigDecimal(countNotNull(collection)).divide(new BigDecimal(collection.size())).multiply(new BigDecimal(100));
		} else {
			return null;
		}
	}

	/**
	 * Get the average of the contents of a collection of {@link Long} values, excluding null entries
	 * @param collection the collection of {@link Long} values
	 * @return the average of the not-null values as a {@link Long} value
	 */
	public static Long average(Collection<Long> collection) {
		return average(convertLongToBigDecimal(collection)).longValue();
	}
	
	/**
	 * Get the average of the contents of a collection of {@link BigDecimal} values, excluding null entries
	 * @param collection the collection of {@link BigDecimal} values
	 * @return the average of the not-null values as a {@link BigDecimal} value
	 */
	public static BigDecimal average(Collection<BigDecimal> collection) {
		BigDecimal total = new BigDecimal(0);
		Collection<BigDecimal> notNullEntries = getNotNullEntries(collection);
		if (notNullEntries.size() == 0) {
			return null;
		}
		
		for (BigDecimal entry : notNullEntries) {
			total = total.add(entry);
		}
		
		return total.divide(new BigDecimal(notNullEntries.size()));
	}
	
	/**
	 * Get the median of the contents of a collection of {@link Long} values, excluding null entries
	 * @param collection the collection of {@link Long} values
	 * @return the median of the not-null values as a {@link Long} value
	 */
	public static Long median(Collection<Long> collection) {
		return median(convertLongToBigDecimal(collection)).longValue();
	}
	
	/**
	 * Get the median of the contents of a collection of {@link BigDecimal} values, excluding null entries
	 * @param collection the collection of {@link BigDecimal} values
	 * @return the median of the not-null values as a {@link BigDecimal} value
	 */
	public static BigDecimal median(Collection<BigDecimal> collection) {
		ArrayList<BigDecimal> notNullEntries = getNotNullEntries(collection);
		Collections.sort(notNullEntries);
		
		if (notNullEntries.size() % 2 == 0) {
			// even number of entries, take the mean of the 2 center ones
			BigDecimal value1, value2;
			value1 = notNullEntries.get(notNullEntries.size() / 2);
			value2 = notNullEntries.get((notNullEntries.size() / 2) - 1);
			return value1.add(value2).divide(new BigDecimal(2));
		} else {
			return notNullEntries.get(notNullEntries.size() / 2);
		}
	}
	
	/**
	 * Utility method, converts a {@link Long} {@link Collection} into a {@link BigDecimal} {@link Collection}
	 * @param collection a {@link Collection} of {@link Long} values
	 * @return a {@link Collection} of {@link BigDecimal} values
	 */
	private static Collection<BigDecimal> convertLongToBigDecimal(Collection<Long> c) {
		Collection<BigDecimal> bd = new ArrayList<BigDecimal>();
		for (Long entry : c) {
			bd.add(new BigDecimal(entry));
		}
		return bd;
	}
	
	/**
	 * Utility method, gets not-null-entries from a collection
	 * @param collection the collection to search
	 * @return an {@link ArrayList} of not-null values (if any)
	 */
	private static ArrayList<BigDecimal> getNotNullEntries(Collection<BigDecimal> collection) {
		ArrayList<BigDecimal> s = new ArrayList<BigDecimal>();
		for (BigDecimal entry : collection) {
			if (entry != null) {
				s.add(entry);
			}
		}
		return s;
	}
	
}
