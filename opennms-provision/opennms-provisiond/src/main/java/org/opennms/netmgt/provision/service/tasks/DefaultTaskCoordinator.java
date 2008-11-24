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
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * TaskCoordinator
 *
 * @author brozow
 */
public class DefaultTaskCoordinator {
    
    /**
     * LatchTaskWaiter
     *
     * @author brozow
     */
    public class LatchTaskWaiter extends BaseTask implements TaskWaiter {
        
        private CountDownLatch m_latch = new CountDownLatch(1);

        public LatchTaskWaiter(DefaultTaskCoordinator coordinator) {
            super(coordinator);
        }
        
        public void run() {
            m_latch.countDown();
        }

        public void waitFor() throws InterruptedException {
            m_latch.await();
        }

        public void waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            m_latch.await(timeout, unit);
        }
        
        public String toString() {
            return "Latch Task";
        }

    }

    /**
     * FutureTaskWaiter
     *
     * @author brozow
     */
    public static class FutureTaskWaiter implements TaskWaiter {

        Future<?> m_future;
        
        public FutureTaskWaiter(Future<?> future) {
            m_future = future;
        }

        public void waitFor() throws InterruptedException, ExecutionException {
            m_future.get();
        }

        public void waitFor(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            m_future.get(timeout, unit);
        }

    }
    
    private class CompletionProcessor extends Thread {
        private CompletionService<BaseTask> m_completionService;
        public CompletionProcessor(CompletionService<BaseTask> completionService) {
            m_completionService = completionService; 
        }
        
        public void run() {
            try {
                while(true) {
                    BaseTask before = m_completionService.take().get();
                    
                    System.err.printf("Task %s completed!\n", before);

                    final Set<BaseTask> afters = getAfters(before);
                    System.err.printf("Task %s afters = %s\n", before, afters);
                    if (afters != null) {
                        for(BaseTask after : afters) {
                            System.err.printf("Checking the prereqs for %s\n", after);
                            Set<BaseTask> befores = getBefores(after);
                            System.err.printf("Found %s befores for task %s\n", befores, after);
                            befores.remove(before);
                            System.err.printf("Updated befores are %s\n", befores);
                            if (befores.isEmpty()) {
                                submit(after);
                            }
                        }
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
    
    ExecutorService m_executor;
    ExecutorCompletionService<BaseTask> m_completionService;
    ConcurrentMap<BaseTask, Set<BaseTask>> m_befores = new ConcurrentHashMap<BaseTask, Set<BaseTask>>();
    ConcurrentMap<BaseTask, Set<BaseTask>> m_afters = new ConcurrentHashMap<BaseTask, Set<BaseTask>>();
    CompletionProcessor m_processor;
    
    public DefaultTaskCoordinator(ExecutorService executor) {
        m_executor = executor;
        m_completionService = new ExecutorCompletionService<BaseTask>(executor);
        m_processor = new CompletionProcessor(m_completionService);
        m_processor.start();
    }

    public TaskWaiter submit(BaseTask task) {
        return new FutureTaskWaiter(m_completionService.submit(task));
    }

    public void schedule(final BaseTask baseTask) {
        if (!hasBefores(baseTask)) {
            submit(baseTask);
        } 
    }
    
    public void addDependency(BaseTask before, BaseTask after) {
        add(m_befores, after, before);
        add(m_afters, before, after);
    }
    
    private void add(ConcurrentMap<BaseTask, Set<BaseTask>> map, BaseTask key, BaseTask addedValue) {
        Set<BaseTask> newSet = new HashSet<BaseTask>();
        Set<BaseTask> set = map.putIfAbsent(key, newSet);
        if (set == null) {
            set = newSet;
        }
        set.add(addedValue);
    }
    
    private Set<BaseTask> getBefores(BaseTask task) {
       return m_befores.get(task);
    }
    
    private boolean hasBefores(BaseTask task) {
        return m_befores.containsKey(task);
    }
    
    private Set<BaseTask> getAfters(BaseTask task) {
        return m_afters.get(task);
    }
    
    private boolean hasAfters(BaseTask task) {
        return m_afters.containsKey(task);
    }

    public void waitFor(BaseTask baseTask) throws InterruptedException {
        createWaiterTask(baseTask).waitFor();
    }

    private LatchTaskWaiter createWaiterTask(BaseTask baseTask) {
        LatchTaskWaiter waiter = new LatchTaskWaiter(this);
        waiter.addDependency(baseTask);
        waiter.schedule();
        return waiter;
    }

    public void waitFor(BaseTask baseTask, long timeout, TimeUnit unit) throws InterruptedException {
        createWaiterTask(baseTask).waitFor(timeout, unit);
    }

}
