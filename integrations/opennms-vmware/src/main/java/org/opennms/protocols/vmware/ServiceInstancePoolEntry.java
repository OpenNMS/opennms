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
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vmware.vim25.mo.ServiceInstance;

public class ServiceInstancePoolEntry {
    private final String hostname;
    private final String username;
    private final String password;
    private final ServiceInstancePool serviceInstancePool;

    private Set<ServiceInstance> locked = new HashSet<>();
    private Set<ServiceInstance> unlocked = new HashSet<>();
    private Map<ServiceInstance, Long> accessTimestamp = new HashMap<>();

    public ServiceInstancePoolEntry(final ServiceInstancePool serviceInstancePool, final String hostname, final String username, final String password) {
        this.serviceInstancePool = serviceInstancePool;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public synchronized ServiceInstance retain() throws MalformedURLException, RemoteException {
        if (this.unlocked.size() > 0) {
            final ServiceInstance serviceInstance = this.unlocked.iterator().next();

            if (this.serviceInstancePool.validate(serviceInstance)) {
                this.unlocked.remove(serviceInstance);
                this.locked.add(serviceInstance);
                this.accessTimestamp.put(serviceInstance, System.currentTimeMillis());

                return serviceInstance;
            } else {
                this.unlocked.remove(serviceInstance);
                this.accessTimestamp.remove(serviceInstance);
            }
        }

        final ServiceInstance serviceInstance = this.serviceInstancePool.create(hostname, username, password);
        this.locked.add(serviceInstance);
        this.accessTimestamp.put(serviceInstance, System.currentTimeMillis());
        return serviceInstance;
    }

    public synchronized void expire(final long ageInMilliseconds) {
        final long now = System.currentTimeMillis();

        /**
         * logout and remove outdated idle connections
         */
        this.unlocked.removeIf(e -> {
            if (!this.accessTimestamp.containsKey(e) || now - this.accessTimestamp.get(e) > ageInMilliseconds) {
                // try to logout
                if (e.getServerConnection() != null) {
                    e.getServerConnection().logout();
                }

                return true;
            } else {
                return !this.serviceInstancePool.validate(e);
            }
        });

        /**
         * remove invalid locked connections
         */
        this.locked.removeIf(e -> {
            if (!this.accessTimestamp.containsKey(e) || now - this.accessTimestamp.get(e) > ageInMilliseconds) {
                return !this.serviceInstancePool.validate(e);
            } else {
                return false;
            }
        });

        this.accessTimestamp.entrySet().removeIf(e -> !this.locked.contains(e.getKey()) && !this.unlocked.contains(e.getKey()));
    }

    public synchronized boolean isUnused() {
        return this.locked.isEmpty() && this.unlocked.isEmpty();
    }

    public synchronized void release(final ServiceInstance serviceInstance) {
        if (serviceInstance == null) {
            return;
        }

        if (this.locked.remove(serviceInstance)) {
            this.unlocked.add(serviceInstance);
            this.accessTimestamp.put(serviceInstance, System.currentTimeMillis());
        }
    }

    public int lockedEntryCount() {
        return this.locked.size();
    }

    public int unlockedEntryCount() {
        return this.unlocked.size();
    }

    protected Map<ServiceInstance, Long> getAccessTimestamp() {
        return accessTimestamp;
    }
}
