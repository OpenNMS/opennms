/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.pollables;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.opennms.core.utils.AsyncReentrantLock;
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
    public void resetStatusChanged(AsyncReentrantLock.Locker locker) {
        setStatusChanged(false);
    }
    /**
     * <p>recalculateStatus</p>
     */
    public void recalculateStatus(AsyncReentrantLock.Locker locker) {
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
    public CompletionStage<PollStatus> doPoll(AsyncReentrantLock.Locker locker, PollableElement elem) {
        if (getParent() == null) {
            resetStatusChanged(locker);
            return poll(locker, elem);
        }
        else
            return getParent().doPoll(locker, elem);
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
    protected void obtainTreeLock(AsyncReentrantLock.Locker locker) {
        getLockRoot().obtainTreeLock(locker);
    }
    
    /**
     * <p>obtainTreeLock</p>
     *
     * @param timeout Lock timeout in milliseconds
     * @throws LockUnavailable 
     */
    protected void obtainTreeLock(AsyncReentrantLock.Locker locker, long timeout) throws LockUnavailable {
        getLockRoot().obtainTreeLock(locker, timeout);
    }
    
    /**
     * <p>releaseTreeLock</p>
     */
    protected void releaseTreeLock(AsyncReentrantLock.Locker locker) {
        getLockRoot().releaseTreeLock(locker);
    }

    /**
     * <p>withTreeLock</p>
     *
     * @param r a {@link java.lang.Runnable} object.
     */
    public final void withTreeLock(AsyncReentrantLock.Locker locker, Runnable r) {
        withTreeLock(locker, Executors.callable(r));
    }

    protected final <T> T withTreeLock(AsyncReentrantLock.Locker locker, Callable<T> c) {
        obtainTreeLock(locker);
        try {
            return c.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            releaseTreeLock(locker);
        }
    }

    /**
     * <p>withTreeLock</p>
     *
     * @param supplier a {@link java.util.concurrent.Callable} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected final <T> CompletionStage<T> withAsyncTreeLock(AsyncReentrantLock.Locker locker, Supplier<CompletionStage<T>> supplier) {
        obtainTreeLock(locker);
        try {
            return supplier.get().whenComplete((v, t) -> releaseTreeLock(locker));
        } catch (RuntimeException | Error t) {
            LOG.error("could not create CompletionStage", t);
            releaseTreeLock(locker);
            throw t;
        }
    }

    protected final <T> CompletionStage<T> withAsyncTreeLock(AsyncReentrantLock.Locker locker, Supplier<CompletionStage<T>> supplier, long timeout) throws LockUnavailable {
        obtainTreeLock(locker, timeout);
        try {
            return supplier.get().whenComplete((v, t) -> releaseTreeLock(locker));
        } catch (RuntimeException | Error t) {
            LOG.error("could not create CompletionStage", t);
            releaseTreeLock(locker);
            throw t;
        }
    }

    /**
     * <p>withTreeLock</p>
     *
     * @param r a {@link java.lang.Runnable} object.
     * @param timeout Lock timeout in milliseconds
     * @throws LockUnavailable 
     */
    protected final void withTreeLock(AsyncReentrantLock.Locker locker, Runnable r, long timeout) throws LockUnavailable {
        withTreeLock(locker, Executors.callable(r), timeout);
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
    protected final <T> T withTreeLock(AsyncReentrantLock.Locker locker, Callable<T> c, long timeout) throws LockUnavailable {
        boolean locked = false;
        try {
            obtainTreeLock(locker, timeout);
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
                releaseTreeLock(locker);
            }
        }
    }

    

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public abstract CompletionStage<PollStatus> poll(AsyncReentrantLock.Locker locker);

    /**
     * <p>poll</p>
     *
     * @param elemvrendmunalv02 a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    protected CompletionStage<PollStatus> poll(AsyncReentrantLock.Locker locker, PollableElement elem) {
        if (elem != this)
            throw new IllegalArgumentException("Invalid parameter to poll on "+this+": "+elem);
        
        return poll(locker);
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
    public void processStatusChange(AsyncReentrantLock.Locker locker, Date date) {
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
    public void delete(AsyncReentrantLock.Locker locker) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                m_deleted = true;
                if (m_parent != null) {
                    getParent().deleteMember(locker, PollableElement.this);
                    getParent().recalculateStatus(locker);
                }
            }
        };
        withTreeLock(locker, r);
    }

    /**
     * <p>processLingeringCauses</p>
     *
     * @param resolvedCause a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @param resolution a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected void processLingeringCauses(PollEvent resolvedCause, PollEvent resolution) {
        if (getStatus().isDown() && resolvedCause.equals(getCause())) {
            resolveAllOutages(resolvedCause, resolution);
            processGoingDown(resolution.getDate());
        } else if (getStatus().isUp() && resolvedCause.equals(getCause())) {
            processResolution(resolvedCause, resolution);
        } else if (getStatus().isUp() && !resolvedCause.equals(getCause())) {
            processComingUp(resolution.getDate());
        }
    }

    /**
     * <p>extrapolateCause</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public PollEvent extrapolateCause(AsyncReentrantLock.Locker locker) {
        return withTreeLock(locker, new Callable<PollEvent>() {
            @Override
            public PollEvent call() throws Exception {
                return doExtrapolateCause(locker);
            }
        });
    }


    /**
     * <p>doExtrapolateCause</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    protected PollEvent doExtrapolateCause(AsyncReentrantLock.Locker locker) {
        return getCause();
    }
    
    /**
     * <p>inheritParentalCause</p>
     */
    public void inheritParentalCause(AsyncReentrantLock.Locker locker) {
        withTreeLock(locker, new Runnable() {

            @Override
            public void run() {
                doInheritParentalCause(locker);
            }
            
        });
    }
    
    /**
     * <p>doInheritParentalCause</p>
     */
    protected void doInheritParentalCause(AsyncReentrantLock.Locker locker) {
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
