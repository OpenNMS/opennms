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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.poller.PollStatus;

/**
 * <ul>
 * <li><code>P</code>: Parent type of this container</li>
 * <li><code>C</code>: Child type of this container</li>
 * </ul>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
abstract public class MockContainer<P extends MockContainer<?,?>, C extends MockElement> extends MockElement {

    private volatile Map<Object, C> m_members = new HashMap<Object, C>();

    /**
     * <p>Constructor for MockContainer.</p>
     *
     * @param parent a P object.
     */
    protected MockContainer(P parent) {
        super(parent);
    }

    // FIXME: generic poll listener?
    /** {@inheritDoc} */
    @Override
    public void addAnticipator(final PollAnticipator trigger) {
        MockVisitor triggerAdder = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService service) {
                service.addAnticipator(trigger);
            }
        };
        visit(triggerAdder);
    }

    // model
    /**
     * <p>addMember</p>
     *
     * @param element a C object.
     * @return a C object.
     */
    protected C addMember(C element) {
        m_members.put(element.getKey(), element);
        element.setParent(this);
        return element;
    }

    // model
    /**
     * <p>getMember</p>
     *
     * @param key a {@link java.lang.Object} object.
     * @return a {@link org.opennms.netmgt.mock.MockElement} object.
     */
    protected MockElement getMember(Object key) {
        return m_members.get(key);
    }

    // model
    /**
     * <p>getMembers</p>
     *
     * @return a {@link java.util.List} object.
     */
    protected List<C> getMembers() {
        return new ArrayList<C>(m_members.values());
    }

    // stats
    /**
     * <p>getPollCount</p>
     *
     * @return a int.
     */
    @Override
    public int getPollCount() {
        class PollCounter extends MockVisitorAdapter {
            int pollCount = 0;

            int getPollCount() {
                return pollCount;
            }

            @Override
            public void visitService(MockService service) {
                pollCount += service.getPollCount();
            }
        }

        PollCounter pollCounter = new PollCounter();
        visit(pollCounter);
        return pollCounter.getPollCount();
    }

    // FIXME: where should this live?
    /**
     * <p>getPollStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Override
    public PollStatus getPollStatus() {
        for (MockElement element : m_members.values()) {
            if (element.getPollStatus().isUp()) {
                return PollStatus.up();
            }
        }
        return PollStatus.down();
    }

    // FIXME: make a generic poll listener
    /** {@inheritDoc} */
    @Override
    public void removeAnticipator(final PollAnticipator trigger) {
        MockVisitor triggerRemover = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService service) {
                service.removeAnticipator(trigger);
            }
        };
        visit(triggerRemover);
    }

    // model
    /**
     * <p>removeMember</p>
     *
     * @param element a {@link org.opennms.netmgt.mock.MockElement} object.
     */
    protected void removeMember(MockElement element) {
        m_members.remove(element.getKey());
        element.setParent(null);
    }

    // stats
    /**
     * <p>resetPollCount</p>
     */
    @Override
    public void resetPollCount() {
        class PollCountReset extends MockVisitorAdapter {
            @Override
            public void visitService(MockService service) {
                service.resetPollCount();
            }
        }

        PollCountReset pollCounter = new PollCountReset();
        visit(pollCounter);
    }

    // impl
    /** {@inheritDoc} */
    @Override
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitContainer(this);
    }

    // impl
    /**
     * <p>visitMembers</p>
     *
     * @param v a {@link org.opennms.netmgt.mock.MockVisitor} object.
     */
    protected void visitMembers(MockVisitor v) {
        for (MockElement element : m_members.values()) {
            element.visit(v);
        }
    }
}
