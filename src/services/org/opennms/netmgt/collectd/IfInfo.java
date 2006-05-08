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

import java.util.ArrayList;
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
final class IfInfo extends CollectionResource {
	
	OnmsSnmpInterface m_snmpIface;
	
	private CollectionAgent m_agent;

	private String m_collectionName;

    private List m_attributeList;
	
    public IfInfo(CollectionAgent agent, String collectionName, OnmsSnmpInterface snmpIface) {
    	m_agent = agent;
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

	public List getAttributeList() {
	    /*
	     * Retrieve list of mib objects to be collected from the
	     * remote agent for this interface.
	     */
        if (m_attributeList == null) {
            m_attributeList = computeAttributeList();
        }
	    return m_attributeList;
	}

    private List computeAttributeList() {
        return DataCollectionConfigFactory.getInstance()
	    .buildCollectionAttributes(getCollectionName(), getCollectionAgent().getSysObjectId(),
	            getCollectionAgent().getHostAddress(), getType());
    }

	public CollectionAgent getCollectionAgent() {
		return m_agent;
	}

	public String getCollectionName() {
		return m_collectionName;
	}

	public void setAttributeList(ArrayList list) {
        m_attributeList = list;
    }

} // end class
