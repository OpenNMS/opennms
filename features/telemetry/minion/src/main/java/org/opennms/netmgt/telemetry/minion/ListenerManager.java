/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.minion;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.listeners.api.Listener;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.utils.ListenerFactory;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This {@link ManagedServiceFactory} for service pids that contain
 * telemetry listener definitions and manages their lifecycle by starting/updating
 * and stopping them accordingly.
 *
 * See {@link MapBasedListenerDef} for a list of supported properties.
 *
 * @author jwhite
 */
public class ListenerManager implements ManagedServiceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerManager.class);

    private MessageDispatcherFactory messageDispatcherFactory;
    private DistPollerDao distPollerDao;

    private Map<String, Listener> listenersByPid = new LinkedHashMap<>();
    private Map<String, AsyncDispatcher<TelemetryMessage>> dispatchersByPid = new LinkedHashMap<>();

    @Override
    public String getName() {
        return "Manages telemetry listener lifecycle.";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> properties) {
        final Listener existingListener = listenersByPid.get(pid);
        if (existingListener != null) {
            LOG.info("Updating existing listener/dispatcher for pid: {}", pid);
            deleted(pid);
        } else {
            LOG.info("Creating new listener/dispatcher for pid: {}", pid);
        }

        // Convert the dictionary to a map
        final Map<String, String> parameters = MapUtils.fromDict(properties);

        // Build the protocol and listener definitions
        final MapBasedProtocolDef protocolDef = new MapBasedProtocolDef(parameters);
        final MapBasedListenerDef listenerDef = new MapBasedListenerDef(parameters);

        final TelemetrySinkModule sinkModule = new TelemetrySinkModule(protocolDef);
        sinkModule.setDistPollerDao(distPollerDao);
        final AsyncDispatcher<TelemetryMessage> dispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);

        try {
            final Listener listener = ListenerFactory.buildListener(listenerDef, dispatcher);
            listener.start();
            listenersByPid.put(pid, listener);
            dispatchersByPid.put(pid, dispatcher);
        } catch (Exception e) {
            LOG.error("Failed to build listener.", e);
            try {
                dispatcher.close();
            } catch (Exception ee) {
                LOG.error("Failed to close dispatcher.", e);
            }
        }

        LOG.info("Successfully started listener/dispatcher for pid: {}", pid);
    }

    @Override
    public void deleted(String pid) {
        final Listener listener = listenersByPid.remove(pid);
        if (listener != null) {
            LOG.info("Stopping listener for pid: {}", pid);
            try {
                listener.stop();
            } catch (InterruptedException e) {
                LOG.error("Error occured while stopping listener for pid: {}", pid, e);
            }
        }

        final AsyncDispatcher<TelemetryMessage> dispatcher = dispatchersByPid.remove(pid);
        if (dispatcher != null) {
            LOG.info("Closing dispatcher for pid: {}", pid);
            try {
                dispatcher.close();
            } catch (Exception e) {
                LOG.error("Error occured while closing dispatcher for pid: {}", pid, e);
            }
        }
    }

    public void init() {
        LOG.info("ListenerManager started.");
    }

    public void destroy() {
        listenersByPid.keySet().forEach(pid -> deleted(pid));
        LOG.info("ListenerManager stopped.");
    }

    public MessageDispatcherFactory getMessageDispatcherFactory() {
        return messageDispatcherFactory;
    }

    public void setMessageDispatcherFactory(MessageDispatcherFactory messageDispatcherFactory) {
        this.messageDispatcherFactory = messageDispatcherFactory;
    }

    public DistPollerDao getDistPollerDao() {
        return distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }

}
