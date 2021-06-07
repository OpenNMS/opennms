/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
package org.opennms.features.jest.client;

import java.io.IOException;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

import com.google.common.base.Strings;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Ping;

/**
 * Verifies the connection to ElasticSearch.
 * The health check may be located in an odd place for now.
 * The reason for this is, that multiple Modules create their own clients.
 * In order to not configure the client for the health check module as well, this {@link HealthCheck} is
 * only validating if the connection to ElasticSearch from the view of the flows/elastic bundle is working.
 *
 * @author mvrueden
 */
public class ElasticHealthCheck implements HealthCheck {

    private final JestClient client;

    private final String featureName;

    public ElasticHealthCheck(JestClient jestClient, String featureName) {
        this.client = Objects.requireNonNull(jestClient);
        this.featureName = Objects.requireNonNull(featureName);
    }

    @Override
    public String getDescription() {
        return "Connecting to ElasticSearch ReST API (" + featureName + ")";
    }

    @Override
    public Response perform(Context context) {
        final Ping ping = new Ping.Builder().build();
        try {
            final JestResult result = client.execute(ping);
            if (result.isSucceeded() && Strings.isNullOrEmpty(result.getErrorMessage())) {
                return new Response(Status.Success);
            } else {
                return new Response(Status.Failure, Strings.isNullOrEmpty(result.getErrorMessage()) ? null : result.getErrorMessage());
            }
        } catch (IOException e) {
            return new Response(e);
        }
    }
}
