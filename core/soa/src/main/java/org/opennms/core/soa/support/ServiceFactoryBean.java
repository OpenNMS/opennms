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
 */
public class ServiceFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean, DisposableBean {
    
    private BeanFactory m_beanFactory;
    private ServiceRegistry m_serviceRegistry;
    private String m_targetBeanName;
    private Object m_target;
    private Class<?>[] m_serviceInterfaces;

    private Registration m_registration;

    
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        m_beanFactory = beanFactory;
    }
    
    public void setTargetBeanName(String targetBeanName) {
        m_targetBeanName = targetBeanName;
    }
    
    public void setTarget(Object target) {
        m_target = target;
    }
    
    public void setInterfaces(Class<?>[] serviceInterfaces) {
        m_serviceInterfaces = serviceInterfaces;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        m_serviceRegistry = serviceRegistry;
    }
    
    public void afterPropertiesSet() throws Exception {
        
        boolean hasText = StringUtils.hasText(m_targetBeanName);
        Assert.isTrue(hasText || m_target != null, "targetBeanName or target must be set");
        Assert.notEmpty(m_serviceInterfaces, "interfaces must be set");

        
        if (m_target == null) {
            Assert.notNull(m_beanFactory, "beanFactory must not be null");
        }
        
        Object provider = m_target != null ? m_target : m_beanFactory.getBean(m_targetBeanName);
        
        m_registration = m_serviceRegistry.register(provider, m_serviceInterfaces);

    }
    

    public void destroy() throws Exception {
        if ( m_registration != null ) {
            m_registration.unregister();
        }
    }

    public Object getObject() throws Exception {
        return m_registration;
    }

    public Class<?> getObjectType() {
        return (m_registration == null ? Registration.class : m_registration.getClass());
    }

    public boolean isSingleton() {
        return true;
    }



    
    
}
