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
package org.opennms.netmgt.telemetry.daemon;

import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface OpenConfigTwinPublisher {
    void publishConfig(ServiceRef serviceRef, List<Map<String, String>> interpolatedMapList, String nodeConnectorKey, String queueName) throws IOException;
    void removeConfig(ServiceRef serviceRef,String nodeConnectorKey) throws IOException;

    /**
     * Applies all additions and removals for one location and publishes the result once.
     */
    void publishConfigs(String location, List<ConnectorTwinConfig.ConnectorConfig> addedConfigs,
                        Collection<String> removedConnectorKeys, String queueName) throws IOException;

    void close() throws IOException;
}