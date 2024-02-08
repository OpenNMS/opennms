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
package org.opennms.core.soa.support;

import java.util.Arrays;
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
        m_serviceInterfaces = Arrays.copyOf(serviceInterfaces, serviceInterfaces.length);
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
