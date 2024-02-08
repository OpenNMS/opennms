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

