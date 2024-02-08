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
