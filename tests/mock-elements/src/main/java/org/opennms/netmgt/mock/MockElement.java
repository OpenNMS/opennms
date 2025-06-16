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

import java.util.Date;

import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>Abstract MockElement class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
abstract public class MockElement {

	private volatile MockContainer<?,? extends MockElement> m_parent;

    /**
     * <p>Constructor for MockElement.</p>
     *
     * @param parent a {@link org.opennms.netmgt.mock.MockContainer} object.
     */
    protected MockElement(MockContainer<?,? extends MockElement> parent) {
        m_parent = parent;
    }

    // FIXME: generic listener
    /**
     * <p>addAnticipator</p>
     *
     * @param trigger a {@link org.opennms.netmgt.mock.PollAnticipator} object.
     */
    public abstract void addAnticipator(PollAnticipator trigger);

    // test
    /**
     * <p>bringDown</p>
     */
    public void bringDown() {
        setServicePollStatus(PollStatus.down());
    }

    // test
    /**
     * <p>bringUp</p>
     */
    public void bringUp() {
        setServicePollStatus(PollStatus.up());
    }
    
    // test
    /**
     * <p>bringUnresponsive</p>
     */
    public void bringUnresponsive() {
        setServicePollStatus(PollStatus.unresponsive());
    }

    // impl
    abstract Object getKey();

    // model
    /**
     * <p>getParent</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockContainer} object.
     */
    public MockContainer<?,? extends MockElement> getParent() {
        return m_parent;
    }
    
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
    public MockNetwork getNetwork() {
        MockElement network = this;
        
        while(network.getParent() != null)
            network = network.getParent();
        
        return (MockNetwork)network;
    }

    // stats
    /**
     * <p>getPollCount</p>
     *
     * @return a int.
     */
    public abstract int getPollCount();

    // test
    /**
     * <p>getPollStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public abstract PollStatus getPollStatus();

    // model
    /**
     * <p>moveTo</p>
     *
     * @param newParent a {@link org.opennms.netmgt.mock.MockContainer} object.
     */
    @SuppressWarnings({ "unchecked" })
	public void moveTo(MockContainer newParent) {
        m_parent.removeMember(this);
        newParent.addMember(this);
    }

    // FIXME: generic listener
    /**
     * <p>removeAnticipator</p>
     *
     * @param trigger a {@link org.opennms.netmgt.mock.PollAnticipator} object.
     */
    public abstract void removeAnticipator(PollAnticipator trigger);

    // stats
    /**
     * <p>resetPollCount</p>
     */
    public abstract void resetPollCount();

    // model
    void setParent(MockContainer<?,? extends MockElement> parent) {
        m_parent = parent;
    }

    // test
    /**
     * <p>setServicePollStatus</p>
     *
     * @param newStatus a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    protected void setServicePollStatus(final PollStatus newStatus) {
        MockVisitor statusSetter = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                svc.setPollStatus(newStatus);
            }
        };
        synchronized(getNetwork()) {
            visit(statusSetter);
        }
    }

    // impl
    /**
     * <p>visit</p>
     *
     * @param v a {@link org.opennms.netmgt.mock.MockVisitor} object.
     */
    public void visit(MockVisitor v) {
        v.visitElement(this);
    }
    
    /**
     * <p>createDownEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public abstract Event createDownEvent();
    
    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public abstract Event createUpEvent();
    

    /**
     * <p>createUpEvent</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createUpEvent(Date date) {
        Event e = createUpEvent();
        MockEventUtil.setEventTime(e, date);
        return e;
    }

    /**
     * <p>createDownEvent</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createDownEvent(Date date) {
        Event e = createDownEvent();
        MockEventUtil.setEventTime(e, date);
        return e;
    }
    
    /**
     * <p>createNewEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public abstract Event createNewEvent();

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public abstract Event createDeleteEvent();

}
