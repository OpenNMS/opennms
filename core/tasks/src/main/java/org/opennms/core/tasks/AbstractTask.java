/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.tasks;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractTask
 *
 * - Currently a collection of 'dependency' tasks defines the set of tasks that are 
 *   dependents of another.
 * - When a task completes, the set of dependency tasks are the ones that need to 
 *   be considered to be run.
 * - When a task is considered, it removes the just-completed prerequisite task from 
 *   its 'prerequisites' list i.e. the set of tasks that must complete before it can run.
 * - If the set of prerequisites for the task becomes empty, then the task can be run.
 * 
 * @author Seth
 * @author brozow
 */
public abstract class AbstractTask implements Task {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTask.class);

    private final TaskCoordinator m_coordinator;
    private final AtomicReference<State> m_state = new AtomicReference<State>(State.NEW);
    
    private final AtomicBoolean m_scheduleCalled = new AtomicBoolean(false);
    private final CountDownLatch m_latch = new CountDownLatch(1);
    
    private final AtomicInteger m_pendingPrereqs = new AtomicInteger(0);
    private final Set<AbstractTask> m_dependents = new CopyOnWriteArraySet<>();
    private final Set<AbstractTask> m_prerequisites = new CopyOnWriteArraySet<>();
    
    private final TaskMonitor m_monitor;
    
    /**
     * <p>Constructor for Task.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     */
    public AbstractTask(TaskCoordinator coordinator, ContainerTask<?> parent) {
        m_coordinator = coordinator;
        m_monitor = parent != null 
            ? parent.getMonitor().getChildTaskMonitor(parent, this) 
            : new DefaultTaskMonitor(this);
        
    }
    
    /**
     * <p>getCoordinator</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskCoordinator} object.
     */
    @Override
    public final TaskCoordinator getCoordinator() {
        return m_coordinator;
    }
    
    /**
     * <p>getMonitor</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskMonitor} object.
     */
    @Override
    public final TaskMonitor getMonitor() {
        return m_monitor;
    }
    
    /**
     * These are final and package protected because they should ONLY be accessed by the TaskCoordinator
     * This is for thread safety and efficiency.  use 'addDependency' to update these.
     */
    final Set<AbstractTask> getDependents() {
        return m_dependents;
    }
    
    final void doAddDependent(final AbstractTask dependent) {
        if (!isFinished()) {
            m_dependents.add(dependent);
        }
    }
    
    final void doAddPrerequisite(final AbstractTask prereq) {
        if (!prereq.isFinished()) {
            m_prerequisites.add(prereq);
            notifyPrerequisiteAdded(prereq);
        }
    }

    private final void notifyPrerequisiteAdded(final AbstractTask prereq) {
        try {
            m_monitor.prerequisiteAdded(this, prereq);
        } catch (final Throwable t) {
            m_monitor.monitorException(t);
        }
    }
    
    private final void notifyPrerequisiteCompleted(final AbstractTask prereq) {
        try {
            m_monitor.prerequisiteCompleted(this, prereq);
        } catch (final Throwable t) {
            m_monitor.monitorException(t);
        }
    }
    
    private final void notifyScheduled() {
        try {
            m_monitor.scheduled(this);
        } catch (final Throwable t) {
            m_monitor.monitorException(t);
        }
    }

    private final void notifySubmitted() {
        try {
            m_monitor.submitted(this);
        } catch (final Throwable t) {
            m_monitor.monitorException(t);
        }
    }
    
    private final void notifyCompleted() {
        try {
            m_monitor.completed(this);
        } catch (final Throwable t) {
            m_monitor.monitorException(t);
        }
    }
        
    final void doCompletePrerequisite(final AbstractTask prereq) {
        m_prerequisites.remove(prereq);
        notifyPrerequisiteCompleted(prereq);
    }
    
    final void clearDependents() {
        m_dependents.clear();
    }


    final void scheduled() {
        setState(State.NEW, State.SCHEDULED);
        notifyScheduled();
    }
    
    private final void setState(final State oldState, final State newState) {
        if (!m_state.compareAndSet(oldState, newState)) {
        	LOG.debug("Attempted to move to state {} with state not {} (actual value {})", newState, oldState, m_state.get());
        } else {
        	LOG.trace("Set state to {}", newState);
        }
    }
    
    final void submitIfReady() {
        if (isReady()) {
            try {
                doSubmit();
            } catch (Throwable e) {
                LOG.error("Unexpected throwable while trying to submit task: " + this, e);
            } finally {
                submitted();
                completeSubmit();
            }
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

    private final void submitted() {
        setState(State.SCHEDULED, State.SUBMITTED);
        notifySubmitted();
    }

    /**
     * This method exists to allow a task to have no processing
     */
    protected void completeSubmit() {
    }

    
    private final void completed() {
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

    private final int getPendingPrereqCount() {
        return m_pendingPrereqs.get();
    }

    private final boolean isInReadyState() {
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
    final void onComplete() {
        completed();
        m_latch.countDown();
    }
    

    /**
     * This is called to add the task to the queue of tasks that can be considered to be runnable
     */
    @Override
    public final void schedule() {
        m_scheduleCalled.set(true);
        try {
            preSchedule();
        } catch (Throwable e) {
            LOG.error("preSchedule() failed for task " + this, e);
        }
        getCoordinator().schedule(this);
        try {
            postSchedule();
        } catch (Throwable e) {
            LOG.error("postSchedule() failed for task " + this, e);
        }
    }
        
    /**
     * <p>preSchedule</p>
     */
    protected void preSchedule() {
    }

    /**
     * <p>postSchedule</p>
     */
    protected void postSchedule() {
    }

    /**
     * This task's run method has completed
     *
     * @return a boolean.
     */
    final boolean isFinished() {
        return m_state.get() == State.COMPLETED;
    }
    
    /**
     * This task has be sent to the TaskCoordinator to be run
     *
     * @return a boolean.
     */
    protected final boolean isScheduled() {
        return m_state.get() != State.NEW || m_scheduleCalled.get();
    }
    
    /**
     * Adds prereq as a Prerequisite of this task. In other words, this task cannot run
     * until prereq has been completed.
     *
     * @param prereq a {@link org.opennms.core.tasks.AbstractTask} object.
     */
    protected void addPrerequisite(final AbstractTask prereq) {
        getCoordinator().addDependency(prereq, this);
    }
    
    /**
     * Adds dependent as a dependent of this task. The dependent will not be able to run
     * until this task has been completed.
     *
     * TODO: Unused?
     * 
     * @param dependent a {@link org.opennms.core.tasks.AbstractTask} object.
     */
    protected final void addDependent(final AbstractTask dependent) {
        getCoordinator().addDependency(this, dependent);
    }

    /**
     * Wait for this task to complete.  The current thread will block until this task has been completed.
     *
     * @throws java.lang.InterruptedException if any.
     * @throws java.util.concurrent.ExecutionException if any.
     */
    @Override
    public final void waitFor() throws InterruptedException, ExecutionException {
        m_latch.await();
    }

    /**
     * Wait for this task to complete or until a timeout occurs
     *
     * @param timeout a long.
     * @param unit a {@link java.util.concurrent.TimeUnit} object.
     * @throws java.lang.InterruptedException if any.
     */
    @Override
    public final boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException {
        return m_latch.await(timeout, unit);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return String.format("Task[%s]", super.toString());
    }

}
