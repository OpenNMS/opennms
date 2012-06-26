/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

/**
 * <p>DefaultServiceMonitorLocator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.Map;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

public class DefaultServiceMonitorLocator implements ServiceMonitorLocator, Serializable {

    private static final long serialVersionUID = 4852206182208816721L;

    String m_serviceName;
    Class<? extends ServiceMonitor> m_serviceClass;
    
    /**
     * <p>Constructor for DefaultServiceMonitorLocator.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param serviceClass a {@link java.lang.Class} object.
     */
    public DefaultServiceMonitorLocator(String serviceName, Class<? extends ServiceMonitor> serviceClass) {
        m_serviceName = serviceName;
        m_serviceClass = serviceClass;
    }

    /*
     * FIXME The use of CastorObjectRetrievalFailureException doesn't seem
     * appropriate below, as I don't see Castor being used at all. - dj@opennms.org
     */
    /**
     * <p>getServiceMonitor</p>
     *
     * @return a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
     */
    public ServiceMonitor getServiceMonitor() {
        try {
            ServiceMonitor mon = m_serviceClass.newInstance();
            mon.initialize((Map<String,Object>)null);
            return mon;
        } catch (InstantiationException e) {
            throw new ConfigObjectRetrievalFailureException("Unable to instantiate monitor for service "
                    +m_serviceName+" with class-name "+m_serviceClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigObjectRetrievalFailureException("Illegal access trying to instantiate monitor for service "
                    +m_serviceName+" with class-name "+m_serviceClass.getName(), e);
        }
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getServiceLocatorKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceLocatorKey() {
        return m_serviceClass.getName();
    }
    
}
