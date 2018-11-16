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

import org.opennms.integration.api.v1.detectors.ServiceDetector;
import org.opennms.integration.api.v1.detectors.ServiceDetectorFactory;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;

/**
 *  Maps {@link ServiceDetectorFactory} implementations to {@link org.opennms.netmgt.provision.ServiceDetectorFactory}
 */
public class ServiceDetectorFactoryImpl implements org.opennms.netmgt.provision.ServiceDetectorFactory {

    private final ServiceDetectorFactory serviceDetectorFactory;

    public ServiceDetectorFactoryImpl(ServiceDetectorFactory serviceDetectorFactory) {
        this.serviceDetectorFactory = serviceDetectorFactory;
    }

    @Override
    public Class getDetectorClass() {
        return serviceDetectorFactory.getDetectorClass();
    }

    @Override
    public org.opennms.netmgt.provision.ServiceDetector createDetector() {
        ServiceDetector serviceDetector = serviceDetectorFactory.createDetector();
        return new ServiceDetectorImpl(serviceDetector);
    }

    @Override
    public void afterDetect(DetectRequest request, DetectResults results, Integer nodeId) {
        // pass
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map attributes) {
        return new DetectRequest() {
            @Override
            public InetAddress getAddress() {
                return address;
            }

            @Override
            public Map<String, String> getRuntimeAttributes() {
                return attributes;
            }
        };
    }
}
