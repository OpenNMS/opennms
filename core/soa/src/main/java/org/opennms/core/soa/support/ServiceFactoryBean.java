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

import java.util.Map;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * ServiceRegistrationBean
 *
 * @author brozow
 * @version $Id: $
 */
public class ServiceFactoryBean implements FactoryBean<Registration>, BeanFactoryAware, InitializingBean, DisposableBean {
    
    private BeanFactory m_beanFactory;
    private ServiceRegistry m_serviceRegistry;
    private String m_targetBeanName;
    private Object m_target;
    private Class<?>[] m_serviceInterfaces;
    private Map<String, String> m_serviceProperties;

    private Registration m_registration;

    
    /** {@inheritDoc} */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        m_beanFactory = beanFactory;
    }
    
    /**
     * <p>setTargetBeanName</p>
     *
     * @param targetBeanName a {@link java.lang.String} object.
     */
    public void setTargetBeanName(String targetBeanName) {
        m_targetBeanName = targetBeanName;
    }
    
    /**
     * <p>setTarget</p>
     *
     * @param target a {@link java.lang.Object} object.
     */
    public void setTarget(Object target) {
        m_target = target;
    }
    
    /**
     * <p>setInterfaces</p>
     *
     * @param serviceInterfaces an array of {@link java.lang.Class} objects.
     */
    public void setInterfaces(Class<?>[] serviceInterfaces) {
        m_serviceInterfaces = serviceInterfaces;
    }
    
    /**
     * <p>setServiceProperties</p>
     *
     * @param serviceInterfaces an array of {@link java.lang.Class} objects.
     */
    public void setServiceProperties(Map<String, String> serviceProperties) {
        m_serviceProperties = serviceProperties;
    }
    
    /**
     * <p>setServiceRegistry</p>
     *
     * @param serviceRegistry a {@link org.opennms.core.soa.ServiceRegistry} object.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        m_serviceRegistry = serviceRegistry;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        
        boolean hasText = StringUtils.hasText(m_targetBeanName);
        Assert.isTrue(hasText || m_target != null, "targetBeanName or target must be set");
        Assert.notEmpty(m_serviceInterfaces, "interfaces must be set");

        
        if (m_target == null) {
            Assert.notNull(m_beanFactory, "beanFactory must not be null");
        }
        
        Object provider = m_target != null ? m_target : m_beanFactory.getBean(m_targetBeanName);
        
        m_registration = m_serviceRegistry.register(provider, m_serviceProperties, m_serviceInterfaces);

    }
    

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() throws Exception {
        if ( m_registration != null ) {
            m_registration.unregister();
        }
    }

    /**
     * <p>getObject</p>
     *
     * @return a {@link org.opennms.core.soa.Registration} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Registration getObject() throws Exception {
        return m_registration;
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Override
    public Class<? extends Registration> getObjectType() {
        return (m_registration == null ? Registration.class : m_registration.getClass());
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



    
    
}
