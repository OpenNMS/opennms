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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A set of logs to read from every node whose interface matches the filter and
 * carries the WS-Man service.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Package {

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlElement(name = "filter", required = true)
    private String filter;

    @XmlElement(name = "log")
    private List<Log> logs = new ArrayList<>();

    @XmlElement(name = "event-mapping")
    private List<EventMapping> eventMappings = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs == null ? new ArrayList<>() : logs;
    }

    public List<EventMapping> getEventMappings() {
        return eventMappings;
    }

    public void setEventMappings(List<EventMapping> eventMappings) {
        this.eventMappings = eventMappings == null ? new ArrayList<>() : eventMappings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filter, logs, eventMappings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Package)) {
            return false;
        }
        final Package other = (Package) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(filter, other.filter)
                && Objects.equals(logs, other.logs)
                && Objects.equals(eventMappings, other.eventMappings);
    }
}
