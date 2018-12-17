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
import javax.ws.rs.core.MediaType;

import org.opennms.features.alarms.history.api.AlarmState;

@Path("alarm-history")
public interface AlarmHistoryRestService {

    @GET
    @Path("/states/by-alarm-id/{alarmId}")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<AlarmState> getStatesForAlarmWithDbId(@PathParam("alarmId") int alarmId);

    @GET
    @Path("/states/by-reduction-key/{reductionKey}")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<AlarmState> getStatesForAlarmWithReductionKey(@PathParam("reductionKey") String reductionKey);

    @GET
    @Path("/states/at/{timestampInMillis}")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<AlarmState> getActiveAlarmsAt(@PathParam("timestampInMillis") long timestampInMillis);

    @GET
    @Path("/states")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<AlarmState> getActiveAlarmsNow();

}
