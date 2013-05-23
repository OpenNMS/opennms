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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>
 * OnmsAccessPoint class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@XmlRootElement(name = "accesspoint")
@Entity
@Table(name = "accesspoints")
public class OnmsAccessPoint implements Serializable, Comparable<OnmsAccessPoint> {
    private static final long serialVersionUID = -52686562234234234L;

    private String m_physAddr;
    private Integer m_nodeId;
    private String m_pollingPackage;
    private AccessPointStatus m_status = AccessPointStatus.UNKNOWN;
    private InetAddress m_controllerIpAddr;

    /**
     * <p>
     * Constructor for OnmsAccessPoint.
     * </p>
     * 
     * @param nodeId
     *            a {@link java.lang.Integer} object.
     * @param physAddr
     *            a {@link java.lang.String} object.
     * @param pollingPackage
     *            a {@link java.lang.String} object.
     */
    public OnmsAccessPoint(String physAddr, Integer nodeId, String pollingPackage) {
        m_physAddr = physAddr;
        m_nodeId = nodeId;
        m_pollingPackage = pollingPackage;
    }

    /**
     * <p>
     * Default constructor for OnmsAccessPoint.
     * </p>
     */
    public OnmsAccessPoint() {

    }

    /**
     * <p>
     * getPhysAddr
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Id
    @Column(name = "physAddr", length = 32, nullable = false)
    @XmlElement(name = "physAddr")
    public String getPhysAddr() {
        return m_physAddr;
    }

    /**
     * <p>
     * setPhysAddr
     * </p>
     * 
     * @param physaddr
     *            a {@link java.lang.String} object.
     */
    public void setPhysAddr(String physaddr) {
        m_physAddr = physaddr;
    }

    /**
     * <p>
     * getNodeId
     * </p>
     * 
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "nodeId", nullable = false)
    @XmlAttribute(name = "nodeId")
    public Integer getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>
     * setNodeId
     * </p>
     * 
     * @param nodeId
     *            a {@link java.lang.Integer} object.
     */
    public void setNodeId(Integer nodeId) {
        m_nodeId = nodeId;
    }

    /**
     * <p>
     * getPollingPackage
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "pollingPackage", length = 256)
    @XmlElement(name = "pollingPackage")
    public String getPollingPackage() {
        return m_pollingPackage;
    }

    /**
     * <p>
     * setPollingPackage
     * </p>
     * 
     * @param pollingpackage
     *            a {@link java.lang.String} object.
     */
    public void setPollingPackage(String pollingpackage) {
        m_pollingPackage = pollingpackage;
    }

    /**
     * <p>
     * getStatus
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.model.AccessPointStatus} object.
     */
    @Column(name = "status", nullable = false)
    // @Enumerated(EnumType.ORDINAL)
    @Type(type = "org.opennms.netmgt.model.AccessPointStatusUserType")
    @XmlTransient
    public AccessPointStatus getStatus() {
        return this.m_status;
    }

    /**
     * <p>
     * setStatus
     * </p>
     * 
     * @param severity
     *            a {@link org.opennms.netmgt.model.AccessPointStatus} object.
     */
    public void setStatus(AccessPointStatus status) {
        m_status = status;
    }

    /**
     * <p>
     * getControllerIpAddress
     * </p>
     * 
     * @return a {@link java.net.InetAddress} object.
     */
    @Column(name = "controllerIpAddr")
    @XmlElement(name = "controllerIpAddress")
    @Type(type = "org.opennms.netmgt.model.InetAddressUserType")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getControllerIpAddress() {
        return m_controllerIpAddr;
    }

    /**
     * <p>
     * setControllerIpAddress
     * </p>
     * 
     * @param ipaddr
     *            a {@link java.lang.String} object.
     */
    public void setControllerIpAddress(InetAddress ipaddr) {
        m_controllerIpAddr = ipaddr;
    }

    /**
     * <p>
     * toString
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("physAddr", getPhysAddr())
            .append("pollingPackage", getPollingPackage())
            .append("status", getStatus())
            .append("controllerIpAddr", getControllerIpAddress())
            .toString();
    }

    /**
     * <p>
     * compareTo
     * </p>
     * 
     * @return a {@link java.lang.int} object.
     */
    @Override
    public int compareTo(OnmsAccessPoint o) {
        return m_physAddr.compareTo(o.m_physAddr);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_physAddr == null) ? 0 : m_physAddr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OnmsAccessPoint)) {
            return false;
        }
        OnmsAccessPoint other = (OnmsAccessPoint) obj;
        if (m_physAddr == null) {
            if (other.m_physAddr != null) {
                return false;
            }
        } else if (!m_physAddr.equalsIgnoreCase(other.m_physAddr)) {
            return false;
        }
        return true;
    }
}
