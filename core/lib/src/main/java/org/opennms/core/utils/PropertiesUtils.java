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
import java.util.Properties;

/**
 * <p>PropertiesUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class PropertiesUtils {
	
	private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String PLACEHOLDER_PREFIX = "${";

    public static interface SymbolTable {
		public String getSymbolValue(String symbol);
	}
	
	private static class PropertyBasedSymbolTable implements SymbolTable {
		Properties m_properties;
		PropertyBasedSymbolTable(Properties properties) {
			m_properties = properties;
		}
                @Override
		public String getSymbolValue(String symbol) {
			return m_properties.getProperty(symbol);
		}
	}
    
    private static class MapBasedSymbolTable implements SymbolTable {
        Map<String,String> m_map;
        MapBasedSymbolTable(Map<String,String> properties) {
            m_map = properties;
        }
        @Override
        public String getSymbolValue(String symbol) {
            return m_map.get(symbol);
        }
    }
    
    /**
     * This recursively substitutes occurrences ${property.name} in initialString with the value of
     * the property property.name taken from the supplied properties object. If
     * property.name is not defined in properties then the substitution is not done.
     *
     * @param initialString the string to perform the substitutions in
     * @return The string with appropriate substitutions made.
     * @param propertiesArray a {@link java.util.Properties} object.
     */
    public static String substitute(String initialString, Properties... propertiesArray) {
        String workingString = initialString;
        for (Properties properties : propertiesArray) {
            if (properties != null)
                workingString = substitute(workingString, new PropertyBasedSymbolTable(properties), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, new ArrayList<String>());
        }
        return workingString;
    }

    /**
     * This recursively substitutes occurrences ${property.name} in initialString with the value of
     * the property property.name taken from the supplied {@link Map} object. If
     * property.name is not defined in the map then the substitution is not done.
     *
     * @param initialString the string to perform the substitutions in
     * @return The string with appropriate substitutions made.
     * @param mapArray a {@link java.util.Map} object.
     */
    public static String substitute(String initialString, Map<String,String>... mapArray) {
        String workingString = initialString;
        for (Map<String,String> properties : mapArray) {
            if (properties != null)
                workingString = substitute(workingString, new MapBasedSymbolTable(properties), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, new ArrayList<String>());
        }
        return workingString;
    }

    /**
     * <p>substitute</p>
     *
     * @param initialString a {@link java.lang.String} object.
     * @param properties a {@link java.util.Properties} object.
     * @param prefix a {@link java.lang.String} object.
     * @param suffix a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String substitute(String initialString, Properties properties, String prefix, String suffix) {
        return substitute(initialString, new PropertyBasedSymbolTable(properties), prefix, suffix, new ArrayList<String>());
    }


    /**
     * <p>substitute</p>
     *
     * @param initialString a {@link java.lang.String} object.
     * @param symbolsArray a {@link org.opennms.core.utils.PropertiesUtils.SymbolTable} object.
     * @return a {@link java.lang.String} object.
     */
    public static String substitute(String initialString, SymbolTable... symbolsArray) {
        String workingString = initialString;
        for (SymbolTable symbols : symbolsArray) {
            workingString = substitute(workingString, symbols, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, new ArrayList<String>());
        }
        return workingString;
    }

    private static String substitute(String initialString,
            SymbolTable symTable, String placeholderPrefix,
            String placeholderSuffix, List<String> list) {
        if (initialString == null) return null;
        
        StringBuffer result = new StringBuffer(initialString);
        
        int startIndex = 0;
        
        while (startIndex >= 0) {
            int beginIndex = result.indexOf(placeholderPrefix, startIndex);
            int endIndex = (beginIndex < 0 ? -1 : result.indexOf(placeholderSuffix, beginIndex+placeholderPrefix.length()));
            if (endIndex >= 0) {
                String symbol = result.substring(beginIndex+placeholderPrefix.length(), endIndex);
                if (list.contains(symbol))
                    throw new IllegalStateException("recursive loop involving symbol "+placeholderPrefix+symbol+placeholderSuffix);
                String symbolVal = symTable.getSymbolValue(symbol);
                if (symbolVal != null) {
                    list.add(symbol);
                    String substVal = substitute(symbolVal, symTable, placeholderPrefix, placeholderSuffix, list);
                    list.remove(list.size()-1);
                    result.replace(beginIndex, endIndex+1, substVal);
                    startIndex = beginIndex + substVal.length();
                    
                } else {
                    startIndex = endIndex+1;
                }
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    /**
     * Get a String valued property, returning default value if it is not set
     * or is set to an invalid value.
     *
     * @param name
     *            the property name
     * @param defaultVal
     *            the default value to use if the property is not set
     * @return the value of the property
     * @param props a {@link java.util.Properties} object.
     */
    public static String getProperty(Properties props, String name, String defaultVal) {
        return props.getProperty(name) == null ? defaultVal : props.getProperty(name);
    }
    
    /**
     * Get a boolean valued property, returning default value if it is not set
     * or is set to an invalid value.
     *
     * @param name
     *            the property name
     * @param defaultVal
     *            the default value to use if the property is not set
     * @return the value of the property
     * @param props a {@link java.util.Properties} object.
     */
    public static boolean getProperty(Properties props, String name, boolean defaultVal) {
        return "true".equalsIgnoreCase(props.getProperty(name, (defaultVal ? "true" : "false")));
    }

    /**
     * Get a int valued property, returning default value if it is not set or is
     * set to an invalid value.
     *
     * @param name
     *            the property name
     * @param defaultVal
     *            the default value to use if the property is not set
     * @return the value of the property
     * @param props a {@link java.util.Properties} object.
     */
    public static int getProperty(Properties props, String name, int defaultVal) {
        String val = props.getProperty(name, (String) null);
        if (val != null) {
            try {
                return Integer.decode(val).intValue();
            } catch (NumberFormatException e) {
            }
        }
        return defaultVal;
    }

    /**
     * Get a long valued property, returning default value if it is not set or
     * is set to an invalid value
     *
     * @param name
     *            the property name
     * @param defaultVal
     *            the default value to use if the property is not set
     * @return the value of the property
     * @param props a {@link java.util.Properties} object.
     */
    public static long getProperty(Properties props, String name, long defaultVal) {
        String val = props.getProperty(name, (String) null);
        if (val != null) {
            try {
                return Long.decode(val).longValue();
            } catch (NumberFormatException e) {
            }
        }
        return defaultVal;
    }
    

}
