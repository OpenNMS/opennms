/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.model;

import java.util.Arrays;

import com.google.common.base.Strings;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class NodeListCommand {
    private String m_nodename = null;
    private String m_iplike = null;
    private String m_maclike = null;
    private String m_foreignsource = null;
    private Integer m_service = null;
    private String m_mib2Parm = null;
    private String m_mib2ParmValue = null;
    private String m_mib2ParmMatchType = null;
    private String m_snmpParm = null;
    private String m_snmpParmValue = null;
    private String m_snmpParmMatchType = null;
    private String[] m_category1 = null;
    private String[] m_category2 = null;
    private String m_statusViewName = null;
    private String m_statusSite = null;
    private String m_statusRowLabel = null;
    private boolean m_nodesWithOutages = false;
    private boolean m_nodesWithDownAggregateStatus = false;
    private boolean m_listInterfaces = false;
    private String m_nodeId;
    private String m_monitoringLocation;
    
    public void setNodeId(String nodeId) {
        m_nodeId = nodeId;
    }
    public String getNodeId() {
        return m_nodeId;
    }
    public boolean hasNodeId() {
        return !Strings.isNullOrEmpty(m_nodeId);
    }
    public void setNodename(String nodename) {
        m_nodename = nodename;
    }
    public String getNodename() {
        return m_nodename;
    }
    public boolean hasNodename() {
        return m_nodename != null;
    }
    
    public void setIplike(String iplike) {
        m_iplike = iplike;
    }
    public String getIplike() {
        return m_iplike;
    }
    public boolean hasIplike() {
        return m_iplike != null;
    }
    
    public void setMaclike(String maclike) {
        m_maclike = maclike;
    }
    public String getMaclike() {
        return m_maclike;
    }
    public boolean hasMaclike() {
        return m_maclike != null;
    }
    
    public void setForeignSource(String foreignSourceLike) {
        m_foreignsource = foreignSourceLike;
    }
    
    public String getForeignSource() {
        return m_foreignsource;
    }
    
    public boolean hasForeignSource() {
        return m_foreignsource != null;
    }
    
    public void setService(Integer service) {
        m_service = service;
    }
    public Integer getService() {
        return m_service;
    }
    public boolean hasService() {
        return m_service != null;
    }
    
    public void setMib2Parm(String mib2Parm) {
        m_mib2Parm = mib2Parm;
    }
    public String getMib2Parm() {
        return m_mib2Parm;
    }
    public boolean hasMib2Parm() {
        return m_mib2Parm != null;
    }
    public void setMib2ParmValue(String mib2ParmValue) {
        m_mib2ParmValue = mib2ParmValue;
    }
    public String getMib2ParmValue() {
        return m_mib2ParmValue;
    }
    public boolean hasMib2ParmValue() {
        return m_mib2ParmValue != null;
    }
    public void setMib2ParmMatchType(String mib2ParmMatchType) {
        m_mib2ParmMatchType = mib2ParmMatchType;
    }
    public String getMib2ParmMatchType() {
        return m_mib2ParmMatchType;
    }
    public boolean hasMib2ParmMatchType() {
        return m_mib2ParmMatchType != null;
    }
    
    public void setSnmpParm(String snmpParm) {
        m_snmpParm = snmpParm;
    }
    public String getSnmpParm() {
        return m_snmpParm;
    }
    public boolean hasSnmpParm() {
        return m_snmpParm != null;
    }
    public void setSnmpParmValue(String snmpParmValue) {
        m_snmpParmValue = snmpParmValue;
    }
    public String getSnmpParmValue() {
        return m_snmpParmValue;
    }
    public boolean hasSnmpParmValue() {
        return m_snmpParmValue != null;
    }
    public void setSnmpParmMatchType(String snmpParmMatchType) {
        m_snmpParmMatchType = snmpParmMatchType;
    }
    public String getSnmpParmMatchType() {
        return m_snmpParmMatchType;
    }
    public boolean hasSnmpParmMatchType() {
        return m_snmpParmMatchType != null;
    }
        
    public void setCategory1(String[] category1) {
        m_category1 = Arrays.copyOf(category1, category1.length);
    }
    public String[] getCategory1() {
        return m_category1;
    }
    public boolean hasCategory1() {
        return m_category1 != null && m_category1.length > 0;
    }
    
    public void setCategory2(String[] category2) {
        m_category2 = Arrays.copyOf(category2, category2.length);
    }
    public String[] getCategory2() {
        return m_category2;
    }
    public boolean hasCategory2() {
        return m_category2 != null && m_category2.length > 0;
    }
    
    public void setStatusViewName(String statusViewName) {
        m_statusViewName = statusViewName;
    }
    public String getStatusViewName() {
        return m_statusViewName;
    }
    public boolean hasStatusViewName() {
        return m_statusViewName != null;
    }
    
    public void setStatusSite(String statusSite) {
        m_statusSite = statusSite;
    }
    public String getStatusSite() {
        return m_statusSite;
    }
    public boolean hasStatusSite() {
        return m_statusSite != null;
    }

    public void setStatusRowLabel(String statusRowLabel) {
        m_statusRowLabel = statusRowLabel;
    }
    public String getStatusRowLabel() {
        return m_statusRowLabel;
    }
    public boolean hasStatusRowLabel() {
        return m_statusRowLabel != null;
    }
    
    public void setNodesWithOutages(boolean nodesWithOutages) {
        m_nodesWithOutages = nodesWithOutages;
    }
    public boolean getNodesWithOutages() {
        return m_nodesWithOutages;
    }
    
    public void setNodesWithDownAggregateStatus(boolean nodesWithDownAggregateStatus) {
        m_nodesWithDownAggregateStatus = nodesWithDownAggregateStatus;
    }
    public boolean getNodesWithDownAggregateStatus() {
        return m_nodesWithDownAggregateStatus;
    }
    
    public void setListInterfaces(boolean listInterfaces) {
        m_listInterfaces = listInterfaces;
    }
    public boolean getListInterfaces() {
        return m_listInterfaces;
    }

    public void setMonitoringLocation(String monitoringLocation) {
        m_monitoringLocation = monitoringLocation;
    }

    public String getMonitoringLocation() {
        return m_monitoringLocation;
    }

    public boolean hasMonitoringLocation() {
        return !Strings.isNullOrEmpty(m_monitoringLocation);
    }
}