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

package org.opennms.core.health.api;

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
     * The name of the {@link HealthCheck}, e.g. "opennms-rest-api".
     * This is used when ignoring certain health checks.
     *
     * @return The string, describing the name of the check.
     */
    String getName();

    /**
     * The external dependency check of the {@link HealthCheck}, e.g. "rely on external data".
     * This is used when determining if the health check is self contained or requires an external component.
     *
     * @return The boolean, indicating if the check is local only or not.
     */
    boolean isLocalCheck();

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
