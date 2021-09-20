/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
