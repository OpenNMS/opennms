/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.support;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.rt.RTTicket;

public class SupportResults implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 2119247915337079075L;
    private boolean m_success = true;
    private boolean m_needsLogin = false;
    private String m_baseUrl;
    private String m_username;
    private String m_queue;
    private List<RTTicket> m_latestTickets;
    private String m_message;

    public SupportResults() {
    }

    public boolean getSuccess() {
        return m_success;
    }
    
    public void setSuccess(final boolean success) {
        m_success = success;
    }

    public boolean getNeedsLogin() {
        return m_needsLogin;
    }
    
    public void setNeedsLogin(final boolean needsLogin) {
        m_needsLogin = needsLogin;
    }
    
    public String getRTUrl() {
        return m_baseUrl;
    }

    public void setRTUrl(final String baseUrl) {
        m_baseUrl = baseUrl;
    }

    public String getUsername() {
        return m_username;
    }
    
    public void setUsername(final String username) {
        m_username = username;
    }
    
    public String getQueue() {
        return m_queue;
    }
    
    public void setQueue(final String queue) {
        m_queue = queue;
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(final String message) {
        m_message = message;
    }

    public List<RTTicket> getLatestTickets() {
        return m_latestTickets;
    }

    public void setLatestTickets(final List<RTTicket> tickets) {
        m_latestTickets = tickets;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("success", m_success)
            .append("needsLogin", m_needsLogin)
            .append("baseUrl", m_baseUrl)
            .append("username", m_username)
            .append("queue", m_queue)
            .append("message", m_message)
            .append("latestTickets", m_latestTickets)
            .toString();
    }

}
