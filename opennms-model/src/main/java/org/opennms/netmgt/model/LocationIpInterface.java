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
package org.opennms.netmgt.model;

import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.springframework.util.Assert;

import com.google.common.base.MoreObjects;


/**
 * <p>LocationMonitorIpInterface class.</p>
 */
public class LocationIpInterface {
    private final OnmsMonitoringLocation m_location;
    private final OnmsIpInterface m_ipInterface;

    /**
     * <p>Constructor for LocationMonitorIpInterface.</p>
     *
     * @param location a {@link org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation} object.
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public LocationIpInterface(final OnmsMonitoringLocation location, final OnmsIpInterface ipInterface) {
        Assert.notNull(location);
        Assert.notNull(ipInterface);
        m_location = location;
        m_ipInterface = ipInterface;
    }

    /**
     * <p>getIpInterface</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public OnmsIpInterface getIpInterface() {
        return m_ipInterface;
    }

    /**
     * <p>getLocation</p>
     *
     * @return a {@link org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation} object.
     */
    public OnmsMonitoringLocation getLocation() {
        return m_location;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("location", m_location)
            .add("ipInterface", m_ipInterface)
            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_ipInterface.getId().hashCode();
        result = prime * result + m_location.getLocationName().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof LocationIpInterface))
            return false;
        LocationIpInterface other = (LocationIpInterface) obj;
        if (m_ipInterface.getId() == null) {
            if (other.m_ipInterface.getId() != null)
                return false;
        } else if (!m_ipInterface.getId().equals(other.m_ipInterface.getId()))
            return false;
        if (m_location.getLocationName() == null) {
            if (other.m_location.getLocationName() != null)
                return false;
        } else if (!m_location.getLocationName().equals(other.m_location.getLocationName()))
            return false;
        return true;
    }
}
