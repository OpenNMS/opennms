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
package org.opennms.netmgt.config.poller.outages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.ValidateUsing;

/**
 * Interface to which the outage applies.
 */

@XmlRootElement(name="interface", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class Interface implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * IP address
     */
    @XmlAttribute(name="address")
    private String m_address;

    public Interface() {
    }

    public String getAddress() {
        return m_address;
    }

    public void setAddress(final String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("'address' is a required field and must be either a valid IP address, or 'match-any'!");
        }
        m_address = address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_address);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Interface) {
            final Interface that = (Interface)obj;
            return Objects.equals(this.m_address, that.m_address);
        }
        return false;
    }

    private boolean isValidAddress(final String addr) {
        if (addr == null) return false;
        if ("match-any".equals(addr)) {
            return true;
        }
        try {
            final InetAddress inetAddr = InetAddressUtils.addr(addr);
            if (inetAddr == null) {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }

        return true;
    }
}
