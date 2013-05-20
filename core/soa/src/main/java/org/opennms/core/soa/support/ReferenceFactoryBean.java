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

package org.opennms.core.soa.support;

import org.opennms.core.soa.ServiceRegistry;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * ReferenceFactoryBean
 *
 * @author brozow
 * @version $Id: $
 */
public class ReferenceFactoryBean<T> implements FactoryBean<T>, InitializingBean {
    
    private ServiceRegistry m_serviceRegistry;
    private Class<T> m_serviceInterface;
    private String m_filter;
    
    private T m_provider;

    /**
     * <p>setServiceRegistry</p>
     *
     * @param serviceRegistry a {@link org.opennms.core.soa.ServiceRegistry} object.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        m_serviceRegistry = serviceRegistry;
    }
    
    /**
     * <p>setServiceInterface</p>
     *
     * @param serviceInterface a {@link java.lang.Class} object.
     */
    public void setServiceInterface(Class<T> serviceInterface) {
        m_serviceInterface = serviceInterface;
    }
    
    /**
     * <p>setServiceInterface</p>
     *
     * @param serviceInterface a {@link java.lang.Class} object.
     */
    public void setFilter(String filter) {
        m_filter = filter;
    }
    
    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public T getObject() throws Exception {
        
        if (m_provider == null) {
            m_provider = ProxyFactory.getProxy(m_serviceInterface, new ServiceRegistryTargetSource(m_serviceRegistry, m_filter, m_serviceInterface));
        }
        return m_provider;
    }
    
    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Override
    public Class<? extends T> getObjectType() {
        return m_serviceInterface;
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_serviceRegistry, "The serviceRegistry must be set");
        Assert.notNull(m_serviceInterface, "The serviceInterface must be set");
    }

}
