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
package org.opennms.netmgt.provision.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


/**
 * TaskBuilders
 *
 * @author brozow
 */
public class TaskBuilders {
    
    public static <A, B> TaskBuilder<A, B> compute(final Computation<A, B> c) {
        return new TaskBuilder<A, B>() {
            public Task<A, B> buildTask(ExecutorService executor) {
                return new Task<A, B>(executor) {
                    @Override
                    public B execute(A a) throws Exception {
                        return c.compute(a);
                    }
                };
            }
        };
    }
    
    public static <A> TaskBuilder<A, Void> action(final Action<A> action) {
        return new TaskBuilder<A, Void>() {
            public Task<A, Void> buildTask(ExecutorService executor) {
                return new Task<A, Void>(executor) {
                    @Override
                    public Void execute(A a) throws Exception {
                        action.action(a);
                        return null;
                    }
                };
            }
        };
    }
    
    public static <A> TaskBuilder<Void, A> callable(final Callable<A> callable) {
        return new TaskBuilder<Void, A>() {
            public Task<Void, A> buildTask(ExecutorService executor) {
                return new Task<Void, A>(executor) {
                    @Override
                    public A execute(Void v) throws Exception {
                        return callable.call();
                    }
                };
            }
        };
    }
    
    public static TaskBuilder<Void, Void> runnable(final Runnable r) {
        return new TaskBuilder<Void, Void>() {
            public Task<Void, Void> buildTask(ExecutorService executor) {
                return new Task<Void, Void>(executor) {
                    @Override
                    public Void execute(Void a) throws Exception {
                        r.run();
                        return null;
                    }
                };
            }
        };
    }
    
    public static <T> TaskBuilder<T, T> identityBuilder() {
        return compute(TaskBuilders.<T>identity());
    }

    private static <T> Computation<T, T> identity() {
        return new Computation<T, T>() {
            public T compute(T t) throws Exception {
                return t;
            }
        };
    }
    
    public static class SequenceBuilder<A, B> {
        private TaskBuilder<A, B> first;
        
        public SequenceBuilder(TaskBuilder<A, B> bldr) {
            first = bldr;
        }
        
        public <C> SequenceBuilder<A, C> seq(TaskBuilder<B, C> next) {
            return new SequenceBuilder<A, C>(TaskBuilders.seq(first, next));
        }
        
        public TaskBuilder<A, B> builder() {
            return first;
        }
        
    }
    
    public static <A, B> SequenceBuilder<A, B> seq(TaskBuilder<A, B> first) {
        return new SequenceBuilder<A, B>(first);
    }

    public static <A, B, C> TaskBuilder<A, C> seq(
            final TaskBuilder<A, B> bldr1,
            final TaskBuilder<B, C> bldr2
      ) 
    {
      return new TaskBuilder<A, C>() {
        public Task<A, C> buildTask(final ExecutorService executor) {
            return new Task<A, C>(executor) {
                @Override
                public C execute(A a) throws Exception {
                    Task<A, B> task1 = bldr1.buildTask(executor);
                    Task<B, C> task2 = bldr2.buildTask(executor);
                    task1.start(a);
                    task2.start(task1.waitFor());
                    return task2.waitFor();
                }
            };
        }
      };
    }
    
}
