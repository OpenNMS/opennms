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

package org.opennms.web.element;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsStpNode.BridgeBaseType;
import org.opennms.netmgt.model.OnmsStpNode.StpProtocolSpecification;
import org.opennms.web.api.Util;


/**
 * <p>StpNode class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class StpNode
{
    int     m_nodeId;
    String     m_basenumports="";
	String  m_basetype;
	String  m_stpprotocolspecification="";
	String     m_stppriority="";
	String     m_stprootcost="";
	String     m_stprootport="";
	int     m_basevlan;
	String  m_basevlanname="default";
    String  m_basebridgeaddress;
	String  m_stpdesignatedroot="";
    String  m_lastPollTime;
    String  m_status;
    int 	m_stprootnodeid;

    /* package-protected so only the NetworkElementFactory can instantiate */
    StpNode()
    {
    }

    /* package-protected so only the NetworkElementFactory can instantiate */
    StpNode(OnmsStpNode node)
    {
        m_nodeId = node.getNode().getId();
        m_basebridgeaddress = node.getBaseBridgeAddress();
		m_basevlan = node.getBaseVlan();
        m_lastPollTime = Util.formatDateToUIString(node.getLastPollTime()); 
        m_status = StatusType.getStatusString(node.getStatus().getCharCode());
		
        if (node.getBaseNumPorts() != null)
			m_basenumports = node.getBaseNumPorts().toString();
		if (node.getBaseType() != null)
			m_basetype = BridgeBaseType.getBridgeBaseTypeString(node.getBaseType().getIntCode());
		if (node.getStpProtocolSpecification() != null)
			m_stpprotocolspecification = StpProtocolSpecification.getStpProtocolSpecificationString(node.getStpProtocolSpecification().getIntCode());
		if (node.getStpPriority() != null)
			m_stppriority = node.getStpPriority().toString();
		if (node.getStpRootCost() != null)
			m_stprootcost = node.getStpRootCost().toString();
		if (node.getStpRootPort() != null)
			m_stprootport = node.getStpRootPort().toString();
		if (node.getBaseVlanName() != null)
			m_basevlanname = node.getBaseVlanName();
        if (node.getStpDesignatedRoot() != null)
        	m_stpdesignatedroot = node.getStpDesignatedRoot();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString()
    {
            StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
            str.append("Bridge number of ports = " + m_basenumports + "\n" );
            str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
            str.append("Node At Status= " + m_status + "\n" );
            return str.toString();
    }

	/**
	 * <p>get_basebridgeaddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String get_basebridgeaddress() {
		return m_basebridgeaddress;
	}

	/**
	 * <p>get_basenumports</p>
	 *
	 * @return a int.
	 */
	public String get_basenumports() {
		return m_basenumports;
	}

	/**
	 * <p>get_basetype</p>
	 *
	 * @return a int.
	 */
	public String get_basetype() {
		return m_basetype;
	}

	/**
	 * <p>getBaseType</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBaseType() {
		return m_basetype;
	}
	/**
	 * <p>get_basevlan</p>
	 *
	 * @return a int.
	 */
	public int get_basevlan() {
		return m_basevlan;
	}

	/**
	 * <p>get_lastPollTime</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String get_lastPollTime() {
		return m_lastPollTime;
	}

	/**
	 * <p>get_nodeId</p>
	 *
	 * @return a int.
	 */
	public int get_nodeId() {
		return m_nodeId;
	}

	/**
	 * <p>get_status</p>
	 *
	 * @return a char.
	 */
	public String get_status() {
		return m_status;
	}

	/**
	 * <p>get_stpdesignatedroot</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String get_stpdesignatedroot() {
		return m_stpdesignatedroot;
	}

	/**
	 * <p>get_stppriority</p>
	 *
	 * @return a int.
	 */
	public String get_stppriority() {
		return m_stppriority;
	}

	/**
	 * <p>get_stpprotocolspecification</p>
	 *
	 * @return a int.
	 */
	public String get_stpprotocolspecification() {
		return m_stpprotocolspecification;
	}

	/**
	 * <p>getStpProtocolSpecification</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStpProtocolSpecification() {
		return m_stpprotocolspecification;
	}

	/**
	 * <p>get_stprootcost</p>
	 *
	 * @return a int.
	 */
	public String get_stprootcost() {
		return m_stprootcost;
	}

	/**
	 * <p>get_stprootport</p>
	 *
	 * @return a int.
	 */
	public String get_stprootport() {
		return m_stprootport;
	}

    /**
     * <p>get_stprootnodeid</p>
     *
     * @return Returns the m_stprootnodeid.
     */
    public int get_stprootnodeid() {
        return m_stprootnodeid;
    }

	/**
	 * <p>getBaseVlanName</p>
	 *
	 * @return Returns the m_basevlanname.
	 */
	public String getBaseVlanName() {
		return m_basevlanname;
	}
	
    /**
     * <p>getStatusString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusString() {
        return m_status;
    }

    /**
     * <p>getVlanColorIdentifier</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVlanColorIdentifier() {
        int red = 128;
        int green = 128;
        int blue = 128;
        int redoffset = 47;
        int greenoffset = 29;
        int blueoffset = 23;
        if (m_basevlan == 0) return "";
        if (m_basevlan == 1) return "#FFFFFF";
        red = (red + m_basevlan * redoffset)%255;
        green = (green + m_basevlan * greenoffset)%255;
        blue = (blue + m_basevlan * blueoffset)%255;
        if (red < 64) red = red+64;
        if (green < 64) green = green+64;
        if (blue < 64) blue = blue+64;
        return "#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
    }

}
