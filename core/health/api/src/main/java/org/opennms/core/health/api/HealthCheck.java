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
package org.opennms.core.health.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface to define a {@link HealthCheck}.
 *
 * @author mvrueden
 * @see Health, {@link HealthCheckService}
 */
public interface HealthCheck {

    /**
     * The description of the {@link HealthCheck}, e.g. "Connecting to OpenNMS ReST API".
     * This is used when visualizing the progress or result of the checks.
     *
     * @return The string, describing the check.
     */
    String getDescription();

    /**
     *
     * A tag to indicate the category of the health-check.
     *
     * @return The HealthTag type, describing category of the health-check.
     */
    List<String> getTags();

    /**
     * Implements the check itself, e.g. Connecting to a HTTP Endpoint.
     *
     * As the method is called by the {@link HealthCheckService}, it is advised that all timeout restrictions
     * etc are handled by the service instead of the {@link HealthCheck} implementation.
     *
     * Implementations might throw an Exception, which should be handled by the {@link HealthCheckService} as well.
     *
     * The response indicates if the check was successful, or encountered other problems. If null is returned,
     * the {@link HealthCheckService} should consider this as {@link Status#Unknown}.
     *
     * @return The response indicating the Success/Failure/Timeout/etc of the check
     * @throws Exception In case of an error
     * @see HealthCheckService
     * @param context
     */
    Response perform(Context context) throws Exception;
}
