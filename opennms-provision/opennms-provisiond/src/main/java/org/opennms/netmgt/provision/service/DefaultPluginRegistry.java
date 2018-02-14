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

package org.opennms.netmgt.provision.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.OnmsPolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * DefaultPluginRegistry
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultPluginRegistry implements PluginRegistry, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginRegistry.class);

    @Autowired(required=false)
    Set<NodePolicy> m_nodePolicies;
    
    @Autowired(required=false)
    Set<IpInterfacePolicy> m_ipInterfacePolicies;
    
    @Autowired(required=false)
    Set<SnmpInterfacePolicy> m_snmpInterfacePolicies;

    @Autowired
    ServiceRegistry m_serviceRegistry;

    @Autowired
    private ApplicationContext m_applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        addAllExtensions(m_nodePolicies, NodePolicy.class, OnmsPolicy.class);
        addAllExtensions(m_ipInterfacePolicies, IpInterfacePolicy.class, OnmsPolicy.class);
        addAllExtensions(m_snmpInterfacePolicies, SnmpInterfacePolicy.class, OnmsPolicy.class);
    }
    
    private static void debug(String format, Object... args) {
        LOG.debug(format, args);
    }
    
    private static void info(String format, Object... args) {
        LOG.info(format, args);
    }
    
    private static void error(Throwable cause, String format, Object... args) {
        if (cause == null) {
            LOG.error(format, args);
        } else {
            LOG.error(format, args, cause);
        }
    }
    
    private <T> void addAllExtensions(Collection<T> extensions, Class<?>... extensionPoints) {
        if (extensions == null || extensions.isEmpty()) {
            info("Found NO Extensions for ExtensionPoints {}", Arrays.toString(extensionPoints));
            return;
        }
        for(T extension : extensions) {
            info("Register Extension {} for ExtensionPoints {}", extension, Arrays.toString(extensionPoints));
            m_serviceRegistry.register(extension, extensionPoints);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> Collection<T> getAllPlugins(Class<T> pluginClass) {
        return beansOfType(pluginClass).values();
    }
    
    /** {@inheritDoc} */
    @Override
    public <T> T getPluginInstance(Class<T> pluginClass, PluginConfig pluginConfig) {
        T pluginInstance = beanWithNameOfType(pluginConfig.getPluginClass(), pluginClass);
        if (pluginInstance == null) {
            return null;
        }
        
        Map<String, String> parameters = new HashMap<String, String>(pluginConfig.getParameterMap());


        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(pluginInstance);
        try {
            wrapper.setPropertyValues(parameters);
        } catch (BeansException e) {
            error(e, "Could not set properties on report definition: {}", e.getMessage());
        }
        
        return pluginInstance;
    }

    private <T> Map<String, T> beansOfType(Class<T> pluginClass) {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(m_applicationContext, pluginClass, true, true);
    }
    
    private <T> T beanWithNameOfType(String beanName, Class<T> pluginClass) {
        Map<String, T> beans = beansOfType(pluginClass);
        T bean = beans.get(beanName);
        if (bean != null) {
            debug("Found bean {} with name {} of type {}", bean, beanName, pluginClass);
        } else {
            debug("Failed to find bean {} with name {} of type {}", bean, beanName, pluginClass);
        }
        return bean;
    }
    
}
