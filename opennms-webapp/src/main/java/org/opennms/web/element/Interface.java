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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.sql.Date;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.api.Util;

/**
 * <p>Interface class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Interface {
    int m_id;
    
    int m_nodeId;

    int m_ifIndex;

    int m_ipStatus;

    String m_ipHostName;

    String m_ipAddr;

    char m_isManaged;

    String m_ipLastCapsdPoll;

    String m_snmpIpAdEntNetMask;

    String m_snmpPhysAddr;

    String m_snmpIfDescr;

    String m_snmpIfName;

    int m_snmpIfIndex;

    int m_snmpIfType;

    int m_snmpIfOperStatus;

    long m_snmpIfSpeed;

    int m_snmpIfAdminStatus;
    
    String m_snmpIfAlias;

    String m_isSnmpPrimary;
    
    char m_isSnmpPoll;
    
    String m_snmpLastSnmpPoll;

    String m_snmpLastCapsdPoll;
    	
    List<String> ipaddresses=null;

    Interface () {
        
    }

    Interface (OnmsSnmpInterface snmpIface) {
        m_id = snmpIface.getId();
        createSnmpInterface(snmpIface);
    }

    public void createSnmpInterface(OnmsSnmpInterface snmpIface) {
        if(snmpIface.getNode() != null) {
            m_nodeId = snmpIface.getNode().getId();
        }
        
        m_snmpIfIndex = snmpIface.getIfIndex();
        m_snmpIpAdEntNetMask = str(snmpIface.getNetMask());
        m_snmpPhysAddr = snmpIface.getPhysAddr();
        m_snmpIfDescr = snmpIface.getIfDescr();
        m_snmpIfName = snmpIface.getIfName();
        
        if(snmpIface.getIfType() != null) {
            m_snmpIfType = snmpIface.getIfType();
        }
        if(snmpIface.getIfOperStatus() != null) {
            m_snmpIfOperStatus = snmpIface.getIfOperStatus();
        }
        if(snmpIface.getIfSpeed() != null) {
            m_snmpIfSpeed = snmpIface.getIfSpeed();
        }
        if(snmpIface.getIfAdminStatus() != null) {
            m_snmpIfAdminStatus = snmpIface.getIfAdminStatus();
        }
            m_snmpIfAlias = snmpIface.getIfAlias();
        
        Object element = snmpIface.getPoll();
        if (element != null) {
            m_isSnmpPoll = ((String) element).charAt(0);
        }

        if (snmpIface.getLastCapsdPoll() != null) {
            m_snmpLastCapsdPoll = Util.formatDateToUIString(snmpIface.getLastCapsdPoll());
        }

        if (snmpIface.getLastSnmpPoll() != null) {
            m_snmpLastSnmpPoll = Util.formatDateToUIString(snmpIface.getLastSnmpPoll());
        }
    }
    
    Interface (OnmsIpInterface ipIface) {        
        m_id = ipIface.getId();
        createIpInterface(ipIface);
    }

    public void createIpInterface(OnmsIpInterface ipIface) {
        if(ipIface.getNode() != null) {
            m_nodeId = ipIface.getNode().getId();
        }
        
        if(ipIface.getSnmpInterface() != null) {
            m_ifIndex = ipIface.getIfIndex();
        }
        m_ipHostName = ipIface.getIpHostName();
        m_ipAddr = InetAddressUtils.str(ipIface.getIpAddress());
        m_isSnmpPrimary = ipIface.getIsSnmpPrimary().getCode();
        m_isManaged = ipIface.getIsManaged().charAt(0);
        if(ipIface.getIpLastCapsdPoll() != null) {
            m_ipLastCapsdPoll = Util.formatDateToUIString(ipIface.getIpLastCapsdPoll());
        }
    }
	public List<String> getIpaddresses() {
		return ipaddresses;
	}

	public void setIpaddresses(List<String> ipaddresses) {
		this.ipaddresses = ipaddresses;
	}
		
	public boolean hasIpAddresses() {
		return !(ipaddresses == null);
	}
    	    

    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return m_id;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>getIpStatus</p>
     *
     * @return a int.
     */
    public int getIpStatus() {
        return m_ipStatus;
    }

    /**
     * <p>getHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return m_ipHostName;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddr;
    }

    /**
     * <p>isManagedChar</p>
     *
     * @return a char.
     */
    public char isManagedChar() {
        return m_isManaged;
    }

    /**
     * <p>getLastCapsdPoll</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLastCapsdPoll() {
        return m_ipLastCapsdPoll;
    }

    /**
     * <p>getSnmpIfIndex</p>
     *
     * @return a int.
     */
    public int getSnmpIfIndex() {
        return m_snmpIfIndex;
    }

    /**
     * <p>getSnmpIpAdEntNetMask</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpIpAdEntNetMask() {
        return m_snmpIpAdEntNetMask;
    }

    /**
     * <p>getPhysicalAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPhysicalAddress() {
        return m_snmpPhysAddr;
    }

    /**
     * <p>getSnmpIfDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpIfDescription() {
        return m_snmpIfDescr;
    }

    /**
     * <p>getSnmpIfName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpIfName() {
        return m_snmpIfName;
    }

    /**
     * <p>getSnmpIfType</p>
     *
     * @return a int.
     */
    public int getSnmpIfType() {
        return m_snmpIfType;
    }

    /**
     * <p>getSnmpIfOperStatus</p>
     *
     * @return a int.
     */
    public int getSnmpIfOperStatus() {
        return m_snmpIfOperStatus;
    }

    /**
     * <p>getSnmpIfSpeed</p>
     *
     * @return a long.
     */
    public long getSnmpIfSpeed() {
        return m_snmpIfSpeed;
    }

    /**
     * <p>getSnmpIfAdminStatus</p>
     *
     * @return a int.
     */
    public int getSnmpIfAdminStatus() {
        return m_snmpIfAdminStatus;
    }

    /**
     * <p>isSnmpPollChar</p>
     *
     * @return a char.
     */
    public char isSnmpPollChar() {
        return m_isSnmpPoll;
    }
    
    /**
     * <p>getSnmpLastSnmpPoll</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpLastSnmpPoll() {
        return m_snmpLastSnmpPoll;
    }

    /**
     * <p>getSnmpLastCapsdPoll</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpLastCapsdPoll() {
        return m_snmpLastCapsdPoll;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return m_ipHostName;
    }

    /**
     * <p>isManaged</p>
     *
     * @return a boolean.
     */
    public boolean isManaged() {
        return (m_isManaged == 'M');
    }

    /**
     * <p>isSnmpPoll</p>
     *
     * @return a boolean.
     */
    public boolean isSnmpPoll() {
        return (m_isSnmpPoll == 'P');
    }

    /**
     * <p>getSnmpIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpIfAlias() {
        return m_snmpIfAlias;
    }

    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIsSnmpPrimary() {
        return m_isSnmpPrimary;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_ipHostName == null? m_ipAddr : m_ipHostName;
    }
}
