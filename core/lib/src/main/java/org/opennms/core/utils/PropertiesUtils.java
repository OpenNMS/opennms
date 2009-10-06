//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Use Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesUtils {
	
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
		public String getSymbolValue(String symbol) {
			return m_properties.getProperty(symbol);
		}
	}
    
    /**
     * This recursively substitutes occurrences ${property.name} in initialString with the value of 
     * the property property.name taken from the supplied properties object. If 
     * property.name is not defined in properties that the substitution is not done. 
     * @param initialString the string to perform the substitutions in
     * @param properties the properties to take the values from
     * @return The string with appropriate substitutions made.
     */
    public static String substitute(String initialString, Properties properties) {
        return substitute(initialString, new PropertyBasedSymbolTable(properties), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, new ArrayList<String>());
    }

    public static String substitute(String initialString, Properties properties, String prefix, String suffix) {
        return substitute(initialString, new PropertyBasedSymbolTable(properties), prefix, suffix, new ArrayList<String>());
    }


    public static String substitute(String initialString, SymbolTable symbols) {
        return substitute(initialString, symbols, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, new ArrayList<String>());
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
