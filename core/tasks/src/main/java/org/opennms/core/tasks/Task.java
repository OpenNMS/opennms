/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.tasks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.core.utils.ThreadCategory;

/**
 * BaseTask
 *
 * @author brozow
 */
public abstract class Task {
    
    private static enum State {
        NEW,
        SCHEDULED,
        SUBMITTED, 
        COMPLETED
    }
    
    private final DefaultTaskCoordinator m_coordinator;
    private final AtomicReference<State> m_state = new AtomicReference<State>(State.NEW);
    
    private final AtomicBoolean m_scheduleCalled = new AtomicBoolean(false);
    private final CountDownLatch m_latch = new CountDownLatch(1);
    
    private final AtomicInteger m_pendingPrereqs = new AtomicInteger(0);
    private final Set<Task> m_dependents = new HashSet<Task>();
    private final Set<Task> m_prerequisites = new HashSet<Task>();
    
    private final TaskMonitor m_monitor;
    
    public Task(DefaultTaskCoordinator coordinator, ContainerTask parent) {
        m_coordinator = coordinator;
        m_monitor = parent != null 
            ? parent.getMonitor().getChildTaskMonitor(parent, this) 
            : new DefaultTaskMonitor(this);
        
    }
    
    public DefaultTaskCoordinator getCoordinator() {
        return m_coordinator;
    }
    
    public TaskMonitor getMonitor() {
        return m_monitor;
    }
    
    /**
     * These are final and package protected because they should ONLY be accessed by the TaskCoordinator
     * This is for thread safety and efficiency.  use 'addDependency' to update these.
     */
    final Set<Task> getDependents() {
        return m_dependents;
    }
    
    final void doAddDependent(Task dependent) {
        if (!isFinished()) {
            m_dependents.add(dependent);
        }
    }
    
    /**
     * These are final and package protected because they should ONLY be accessed by the TAskCoordinator
     * This is for thread safety and efficiency.  use 'addDependency' to update these
     */
    final Set<Task> getPrerequisites() {
        return m_prerequisites;
    }
    
    final void doAddPrerequisite(Task prereq) {
        if (!prereq.isFinished()) {
            m_prerequisites.add(prereq);
            notifyPrerequisteAdded(prereq);
        }
    }

    private void notifyPrerequisteAdded(Task prereq) {
        try {
            m_monitor.prerequisiteAdded(this, prereq);
        } catch (Throwable t) {
            m_monitor.monitorException(t);
        }
    }
    
    private void notifyPrerequisteCompleted(Task prereq) {
        try {
            m_monitor.prerequisiteCompleted(this, prereq);
        } catch (Throwable t) {
            m_monitor.monitorException(t);
        }
    }
    
    private void notifyScheduled() {
        try {
            m_monitor.scheduled(this);
        } catch (Throwable t) {
            m_monitor.monitorException(t);
        }
    }

    private void notifySubmitted() {
        try {
            m_monitor.submitted(this);
        } catch (Throwable t) {
            m_monitor.monitorException(t);
        }
    }
    
    private void notifyCompleted() {
        try {
            m_monitor.completed(this);
        } catch (Throwable t) {
            m_monitor.monitorException(t);
        }
    }
        
    final void doCompletePrerequisite(Task prereq) {
        m_prerequisites.remove(prereq);
        notifyPrerequisteCompleted(prereq);
    }
    
    final void clearDependents() {
        m_dependents.clear();
    }

 
    final void scheduled() {
        setState(State.NEW, State.SCHEDULED);
        notifyScheduled();
    }
    
    private final void setState(State oldState, State newState) {
        if (!m_state.compareAndSet(oldState, newState)) {
            String msg = String.format("Attempted to move to state %s with state not %s (actual value %s) for task %s", newState, oldState, m_state.get(), this);
            new IllegalStateException(msg).printStackTrace();
        } else {
            //System.err.printf("Set state of %s to %s\n", this, newState);
        }
    }
    
    void submitIfReady() {
        if (isReady()) {
            doSubmit();
            submitted();
            completeSubmit();
        }
    }

    /**
     * This method submits a task to be executed and is called when all dependencies are completed for that task
     * This method should place a runnable on an executor or submit the task in some other way so that it will
     * run as soon as possible.  Tasks that have no processing to be done may override completeSubmit to notify
     * the Task coordinator that the task is done.
     */
    protected void doSubmit() {
    }

    final void submitted() {
        setState(State.SCHEDULED, State.SUBMITTED);
        notifySubmitted();
    }

    /**
     * This method exists to allow a task to have no processing
     */
    protected void completeSubmit() {
    }

    
    final void completed() {
        m_state.compareAndSet(State.SUBMITTED, State.COMPLETED);
        notifyCompleted();
    }
    
    /**
     * These are final and package protected because they should ONLY be accessed by the TaskCoordinator
     * This is for thread safety and efficiency.  use 'addDependency' to update these
     */
    final boolean isReady() {
        return isInReadyState() && m_prerequisites.isEmpty() && getPendingPrereqCount() == 0;
    }

    private int getPendingPrereqCount() {
        return m_pendingPrereqs.get();
    }

    private boolean isInReadyState() {
        return m_state.get() == State.SCHEDULED;
    }
    
    final void incrPendingPrereqCount() {
        m_pendingPrereqs.incrementAndGet();
    }

    final void decrPendingPrereqCount() {
        m_pendingPrereqs.decrementAndGet();
    }


    /**
     * Called from execute after the 'body' of the task has completed
     */
    void onComplete() {
        completed();
        m_latch.countDown();
    }
    

    /**
     * This is called to add the task to the queue of tasks that can be considered to be runnable
     */
    public void schedule() {
        m_scheduleCalled.set(true);
        preSchedule();
        getCoordinator().schedule(this);
        postSchedule();
    }
        
    protected void preSchedule() {
    }

    protected void postSchedule() {
    }

    /**
     * This task's run method has completed
     */
    public boolean isFinished() {
        return m_state.get() == State.COMPLETED;
    }
    
    /**
     * This task has be sent to the TaskCoordinator to be run
     */
    public boolean isScheduled() {
        return m_state.get() != State.NEW || m_scheduleCalled.get();
    }
    
    /**
     * Add's prereq as a Prerequisite of this task. In other words... this taks cannot run
     * until prereq was been complted.
     */
    public void addPrerequisite(Task prereq) {
        getCoordinator().addDependency(prereq, this);
    }
    
    /**
     * Adds dependent as a dependent of this task.  So dependent will not be able to run
     * until this task has been completed.
     */
    public void addDependent(Task dependent) {
        getCoordinator().addDependency(this, dependent);
    }

    /**
     * Wait for this task to complete.  The current thread will block until this task has been completed.
     */
    public void waitFor() throws InterruptedException, ExecutionException {
        m_latch.await();
    }

    /**
     * Wait for this task to complete or until a timeout occurs
     */
    public void waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        m_latch.await(timeout, unit);
    }
    
    protected void markTaskAsCompleted() {
        getCoordinator().markTaskAsCompleted(this);
    }

    protected void submitRunnable(Runnable runnable, String preferredExecutor) {
        getCoordinator().submitToExecutor(preferredExecutor, runnable, this);
    }
    
    public String toString() {
        return String.format("Task[%s]", super.toString());
    }

    protected void info(String format, Object... args) {
        log().info(String.format(format, args));
    }

    protected void debug(String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args));
        }
    }

    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
