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

package org.opennms.core.twin.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.twin.publisher.api.OnmsTwinPublisher;
import org.opennms.core.twin.publisher.api.TwinBrokerOnOpennms;

public class MockTwinPublisher implements OnmsTwinPublisher {

    private TwinBrokerOnOpennms twinBrokerOnOpennms;
    private Map<String, OnmsTwin> objects = new ConcurrentHashMap<>();

    public MockTwinPublisher(TwinBrokerOnOpennms twinBrokerOnOpennms) {
        this.twinBrokerOnOpennms = twinBrokerOnOpennms;
    }

    public void init() {
        twinBrokerOnOpennms.register(new RpcReceiver() {
            @Override
            public CompletableFuture<OnmsTwin> rpcCallback(OnmsTwinRequest request) {
                OnmsTwin response = objects.get(request.getKey());
                CompletableFuture<OnmsTwin> future = new CompletableFuture<>();
                future.complete(response);
                return future;
            }
        });
    }

    @Override
    public Callback register(OnmsTwin onmsTwin) {
        objects.put(onmsTwin.getKey(), onmsTwin);
        return new Callback() {
            @Override
            public void onUpdate(OnmsTwin update) {
                twinBrokerOnOpennms.send(update);
            }
        };
    }
}
