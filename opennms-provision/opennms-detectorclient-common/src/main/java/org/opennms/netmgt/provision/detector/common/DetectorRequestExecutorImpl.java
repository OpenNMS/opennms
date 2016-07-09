/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.common;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.provision.ServiceDetector;

import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;

public class DetectorRequestExecutorImpl implements DetectorRequestExecutor {

    private ServiceDetectorRegistry serviceDetectorRegistry;

    @Override
    public CompletableFuture<DetectorResponseDTO> execute(DetectorRequestDTO request) {
        String serviceName = request.getServiceName();
        String address = request.getAddress();
        Map<String, String> attributes = request.getAttributes();

        ServiceDetector detector = serviceDetectorRegistry.getDetectorByServiceName(serviceName, attributes);
        DetectorHandler detectorHandler = new DetectorHandler();
        final CompletableFuture<DetectorResponseDTO> output = detectorHandler.execute(detector, address);

        return output;
    }

    public void setServiceDetectorRegistry(ServiceDetectorRegistry serviceDetectorRegistry) {
        this.serviceDetectorRegistry = serviceDetectorRegistry;
    }

}
