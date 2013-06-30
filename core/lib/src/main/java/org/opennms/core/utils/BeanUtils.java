/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import static org.springframework.util.Assert.notNull;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.access.DefaultLocatorFactory;

/**
 * Helper methods for working with Spring beans.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public abstract class BeanUtils {
	
	public static final Logger LOG = LoggerFactory.getLogger(BeanUtils.class);

    /**
     * Get a Spring BeanFactory by context ID.
     *
     * @param contextId the context ID of the BeanFactory to fetch
     * @return the BeanFactory
     */
    public static BeanFactoryReference getBeanFactory(String contextId) {
        BeanFactoryLocator beanFactoryLoader = DefaultLocatorFactory.getInstance();
        return beanFactoryLoader.useBeanFactory(contextId);
    }

    /**
     * Get a Spring bean by name.  Uses Java 5 generics to cast the returned
     * class to the appropriate type.
     *
     * @param <T> class for the returned bean
     * @param beanFactory bean factory to use to fetch this bean
     * @param beanId ID of the bean to fetch
     * @param clazz class representing the type for the returned bean
     * @return bean for given bean ID casted to the appropriate class
     */
    public static <T> T getBean(BeanFactoryReference beanFactory, String beanId, Class<T> clazz) {
        return clazz.cast(beanFactory.getFactory().getBean(beanId));
    }
    
    /**
     * Helper method that combines getBeanFactory and getBean.
     *
     * @param <T> class for the returned bean
     * @param contextId the context ID of the BeanFactory from which to fetch
     *      this bean
     * @param beanId ID of the bean to fetch
     * @param clazz class representing the type for the returned bean
     * @return bean for given bean ID casted to the appropriate class
     */
    public static <T> T getBean(String contextId, String beanId, Class<T> clazz) {
        return getBean(getBeanFactory(contextId), beanId, clazz);
    }
    
    /**
     * Helper method that calls getBeanFactory(contextId).getFactory()
     * and casts the result.
     *
     * @param <T> class for the returned factory
     * @param contextId the context ID of the BeanFactory to fetch
     * @param clazz class representing the type for the returned factory
     * @return the factory casted to <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFactory(String contextId, Class<T> clazz) {
        return (T) getBeanFactory(contextId).getFactory();
    }

    /**
     * Check that all fields that are marked with @Autowired are not null.
     */
    public static <T> void assertAutowiring(T instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            Resource resource = field.getAnnotation(Resource.class);
            if ((autowired != null && autowired.required()) || (resource != null)) {
                try {
                    field.setAccessible(true);
                    notNull(field.get(instance), "@Autowired/@Resource field " + field.getName() + " cannot be null");
                    LOG.debug("{} is not null", field.getName());
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Illegal access to @Autowired/@Resource field " + field.getName());
                }
            }
        }
    }
}
