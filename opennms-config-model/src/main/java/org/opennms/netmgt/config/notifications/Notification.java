/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notifications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.core.xml.YesNoAdapter;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifications.xsd")
public class Notification implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "status", required = true)
    private String m_status;

    @XmlAttribute(name = "writeable")
    @XmlJavaTypeAdapter(YesNoAdapter.class)
    private Boolean m_writeable;

    @XmlElement(name = "uei", required = true)
    private String m_uei;

    @XmlElement(name = "description")
    private String m_description;

    @XmlElement(name = "rule", required = true)
    private String m_rule;

    @XmlElement(name = "notice-queue")
    private String m_noticeQueue;

    @XmlElement(name = "destinationPath", required = true)
    private String m_destinationPath;

    @XmlElement(name = "text-message", required = true)
    private String m_textMessage;

    @XmlElement(name = "subject")
    private String m_subject;

    @XmlElement(name = "numeric-message")
    private String m_numericMessage;

    @XmlElement(name = "event-severity")
    private String m_eventSeverity;

    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    @XmlElement(name = "varbind")
    private Varbind m_varbind;

    public Notification() { }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        m_status = ConfigUtils.assertNotEmpty(status, "status");
    }

    public Boolean getWriteable() {
        return m_writeable != null ? m_writeable : Boolean.TRUE;
    }

    public void setWriteable(final Boolean writeable) {
        m_writeable = writeable;
    }

    public String getUei() {
        return m_uei;
    }

    public void setUei(final String uei) {
        m_uei = ConfigUtils.assertNotEmpty(uei, "uei");
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(m_description);
    }

    public void setDescription(final String description) {
        m_description = ConfigUtils.normalizeString(description);
    }

    public String getRule() {
        return m_rule;
    }

    public void setRule(final String rule) {
        m_rule = ConfigUtils.assertNotEmpty(rule, "rule");
    }

    public Optional<String> getNoticeQueue() {
        return Optional.ofNullable(m_noticeQueue);
    }

    public void setNoticeQueue(final String noticeQueue) {
        m_noticeQueue = ConfigUtils.normalizeString(noticeQueue);
    }

    public String getDestinationPath() {
        return m_destinationPath;
    }

    public void setDestinationPath(final String destinationPath) {
        m_destinationPath = ConfigUtils.assertNotEmpty(destinationPath, "destinationPath");
    }

    public String getTextMessage() {
        return m_textMessage;
    }

    public void setTextMessage(final String textMessage) {
        m_textMessage = ConfigUtils.assertNotEmpty(textMessage, "text-message");
    }

    public Optional<String> getSubject() {
        return Optional.ofNullable(m_subject);
    }

    public void setSubject(final String subject) {
        m_subject = ConfigUtils.normalizeString(subject);
    }

    public Optional<String> getNumericMessage() {
        return Optional.ofNullable(m_numericMessage);
    }

    public void setNumericMessage(final String numericMessage) {
        m_numericMessage = ConfigUtils.normalizeString(numericMessage);
    }

    public Optional<String> getEventSeverity() {
        return Optional.ofNullable(m_eventSeverity);
    }

    public void setEventSeverity(final String eventSeverity) {
        m_eventSeverity = ConfigUtils.normalizeString(eventSeverity);
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (parameters == m_parameters) return;
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    public Varbind getVarbind() {
        return m_varbind;
    }

    public void setVarbind(final Varbind varbind) {
        m_varbind = varbind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_status, 
                            m_writeable, 
                            m_uei, 
                            m_description, 
                            m_rule, 
                            m_noticeQueue, 
                            m_destinationPath, 
                            m_textMessage, 
                            m_subject, 
                            m_numericMessage, 
                            m_eventSeverity, 
                            m_parameters, 
                            m_varbind);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Notification) {
            final Notification that = (Notification)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_status, that.m_status)
                    && Objects.equals(this.m_writeable, that.m_writeable)
                    && Objects.equals(this.m_uei, that.m_uei)
                    && Objects.equals(this.m_description, that.m_description)
                    && Objects.equals(this.m_rule, that.m_rule)
                    && Objects.equals(this.m_noticeQueue, that.m_noticeQueue)
                    && Objects.equals(this.m_destinationPath, that.m_destinationPath)
                    && Objects.equals(this.m_textMessage, that.m_textMessage)
                    && Objects.equals(this.m_subject, that.m_subject)
                    && Objects.equals(this.m_numericMessage, that.m_numericMessage)
                    && Objects.equals(this.m_eventSeverity, that.m_eventSeverity)
                    && Objects.equals(this.m_parameters, that.m_parameters)
                    && Objects.equals(this.m_varbind, that.m_varbind);
        }
        return false;
    }
}
