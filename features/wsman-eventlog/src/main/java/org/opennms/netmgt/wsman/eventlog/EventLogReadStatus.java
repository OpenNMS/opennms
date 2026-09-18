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
package org.opennms.netmgt.wsman.eventlog;

import java.util.ArrayList;
import java.util.List;

/** What the daemon last saw for one node, one entry per package and log; stored as JSON. */
public class EventLogReadStatus {

    public int nodeId;
    public String nodeLabel;
    public String address;
    public String location;
    public List<LogStatus> logs = new ArrayList<>();

    public static class LogStatus {
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

    public LogStatus forLog(String packageName, String log) {
        for (LogStatus status : logs) {
            if (status.packageName.equals(packageName) && status.log.equalsIgnoreCase(log)) {
                return status;
            }
        }
        final LogStatus status = new LogStatus();
        status.packageName = packageName;
        status.log = log;
        logs.add(status);
        return status;
    }
}
