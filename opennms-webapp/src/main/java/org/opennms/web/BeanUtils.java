package org.opennms.web;

import org.springframework.beans.BeanWrapperImpl;

public class BeanUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getPathValue(Object bean, String path, Class<T> expectedClass) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        Class propType = wrapper.getPropertyType(path);
        if (propType == null) {
            // we were unable to find the property
            return null;
        }
        if (!expectedClass.isAssignableFrom(propType))
            throw new IllegalArgumentException("Could not retrieve property of type "+propType+" as type "+expectedClass);
        return (T) wrapper.getPropertyValue(path);
    }

}
