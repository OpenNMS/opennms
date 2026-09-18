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
package org.opennms.netmgt.config.wsman.eventlog;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/** Gives one Event ID (optionally from one log and source) its own UEI and severity. */
@XmlAccessorType(XmlAccessType.FIELD)
public class EventMapping {

    @XmlAttribute(name = "logfile")
    private String logfile;

    @XmlAttribute(name = "source")
    private String source;

    @XmlAttribute(name = "event-id", required = true)
    private int eventId;

    @XmlAttribute(name = "uei", required = true)
    private String uei;

    @XmlAttribute(name = "severity")
    private String severity;

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getUei() {
        return uei;
    }

    public void setUei(String uei) {
        this.uei = uei;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logfile, source, eventId, uei, severity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventMapping)) {
            return false;
        }
        final EventMapping other = (EventMapping) obj;
        return eventId == other.eventId
                && Objects.equals(logfile, other.logfile)
                && Objects.equals(source, other.source)
                && Objects.equals(uei, other.uei)
                && Objects.equals(severity, other.severity);
    }
}
