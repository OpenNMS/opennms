/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.opennms.test.mock.MockUtil;

/**
 * @author brozow
 */
public class NotificationAnticipator {

    List<MockNotification> m_anticipated = new ArrayList<MockNotification>();
    List<MockNotification> m_unanticipated = new ArrayList<MockNotification>();
    List<MockNotification[]> m_earlyArrival = new ArrayList<MockNotification[]>();
    List<MockNotification[]> m_lateBloomers = new ArrayList<MockNotification[]>();

    long m_expectedDiff = 1000;

    /**
     */
    public NotificationAnticipator() {
    }

    /**
     * @param expectedDiff
     */
    public void setExpectedDifference(long expectedDiff) {
        m_expectedDiff = expectedDiff;
    }

    /**
     * 
     * @return
     */
    public long getExpectedDifference() {
        return m_expectedDiff;
    }

    /**
     * @param event
     *  
     */
    public void anticipateNotification(MockNotification mn) {
        MockUtil.println("Anticipating notification: " + mn);
        m_anticipated.add(mn);
    }

    /**
     * @param event
     */
    public synchronized void notificationReceived(MockNotification mn) {
        int i = m_anticipated.indexOf(mn);
        if (i != -1) {
            MockNotification notification = m_anticipated.get(i);

            long receivedTime = mn.getExpectedTime();
            long expectedTime = notification.getExpectedTime();
            long difference = expectedTime - receivedTime;

            if (Math.abs(difference) < m_expectedDiff) {
                MockUtil.println("Received expected notification: " + mn);
                m_anticipated.remove(mn);
                notifyAll();
            } else {
                MockNotification[] n = new MockNotification[] { notification, mn };
                if (difference > 0) {
                    MockUtil.println("Received early notification: " + mn);
                    m_earlyArrival.add(n);
                } else {
                    MockUtil.println("Received late notification: " + mn);
                    m_lateBloomers.add(n);
                }
            }
        } else {
            MockUtil.println("Received unexpected notification: " + mn);
            m_unanticipated.add(mn);
        }
    }

    public Collection<MockNotification> getAnticipatedNotifications() {
        return Collections.unmodifiableCollection(m_anticipated);
    }

    public void reset() {
        m_anticipated = new ArrayList<MockNotification>();
        m_unanticipated = new ArrayList<MockNotification>();
    }

    /**
     * @return
     */
    public Collection<MockNotification> getUnanticipated() {
        return Collections.unmodifiableCollection(m_unanticipated);
    }

    /**
     * @param i
     * @return
     */
    public synchronized Collection<MockNotification> waitForAnticipated(long millis) {
        long waitTime = millis;
        long start = System.currentTimeMillis();
        long now = start;
        while (waitTime > 0) {
            if (m_anticipated.isEmpty())
                return new ArrayList<MockNotification>(0);
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
            }
            now = System.currentTimeMillis();
            waitTime -= (now - start);
        }
        return getAnticipatedNotifications();
    }

    public void verifyAnticipated(long lastNotifyTime, long waitTime,
            long sleepTime) {
        StringBuffer problems = new StringBuffer();

        long totalWaitTime = Math.max(0, lastNotifyTime + waitTime
                - System.currentTimeMillis());

        Collection<MockNotification> missingNotifications = waitForAnticipated(totalWaitTime);
        // make sure that we didn't start before we should have
        long now = System.currentTimeMillis();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }

        if (missingNotifications.size() != 0) {
            problems.append(missingNotifications.size() +
            " expected notifications still outstanding:\n");
            problems.append(listNotifications("\t", missingNotifications));
        }
        if (getUnanticipated().size() != 0) {
            problems.append(getUnanticipated().size() +
            " unanticipated notifications received:\n");
            problems.append(listNotifications("\t", getUnanticipated()));
        }
        if (m_earlyArrival.size() != 0) {
            problems.append(m_earlyArrival.size() +
            " early notifications received:\n");
            problems.append(listNotifications("\t", m_earlyArrival));
        }
        if (m_lateBloomers.size() != 0) {
            problems.append(m_lateBloomers.size() +
            " late notifications received:\n");
            problems.append(listNotifications("\t", m_lateBloomers));
        }
        if (lastNotifyTime > now) {
            problems.append("Anticipated notifications received at " +
                    lastNotifyTime + ", later than the last expected time of " +
                    now + "\n");
        }

        if (problems.length() > 0) {
            problems.deleteCharAt(problems.length() - 1);
            Assert.fail(problems.toString());
        }
    }

    private static String listNotifications(String prefix,
            Collection<?> notifications) {
        StringBuffer b = new StringBuffer();

        for (Object o : notifications) {
            MockNotification notification;
            MockNotification received = null;

            if (o instanceof MockNotification[]) {
                notification = ((MockNotification[]) o)[0];
                received = ((MockNotification[]) o)[1];
            } else {
                notification = (MockNotification) o;
            }
            b.append(prefix);
            b.append(notification);
            if (received != null) {
                b.append(" (received: ");
                b.append(received.getExpectedTime());
                b.append(")");
            }
            b.append("\n");
        }

        return b.toString();
    }
}
