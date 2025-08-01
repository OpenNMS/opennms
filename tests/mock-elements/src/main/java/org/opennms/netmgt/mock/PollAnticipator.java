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

/**
 * <p>PollAnticipator class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class PollAnticipator {

    List<MockService> m_anticipatedPolls = new ArrayList<>();

    List<MockService> m_unanticipatedPolls = new ArrayList<>();

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
