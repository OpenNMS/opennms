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

package org.opennms.dashboard.client;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>Notification class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.8.1
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
    
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    /**
     * <p>setNodeLabel</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     */
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    /**
     * <p>getResponder</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResponder() {
        return m_responder;
    }
    /**
     * <p>setResponder</p>
     *
     * @param responder a {@link java.lang.String} object.
     */
    public void setResponder(String responder) {
        m_responder = responder;
    }
    /**
     * <p>getRespondTime</p>
     *
     * @return a java$util$Date object.
     */
    public Date getRespondTime() {
        return m_respondTime;
    }
    /**
     * <p>setRespondTime</p>
     *
     * @param respondTime a java$util$Date object.
     */
    public void setRespondTime(Date respondTime) {
        m_respondTime = respondTime;
    }
    /**
     * <p>getSentTime</p>
     *
     * @return a java$util$Date object.
     */
    public Date getSentTime() {
        return m_sentTime;
    }
    /**
     * <p>setSentTime</p>
     *
     * @param sentTime a java$util$Date object.
     */
    public void setSentTime(Date sentTime) {
        m_sentTime = sentTime;
    }
    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }
    /**
     * <p>setServiceName</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }
    /**
     * <p>getSeverity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSeverity() {
        return m_severity;
    }
    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link java.lang.String} object.
     */
    public void setSeverity(String severity) {
        m_severity = severity;
    }
	/**
	 * <p>getTextMessage</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTextMessage() {
		return m_textMessage;
	}
	/**
	 * <p>setTextMessage</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public void setTextMessage(String message) {
		m_textMessage = message;
	}
    
	/**
	 * <p>setIsDashboardRole</p>
	 *
	 * @param isDashboardRole a boolean.
	 */
	public void setIsDashboardRole(boolean isDashboardRole) {
        m_isDashboardRole = isDashboardRole;
    }

    /**
     * <p>getIsDashboardRole</p>
     *
     * @return a boolean.
     */
    public boolean getIsDashboardRole() {
        return m_isDashboardRole;
    }
    
    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeId() {
        return m_nodeId;
    }
    /**
     * <p>setNodeId</p>
     *
     * @param nodeId a {@link java.lang.String} object.
     */
    public void setNodeId(String nodeId) {
        m_nodeId = nodeId;
    }

}
