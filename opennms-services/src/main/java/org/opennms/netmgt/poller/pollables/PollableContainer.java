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
package org.opennms.netmgt.poller.pollables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.netmgt.poller.PollStatus;



/**
 * Represents a PollableContainer
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
abstract public class PollableContainer extends PollableElement {

    private final Map<Object, PollableElement> m_members = new HashMap<Object, PollableElement>();

    /**
     * <p>Constructor for PollableContainer.</p>
     *
     * @param parent a {@link org.opennms.netmgt.poller.pollables.PollableContainer} object.
     * @param scope a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    public PollableContainer(PollableContainer parent, Scope scope) {
        super(parent, scope);
    }

    @Override
    protected boolean isSuspended() {
        if (getMembers().size() == 0) {
            return false;
        } else {
            for (final PollableElement pollableElement : getMembers()) {
                if (!pollableElement.isSuspended()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * <p>getMember</p>
     *
     * @param key a {@link java.lang.Object} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    protected synchronized PollableElement getMember(Object key) {
        return m_members.get(key);
    }

    /**
     * <p>getMemberCount</p>
     *
     * @return a int.
     */
    protected synchronized int getMemberCount() {
        return m_members.size();
    }
    
    /**
     * <p>getMembers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    protected synchronized Collection<PollableElement> getMembers() {
        return new ArrayList<PollableElement>(m_members.values());
    }
    
    /**
     * <p>createMemberKey</p>
     *
     * @param member a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     * @return a {@link java.lang.Object} object.
     */
    protected abstract Object createMemberKey(PollableElement member);

    /**
     * <p>addMember</p>
     *
     * @param member a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    protected synchronized void addMember(PollableElement member) {
        Object key = createMemberKey(member);
        m_members.put(key, member);
    }
    
    /**
     * <p>removeMember</p>
     *
     * @param member a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    public synchronized void removeMember(PollableElement member) {
        Object key = createMemberKey(member);
        m_members.remove(key);
    }

    
    /**
     * <p>deleteMember</p>
     *
     * @param member a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    public void deleteMember(PollableElement member) {
        removeMember(member);
        if (m_members.size() == 0)
            this.delete();
    }
    
    /**
     * <p>delete</p>
     */
    @Override
    public void delete() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Collection<PollableElement> members = getMembers();
                for (Iterator<PollableElement> it = members.iterator(); it.hasNext();) {
                    PollableElement member = it.next();
                    member.delete();
                }
                PollableContainer.super.delete();
            }
        };
        withTreeLock(r);
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void visit(PollableVisitor v) {
        visitThis(v);
        visitMembers(v);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitContainer(this);
    }

    /**
     * <p>visitMembers</p>
     *
     * @param v a {@link org.opennms.netmgt.poller.pollables.PollableVisitor} object.
     */
    protected void visitMembers(PollableVisitor v) {
        for (Iterator<PollableElement> it = getMembers().iterator(); it.hasNext();) {
            PollableElement element = it.next();
            element.visit(v);
        }
        
    }
    
    protected interface Iter {
        public void forEachElement(PollableElement element);
    }
    
    protected abstract class SimpleIter<T> implements Iter {
        private T result;
        public SimpleIter(T initial) { result = initial; }
        public SimpleIter() { this(null); }
        public T getResult() { return result; }
        public void setResult(T newResult) { result = newResult; }
    }
    
    protected abstract class Accumulator<T> extends SimpleIter<T> {
        public Accumulator(T initial) { super(initial); }
        public Accumulator() { super(null); }
        @Override
        public void forEachElement(PollableElement element) {
            setResult(processNextMember(element, getResult()));
        }
        abstract T processNextMember(PollableElement member, T currentValue);
    }
    
    
    
    /**
     * <p>forEachMember</p>
     *
     * @param iter a {@link org.opennms.netmgt.poller.pollables.PollableContainer.Iter} object.
     */
    protected void forEachMember(Iter iter) {
        forEachMember(false, iter);
    }
    
    /**
     * <p>deriveValueFromMembers</p>
     *
     * @param iter a {@link org.opennms.netmgt.poller.pollables.PollableContainer.SimpleIter} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected <T> T deriveValueFromMembers(SimpleIter<T> iter) {
        return deriveValueFromMembers(false, iter);
    }
    
    /**
     * <p>deriveValueFromMembers</p>
     *
     * @param withTreeLock a boolean.
     * @param iter a {@link org.opennms.netmgt.poller.pollables.PollableContainer.SimpleIter} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected <T> T deriveValueFromMembers(boolean withTreeLock, SimpleIter<T> iter) {
        forEachMember(withTreeLock, iter);
        return iter.getResult();
    }
    
    /**
     * <p>forEachMember</p>
     *
     * @param withTreeLock a boolean.
     * @param iter a {@link org.opennms.netmgt.poller.pollables.PollableContainer.Iter} object.
     */
    protected void forEachMember(boolean withTreeLock, final Iter iter) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Iterator<PollableElement> it = getMembers().iterator(); it.hasNext(); ) {
                    PollableElement element = it.next();
                    iter.forEachElement(element);
                }
            }
        };
        
        if (withTreeLock) {
            withTreeLock(r);
        } else {
            r.run();
        }
    }
    
    /**
     * <p>recalculateStatus</p>
     */
    @Override
    public void recalculateStatus() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                SimpleIter<PollStatus> iter = new SimpleIter<PollStatus>(PollStatus.down()) {
                    @Override
                    public void forEachElement(PollableElement elem) {
                        elem.recalculateStatus();
                        if (elem.getStatus().isUp())
                            setResult(PollStatus.up());
                    }
                };
                forEachMember(iter);
                updateStatus(iter.getResult());
            }
        };
        withTreeLock(r);
    }
    
    /**
     * <p>resetStatusChanged</p>
     */
    @Override
    public void resetStatusChanged() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                PollableContainer.super.resetStatusChanged();
                Iter iter = new Iter() {
                    @Override
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


    /** {@inheritDoc} */
    @Override
    protected PollStatus poll(final PollableElement elem) {
        final PollStatus[] retVal = new PollStatus[1];
        Runnable r = new Runnable() {
            @Override
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
     * <p>pollRemainingMembers</p>
     *
     * @param member a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus pollRemainingMembers(final PollableElement member) {
        SimpleIter<PollStatus> iter = new SimpleIter<PollStatus>(member.getStatus()) {
            @Override
            public void forEachElement(PollableElement elem) {
                if (elem != member) {
                    if (elem.poll().isUp())
                        setResult(PollStatus.up());
                }
            }
        };
        forEachMember(iter);
        return iter.getResult();
    }
    
    /**
     * <p>getMemberStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus getMemberStatus() {
        SimpleIter<PollStatus> iter = new SimpleIter<PollStatus>(PollStatus.down()) {
            @Override
            public void forEachElement(PollableElement elem) {
                if (elem.getStatus().isUp())
                    setResult(PollStatus.up());
            }
            
        };
        forEachMember(iter);
        return iter.getResult();
    }
    
    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Override
    public PollStatus poll() {
        PollableElement leaf = selectPollElement();
        if (leaf == null) return PollStatus.up();
        return poll(leaf);
    }

    /**
     * <p>selectPollElement</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    @Override
    public PollableElement selectPollElement() {
        if (getMemberCount() == 0) 
            return null;

        PollableElement member = (PollableElement)getMembers().iterator().next();
        return member.selectPollElement();
            
    }

    /** {@inheritDoc} */
    @Override
    public void processStatusChange(final Date date) {
        Runnable r = new Runnable() {
            @Override
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

    /**
     * <p>processMemberStatusChanges</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    public void processMemberStatusChanges(final Date date) {
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.processStatusChange(date);
            }
            
        };
        forEachMember(iter);
    }

    
    
    /** {@inheritDoc} */
    @Override
    protected void processResolution(PollEvent resolvedCause, PollEvent resolution) {
        super.processResolution(resolvedCause, resolution);
        processLingeringMemberCauses(resolvedCause, resolution);
    }

    private void processLingeringMemberCauses(final PollEvent resolvedCause, final PollEvent resolution) {
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.processLingeringCauses(resolvedCause, resolution);
            }
            
        };
        forEachMember(iter);
    }
    
    
    /** {@inheritDoc} */
    @Override
    protected void processCause(final PollEvent cause) {
        super.processCause(cause);
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.processCause(cause);
            }
            
        };
        forEachMember(iter);
    }
    
    
    /** {@inheritDoc} */
    @Override
    protected void resolveAllOutages(final PollEvent resolvedCause, final PollEvent resolution) {
        super.resolveAllOutages(resolvedCause, resolution);
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                if (!hasOpenOutage())
                    elem.resolveAllOutages(resolvedCause, resolution);
            }
            
        };
        forEachMember(iter);
    }

    /** {@inheritDoc} */
    @Override
    protected PollEvent doExtrapolateCause() {

        // find the member cause with the largest scope
        PollEvent cause = extrapolateMemberCauseWithLargestScope();

        // use this largest scope as the cause for the container
        setCause(cause);
        if (cause != null) {
            // we must be down set set status appropriately
            updateStatus(PollStatus.down());
        }
        
        // return the cause to parent container
        return cause;
    }

    private PollEvent extrapolateMemberCauseWithLargestScope() {
        PollEvent cause = null;
        for(PollableElement member : getMembers()) {
            PollEvent memberCause = member.extrapolateCause();
            if (memberCause != null && !memberCause.hasScopeSmallerThan(getScope())) {
                // a cause has been found that exceeds the scope of the members
                // choose between the current scope and the newly found scope be taking
                // the cause with the largest scope
                cause = PollEvent.withLargestScope(cause, memberCause);
                
            }
        }
        return cause;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInheritParentalCause() {
        super.doInheritParentalCause();
        for(PollableElement member : getMembers()) {
            member.inheritParentalCause();
        }
        
    }
    
}
