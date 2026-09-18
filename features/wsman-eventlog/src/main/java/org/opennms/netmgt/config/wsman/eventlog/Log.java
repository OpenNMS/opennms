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

/** One Windows event log (Application, System, Security, ...) and how often to read it. */
@XmlAccessorType(XmlAccessType.FIELD)
public class Log {

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "enabled")
    private Boolean enabled;

    /** Poll interval in milliseconds. */
    @XmlAttribute(name = "interval")
    private Long interval;

    @XmlAttribute(name = "max-records")
    private Integer maxRecords;

    /** How far back the first poll of a node reads, e.g. {@code 1h}, {@code 15m}, {@code 2d}. */
    @XmlAttribute(name = "lookback")
    private String lookback;

    /** Comma-separated {@code Error,Warning,Information,AuditSuccess,AuditFailure}; empty keeps every level. */
    @XmlAttribute(name = "levels")
    private String levels;

    /** Comma-separated Event IDs to keep; empty keeps every ID. */
    @XmlAttribute(name = "include-event-ids")
    private String includeEventIds;

    @XmlAttribute(name = "exclude-event-ids")
    private String excludeEventIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public long getInterval() {
        return interval == null ? 60_000L : interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public int getMaxRecords() {
        return maxRecords == null ? 500 : maxRecords;
    }

    public void setMaxRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
    }

    public String getLookback() {
        return lookback == null ? "1h" : lookback;
    }

    public void setLookback(String lookback) {
        this.lookback = lookback;
    }

    public String getLevels() {
        return levels;
    }

    public void setLevels(String levels) {
        this.levels = levels;
    }

    public String getIncludeEventIds() {
        return includeEventIds;
    }

    public void setIncludeEventIds(String includeEventIds) {
        this.includeEventIds = includeEventIds;
    }

    public String getExcludeEventIds() {
        return excludeEventIds;
    }

    public void setExcludeEventIds(String excludeEventIds) {
        this.excludeEventIds = excludeEventIds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enabled, interval, maxRecords, lookback, levels, includeEventIds, excludeEventIds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Log)) {
            return false;
        }
        final Log other = (Log) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(enabled, other.enabled)
                && Objects.equals(interval, other.interval)
                && Objects.equals(maxRecords, other.maxRecords)
                && Objects.equals(lookback, other.lookback)
                && Objects.equals(levels, other.levels)
                && Objects.equals(includeEventIds, other.includeEventIds)
                && Objects.equals(excludeEventIds, other.excludeEventIds);
    }
}
