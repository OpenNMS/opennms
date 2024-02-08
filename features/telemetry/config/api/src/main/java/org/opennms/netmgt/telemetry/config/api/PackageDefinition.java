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
package org.opennms.netmgt.telemetry.config.api;

import java.util.Map;

/**
 * Telemetry protocol package configuration.
 */
public interface PackageDefinition {

    /**
     * The filter rule is used to match which sources should belong to this package.
     *
     * If the rule is <code>null</code>, then all sources should match.
     *
     * @return the filter rule
     */
    String getFilterRule();

    /**
     * The RRD settings are use to control the control of RRD files, when applicable.
     *
     * @return the rrd settings
     */
    RrdDefinition getRrd();

    /**
     * Package specific parameters.
     *
     * @return the parameter map
     */
    Map<String, String> getParameterMap();
}
