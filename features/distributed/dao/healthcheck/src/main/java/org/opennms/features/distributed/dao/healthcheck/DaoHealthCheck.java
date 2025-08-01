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
package org.opennms.features.distributed.dao.healthcheck;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static org.opennms.core.health.api.HealthCheckConstants.DAO;
import static org.opennms.core.health.api.HealthCheckConstants.LOCAL;

/**
 * Verifies that at least the NodeDao can be consumed as a OSGi service, otherwise
 * it is considered a Failure.
 * This is necessary to ensure that in case of running the DAOs inside sentinel they
 * were exposed through the spring extender as the bundle may be ACTIVE but no spring services were
 * exposed.
 *
 * @author mvrueden
 */
public class DaoHealthCheck implements HealthCheck {

    private BundleContext bundleContext;

    public DaoHealthCheck(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getDescription() {
        return "Retrieving NodeDao";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(LOCAL, DAO);
    }

    @Override
    public Response perform(Context context) {
        // IMPORTANT: Do not change to Class reference here. This is a string on purpose to not have the maven bundle
        // plugin put a IMPORT-Package statement for org.opennms.netmgt.dao.api. Otherwise this Health Check is never
        // loaded and will never be invoked, thus can not fullfil its purpose.
        final ServiceReference serviceReference = bundleContext.getServiceReference("org.opennms.netmgt.dao.api.NodeDao");
        if (serviceReference != null) {
            return new Response(Status.Success);
        } else {
            return new Response(Status.Failure, "No NodeDao available");
        }
    }
}
