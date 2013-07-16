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

/*
 * Created on 9-mar-2006
 *
 * Class that holds informations for node bridges
 */
package org.opennms.netmgt.linkd;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>CdpInterface class.</p>
 *
 * @author antonio
 * @version $Id: $
 */
public class CdpInterface {
	
	/**
	 * the int that indicated cdp address type
	 * 
	 */

	public static final int CDP_ADDRESS_TYPE_IP_ADDRESS = 1;

	/**
	 * the ip address 
	 */
	
	private final int m_cdpIfIndex; 
	
	int m_cdpTargetNodeId;
	
	int m_cdpTargetIfIndex;
	
	String m_cdpTargetDeviceId;
	
	CdpInterface(int ifindex) {
		m_cdpIfIndex = ifindex;
	}
	
	/**
	 * <p>Getter for the field <code>cdpIfIndex</code>.</p>
	 *
	 * @return Returns the cdpIfIndex.
	 */
	public int getCdpIfIndex() {
		return m_cdpIfIndex;
	}
	/**
	 * <p>Getter for the field <code>cdpTargetIfIndex</code>.</p>
	 *
	 * @return Returns the cdpTargetDevicePort.
	 */
	public int getCdpTargetIfIndex() {
		return m_cdpTargetIfIndex;
	}
	/**
	 * <p>Setter for the field <code>cdpTargetIfIndex</code>.</p>
	 *
	 * @param ifindex a int.
	 */
	public void setCdpTargetIfIndex(int ifindex) {
		m_cdpTargetIfIndex = ifindex;
	}
	/**
	 * <p>Getter for the field <code>cdpTargetNodeId</code>.</p>
	 *
	 * @return Returns the cdpTargetNodeId.
	 */
	public int getCdpTargetNodeId() {
		return m_cdpTargetNodeId;
	}
	/**
	 * <p>Setter for the field <code>cdpTargetNodeId</code>.</p>
	 *
	 * @param cdpTargetNodeId The cdpTargetNodeId to set.
	 */
	public void setCdpTargetNodeId(int cdpTargetNodeId) {
		m_cdpTargetNodeId = cdpTargetNodeId;
	}
	
	public String getCdpTargetDeviceId() {
		return m_cdpTargetDeviceId;
	}

	public void setCdpTargetDeviceId(String cdpTargetDeviceId) {
		m_cdpTargetDeviceId = cdpTargetDeviceId;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
	    return new ToStringBuilder(this)
	                .append("ifindex",m_cdpIfIndex)
	                .append("targetNodeid",m_cdpTargetNodeId)
	                .append("cdptargetIfIndex:",m_cdpTargetIfIndex)
	                .append("cdptargetDeviceId:",m_cdpTargetDeviceId)
	                .toString();
	} 
}
