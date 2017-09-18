/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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
