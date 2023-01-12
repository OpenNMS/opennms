/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

/**
 * <p>DefaultServiceMonitorLocator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.Objects;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;

import com.google.common.base.MoreObjects;

public class DefaultServiceMonitorLocator implements ServiceMonitorLocator, Serializable {

    String m_serviceName;
    String m_serviceClass;
    
    /**
     * <p>Constructor for DefaultServiceMonitorLocator.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param serviceClass a {@link java.lang.Class} object.
     */
    public DefaultServiceMonitorLocator(String serviceName, String serviceClass) {
        m_serviceName = serviceName;
        m_serviceClass = serviceClass;
    }

    @Override
    public ServiceMonitor getServiceMonitor(ServiceMonitorRegistry registry) {
        return registry.getMonitorByClassName(m_serviceClass);
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getServiceLocatorKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getServiceLocatorKey() {
        return m_serviceClass;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultServiceMonitorLocator)) {
            return false;
        }
        final DefaultServiceMonitorLocator that = (DefaultServiceMonitorLocator) o;
        return Objects.equals(this.m_serviceName, that.m_serviceName) &&
               Objects.equals(this.m_serviceClass, that.m_serviceClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.m_serviceName, this.m_serviceClass);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("serviceName", this.m_serviceName)
                          .add("serviceClass", this.m_serviceClass)
                          .toString();
    }
}
