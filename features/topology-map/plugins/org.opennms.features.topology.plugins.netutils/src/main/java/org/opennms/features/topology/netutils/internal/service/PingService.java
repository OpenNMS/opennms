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

package org.opennms.features.topology.netutils.internal.service;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opennms.netmgt.icmp.Pinger;

/**
 * Helper class to execute ping operations using the {@link Pinger}.
 *
 * @author mvrueden
 */
public class PingService {

    public interface Callback {
        void onUpdate(PingResult result);
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final Pinger pinger;

    private Future<Void> currentFuture;

    public PingService(Pinger pinger) {
        this.pinger = Objects.requireNonNull(pinger);
    }

    public void cancel() {
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }
    }

    public void ping(PingRequest pingRequest, Callback uiCallback) {
        Objects.requireNonNull(pingRequest);
        Objects.requireNonNull(uiCallback);
        cancel();

        final PingServiceResponseCallback callback = new PingServiceResponseCallback(pingRequest, uiCallback);

        currentFuture = executor.submit(() -> {
            for (int sequenceId = 1; sequenceId <= pingRequest.getNumberRequests(); sequenceId++) {
                pinger.ping(
                        pingRequest.getInetAddress(),
                        pingRequest.getTimeout(),
                        pingRequest.getRetries(),
                        pingRequest.getPackageSize(),
                        sequenceId,
                        callback);
                Thread.sleep(pingRequest.getDelay()); // add a delay before executing the next ping
            }
            while(!callback.isDone()) {
                Thread.sleep(250);
            }
            callback.notifyUI();
            return null;
        });
    }
}
