/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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


package org.opennms.core.twin.publisher.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.twin.api.OnmsTwin;
import org.opennms.core.twin.api.OnmsTwinRequest;
import org.opennms.core.twin.publisher.api.OnmsTwinPublisher;
import org.opennms.core.twin.publisher.api.TwinBrokerOnOpennms;

public class OnmsTwinPublisherImpl implements OnmsTwinPublisher {

    private final TwinBrokerOnOpennms twinBrokerOnOpennms;
    // Publisher caches all objects from modules.
    private final Map<String, OnmsTwin> objects = new ConcurrentHashMap<>();
    private final Map<String, Callback> moduleCallbacks = new ConcurrentHashMap<>();

    public OnmsTwinPublisherImpl(TwinBrokerOnOpennms twinBrokerOnOpennms) {
        this.twinBrokerOnOpennms = twinBrokerOnOpennms;
        twinBrokerOnOpennms.register(new RpcHandler());
    }

    @Override
    public Callback register(OnmsTwin onmsTwin) {
        objects.put(onmsTwin.getKey(), onmsTwin);
        Callback callback = moduleCallbacks.get(onmsTwin.getKey());
        if(callback != null) {
            return callback;
        }
        return  new Callback() {
            @Override
            public void onUpdate(OnmsTwin updatedTwin) {
                // Update broker.
                twinBrokerOnOpennms.send(updatedTwin);
            }
        };
    }

    private class RpcHandler implements RpcReceiver {

        @Override
        public CompletableFuture<OnmsTwin> rpcCallback(OnmsTwinRequest request) {
            // TODO : May need to take in account of location ?
            OnmsTwin onmsTwin = objects.get(request.getKey());
            CompletableFuture<OnmsTwin> future = new CompletableFuture<>();
            future.complete(onmsTwin);
            return future;
        }
    }
}
