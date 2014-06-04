package org.opennms.netmgt.provision.service;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class ProvisioningTestCase {
    @Autowired
    @Qualifier("scanExecutor")
    private PausibleScheduledThreadPoolExecutor m_scanExecutor;

    @Autowired
    @Qualifier("scheduledExecutor")
    private PausibleScheduledThreadPoolExecutor m_scheduledExecutor;
    
    @Autowired
    @Qualifier("importExecutor")
    private PausibleScheduledThreadPoolExecutor m_importExecutor;

    @Autowired
    @Qualifier("writeExecutor")
    private PausibleScheduledThreadPoolExecutor m_writeExecutor;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;

    public PausibleScheduledThreadPoolExecutor getScanExecutor() {
        return m_scanExecutor;
    }

    public PausibleScheduledThreadPoolExecutor getScheduledExecutor() {
        return m_scheduledExecutor;
    }

    public PausibleScheduledThreadPoolExecutor getImportExecutor() {
        return m_importExecutor;
    }

    public PausibleScheduledThreadPoolExecutor getWriteExecutor() {
        return m_writeExecutor;
    }

    protected void waitForEverything() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                latch.countDown();
            }
        };
        m_scheduledExecutor.execute(runnable);
        m_scanExecutor.execute(runnable);
        m_importExecutor.execute(runnable);
        m_writeExecutor.execute(runnable);
        latch.await(5, TimeUnit.MINUTES);
    }

    protected void waitForImport() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                latch.countDown();
            }
        };
        m_scanExecutor.execute(runnable);
        m_importExecutor.execute(runnable);
        m_writeExecutor.execute(runnable);
        latch.await(5, TimeUnit.MINUTES);
    }

    protected CountDownLatch anticipateEvents(final int numberToMatch, final String... ueis) {
        final CountDownLatch eventReceived = new CountDownLatch(numberToMatch);
        m_eventSubscriber.addEventListener(new EventListener() {
            @Override public void onEvent(final Event e) {
                eventReceived.countDown();
            }

            @Override public String getName() {
                return "Provisioning Test Case";
            }
        }, Arrays.asList(ueis));
        return eventReceived;
    }
}
