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
package org.opennms.web.api;

import java.beans.PropertyEditor;
import java.net.InetAddress;
import java.util.Date;

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
	 * @param bean
	 * @param properties
	 */
	public static void setBeanProperties(final Object bean, final MultivaluedMap<String,String> properties) {
	    final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
	    wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
	    wrapper.registerCustomEditor(Date.class, new ISO8601DateEditor());
	    wrapper.registerCustomEditor(InetAddress.class, new InetAddressTypeEditor());
	    wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
	    wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
	    for(final String key : properties.keySet()) {
	        final String propertyName = convertNameToPropertyName(key);
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
