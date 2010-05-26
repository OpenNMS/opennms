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
// Modifications:
//
// 2008 Feb 09: Java 5 generics. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author brozow
 */
public class PollAnticipator {

    List<MockService> m_anticipatedPolls = new ArrayList<MockService>();

    List<MockService> m_unanticipatedPolls = new ArrayList<MockService>();

    /**
     * @param element
     * @return
     */
    public void anticipateAllServices(MockElement element) {
        /*
         * This visit anticipates a poll on all of the services beneath
         * an element it visits.
         */
        MockVisitor anticipateSvcs = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                anticipatePoll(svc);
            }
        };

        // visit the elements and ensure that we anticpate polls on them
        element.visit(anticipateSvcs);
    }

    /**
     * @param svc
     */
    public synchronized void anticipatePoll(MockService svc) {
        m_anticipatedPolls.add(svc);
    }

    /**
     * @param service
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

    public synchronized void reset() {
        m_anticipatedPolls.clear();
        m_unanticipatedPolls.clear();
    }

    /**
     * @return
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
     * readonly list of the services that were anticipated but not received.
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
