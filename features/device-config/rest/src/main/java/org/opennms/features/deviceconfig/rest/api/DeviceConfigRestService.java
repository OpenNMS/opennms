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

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/device-config")
public interface DeviceConfigRestService {

    // paging, filter by ipaddressId, created time, device type
    // order by created time (asc, desc)

    /**
     * Gets a list of device configs.
     * @param limit used for paging; defaults to 10
     * @param offset used for paging; defaults to 0
     * @param orderBy used for paging; defaults to "version"
     * @param order used for paging; defaults to "desc"
     * @param ipInterfaceId database id of OnmsIpInterface instance
     * @param createdAfter epoche millis
     * @param createdBefore epoche millis
     * @return
     */
    @GET
    Response getDeviceConfigs(
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("orderBy") @DefaultValue("version") String orderBy,
            @QueryParam("order") @DefaultValue("desc") String order,

            @QueryParam("ipInterfaceId") Integer ipInterfaceId,
            @QueryParam("createdAfter") Long createdAfter,
            @QueryParam("createdBefore") Long createdBefore
    );

    @GET
    @Path("{id}")
    DeviceConfigDTO getDeviceConfig(@PathParam("id") long id);

    @DELETE
    @Path("{id}")
    void deleteDeviceConfig(@PathParam("id") long id);

}
