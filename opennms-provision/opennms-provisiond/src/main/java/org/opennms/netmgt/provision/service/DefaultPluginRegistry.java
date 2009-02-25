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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.ExtensionManager;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.Policy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * DefaultPluginRegistry
 *
 * @author brozow
 */
public class DefaultPluginRegistry implements PluginRegistry, InitializingBean {
    
    
    @Autowired(required=false)
    Set<SyncServiceDetector> m_syncDetectors;
    
    @Autowired(required=false)
    Set<AsyncServiceDetector> m_asyncDetectors;
    
    @Autowired(required=false)
    Set<NodePolicy> m_nodePolicies;
    
    @Autowired(required=false)
    Set<IpInterfacePolicy> m_ipInterfacePolicies;
    
    @Autowired(required=false)
    Set<SnmpInterfacePolicy> m_snmpInterfacePolicies;
    
    @Autowired
    ExtensionManager m_extensionManager;
    
    @Autowired
    private ApplicationContext m_applicationContext;
    
    //@PostConstruct
    public void afterPropertiesSet() {
        Assert.notNull(m_extensionManager, "ExtensionManager must not be null");
        addAllExtensions(m_asyncDetectors, AsyncServiceDetector.class, ServiceDetector.class);
        addAllExtensions(m_syncDetectors, SyncServiceDetector.class, ServiceDetector.class);
        addAllExtensions(m_nodePolicies, NodePolicy.class, Policy.class);
        addAllExtensions(m_ipInterfacePolicies, IpInterfacePolicy.class, Policy.class);
        addAllExtensions(m_snmpInterfacePolicies, SnmpInterfacePolicy.class, Policy.class);
    }
    
    private <T> void addAllExtensions(Collection<T> extensions, Class<?>... extensionPoints) {
        if (extensions == null) {
            return;
        }
        for(T extension : extensions) {
            m_extensionManager.registerExtension(extension, extensionPoints);
        }
    }
    
    public <T> Collection<T> getAllPlugins(Class<T> pluginClass) {
        return beansOfType(pluginClass).values();
    }
    
    public <T> T getPluginInstance(Class<T> pluginClass, PluginConfig pluginConfig) {
        T pluginInstance = beanWithNameOfType(pluginConfig.getPluginClass(), pluginClass);
        if (pluginInstance == null) {
            return null;
        }
        
        Map<String, String> parameters = new HashMap<String, String>(pluginConfig.getParameters());

        
        BeanWrapper wrapper = new BeanWrapperImpl(pluginInstance);
        wrapper.setPropertyValues(parameters);
        
        return pluginInstance;
    }

    @SuppressWarnings("unchecked")
    private <T> Map<String, T> beansOfType(Class<T> pluginClass) {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(m_applicationContext, pluginClass, true, true);
    }
    
    private <T> T beanWithNameOfType(String beanName, Class<T> pluginClass) {
        Map<String, T> beans = beansOfType(pluginClass);
        return beans.get(beanName);
    }
    
    
}
