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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * TaskCoordinator
 *
 * @author brozow
 */
public class DefaultTaskCoordinator {
    
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
                while(true) {
                    Runnable r = m_queue.take().get();
                    if (r != null) {
                        r.run();
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
    
    final ExecutorService m_executor;

    final ConcurrentMap<BaseTask, Set<BaseTask>> m_befores = new ConcurrentHashMap<BaseTask, Set<BaseTask>>();
    final ConcurrentMap<BaseTask, Set<BaseTask>> m_afters = new ConcurrentHashMap<BaseTask, Set<BaseTask>>();
    
    final BlockingQueue<Future<Runnable>> m_queue;
    final CompletionService<Runnable> m_taskCompletionService;
    final RunnableActor m_actor;
    
    public DefaultTaskCoordinator(ExecutorService executor) {
        m_executor = executor;
        m_queue = new LinkedBlockingQueue<Future<Runnable>>();
        m_taskCompletionService = new ExecutorCompletionService<Runnable>(m_executor, m_queue);
        m_actor = new RunnableActor(m_queue);
        
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
        };
        m_queue.add(now);
    }

    private void submit(BaseTask task) {
        m_taskCompletionService.submit(task.getRunnable(), taskCompleter(task));
    }
    

    public void schedule(final BaseTask task) {
        onProcessorThread(scheduler(task));
    }
    
    private Runnable scheduler(final BaseTask task) {
        return new Runnable() {
            public void run() {
                submitIfReady(task); 
            }
        };
    }
    
    private Runnable taskCompleter(final BaseTask task) {
        return new Runnable() {
            public void run() {
                notifyDependents(task);
            }
        };
    }
    
    
    private void notifyDependents(BaseTask completed) {
        System.err.printf("Task %s completed!\n", completed);

        final Set<BaseTask> dependents = completed.getDependents();
        System.err.printf("Task %s afters = %s\n", completed, dependents);
        for(BaseTask dependent : dependents) {
            System.err.printf("Checking the prereqs for %s\n", dependent);
            dependent.doRemovePrerequisite(completed);
            System.err.printf("Task %s %s ready\n", dependent, dependent.isReady() ? "is" : "is not");
            
            submitIfReady(dependent);
        }   
    }

    public void addDependency(BaseTask prereq, BaseTask dependent) {
        // this is only needed when add dependencies while running
        dependent.incrPendingPrereq();
        onProcessorThread(dependencyAdder(prereq, dependent));
    }
    
    /**
     * The returns a runnable that is run on the taskCoordinator thread.. This is 
     * done to keep the Task data structures thread safe.
     */
    private Runnable dependencyAdder(final BaseTask prereq, final BaseTask dependent) {
        return new Runnable() {
            public void run() {
                prereq.doAddDependent(dependent);
                dependent.doAddPrerequisite(prereq);
                // this is only needed when add dependencies while running
                dependent.decrPendingPrereq();
                // XXX I do this concerned the the pending prereq prevented things from getting
                // submitted when it should have.. Do I really need to? 
                // If I put this here than some tasks get run more than once
                //submitIfReady(dependent);
            }
        };
    }

    private void submitIfReady(final BaseTask task) {
        if (task.isReady()) {
            submit(task);
        }
    }
    

}
