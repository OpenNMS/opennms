/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class RTTicket implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 445141747501076112L;
	private static Pattern m_trim = Pattern.compile("\\s*\\n*$");
    private Long m_id;
    private String m_status;
    private String m_queue;
    private String m_created;
    private List<String> m_requestors = new ArrayList<String>();
    private String m_subject;
    private String m_text;

    public RTTicket() {
    }

    public RTTicket(final String queue, final String requestor, final String subject, final String text) {
        this(null, queue, requestor, subject, text);
    }

    public RTTicket(final Long id, final String queue, final String requestor, final String subject, final String text) {
        m_id = id;
        m_queue = queue;
        m_requestors.add(requestor);
        m_subject = subject;
        m_text = text;
    }

    public Long getId() {
        return m_id;
    }

    public void setId(final Long id) {
        m_id = id;
    }

    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        m_status = status;
    }

    public String getQueue() {
        return m_queue;
    }

    public void setQueue(final String queue) {
        m_queue = queue;
    }

    public String getCreated() {
        return m_created;
    }
    
    public void setCreated(final String created) {
        m_created = created;
    }

    public String getRequestor() {
        synchronized (m_requestors) {
            if (m_requestors.size() > 0) {
                return m_requestors.get(0);
            }
        }
        return null;
    }

    public List<String> getRequestors() {
        return m_requestors;
    }

    public void setRequestors(final List<String> requestors) {
        synchronized (m_requestors) {
            m_requestors.clear();
            m_requestors.addAll(requestors);
        }
    }

    public void setRequestor(final String requestor) {
        synchronized (m_requestors) {
            m_requestors.clear();
            m_requestors.add(requestor);
        }
    }

    public void addRequestor(final String requestor) {
        m_requestors.add(requestor);
    }

    public String getSubject() {
        return m_subject;
    }

    public void setSubject(final String subject) {
        m_subject = subject;
    }

    public String getText() {
        return m_text;
    }

    public void setText(final String text) {
        m_text = text;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("ID", m_id)
            .append("Queue", m_queue)
            .append("Created", m_created)
            .append("Requestors", StringUtils.join(m_requestors, ", "))
            .append("Status", m_status)
            .append("Subject", m_subject)
            .append("Text", m_text)
            .toString();
    }

    public String toContent() {
        final StringBuilder contentBuilder = new StringBuilder();
        if (m_id == null) {
            contentBuilder.append("id: ticket/new\n");
            if (m_queue      != null) contentBuilder.append("Queue: ").append(m_queue).append("\n");
            if (m_requestors != null) contentBuilder.append("Requestor: ").append(getRequestor()).append("\n");
            if (m_subject    != null) contentBuilder.append("Subject: ").append(m_subject.replaceAll("[\\r\\n]+", " ")).append("\n");
            if (m_text       != null) contentBuilder.append("text: ").append(m_text.replaceAll("\\r?\\n", "\n ")).append("\n");
        } else {
            // contentBuilder.append("id: ticket/").append(m_id).append("\n");
        }
        if (m_status != null) contentBuilder.append("Status: ").append(m_status).append("\n");

        return m_trim.matcher(contentBuilder.toString()).replaceAll("");
    }

    public RTTicket copy() {
        final RTTicket newTicket = new RTTicket();
        newTicket.setId(m_id);
        newTicket.setQueue(m_queue);
        newTicket.setRequestors(m_requestors);
        newTicket.setStatus(m_status);
        newTicket.setSubject(m_subject);
        newTicket.setText(m_text);
        return newTicket;
    }

}
