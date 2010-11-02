/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.soa.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * ReferenceFactoryBean
 *
 * @author brozow
 * @version $Id: $
 */
public class ReferenceListFactoryBean<T> implements FactoryBean<List<T>>, InitializingBean, RegistrationListener<T> {
    
    private ServiceRegistry m_serviceRegistry;
    private Class<T> m_serviceInterface;
    private List<RegistrationListener<T>> m_listeners = new CopyOnWriteArrayList<RegistrationListener<T>>();
    
    private List<T> m_providerRegistrations = new CopyOnWriteArrayList<T>();

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
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    public List<T> getObject() throws Exception {
        return m_providerRegistrations;
    }
    
    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<?> getObjectType() {
        return List.class;
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_serviceRegistry, "The serviceRegistry must be set");
        Assert.notNull(m_serviceInterface, "The serviceInterface must be set");
        
        m_serviceRegistry.addListener(m_serviceInterface, this, true);
    }

    /** {@inheritDoc} */
    public void providerRegistered(Registration registration, T provider) {
        m_providerRegistrations.add(provider);
        
        for(RegistrationListener<T> listener : m_listeners) {
            listener.providerRegistered(registration, provider);
        }
    }

    /** {@inheritDoc} */
    public void providerUnregistered(Registration registration, T provider) {
        m_providerRegistrations.remove(provider);
        
        for(RegistrationListener<T> listener : m_listeners) {
            listener.providerUnregistered(registration, provider);
        }
        
    }
    
    /**
     * <p>setListener</p>
     *
     * @param listener a {@link org.opennms.core.soa.RegistrationListener} object.
     */
    public void setListener(RegistrationListener<T> listener) {
    	addListener(listener);
    }
    
    /**
     * <p>addListener</p>
     *
     * @param listener a {@link org.opennms.core.soa.RegistrationListener} object.
     */
    public void addListener(RegistrationListener<T> listener) {
        m_listeners.add((RegistrationListener<T>) listener);
    }
    
    /**
     * <p>removeListener</p>
     *
     * @param listener a {@link org.opennms.core.soa.RegistrationListener} object.
     */
    public void removeListener(RegistrationListener<?> listener) {
        m_listeners.remove(listener);
    }

}
