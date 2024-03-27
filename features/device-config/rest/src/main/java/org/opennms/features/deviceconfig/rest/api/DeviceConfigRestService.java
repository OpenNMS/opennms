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
package org.opennms.features.deviceconfig.rest.api;

import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
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
import java.util.List;
import java.util.Set;

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
     *      shape of {@link DeviceConfigDTO }
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
     * @param pageEnter An optional flag as to whether the user is entering the DCB page. Used for usage analytics.
     *                  Parameter is added here since this API call is made when entering the DCB page.
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
        @QueryParam("status") Set<DeviceConfigStatus> statuses,
        @QueryParam("pageEnter") @DefaultValue("false") boolean pageEnter
    );

    /**
     * Get a list of device configs for a given IP interface id.
     * This is a history of configs for a particular device.
     * Returns all config types by default, use 'configType' to filter.
     */
    @GET
    @Path("/interface/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getDeviceConfigsByInterface(
        @PathParam("id") Integer ipInterfaceId,
        @QueryParam("configType") @DefaultValue("") String configType);

    /**
     * Delete multiple device config.
     * @param ids comma separated list of ids (Long)
     */
    @DELETE
    Response deleteDeviceConfigs(@QueryParam("id") List<Long> ids);

    /**
     * Delete a single device config.
     * @param id
     */
    @DELETE
    @Path("{id : \\d+}")
    Response deleteDeviceConfig(@PathParam("id") long id);


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
