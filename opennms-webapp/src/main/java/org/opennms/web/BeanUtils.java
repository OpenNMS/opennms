/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

public class BeanUtils {

    public static Collection<String> getProperties(Object bean) {
        Collection<String> props = new LinkedList<String>();

        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            props.add(pd.getName());
        }
        return props;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getPathValue(Object bean, String path, Class<T> expectedClass) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        Class propType = wrapper.getPropertyType(path);
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
