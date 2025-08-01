/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.config.service.impl;

import static org.opennms.features.config.service.api.ConfigUpdateInfo.WILDCARD_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventHandlerManager {

    private final static Logger LOG = LoggerFactory.getLogger(EventHandlerManager.class);

    private final ConcurrentHashMap<EventType, Map<ConfigUpdateInfo, Collection<Consumer<ConfigUpdateInfo>>>> handlerMap;

    public EventHandlerManager() {
        handlerMap = new ConcurrentHashMap<>();
        handlerMap.put(EventType.CREATE, new ConcurrentHashMap<>());
        handlerMap.put(EventType.DELETE, new ConcurrentHashMap<>());
        handlerMap.put(EventType.VALIDATE, new ConcurrentHashMap<>());
        handlerMap.put(EventType.UPDATE, new ConcurrentHashMap<>());
    }

    void callEventHandlers(EventType type, ConfigUpdateInfo specificIdent) {
        LOG.debug("Call {} handlers for {}.", type, specificIdent);
        Map<ConfigUpdateInfo, Collection<Consumer<ConfigUpdateInfo>>> map = handlerMap.get(type);
        ConfigUpdateInfo wildcardIdent = new ConfigUpdateInfo(specificIdent.getConfigName(), WILDCARD_ID);
        callEventHandlers(map, specificIdent, specificIdent); // specific
        callEventHandlers(map, wildcardIdent, specificIdent); // wildcard
    }

    private void callEventHandlers(Map<ConfigUpdateInfo,
            Collection<Consumer<ConfigUpdateInfo>>> map,
            ConfigUpdateInfo searchIdent,
            ConfigUpdateInfo actualIdent) {
        map.computeIfPresent(searchIdent, (k, v) -> {
            v.forEach(c -> {
                try {
                    c.accept(actualIdent);
                } catch (Exception e) {
                    // throw out to let web api obtain exception
                    if (e instanceof ValidationException) {
                        throw e;
                    }
                    LOG.warn("Fail to notify {}, callback: {}, error: {}",
                            actualIdent, v, e.getMessage(), e);
                }
            });
            return v;
        });
    }

    void registerEventHandler(EventType type, ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
        Map<ConfigUpdateInfo, Collection<Consumer<ConfigUpdateInfo>>> map = handlerMap.get(type);
        map.compute(info, (k, v) -> {
            if (v == null) {
                ArrayList<Consumer<ConfigUpdateInfo>> consumers = new ArrayList<>();
                consumers.add(consumer);
                return consumers;
            } else {
                v.add(consumer);
                return v;
            }
        });
    }
}
