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
package org.opennms.netmgt.flows.postgres;

import java.sql.Timestamp;

/**
 * A single row destined for the {@code flow} table: the promoted "hot" columns used by the
 * query/filter paths, plus the full enriched flow serialized as a jsonb document.
 */
public class FlowRow {
    public Timestamp flowTs;          // @timestamp equivalent; partition key + TimeRangeFilter selection
    public Long deltaSwitched;        // netflow.delta_switched (proration range start, epoch ms)
    public Long lastSwitched;         // netflow.last_switched  (proration range end, epoch ms)
    public Long firstSwitched;
    public Long bytes;
    public Long packets;
    public Double samplingInterval;
    public String direction;          // INGRESS / EGRESS
    public String application;
    public String convoKey;
    public String srcAddr;
    public String dstAddr;
    public Integer protocol;
    public Integer dscp;
    public Integer exporterNodeId;
    public Integer inputSnmp;
    public Integer outputSnmp;
    public String location;
    public String documentJson;       // full enriched flow as JSON (stored as jsonb)
}