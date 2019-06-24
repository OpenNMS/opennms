/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.registry.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.protocols.registry.api.TelemetryServiceRegistry;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TelemetryRegistryImpl implements TelemetryRegistry {

    @Autowired
    @Qualifier("adapterRegistry")
    private TelemetryServiceRegistry<AdapterDefinition, Adapter> adapterRegistryDelegate;

    @Autowired
    @Qualifier("listenerRegistry")
    private TelemetryServiceRegistry<ListenerDefinition, Listener> listenerRegistryDelegate;

    @Autowired
    @Qualifier("parserRegistry")
    private TelemetryServiceRegistry<ParserDefinition, Parser> parserRegistryDelegate;

    private final Map<String, AsyncDispatcher<TelemetryMessage>> dispatchers = new HashMap<>();

    @Override
    public Adapter getAdapter(AdapterDefinition adapterDefinition) {
        return adapterRegistryDelegate.getService(adapterDefinition);
    }

    @Override
    public Listener getListener(ListenerDefinition listenerDefinition) {
        return listenerRegistryDelegate.getService(listenerDefinition);
    }

    @Override
    public Parser getParser(ParserDefinition parserDefinition) {
        return parserRegistryDelegate.getService(parserDefinition);
    }

    @Override
    public void registerDispatcher(String queueName, AsyncDispatcher<TelemetryMessage> dispatcher) {
        if (dispatchers.containsKey(queueName)) {
            throw new IllegalArgumentException("A queue with name '" + queueName + "' is already registered");
        }
        this.dispatchers.put(queueName, dispatcher);
    }

    @Override
    public void clearDispatchers() {
        dispatchers.clear();
    }

    @Override
    public Collection<AsyncDispatcher<TelemetryMessage>> getDispatchers() {
        return dispatchers.values();
    }

    @Override
    public AsyncDispatcher<TelemetryMessage> getDispatcher(String queueName) {
        return dispatchers.get(queueName);
    }

    @Override
    public void removeDispatcher(String queueName) {
        dispatchers.remove(queueName);
    }

    public void setAdapterRegistryDelegate(TelemetryServiceRegistry<AdapterDefinition, Adapter> adapterRegistryDelegate) {
        this.adapterRegistryDelegate = adapterRegistryDelegate;
    }

    public void setListenerRegistryDelegate(TelemetryServiceRegistry<ListenerDefinition, Listener> listenerRegistryDelegate) {
        this.listenerRegistryDelegate = listenerRegistryDelegate;
    }

    public void setParserRegistryDelegate(TelemetryServiceRegistry<ParserDefinition, Parser> parserRegistryDelegate) {
        this.parserRegistryDelegate = parserRegistryDelegate;
    }
}
