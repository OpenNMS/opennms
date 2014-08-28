package org.opennms.web.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class SnmpConfigAccessService {
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    private final ExecutorService m_executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "SnmpConfig-Accessor-Thread");
        }
    });

    public SnmpConfigAccessService() {
    }

    public void flushAll() {
        try {
            m_executor.submit(new Runnable() {
                @Override public void run() {
                    try {
                        SnmpPeerFactory.getInstance().saveCurrent();
                    } catch (final IOException e) {
                        LogUtils.debugf(this, e, "Failed to save SNMP configuration.");
                    }
                }
            }).get();
        } catch (final InterruptedException e) {
            LogUtils.warnf(this, e, "Interrupted while flushing.  Passing interrupt up to caller.");
        } catch (final ExecutionException e) {
            LogUtils.errorf(this, e, "An error occurred while waiting for SnmpPeerFactory operations to complete.");
        }
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress addr) {
        return submitAndWait(new Callable<SnmpAgentConfig>() {
            @Override public SnmpAgentConfig call() throws Exception {
                flushAll();
                return SnmpPeerFactory.getInstance().getAgentConfig(addr);
            }
        });
    }

    public void define(final SnmpEventInfo eventInfo) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                SnmpPeerFactory.getInstance().define(eventInfo);
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
