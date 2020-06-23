/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import io.searchbox.client.JestClient;

/**
 * {@link ElasticHealthCheck} that requires configuration to be present in order to actually verify the connection.
 * This is required as some features may be installed but the connection to Elasticsearch may not be configured yet,
 * meaning the <code>health:check</code> would always fail, which may not be the desired behaviour.
 *
 * @author mvrueden
 */
public class RequireConfigurationElasticHealthCheck extends ElasticHealthCheck {

    private final ConfigurationAdmin configAdmin;
    private final String pid;

    public RequireConfigurationElasticHealthCheck(final JestClient jestClient, final String featureName, final ConfigurationAdmin configAdmin, final String pid) {
        super(jestClient, featureName);
        this.configAdmin = Objects.requireNonNull(configAdmin);
        this.pid = Objects.requireNonNull(pid);
    }

    @Override
    public Response perform(Context context) {
        // If not configured, make it unknown
        try {
            final Configuration configuration = configAdmin.getConfiguration(pid);
            if(configuration.getProperties() == null) {
                return new Response(Status.Success, "Not configured");
            }
        } catch (IOException e) {
            return new Response(e);
        }

        // Connection to Elastic is configured, now perform the check
        return super.perform(context);
    }
}
