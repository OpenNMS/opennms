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

import org.opennms.integration.api.v1.detectors.DetectRequest;
import org.opennms.integration.api.v1.detectors.ServiceDetector;
import org.opennms.integration.api.v1.detectors.ServiceDetectorFactory;
import org.opennms.netmgt.provision.DetectResults;

/**
 * This is a proxy object created to map {@link ServiceDetectorFactory} implementations to {@link org.opennms.netmgt.provision.ServiceDetectorFactory}
 */
public class ServiceDetectorFactoryImpl<T extends org.opennms.netmgt.provision.ServiceDetector> implements org.opennms.netmgt.provision.ServiceDetectorFactory<T> {

    private final ServiceDetectorFactory serviceDetectorFactory;

    public ServiceDetectorFactoryImpl(ServiceDetectorFactory serviceDetectorFactory) {
        this.serviceDetectorFactory = serviceDetectorFactory;
    }

    @Override
    public Class getDetectorClass() {
        return serviceDetectorFactory.getDetectorClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createDetector(Map<String, String> properties) {
        ServiceDetector serviceDetector = serviceDetectorFactory.createDetector(properties);
        return (T) new ServiceDetectorImpl(serviceDetector);
    }

    @Override
    public void afterDetect(org.opennms.netmgt.provision.DetectRequest request, DetectResults results, Integer nodeId) {
        //pass
    }

    @Override
    public org.opennms.netmgt.provision.DetectRequest buildRequest(String location, InetAddress address, Integer port, Map attributes) {
        DetectRequest detectRequest = serviceDetectorFactory.buildRequest(address, attributes);
        return new org.opennms.netmgt.provision.DetectRequest() {
            @Override
            public void preDetect() {
                // pass
            }

            @Override
            public InetAddress getAddress() {
                return detectRequest.getAddress();
            }

            @Override
            public Map<String, String> getRuntimeAttributes() {
                return detectRequest.getRuntimeAttributes();
            }

        };
    }
}
