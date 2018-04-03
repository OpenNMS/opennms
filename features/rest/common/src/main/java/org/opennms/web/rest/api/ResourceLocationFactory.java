/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.api;

/**
 * Factory to create {@link ResourceLocation} objects for certain REST endpoints.
 */
public class ResourceLocationFactory {

    public static ResourceLocation createIpServiceLocation(String id) {
        return new ResourceLocation(ApiVersion.Version1, "ifservices", id);
    }

    public static ResourceLocation createBusinessServiceLocation(String id) {
        return new ResourceLocation(ApiVersion.Version2, "business-services", id);
    }

    public static ResourceLocation createBusinessServiceLocation() {
        return new ResourceLocation(ApiVersion.Version2, "business-services");
    }

    public static ResourceLocation createBusinessServiceIpServiceLocation(int ipServiceId) {
        return new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", String.valueOf(ipServiceId));
    }

    public static ResourceLocation createBusinessServiceEdgeLocation(long bsId, long edgeId) {
        return new ResourceLocation(ApiVersion.Version2, "business-services", String.valueOf(bsId), "edges", String.valueOf(edgeId));
    }
}
