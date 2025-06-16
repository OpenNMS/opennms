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
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vmware.vim25.VimPortType;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.ws.Client;

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

    public synchronized ServiceInstance retain(final int timeout) throws MalformedURLException, RemoteException {
        if (this.unlocked.size() > 0) {
            final ServiceInstance serviceInstance = this.unlocked.iterator().next();

            if (this.serviceInstancePool.validate(serviceInstance)) {
                this.unlocked.remove(serviceInstance);
                this.locked.add(serviceInstance);
                setTimeout(serviceInstance, timeout);

                this.accessTimestamp.put(serviceInstance, System.currentTimeMillis());

                return serviceInstance;
            } else {
                this.unlocked.remove(serviceInstance);
                this.accessTimestamp.remove(serviceInstance);
            }
        }

        final ServiceInstance serviceInstance = this.serviceInstancePool.create(hostname, username, password, timeout);
        this.locked.add(serviceInstance);
        this.accessTimestamp.put(serviceInstance, System.currentTimeMillis());
        return serviceInstance;
    }

    public static void setTimeout(final ServiceInstance serviceInstance, final int timeout) {
        if (serviceInstance != null) {
            ServerConnection serverConnection = serviceInstance.getServerConnection();
            if (serverConnection != null) {
                VimPortType vimService = serverConnection.getVimService();
                if (vimService != null) {
                    Client client = vimService.getWsc();
                    if (client != null) {
                        client.setConnectTimeout(timeout);
                        client.setReadTimeout(timeout);
                    }
                }
            }
        }
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
