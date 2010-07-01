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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.core.style.ToStringCreator;


@Entity
/**
 * <p>OnmsServerMap class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Table(name="servermap")
public class OnmsServerMap extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -858347716282069343L;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_serverName;

    private Integer m_id;

    /**
     * full constructor
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param serverName a {@link java.lang.String} object.
     */
    public OnmsServerMap(String ipAddr, String serverName) {
        m_ipAddr = ipAddr;
        m_serverName = serverName;
    }

    /**
     * default constructor
     */
    public OnmsServerMap() {
    }

    
    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(name="id")
    @SequenceGenerator(name="serverMapSequence", sequenceName="svrMapNxtId")
    @GeneratedValue(generator="outageSequence")
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
        return m_ipAddr;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    
    /**
     * <p>getServerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="serverName", length=64)
    public String getServerName() {
        return m_serverName;
    }

    /**
     * <p>setServerName</p>
     *
     * @param serverName a {@link java.lang.String} object.
     */
    public void setServerName(String serverName) {
        m_serverName = serverName;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
            .append("ipAddr", getIpAddress())
            .append("serverName", getServerName())
            .toString();
    }

	/** {@inheritDoc} */
	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
	}

}
