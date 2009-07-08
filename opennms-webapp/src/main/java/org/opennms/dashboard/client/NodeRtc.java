/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class NodeRtc implements IsSerializable {

    private String m_nodeLabel;
    private String m_nodeId;
    private String m_availability;
    private int m_downServiceCount;
    private int m_serviceCount;
    private String m_serviceStyle;
    private String m_availabilityStyle;
    private boolean m_isDashboardRole;

    public void setNodeLabel(String label) {
        m_nodeLabel = label;
    }

    public void setAvailability(String availability) {
        m_availability = availability;
    }

    public void setDownServiceCount(int downServiceCount) {
        m_downServiceCount = downServiceCount;
    }

    public void setServiceCount(int serviceCount) {
        m_serviceCount = serviceCount;
    }

    public String getAvailability() {
        return m_availability;
    }

    public int getDownServiceCount() {
        return m_downServiceCount;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public int getServiceCount() {
        return m_serviceCount;
    }

    public String getServiceStyle() {
        return m_serviceStyle;
    }

    public String getAvailabilityStyle() {
        return m_availabilityStyle;
    }

    public void setServiceStyle(String serviceStyle) {
        m_serviceStyle = serviceStyle;
    }

    public void setAvailabilityStyle(String availabilityStyle) {
        m_availabilityStyle = availabilityStyle;
    }
    
    public void setIsDashboardRole(boolean isDashboardRole) {
        m_isDashboardRole = isDashboardRole;
    }

    public boolean getIsDashboardRole() {
        return m_isDashboardRole;
    }
    
    public void setNodeId(String nodeId) {
        m_nodeId = nodeId;
    }
    
    public String getNodeId() {
        return m_nodeId;
    }
}
