/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

/**
 * <p>PollAnticipator class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class PollAnticipator {

    List<MockService> m_anticipatedPolls = new ArrayList<MockService>();

    List<MockService> m_unanticipatedPolls = new ArrayList<MockService>();

    /**
     * <p>anticipateAllServices</p>
     *
     * @param element a {@link org.opennms.netmgt.mock.MockElement} object.
     */
    public void anticipateAllServices(MockElement element) {
        /*
         * This visit anticipates a poll on all of the services beneath
         * an element it visits.
         */
        MockVisitor anticipateSvcs = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                anticipatePoll(svc);
            }
        };

        // visit the elements and ensure that we anticipate polls on them
        element.visit(anticipateSvcs);
    }

    /**
     * <p>anticipatePoll</p>
     *
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public synchronized void anticipatePoll(MockService svc) {
        m_anticipatedPolls.add(svc);
    }

    /**
     * <p>poll</p>
     *
     * @param service a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public synchronized void poll(MockService service) {

        if (m_anticipatedPolls.contains(service)) {
            m_anticipatedPolls.remove(service);
        } else {
            m_unanticipatedPolls.add(service);
        }

        if (m_anticipatedPolls.isEmpty()) {
            notifyAll();
        }
    }

    /**
     * <p>reset</p>
     */
    public synchronized void reset() {
        m_anticipatedPolls.clear();
        m_unanticipatedPolls.clear();
    }

    /**
     * <p>unanticipatedPolls</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<MockService> unanticipatedPolls() {
        return Collections.unmodifiableCollection(m_unanticipatedPolls);
    }

    /**
     * @param millis
     */
    private void waitFor(long millis) {
        try {
            wait(millis);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Waits for millis milliseconds for the anticipated polls to come. If they
     * all come in before the timeout return an empty list. Otherwise return a
     * read-only list of the services that were anticipated but not received.
     *
     * @param millis a long.
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<MockService> waitForAnticipated(long millis) {
        long start = System.currentTimeMillis();
        long now = start;
        while ((now - start) < millis) {
            if (m_anticipatedPolls.isEmpty()) {
                return new ArrayList<MockService>(0);
            }
            waitFor(millis);
            now = System.currentTimeMillis();
        }
        return Collections.unmodifiableCollection(m_anticipatedPolls);
    }

}
