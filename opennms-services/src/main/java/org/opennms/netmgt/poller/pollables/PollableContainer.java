/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created December 31, 2004
 *
 * Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.pollables;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;



/**
 * Represents a PollableContainer 
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
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
    protected synchronized PollableElement getMember(Object key) {
        return (PollableElement)m_members.get(key);
    }

    protected synchronized int getMemberCount() {
        return m_members.size();
    }
    
    protected synchronized Collection getMembers() {
        return new LinkedList(m_members.values());
    }
    
    /**
     * @param member
     * @return
     */
    abstract protected Object createMemberKey(PollableElement member);

    /**
     * @param node
     */
    protected synchronized void addMember(PollableElement member) {
        Object key = createMemberKey(member);
        m_members.put(key, member);
    }
    
    public synchronized void removeMember(PollableElement member) {
        Object key = createMemberKey(member);
        m_members.remove(key);
    }

    
    public void deleteMember(PollableElement member) {
        removeMember(member);
        if (m_members.size() == 0)
            this.delete();
    }
    
    public void delete() {
        Runnable r = new Runnable() {
            public void run() {
                Collection members = getMembers();
                for (Iterator it = members.iterator(); it.hasNext();) {
                    PollableElement member = (PollableElement) it.next();
                    member.delete();
                }
                PollableContainer.super.delete();
            }
        };
        withTreeLock(r);
        
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
        Runnable r = new Runnable() {
            public void run() {
                SimpleIter iter = new SimpleIter(PollStatus.down()) {
                    public void forEachElement(PollableElement elem) {
                        elem.recalculateStatus();
                        if (elem.getStatus().isUp())
                            setResult(PollStatus.up());
                    }
                };
                forEachMember(iter);
                updateStatus((PollStatus)iter.getResult());
            }
        };
        withTreeLock(r);
    }
    
    public void resetStatusChanged() {
        Runnable r = new Runnable() {
            public void run() {
                PollableContainer.super.resetStatusChanged();
                Iter iter = new Iter() {
                    public void forEachElement(PollableElement elem) {
                        elem.resetStatusChanged();
                    }
                };
                forEachMember(iter);
            }
        };
        withTreeLock(r);
    }
    
    PollableElement findMemberWithDescendent(PollableElement elem) {
        PollableElement member = elem;
        while (member != null && member.getParent() != this) {
            member = member.getParent();
        }
        return member;
    }


    protected PollStatus poll(final PollableElement elem) {
        final PollStatus retVal[] = new PollStatus[1];
        Runnable r = new Runnable() {
            public void run() {
                PollableElement member = findMemberWithDescendent(elem);
                PollStatus memberStatus = member.poll(elem);
                if (memberStatus.isUp() != getStatus().isUp() && member.isStatusChanged()) {
                    updateStatus(pollRemainingMembers(member));
                }
                retVal[0] = getStatus();
            }
        };
        elem.withTreeLock(r);
        return retVal[0];
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
                        setResult(PollStatus.up());
                }
            }
        };
        forEachMember(iter);
        return (PollStatus)iter.getResult();
    }
    
    public PollStatus getMemberStatus() {
        SimpleIter iter = new SimpleIter(PollStatus.down()) {
            public void forEachElement(PollableElement elem) {
                if (elem.getStatus().isUp())
                    setResult(PollStatus.up());
            }
            
        };
        forEachMember(iter);
        return (PollStatus)iter.getResult();
    }
    
    public PollStatus poll() {
        PollableElement leaf = selectPollElement();
        if (leaf == null) return PollStatus.up();
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

    public void processStatusChange(final Date date) {
        Runnable r = new Runnable() {
            public void run() {
                if (isStatusChanged()) {
                    PollableContainer.super.processStatusChange(date);
                } else if (getStatus().isUp()) {
                    processMemberStatusChanges(date);
                }
            }
        };
        withTreeLock(r);
        
    }

    public void processMemberStatusChanges(final Date date) {
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.processStatusChange(date);
            }
            
        };
        forEachMember(iter);
    }

    
    
    protected void processResolution(PollEvent resolvedCause, PollEvent resolution) {
        super.processResolution(resolvedCause, resolution);
        processLingeringMemberCauses(resolvedCause, resolution);
    }

    private void processLingeringMemberCauses(final PollEvent resolvedCause, final PollEvent resolution) {
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.processLingeringCauses(resolvedCause, resolution);
            }
            
        };
        forEachMember(iter);
    }
    
    
    protected void processCause(final PollEvent cause) {
        super.processCause(cause);
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.processCause(cause);
            }
            
        };
        forEachMember(iter);
    }
    
    
    protected void resolveAllOutages(final PollEvent resolvedCause, final PollEvent resolution) {
        super.resolveAllOutages(resolvedCause, resolution);
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                if (!hasOpenOutage())
                    elem.resolveAllOutages(resolvedCause, resolution);
            }
            
        };
        forEachMember(iter);
    }

}
