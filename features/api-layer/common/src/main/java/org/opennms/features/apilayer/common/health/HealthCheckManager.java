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
package org.opennms.features.apilayer.common.health;

import static org.opennms.core.health.api.HealthCheckConstants.LOCAL;

import java.util.Arrays;
import java.util.List;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.integration.api.v1.health.Context;
import org.opennms.integration.api.v1.health.HealthCheck;
import org.opennms.integration.api.v1.health.Response;
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
            public List<String> getTags() {
                return Arrays.asList(LOCAL);
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
