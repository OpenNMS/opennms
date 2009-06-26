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
 * Created: March 2, 2007
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
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class Notification implements IsSerializable {
	
	private String m_nodeLabel;
	private String m_nodeId;
	private String m_serviceName;
	private String m_severity;
	private Date m_sentTime;
	private String m_responder;
	private Date m_respondTime;
	private String m_textMessage;
	private boolean m_isDashboardRole;
    
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    public String getResponder() {
        return m_responder;
    }
    public void setResponder(String responder) {
        m_responder = responder;
    }
    public Date getRespondTime() {
        return m_respondTime;
    }
    public void setRespondTime(Date respondTime) {
        m_respondTime = respondTime;
    }
    public Date getSentTime() {
        return m_sentTime;
    }
    public void setSentTime(Date sentTime) {
        m_sentTime = sentTime;
    }
    public String getServiceName() {
        return m_serviceName;
    }
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
	public String getTextMessage() {
		return m_textMessage;
	}
	public void setTextMessage(String message) {
		m_textMessage = message;
	}
    
	public void setIsDashboardRole(boolean isDashboardRole) {
        m_isDashboardRole = isDashboardRole;
    }

    public boolean getIsDashboardRole() {
        return m_isDashboardRole;
    }
    
    public String getNodeId() {
        return m_nodeId;
    }
    public void setNodeId(String nodeId) {
        m_nodeId = nodeId;
    }

}
