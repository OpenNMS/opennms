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
package org.opennms.netmgt.provision.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

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
        addAllExtensions(m_nodePolicies, NodePolicy.class, OnmsPolicy.class);
        addAllExtensions(m_ipInterfacePolicies, IpInterfacePolicy.class, OnmsPolicy.class);
        addAllExtensions(m_snmpInterfacePolicies, SnmpInterfacePolicy.class, OnmsPolicy.class);
    }
    
    private static void trace(String format, Object... args) {
        LOG.trace(format, args);
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
    public <T> T getPluginInstance(Class<T> pluginClass, @Valid PluginConfig pluginConfig) {
        T pluginInstance = beanWithNameOfType(pluginConfig.getPluginClass(), pluginClass);
        if (pluginInstance == null) {
            return null;
        }
        
        Map<String, String> parameters = new HashMap<>(pluginConfig.getParameterMap());


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
            // At this point we know, that the bean isn't found or the bean does not match the given super class.
            // So we now check whether the bean could be found for the base class...
            final OnmsPolicy onmsPolicy = beansOfType(OnmsPolicy.class).get(beanName);

            if (onmsPolicy == null) {
                // if not, the policy definition seems to be broken
                error(null,"Policy class not found or not a policy class: '{}' of type {}", beanName, pluginClass);
            } else {
                trace("Bean {} with name {} is a policy, but does not match requested type {}", bean, beanName, pluginClass);
            }
        }
        return bean;
    }
    
}
