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

package org.opennms.core.twin.subscriber.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.twin.api.OnmsTwin;
import org.opennms.core.twin.api.OnmsTwinRequest;
import org.opennms.core.twin.subscriber.api.OnmsTwinSubscriber;
import org.opennms.core.twin.subscriber.api.TwinBrokerOnMinion;
import org.opennms.core.twin.subscriber.api.TwinSubscriberModule;
import org.opennms.distributed.core.api.MinionIdentity;

public class OnmsTwinSubscriberImpl implements OnmsTwinSubscriber {

    private final TwinBrokerOnMinion twinBrokerOnMinion;
    private final MinionIdentity minionIdentity;
    private final Map<String, TwinSubscriberModule<?>> modules = new ConcurrentHashMap<>();

    public OnmsTwinSubscriberImpl(TwinBrokerOnMinion twinBrokerOnMinion, MinionIdentity minionIdentity) {
        this.twinBrokerOnMinion = twinBrokerOnMinion;
        this.minionIdentity = minionIdentity;
        twinBrokerOnMinion.registerSinkUpdate(this);
    }

    @Override
    public <T> CompletableFuture<T> getObject(String key, TwinSubscriberModule<T> twinSubscriberModule) {
        modules.put(key, twinSubscriberModule);
        CompletableFuture<OnmsTwin> future = twinBrokerOnMinion.sendRpcRequest(new OnmsTwinRequest() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getLocation() {
                return minionIdentity.getLocation();
            }

            @Override
            public int getTTL() {
                return 0;
            }
        });
        CompletableFuture<T> response = new CompletableFuture<>();
        future.whenComplete((res, ex) -> {
            if(res != null) {
                T obj = unmarshalResponse(res);
                response.complete(obj);
            }
        });
        return response;
    }

    public void subscribe(OnmsTwin onmsTwin) {
            TwinSubscriberModule<?> module = modules.get(onmsTwin.getKey());
            if (module != null) {
                module.update(unmarshalResponse(onmsTwin));
            }
    }


    <T> T unmarshalResponse(OnmsTwin onmsTwin) {
        TwinSubscriberModule<?> module = modules.get(onmsTwin.getKey());
        // Unmarshal with specific class and return
        return null;
    }

}
