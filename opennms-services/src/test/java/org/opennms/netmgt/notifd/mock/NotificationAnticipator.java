//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.notifd.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.opennms.test.mock.MockUtil;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class NotificationAnticipator {

    List m_anticipated = new ArrayList();
    List m_unanticipated = new ArrayList();
    List m_earlyArrival = new ArrayList();
    List m_lateBloomers = new ArrayList();

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
            MockNotification notification = (MockNotification) m_anticipated.get(i);

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

    public Collection getAnticipatedNotifications() {
        return Collections.unmodifiableCollection(m_anticipated);
    }

    public void reset() {
        m_anticipated = new ArrayList();
        m_unanticipated = new ArrayList();
    }

    /**
     * @return
     */
    public Collection getUnanticipated() {
        return Collections.unmodifiableCollection(m_unanticipated);
    }

    /**
     * @param i
     * @return
     */
    public synchronized Collection waitForAnticipated(long millis) {
        long waitTime = millis;
        long start = System.currentTimeMillis();
        long now = start;
        while (waitTime > 0) {
            if (m_anticipated.isEmpty())
                return Collections.EMPTY_LIST;
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

        Collection missingNotifications = waitForAnticipated(totalWaitTime);
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
            Collection notifications) {
        StringBuffer b = new StringBuffer();

        for (Iterator it = notifications.iterator(); it.hasNext();) {
            MockNotification notification;
            MockNotification received = null;
            Object o = it.next();
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
