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
package org.opennms.features.events.store;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only representation of an event stored in the {@code events_archive} table.
 *
 * <p>This is a lightweight POJO — not a JPA entity. The archive is populated
 * by a Kafka consumer and queried via JDBC for REST, UI, and reporting purposes.</p>
 */
public class StoredEvent {

    private final long eventTsid;
    private final String eventUei;
    private final String eventSource;
    private final int eventSeverity;
    private final Instant eventTime;
    private final Long nodeId;
    private final String ipAddress;
    private final String serviceName;
    private final Integer ifIndex;
    private final String eventLogMsg;
    private final String eventDescr;
    private final String eventDisplay;
    private final String eventLog;
    private final Map<String, String> eventData;
    private final Instant createdAt;

    private StoredEvent(Builder builder) {
        this.eventTsid = builder.eventTsid;
        this.eventUei = Objects.requireNonNull(builder.eventUei, "eventUei");
        this.eventSource = builder.eventSource;
        this.eventSeverity = builder.eventSeverity;
        this.eventTime = Objects.requireNonNull(builder.eventTime, "eventTime");
        this.nodeId = builder.nodeId;
        this.ipAddress = builder.ipAddress;
        this.serviceName = builder.serviceName;
        this.ifIndex = builder.ifIndex;
        this.eventLogMsg = builder.eventLogMsg;
        this.eventDescr = builder.eventDescr;
        this.eventDisplay = builder.eventDisplay;
        this.eventLog = builder.eventLog;
        this.eventData = builder.eventData;
        this.createdAt = builder.createdAt;
    }

    public long getEventTsid() {
        return eventTsid;
    }

    public String getEventUei() {
        return eventUei;
    }

    public String getEventSource() {
        return eventSource;
    }

    public int getEventSeverity() {
        return eventSeverity;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public String getEventLogMsg() {
        return eventLogMsg;
    }

    public String getEventDescr() {
        return eventDescr;
    }

    public String getEventDisplay() {
        return eventDisplay;
    }

    public String getEventLog() {
        return eventLog;
    }

    public Map<String, String> getEventData() {
        return eventData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long eventTsid;
        private String eventUei;
        private String eventSource;
        private int eventSeverity;
        private Instant eventTime;
        private Long nodeId;
        private String ipAddress;
        private String serviceName;
        private Integer ifIndex;
        private String eventLogMsg;
        private String eventDescr;
        private String eventDisplay = "Y";
        private String eventLog = "Y";
        private Map<String, String> eventData;
        private Instant createdAt;

        public Builder eventTsid(long eventTsid) {
            this.eventTsid = eventTsid;
            return this;
        }

        public Builder eventUei(String eventUei) {
            this.eventUei = eventUei;
            return this;
        }

        public Builder eventSource(String eventSource) {
            this.eventSource = eventSource;
            return this;
        }

        public Builder eventSeverity(int eventSeverity) {
            this.eventSeverity = eventSeverity;
            return this;
        }

        public Builder eventTime(Instant eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder nodeId(Long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder ifIndex(Integer ifIndex) {
            this.ifIndex = ifIndex;
            return this;
        }

        public Builder eventLogMsg(String eventLogMsg) {
            this.eventLogMsg = eventLogMsg;
            return this;
        }

        public Builder eventDescr(String eventDescr) {
            this.eventDescr = eventDescr;
            return this;
        }

        public Builder eventDisplay(String eventDisplay) {
            this.eventDisplay = eventDisplay;
            return this;
        }

        public Builder eventLog(String eventLog) {
            this.eventLog = eventLog;
            return this;
        }

        public Builder eventData(Map<String, String> eventData) {
            this.eventData = eventData;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public StoredEvent build() {
            return new StoredEvent(this);
        }
    }

    @Override
    public String toString() {
        return "StoredEvent{" +
                "eventTsid=" + eventTsid +
                ", eventUei='" + eventUei + '\'' +
                ", eventTime=" + eventTime +
                ", nodeId=" + nodeId +
                '}';
    }
}
