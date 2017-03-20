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

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the notifd-configuration.xml
 *  configuration file.
 */
@XmlRootElement(name = "notifd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifd-configuration.xsd")
public class NotifdConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_PAGES_SENT = "SELECT * FROM notifications";
    private static final String DEFAULT_NEXT_NOTIFID = "SELECT nextval('notifynxtid')";
    private static final String DEFAULT_NEXT_USER_NOTIFID = "SELECT nextval('userNotifNxtId')";
    private static final String DEFAULT_NEXT_GROUP_ID = "SELECT nextval('notifygrpid')";
    private static final String DEFAULT_SERVICEID_SQL = "SELECT serviceID from service where serviceName = ?";
    private static final String DEFAULT_OUTSTANDING_NOTICES_SQL = "SELECT notifyid FROM notifications where notifyId = ? AND respondTime is not null";
    private static final String DEFAULT_ACKNOWLEDGEID_SQL = "SELECT notifyid FROM notifications WHERE eventuei=? AND nodeid=? AND interfaceid=? AND serviceid=?";
    private static final String DEFAULT_ACKNOWLEDGE_UPDATE_SQL = "UPDATE notifications SET answeredby=?, respondtime=? WHERE notifyId=?";
    private static final String DEFAULT_EMAIL_ADDRESS_COMMAND = "javaEmail";

    @XmlAttribute(name = "status", required = true)
    private String m_status;

    @XmlAttribute(name = "pages-sent")
    private String m_pagesSent;

    @XmlAttribute(name = "next-notif-id")
    private String m_nextNotifId;

    @XmlAttribute(name = "next-user-notif-id")
    private String m_nextUserNotifId;

    @XmlAttribute(name = "next-group-id")
    private String m_nextGroupId;

    @XmlAttribute(name = "service-id-sql")
    private String m_serviceIdSql;

    @XmlAttribute(name = "outstanding-notices-sql")
    private String m_outstandingNoticesSql;

    @XmlAttribute(name = "acknowledge-id-sql")
    private String m_acknowledgeIdSql;

    @XmlAttribute(name = "acknowledge-update-sql")
    private String m_acknowledgeUpdateSql;

    @XmlAttribute(name = "match-all", required = true)
    private Boolean m_matchAll;

    @XmlAttribute(name = "email-address-command")
    private String m_emailAddressCommand;

    @XmlAttribute(name = "numeric-skip-resolution-prefix")
    private Boolean m_numericSkipResolutionPrefix;

    @XmlElement(name = "auto-acknowledge-alarm")
    private AutoAcknowledgeAlarm m_autoAcknowledgeAlarm;

    @XmlElement(name = "auto-acknowledge")
    private List<AutoAcknowledge> m_autoAcknowledges = new ArrayList<>();

    @XmlElement(name = "queue", required = true)
    private List<Queue> m_queues = new ArrayList<>();

    @XmlElement(name = "outage-calendar")
    private List<String> m_outageCalendars = new ArrayList<>();

    public NotifdConfiguration() { }

    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        if (status == null) {
            throw new IllegalArgumentException("'status' is a required attribute!");
        }
        m_status = status;
    }

    public String getPagesSent() {
        return m_pagesSent != null ? m_pagesSent : DEFAULT_PAGES_SENT;
    }

    public void setPagesSent(final String pagesSent) {
        m_pagesSent = pagesSent;
    }

    public String getNextNotifId() {
        return m_nextNotifId != null ? m_nextNotifId : DEFAULT_NEXT_NOTIFID;
    }

    public void setNextNotifId(final String nextNotifId) {
        m_nextNotifId = nextNotifId;
    }

    public String getNextUserNotifId() {
        return m_nextUserNotifId != null ? m_nextUserNotifId : DEFAULT_NEXT_USER_NOTIFID;
    }

    public void setNextUserNotifId(final String nextUserNotifId) {
        m_nextUserNotifId = nextUserNotifId;
    }

    public String getNextGroupId() {
        return m_nextGroupId != null ? m_nextGroupId : DEFAULT_NEXT_GROUP_ID;
    }

    public void setNextGroupId(final String nextGroupId) {
        m_nextGroupId = nextGroupId;
    }

    public String getServiceIdSql() {
        return m_serviceIdSql != null ? m_serviceIdSql : DEFAULT_SERVICEID_SQL;
    }

    public void setServiceIdSql(final String serviceIdSql) {
        m_serviceIdSql = serviceIdSql;
    }

    public String getOutstandingNoticesSql() {
        return m_outstandingNoticesSql != null ? m_outstandingNoticesSql : DEFAULT_OUTSTANDING_NOTICES_SQL;
    }

    public void setOutstandingNoticesSql(final String outstandingNoticesSql) {
        m_outstandingNoticesSql = outstandingNoticesSql;
    }

    public String getAcknowledgeIdSql() {
        return m_acknowledgeIdSql != null ? m_acknowledgeIdSql : DEFAULT_ACKNOWLEDGEID_SQL;
    }

    public void setAcknowledgeIdSql(final String acknowledgeIdSql) {
        m_acknowledgeIdSql = acknowledgeIdSql;
    }

    public String getAcknowledgeUpdateSql() {
        return m_acknowledgeUpdateSql != null ? m_acknowledgeUpdateSql : DEFAULT_ACKNOWLEDGE_UPDATE_SQL;
    }

    public void setAcknowledgeUpdateSql(final String acknowledgeUpdateSql) {
        m_acknowledgeUpdateSql = acknowledgeUpdateSql;
    }

    public Boolean getMatchAll() {
        return m_matchAll;
    }

    public void setMatchAll(final Boolean matchAll) {
        if (matchAll == null) {
            throw new IllegalArgumentException("match-all is a required field!");
        }
        m_matchAll = matchAll;
    }

    public String getEmailAddressCommand() {
        return m_emailAddressCommand != null ? m_emailAddressCommand : DEFAULT_EMAIL_ADDRESS_COMMAND;
    }

    public void setEmailAddressCommand(final String emailAddressCommand) {
        m_emailAddressCommand = emailAddressCommand;
    }

    public Boolean getNumericSkipResolutionPrefix() {
        return m_numericSkipResolutionPrefix != null ? m_numericSkipResolutionPrefix : Boolean.valueOf("false");
    }

    public void setNumericSkipResolutionPrefix(final Boolean prefix) {
        m_numericSkipResolutionPrefix = prefix;
    }

    public Optional<AutoAcknowledgeAlarm> getAutoAcknowledgeAlarm() {
        return Optional.ofNullable(m_autoAcknowledgeAlarm);
    }

    public void setAutoAcknowledgeAlarm(final AutoAcknowledgeAlarm autoAcknowledgeAlarm) {
        m_autoAcknowledgeAlarm = autoAcknowledgeAlarm;
    }

    public List<AutoAcknowledge> getAutoAcknowledges() {
        return m_autoAcknowledges;
    }

    public void setAutoAcknowledges(final List<AutoAcknowledge> autoAcknowledges) {
        m_autoAcknowledges.clear();
        m_autoAcknowledges.addAll(autoAcknowledges);
    }

    public void addAutoAcknowledge(final AutoAcknowledge autoAcknowledge) throws IndexOutOfBoundsException {
        m_autoAcknowledges.add(autoAcknowledge);
    }

    public List<String> getOutageCalendars() {
        return m_outageCalendars;
    }

    public void setOutageCalendars(final List<String> calendars) {
        m_outageCalendars.clear();
        m_outageCalendars.addAll(calendars);
    }

    public void addOutageCalendar(final String calendar) {
        m_outageCalendars.add(calendar);
    }

    public boolean removeOutageCalendar(final String calendar) {
        return m_outageCalendars.remove(calendar);
    }

    public List<Queue> getQueues() {
        return m_queues;
    }

    public void setQueues(final List<Queue> queues) {
        m_queues.clear();
        m_queues.addAll(queues);
    }

    public void addQueue(final Queue queue) {
        m_queues.add(queue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_status, 
                            m_pagesSent, 
                            m_nextNotifId, 
                            m_nextUserNotifId, 
                            m_nextGroupId, 
                            m_serviceIdSql, 
                            m_outstandingNoticesSql, 
                            m_acknowledgeIdSql, 
                            m_acknowledgeUpdateSql, 
                            m_matchAll, 
                            m_emailAddressCommand, 
                            m_numericSkipResolutionPrefix, 
                            m_autoAcknowledgeAlarm, 
                            m_autoAcknowledges, 
                            m_queues, 
                            m_outageCalendars);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof NotifdConfiguration) {
            final NotifdConfiguration that = (NotifdConfiguration)obj;
            return Objects.equals(this.m_status, that.m_status)
                    && Objects.equals(this.m_pagesSent, that.m_pagesSent)
                    && Objects.equals(this.m_nextNotifId, that.m_nextNotifId)
                    && Objects.equals(this.m_nextUserNotifId, that.m_nextUserNotifId)
                    && Objects.equals(this.m_nextGroupId, that.m_nextGroupId)
                    && Objects.equals(this.m_serviceIdSql, that.m_serviceIdSql)
                    && Objects.equals(this.m_outstandingNoticesSql, that.m_outstandingNoticesSql)
                    && Objects.equals(this.m_acknowledgeIdSql, that.m_acknowledgeIdSql)
                    && Objects.equals(this.m_acknowledgeUpdateSql, that.m_acknowledgeUpdateSql)
                    && Objects.equals(this.m_matchAll, that.m_matchAll)
                    && Objects.equals(this.m_emailAddressCommand, that.m_emailAddressCommand)
                    && Objects.equals(this.m_numericSkipResolutionPrefix, that.m_numericSkipResolutionPrefix)
                    && Objects.equals(this.m_autoAcknowledgeAlarm, that.m_autoAcknowledgeAlarm)
                    && Objects.equals(this.m_autoAcknowledges, that.m_autoAcknowledges)
                    && Objects.equals(this.m_queues, that.m_queues)
                    && Objects.equals(this.m_outageCalendars, that.m_outageCalendars);
        }
        return false;
    }

}
