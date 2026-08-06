/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.api;

import java.beans.PropertyEditor;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.datatype.XMLGregorianCalendar;

import org.opennms.netmgt.model.InetAddressTypeEditor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSeverityEditor;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.PrimaryTypeEditor;
import org.opennms.netmgt.provision.persist.StringXmlCalendarPropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public abstract class RestUtils {

	private static final Logger LOG = LoggerFactory.getLogger(RestUtils.class);

	/** Nested ('.') and indexed ('[', ']') separators in a Spring bean property path. */
	private static final Pattern PROPERTY_PATH_SEPARATOR = Pattern.compile("[.\\[\\]]");

	/** Primary keys and the category ACL collection: never settable from request parameters. */
	public static final Set<String> IMMUTABLE_PROPERTIES = Collections.unmodifiableSet(
	        new HashSet<>(Arrays.asList("id", "dbId", "nodeId", "authorizedGroups")));

	/** Node identity and provisioning-ownership properties. */
	public static final Set<String> PROTECTED_NODE_PROPERTIES = Collections.unmodifiableSet(
	        new HashSet<>(Arrays.asList("foreignSource", "foreignId", "type")));

	/** Enforced for every caller that does not explicitly opt out. */
	private static final Set<String> DEFAULT_PROTECTED_PROPERTIES;
	static {
	    final Set<String> defaults = new HashSet<>(IMMUTABLE_PROPERTIES);
	    defaults.addAll(PROTECTED_NODE_PROPERTIES);
	    DEFAULT_PROTECTED_PROPERTIES = Collections.unmodifiableSet(defaults);
	}

	/**
	 * Whether a request parameter name resolves to a property protected by default, including via
	 * a key variant ({@code foreign_source}) or traversal ({@code asset_record.node.foreign_source}).
	 */
	public static boolean isProtectedProperty(final String key) {
	    return pathReachesProperty(key, DEFAULT_PROTECTED_PROPERTIES);
	}

	/** As {@link #isProtectedProperty(String)}, plus names protected for this endpoint only. */
	public static boolean isProtectedProperty(final String key, final Set<String> additionalProtectedProperties) {
	    return isProtectedProperty(key) || pathReachesProperty(key, additionalProtectedProperties);
	}

	/** As {@link #isProtectedProperty}, for endpoints that reject the request outright. */
	public static boolean containsProperty(final MultivaluedMap<String,String> properties, final String propertyName) {
	    final Set<String> wanted = Collections.singleton(propertyName);
	    for (final String key : properties.keySet()) {
	        if (pathReachesProperty(key, wanted)) {
	            return true;
	        }
	    }
	    return false;
	}

	/**
	 * Callers bind either the raw key or the normalized one, and normalization is not
	 * case-preserving, so both spellings are compared ignoring case. BeanWrapper resolves nested
	 * and indexed paths, hence the per-segment check.
	 */
	private static boolean pathReachesProperty(final String key, final Set<String> propertyNames) {
	    for (final String path : new String[] { key, convertNameToPropertyName(key) }) {
	        for (final String segment : PROPERTY_PATH_SEPARATOR.split(path)) {
	            for (final String propertyName : propertyNames) {
	                if (propertyName.equalsIgnoreCase(segment)) {
	                    return true;
	                }
	            }
	        }
	    }
	    return false;
	}

	/**
	 * <p>Use Spring's {@link PropertyAccessorFactory} to set values on the specified bean.
	 * This call registers several {@link PropertyEditor} classes to properly convert
	 * values.</p>
	 * 
	 * <ul>
	 * <li>{@link StringXmlCalendarPropertyEditor}</li>
	 * <li>{@link ISO8601DateEditor}</li>
	 * <li>{@link InetAddressTypeEditor}</li>
	 * <li>{@link OnmsSeverityEditor}</li>
	 * <li>{@link PrimaryTypeEditor}</li>
	 * </ul>
	 * 
	 * <p>Properties protected by {@link #isProtectedProperty(String)} are ignored.</p>
	 *
	 * @param bean
	 * @param properties
	 */
	public static void setBeanProperties(final Object bean, final MultivaluedMap<String,String> properties) {
	    applyProperties(bean, properties, DEFAULT_PROTECTED_PROPERTIES);
	}

	/** As {@link #setBeanProperties(Object, MultivaluedMap)}, plus endpoint-specific names. */
	public static void setBeanProperties(final Object bean, final MultivaluedMap<String,String> properties, final Set<String> additionalProtectedProperties) {
	    final Set<String> protectedProperties = new HashSet<>(DEFAULT_PROTECTED_PROPERTIES);
	    protectedProperties.addAll(additionalProtectedProperties);
	    applyProperties(bean, properties, protectedProperties);
	}

	/**
	 * For provisioning requisitions only, where {@code foreignSource}/{@code foreignId} identify
	 * the requisition and so are legitimately settable. Enforces {@link #IMMUTABLE_PROPERTIES}
	 * alone; use {@link #setBeanProperties(Object, MultivaluedMap)} for anything node-reachable.
	 */
	public static void setRequisitionProperties(final Object bean, final MultivaluedMap<String,String> properties) {
	    applyProperties(bean, properties, IMMUTABLE_PROPERTIES);
	}

	private static void applyProperties(final Object bean, final MultivaluedMap<String,String> properties, final Set<String> protectedProperties) {
	    final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
	    wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
	    wrapper.registerCustomEditor(Date.class, new ISO8601DateEditor());
	    wrapper.registerCustomEditor(InetAddress.class, new InetAddressTypeEditor());
	    wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
	    wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
	    for(final String key : properties.keySet()) {
	        final String propertyName = convertNameToPropertyName(key);
	        if (pathReachesProperty(key, protectedProperties)) {
	            LOG.warn("Ignoring attempt to set protected property '{}' from request parameters", propertyName);
	            continue;
	        }
	        if (wrapper.isWritableProperty(propertyName)) {
	            final String stringValue = properties.getFirst(key);
	            Object value = convertIfNecessary(wrapper, propertyName, stringValue);
	            wrapper.setPropertyValue(propertyName, value);
	        }
	    }
	}

	private static Object convertIfNecessary(final BeanWrapper wrapper,	final String propertyName, final String stringValue) {
		LOG.debug("convertIfNecessary({}, {})", propertyName, stringValue);
		return wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(propertyName));
	}

	/**
	 * Convert a column name with underscores to the corresponding property name using "camel case".  A name
	 * like "customer_number" would match a "customerNumber" property name.
	 *
	 * @param name the column name to be converted
	 * @return the name using "camel case"
	 */
	public static String convertNameToPropertyName(String name) {
	    final StringBuilder result = new StringBuilder();
	    boolean nextIsUpper = false;
	    if (name != null && name.length() > 0) {
	        if (name.length() > 1 && (name.substring(1, 2).equals("_") || (name.substring(1, 2).equals("-")))) {
	            result.append(name.substring(0, 1).toUpperCase());
	        } else {
	            result.append(name.substring(0, 1).toLowerCase());
	        }
	        for (int i = 1; i < name.length(); i++) {
	            String s = name.substring(i, i + 1);
	            if (s.equals("_") || s.equals("-")) {
	                nextIsUpper = true;
	            } else {
	                if (nextIsUpper) {
	                    result.append(s.toUpperCase());
	                    nextIsUpper = false;
	                } else {
	                    result.append(s.toLowerCase());
	                }
	            }
	        }
	    }
	    return result.toString();
	}
}
