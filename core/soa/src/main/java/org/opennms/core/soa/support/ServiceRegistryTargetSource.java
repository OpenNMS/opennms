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

import org.opennms.core.soa.ServiceRegistry;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;

/**
 * ServiceRegistryTargetSource
 *
 * @author brozow
 * @version $Id: $
 */
public class ServiceRegistryTargetSource extends AbstractLazyCreationTargetSource {
    
    private ServiceRegistry m_serviceRegistry;
    private Class<?> m_serviceInterface;
    private String m_filter;

    /**
     * <p>Constructor for ServiceRegistryTargetSource.</p>
     *
     * @param serviceRegistry a {@link org.opennms.core.soa.ServiceRegistry} object.
     * @param filter 
     * @param serviceInterface a {@link java.lang.Class} object.
     */
    public ServiceRegistryTargetSource(ServiceRegistry serviceRegistry, String filter, Class<?> serviceInterface) {
        m_serviceRegistry = serviceRegistry;
        m_filter = filter;
        m_serviceInterface = serviceInterface;
    }

    /** {@inheritDoc} */
    @Override
    protected Object createObject() throws Exception {
        Object object = m_serviceRegistry.findProvider(m_serviceInterface, m_filter);
        if (object == null) {
            throw new IllegalStateException("Unable to find an object that implements '" + 
                    m_serviceInterface + 
                    (m_filter == null ? "'" : " matching filter "+m_filter));
        }
        return object;
    }

}
