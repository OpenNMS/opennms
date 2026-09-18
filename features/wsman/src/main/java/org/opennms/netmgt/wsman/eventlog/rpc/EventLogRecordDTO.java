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

/** One Win32_NTLogEvent row, with the DMTF timestamp kept verbatim. */
@XmlAccessorType(XmlAccessType.NONE)
public class EventLogRecordDTO {

    @XmlAttribute(name = "logfile")
    private String logfile;

    @XmlAttribute(name = "record-number")
    private long recordNumber;

    @XmlAttribute(name = "event-code")
    private Integer eventCode;

    @XmlAttribute(name = "event-type")
    private Integer eventType;

    @XmlAttribute(name = "source-name")
    private String sourceName;

    @XmlAttribute(name = "time-generated")
    private String timeGenerated;

    @XmlAttribute(name = "computer-name")
    private String computerName;

    @XmlElement(name = "message")
    private String message;

    @XmlElement(name = "insertion-string")
    private List<String> insertionStrings = new ArrayList<>();

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public long getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(long recordNumber) {
        this.recordNumber = recordNumber;
    }

    public Integer getEventCode() {
        return eventCode;
    }

    public void setEventCode(Integer eventCode) {
        this.eventCode = eventCode;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = xmlSafe(sourceName);
    }

    public String getTimeGenerated() {
        return timeGenerated;
    }

    public void setTimeGenerated(String timeGenerated) {
        this.timeGenerated = timeGenerated;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = xmlSafe(computerName);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = xmlSafe(message);
    }

    public List<String> getInsertionStrings() {
        return insertionStrings;
    }

    public void setInsertionStrings(List<String> insertionStrings) {
        this.insertionStrings = new ArrayList<>();
        if (insertionStrings != null) {
            for (String s : insertionStrings) {
                this.insertionStrings.add(xmlSafe(s));
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(logfile, recordNumber, eventCode, eventType, sourceName, timeGenerated, computerName, message, insertionStrings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventLogRecordDTO)) {
            return false;
        }
        final EventLogRecordDTO other = (EventLogRecordDTO) obj;
        return recordNumber == other.recordNumber
                && Objects.equals(logfile, other.logfile)
                && Objects.equals(eventCode, other.eventCode)
                && Objects.equals(eventType, other.eventType)
                && Objects.equals(sourceName, other.sourceName)
                && Objects.equals(timeGenerated, other.timeGenerated)
                && Objects.equals(computerName, other.computerName)
                && Objects.equals(message, other.message)
                && Objects.equals(insertionStrings, other.insertionStrings);
    }

    @Override
    public String toString() {
        return "EventLogRecord[" + logfile + "#" + recordNumber + " id=" + eventCode + " type=" + eventType + " source=" + sourceName + "]";
    }
    /** Drops the control characters XML 1.0 cannot carry, so a record never poisons the RPC response. */
    static String xmlSafe(String value) {
        if (value == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == 0x9 || c == 0xA || c == 0xD || (c >= 0x20 && c <= 0xD7FF) || (c >= 0xE000 && c <= 0xFFFD)) {
                sb.append(c);
            }
        }
        return sb.length() == value.length() ? value : sb.toString();
    }
}
