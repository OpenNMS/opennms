/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for looking up string and integer values in a parameter
 * map.
 * 
 * @deprecated This class *modifies* the maps that are passed in, we should really do it another way.
 */
public abstract class ParameterMap {
	
	private static final Logger LOG = LoggerFactory.getLogger(ParameterMap.class);
	
	/**
	 * This method is used to lookup a specific key in the map. If the mapped
	 * value is a string it is converted to a long and the original string
	 * value is replaced in the map. The converted value is returned to the
	 * caller. If the value cannot be converted then the default value is stored
	 * in the map. If the specified key does not exist in the map then the
	 * default value is returned.
	 *
	 * @return The long value associated with the key.
	 * @param map a {@link java.util.Map} object.
	 * @param key a {@link java.lang.String} object.
	 * @param defValue a long.
	 */
    @SuppressWarnings("unchecked")
    public static long getKeyedLong(final Map map, final String key, final long defValue) {
	    
	    if (map == null) return defValue;
	    
        long value = defValue;
        Object oValue = map.get(key);

        if (oValue != null && oValue instanceof String) {
            try {
                value = Long.parseLong((String) oValue);
            } catch (NumberFormatException ne) {
                value = defValue;
                LOG.info("getKeyedLong: Failed to convert value {} for key {}", oValue , key, ne);
            }
            map.put(key, new Long(value));
        } else if (oValue != null) {
            value = ((Number) oValue).longValue();
        }
        return value;
	}
	
    /**
     * This method is used to lookup a specific key in the map. If the mapped
     * value is a string it is converted to an integer and the original string
     * value is replaced in the map. The converted value is returned to the
     * caller. If the value cannot be converted then the default value is stored
     * in the map. If the specified key does not exist in the map then the
     * default value is returned.
     *
     * @return The int value associated with the key.
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a int.
     */
    public static int getKeyedInteger(@SuppressWarnings("unchecked") final Map map, final String key, final int defValue) {
    	return new Long(ParameterMap.getKeyedLong(map, key, new Long(defValue))).intValue();
    }

    /**
     * This method is used to lookup a specific key in the map. If the mapped
     * value is a string is is converted to an integer and the original string
     * value is replaced in the map. The converted value is returned to the
     * caller. If the value cannot be converted then the default value is used.
     *
     * @return The array of integer values associated with the key.
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValues an array of int.
     */
    @SuppressWarnings("unchecked")
    public final static int[] getKeyedIntegerArray(final Map map, final String key, final int[] defValues) {
        
        if (map == null) return defValues;
        
        int[] result = defValues;
        Object oValue = map.get(key);

        if (oValue != null && oValue instanceof int[]) {
            result = (int[]) oValue;
        } else if (oValue != null) {
            List<Integer> tmpList = new ArrayList<Integer>(5);

            // Split on spaces, commas, colons, or semicolons
            //
            StringTokenizer ints = new StringTokenizer(oValue.toString(), " ;:,");
            while (ints.hasMoreElements()) {
                String token = ints.nextToken();
                try {
                    int x = Integer.parseInt(token);
                    tmpList.add(Integer.valueOf(x));
                } catch (NumberFormatException e) {
                	LOG.warn("getKeyedIntegerArray: failed to convert value {} to int array for key {} due to value {}", oValue, key, token, e);
                }
            }
            result = new int[tmpList.size()];

            for (int x = 0; x < result.length; x++)
                result[x] = ((Integer) tmpList.get(x)).intValue();

            map.put(key, result);
        }
        return result;
    }

    /**
     * This method is used to lookup a specific key in the map. If the mapped
     * value is not a String it is converted to a String and the original value
     * is replaced in the map. The converted value is returned to the caller. If
     * the specified key does not exist in the map then the default value is
     * returned.
     *
     * @return The String value associated with the key.
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a {@link java.lang.String} object.
     */
    @SuppressWarnings("unchecked")
    public static String getKeyedString(final Map map, final String key, final String defValue) {
        
        if (map == null) return defValue;

        String value = defValue;
        Object oValue = map.get(key);

        if (oValue != null && oValue instanceof String) {
            value = (String) oValue;
        } else if (oValue != null) {
            value = oValue.toString();
            map.put(key, value);
        }
        return value;
    }

    /**
     * This method is used to lookup a specific key in the map. If the mapped
     * value is a string it is converted to a boolean and the original string
     * value is replaced in the map. The converted value is returned to the
     * caller. If the value cannot be converted then the default value is stored
     * in the map. If the specified key does not exist in the map then the
     * default value is returned.
     *
     * @return The bool value associated with the key.
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a boolean.
     */
    @SuppressWarnings("unchecked")
    public static boolean getKeyedBoolean(final Map map, final String key, final boolean defValue) {
        
        if (map == null) return defValue;
        
        boolean value = defValue;
        Object oValue = map.get(key);

               if (oValue != null && oValue instanceof String) {
                       oValue = Boolean.valueOf((String) oValue);
               }

        if (oValue != null && oValue instanceof Boolean) {
            try {
                value = ((Boolean) oValue).booleanValue();
            } catch (NumberFormatException ne) {
                value = defValue;
                LOG.info("getKeyedBoolean: Failed to convert value {} for key {}", oValue, key, ne);
            }
            map.put(key, Boolean.valueOf(value));
        } else if (oValue != null) {
            value = ((Boolean) oValue).booleanValue();
        }
        return value;
    }

    /**
     * This method is used to lookup a specific key in the map. If the value 
     * cannot be found in the map then the default value is stored
     * in the map. If the specified key does not exist in the map then the
     * default value is returned.
     *
     */
    public static <T> T getKeyedValue(final Map<String,T> map, final String key, final T defValue) {

        if (map == null) return defValue;

        T oValue = map.get(key);

        if (oValue != null) {
            return oValue;
        } else {
            map.put(key, defValue);
            return defValue;
        }
    }
}
