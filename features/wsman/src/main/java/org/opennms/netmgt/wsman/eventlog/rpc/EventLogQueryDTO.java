/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.wsman.eventlog.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * One event log to read on the remote host: everything after {@code afterRecordNumber},
 * or when no cursor is known yet everything at or after {@code sinceTime}.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class EventLogQueryDTO {

    public static final String MODE_WQL = "wql";
    public static final String MODE_SHELL = "shell";

    @XmlAttribute(name = "logfile")
    private String logfile;

    @XmlAttribute(name = "after-record-number")
    private Long afterRecordNumber;

    /** DMTF timestamp (yyyyMMddHHmmss.ffffff+UTC offset) used only when no cursor is known. */
    @XmlAttribute(name = "since-time")
    private String sinceTime;

    @XmlAttribute(name = "max-records")
    private int maxRecords = 500;

    /** {@link #MODE_WQL} reads Win32_NTLogEvent; {@link #MODE_SHELL} runs Get-WinEvent through WinRS. */
    @XmlAttribute(name = "mode")
    private String mode = MODE_WQL;

    /** Win32_NTLogEvent.EventType values to keep; empty means every level. */
    @XmlElement(name = "event-type")
    private List<Integer> eventTypes = new ArrayList<>();

    public EventLogQueryDTO() {
    }

    public EventLogQueryDTO(String logfile) {
        this.logfile = logfile;
    }

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public Long getAfterRecordNumber() {
        return afterRecordNumber;
    }

    public void setAfterRecordNumber(Long afterRecordNumber) {
        this.afterRecordNumber = afterRecordNumber;
    }

    public String getSinceTime() {
        return sinceTime;
    }

    public void setSinceTime(String sinceTime) {
        this.sinceTime = sinceTime;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    public String getMode() {
        return mode == null ? MODE_WQL : mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isShellMode() {
        return MODE_SHELL.equalsIgnoreCase(getMode());
    }

    public List<Integer> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<Integer> eventTypes) {
        this.eventTypes = eventTypes == null ? new ArrayList<>() : eventTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logfile, afterRecordNumber, sinceTime, maxRecords, mode, eventTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventLogQueryDTO)) {
            return false;
        }
        final EventLogQueryDTO other = (EventLogQueryDTO) obj;
        return Objects.equals(logfile, other.logfile)
                && Objects.equals(afterRecordNumber, other.afterRecordNumber)
                && Objects.equals(sinceTime, other.sinceTime)
                && maxRecords == other.maxRecords
                && Objects.equals(getMode(), other.getMode())
                && Objects.equals(eventTypes, other.eventTypes);
    }
}
