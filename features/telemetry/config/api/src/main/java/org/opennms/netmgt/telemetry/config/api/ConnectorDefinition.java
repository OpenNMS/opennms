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

import java.util.List;

/**
 * Telemetry connector definition.
 */
public interface ConnectorDefinition extends TelemetryBeanDefinition {

    /**
     * The name of the connect.
     *
     * @return the protocol name
     */
    @Override
    String getName();

    /**
     * The name of the queue the connector "writes" to.
     *
     * @return The name of the queue the parser "writes" to. Must not be null.
     */
    String getQueueName();

    /**
     * The name of the IP-service associated with this connector definition
     *
     * @return the name of the service
     */
    String getServiceName();

    /**
     * Packages may contain settings for specific sources.
     *
     * @return the list of configured packages
     */
    List<? extends PackageDefinition> getPackages();

}
