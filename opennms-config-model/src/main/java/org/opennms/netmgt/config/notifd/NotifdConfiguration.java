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

package org.opennms.netmgt.config.notifd;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the notifd-configuration.xml
 *  configuration file.
 */
public class NotifdConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    @JsonIgnore
    private static final String DEFAULT_PAGES_SENT = "SELECT * FROM notifications";
    @JsonIgnore
    private static final String DEFAULT_NEXT_NOTIFID = "SELECT nextval('notifynxtid')";
    @JsonIgnore
    private static final String DEFAULT_NEXT_USER_NOTIFID = "SELECT nextval('userNotifNxtId')";
    @JsonIgnore
    private static final String DEFAULT_NEXT_GROUP_ID = "SELECT nextval('notifygrpid')";
    @JsonIgnore
    private static final String DEFAULT_SERVICEID_SQL = "SELECT serviceID from service where serviceName = ?";
    @JsonIgnore
    private static final String DEFAULT_OUTSTANDING_NOTICES_SQL = "SELECT notifyid FROM notifications where notifyId = ? AND respondTime is not null";
    @JsonIgnore
    private static final String DEFAULT_ACKNOWLEDGEID_SQL = "SELECT notifyid FROM notifications WHERE eventuei=? AND nodeid=? AND interfaceid=? AND serviceid=?";
    @JsonIgnore
    private static final String DEFAULT_ACKNOWLEDGE_UPDATE_SQL = "UPDATE notifications SET answeredby=?, respondtime=? WHERE notifyId=?";
    @JsonIgnore
    private static final String DEFAULT_EMAIL_ADDRESS_COMMAND = "javaEmail";
    @JsonIgnore
    private static final Integer DEFAULT_MAX_THREADS = 100;

    private String status;

    private String pagesSent;

    private String nextNotifId;

    private String nextUserNotifId;

    private String nextGroupId;

    private String serviceIdSql;

    private String outstandingNoticesSql;

    private String acknowledgeIdSql;

    private String acknowledgeUpdateSql;

    private Boolean matchAll;

    private String emailAddressCommand;

    private Boolean numericSkipResolutionPrefix;

    private Integer maxThreads;

    private AutoAcknowledgeAlarm autoAcknowledgeAlarm;

    @JsonProperty("auto-acknowledge")
    private List<AutoAcknowledge> autoAcknowledges = new ArrayList<>();

    @JsonProperty("queue")
    private List<Queue> queues = new ArrayList<>();

    @JsonProperty("outage-calendar")
    private List<String> outageCalendars = new ArrayList<>();

    public NotifdConfiguration() { }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = ConfigUtils.assertNotEmpty(status, "status");
    }

    public String getPagesSent() {
        return pagesSent != null ? pagesSent : DEFAULT_PAGES_SENT;
    }

    public void setPagesSent(final String pagesSent) {
        this.pagesSent = ConfigUtils.normalizeString(pagesSent);
    }

    public String getNextNotifId() {
        return nextNotifId != null ? nextNotifId : DEFAULT_NEXT_NOTIFID;
    }

    public void setNextNotifId(final String nextNotifId) {
        this.nextNotifId = ConfigUtils.normalizeString(nextNotifId);
    }

    public String getNextUserNotifId() {
        return nextUserNotifId != null ? nextUserNotifId : DEFAULT_NEXT_USER_NOTIFID;
    }

    public void setNextUserNotifId(final String nextUserNotifId) {
        this.nextUserNotifId = ConfigUtils.normalizeString(nextUserNotifId);
    }

    public String getNextGroupId() {
        return nextGroupId != null ? nextGroupId : DEFAULT_NEXT_GROUP_ID;
    }

    public void setNextGroupId(final String nextGroupId) {
        this.nextGroupId = ConfigUtils.normalizeString(nextGroupId);
    }

    public String getServiceIdSql() {
        return serviceIdSql != null ? serviceIdSql : DEFAULT_SERVICEID_SQL;
    }

    public void setServiceIdSql(final String serviceIdSql) {
        this.serviceIdSql = ConfigUtils.normalizeString(serviceIdSql);
    }

    public String getOutstandingNoticesSql() {
        return outstandingNoticesSql != null ? outstandingNoticesSql : DEFAULT_OUTSTANDING_NOTICES_SQL;
    }

    public void setOutstandingNoticesSql(final String outstandingNoticesSql) {
        this.outstandingNoticesSql = ConfigUtils.normalizeString(outstandingNoticesSql);
    }

    public String getAcknowledgeIdSql() {
        return acknowledgeIdSql != null ? acknowledgeIdSql : DEFAULT_ACKNOWLEDGEID_SQL;
    }

    public void setAcknowledgeIdSql(final String acknowledgeIdSql) {
        this.acknowledgeIdSql = ConfigUtils.normalizeString(acknowledgeIdSql);
    }

    public String getAcknowledgeUpdateSql() {
        return acknowledgeUpdateSql != null ? acknowledgeUpdateSql : DEFAULT_ACKNOWLEDGE_UPDATE_SQL;
    }

    public void setAcknowledgeUpdateSql(final String acknowledgeUpdateSql) {
        this.acknowledgeUpdateSql = ConfigUtils.normalizeString(acknowledgeUpdateSql);
    }

    public Boolean getMatchAll() {
        return matchAll;
    }

    public void setMatchAll(final Boolean matchAll) {
        this.matchAll = ConfigUtils.assertNotNull(matchAll, "match-all");
    }

    public String getEmailAddressCommand() {
        return emailAddressCommand != null ? emailAddressCommand : DEFAULT_EMAIL_ADDRESS_COMMAND;
    }

    public void setEmailAddressCommand(final String emailAddressCommand) {
        this.emailAddressCommand = ConfigUtils.normalizeString(emailAddressCommand);
    }

    public Boolean getNumericSkipResolutionPrefix() {
        return numericSkipResolutionPrefix != null ? numericSkipResolutionPrefix : Boolean.valueOf("false");
    }

    public void setNumericSkipResolutionPrefix(final Boolean prefix) {
        this.numericSkipResolutionPrefix = prefix;
    }

    public Integer getMaxThreads() {
        return maxThreads != null ? maxThreads : DEFAULT_MAX_THREADS;
    }

    public void setMaxThreads(Integer maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Optional<AutoAcknowledgeAlarm> getAutoAcknowledgeAlarm() {
        return Optional.ofNullable(autoAcknowledgeAlarm);
    }

    public void setAutoAcknowledgeAlarm(final AutoAcknowledgeAlarm autoAcknowledgeAlarm) {
        this.autoAcknowledgeAlarm = autoAcknowledgeAlarm;
    }

    public List<AutoAcknowledge> getAutoAcknowledges() {
        return autoAcknowledges;
    }

    public void setAutoAcknowledges(final List<AutoAcknowledge> autoAcknowledges) {
        this.autoAcknowledges.clear();
        this.autoAcknowledges.addAll(autoAcknowledges);
    }

    public void addAutoAcknowledge(final AutoAcknowledge autoAcknowledge) throws IndexOutOfBoundsException {
        this.autoAcknowledges.add(autoAcknowledge);
    }

    public List<String> getOutageCalendars() {
        return outageCalendars;
    }

    public void setOutageCalendars(final List<String> calendars) {
        if (calendars == outageCalendars) return;
        this.outageCalendars.clear();
        if (calendars != null) this.outageCalendars.addAll(calendars);
    }

    public void addOutageCalendar(final String calendar) {
        this.outageCalendars.add(calendar);
    }

    public boolean removeOutageCalendar(final String calendar) {
        return this.outageCalendars.remove(calendar);
    }

    public List<Queue> getQueues() {
        return this.queues;
    }

    public void setQueues(final List<Queue> queues) {
        if (this.queues == queues) return;
        this.queues.clear();
        if (queues != null) this.queues.addAll(queues);
    }

    public void addQueue(final Queue queue) {
        this.queues.add(queue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, 
                            pagesSent, 
                            nextNotifId, 
                            nextUserNotifId, 
                            nextGroupId, 
                            serviceIdSql, 
                            outstandingNoticesSql, 
                            acknowledgeIdSql, 
                            acknowledgeUpdateSql, 
                            matchAll, 
                            emailAddressCommand, 
                            numericSkipResolutionPrefix,
                            maxThreads,
                            autoAcknowledgeAlarm, 
                            autoAcknowledges, 
                            queues, 
                            outageCalendars);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof NotifdConfiguration) {
            final NotifdConfiguration that = (NotifdConfiguration)obj;
            return Objects.equals(this.status, that.status)
                    && Objects.equals(this.pagesSent, that.pagesSent)
                    && Objects.equals(this.nextNotifId, that.nextNotifId)
                    && Objects.equals(this.nextUserNotifId, that.nextUserNotifId)
                    && Objects.equals(this.nextGroupId, that.nextGroupId)
                    && Objects.equals(this.serviceIdSql, that.serviceIdSql)
                    && Objects.equals(this.outstandingNoticesSql, that.outstandingNoticesSql)
                    && Objects.equals(this.acknowledgeIdSql, that.acknowledgeIdSql)
                    && Objects.equals(this.acknowledgeUpdateSql, that.acknowledgeUpdateSql)
                    && Objects.equals(this.matchAll, that.matchAll)
                    && Objects.equals(this.emailAddressCommand, that.emailAddressCommand)
                    && Objects.equals(this.numericSkipResolutionPrefix, that.numericSkipResolutionPrefix)
                    && Objects.equals(this.maxThreads, that.maxThreads)
                    && Objects.equals(this.autoAcknowledgeAlarm, that.autoAcknowledgeAlarm)
                    && Objects.equals(this.autoAcknowledges, that.autoAcknowledges)
                    && Objects.equals(this.queues, that.queues)
                    && Objects.equals(this.outageCalendars, that.outageCalendars);
        }
        return false;
    }
}