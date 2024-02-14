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
package org.opennms.features.alarms.history.rest.api;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.opennms.features.alarms.history.api.AlarmState;

/**
 * Query the alarm history and state changes stored in
 * the {@link org.opennms.features.alarms.history.api.AlarmHistoryRepository}.
 */
@Path("alarms/history")
public interface AlarmHistoryRestService {

    /**
     * Retrieve the complete set of state changes for the given alarm.
     *
     * @param alarmId alarm id to query
     * @param matchType when set the 'reduction-key', lookup by reduction key instead of alarm id
     * @return state changes
     */
    @GET
    @Path("{alarmId}/states")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<AlarmState> getStatesForAlarm(@PathParam("alarmId") String alarmId,
            @QueryParam("matchType") String matchType);

    /**
     * Retrieve the last known state of an alarm at the given time.
     *
     * @param alarmId alarm id to query
     * @param matchType when set the 'reduction-key', lookup by reduction key instead of alarm id
     * @param timestampInMillis timestamp in milliseconds - defaults to "now" when null
     * @return last known state
     */
    @GET
    @Path("{alarmId}")
    @Produces(MediaType.APPLICATION_JSON)
    AlarmState getAlarm(@PathParam("alarmId") String alarmId,
            @QueryParam("matchType") String matchType,
            @QueryParam("at") Long timestampInMillis);

    /**
     * Retrieve the last known state of all alarms which were active at the given time.
     *
     * @param timestampInMillis timestamp in milliseconds
     * @return last known state of all alarms which were active at the given time - defaults to "now" when null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Collection<AlarmState> getActiveAlarmsAt(@QueryParam("at") Long timestampInMillis);

}
