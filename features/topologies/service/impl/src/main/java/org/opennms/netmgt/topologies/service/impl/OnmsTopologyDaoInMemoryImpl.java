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
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyUpdater;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;

public class OnmsTopologyDaoInMemoryImpl implements OnmsTopologyDao {


    private Map<String,OnmsTopologyUpdater> m_updatersMap = new HashMap<String, OnmsTopologyUpdater>();
    Set<OnmsTopologyConsumer> m_consumers = new HashSet<OnmsTopologyConsumer>();

    @Override
    public OnmsTopology getTopology(String protocolSupported) throws OnmsTopologyException {
        if (m_updatersMap.containsKey(protocolSupported)) {
            return m_updatersMap.get(protocolSupported).getTopology();
        }
        throw new OnmsTopologyException(protocolSupported + "protocol not supported");
    }

    @Override
    public void subscribe(OnmsTopologyConsumer consumer) {
        synchronized (m_consumers) {
            m_consumers.add(consumer);            
        }
    }

    @Override
    public void unsubscribe(OnmsTopologyConsumer consumer) {
        synchronized (m_consumers) {
            m_consumers.remove(consumer);
        }
    }

    @Override
    public void register(OnmsTopologyUpdater updater) throws OnmsTopologyException {
        synchronized (m_updatersMap) {
            if (m_updatersMap.containsKey(updater.getProtocol())) {
                throw new OnmsTopologyException("Protocol already registered", updater, updater.getProtocol());
            }
            m_updatersMap.put(updater.getProtocol(), updater);
        }
    }

    @Override
    public void unregister(OnmsTopologyUpdater updater) throws OnmsTopologyException {
        synchronized (m_updatersMap) {
            OnmsTopologyUpdater subscribed =  m_updatersMap.remove(updater.getProtocol());
            if (subscribed == null) {
                throw new OnmsTopologyException("updater is not registered", updater, updater.getProtocol());
            }
        }
    }

    @Override
    public Set<String> getSupportedProtocols() {
        Set<String> protocols = new HashSet<String>();
        synchronized (m_updatersMap) {
            protocols.addAll(m_updatersMap.keySet());
        }
        return protocols;
    }

    @Override
    public void update(OnmsTopologyUpdater updater,
            OnmsTopologyMessage message) throws OnmsTopologyException {
        if (!m_updatersMap.containsKey(updater.getProtocol())) {
            throw new OnmsTopologyException("cannot update message with id: " + message.getMessagebody().getId() + ". Protocol not supported", updater,updater.getProtocol(), message.getMessagestatus());
        }
        if ( m_updatersMap.get(updater.getProtocol()) != updater
                           ) {
            throw new OnmsTopologyException("cannot update message with id: " + message.getMessagebody().getId() + ". updater not supported", updater,updater.getProtocol(), message.getMessagestatus());
        }
        synchronized (m_consumers) {
            m_consumers.stream().
            filter(consumer -> consumer.getProtocols().contains(updater.getProtocol())).
            forEach(consumer -> consumer.consume(message));            
        }
    }
}
