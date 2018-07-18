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

package org.opennms.netmgt.flows.elastic.health;

import java.io.IOException;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Ping;

/**
 * Verifies the connection to ElasticSearch.
 * The health check may be located in an odd place for now.
 * The reason for this is, that multiple Modules create their own clients.
 * In order to not configure the client for the health check module as well, this healthcheck is
 * only validating if ElasticSearch from the view of the flows/elastic bundle is working.
 */
public class ElasticHealthCheck implements HealthCheck {

    private BundleContext bundleContext;

    public ElasticHealthCheck(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getDescription() {
        return "Connecting to ElasticSearch ReST API (Flows)";
    }

    @Override
    public Response perform(Context context) {
        final ServiceReference<JestClient> serviceReference = bundleContext.getServiceReference(JestClient.class);
        if (serviceReference != null) {
                final JestClient client = bundleContext.getService(serviceReference);
                final Ping ping = new Ping.Builder().build();
            try {
                final JestResult result = client.execute(ping);
                if (result.isSucceeded() && result.getErrorMessage() != null) {
                    return new Response(Status.Success);
                } else {
                    return new Response(Status.Failure, result.getErrorMessage() != null ? result.getErrorMessage() : "");
                }
            } catch (IOException e) {
                return new Response(e);
            }
        } else {
            return new Response(Status.Failure, "No service of type " + JestClient.class.getName() + " available");
        }
    }
}

