/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * <p>WebSecurityUtils class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public abstract class WebSecurityUtils {
	
	private final static Pattern ILLEGAL_IN_INTEGER = Pattern.compile("[^0-9+-]");
	
	private final static Pattern ILLEGAL_IN_FLOAT = Pattern.compile("[^0-9.Ee+-]");
	
	private final static Pattern ILLEGAL_IN_COLUMN_NAME_PATTERN = Pattern.compile("[^A-Za-z0-9_]");
	
    private final static Pattern scriptPattern = Pattern.compile("script", Pattern.CASE_INSENSITIVE);

    /**
     * <p>sanitizeString</p>
     *
     * @param raw an array of {@link java.lang.String} objects.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] sanitizeString(String[] raw) {
        for (int i = 0; i < raw.length; i++) {
            raw[i] = sanitizeString(raw[i]);
        }
        return raw;
    }
    
    /**
     * <p>sanitizeString</p>
     *
     * @param raw a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String sanitizeString(String raw) {
        return sanitizeString(raw, false);
    }
    
    /**
     * <p>sanitizeString</p>
     *
     * @param raw a {@link java.lang.String} object.
     * @param allowHTML a boolean.
     * @return a {@link java.lang.String} object.
     */
    public static String sanitizeString(String raw, boolean allowHTML)
    {
        if (raw==null || raw.length()==0) {
            return raw;
        }

        Matcher scriptMatcher = scriptPattern.matcher(raw);
        String next = scriptMatcher.replaceAll("&#x73;cript");
        if (!allowHTML) {
            next = next.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
        }
        return next;
    }

    /**
     * <p>safeParseInt</p>
     *
     * @param dirty an array of {@link java.lang.String} objects.
     * @return an array of int.
     * @throws java.lang.NumberFormatException if any.
     */
    public static int[] safeParseInt(String[] dirty) throws NumberFormatException {
        final int[] clean = new int[dirty.length];
        String cleanString;
        for (int i = 0; i < dirty.length; i++) {
            cleanString = ILLEGAL_IN_INTEGER.matcher(dirty[i]).replaceAll("");
            clean[i] = Integer.parseInt(cleanString);
        }
        return clean;
    }

	/**
	 * <p>safeParseInt</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a int.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static int safeParseInt(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_INTEGER.matcher(dirty).replaceAll("");
		return Integer.parseInt(clean);
	}
	
	/**
	 * <p>safeParseLong</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a long.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static long safeParseLong(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_INTEGER.matcher(dirty).replaceAll("");
		return Long.parseLong(clean);
	}
	
	/**
	 * <p>safeParseFloat</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a float.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static float safeParseFloat(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_FLOAT.matcher(dirty).replaceAll("");
		return Float.parseFloat(clean);
	}
	
	/**
	 * <p>safeParseDouble</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a double.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static double safeParseDouble(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_FLOAT.matcher(dirty).replaceAll("");
		return Double.parseDouble(clean);
	}
	
    /**
     * <p>sanitizeDbColumnName</p>
     *
     * @param dirty a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String sanitizeDbColumnName(String dirty) {
        return ILLEGAL_IN_COLUMN_NAME_PATTERN.matcher(dirty).replaceAll("");
    }

    /**
     * <p>sanitizeBeanStringProperties</p>
     * This is a simple method is used to sanitize all bean string properties. 
     * 
     * @param bean a {@link java.lang.Object} object. 
     * @param Set of fieldnames as Strings that are allowed for html content. All fieldnames in lowercase. null -> no html
     * @return a {@link java.lang.Object} object.
     */
    public static <T> T sanitizeBeanStringProperties(T bean, Set<String> allowHtmlFields) {
    	BeanWrapper beanWrapper = new BeanWrapperImpl(bean.getClass());
    	
    	// get all bean property descriptors
    	PropertyDescriptor[] descriptions = beanWrapper.getPropertyDescriptors();
    	
    	// Iterate over all properties
    	for (PropertyDescriptor description : descriptions) {
    		
    		// If we have a property with type of java.lang.String, then sanitize string and write back
    		if (description.getReadMethod().getReturnType().equals(java.lang.String.class)) {
    			try {
    				boolean allowHTML = false;
	    	        if (allowHtmlFields != null && allowHtmlFields.contains(description.getName().toLowerCase())) {
	    	            allowHTML = true;
	    	        }
    				LogUtils.debugf(WebSecurityUtils.class, "Try to sanitize string %s in %s with html %b", description.getName(), bean.getClass(), allowHTML);
    				description.getWriteMethod().invoke(bean, WebSecurityUtils.sanitizeString((String)description.getReadMethod().invoke(bean), allowHTML));
    			}catch (IllegalArgumentException e) {
					LogUtils.errorf(WebSecurityUtils.class, "Illegal argument by sanitize object %s on property %s. Error %s", description.getName(), bean.getClass(), e.getMessage());
				} catch (IllegalAccessException e) {
					LogUtils.errorf(WebSecurityUtils.class, "Illegal access by sanitize object %s on property %s. Error %s", description.getName(), bean.getClass(), e.getMessage());
				} catch (InvocationTargetException e) {
					LogUtils.errorf(WebSecurityUtils.class, "Invocation target exception by sanitize object %s on property %s. Error %s", description.getName(), bean.getClass(), e.getMessage());
				}
    		}
    	}
    	return bean;
    }
}
