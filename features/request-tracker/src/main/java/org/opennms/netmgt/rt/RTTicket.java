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

package org.opennms.netmgt.rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTTicket implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(RTTicket.class);
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
    private List<CustomField> m_customFields;

    public RTTicket() {
        m_customFields = new ArrayList<CustomField>();
    }

    public RTTicket(final String queue, final String requestor, final String subject, final String text, final List<CustomField> customFields) {
        this(null, queue, requestor, subject, text, customFields);
    }

    public RTTicket(final Long id, final String queue, final String requestor, final String subject, final String text, final List<CustomField> customFields) {
        m_id = id;
        m_queue = queue;
        m_requestors.add(requestor);
        m_subject = subject;
        m_text = text;
        m_customFields = customFields;
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
            if (m_requestors == requestors) return;
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

    public List<CustomField> getCustomFields() {
        return m_customFields;
    }

    public void setCustomFields(List<CustomField> customFields) {
        m_customFields = customFields;
    }

    public void addCustomField(CustomField customField) {
        m_customFields.add(customField);
    }

    @Override
    public String toString() {
        StringBuilder customFields = new StringBuilder();
        for (CustomField cf : m_customFields) {
            customFields.append(cf.toString())
            .append("\n");
        }
        return new ToStringBuilder(this)
        .append("ID", m_id)
        .append("Queue", m_queue)
        .append("Created", m_created)
        .append("Requestors", StringUtils.join(m_requestors, ", "))
        .append("Status", m_status)
        .append("Subject", m_subject)
        .append("Text", m_text)
        .append("Custom Fields", customFields.toString())
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
            if (m_customFields.size() > 0) {
                for (CustomField field : m_customFields) {
                    if (field.getValues().size() == 0) continue;
                    if (field.getValues().size() > 1) LOG.warn("Field {} has {} values, using only the first one", field.getName(), field.getValues().size());
                    String value = field.getValues().get(0).getValue();
                    contentBuilder.append("CF.{").append(field.getName()).append("}: ").append(value).append("\n");
                }
            }
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
        newTicket.setCustomFields(m_customFields);
        return newTicket;
    }

}
