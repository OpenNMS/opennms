/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.core.utils;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.DefaultLocatorFactory;

/**
 * Helper methods for working with Spring beans.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class BeanUtils {
    /**
     * Don't allow this class to be instantiated.  All methods are static.
     */
    private BeanUtils() {
    }

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
}
