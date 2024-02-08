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
