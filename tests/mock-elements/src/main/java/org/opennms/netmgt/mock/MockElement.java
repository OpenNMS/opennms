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

import java.util.Date;

import org.opennms.netmgt.model.PollStatus;
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
    abstract public void addAnticipator(PollAnticipator trigger);

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
    abstract public int getPollCount();

    // test
    /**
     * <p>getPollStatus</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    abstract public PollStatus getPollStatus();

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
    abstract public void removeAnticipator(PollAnticipator trigger);

    // stats
    /**
     * <p>resetPollCount</p>
     */
    abstract public void resetPollCount();

    // model
    void setParent(MockContainer<?,? extends MockElement> parent) {
        m_parent = parent;
    }

    // test
    /**
     * <p>setServicePollStatus</p>
     *
     * @param newStatus a {@link org.opennms.netmgt.model.PollStatus} object.
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
    abstract public Event createDownEvent();
    
    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    abstract public Event createUpEvent();
    

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
    abstract public Event createNewEvent();

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    abstract public Event createDeleteEvent();

}
