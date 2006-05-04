//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.collectd;

import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

/**
 * This class encapsulates all the information required by the SNMP collector in
 * order to perform data collection for an individual interface and store that
 * data in an appropriately named RRD file.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class IfInfo {
	
	OnmsSnmpInterface m_snmpIface;
	
	private CollectionInterface m_collectionInterface;

	private String m_collectionName;
	
	private List m_oidList = null;

    public IfInfo(CollectionInterface collectionInterface, String collectionName, OnmsSnmpInterface snmpIface) {
    	m_collectionInterface = collectionInterface;
    	m_collectionName = collectionName;
    	m_snmpIface = snmpIface;
    	
    }

	public int getIndex() {
        return m_snmpIface.getIfIndex().intValue();
    }

    public int getType() {
        return m_snmpIface.getIfType().intValue();
    }

    public String getLabel() {
        return m_snmpIface.computeLabelForRRD();
    }

    public CollectionType getCollType() {
        return m_snmpIface.getCollectionType();
    }

    public List getDsList() {
        List dsList = DataCollectionConfigFactory.buildDataSourceList(getCollectionName(), getOidList());
		return dsList;
    }

    public List getOidList() {
        return (m_oidList == null ? computeOidList() : m_oidList);
    }

	private List computeOidList() {
		/*
		 * Retrieve list of mib objects to be collected from the
		 * remote agent for this interface.
		 */
		List oidList = DataCollectionConfigFactory.getInstance()
		.getMibObjectList(getCollectionName(), getCollectionInterface().getSysObjectId(),
				getCollectionInterface().getHostAddress(), getIndex());
		return oidList;
	}
    
	public List getAttributeList() {
	    /*
	     * Retrieve list of mib objects to be collected from the
	     * remote agent for this interface.
	     */
	    return DataCollectionConfigFactory.getInstance()
	    .buildCollectionAttributes(getCollectionName(), getCollectionInterface().getSysObjectId(),
	            getCollectionInterface().getHostAddress(), getType());
	}

	public CollectionInterface getCollectionInterface() {
		return m_collectionInterface;
	}

	public String getCollectionName() {
		return m_collectionName;
	}

	// FIXME: Figure out how to delete this since it is used by the tests
	public void setOidList(List list) {
		m_oidList = list;
	}

} // end class
