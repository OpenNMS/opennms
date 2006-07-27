//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="ifservices"
 *     
*/
public class OnmsMonitoredService extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7106081378757872886L;
    
    private Integer m_id;

    private Date m_lastGood;

    private Date m_lastFail;

    private String m_qualifier;

    private String m_status;

    private String m_source;

    private String m_notify;

    private OnmsServiceType m_serviceType;

    private OnmsIpInterface m_ipInterface;

    public OnmsMonitoredService() {
    }
    
    public OnmsMonitoredService(OnmsIpInterface ipIf, OnmsServiceType serviceType) {
        m_ipInterface = ipIf;
        m_ipInterface.getMonitoredServices().add(this);
        m_serviceType = serviceType;
        
    }
    
    /**
     * Unique identifier for ifServivce.
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="ifSvcNxtId"
     *         
     */
    public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        m_id = id;
    }
    
    /**
     *  
     * @hibernate.property column="ipAddr" length="16"
     */
    public String getIpAddress() {
        return m_ipInterface.getIpAddress();
    }

    /** 
     *                @hibernate.property
     *                 column="ifindex"
     *                 length="4"
     *             
     */
    public Integer getIfIndex() {
        return m_ipInterface.getIfIndex();
    }

    /** 
     *                @hibernate.property
     *                 column="lastgood"
     *                 length="8"
     *             
     */
    public Date getLastGood() {
        return m_lastGood;
    }

    public void setLastGood(Date lastgood) {
        m_lastGood = lastgood;
    }

    /** 
     *                @hibernate.property
     *                 column="lastfail"
     *                 length="8"
     *             
     */
    public Date getLastFail() {
        return m_lastFail;
    }

    public void setLastFail(Date lastfail) {
        m_lastFail = lastfail;
    }

    /** 
     *                @hibernate.property
     *                 column="qualifier"
     *                 length="16"
     *             
     */
    public String getQualifier() {
        return m_qualifier;
    }

    public void setQualifier(String qualifier) {
        m_qualifier = qualifier;
    }

    /** 
     *                @hibernate.property
     *                 column="status"
     *                 length="1"
     *             
     */
    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    /** 
     *                @hibernate.property
     *                 column="source"
     *                 length="1"
     *             
     */
    public String getSource() {
        return m_source;
    }

    public void setSource(String source) {
        m_source = source;
    }

    /** 
     *                @hibernate.property
     *                 column="notify"
     *                 length="1"
     *             
     */
    public String getNotify() {
        return m_notify;
    }

    public void setNotify(String notify) {
        m_notify = notify;
    }
    
    /**
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="ipIfId" 
     */
    public OnmsIpInterface getIpInterface() {
        return m_ipInterface;
    }
    
    public void setIpInterface(OnmsIpInterface ipInterface) {
        m_ipInterface = ipInterface;
    }

    /** 
     *            @hibernate.property column="nodeid"       
     *         
     */
    public Integer getNodeId() {
        return m_ipInterface.getNode().getId();
    }

    /** 
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="serviceid"         
     *         
     */
    public OnmsServiceType getServiceType() {
        return m_serviceType;
    }

    public void setServiceType(OnmsServiceType service) {
        m_serviceType = service;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipAddr", getIpAddress())
            .append("ifindex", getIfIndex())
            .append("lastgood", getLastGood())
            .append("lastfail", getLastFail())
            .append("qualifier", getQualifier())
            .append("status", getStatus())
            .append("source", getSource())
            .append("notify", getNotify())
            .toString();
    }

    public Integer getServiceId() {
        return getServiceType().getId();
    }

	public void visit(EntityVisitor visitor) {
		visitor.visitMonitoredService(this);
		visitor.visitMonitoredServiceComplete(this);
	}

	public String getServiceName() {
		return getServiceType().getName();
	}

}
