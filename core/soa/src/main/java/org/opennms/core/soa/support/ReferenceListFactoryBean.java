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
 */
public class ReferenceListFactoryBean implements FactoryBean, InitializingBean, RegistrationListener {
    
    private ServiceRegistry m_serviceRegistry;
    private Class<?> m_serviceInterface;
    private List<RegistrationListener<Object>> m_listeners = new CopyOnWriteArrayList<RegistrationListener<Object>>();
    
    private List<Object> m_providerRegistrations = new CopyOnWriteArrayList<Object>();

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        m_serviceRegistry = serviceRegistry;
    }
    
    public void setServiceInterface(Class<?> serviceInterface) {
        m_serviceInterface = serviceInterface;
    }
    
    public Object getObject() throws Exception {
        return m_providerRegistrations;
    }
    
    public Class<?> getObjectType() {
        return List.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_serviceRegistry, "The serviceRegistry must be set");
        Assert.notNull(m_serviceInterface, "The serviceInterface must be set");
        
        m_serviceRegistry.addListener(m_serviceInterface, this, true);
    }

    public void providerRegistered(Registration registration, Object provider) {
        m_providerRegistrations.add(provider);
        
        for(RegistrationListener<Object> listener : m_listeners) {
            listener.providerRegistered(registration, provider);
        }
    }

    public void providerUnregistered(Registration registration, Object provider) {
        m_providerRegistrations.remove(provider);
        
        for(RegistrationListener<Object> listener : m_listeners) {
            listener.providerUnregistered(registration, provider);
        }
        
    }
    
    public void setListener(RegistrationListener<?> listener) {
    	addListener(listener);
    }
    
    @SuppressWarnings("unchecked")
    public void addListener(RegistrationListener<?> listener) {
        m_listeners.add((RegistrationListener<Object>) listener);
    }
    
    public void removeListener(RegistrationListener<?> listener) {
        m_listeners.remove(listener);
    }

}
