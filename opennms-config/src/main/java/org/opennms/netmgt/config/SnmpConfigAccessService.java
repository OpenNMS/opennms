/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
                return SnmpPeerFactory.getInstance().getAgentConfig(addr, location);
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
