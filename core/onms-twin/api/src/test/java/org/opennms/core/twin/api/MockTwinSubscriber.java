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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.twin.subscriber.api.TwinBrokerOnMinion;
import org.opennms.core.twin.subscriber.api.OnmsTwinSubscriber;
import org.opennms.core.twin.subscriber.api.TwinSubscriberModule;

public class MockTwinSubscriber implements OnmsTwinSubscriber {

    private TwinBrokerOnMinion twinBrokerOnMinion;
    private Map<String, TwinSubscriberModule<?>> modules = new ConcurrentHashMap<>();
    private Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    public void init() {
        twinBrokerOnMinion.registerSinkUpdate(this);
    }

    @Override
    public void subscribe(OnmsTwin onmsTwin) {
        TwinSubscriberModule<?> module = modules.get(onmsTwin.getKey());
        unmarshalResponse(onmsTwin, module);
    }

    public MockTwinSubscriber(TwinBrokerOnMinion twinBrokerOnMinion) {
        this.twinBrokerOnMinion = twinBrokerOnMinion;
    }

    @Override
    public <T> CompletableFuture<T> getObject(String key, TwinSubscriberModule<T> module) {
        modules.put(key, module);
        OnmsTwinRequest request = new OnmsTwinRequest() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getLocation() {
                return "MINION";
            }

            @Override
            public int getTTL() {
                return 30;
            }

        };
        CompletableFuture<T> response = new CompletableFuture<>();
        CompletableFuture<OnmsTwin> future = twinBrokerOnMinion.sendRpcRequest(request);
        future.whenComplete((res, ex) -> {
            if(res != null) {
                T value = unmarshalResponse(res);
                response.complete(value);
            }
        });
        return response;
    }

    <T> T unmarshalResponse(OnmsTwin onmsTwin) {
       Class<?> clazz = classMap.get(onmsTwin.getKey());
       return (T) new String(onmsTwin.getObjectValue(), StandardCharsets.UTF_8);
    }

    <T> void unmarshalResponse(OnmsTwin onmsTwin, TwinSubscriberModule<T> module) {
        if (module != null) {
            T response = unmarshalResponse(onmsTwin);
            module.update(response);
        }
    }

}
