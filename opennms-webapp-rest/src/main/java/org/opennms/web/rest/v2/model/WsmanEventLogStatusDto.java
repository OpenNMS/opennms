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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.wsman.eventlog.EventLogReadStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/** The daemon's last read outcome per node and log, one row per (node, package, log). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsmanEventLogStatusDto {

    public static class Row {
        public int nodeId;
        public String nodeLabel;
        public String ipAddress;
        public String location;
        public String packageName;
        public String log;
        public Long lastSuccess;
        public Long lastFailure;
        public String lastError;
        public int consecutiveFailures;
        public boolean backingOff;
        public long recordsRead;
        public long eventsPublished;
        public Long cursor;
    }

    public List<Row> rows = new ArrayList<>();

    public static WsmanEventLogStatusDto from(final List<EventLogReadStatus> statuses) {
        final WsmanEventLogStatusDto dto = new WsmanEventLogStatusDto();
        for (final EventLogReadStatus status : statuses) {
            for (final EventLogReadStatus.LogStatus log : status.logs) {
                final Row row = new Row();
                row.nodeId = status.nodeId;
                row.nodeLabel = status.nodeLabel;
                row.ipAddress = status.address;
                row.location = status.location;
                row.packageName = log.packageName;
                row.log = log.log;
                row.lastSuccess = log.lastSuccess;
                row.lastFailure = log.lastFailure;
                row.lastError = log.lastError;
                row.consecutiveFailures = log.consecutiveFailures;
                row.backingOff = log.backingOff;
                row.recordsRead = log.recordsRead;
                row.eventsPublished = log.eventsPublished;
                row.cursor = log.cursor;
                dto.rows.add(row);
            }
        }
        return dto;
    }
}
