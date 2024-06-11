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

    protected ServiceInstance create(final String hostname, final String username, final String password, final int timeout) throws MalformedURLException, RemoteException {
        return new ServiceInstance(new URL("https://" + hostname + "/sdk"), username, password, timeout, timeout);
    }

    public ServiceInstance retain(final String host, final String username, final String password, final int timeout) throws MalformedURLException, RemoteException {
        final ServiceInstancePoolEntry serviceInstancePoolEntry;

        synchronized (this) {
            serviceInstancePoolEntry = this.serviceInstancePoolEntries.computeIfAbsent(host + "/" + username + "/" + password, k -> new ServiceInstancePoolEntry(this, host, username, password));
        }

        return serviceInstancePoolEntry.retain(timeout);
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