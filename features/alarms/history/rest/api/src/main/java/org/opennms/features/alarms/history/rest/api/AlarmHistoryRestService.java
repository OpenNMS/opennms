/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
