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
package org.opennms.netmgt.poller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.poller.pollables.PollableService;

/**
 * <p>QueryManager interface.</p>
 *
 * @author brozow
 */
public interface QueryManager {

    /**
     * <p>getNodeLabel</p>
     *
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     * @return a {@link java.lang.String} object.
     */
    String getNodeLabel(int nodeId) throws SQLException;

    String getNodeLocation(int nodeId);

    /**
     * Creates a new outage for the given service without setting
     * the lost event id.
     */
    Integer openOutagePendingLostEventId(int nodeId, String ipAddr, String svcName, Date lostTime);

    /**
     * Set or updates the lost event id on the specified outage.
     */
    void updateOpenOutageWithEventId(int outageId, long lostEventId);

    /**
     * Marks the outage for the given service as resolved
     * with the given time and returns the id of this outage.
     *
     * If no outages are currently open, then no action is take
     * and the function returns null.
     */
    Integer resolveOutagePendingRegainEventId(int nodeId, String ipAddr, String svcName, Date regainedTime);

    /**
     * Set or updates the regained event id on the specified outage.
     */
    void updateResolvedOutageWithEventId(int outageId, long regainedEventId);

    /**
     * @param nodeId
     * @return
     */
    List<String[]> getNodeServices(int nodeId);

    void closeOutagesForUnmanagedServices();

    void closeOutagesForNode(Date closeDate, long eventId, int nodeId);

    void closeOutagesForInterface(Date closeDate, long eventId, int nodeId, String ipAddr);

    void closeOutagesForService(Date closeDate, long eventId, int nodeId, String ipAddr, String serviceName);

    void updateServiceStatus(int nodeId, String ipAddr, String serviceName, String status);

    void updateLastGoodOrFail(PollableService service, PollStatus status);

}
