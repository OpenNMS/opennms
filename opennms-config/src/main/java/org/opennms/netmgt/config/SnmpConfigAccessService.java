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
package org.opennms.netmgt.config;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpConfigAccessService {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpConfigAccessService.class);

    private boolean m_dirty = false;
    private final ScheduledExecutorService m_executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "SnmpConfig-Accessor-Thread");
        }
    });

    private final class SaveCallable implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            if (m_dirty) {
                LOG.debug("SnmpPeerFactory has been updated. Persisting to disk.");
                SnmpPeerFactory.getInstance().saveCurrent();
                m_dirty = false;
            }
            return null;
        }
    }

    public SnmpConfigAccessService() {
        m_executor.schedule(new SaveCallable(), 1, TimeUnit.SECONDS);
    }

    public void flushAll() {
        submitAndWait(new SaveCallable());
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress addr, String location) {
        flushAll();
        return submitAndWait(new Callable<SnmpAgentConfig>() {
            @Override public SnmpAgentConfig call() throws Exception {
                return SnmpPeerFactory.getInstance().getAgentConfig(addr, location, false);
            }
        });
    }

    public void define(final SnmpEventInfo eventInfo) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                SnmpPeerFactory.getInstance().define(eventInfo);
                m_dirty = true;
            }
        });
    }
    
    private <T> T submitAndWait(final Callable<T> callable) {
        try {
            return m_executor.submit(callable).get();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw ((RuntimeException) e.getCause());
            } else {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private Future<?> submitWriteOp(final Runnable r) {
        return m_executor.submit(r);
    }

}
