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
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;

/**
 * This is a proxy object created to map {@link ServiceDetector} with {@link AsyncServiceDetector}
 * {@link ServiceDetector} from integration api is Async in nature.
 *
 */
public class ServiceDetectorImpl implements AsyncServiceDetector {

    private final ServiceDetector detector;

    public ServiceDetectorImpl(ServiceDetector detector) {
        this.detector = detector;
    }

    @Override
    public DetectFuture detect(org.opennms.netmgt.provision.DetectRequest request) {
        DetectRequest detectRequest = new DetectRequest() {
            @Override
            public InetAddress getAddress() {
                return request.getAddress();
            }

            @Override
            public Map<String, String> getRuntimeAttributes() {
                return request.getRuntimeAttributes();
            }
        };
        return new DetectorFutureImpl(detector.detect(detectRequest));
    }

    @Override
    public void init() {
        detector.init();
    }

    @Override
    public String getServiceName() {
        return detector.getServiceName();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public void setServiceName(String serviceName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public int getPort() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public void setPort(int port) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public int getTimeout() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public void setTimeout(int timeout) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public String getIpMatch() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported on {@link ServiceDetector}
     * throws {@link UnsupportedOperationException}
     */
    @Override
    public void setIpMatch(String ipMatch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        detector.dispose();
    }
}
