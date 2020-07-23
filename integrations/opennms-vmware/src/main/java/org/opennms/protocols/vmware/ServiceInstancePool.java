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

package org.opennms.protocols.vmware;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.utils.PropertiesUtils;

import com.vmware.vim25.mo.ServiceInstance;

public class ServiceInstancePool {
    private final long HOUSEKEEPING_INTERVAL = PropertiesUtils.getProperty(
            System.getProperties(),
            "org.opennms.protocols.vmware.housekeepingInterval",
            300000L
    );

    private final Map<String, ServiceInstancePoolEntry> serviceInstancePoolEntries = new ConcurrentHashMap<>();
    private final Timer timer = new Timer("ServiceInstancePool-Timer", true);

    public ServiceInstancePool() {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expire();
            }
        }, this.HOUSEKEEPING_INTERVAL, this.HOUSEKEEPING_INTERVAL);
    }

    private synchronized void expire() {
        for (final Iterator<Map.Entry<String, ServiceInstancePoolEntry>> mapIterator = serviceInstancePoolEntries.entrySet().iterator(); mapIterator.hasNext(); ) {
            Map.Entry<String, ServiceInstancePoolEntry> mapEntry = mapIterator.next();
            mapEntry.getValue().expire(this.HOUSEKEEPING_INTERVAL);

            if (mapEntry.getValue().isUnused()) {
                mapIterator.remove();
            }
        }
    }

    protected boolean validate(final ServiceInstance serviceInstance) {
        if (serviceInstance == null || serviceInstance.getSessionManager() == null) {
            return false;
        } else {
            return serviceInstance.getSessionManager().getCurrentSession() != null;
        }
    }

    protected ServiceInstance create(final String hostname, final String username, final String password) throws MalformedURLException, RemoteException {
        return new ServiceInstance(new URL("https://" + hostname + "/sdk"), username, password);
    }

    public ServiceInstance retain(final String host, final String username, final String password) throws MalformedURLException, RemoteException {
        final ServiceInstancePoolEntry serviceInstancePoolEntry;

        synchronized (this) {
            serviceInstancePoolEntry = this.serviceInstancePoolEntries.computeIfAbsent(host + "/" + username + "/" + password, k -> new ServiceInstancePoolEntry(this, host, username, password));
        }

        return serviceInstancePoolEntry.retain();
    }

    public synchronized void release(final ServiceInstance serviceInstance) {
        if (serviceInstance == null) {
            return;
        }

        for (Map.Entry<String, ServiceInstancePoolEntry> entry : this.serviceInstancePoolEntries.entrySet()) {
            entry.getValue().release(serviceInstance);
        }
    }

    public int lockedEntryCount(final String key) {
        return this.serviceInstancePoolEntries.containsKey(key) ? this.serviceInstancePoolEntries.get(key).lockedEntryCount() : 0;
    }

    public int unlockedEntryCount(final String key) {
        return this.serviceInstancePoolEntries.containsKey(key) ? this.serviceInstancePoolEntries.get(key).unlockedEntryCount() : 0;
    }

    public int lockedEntryCount() {
        return this.serviceInstancePoolEntries.values().stream().mapToInt(e -> e.lockedEntryCount()).sum();
    }

    public int unlockedEntryCount() {
        return this.serviceInstancePoolEntries.values().stream().mapToInt(e -> e.unlockedEntryCount()).sum();
    }
}