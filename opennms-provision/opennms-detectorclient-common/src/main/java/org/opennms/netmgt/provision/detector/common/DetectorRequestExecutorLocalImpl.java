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

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public class DetectorRequestExecutorLocalImpl implements DetectorRequestExecutor {

    @Autowired
    private ServiceDetectorRegistry serviceDetectorRegistry;

    // TODO: Use a better threading strategy for sync detectors.
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public CompletableFuture<DetectorResponseDTO> execute(DetectorRequestDTO request) {
        String className = request.getClassName();
        InetAddress address = request.getAddress();
        Map<String, String> attributes = request.getAttributeMap();
        ServiceDetector detector = serviceDetectorRegistry.getDetectorByClassName(className, attributes);
        if (detector == null) {
            throw new IllegalArgumentException("No detector found with class name '" + className + "'.");
        }
        return detectService(detector, address);
    }

    private CompletableFuture<DetectorResponseDTO> detectService(ServiceDetector detector, InetAddress address) {
        detector.init();
        if (detector instanceof SyncServiceDetector) {
            final SyncServiceDetector syncDetector = (SyncServiceDetector) detector;
            return CompletableFuture.supplyAsync(new Supplier<DetectorResponseDTO>() {
                @Override
                public DetectorResponseDTO get() {
                    DetectorResponseDTO responseDTO = new DetectorResponseDTO();
                    try {
                        responseDTO.setDetected(syncDetector.isServiceDetected(address));
                    } catch (Throwable t) {
                        responseDTO.setDetected(false);
                        responseDTO.setFailureMesage(t.getMessage());
                    } finally {
                        syncDetector.dispose();
                    }
                    return responseDTO;
                }
            }, executor);
        } else if (detector instanceof AsyncServiceDetector) {
            final AsyncServiceDetector asyncDetector = (AsyncServiceDetector) detector;
            // TODO: HZN-839: We should update the AsyncServiceDetector interface to
            // return a CompletableFuture instead of a DetectFuture.
            return CompletableFuture.supplyAsync(new Supplier<DetectorResponseDTO>() {
                @Override
                public DetectorResponseDTO get() {
                    DetectorResponseDTO responseDTO = new DetectorResponseDTO();
                    try {
                        DetectFuture future = asyncDetector.isServiceDetected(address);
                        future.awaitFor();
                        responseDTO.setDetected(future.isServiceDetected());
                    } catch (Throwable t) {
                        responseDTO.setDetected(false);
                        responseDTO.setFailureMesage(t.getMessage());
                    } finally {
                        asyncDetector.dispose();
                    }
                    return responseDTO;
                }
            }, executor);
        } else {
            throw new IllegalArgumentException("Unsupported detector type.");
        }
    }

    public void setServiceDetectorRegistry(ServiceDetectorRegistry serviceDetectorRegistry) {
        this.serviceDetectorRegistry = serviceDetectorRegistry;
    }
}
