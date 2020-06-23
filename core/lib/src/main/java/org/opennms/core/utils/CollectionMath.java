/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>CollectionMath class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 */
public abstract class CollectionMath {

	/**
	 * Get the number of null entries in a {@link List}
	 *
	 * @param list the {@link List}
	 * @return the number of null entries
	 */
	public static long countNull(List<?> list) {
		long count = 0;
		for (Object entry : list) {
			if (entry == null) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Get the number of non-null entries in a {@link List}
	 *
	 * @param list the list
	 * @return the number of non-null entries
	 */
	public static long countNotNull(List<?> list) {
		return (list.size() - countNull(list));
	}

	/**
	 * Get the percentage of null entries in a {@link List} of {@link Number} values
	 *
	 * @param list the {@link List} of {@link Number} values
	 * @return the percentage of null values as a {@link Number} value
	 */
	public static Number percentNull(List<? extends Number> list) {
		return percentNullBigDecimal(convertNumberToBigDecimal(list));
	}

	/**
	 * Get the percentage of null entries in a {@link List} of {@link BigDecimal} values
	 * @param list the {@link List} of {@link BigDecimal} values
	 * @return the percentage of null values as a {@link BigDecimal} value
	 */
	private static BigDecimal percentNullBigDecimal(List<BigDecimal> list) {
		if (list.size() > 0) {
			return new BigDecimal(countNull(list)).divide(new BigDecimal(list.size()), 16, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100));
		} else {
			return null;
		}
	}
	
	/**
	 * Get the percentage of not-null entries in a {@link List} of {@link Number} values
	 *
	 * @param list the {@link List} of {@link Number} values
	 * @return the percentage of not-null values as a {@link Number} value
	 */
	public static Number percentNotNull(List<? extends Number> list) {
		return percentNotNullBigDecimal(convertNumberToBigDecimal(list));
	}
	
	/**
	 * Get the percentage of not-null entries in a {@link List} of {@link BigDecimal} values
	 * @param list the {@link List} of {@link BigDecimal} values
	 * @return the percentage of not-null values as a {@link BigDecimal} value
	 */
    private static BigDecimal percentNotNullBigDecimal(List<BigDecimal> list) {
		if (list.size() > 0) {
			return new BigDecimal(countNotNull(list)).divide(new BigDecimal(list.size()), 16, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100));
		} else {
			return null;
		}
	}

	/**
	 * Get the average of the contents of a {@link List} of {@link Number} values, excluding null entries
	 *
	 * @param list the {@link List} of {@link Number} values
	 * @return the average of the not-null values as a {@link Number} value
	 */
	public static Number average(List<? extends Number> list) {
		return averageBigDecimal(convertNumberToBigDecimal(list));
	}
	
	/**
	 * Get the average of the contents of a {@link List} of {@link BigDecimal} values, excluding null entries
	 * @param list the {@link List} of {@link BigDecimal} values
	 * @return the average of the not-null values as a {@link BigDecimal} value
	 */
    private static BigDecimal averageBigDecimal(List<BigDecimal> list) {
		BigDecimal total = BigDecimal.ZERO;
		List<BigDecimal> notNullEntries = getNotNullEntries(list);
		if (notNullEntries.size() == 0) {
			return null;
		}
		
		for (BigDecimal entry : notNullEntries) {
			total = total.add(entry);
		}
		
		return total.divide(new BigDecimal(notNullEntries.size()), 16, BigDecimal.ROUND_HALF_EVEN);
	}
	
	/**
	 * Get the median of the contents of a {@link List} of {@link Number} values, excluding null entries
	 *
	 * @param list the {@link List} of {@link Number} values
	 * @return the median of the not-null values as a {@link Number} value
	 */
	public static Number median(List<? extends Number> list) {
		return medianBigDecimal(convertNumberToBigDecimal(list));
	}
	
	/**
	 * Get the median of the contents of a {@link List} of {@link BigDecimal} values, excluding null entries
	 * @param list the {@link List} of {@link BigDecimal} values
	 * @return the median of the not-null values as a {@link BigDecimal} value
	 */
    private static BigDecimal medianBigDecimal(List<BigDecimal> list) {
		List<BigDecimal> notNullEntries = getNotNullEntries(list);
		Collections.sort(notNullEntries);
		
		if (notNullEntries.size() == 0) {
		    return null;
		}
		
		if (notNullEntries.size() % 2 == 0) {
			// even number of entries, take the mean of the 2 center ones
			BigDecimal value1, value2;
		    value1 = notNullEntries.get(notNullEntries.size() / 2);
		    value2 = notNullEntries.get((notNullEntries.size() / 2) - 1);
			return value1.add(value2).divide(new BigDecimal(2), 16, BigDecimal.ROUND_HALF_EVEN);
		} else {
			return notNullEntries.get(notNullEntries.size() / 2);
		}
	}
	
	/**
	 * Utility method, converts a {@link Number} {@link List} into a {@link BigDecimal} {@link List}
	 * @param c a {@link List} of {@link Number} values
	 * @return a {@link List} of {@link BigDecimal} values
	 */
	private static List<BigDecimal> convertNumberToBigDecimal(List<? extends Number> c) {
		List<BigDecimal> bd = new ArrayList<>();
		for (Number entry : c) {
		    if (entry != null) {
		        bd.add(new BigDecimal(entry.doubleValue()));
		    } else {
		        bd.add(null);      
            }
		}
		return bd;
	}
	
	/**
	 * Utility method, gets not-null-entries from a {@link List}
	 * @param list the {@link List} to search
	 * @return an {@link ArrayList} of not-null values (if any)
	 */
	private static List<BigDecimal> getNotNullEntries(List<BigDecimal> list) {
		List<BigDecimal> s = new ArrayList<>();
		for (BigDecimal entry : list) {
			if (entry != null) {
				s.add(entry);
			}
		}
		return s;
	}
	
}
