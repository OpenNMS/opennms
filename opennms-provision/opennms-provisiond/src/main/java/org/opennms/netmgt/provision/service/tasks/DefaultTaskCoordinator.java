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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * TaskCoordinator
 *
 * @author brozow
 */
public class DefaultTaskCoordinator implements InitializingBean {
    
    /*
     * Refactor this code....
     * 
     * - Currently a map of 'afters' defines the set of tasks that are dependents of another...
     * - When a task completes the set of tasks in its 'afters' set are the one's that need to 
     *   be considered to be run
     * - When a task is considered... it removes the just completed task from its 'befores' list
     *   i.e. the set of tasks that must complete before it can run
     * - If the set of before for a task becomes empty, then the task can be run
     * 
     * Refactoring Ideas:
     * Create a TaskKeeper class
     * - holds the 'dependents' of the task.. those tasks that are awaiting completion 
     *   of the task held by the keeper
     * - holds the set of tasks that must complete in order for the task to run
     * 
     * Put all dependencies management work on a single thread
     * - in order to reduce the need for syncrhonization, all the dependency management work can be
     *   handled by a single thread
     *   
     * The single thread can...
     * - process the completion queue
     * - update dependencies due to completing tasks
     * - schedule tasks that must be run due to completing dependencies
     * - schedule the adding of dependencies
     *  
     * 
     */
    
    private class RunnableActor extends Thread {
        private BlockingQueue<Future<Runnable>> m_queue;
        public RunnableActor(BlockingQueue<Future<Runnable>> queue) {
            m_queue = queue;
            start();
        }
        
        public void run() {
            try {
                int count = 0;
                while(true) {
                    Runnable r = m_queue.take().get();
                    //System.out.printf("Processing completion %d with queue size %d\n", ++count, m_queue.size());
                    //System.err.printf("Processing %s\n", r);
                    //System.err.printf("Processing %s, queue is %s\n", r, m_queue);
                    if (r != null) {
                        r.run();
                    }
                    if (m_loopDelay != null) {
                        sleep(m_loopDelay);
                    }
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    

    private final BlockingQueue<Future<Runnable>> m_queue;
    private final ConcurrentHashMap<String, CompletionService<Runnable>> m_taskCompletionServices = new ConcurrentHashMap<String, CompletionService<Runnable>>();
    private final RunnableActor m_actor;
    
    private String m_defaultExecutor ;
    private CompletionService<Runnable> m_defaultCompletionService;
    
    // This is used to adjust timing during testing
    private Long m_loopDelay;

    public DefaultTaskCoordinator() {
        m_queue = new LinkedBlockingQueue<Future<Runnable>>();
        m_actor = new RunnableActor(m_queue);
        addExecutor(SyncTask.ADMIN_EXECUTOR, Executors.newSingleThreadExecutor());
    }
    
    public DefaultTaskCoordinator(Executor defaultExecutor) {
        this();
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
    
    public SyncTask createTask(Runnable r) {
        return new SyncTask(this, r);
    }
    
    public BatchTask createBatch() {
        return new BatchTask(this);
    }
    
    public SequenceTask createSequence() {
        return new SequenceTask(this);
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
        System.err.printf("Task %s completed!\n", completed);
        completed.onComplete();

        final Set<Task> dependents = completed.getDependents();
        //System.err.printf("Task %s afters = %s\n", completed, dependents);
        for(Task dependent : dependents) {
            //System.err.printf("Checking the prereqs for %s\n", dependent);
            dependent.doRemovePrerequisite(completed);
            if (dependent.isReady()) {
                System.err.printf("\tTask %s %s ready.\n", dependent, dependent.isReady() ? "is" : "is not");
            }
            
            dependent.submitIfReady();
        }
        
        System.err.printf("CLEAN: removing dependents of %s\n", completed);
        completed.clearDependents();
        
        
    }

    /**
     * The returns a runnable that is run on the taskCoordinator thread.. This is 
     * done to keep the Task data structures thread safe.
     */
    private Runnable dependencyAdder(final Task prereq, final Task dependent) {
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
        System.err.printf("USING COMPLETION SERVICE %s : %s!!!!!!\n", name, selected);
        return selected;
    }
    
    void markTaskAsCompleted(Task task) {
        onProcessorThread(taskCompleter(task));
    }

    void submitToExecutor(String executorPreference, Runnable workToBeDone, Task owningTask) {
        submitToExecutor(executorPreference, workToBeDone, taskCompleter(owningTask));
    }
    
    void submitToExecutor(String executorPreference, Runnable workToBeDone, Runnable completionProcessor) {
        final String preferredExecutor = executorPreference;
        getCompletionService(preferredExecutor).submit(workToBeDone, completionProcessor);
        System.out.printf("SUBMIT: Task %s to executor %s\n", workToBeDone, preferredExecutor);
    }
    
    public void addExecutor(String executorName, Executor executor) {
        m_taskCompletionServices.put(executorName, new ExecutorCompletionService<Runnable>(executor, m_queue));
    }

}
