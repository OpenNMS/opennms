/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
