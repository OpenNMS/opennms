//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents an aggregate network element such as an interface containing svcs or
 * a node containing interfaces
 * @author brozow
 */
abstract public class PollerContainer extends PollerElement {

    /**
     * Map of 'PollableService' objects keyed by service name
     */
    protected Map m_members;

    /**
     * @param status
     */
    public PollerContainer(PollStatus status) {
        super(status);
        m_members = Collections.synchronizedMap(new HashMap());
    }

    protected void generateMemberEvents(Date date) {
        Iterator it = m_members.values().iterator();
        while (it.hasNext()) {
            PollerElement member = (PollerElement) it.next();
            member.generateEvents(date);
        }
    }
    
    private void generateLingeringMemberEvents(Date date, int cause) {
        Iterator it = m_members.values().iterator();
        while (it.hasNext()) {
            PollerElement member = (PollerElement) it.next();
            member.generateLingeringDownEvents(date, cause);
        }
    }

    /**
     * @param date
     */
    public void generateEvents(Date date) {
        if (statusChanged() && getStatus() == PollStatus.STATUS_DOWN) {
            int cause = sendEvent(createDownEvent(date));
            propagateCause(cause);
            resetStatusChanged();
        } else if (statusChanged() && getStatus() == PollStatus.STATUS_UP) {
            sendEvent(createUpEvent(date));
            int cause = getCause();
            setCause(-1);
            System.err.println("Processing items with cause "+cause);
            generateLingeringMemberEvents(date, cause);

            resetStatusChanged();

        } else if (getStatus() == PollStatus.STATUS_UP) {
            generateMemberEvents(date);
        }
    }


    /**
     * @param dbid
     */
    private void propagateCause(final int cause) {
        PollerVisitor causePropagator = new PollerVisitorAdaptor() {
            public void visitElement(PollerElement elem) {
                if (elem.getCause() < 0)
                    elem.setCause(cause);
            }
        };
        this.visit(causePropagator);
    }

    /**
     * @param date
     */
    public void generateLingeringDownEvents(Date date, int originalCause) {
        if (getStatus() == PollStatus.STATUS_DOWN && getCause() == originalCause) {
            int newCause = sendEvent(createDownEvent(date));
            propagateCause(newCause);
            resetStatusChanged();
        } else if (getStatus() == PollStatus.STATUS_UP && getCause() == originalCause) {
            setCause(-1);
            generateLingeringMemberEvents(date, originalCause);
        } else if (getStatus() == PollStatus.STATUS_UP) {
            sendEvent(createUpEvent(date));
            int newCause = getCause();
            setCause(-1);
            generateLingeringMemberEvents(date, newCause);
        }
    }

    protected void removeMember(String key) {
        m_members.remove(key);
    }

    protected PollerElement findMember(String key) {
        return (PollerElement)m_members.get(key);
    }

    protected void deleteMembers() {
        m_members.clear();
    }

    protected void addMember(String key, PollerElement member) {
        m_members.put(key, member);
    }

    protected Collection getMembers() {
        return m_members.values();
    }

    public synchronized void resetStatusChanged() {
        super.resetStatusChanged();
    
        // Iterate over service list and reset each services's
        // status changed flag
        Iterator it = getMembers().iterator();
        while (it.hasNext()) {
            PollerElement member = (PollerElement) it.next();
            member.resetStatusChanged();
        }
    }
    
    public void visit(PollerVisitor v) {
        super.visit(v);
        v.visitContainer(this);
    }

    /**
     * 
     */
    public void visitMembers(PollerVisitor v) {
        Iterator it = getMembers().iterator();
        while (it.hasNext()) {
            PollerElement member = (PollerElement)it.next();
            member.visit(v);
        }
    }

}
