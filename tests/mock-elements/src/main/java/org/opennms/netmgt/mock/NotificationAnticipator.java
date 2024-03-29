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
package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import org.opennms.test.mock.MockUtil;

/**
 * @author brozow
 */
public class NotificationAnticipator {

    List<MockNotification> m_anticipated = new ArrayList<>();
    List<MockNotification> m_unanticipated = new ArrayList<>();
    List<MockNotification[]> m_earlyArrival = new ArrayList<>();
    List<MockNotification[]> m_lateBloomers = new ArrayList<>();

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
        m_anticipated = new ArrayList<>();
        m_unanticipated = new ArrayList<>();
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
        final StringBuilder problems = new StringBuilder();

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
        final StringBuilder b = new StringBuilder();

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
