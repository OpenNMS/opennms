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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * TaskCoordinator
 *
 * @author brozow
 */
public class DefaultTaskCoordinator implements InitializingBean {

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
                    log().warn("runnable actor interrupted", e);
                } catch (ExecutionException e) {
                    log().warn("runnable actor execution failed", e);
                } catch (Throwable e) {
                    log().error("an unknown error occurred in the runnable actor", e);
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

    public DefaultTaskCoordinator(String name) {
        m_queue = new LinkedBlockingQueue<Future<Runnable>>();
        m_actor = new RunnableActor(name+"-TaskScheduler", m_queue);
        addExecutor(SyncTask.ADMIN_EXECUTOR, Executors.newSingleThreadExecutor());
    }
    
    public DefaultTaskCoordinator(String name, Executor defaultExecutor) {
        this(name);
        m_defaultExecutor = SyncTask.DEFAULT_EXECUTOR;
        addExecutor(SyncTask.DEFAULT_EXECUTOR, defaultExecutor);
        afterPropertiesSet();
    }

    public void setDefaultExecutor(String executorName) {
        m_defaultExecutor = executorName;
    }
    
    public void afterPropertiesSet() {
        Assert.notNull(m_defaultExecutor, "defaultExecutor must be set");
        
        m_defaultCompletionService = getCompletionService(m_defaultExecutor);
        
        Assert.notNull(m_defaultCompletionService, "defaultExecutor must be set to the name of an added executor");
        
    }
    
    public SyncTask createTask(ContainerTask parent, Runnable r) {
        return new SyncTask(this, parent, r);
    }
    
    public SyncTask createTask(ContainerTask parent, Runnable r, String schedulingHint) {
        return new SyncTask(this, parent, r, schedulingHint);
    }
    
    public <T> AsyncTask<T> createTask(ContainerTask parent, Async<T> async, Callback<T> cb) {
        return new AsyncTask<T>(this, parent, async, cb);
    }
    

    public BatchTask createBatch(ContainerTask parent) {
        return new BatchTask(this, parent);
    }
    
    public SequenceTask createSequence(ContainerTask parent) {
        return new SequenceTask(this, parent);
    }
    
    public void setLoopDelay(long millis) {
        m_loopDelay = millis;
    }
    
    public void schedule(final Task task) {
        onProcessorThread(scheduler(task));
    }
    
    public void addDependency(Task prereq, Task dependent) {
        // this is only needed when add dependencies while running
        dependent.incrPendingPrereqCount();
        onProcessorThread(dependencyAdder(prereq, dependent));
    }
    
    private void onProcessorThread(final Runnable r) {
        Future<Runnable> now = new Future<Runnable>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }
            public Runnable get() {
                return r;
            }
            public Runnable get(long timeout, TimeUnit unit) {
                return get();
            }
            public boolean isCancelled() {
                return false;
            }
            public boolean isDone() {
                return true;
            }
            public String toString() {
                return "Future<"+r+">";
            }
        };
        m_queue.add(now);
    }

    

    private Runnable scheduler(final Task task) {
        return new Runnable() {
            public void run() {
                task.scheduled();
                task.submitIfReady(); 
            }
            public String toString() {
                return String.format("schedule(%s)", task);
            }
        };
    }
    
    Runnable taskCompleter(final Task task) {
        return new Runnable() {
            public void run() {
                notifyDependents(task);
            }
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
    
    public void addExecutor(String executorName, Executor executor) {
        m_taskCompletionServices.put(executorName, new ExecutorCompletionService<Runnable>(executor, m_queue));
    }

    public void setExecutors(Map<String,Executor> executors) {
        m_taskCompletionServices.clear();
        for (Map.Entry<String, Executor> e : executors.entrySet()) {
            addExecutor(e.getKey(), e.getValue());
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
