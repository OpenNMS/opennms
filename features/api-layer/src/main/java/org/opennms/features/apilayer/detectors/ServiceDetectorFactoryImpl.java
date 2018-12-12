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
