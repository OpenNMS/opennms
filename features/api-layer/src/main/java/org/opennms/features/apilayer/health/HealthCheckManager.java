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

package org.opennms.features.apilayer.health;

import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.integration.api.v1.health.HealthCheck;
import org.opennms.integration.api.v1.health.Response;
import org.opennms.integration.api.v1.health.Context;
import org.opennms.integration.api.v1.health.Status;
import org.osgi.framework.BundleContext;

public class HealthCheckManager extends InterfaceMapper<HealthCheck, org.opennms.core.health.api.HealthCheck> {

    public HealthCheckManager(BundleContext bundleContext) {
        super(org.opennms.core.health.api.HealthCheck.class, bundleContext);
    }

    @Override
    public org.opennms.core.health.api.HealthCheck map(HealthCheck healthCheck) {
        return new org.opennms.core.health.api.HealthCheck() {
            @Override
            public String getDescription() {
                return healthCheck.getDescription();
            }

            @Override
            public org.opennms.core.health.api.Response perform(org.opennms.core.health.api.Context context) throws Exception {
                final org.opennms.integration.api.v1.health.Response response = healthCheck.perform(new Context() {
                    @Override
                    public long getTimeout() {
                        return context.getTimeout();
                    }
                });
                return toResponse(response);
            }
        };
    }

    private static org.opennms.core.health.api.Response toResponse(Response response) {
        return new org.opennms.core.health.api.Response(toStatus(response.getStatus()), response.getMessage());
    }

    private static org.opennms.core.health.api.Status toStatus(Status status) {
        switch(status) {
            case Starting:
                return org.opennms.core.health.api.Status.Starting;
            case Success:
                return org.opennms.core.health.api.Status.Success;
            case Timeout:
                return org.opennms.core.health.api.Status.Timeout;
            case Failure:
                return org.opennms.core.health.api.Status.Failure;
            default:
                return org.opennms.core.health.api.Status.Unknown;
        }
    }

}
