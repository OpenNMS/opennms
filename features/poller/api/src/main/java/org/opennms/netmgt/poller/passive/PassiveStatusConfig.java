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
package org.opennms.netmgt.poller.passive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.poller.PollStatus;

/**
 * Serializable configuration for passive service status, transported via Twin API
 * from Pollerd to Minion so that PassiveServiceMonitor can execute on Minion.
 */
public class PassiveStatusConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TWIN_KEY = "passive-status";

    private List<Entry> entries = new ArrayList<>();

    public PassiveStatusConfig() {
    }

    public PassiveStatusConfig(List<Entry> entries) {
        this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
    }

    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }

    public Map<PassiveStatusKey, PollStatus> toStatusMap() {
        Map<PassiveStatusKey, PollStatus> map = new HashMap<>();
        for (Entry entry : entries) {
            PassiveStatusKey key = new PassiveStatusKey(entry.getNodeLabel(), entry.getIpAddr(), entry.getServiceName());
            PollStatus status = PollStatus.decode(entry.getStatusName(), entry.getReason());
            map.put(key, status);
        }
        return map;
    }

    public static PassiveStatusConfig fromStatusMap(Map<PassiveStatusKey, PollStatus> statusMap) {
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<PassiveStatusKey, PollStatus> mapEntry : statusMap.entrySet()) {
            PassiveStatusKey key = mapEntry.getKey();
            PollStatus status = mapEntry.getValue();
            entries.add(new Entry(key.getNodeLabel(), key.getIpAddr(), key.getServiceName(),
                    status.getStatusName(), status.getReason()));
        }
        return new PassiveStatusConfig(entries);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(entries, ((PassiveStatusConfig) o).entries);
    }

    @Override
    public int hashCode() { return Objects.hash(entries); }

    @Override
    public String toString() { return "PassiveStatusConfig{entries=" + entries.size() + "}"; }

    public static class Entry implements Serializable {
        private static final long serialVersionUID = 1L;
        private String nodeLabel;
        private String ipAddr;
        private String serviceName;
        private String statusName;
        private String reason;

        public Entry() {}

        public Entry(String nodeLabel, String ipAddr, String serviceName, String statusName, String reason) {
            this.nodeLabel = nodeLabel;
            this.ipAddr = ipAddr;
            this.serviceName = serviceName;
            this.statusName = statusName;
            this.reason = reason;
        }

        public String getNodeLabel() { return nodeLabel; }
        public void setNodeLabel(String nodeLabel) { this.nodeLabel = nodeLabel; }
        public String getIpAddr() { return ipAddr; }
        public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getStatusName() { return statusName; }
        public void setStatusName(String statusName) { this.statusName = statusName; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry e = (Entry) o;
            return Objects.equals(nodeLabel, e.nodeLabel) && Objects.equals(ipAddr, e.ipAddr) &&
                    Objects.equals(serviceName, e.serviceName) && Objects.equals(statusName, e.statusName) &&
                    Objects.equals(reason, e.reason);
        }

        @Override
        public int hashCode() { return Objects.hash(nodeLabel, ipAddr, serviceName, statusName, reason); }

        @Override
        public String toString() { return nodeLabel + ":" + ipAddr + ":" + serviceName + " -> " + statusName; }
    }
}
