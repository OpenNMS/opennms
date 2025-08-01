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
package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * list of IP address or IP address mask values to which
 *  this system definition applies.
 */

@XmlRootElement(name="ipList", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class IpList implements Serializable {
    private static final long serialVersionUID = -6384287387760637940L;

    /**
     * List of IP addresses
     */
    @XmlElement(name="ipAddr")
    private List<String> m_ipAddresses = new ArrayList<>();

    /**
     * List of IP address masks
     */
    @XmlElement(name="ipAddrMask")
    private List<String> m_ipAddressMasks = new ArrayList<>();

    public List<String> getIpAddresses() {
        if (m_ipAddresses == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_ipAddresses);
        }
    }

    public void setIpAddresses(final List<String> ipAddrs) {
        m_ipAddresses = new ArrayList<String>(ipAddrs);
    }

    public void addIpAddress(final String ipAddr) throws IndexOutOfBoundsException {
        m_ipAddresses.add(ipAddr.intern());
    }

    public boolean removeIpAddress(final String ipAddr) {
        return m_ipAddresses.remove(ipAddr);
    }

    public List<String> getIpAddressMasks() {
        if (m_ipAddressMasks == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_ipAddressMasks);
        }
    }

    public void setIpAddressMasks(final List<String> masks) {
        m_ipAddressMasks = new ArrayList<String>(masks);
    }

    public void addIpAddressMask(final String mask) throws IndexOutOfBoundsException {
        m_ipAddressMasks.add(mask.intern());
    }

    public boolean removeIpAddressMask(final String mask) {
        return m_ipAddressMasks.remove(mask);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_ipAddressMasks == null) ? 0 : m_ipAddressMasks.hashCode());
        result = prime * result + ((m_ipAddresses == null) ? 0 : m_ipAddresses.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IpList)) {
            return false;
        }
        final IpList other = (IpList) obj;
        if (m_ipAddressMasks == null) {
            if (other.m_ipAddressMasks != null) {
                return false;
            }
        } else if (!m_ipAddressMasks.equals(other.m_ipAddressMasks)) {
            return false;
        }
        if (m_ipAddresses == null) {
            if (other.m_ipAddresses != null) {
                return false;
            }
        } else if (!m_ipAddresses.equals(other.m_ipAddresses)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IpList [ipAddresses=" + m_ipAddresses + ", ipAddressMasks=" + m_ipAddressMasks + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitIpList(this);
        visitor.visitIpListComplete();
    }

}
