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
package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.model.OnmsSeverity;

public class MonitoredServiceStatusEntity {
    private final int nodeId;
    private final Date lastEventTime;
    private final OnmsSeverity severity;
    private final long alarmCount;
    private final InetAddress ipAddress;
    private final int serviceTypeId;

    public MonitoredServiceStatusEntity(final int nodeId, final InetAddress ipAddress, final int serviceTypeId,
                                        final Date lastEventTime, final OnmsSeverity severity, final long alarmCount) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.serviceTypeId = serviceTypeId;
        this.lastEventTime = lastEventTime;
        this.severity = severity;
        this.alarmCount = alarmCount;
    }

    public OnmsSeverity getSeverity() {
        return severity;
    }

    public long getCount() {
        return alarmCount;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getServiceTypeId() {
        return serviceTypeId;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }
}
