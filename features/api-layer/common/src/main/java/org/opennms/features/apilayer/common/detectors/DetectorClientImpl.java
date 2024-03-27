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
package org.opennms.features.apilayer.common.detectors;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.integration.api.v1.detectors.DetectorClient;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;

/**
 *  Implements {@link DetectorClient} for OpenNMS instance.
 */
public class DetectorClientImpl implements DetectorClient {

    private LocationAwareDetectorClient locationAwareDetectorClient;

    public DetectorClientImpl(LocationAwareDetectorClient locationAwareDetectorClient) {
        this.locationAwareDetectorClient = locationAwareDetectorClient;
    }

    @Override
    public CompletableFuture<Boolean> detect(String serviceName, String hostName, Map<String, String> detectorAttributes) {
        return detect(serviceName, hostName, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, detectorAttributes);
    }

    @Override
    public CompletableFuture<Boolean> detect(String serviceName, String hostName, String location, Map<String, String> detectorAttributes) {
        CompletableFuture<Boolean> detected = new CompletableFuture<>();
        try {
            return locationAwareDetectorClient.detect()
                    .withServiceName(serviceName)
                    .withLocation(location)
                    .withAddress(InetAddress.getByName(hostName))
                    .withAttributes(detectorAttributes)
                    .execute();
        } catch (Exception e) {
            detected.completeExceptionally(e);
            return detected;
        }
    }
}
