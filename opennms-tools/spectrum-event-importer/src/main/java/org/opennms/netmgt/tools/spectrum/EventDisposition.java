/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tools.spectrum;

import java.util.ArrayList;
import java.util.List;

public class EventDisposition {

    private String m_eventCode;
    private boolean m_logEvent;         /* Maps to logmsg dest="donotpersist" if false, dest="logndisplay" if true */
    private int m_eventSeverity;        /* Unused */
    private boolean m_createAlarm;      /* Whether to annotate event with alarm-data */
    private int m_alarmSeverity;        /* Spectrum alarm severity value */
    private String m_alarmCause;        /* Often, but not always, same as eventCode */
    private boolean m_clearAlarm;       /* Whether this alarm clears another alarm type */
    private String m_clearAlarmCause;   /* Which alarm type this alarm clears if isClearAlarm */
    private boolean m_uniqueAlarm;      /* Whether to create a new alarm for each occurrence of this event */
    private boolean m_userClearable;    /* Whether this alarm is user-clearable */
    private boolean m_persistent;       /* Whether this alarm is persisted across restarts */

    private List<Integer> m_discriminators;     /* An optional list of event parameter numbers used in deduplication */
    
    
    public EventDisposition(String eventCode) {
        if (eventCode == null) {
            throw new IllegalArgumentException("The event code must not be null");
        }
        m_eventCode = eventCode;
        
        m_logEvent = false;
        m_eventSeverity = -1;
        m_createAlarm = false;
        m_alarmSeverity = -1;
        m_alarmCause = null;
        m_clearAlarm = false;
        m_clearAlarmCause = null;
        m_uniqueAlarm = false;
        m_userClearable = true;
        m_persistent = true;
        m_discriminators = new ArrayList<>();
    }
    
    public String toString() {
        final StringBuilder bldr = new StringBuilder("EventDisposition [");
        bldr.append("eventCode: ").append(m_eventCode).append("; ");
        bldr.append("logEvent: ").append(m_logEvent).append("; ");
        bldr.append("eventSeverity: ").append(m_eventSeverity).append("; ");
        bldr.append("createAlarm: ").append(m_createAlarm).append("; ");
        bldr.append("alarmSeverity: ").append(m_alarmSeverity).append("; ");
        bldr.append("alarmCause: ").append(m_alarmCause).append("; ");
        bldr.append("clearAlarm: ").append(m_clearAlarm).append("; ");
        bldr.append("clearAlarmCause: ").append(m_clearAlarmCause).append("; ");
        bldr.append("uniqueAlarm: ").append(m_uniqueAlarm).append("; ");
        bldr.append("userClearable: ").append(m_userClearable).append("; ");
        bldr.append("persistent: ").append(m_persistent).append("; ");
        bldr.append("discriminators: ");
        for (int dis : m_discriminators) {
            bldr.append(" ").append(dis).append(" ");
        }
        bldr.append(";");
        bldr.append("]");
        return bldr.toString();
    }
    
    /**
     * @return the m_eventCode
     */
    public String getEventCode() {
        return m_eventCode;
    }

    /**
     * @param eventCode the eventCode to set
     */
    public void setEventCode(String eventCode) {
        m_eventCode = eventCode;
    }

    /**
     * @return the logEvent
     */
    public boolean isLogEvent() {
        return m_logEvent;
    }

    /**
     * @param logEvent the logEvent to set
     */
    public void setLogEvent(boolean logEvent) {
        m_logEvent = logEvent;
    }

    /**
     * @return the eventSeverity
     */
    public int getEventSeverity() {
        return m_eventSeverity;
    }

    /**
     * @param eventSeverity the eventSeverity to set
     */
    public void setEventSeverity(int eventSeverity) {
        m_eventSeverity = eventSeverity;
    }

    /**
     * @return the createAlarm
     */
    public boolean isCreateAlarm() {
        return m_createAlarm;
    }

    /**
     * @param createAlarm the createAlarm to set
     */
    public void setCreateAlarm(boolean createAlarm) {
        m_createAlarm = createAlarm;
    }

    /**
     * @return the alarmSeverity
     */
    public int getAlarmSeverity() {
        return m_alarmSeverity;
    }

    /**
     * @param alarmSeverity the alarmSeverity to set
     */
    public void setAlarmSeverity(int alarmSeverity) {
        m_alarmSeverity = alarmSeverity;
    }

    /**
     * @return the alarmCause
     */
    public String getAlarmCause() {
        return m_alarmCause;
    }

    /**
     * @param alarmCause the alarmCause to set
     */
    public void setAlarmCause(String alarmCause) {
        m_alarmCause = alarmCause;
    }

    /**
     * @return the clearAlarm
     */
    public boolean isClearAlarm() {
        return m_clearAlarm;
    }

    /**
     * @param clearAlarm the clearAlarm to set
     */
    public void setClearAlarm(boolean clearAlarm) {
        m_clearAlarm = clearAlarm;
    }
    
    /**
     * @return the clearAlarmCause
     */
    public String getClearAlarmCause() {
        return m_clearAlarmCause;
    }
    
    /**
     * @param clearAlarmCause the clearAlarmCause to set
     */
    public void setClearAlarmCause(String clearAlarmCause) {
        m_clearAlarmCause = clearAlarmCause;
    }

    /**
     * @return the uniqueAlarm
     */
    public boolean isUniqueAlarm() {
        return m_uniqueAlarm;
    }

    /**
     * @param uniqueAlarm the uniqueAlarm to set
     */
    public void setUniqueAlarm(boolean uniqueAlarm) {
        m_uniqueAlarm = uniqueAlarm;
    }

    /**
     * @return the userClearable
     */
    public boolean isUserClearable() {
        return m_userClearable;
    }

    /**
     * @param userClearable the userClearable to set
     */
    public void setUserClearable(boolean userClearable) {
        m_userClearable = userClearable;
    }

    /**
     * @return the persistent
     */
    public boolean isPersistent() {
        return m_persistent;
    }

    /**
     * @param persistent the persistent to set
     */
    public void setPersistent(boolean persistent) {
        m_persistent = persistent;
    }

    /**
     * @return the discriminators
     */
    public List<Integer> getDiscriminators() {
        return m_discriminators;
    }

    /**
     * @param discriminators the discriminators to set
     */
    public void setDiscriminators(List<Integer> discriminators) {
        m_discriminators = discriminators;
    }
    
    /**
     * @param discriminator the discriminator to add
     */
    public void addDiscriminator(int discriminator) {
        m_discriminators.add(discriminator);
    }
}