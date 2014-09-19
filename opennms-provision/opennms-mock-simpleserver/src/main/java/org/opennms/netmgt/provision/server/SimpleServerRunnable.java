package org.opennms.netmgt.provision.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class SimpleServerRunnable implements Runnable {
    final CountDownLatch m_startingLatch = new CountDownLatch(1);
    final CountDownLatch m_stoppingLatch = new CountDownLatch(1);

    public void awaitStartup() throws InterruptedException {
        m_startingLatch.await(5, TimeUnit.SECONDS);
    }

    public void awaitShutdown() throws InterruptedException {
        m_stoppingLatch.await(5, TimeUnit.SECONDS);
    }

    protected void ready() {
        m_startingLatch.countDown();
    }

    protected void finished() {
        m_stoppingLatch.countDown();
    }
}