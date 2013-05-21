/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import org.springframework.core.style.ToStringCreator;


/**
 * <p>OnmsServiceMap class.</p>
 */
public class OnmsServiceMap extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6550492519192174055L;
    
    private Integer m_id;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_serviceMapName;

    /**
     * full constructor
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceMapName a {@link java.lang.String} object.
     */
    public OnmsServiceMap(String ipAddr, String serviceMapName) {
        this.m_ipAddr = ipAddr;
        this.m_serviceMapName = serviceMapName;
    }

    /**
     * default constructor
     */
    public OnmsServiceMap() {
    }
    

/**
 * <p>getId</p>
 *
 * @hibernate.id generator-class="native" column="id"
 * @hibernate.generator-param name="sequence" value="svcMapNxtId"
 * @return a {@link java.lang.Integer} object.
 */
public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        this.m_id = id;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return this.m_ipAddr;
    }

    /**
     * <p>setIpAddr</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     */
    public void setIpAddr(String ipAddr) {
        this.m_ipAddr = ipAddr;
    }

    /**
     * <p>getServiceMapName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceMapName() {
        return this.m_serviceMapName;
    }

    /**
     * <p>setServiceMapName</p>
     *
     * @param serviceMapName a {@link java.lang.String} object.
     */
    public void setServiceMapName(String serviceMapName) {
        this.m_serviceMapName = serviceMapName;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("ipAddr", getIpAddr())
            .append("serviceMapName", getServiceMapName())
            .toString();
    }

	/** {@inheritDoc} */
    @Override
	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
		
	}

}
