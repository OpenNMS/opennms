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

/**
 * Query criteria for filtering events in the {@code events_archive} table.
 *
 * <p>Uses the builder pattern to compose filter predicates. All criteria
 * are ANDed together. {@code null} fields are not included in the filter.</p>
 */
public class EventCriteria {

    public enum SortOrder {
        ASC, DESC
    }

    private final String uei;
    private final Long nodeId;
    private final String ipAddress;
    private final String serviceName;
    private final Integer severityGte;
    private final Integer severityLte;
    private final Instant afterTime;
    private final Instant beforeTime;
    private final String eventDisplayFilter;
    private final SortOrder sortOrder;
    private final int limit;
    private final int offset;

    private EventCriteria(Builder builder) {
        this.uei = builder.uei;
        this.nodeId = builder.nodeId;
        this.ipAddress = builder.ipAddress;
        this.serviceName = builder.serviceName;
        this.severityGte = builder.severityGte;
        this.severityLte = builder.severityLte;
        this.afterTime = builder.afterTime;
        this.beforeTime = builder.beforeTime;
        this.eventDisplayFilter = builder.eventDisplayFilter;
        this.sortOrder = builder.sortOrder;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    public String getUei() {
        return uei;
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

    public Integer getSeverityGte() {
        return severityGte;
    }

    public Integer getSeverityLte() {
        return severityLte;
    }

    public Instant getAfterTime() {
        return afterTime;
    }

    public Instant getBeforeTime() {
        return beforeTime;
    }

    public String getEventDisplayFilter() {
        return eventDisplayFilter;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uei;
        private Long nodeId;
        private String ipAddress;
        private String serviceName;
        private Integer severityGte;
        private Integer severityLte;
        private Instant afterTime;
        private Instant beforeTime;
        private String eventDisplayFilter;
        private SortOrder sortOrder = SortOrder.DESC;
        private int limit = 100;
        private int offset = 0;

        public Builder uei(String uei) {
            this.uei = uei;
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

        public Builder severityGte(Integer severityGte) {
            this.severityGte = severityGte;
            return this;
        }

        public Builder severityLte(Integer severityLte) {
            this.severityLte = severityLte;
            return this;
        }

        public Builder afterTime(Instant afterTime) {
            this.afterTime = afterTime;
            return this;
        }

        public Builder beforeTime(Instant beforeTime) {
            this.beforeTime = beforeTime;
            return this;
        }

        public Builder eventDisplayFilter(String eventDisplayFilter) {
            this.eventDisplayFilter = eventDisplayFilter;
            return this;
        }

        public Builder sortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public EventCriteria build() {
            return new EventCriteria(this);
        }
    }
}
