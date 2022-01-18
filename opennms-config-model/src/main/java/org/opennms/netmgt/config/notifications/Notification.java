/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.opennms.features.config.util.json.YesNoDeserializer;
import org.opennms.netmgt.config.utils.ConfigUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Notification implements Serializable {
    private static final long serialVersionUID = 2L;

    private String name;

    private String status;

    @JsonDeserialize(using = YesNoDeserializer.class)
    private Boolean writeable;

    private String uei;

    private String description;

    private Rule rule;

    private String noticeQueue;

    @JsonProperty("destinationPath")
    private String destinationPath;

    private String textMessage;

    private String subject;

    private String numericMessage;

    private String eventSeverity;

    @JsonProperty("parameter")
    private List<Parameter> parameters = new ArrayList<>();

    private Varbind varbind;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = ConfigUtils.assertNotEmpty(status, "status");
    }

    public Boolean getWriteable() {
        return this.writeable != null ? this.writeable : Boolean.TRUE;
    }

    public void setWriteable(final Boolean writeable) {
        this.writeable = writeable;
    }

    public String getUei() {
        return this.uei;
    }

    public void setUei(final String uei) {
        this.uei = ConfigUtils.assertNotEmpty(uei, "uei");
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    public void setDescription(final String description) {
        this.description = ConfigUtils.normalizeString(description);
    }

    public Rule getRule() {
        return this.rule;
    }

    public void setRule(final Rule rule) {
        this.rule = ConfigUtils.assertNotEmpty(rule, "rule");
    }

    public Optional<String> getNoticeQueue() {
        return Optional.ofNullable(this.noticeQueue);
    }

    public void setNoticeQueue(final String noticeQueue) {
        this.noticeQueue = ConfigUtils.normalizeString(noticeQueue);
    }

    public String getDestinationPath() {
        return this.destinationPath;
    }

    public void setDestinationPath(final String destinationPath) {
        this.destinationPath = ConfigUtils.assertNotEmpty(destinationPath, "destinationPath");
    }

    public String getTextMessage() {
        return this.textMessage;
    }

    public void setTextMessage(final String textMessage) {
        this.textMessage = ConfigUtils.assertNotEmpty(textMessage, "text-message");
    }

    public Optional<String> getSubject() {
        return Optional.ofNullable(this.subject);
    }

    public void setSubject(final String subject) {
        this.subject = ConfigUtils.normalizeString(subject);
    }

    public Optional<String> getNumericMessage() {
        return Optional.ofNullable(this.numericMessage);
    }

    public void setNumericMessage(final String numericMessage) {
        this.numericMessage = ConfigUtils.normalizeString(numericMessage);
    }

    public Optional<String> getEventSeverity() {
        return Optional.ofNullable(this.eventSeverity);
    }

    public void setEventSeverity(final String eventSeverity) {
        this.eventSeverity = ConfigUtils.normalizeString(eventSeverity);
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (parameters == this.parameters) return;
        this.parameters.clear();
        if (parameters != null) this.parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        this.parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return this.parameters.remove(parameter);
    }

    public Varbind getVarbind() {
        return this.varbind;
    }

    public void setVarbind(final Varbind varbind) {
        this.varbind = varbind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,
                status,
                writeable,
                uei,
                description,
                rule,
                noticeQueue,
                destinationPath,
                textMessage,
                subject,
                numericMessage,
                eventSeverity,
                parameters,
                varbind);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Notification) {
            final Notification that = (Notification) obj;
            return Objects.equals(this.name, that.name)
                    && Objects.equals(this.status, that.status)
                    && Objects.equals(this.writeable, that.writeable)
                    && Objects.equals(this.uei, that.uei)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.rule, that.rule)
                    && Objects.equals(this.noticeQueue, that.noticeQueue)
                    && Objects.equals(this.destinationPath, that.destinationPath)
                    && Objects.equals(this.textMessage, that.textMessage)
                    && Objects.equals(this.subject, that.subject)
                    && Objects.equals(this.numericMessage, that.numericMessage)
                    && Objects.equals(this.eventSeverity, that.eventSeverity)
                    && Objects.equals(this.parameters, that.parameters)
                    && Objects.equals(this.varbind, that.varbind);
        }
        return false;
    }
}
