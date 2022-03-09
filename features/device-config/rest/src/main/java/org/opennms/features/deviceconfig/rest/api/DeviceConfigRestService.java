/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.rest.api;

import org.opennms.features.deviceconfig.rest.BackupRequestDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/device-config")
public interface DeviceConfigRestService {
    public static final String DEVICE_CONFIG_SERVICE_PREFIX = "DeviceConfig";

    // paging, filter by ipaddressId, last updated time, config created time, last succeeded, last failed, config type
    // order by last updated time (asc, desc)

    /**
     * Gets a list of device configs along with backup schedule information.
     * @param limit used for paging; defaults to 10
     * @param offset used for paging; defaults to 0
     * @param orderBy used for paging; defaults to "lastUpdated"
     * @param order used for paging; defaults to "desc"
     * @param deviceName filter results by device name
     * @param ipAddress filter results by device IP address
     * @param ipInterfaceId database id of OnmsIpInterface instance
     * @param configType Configuration type, 'default' or 'running'
     * @param createdAfter If set, only return items with saved backup after this date in epoch millis
     * @param createdBefore If set, only return items with saved backup before this date in epoch millis
     * @return Json response containing a list of device configs in the
     *      shape of {@link org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO }
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getDeviceConfigs(
        @QueryParam("limit") @DefaultValue("10") Integer limit,
        @QueryParam("offset") @DefaultValue("0") Integer offset,
        @QueryParam("orderBy") @DefaultValue("lastUpdated") String orderBy,
        @QueryParam("order") @DefaultValue("desc") String order,
        @QueryParam("deviceName") String deviceName,
        @QueryParam("ipAddress") String ipAddress,
        @QueryParam("ipInterfaceId") Integer ipInterfaceId,
        @QueryParam("configType") String configType,
        @QueryParam("createdAfter") Long createdAfter,
        @QueryParam("createdBefore") Long createdBefore
    );

    @GET
    @Path("{id}")
    DeviceConfigDTO getDeviceConfig(@PathParam("id") long id);

    @DELETE
    @Path("{id}")
    void deleteDeviceConfig(@PathParam("id") long id);

    /**
     * Download the most recent backup configuration for a single device.
     */
    @GET
    @Path("/download/{id}")
    Response downloadDeviceConfig(@PathParam("id") long id);

    /**
     * Download a zip file containing the most recent backup configurations for multiple devices.
     * POST will be Json
     */
    @POST
    @Path("/download")
    Response downloadDeviceConfigs();


    @POST
    @Path("/backup")
    @Consumes({MediaType.APPLICATION_JSON})
    Response triggerDeviceConfigBackup(BackupRequestDTO backupRequestDTO);
}
