/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedList;

import org.opennms.netmgt.model.InetAddressTypeEditor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSeverityEditor;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.PrimaryTypeEditor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

/**
 * <p>BeanUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class PropertyUtils {

    /**
     * <p>getProperties</p>
     *
     * @param bean a {@link java.lang.Object} object.
     * @return a {@link java.util.Collection} object.
     */
    public static Collection<String> getProperties(Object bean) {
        Collection<String> props = new LinkedList<String>();

        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            props.add(pd.getName());
        }
        return props;
    }
    
    /**
     * <p>getPathValue</p>
     *
     * @param bean a {@link java.lang.Object} object.
     * @param path a {@link java.lang.String} object.
     * @param expectedClass a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPathValue(final Object bean, final String path, final Class<T> expectedClass) {
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
        wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
        wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
        final Class<?> propType = wrapper.getPropertyType(path);
        if (propType == null) {
            // we were unable to find the property
            Assert.notNull(propType, "propType in BeanUtils is null path: " + path); //for debug purposes
            return null;
        }
        if (!expectedClass.isAssignableFrom(propType)) {
            throw new IllegalArgumentException("Could not retrieve property of type "+propType+" as type "+expectedClass);
        }
        return (T) wrapper.getPropertyValue(path);
    }

}
