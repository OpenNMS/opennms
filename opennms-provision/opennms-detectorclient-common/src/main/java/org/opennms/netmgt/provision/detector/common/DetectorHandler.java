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

public class DetectorHandler {

    private Executor executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<DetectorResponseDTO> execute(ServiceDetector detector, String address) {

        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        final CompletableFuture<DetectorResponseDTO> output = new CompletableFuture<DetectorResponseDTO>();
        DetectorResponseDTO response = new DetectorResponseDTO();

        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            response.setDetected(false);
            response.setFailureMesage(e.getMessage());
        }
        boolean isServiceDetected = false;
        future = detectService(detector, ipAddress);
        while (true) {
            try {
                isServiceDetected = future.get(1, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException | InterruptedException
                    | ExecutionException e1) {
                response.setDetected(false);
                response.setFailureMesage(e1.getMessage());
            }
        }

        response.setDetected(isServiceDetected);
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
            // TODO: We should update the AsyncServiceDetector interface to
            // return a CompletableFuture instead of a DetectFuture.
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

}
