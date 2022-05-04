/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
