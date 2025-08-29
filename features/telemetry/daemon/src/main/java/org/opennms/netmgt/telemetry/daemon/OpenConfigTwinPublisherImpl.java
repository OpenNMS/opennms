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


import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class OpenConfigTwinPublisherImpl implements OpenConfigTwinPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigTwinPublisherImpl.class);


    private final LocationPublisherManager locationPublisherManager;

    @Autowired
    public OpenConfigTwinPublisherImpl(LocationPublisherManager locationPublisherManager) {
        this.locationPublisherManager = locationPublisherManager;
    }

    @Override
    public void publishConfig(ServiceRef serviceRef, List<Map<String, String>> interpolatedMapList,String nodeConnectorKey) throws IOException {
        ConnectorTwinConfig.ConnectorConfig twinConfig = createTwinConfig(serviceRef,interpolatedMapList,nodeConnectorKey);
        LocationPublisher locationPublisher = locationPublisherManager.getOrCreate(serviceRef.getLocation());
        locationPublisher.addConfigAndPublish(twinConfig);
    }

    @Override
    public void removeConfig(ServiceRef serviceRef,String nodeConnectorKey) throws IOException {
        LocationPublisher locationPublisher = locationPublisherManager.getOrCreate(serviceRef.getLocation());
        locationPublisher.removeConfigAndPublish(nodeConnectorKey);
        locationPublisherManager.removeIfEmpty(serviceRef.getLocation());
    }

    @Override
    public void close() throws IOException {
        locationPublisherManager.forceCloseAll();
    }

    private ConnectorTwinConfig.ConnectorConfig createTwinConfig(ServiceRef serviceRef,List<Map<String, String>> parameters,String nodeConnectotrKey) {
        return new ConnectorTwinConfig.ConnectorConfig(
            serviceRef.getNodeId(),
            InetAddressUtils.str(serviceRef.getIpAddress()), nodeConnectotrKey,
            parameters
        );
    }
}