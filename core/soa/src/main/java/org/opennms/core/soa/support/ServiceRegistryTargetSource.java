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
