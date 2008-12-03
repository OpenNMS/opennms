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
package org.opennms.netmgt.provision.service.tasks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * BaseTask
 *
 * @author brozow
 */
public class Task {
    
    private final DefaultTaskCoordinator m_coordinator;
    private final Runnable m_action;
    
    private static enum State {
        NEW,
        SCHEDULED,
        SUBMITTED,
        COMPLETED
    }

    private final AtomicReference<State> m_state = new AtomicReference<State>(State.NEW);
    
    private final AtomicBoolean m_scheduleCalled = new AtomicBoolean(false);
    private final CountDownLatch m_latch = new CountDownLatch(1);
    
    private final AtomicInteger m_pendingPrereqs = new AtomicInteger(0);
    private final Set<Task> m_dependents = new HashSet<Task>();
    private final Set<Task> m_prerequisites = new HashSet<Task>();
    
    public Task(DefaultTaskCoordinator coordinator) {
        this(coordinator, null);
    }

    public Task(DefaultTaskCoordinator coordinator, Runnable action) {
        m_coordinator = coordinator;
        m_action = action;
    }
    
    public DefaultTaskCoordinator getCoordinator() {
        return m_coordinator;
    }
    
    /**
     * These are final and package protected because they should ONLY be accessed by the TAskCoordinator
     * This is for thread safety and efficiency.  use 'addDependency' to update these.
     */
    final Set<Task> getDependents() {
        return m_dependents;
    }
    
    final void doAddDependent(Task dependent) {
        m_dependents.add(dependent);
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
        }
    }
    
    final void doRemovePrerequisite(Task prereq) {
        m_prerequisites.remove(prereq);
    }
    
    
    final void scheduled() {
        setState(State.NEW, State.SCHEDULED);
    }
    
    private final void setState(State oldState, State newState) {
        if (!m_state.compareAndSet(oldState, newState)) {
            String msg = String.format("Attempted to move to state %s with state not %s (actual value %s) for task %s", newState, oldState, m_state.get(), this);
            new IllegalStateException(msg).printStackTrace();
        } else {
            //System.err.printf("Set state of %s to %s\n", this, newState);
        }
    }
    
    final void submitted() {
        setState(State.SCHEDULED, State.SUBMITTED);
    }
    
    final void completed() {
        m_state.compareAndSet(State.SUBMITTED, State.COMPLETED);
    }
    
    /**
     * These are final and package protected because they should ONLY be accessed by the TaskCoordinator
     * This is for thread safety and efficiency.  use 'addDependency' to update these
     */
    final boolean isReady() {
        return m_state.get() == State.SCHEDULED && m_prerequisites.isEmpty() && m_pendingPrereqs.get() == 0;
    }
    
    final void incrPendingPrereq() {
        m_pendingPrereqs.incrementAndGet();
    }

    final void decrPendingPrereq() {
        m_pendingPrereqs.decrementAndGet();
    }


    /**
     * This method is used by the TaskCoordinator to create runnable that will run this task
     */
    final Runnable getRunnable() {
        return new Runnable() {
          public void run() {
              Task.this.run();
          }
        };
    }
    
    /**
     * Called from execute after the 'body' of the task has completed
     */
    void onComplete() {
        completed();
        m_latch.countDown();
    }
    

    /**
     * This is the run method where the 'work' related to the Task gets down.  This method can be overridden
     * or a Runnable can be passed to the task in the constructor.  The Task is not complete until this method
     * finishes
     */
    public void run() {
        if (m_action != null) {
            m_action.run();
        }
    }
    
    /**
     * This is called to add the task to the queue of tasks that can be considered to be runnable
     */
    public void schedule() {
        m_scheduleCalled.set(true);
        m_coordinator.schedule(this);
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
        m_coordinator.addDependency(prereq, this);
    }
    
    /**
     * Adds dependent as a dependent of this task.  So dependent will not be able to run
     * until this task has been completed.
     */
    public void addDependent(Task dependent) {
        m_coordinator.addDependency(this, dependent);
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

    public String toString() {
        return m_action == null ? super.toString() : m_action.toString();
    }

}
