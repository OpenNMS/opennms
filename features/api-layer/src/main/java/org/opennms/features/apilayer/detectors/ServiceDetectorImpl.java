/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.detectors;

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
