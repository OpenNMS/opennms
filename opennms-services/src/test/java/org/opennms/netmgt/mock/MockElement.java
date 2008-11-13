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
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
abstract public class MockElement {

    MockContainer m_parent;

    protected MockElement(MockContainer parent) {
        m_parent = parent;
    }

    // FIXME: generic listener
    abstract public void addAnticipator(PollAnticipator trigger);

    // test
    public void bringDown() {
        setServicePollStatus(PollStatus.down());
    }

    // test
    public void bringUp() {
        setServicePollStatus(PollStatus.up());
    }
    
    // test
    public void bringUnresponsive() {
        setServicePollStatus(PollStatus.unresponsive());
    }

    // impl
    abstract Object getKey();

    // model
    public MockContainer getParent() {
        return m_parent;
    }
    
    public MockNetwork getNetwork() {
        MockElement network = this;
        
        while(network.getParent() != null)
            network = network.getParent();
        
        return (MockNetwork)network;
    }

    // stats
    abstract public int getPollCount();

    // test
    abstract public PollStatus getPollStatus();

    // model
    public void moveTo(MockContainer newParent) {
        m_parent.removeMember(this);
        newParent.addMember(this);
    }

    // FIXME: generic listener
    abstract public void removeAnticipator(PollAnticipator trigger);

    // stats
    abstract public void resetPollCount();

    // model
    void setParent(MockContainer parent) {
        m_parent = parent;
    }

    // test
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
    public void visit(MockVisitor v) {
        v.visitElement(this);
    }
    
    abstract public Event createDownEvent();
    
    abstract public Event createUpEvent();
    

    /**
     * @param upDate
     * @return
     */
    public Event createUpEvent(Date date) {
        Event e = createUpEvent();
        MockEventUtil.setEventTime(e, date);
        return e;
    }

    /**
     * @param date
     * @return
     */
    public Event createDownEvent(Date date) {
        Event e = createDownEvent();
        MockEventUtil.setEventTime(e, date);
        return e;
    }
    
    abstract public Event createNewEvent();

    abstract public Event createDeleteEvent();

}
