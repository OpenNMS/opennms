/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.core.utils;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;

public class PropertyPath {
    private PropertyPath parent = null;
    private String propertyName;
    private String key;
    
    public PropertyPath(String nestedPath) {
        String canonicalPath = PropertyAccessorUtils.canonicalPropertyName(nestedPath);
        int lastIndex = PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(canonicalPath);
        if (lastIndex < 0) {
            propertyName = PropertyAccessorUtils.getPropertyName(canonicalPath);
            key = computeKey(canonicalPath);
        } 
        else {
            parent = new PropertyPath(canonicalPath.substring(0, lastIndex));
            String lastProperty = canonicalPath.substring(lastIndex+1);
            propertyName = PropertyAccessorUtils.getPropertyName(lastProperty);
            key = computeKey(lastProperty);
        }
    }

    private String computeKey(String property) {
        int keyPrefix = property.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
        if (keyPrefix < 0) {
            return "";
        }
        
        int keySuffix = property.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
        return property.substring(keyPrefix+1, keySuffix);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(parent == null ? "" : parent.toString()+'.');
        buf.append(propertyName);
        if (key.length() > 0) {
            buf.append(PropertyAccessor.PROPERTY_KEY_PREFIX);
            buf.append(key);
            buf.append(PropertyAccessor.PROPERTY_KEY_SUFFIX);
        }
        return buf.toString();
    }

    public String getKey() {
        return key;
    }

    public PropertyPath getParent() {
        return parent;
    }

    public String getPropertyName() {
        return propertyName;
    }
    
    public Object getValue(Object root) {
        return getValue(new BeanWrapperImpl(root));
    }
    
    public Object getValue(BeanWrapper beanWrapper) {
        return beanWrapper.getPropertyValue(toString());
    }
    
}

