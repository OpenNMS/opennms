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
 * Represents a PollableContainer 
 *
 * @author brozow
 */
abstract public class PollableContainer extends PollableElement {

    private Map m_members = new HashMap();

    public PollableContainer(PollableContainer parent) {
        super(parent);
    }

    /**
     * @param integer
     * @return
     */
    protected PollableElement getMember(Object key) {
        return (PollableElement)m_members.get(key);
    }

    protected int getMemberCount() {
        return m_members.size();
    }
    
    protected Collection getMembers() {
        return Collections.unmodifiableCollection(m_members.values());
    }
    
    /**
     * @param member
     * @return
     */
    abstract protected Object createMemberKey(PollableElement member);

    /**
     * @param node
     */
    protected void addMember(PollableElement member) {
        Object key = createMemberKey(member);
        m_members.put(key, member);
    }
    
    public void visit(PollableVisitor v) {
        visitThis(v);
        visitMembers(v);
    }
    
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitContainer(this);
    }

    /**
     * @param v
     */
    protected void visitMembers(PollableVisitor v) {
        for (Iterator it = getMembers().iterator(); it.hasNext();) {
            PollableElement element = (PollableElement) it.next();
            element.visit(v);
        }
        
    }
    
    protected interface Iter {
        public void forEachElement(PollableElement element);
    }
    
    abstract protected class SimpleIter implements Iter {
        private Object result;
        public SimpleIter(Object initial) { result = initial; }
        public SimpleIter() { this(null); }
        public Object getResult() { return result; }
        public void setResult(Object newResult) { result = newResult; }
    }
    
    protected void forEachMember(Iter iter) {
        for (Iterator it = getMembers().iterator(); it.hasNext(); ) {
            PollableElement element = (PollableElement) it.next();
            iter.forEachElement(element);
        }
    }
    
    public void recalculateStatus() {
        SimpleIter iter = new SimpleIter(PollStatus.STATUS_DOWN) {
            public void forEachElement(PollableElement elem) {
                elem.recalculateStatus();
                if (elem.getStatus().isUp())
                    setResult(PollStatus.STATUS_UP);
            }
        };
        forEachMember(iter);
        updateStatus((PollStatus)iter.getResult());
    }
    
    public void resetStatusChanged() {
        super.resetStatusChanged();
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.resetStatusChanged();
            }
        };
        forEachMember(iter);
    }
    
    PollableElement findMemberWithDescendent(PollableElement elem) {
        PollableElement member = elem;
        while (member != null && member.getParent() != this) {
            member = member.getParent();
        }
        return member;
    }


    protected PollStatus poll(PollableElement elem) {
        PollableElement member = findMemberWithDescendent(elem);
        PollStatus memberStatus = member.poll(elem);
        if (memberStatus != getStatus() && member.isStatusChanged()) {
            updateStatus(pollRemainingMembers(member));
        }
        return getStatus();
    }

    /**
     * @param member
     * @return
     */
    public PollStatus pollRemainingMembers(final PollableElement member) {
        SimpleIter iter = new SimpleIter(member.getStatus()) {
            public void forEachElement(PollableElement elem) {
                if (elem != member) {
                    if (elem.poll().isUp())
                        setResult(PollStatus.STATUS_UP);
                }
            }
        };
        forEachMember(iter);
        return (PollStatus)iter.getResult();
    }
    
    public PollStatus getMemberStatus() {
        SimpleIter iter = new SimpleIter(PollStatus.STATUS_DOWN) {
            public void forEachElement(PollableElement elem) {
                if (elem.getStatus().isUp())
                    setResult(PollStatus.STATUS_UP);
            }
            
        };
        forEachMember(iter);
        return (PollStatus)iter.getResult();
    }
    
    public PollStatus poll() {
        PollableElement leaf = selectPollElement();
        if (leaf == null) return PollStatus.STATUS_UP;
        return poll(leaf);
    }

    /**
     * @return
     */
    public PollableElement selectPollElement() {
        if (getMemberCount() == 0) 
            return null;

        PollableElement member = (PollableElement)getMembers().iterator().next();
        return member.selectPollElement();
            
    }
    public void processStatusChange(Date date) {
        if (isStatusChanged()) {
            super.processStatusChange(date);
        } else if (getStatus().isUp()) {
            processMemberStatusChanges(date);
        }
        
    }

    public void processMemberStatusChanges(final Date date) {
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.processStatusChange(date);
            }
            
        };
        forEachMember(iter);
    }
    
    
    protected void processComingUp(Date date) {
        super.processComingUp(date);
        processMemberLingeringStatusChanges(date);
    }
    /**
     * @param date
     */
    private void processMemberLingeringStatusChanges(final Date date) {
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.processLingeringStatusChanges(date);
            }
            
        };
        forEachMember(iter);
        
    }

    protected void processGoingDown(Date date) {
        super.processGoingDown(date);
    }
    public void processLingeringStatusChanges(Date date) {
        super.processLingeringStatusChanges(date);
        if (getStatus().isUp())
            processMemberLingeringStatusChanges(date);
    }
}
