/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.distributed.core.impl;

import org.opennms.core.health.api.Context;
import org.opennms.distributed.core.api.RestClient;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

/**
 * Verifies the connection to the OpenNMS ReST API.
 *
 * @author mvrueden
 */
public class RestConnectionHealthCheck implements HealthCheck {

    private final RestClient restClient;

    public RestConnectionHealthCheck(final RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Response perform(Context context) throws Exception {
        restClient.ping();
        return new Response(Status.Success);
    }

    @Override
    public String getDescription() {
        return "Connecting to OpenNMS ReST API";
    }
}
