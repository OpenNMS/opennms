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
 * 2009 Feb 09: Add node links for users NOT in dashboard role. ayres@opennms.org
 * Created: February 22, 2007
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

package org.opennms.dashboard.client;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class Alarm implements IsSerializable {
    
    private String m_logMsg;
    private String m_descrption;
    private String m_severity;
    private int m_count;
    private String m_nodeLabel;
    private int m_nodeId;
    private String m_ipAddress;
    private String m_svcName;
    private Date m_firstEventTime;
    private Date m_lastEventTime;
    private boolean m_isDashboardRole;
    
    public Alarm() {
        
    }
    
    public Alarm(String severity, String nodeLabel, int nodeId, boolean isDashboardRole, String logMsg, String description, int count, Date firstEventTime, Date lastEventTime) {
        m_severity = severity;
        m_nodeLabel = nodeLabel;
        m_nodeId = nodeId;
        m_isDashboardRole = isDashboardRole;
        m_logMsg = logMsg;
        m_descrption = description;
        m_count = count;
        m_firstEventTime = firstEventTime;
        m_lastEventTime = lastEventTime;
    }
    public int getCount() {
        return m_count;
    }
    public void setCount(int count) {
        m_count = count;
    }
    public String getDescrption() {
        return m_descrption;
    }
    public void setDescrption(String descrption) {
        m_descrption = descrption;
    }
    public String getIpAddress() {
        return m_ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }
    public int getNodeId() {
        return m_nodeId;
    }
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    public String getSvcName() {
        return m_svcName;
    }
    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    public Date getFirstEventTime() {
        return m_firstEventTime;
    }

    public void setFirstEventTime(Date firstEventTime) {
        m_firstEventTime = firstEventTime;
    }

    public Date getLastEventTime() {
        return m_lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime) {
        m_lastEventTime = lastEventTime;
    }

    public String getLogMsg() {
        return m_logMsg;
    }

    public void setLogMsg(String logMsg) {
        m_logMsg = logMsg;
    }
    
    public void setIsDashboardRole(boolean isDashboardRole) {
        m_isDashboardRole = isDashboardRole;
    }

    public boolean getIsDashboardRole() {
        return m_isDashboardRole;
    }

}
