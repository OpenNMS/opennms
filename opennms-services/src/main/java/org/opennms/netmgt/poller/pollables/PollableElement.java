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

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PollableElement
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public abstract class PollableElement {
    private static final Logger LOG = LoggerFactory.getLogger(PollableElement.class);
    private final Scope m_scope; 

    private volatile PollableContainer m_parent;
    private volatile PollStatus m_status = PollStatus.unknown();
    private volatile boolean m_statusChanged = false;
    private volatile PollEvent m_cause;
    private volatile boolean m_deleted;


    /**
     * <p>Constructor for PollableElement.</p>
     *
     * @param parent a {@link org.opennms.netmgt.poller.pollables.PollableContainer} object.
     * @param scope a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    protected PollableElement(PollableContainer parent, Scope scope) {
        m_parent = parent;
	    if (parent != null) {
	        m_cause = parent.getCause();
	    }
        m_scope = scope;
    }

    /**
     * <p>getParent</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableContainer} object.
     */
    public PollableContainer getParent() {
        return m_parent;
    }
    
    /**
     * <p>setParent</p>
     *
     * @param newParent a {@link org.opennms.netmgt.poller.pollables.PollableContainer} object.
     */
    protected void setParent(PollableContainer newParent) {
        m_parent = newParent;
    }

    /**
     * <p>getScope</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    public Scope getScope() {
        return m_scope;
    }

    /**
     * <p>visit</p>
     *
     * @param v a {@link org.opennms.netmgt.poller.pollables.PollableVisitor} object.
     */
    public void visit(PollableVisitor v) {
        visitThis(v);
    }
    
    /**
     * <p>visitThis</p>
     *
     * @param v a {@link org.opennms.netmgt.poller.pollables.PollableVisitor} object.
     */
    protected void visitThis(PollableVisitor v) {
        v.visitElement(this);
    }
    
    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus getStatus() {
        return m_status;
    }
    private void setStatus(PollStatus status) {
        m_status = status;
    }
    /**
     * <p>isStatusChanged</p>
     *
     * @return a boolean.
     */
    public boolean isStatusChanged() {
        return m_statusChanged;
    }
    private void setStatusChanged(boolean statusChanged) {
        m_statusChanged = statusChanged;
    }
    /**
     * <p>updateStatus</p>
     *
     * @param newStatus a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public void updateStatus(PollStatus newStatus) {
        PollStatus oldStatus = getStatus();
        if (!oldStatus.equals(newStatus)) {
            
            LOG.info("Changing status of PollableElement {} from {} to {}", this, oldStatus, newStatus);
            setStatus(newStatus);
            setStatusChanged(true);
        }
    }
    /**
     * <p>resetStatusChanged</p>
     */
    public void resetStatusChanged() {
        setStatusChanged(false);
    }
    /**
     * <p>recalculateStatus</p>
     */
    public void recalculateStatus() {
        // do nothing for just an element
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollContext} object.
     */
    public abstract PollContext getContext();

    /**
     * <p>doPoll</p>
     *
     * @param elemvrendmunalv02 a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus doPoll(PollableElement elem) {
        if (getParent() == null) {
            resetStatusChanged();
            return poll(elem);
        }
        else
            return getParent().doPoll(elem);
    }
    
    /**
     * <p>getLockRoot</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    protected PollableElement getLockRoot() {
        PollableContainer parent = getParent();
        return (parent == null ? this : parent.getLockRoot());
    }

    /**
     * <p>obtainTreeLock</p>
     */
    protected void obtainTreeLock() {
        getLockRoot().obtainTreeLock();
    }
    
    /**
     * <p>obtainTreeLock</p>
     *
     * @param timeout Lock timeout in milliseconds
     * @throws LockUnavailable 
     */
    protected void obtainTreeLock(long timeout) throws LockUnavailable {
        getLockRoot().obtainTreeLock(timeout);
    }
    
    /**
     * <p>releaseTreeLock</p>
     */
    protected void releaseTreeLock() {
        getLockRoot().releaseTreeLock();
    }

    /**
     * <p>withTreeLock</p>
     *
     * @param r a {@link java.lang.Runnable} object.
     */
    public final void withTreeLock(Runnable r) {
        withTreeLock(Executors.callable(r));
    }
    
    /**
     * <p>withTreeLock</p>
     *
     * @param c a {@link java.util.concurrent.Callable} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected final <T> T withTreeLock(Callable<T> c) {
        try {
            obtainTreeLock();
            return c.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            releaseTreeLock();
        }
    }
    
    
    /**
     * <p>withTreeLock</p>
     *
     * @param r a {@link java.lang.Runnable} object.
     * @param timeout Lock timeout in milliseconds
     * @throws LockUnavailable 
     */
    protected final void withTreeLock(Runnable r, long timeout) throws LockUnavailable {
        withTreeLock(Executors.callable(r), timeout);
    }

    /**
     * <p>withTreeLock</p>
     *
     * @param c a {@link java.util.concurrent.Callable} object.
     * @param timeout Lock timeout in milliseconds
     * @param <T> a T object.
     * @return a T object.
     * @throws LockUnavailable 
     */
    protected final <T> T withTreeLock(Callable<T> c, long timeout) throws LockUnavailable {
        boolean locked = false;
        try {
            obtainTreeLock(timeout);
            locked = true;
            return c.call();
        } catch (LockUnavailable e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) {
                releaseTreeLock();
            }
        }
    }

    

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public abstract PollStatus poll();

    /**
     * <p>poll</p>
     *
     * @param elemvrendmunalv02 a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    protected PollStatus poll(PollableElement elem) {
        if (elem != this)
            throw new IllegalArgumentException("Invalid parameter to poll on "+this+": "+elem);
        
        return poll();
    }

    /**
     * <p>selectPollElement</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    public PollableElement selectPollElement() {
        return this;
    }

    /**
     * <p>createDownEvent</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public abstract Event createDownEvent(Date date);

    /**
     * <p>createUpEvent</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public abstract Event createUpEvent(Date date);

    /**
     * <p>createOutage</p>
     *
     * @param cause TODO
     */
    protected void createOutage(PollEvent cause) {
        setCause(cause);
    }

    /**
     * <p>resolveOutage</p>
     *
     * @param resolution TODO
     */
    protected void resolveOutage(PollEvent resolution) {
        setCause(null);
    }
    
    /**
     * <p>hasOpenOutage</p>
     *
     * @return a boolean.
     */
    public boolean hasOpenOutage() {
        return m_cause != null;
    }
    
    /**
     * <p>setCause</p>
     *
     * @param cause a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public void setCause(PollEvent cause) {
        m_cause = cause;
    }
    
    /**
     * <p>getCause</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public PollEvent getCause() {
        return m_cause;
    }

    /**
     * <p>processStatusChange</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    public void processStatusChange(Date date) {
        if (getStatus().isDown() && isStatusChanged()) {
            processGoingDown(date);
        } else if (getStatus().isUp() && isStatusChanged()) {
            processComingUp(date);
        }
    }

    /**
     * <p>processComingUp</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    protected void processComingUp(Date date) {
        if (getCause() != null) {
            PollEvent resolution = getContext().sendEvent(createUpEvent(date));
            processResolution(getCause(), resolution);
        }
    }

    /**
     * <p>processResolution</p>
     *
     * @param cause a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @param resolution a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected void processResolution(PollEvent cause, PollEvent resolution) {
        resolveOutage(resolution);
    }

    /**
     * <p>processGoingDown</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    protected void processGoingDown(Date date) {
        PollEvent cause = getContext().sendEvent(createDownEvent(date));
        processCause(cause);
    }

    /**
     * <p>processCause</p>
     *
     * @param cause a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected void processCause(PollEvent cause) {
        if (!hasOpenOutage())
            createOutage(cause);
    }

    /**
     * <p>resolveAllOutages</p>
     *
     * @param resolvedCause a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @param resolution a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected void resolveAllOutages(PollEvent resolvedCause, PollEvent resolution) {
        if (resolvedCause.equals(getCause()))
            resolveOutage(resolution);
    }
    
    /**
     * <p>isDeleted</p>
     *
     * @return a boolean.
     */
    public boolean isDeleted() {
        return m_deleted;
    }
    /**
     * <p>delete</p>
     */
    public void delete() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                m_deleted = true;
                if (m_parent != null) {
                    getParent().deleteMember(PollableElement.this);
                    getParent().recalculateStatus();
                }
            }
        };
        withTreeLock(r);
    }

    /**
     * <p>processLingeringCauses</p>
     *
     * @param resolvedCause a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @param resolution a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected void processLingeringCauses(PollEvent resolvedCause, PollEvent resolution) {
        if (isSuspended()) {
            LOG.debug("Element '{}' polling suspended - processing of lingering causes skipped", this.toString());
            return;
        }

        if (getStatus().isDown() && resolvedCause.equals(getCause())) {
            resolveAllOutages(resolvedCause, resolution);
            processGoingDown(resolution.getDate());
        } else if (getStatus().isUp() && resolvedCause.equals(getCause())) {
            processResolution(resolvedCause, resolution);
        } else if (getStatus().isUp() && !resolvedCause.equals(getCause())) {
            processComingUp(resolution.getDate());
        }
    }

    protected abstract boolean isSuspended();

    /**
     * <p>extrapolateCause</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public PollEvent extrapolateCause() {
        return withTreeLock(new Callable<PollEvent>() {
            @Override
            public PollEvent call() throws Exception {
                return doExtrapolateCause();
            }
        });
    }


    /**
     * <p>doExtrapolateCause</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected PollEvent doExtrapolateCause() {
        return getCause();
    }
    
    /**
     * <p>inheritParentalCause</p>
     */
    public void inheritParentalCause() {
        withTreeLock(new Runnable() {

            @Override
            public void run() {
                doInheritParentalCause();
            }
            
        });
    }
    
    /**
     * <p>doInheritParentalCause</p>
     */
    protected void doInheritParentalCause() {
        if (getParent() == null) return;
            
        PollEvent parentalCause = getParent().getCause();
        PollStatus parentalStatus = getParent().getStatus();
        if (parentalCause == null) {
            // parent has no cause so no need to do anything here
            return;
        }
        
        if (getCause() == null || getCause().hasScopeLargerThan(getScope())) { 
            // I have no cause but my parent is down.. mark me as down as well
            // I already have a cause that's larger than myself then inherit as well
            setCause(parentalCause);
            updateStatus(parentalStatus);
        }
        
        
    }
    

}
