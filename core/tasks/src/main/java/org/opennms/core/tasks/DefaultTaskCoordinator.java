/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.tasks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * TaskCoordinator
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultTaskCoordinator implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskCoordinator.class);

    /**
     * A RunnableActor class is a thread that simple removes Future<Runnable> from a queue
     * and executes them.  This
     *
     * @author brozow
     */
    private class RunnableActor extends Thread {
        private final BlockingQueue<Future<Runnable>> m_queue;
        public RunnableActor(String name, BlockingQueue<Future<Runnable>> queue) {
            super(name);
            m_queue = queue;
            start();
        }
        
        @Override
        public void run() {
            while(true) {
                try {
                    Runnable r = m_queue.take().get();
                    if (r != null) {
                        r.run();
                    }
                    if (m_loopDelay != null) {
                        sleep(m_loopDelay);
                    }
                } catch (InterruptedException e) {
                	LOG.warn("runnable actor interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                	LOG.warn("runnable actor execution failed", e);
                } catch (Throwable e) {
                	LOG.error("an unknown error occurred in the runnable actor", e);
                }
            }
        }
    }
    

    private final BlockingQueue<Future<Runnable>> m_queue;
    private final ConcurrentHashMap<String, CompletionService<Runnable>> m_taskCompletionServices = new ConcurrentHashMap<String, CompletionService<Runnable>>();
    @SuppressWarnings("unused")
    private final RunnableActor m_actor;
    
    private String m_defaultExecutor ;
    private CompletionService<Runnable> m_defaultCompletionService;
    
    // This is used to adjust timing during testing
    private Long m_loopDelay;

    /**
     * <p>Constructor for DefaultTaskCoordinator.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public DefaultTaskCoordinator(String name) {
        m_queue = new LinkedBlockingQueue<Future<Runnable>>();
        m_actor = new RunnableActor(name+"-TaskScheduler", m_queue);
        addExecutor(SyncTask.ADMIN_EXECUTOR, Executors.newSingleThreadExecutor(
            new LogPreservingThreadFactory(SyncTask.ADMIN_EXECUTOR, 1, false)
        ));
    }
    
    /**
     * <p>Constructor for DefaultTaskCoordinator.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param defaultExecutor a {@link java.util.concurrent.Executor} object.
     */
    public DefaultTaskCoordinator(String name, Executor defaultExecutor) {
        this(name);
        m_defaultExecutor = SyncTask.DEFAULT_EXECUTOR;
        addExecutor(SyncTask.DEFAULT_EXECUTOR, defaultExecutor);
        afterPropertiesSet();
    }

    /**
     * <p>setDefaultExecutor</p>
     *
     * @param executorName a {@link java.lang.String} object.
     */
    public void setDefaultExecutor(String executorName) {
        m_defaultExecutor = executorName;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_defaultExecutor, "defaultExecutor must be set");
        
        m_defaultCompletionService = getCompletionService(m_defaultExecutor);
        
        Assert.notNull(m_defaultCompletionService, "defaultExecutor must be set to the name of an added executor");
        
    }
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param r a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SyncTask} object.
     */
    public SyncTask createTask(ContainerTask<?> parent, Runnable r) {
        return new SyncTask(this, parent, r);
    }
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param r a {@link java.lang.Runnable} object.
     * @param schedulingHint a {@link java.lang.String} object.
     * @return a {@link org.opennms.core.tasks.SyncTask} object.
     */
    public SyncTask createTask(ContainerTask<?> parent, Runnable r, String schedulingHint) {
        return new SyncTask(this, parent, r, schedulingHint);
    }
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param cb a {@link org.opennms.core.tasks.Callback} object.
     * @param <T> a T object.
     * @return a {@link org.opennms.core.tasks.AsyncTask} object.
     */
    public <T> AsyncTask<T> createTask(ContainerTask<?> parent, Async<T> async, Callback<T> cb) {
        return new AsyncTask<T>(this, parent, async, cb);
    }
    

    /**
     * <p>createBatch</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<BatchTask> createBatch(ContainerTask<?> parent) {
        return new TaskBuilder<BatchTask>(new BatchTask(this, parent));
    }
    
    /**
     * <p>createBatch</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<BatchTask> createBatch() {
        return createBatch((ContainerTask<?>)null);
    }
    
    /**
     * <p>createBatch</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.BatchTask} object.
     */
    public BatchTask createBatch(ContainerTask<?> parent, Runnable... tasks) {
        return createBatch(parent).add(tasks).get(parent);
    }

    
    /**
     * <p>createBatch</p>
     *
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.BatchTask} object.
     */
    public BatchTask createBatch(Runnable... tasks) {
        return createBatch().add(tasks).get();
    }

    
    /**
     * <p>createSequence</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<SequenceTask> createSequence(ContainerTask<?> parent) {
        return new TaskBuilder<SequenceTask>(new SequenceTask(this, parent));
    }
    
    /**
     * <p>createSequence</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<SequenceTask> createSequence() {
        return createSequence((ContainerTask<?>)null);
    }
    
    /**
     * <p>createSequence</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SequenceTask} object.
     */
    public SequenceTask createSequence(ContainerTask<?> parent, Runnable... tasks) {
        return createSequence(parent).add(tasks).get(parent);
    }

    
    /**
     * <p>createSquence</p>
     *
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SequenceTask} object.
     */
    public SequenceTask createSquence(Runnable... tasks) {
        return createSequence().add(tasks).get();
    }

    
    
    /**
     * <p>setLoopDelay</p>
     *
     * @param millis a long.
     */
    public void setLoopDelay(long millis) {
        m_loopDelay = millis;
    }
    
    /**
     * <p>schedule</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public void schedule(final Task task) {
        onProcessorThread(scheduler(task));
    }
    
    /**
     * <p>addDependency</p>
     *
     * @param prereq a {@link org.opennms.core.tasks.Task} object.
     * @param dependent a {@link org.opennms.core.tasks.Task} object.
     */
    public void addDependency(Task prereq, Task dependent) {
        // this is only needed when add dependencies while running
        dependent.incrPendingPrereqCount();
        onProcessorThread(dependencyAdder(prereq, dependent));
    }
    
    private void onProcessorThread(final Runnable r) {
        Future<Runnable> now = new Future<Runnable>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }
            @Override
            public Runnable get() {
                return r;
            }
            @Override
            public Runnable get(long timeout, TimeUnit unit) {
                return get();
            }
            @Override
            public boolean isCancelled() {
                return false;
            }
            @Override
            public boolean isDone() {
                return true;
            }
            @Override
            public String toString() {
                return "Future<"+r+">";
            }
        };
        m_queue.add(now);
    }

    

    private Runnable scheduler(final Task task) {
        return new Runnable() {
            @Override
            public void run() {
                task.scheduled();
                task.submitIfReady(); 
            }
            @Override
            public String toString() {
                return String.format("schedule(%s)", task);
            }
        };
    }
    
    Runnable taskCompleter(final Task task) {
        return new Runnable() {
            @Override
            public void run() {
                notifyDependents(task);
            }
            @Override
            public String toString() {
                return String.format("notifyDependents(%s)", task);
            }
        };
    }
    
    
    private void notifyDependents(Task completed) {
        // log().debug(String.format("Task %s completed!", completed));
        completed.onComplete();

        final Set<Task> dependents = completed.getDependents();
        for(Task dependent : dependents) {
            dependent.doCompletePrerequisite(completed);
            if (dependent.isReady()) {
                // log().debug(String.format("Task %s %s ready.", dependent, dependent.isReady() ? "is" : "is not"));
            }
            
            dependent.submitIfReady();
        }

        // log().debug(String.format("CLEAN: removing dependents of %s", completed));
        completed.clearDependents();
        
        
    }

    /**
     * The returns a runnable that is run on the taskCoordinator thread.. This is 
     * done to keep the Task data structures thread safe.
     */
    private Runnable dependencyAdder(final Task prereq, final Task dependent) {
        Assert.notNull(prereq, "prereq must not be null");
        Assert.notNull(dependent, "dependent must not be null");
        return new Runnable() {
            @Override
            public void run() {
                prereq.doAddDependent(dependent);
                dependent.doAddPrerequisite(prereq);
                dependent.decrPendingPrereqCount();

                /**
                 *  the prereq task may have completed between the time this adder was enqueued
                 *  and the time we got here.  In this case there will be no tasks to kick this
                 *  one off... so check it here. 
                 */
                dependent.submitIfReady();
            }
            @Override
            public String toString() {
                return String.format("%s.addPrerequisite(%s)", dependent, prereq);
            }
        };
    }
    
    
    private CompletionService<Runnable> getCompletionService(String name) {
        CompletionService<Runnable> completionService = m_taskCompletionServices.get(name);
        CompletionService<Runnable> selected = completionService != null ? completionService : m_defaultCompletionService;
        // log().debug(String.format("USING COMPLETION SERVICE %s : %s!", name, selected));
        return selected;
    }
    
    void markTaskAsCompleted(Task task) {
        onProcessorThread(taskCompleter(task));
    }

    void submitToExecutor(String executorPreference, Runnable workToBeDone, Task owningTask) {
        submitToExecutor(executorPreference, workToBeDone, taskCompleter(owningTask));
    }
    
    void submitToExecutor(String executorPreference, final Runnable workToBeDone, Runnable completionProcessor) {
        getCompletionService(executorPreference).submit(workToBeDone, completionProcessor);
    }
    
    /**
     * <p>addExecutor</p>
     *
     * @param executorName a {@link java.lang.String} object.
     * @param executor a {@link java.util.concurrent.Executor} object.
     */
    public void addExecutor(String executorName, Executor executor) {
        m_taskCompletionServices.put(executorName, new ExecutorCompletionService<Runnable>(executor, m_queue));
    }

    /**
     * <p>setExecutors</p>
     *
     * @param executors a {@link java.util.Map} object.
     */
    public void setExecutors(Map<String,Executor> executors) {
        m_taskCompletionServices.clear();
        for (Map.Entry<String, Executor> e : executors.entrySet()) {
            addExecutor(e.getKey(), e.getValue());
        }
    }

}
