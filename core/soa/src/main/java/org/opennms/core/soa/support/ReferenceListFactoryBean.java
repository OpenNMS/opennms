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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.soa.Filter;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.filter.FilterParser;
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
    private Filter m_filter;

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
    
    public void setFilter(String filter) {
        m_filter = (filter == null ? null : new FilterParser().parse(filter));
    }
    
    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public List<T> getObject() throws Exception {
        return m_providerRegistrations;
    }
    
    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Override
    public Class<?> getObjectType() {
        return List.class;
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
        
        m_serviceRegistry.addListener(m_serviceInterface, this, true);
    }

    /** {@inheritDoc} */
    @Override
    public void providerRegistered(Registration registration, T provider) {
        
        if (m_filter != null && !m_filter.match(registration.getProperties())) {
            // this object doesn't match the filter so skip it
            return;
        }
        
        m_providerRegistrations.add(provider);
        
        for(RegistrationListener<T> listener : m_listeners) {
            listener.providerRegistered(registration, provider);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void providerUnregistered(Registration registration, T provider) {
        boolean found = m_providerRegistrations.remove(provider);

        if (!found) {
            // this object didn't belong to the match registrations so do nothing
            return;
        }
        
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
