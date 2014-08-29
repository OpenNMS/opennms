package org.opennms.netmgt.config;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class SnmpConfigAccessService {
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

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
                LogUtils.debugf(this, "SnmpPeerFactory has been updated. Persisting to disk.");
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

    public SnmpAgentConfig getAgentConfig(final InetAddress addr) {
        flushAll();
        return submitAndWait(new Callable<SnmpAgentConfig>() {
            @Override public SnmpAgentConfig call() throws Exception {
                return SnmpPeerFactory.getInstance().getAgentConfig(addr);
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
