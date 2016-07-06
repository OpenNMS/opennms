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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class DetectorRequestExecutorLocalImpl
        implements DetectorRequestExecutor {

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public CompletableFuture<DetectorResponseDTO> execute(
            DetectorRequestDTO request) {

        String serviceName = request.getServiceName();
        String address = request.getAddress();
        List<String> properties = request.getProperties();
        Map<String, String> attributes = parse(properties);

        ServiceDetectorFactoryProvider serviceProvider = new ServiceDetectorFactoryProvider();
        ServiceDetector detector = serviceProvider.getDetector(serviceName);

        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(detector);
        wrapper.setPropertyValues(attributes);

        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e1) {
            System.out.print(" Invalid IP Address");
        }
        boolean isServiceDetected = false;
        final CompletableFuture<Boolean> future = detectService(detector,
                                                                ipAddress);
        while (true) {
            try {
                isServiceDetected = future.get(1, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException | InterruptedException
                    | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }

        DetectorResponseDTO response = new DetectorResponseDTO();
        response.setDetected(isServiceDetected);
        final CompletableFuture<DetectorResponseDTO> output = new CompletableFuture<DetectorResponseDTO>();
        output.complete(response);

        return output;
    }

    private CompletableFuture<Boolean> detectService(ServiceDetector detector,
            InetAddress address) {

        detector.init();
        if (detector instanceof SyncServiceDetector) {
            final SyncServiceDetector syncDetector = (SyncServiceDetector) detector;
            return CompletableFuture.supplyAsync(new Supplier<Boolean>() {
                @Override
                public Boolean get() {
                    try {
                        return syncDetector.isServiceDetected(address);
                    } finally {
                        syncDetector.dispose();
                    }
                }
            }, executor);
        } else if (detector instanceof AsyncServiceDetector) {
            final AsyncServiceDetector asyncDetector = (AsyncServiceDetector) detector;

            return CompletableFuture.supplyAsync(new Supplier<Boolean>() {
                @Override
                public Boolean get() {
                    DetectFuture future = asyncDetector.isServiceDetected(address);
                    try {
                        future.awaitFor();
                        return future.isServiceDetected();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        asyncDetector.dispose();
                    }
                }
            }, executor);
        } else {
            throw new IllegalArgumentException("Unsupported detector type.");
        }
    }

    private Map<String, String> parse(List<String> attributeList) {
        Map<String, String> properties = new HashMap<>();
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property "
                            + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1,
                                                      keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
}
