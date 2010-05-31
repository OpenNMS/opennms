/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 9, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.web.command;

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
    private int m_nodeId = -1;
    
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }
    public int getNodeId() {
        return m_nodeId;
    }
    public boolean hasNodeId() {
        return m_nodeId >= 0;
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
        m_category1 = category1;
    }
    public String[] getCategory1() {
        return m_category1;
    }
    public boolean hasCategory1() {
        return m_category1 != null && m_category1.length > 0;
    }
    
    public void setCategory2(String[] category2) {
        m_category2 = category2;
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
    
}