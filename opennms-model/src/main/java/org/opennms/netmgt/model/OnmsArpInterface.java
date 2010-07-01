
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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.core.style.ToStringCreator;


@Entity
/**
 * <p>OnmsArpInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Table(name="atInterface")
public class OnmsArpInterface implements Serializable {

    @Embeddable
    public static class StatusType implements Comparable<StatusType>, Serializable {
        private static final long serialVersionUID = -4784344871599250528L;
        private static final char[] s_order = {'A', 'N', 'D', 'K' };
        private char m_statusType;

        @SuppressWarnings("unused")
        private StatusType() {
        }

        public StatusType(char statusType) {
            m_statusType = statusType;
        }

        @Column(name="status")
        public char getCharCode() {
            return m_statusType;
        }

        public void setCharCode(char statusType) {
            m_statusType = statusType;
        }

        public int compareTo(StatusType o) {
            return getIndex(m_statusType) - getIndex(o.m_statusType);
        }

        private static int getIndex(char code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal statusType code '"+code+"'");
        }

        public boolean equals(Object o) {
            if (o instanceof StatusType) {
                return m_statusType == ((StatusType)o).m_statusType;
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return String.valueOf(m_statusType);
        }

        public static StatusType get(char code) {
            switch (code) {
            case 'A': return ACTIVE;
            case 'N': return INACTIVE;
            case 'D': return DELETED;
            case 'K': return UNKNOWN;
            default:
                throw new IllegalArgumentException("Cannot create statusType from code "+code);
            }
        }

        public static StatusType get(String code) {
            if (code == null)
                return UNKNOWN;
            code = code.trim();
            if (code.length() < 1)
                return UNKNOWN;
            else if (code.length() > 1)
                throw new IllegalArgumentException("Cannot convert string "+code+" to a StatusType");
            else
                return get(code.charAt(0));
        }

        public static StatusType ACTIVE = new StatusType('A');
        public static StatusType INACTIVE = new StatusType('N');
        public static StatusType DELETED = new StatusType('D');
        public static StatusType UNKNOWN = new StatusType('K');


    }

    private static final long serialVersionUID = 7750043250236397014L;

    private Integer m_id;

    private OnmsNode m_node;
    
    private String m_ipAddress;

    private String m_physAddr;

    private StatusType m_status = StatusType.UNKNOWN;

    private OnmsNode m_sourceNode;

    private Integer m_ifIndex;

    private Date m_lastPoll;

    /**
     * <p>Constructor for OnmsArpInterface.</p>
     */
    public OnmsArpInterface() {
    }

    /**
     * minimal constructor
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param physAddr a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsArpInterface(String ipAddr, String physAddr, OnmsNode node) {
        m_ipAddress = ipAddr;
        m_physAddr = physAddr;
        m_node = node;
        node.getArpInterfaces().add(this);
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
    public void setNode(OnmsNode node) {
        m_node = node;
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
    
    /**
     * <p>getPhysAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="atPhysAddr", length=32)
    public String getPhysAddr() {
        return m_physAddr;
    }

    /**
     * <p>setPhysAddr</p>
     *
     * @param physAddr a {@link java.lang.String} object.
     */
    public void setPhysAddr(String physAddr) {
        m_physAddr = physAddr;
    }
    
    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsArpInterface.StatusType} object.
     */
    @Column(name="status", length=1)
    public StatusType getStatus() {
        return m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a {@link org.opennms.netmgt.model.OnmsArpInterface.StatusType} object.
     */
    public void setStatus(StatusType status) {
        m_status = status;
    }

    /**
     * <p>getSourceNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="sourceNodeId")
    public OnmsNode getSourceNode() {
        return m_sourceNode;
    }

    /**
     * <p>setSourceNode</p>
     *
     * @param sourceNode a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setSourceNode(OnmsNode sourceNode) {
        m_sourceNode = sourceNode;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="ifIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }
    
    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer ifIndex) {
        m_ifIndex = ifIndex;
    }


    /**
     * <p>getLastPoll</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastPollTime")
    public Date getLastPoll() {
        return m_lastPoll;
    }

    /**
     * <p>setLastPoll</p>
     *
     * @param lastPoll a {@link java.util.Date} object.
     */
    public void setLastPoll(Date lastPoll) {
        m_lastPoll = lastPoll;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
        .append("ipaddr", getIpAddress())
        .append("physaddr", getPhysAddr())
        .append("status", getStatus())
        .append("sourcenode", getSourceNode())
        .append("ifindex", getIfIndex())
        .append("lastpoll", getLastPoll())
        .toString();
    }

}
