package org.opennms.netmgt.syslogd;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.Consume;
import org.opennms.netmgt.xml.event.Event;

public class EventCounter {
    private AtomicInteger m_eventCount = new AtomicInteger(0);
    private int m_expectedCount = 0;

    // Me love you, long time.
    public void waitForFinish(final long time) {
        final long start = System.currentTimeMillis();
        while (this.getEventCount() < m_expectedCount) {
            if (System.currentTimeMillis() - start > time) {
                SyslogdImplementationsIT.LOG.warn("waitForFinish timeout ({}) reached", time);
                break;
            }
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                SyslogdImplementationsIT.LOG.warn("thread was interrupted while sleeping", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setAnticipatedEventCount(final int eventCount) {
        m_expectedCount = eventCount;
    }

    public int getEventCount() {
        return m_eventCount.get();
    }

    public void anticipateEvent() {
        m_expectedCount++;
    }

    public void reset() {
        m_expectedCount = 0;
        m_eventCount.set(0);
    }

    @Consume(uri="queuingservice:topic:OpenNMS.Eventd.BroadcastEvent?concurrentConsumers=1")
    public void onEvent(final Event e) {
        final int current = m_eventCount.incrementAndGet();
        if (current % 100 == 0) {
            System.err.println(current + " out of " + m_expectedCount + " expected events received");
        }
    }

}