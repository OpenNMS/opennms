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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

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

    MockContainer m_parent;

    /**
     * <p>Constructor for MockElement.</p>
     *
     * @param parent a {@link org.opennms.netmgt.mock.MockContainer} object.
     */
    protected MockElement(MockContainer parent) {
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
    public MockContainer getParent() {
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
    void setParent(MockContainer parent) {
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
