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

import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.rest.BackupRequestDTO;

import java.util.List;
import java.util.Set;
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
    /**
     * Get device config info for a single item, by DeviceConfig id.
     * @param id database id of device config
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id : \\d+}")
    Response getDeviceConfig(@PathParam("id") long id);

    /**
     * Gets a list of device configs along with backup schedule information.
     *
     * @param limit used for paging; defaults to 10
     * @param offset used for paging; defaults to 0
     * @param orderBy used for sorting. Valid values are 'lastUpdated', 'deviceName', 'lastBackup' and 'ipAddress'. Defaults to 'lastUpdated'
     * @param order used for sorting; valid values are 'asc' and 'desc', defaults to 'desc'
     * @param deviceName filter results by device name. Should use 'searchTerm' instead.
     * @param ipAddress filter results by device IP address. Should use 'searchTerm' instead.
     * @param ipInterfaceId database id of OnmsIpInterface instance. This will retrieve a record history.
     * @param configType Configuration type, typically 'default' or 'running'
     * @param searchTerm A search term, currently to search by device name or IP address.
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
        @QueryParam("status") Set<DeviceConfigStatus> statuses,
        @QueryParam("createdAfter") Long createdAfter,
        @QueryParam("createdBefore") Long createdBefore
    );

    /**
     * Gets the most recent device config for each device / config type combination.
     * Typically called by UI to get latest status.
     *
     * @param limit used for paging; defaults to 10
     * @param offset used for paging; defaults to 0
     * @param orderBy used for sorting. Valid values are 'lastUpdated', 'deviceName', 'lastBackup' and 'ipAddress'. Defaults to 'lastUpdated'
     * @param order used for sorting; valid values are 'asc' and 'desc', defaults to 'desc'
     * @param searchTerm A search term, currently to search by device name or IP address.
     * @param statuses An optional set of {@link DeviceConfigStatus} values. If supplied, only return records
     *      with any of the given statuses; defaults to returning all values.
     */
    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    Response getLatestDeviceConfigsForDeviceAndConfigType(
        @QueryParam("limit") @DefaultValue("10") Integer limit,
        @QueryParam("offset") @DefaultValue("0") Integer offset,
        @QueryParam("orderBy") @DefaultValue("lastUpdated") String orderBy,
        @QueryParam("order") @DefaultValue("desc") String order,
        @QueryParam("search") String searchTerm,
        @QueryParam("status") Set<DeviceConfigStatus> statuses
    );

    /**
     * Get a list of device configs for a given IP interface id.
     * This is a history of configs for a particular device.
     * Returns all config types.
     */
    @GET
    @Path("/interface/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getDeviceConfigsByInterface(@PathParam("id") Integer ipInterfaceId);

    /**
     * Delete a single device config.
     * @param id
     */
    @DELETE
    @Path("{id : \\d+}")
    void deleteDeviceConfig(@PathParam("id") long id);

    /**
     * Download configurations for the given id or comma-separated list of ids.
     * Single configurations will be returned as a single file.
     * Multiple configurations will be returned inside a .tgz file.
     */
    @GET
    @Path("/download")
    Response downloadDeviceConfig(@QueryParam("id") @DefaultValue("") String id);

    @POST
    @Path("/backup")
    @Consumes({MediaType.APPLICATION_JSON})
    Response triggerDeviceConfigBackup(List<BackupRequestDTO> backupRequestDtoList);
}
