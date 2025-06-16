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
package org.opennms.netmgt.telemetry.api.adapter;

import java.util.List;

public interface TelemetryMessageLog {
    /**
     * the Minion's location
     * @return the location
     */
    String getLocation();

    /**
     * the Minion's system ID
     * 
     * @return the system ID
     */
    String getSystemId();

    /**
     * the source address the incoming message log entries came from
     * @return the address
     */
    String getSourceAddress();

    /**
     * the source port the incoming message log entries came from
     * @return the port
     */
    int getSourcePort();

    /**
     * raw message log entries coming in from the listener
     * @return the log entry or entries
     */
    List<? extends TelemetryMessageLogEntry> getMessageList();

}
