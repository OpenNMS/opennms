/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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