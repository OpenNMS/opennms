/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries;

/**
 * The Timeseries Integration Layer allows for easy integration of 3rd party timeseries databases in OpenNMS.
 * In order to add a new timeseries database you need to implement {@link org.opennms.integration.api.v1.timeseries.TimeSeriesStorage}.
 * See https://docs.opennms.org/opennms/releases/26.1.1/guide-admin/guide-admin.html#ga-opennms-operation-timeseries for details.
 *
 * We deal with different types data:
 * - Metric: Identifies a unique timeseries. For details, see {@link org.opennms.integration.api.v1.timeseries.Metric}
 * - Sample: Describes a single value at a given point in time for a Metric, see @{@link org.opennms.integration.api.v1.timeseries.Sample
 *           It is stored in the timeseries database
 * - meta data tags: additional data that can be exported to the timeseries database as meta tags in the Metric. Configured via opennms.properties,
 *           see {@link org.opennms.netmgt.timeseries.samplewrite.MetaTagDataLoader}
 */
