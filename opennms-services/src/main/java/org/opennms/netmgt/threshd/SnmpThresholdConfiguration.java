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
// Modifications:
//
// 2007 Jan 29: Indenting, do not store instance of this object as attributes in NetworkInterface. - dj@opennms.org
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

import org.opennms.core.utils.ParameterMap;

public class SnmpThresholdConfiguration {

    /**
     * Default thresholding group.
     */
    private static final String DEFAULT_GROUP = "default";

    /**
     * Default thresholding interval (in milliseconds).
     * 
     */
    private static final int DEFAULT_INTERVAL = 300000; // 300s or 5m

    /**
     * Default age before which a data point is considered "out of date"
     */
    private static final int DEFAULT_RANGE = 0; 

    private ThresholdGroup m_thresholdGroup;

    private int m_range;

    private int m_interval;

    public SnmpThresholdConfiguration(ThresholdsDao thresholdsDao, Map parms) {
        setRange(ParameterMap.getKeyedInteger(parms, "range", SnmpThresholdConfiguration.DEFAULT_RANGE));
        setInterval(ParameterMap.getKeyedInteger(parms, "interval", SnmpThresholdConfiguration.DEFAULT_INTERVAL));
        setThresholdGroup(thresholdsDao.get(ParameterMap.getKeyedString(parms, "thresholding-group", DEFAULT_GROUP)));
    }

    public int getRange() {
        return m_range;
    }

    public void setRange(int range) {
        m_range = range;
    }

    public int getInterval() {
        return m_interval;
    }

    public void setInterval(int interval) {
        m_interval = interval;
    }

    public ThresholdGroup getThresholdGroup() {
        return m_thresholdGroup;
    }

    public void setThresholdGroup(ThresholdGroup thresholdGroup) {
        m_thresholdGroup = thresholdGroup;
    }

    public File getRrdRepository() {
        return m_thresholdGroup.getRrdRepository();
    }

    public String getGroupName() {
        return m_thresholdGroup.getName();
    }

    public ThresholdResourceType getIfResourceType() {
        return m_thresholdGroup.getIfResourceType();
    }

    public void setIfResourceType(ThresholdResourceType ifResourceType) {
        m_thresholdGroup.setIfResourceType(ifResourceType);
    }

    public ThresholdResourceType getNodeResourceType() {
        return m_thresholdGroup.getNodeResourceType();
    }

    public void setNodeResourceType(ThresholdResourceType nodeResourceType) {
        m_thresholdGroup.setNodeResourceType(nodeResourceType);
    }

    public Map<String,ThresholdResourceType> getGenericResourceTypeMap() {
        return m_thresholdGroup.getGenericResourceTypeMap();
    }

    public void setGenericResourceTypeMap(Map<String,ThresholdResourceType> genericResourceTypeMap) {
        m_thresholdGroup.setGenericResourceTypeMap(genericResourceTypeMap);
    }


}
