
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.core.style.ToStringCreator;


@Entity
/**
 * <p>OnmsIpInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Table(name="ipInterface")
public class OnmsIpInterface extends OnmsEntity implements Serializable {


    @Embeddable
    public static class CollectionType implements Comparable<CollectionType>, Serializable {
        private static final long serialVersionUID = -647348487361201657L;
        private static final char[] s_order = { 'N', 'C', 'S', 'P' };
        char m_collType = 'N';

        @SuppressWarnings("unused")
        private CollectionType() {
        }

        public CollectionType(char collType) {
            m_collType = collType;
        }

        @Column(name="isSnmpPrimary")
        public char getCharCode() {
            return m_collType;
        }

        public void setCharCode(char collType) {
            m_collType = collType;
        }

        public int compareTo(CollectionType collType) {
            return getIndex(m_collType) - getIndex(collType.m_collType);
        }

        private static int getIndex(char code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) return i;
            }
            throw new IllegalArgumentException("illegal collType code '"+code+"'");
        }

        public boolean equals(CollectionType o) {
            if (o == null)
                return false;
            return m_collType == o.m_collType;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return String.valueOf(m_collType);
        }

        public boolean isLessThan(CollectionType collType) {
            return compareTo(collType) < 0;
        }

        public boolean isGreaterThan(CollectionType collType) {
            return compareTo(collType) > 0;
        }

        public CollectionType max(CollectionType collType) {
            return this.isLessThan(collType) ? collType : this;
        }

        public CollectionType min(CollectionType collType) {
            return this.isLessThan(collType) ? this : collType;
        }

        public static CollectionType get(char code) {
            switch (code) {
            case 'P': return PRIMARY;
            case 'S': return SECONDARY;
            case 'C': return COLLECT;
            case 'N': return NO_COLLECT;
            default:
                throw new IllegalArgumentException("Cannot create collType from code "+code);
            }
        }

        public static CollectionType get(String code) {
            if (code == null)
                return NO_COLLECT;
            code = code.trim();
            if (code.length() < 1)
                return NO_COLLECT;
            else if (code.length() > 1)
                throw new IllegalArgumentException("Cannot convert string "+code+" to a collType");
            else
                return get(code.charAt(0));
        }

        public static CollectionType PRIMARY = new CollectionType('P');
        public static CollectionType SECONDARY = new CollectionType('S');
        public static CollectionType COLLECT = new CollectionType('C');
        public static CollectionType NO_COLLECT = new CollectionType('N');


    }

    private static final long serialVersionUID = 7750043250236397014L;

    private Integer m_id;

    private String m_ipAddress;

    private String m_ipHostName;

    private String m_isManaged;

    private Integer m_ipStatus;

    private Date m_ipLastCapsdPoll;

    private CollectionType m_isSnmpPrimary = CollectionType.NO_COLLECT;

    private OnmsNode m_node;

    private Set<OnmsMonitoredService> m_monitoredServices = new HashSet<OnmsMonitoredService>();

    private OnmsSnmpInterface m_snmpInterface;

    /**
     * <p>Constructor for OnmsIpInterface.</p>
     */
    public OnmsIpInterface() {
    }

    /**
     * minimal constructor
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsIpInterface(String ipAddr, OnmsNode node) {
        m_ipAddress = ipAddr;
        m_node = node;
        node.getIpInterfaces().add(this);
    }

    /**
     * Unique identifier for ipInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }



    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ipAddr", length=16)
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipaddr) {
        m_ipAddress = ipaddr;
    }

    //@Column(name="ifIndex")
    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    public Integer getIfIndex() {
        if (m_snmpInterface == null) {
            return null;
        }
        return m_snmpInterface.getIfIndex();
        //return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifindex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer ifindex) {
        if (m_snmpInterface == null) {
            throw new IllegalStateException("Cannot set ifIndex if snmpInterface relation isn't setup");
        }
        m_snmpInterface.setIfIndex(ifindex);
        //m_ifIndex = ifindex;
    }

    /**
     * <p>getIpHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ipHostName", length=256)
    public String getIpHostName() {
        return m_ipHostName;
    }

    /**
     * <p>setIpHostName</p>
     *
     * @param iphostname a {@link java.lang.String} object.
     */
    public void setIpHostName(String iphostname) {
        m_ipHostName = iphostname;
    }

    /**
     * <p>getIsManaged</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="isManaged", length=1)
    public String getIsManaged() {
        return m_isManaged;
    }

    /**
     * <p>setIsManaged</p>
     *
     * @param ismanaged a {@link java.lang.String} object.
     */
    public void setIsManaged(String ismanaged) {
        m_isManaged = ismanaged;
    }

    /**
     * <p>getIpStatus</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="ipStatus")
    public Integer getIpStatus() {
        return m_ipStatus;
    }

    /**
     * <p>setIpStatus</p>
     *
     * @param ipstatus a {@link java.lang.Integer} object.
     */
    public void setIpStatus(Integer ipstatus) {
        m_ipStatus = ipstatus;
    }

    /**
     * <p>getIpLastCapsdPoll</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ipLastCapsdPoll")
    public Date getIpLastCapsdPoll() {
        return m_ipLastCapsdPoll;
    }

    /**
     * <p>setIpLastCapsdPoll</p>
     *
     * @param iplastcapsdpoll a {@link java.util.Date} object.
     */
    public void setIpLastCapsdPoll(Date iplastcapsdpoll) {
        m_ipLastCapsdPoll = iplastcapsdpoll;
    }

    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface.CollectionType} object.
     */
    @Column(name="isSnmpPrimary", length=1)
    public CollectionType getIsSnmpPrimary() {
        return m_isSnmpPrimary;
    }

    /**
     * <p>setIsSnmpPrimary</p>
     *
     * @param issnmpprimary a {@link org.opennms.netmgt.model.OnmsIpInterface.CollectionType} object.
     */
    public void setIsSnmpPrimary(CollectionType issnmpprimary) {
        m_isSnmpPrimary = issnmpprimary;
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        m_node = node;
    }

    /**
     * The services on this node
     *
     * @return a {@link java.util.Set} object.
     */
    @OneToMany(mappedBy="ipInterface")
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
        public Set<OnmsMonitoredService> getMonitoredServices() {
        return m_monitoredServices ;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param ifServices a {@link java.util.Set} object.
     */
    public void setMonitoredServices(Set<OnmsMonitoredService> ifServices) {
        m_monitoredServices = ifServices;
    }


    /**
     * The SnmpInterface associated with this interface if any
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    @ManyToOne(optional=true, fetch=FetchType.LAZY)
    @JoinColumn(name="snmpInterfaceId")
    public OnmsSnmpInterface getSnmpInterface() {
        return m_snmpInterface;
    }


    /**
     * <p>setSnmpInterface</p>
     *
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public void setSnmpInterface(OnmsSnmpInterface snmpInterface) {
        m_snmpInterface = snmpInterface;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
        .append("ipaddr", getIpAddress())
        .append("ifindex", getIfIndex())
        .append("iphostname", getIpHostName())
        .append("ismanaged", getIsManaged())
        .append("ipstatus", getIpStatus())
        .append("iplastcapsdpoll", getIpLastCapsdPoll())
        .append("issnmpprimary", getIsSnmpPrimary())
        .toString();
    }

    /** {@inheritDoc} */
    public void visit(EntityVisitor visitor) {
        visitor.visitIpInterface(this);

        for (OnmsMonitoredService monSvc : getMonitoredServices()) {
            monSvc.visit(visitor);
        }

        visitor.visitIpInterfaceComplete(this);
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Transient
    public InetAddress getInetAddress() {
        String ipAddr = getIpAddress();
        if (ipAddr == null) return null;

        InetAddress addr = null;
        try {
            String hostName = getIpHostName() == null ? ipAddr : getIpHostName();
            addr = InetAddress.getByName(ipAddr);
            addr = InetAddress.getByAddress(hostName, addr.getAddress());
        } catch (UnknownHostException e) {
            // this can't happen here
        }
        return addr;
    }

    /**
     * <p>isDown</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isDown() {
        boolean down = true;
        for (OnmsMonitoredService svc : m_monitoredServices) {
            if (!svc.isDown()) {
                return !down;
            }
        }
        return down;
    }

    /**
     * <p>getMonitoredServiceByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public OnmsMonitoredService getMonitoredServiceByServiceType(String svcName) {
        for (OnmsMonitoredService monSvc : getMonitoredServices()) {
            if (monSvc.getServiceType().getName().equals(svcName)) {
                return monSvc;
            }
        }
        return null;
    }

}
