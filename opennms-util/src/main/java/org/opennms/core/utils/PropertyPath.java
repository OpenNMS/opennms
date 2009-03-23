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

