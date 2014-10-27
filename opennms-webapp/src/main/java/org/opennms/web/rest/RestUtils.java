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

package org.opennms.web.rest;

import java.net.InetAddress;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSeverityEditor;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.PrimaryTypeEditor;
import org.opennms.netmgt.provision.persist.StringXmlCalendarPropertyEditor;
import org.opennms.web.rest.support.InetAddressTypeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public abstract class RestUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(RestUtils.class);


	public static void setBeanProperties(final Object bean, final MultivaluedMapImpl properties) {
		final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
	    wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
	    wrapper.registerCustomEditor(Date.class, new ISO8601DateEditor());
	    wrapper.registerCustomEditor(InetAddress.class, new InetAddressTypeEditor());
	    wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
	    wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
	    for(final String key : properties.keySet()) {
	        final String propertyName = OnmsRestService.convertNameToPropertyName(key);
	        if (wrapper.isWritableProperty(propertyName)) {
	            final String stringValue = properties.getFirst(key);
				Object value = convertIfNecessary(wrapper, propertyName, stringValue);
	            wrapper.setPropertyValue(propertyName, value);
	        }
	    }
	}

	@SuppressWarnings("unchecked")
	private static Object convertIfNecessary(final BeanWrapper wrapper,	final String propertyName, final String stringValue) {
		LOG.debug("convertIfNecessary({}, {})", propertyName, stringValue);
		return wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(propertyName));
	}

}
