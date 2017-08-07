/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.spring;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyAccessorUtils;

/**
 * <p>PropertyPath class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PropertyPath {
    private PropertyPath parent = null;
    private String propertyName;
    private String key;
    
    /**
     * <p>Constructor for PropertyPath.</p>
     *
     * @param nestedPath a {@link java.lang.String} object.
     */
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(parent == null ? "" : parent.toString()+'.');
        buf.append(propertyName);
        if (key.length() > 0) {
            buf.append(PropertyAccessor.PROPERTY_KEY_PREFIX);
            buf.append(key);
            buf.append(PropertyAccessor.PROPERTY_KEY_SUFFIX);
        }
        return buf.toString();
    }

    /**
     * <p>Getter for the field <code>key</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKey() {
        return key;
    }

    /**
     * <p>Getter for the field <code>parent</code>.</p>
     *
     * @return a {@link org.opennms.core.spring.PropertyPath} object.
     */
    public PropertyPath getParent() {
        return parent;
    }

    /**
     * <p>Getter for the field <code>propertyName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPropertyName() {
        return propertyName;
    }
    
    /**
     * <p>getValue</p>
     *
     * @param root a {@link java.lang.Object} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object getValue(Object root) throws InvalidPropertyException {
        return getValue(PropertyAccessorFactory.forBeanPropertyAccess(root));
    }
    
    /**
     * <p>getValue</p>
     *
     * @param beanWrapper a {@link org.springframework.beans.BeanWrapper} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object getValue(BeanWrapper beanWrapper) throws InvalidPropertyException {
        return beanWrapper.getPropertyValue(toString());
    }
    
}

