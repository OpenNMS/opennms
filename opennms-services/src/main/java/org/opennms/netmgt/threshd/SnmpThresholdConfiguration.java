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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.utils.ParameterMap;

public class SnmpThresholdConfiguration {
    
    /**
     * Default thresholding interval (in milliseconds).
     * 
     */
    private static final int DEFAULT_INTERVAL = 300000; // 300s or 5m
    
    /**
     * Default age before which a data point is considered "out of date"
     */
    
    private static final int DEFAULT_RANGE = 0; 

    private static final String THRESHD_SERVICE_CONFIG_KEY = SnmpThresholdConfiguration.class.getName();


    public static SnmpThresholdConfiguration get(NetworkInterface iface, Map parms) {
        SnmpThresholdConfiguration config = (SnmpThresholdConfiguration)iface.getAttribute(THRESHD_SERVICE_CONFIG_KEY);
        if (config == null) {
            config = new SnmpThresholdConfiguration(parms);
            iface.setAttribute(THRESHD_SERVICE_CONFIG_KEY, config);
        }
        return config;
    }

    private Map m_parms;
    
    private ThresholdGroup m_thresholdGroup;

    private SnmpThresholdConfiguration(Map parms) {
        m_parms = parms;
        DefaultThresholdsDao dao = new DefaultThresholdsDao();
        m_thresholdGroup = dao.get(ParameterMap.getKeyedString(m_parms, "thresholding-group", "default"));
    }
    
    File getRrdRepository() {
        return m_thresholdGroup.getRrdRepository();
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public String getGroupName() {
        return m_thresholdGroup.getName();
    }

    public int getRange() {
        return ParameterMap.getKeyedInteger(m_parms, "range", SnmpThresholdConfiguration.DEFAULT_RANGE);
    }

    public int getInterval() {
        return ParameterMap.getKeyedInteger(m_parms, "interval", SnmpThresholdConfiguration.DEFAULT_INTERVAL);
    }

	public ThresholdResourceType getIfResourceType() {
		return m_thresholdGroup.getIfResourceType();
	}

	public ThresholdResourceType getNodeResourceType() {
		return m_thresholdGroup.getNodeResourceType();
	}

	public void setIfResourceType(ThresholdResourceType ifResourceType) {
		m_thresholdGroup.setIfResourceType(ifResourceType);
	}

	public void setNodeResourceType(ThresholdResourceType nodeResourceType) {
		m_thresholdGroup.setNodeResourceType(nodeResourceType);
	}


}
