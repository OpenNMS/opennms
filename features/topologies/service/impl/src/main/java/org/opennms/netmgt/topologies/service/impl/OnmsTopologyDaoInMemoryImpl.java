/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.topologies.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnmsTopologyDaoInMemoryImpl implements OnmsTopologyDao {

    private static final Logger LOG = LoggerFactory.getLogger(OnmsTopologyDaoInMemoryImpl.class);
    private final Map<OnmsTopologyProtocol, OnmsTopologyUpdater> m_updatersMap = new HashMap<>();
    private final Set<OnmsTopologyConsumer> m_consumers = new HashSet<>();

    @Override
    public OnmsTopology getTopology(String protocolSupported) {
        OnmsTopologyProtocol protocol = OnmsTopologyProtocol.create(protocolSupported);
        if (m_updatersMap.containsKey(protocol)) {
            return m_updatersMap.get(protocol).getTopology();
        }
        throw new IllegalArgumentException(String.format("%s protocol not supported", protocolSupported));
    }

    @Override
    public Map<OnmsTopologyProtocol, OnmsTopology> getTopologies() {
        Map<OnmsTopologyProtocol, OnmsTopology> topologies = new HashMap<>();
        synchronized (m_updatersMap) {
            m_updatersMap.forEach((key, value) -> topologies.put(key, value.getTopology()));
        }
        return topologies;
    }

    @Override
    public void subscribe(OnmsTopologyConsumer consumer) {
        synchronized (m_consumers) {
            LOG.debug("Consumer subscribed {}", consumer);
            m_consumers.add(consumer);
        }
    }

    @Override
    public void unsubscribe(OnmsTopologyConsumer consumer) {
        synchronized (m_consumers) {
            LOG.debug("Consumer unsubscribed {}", consumer);
            m_consumers.remove(consumer);
        }
    }

    public void onBind(OnmsTopologyConsumer consumer, Map<String, String> properties) {
        synchronized (m_consumers) {
            LOG.debug("Consumer {} bind with properties {}", consumer, properties);
            m_consumers.add(consumer);
        }
    }

    public void onUnbind(OnmsTopologyConsumer consumer, Map<String, String> properties) {
        synchronized (m_consumers) {
            LOG.debug("Consumer {} unbind with properties {}", consumer, properties);
            m_consumers.remove(consumer);
        }
    }

    @Override
    public void register(OnmsTopologyUpdater updater) {
        synchronized (m_updatersMap) {
            if (m_updatersMap.containsKey(updater.getProtocol())) {
                throw new IllegalArgumentException("Protocol already registered " + updater.getProtocol());
            }
            m_updatersMap.put(updater.getProtocol(), updater);
        }
    }

    @Override
    public void unregister(OnmsTopologyUpdater updater) {
        synchronized (m_updatersMap) {
            OnmsTopologyUpdater subscribed = m_updatersMap.get(updater.getProtocol());
            if (subscribed == null || subscribed != updater) {
                throw new IllegalArgumentException("updater is not registered " + updater.getProtocol());
            }

            m_updatersMap.remove(updater.getProtocol());
        }
    }

    @Override
    public Set<OnmsTopologyProtocol> getSupportedProtocols() {
        return m_updatersMap.keySet();
    }

    @Override
    public void update(OnmsTopologyUpdater updater, OnmsTopologyMessage message) {
        final OnmsTopologyProtocol protocol = updater.getProtocol();
        if (!m_updatersMap.containsKey(protocol)) {
            throw new IllegalArgumentException(String.format("cannot update message with id: %s. Protocol %s not " +
                            "supported for message status %s", message.getMessagebody().getId(), protocol,
                    message.getMessagestatus()));
        }
        if (m_updatersMap.get(protocol) != updater) {
            throw new IllegalArgumentException(String.format("cannot update message with id: %s, protocol: %s and " +
                            "message status: %s. Updater not registered", message.getMessagebody().getId(), protocol,
                    message.getMessagestatus()));
        }
        synchronized (m_consumers) {
            m_consumers
                    .stream()
                    .filter(consumer -> {
                        if (consumer.getProtocols() == null || consumer.getProtocols().isEmpty()) {
                            return false;
                        }
                        return consumer.getProtocols().contains(OnmsTopologyProtocol.allProtocols()) ||
                                consumer.getProtocols().contains(protocol);
                    })
                    .forEach(consumer -> consumer.consume(message));
        }
    }
}
