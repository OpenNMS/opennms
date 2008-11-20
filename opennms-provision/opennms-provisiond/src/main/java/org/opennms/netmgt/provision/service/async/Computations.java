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
package org.opennms.netmgt.provision.service.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.opennms.netmgt.provision.service.Action;
import org.opennms.netmgt.provision.service.Computation;

/**
 * Computations
 *
 * @author brozow
 */
public class Computations {

    public static <T> Computation<T, T> identity() {
        return new Computation<T, T>() {
            public T compute(T t) throws Exception {
                return t;
            }
        };
    }
    
    public static Computation<Void, Void> computation(final Runnable r) {
        return new Computation<Void, Void>() {
            public Void compute(Void v) {
                r.run();
                return null;
            }
        };
    }
    
    public static <T> Computation<T, Void> computation(final Action<T> action) {
        return new Computation<T, Void>() {
            public Void compute(T a) throws Exception {
                action.action(a);
                return null;
            }
        };
    }
    
    public static <T> Computation<Void, T> computation(final Callable<T> callable) {
        return new Computation<Void, T>() {
            public T compute(Void a) throws Exception {
                return callable.call();
            }
        };
    }
    
    
    public static <A, B> Computation<A, B> waiter(final Async<A, B> async) {
        return new Computation<A, B>() {
            public B compute(A a) throws Exception {
                async.start(a);
                return async.waitFor();
            }
        };
    }
    
    public static interface Callback<T> {
        public void callback(T t);
    }
    
    public static <A, B> Async<A, B> async(ExecutorService executor, Computation<A, B> c) {
        return new SimpleAsync<A, B>(executor, c);
    }
    
    public static <A, B> Async<A, B> async(ExecutorService executor, Computation<A, B> c, Action<B> cb) {
        return new SimpleAsync<A, B>(executor, c, cb);
    }
    
    public static AsyncRunnable async(ExecutorService executor, Runnable r) {
        return new AsyncRunnable(executor, r);
    }
    
    public static class SequenceBuilder<A,B> {
        private final Computation<A, B> m_computation;
        
        public SequenceBuilder(Computation<A, B> computation) {
            m_computation = computation;
        }
        
        public <C> SequenceBuilder<A, C> add(Computation<B, C> next) {
            return new SequenceBuilder<A, C>(Computations.composition(m_computation, next));
        }

        public Computation<A, B> computation() {
            return m_computation;
        }
        
    }
    
    public static <A, B, C> Computation<A, C> composition(final Computation<A, B> c1, final Computation<B, C> c2) {
        return new Computation<A, C>() {
            public C compute(A a) throws Exception {
                return c2.compute(c1.compute(a));
            }
        };
    }
    

    public static <A, B> SequenceBuilder<A, B> seq(Computation<A, B> computation) {
        return new SequenceBuilder<A, B>(computation);
    }
    
    public static class LinkedAsyncComputation<A, B, C, D> implements Async<A, D> {
        
        private Async<A, B> m_head;
        private Async<C, D> m_tail;
        
        public LinkedAsyncComputation(Async<A, B> head, Async<C, D> tail) {
            m_head = head;
            m_tail = tail;
        }
        
        public void addCallback(Action<D> cb) {
            m_tail.addCallback(cb);
        }

        public void start(A a) {
            if (m_head != null) {
                System.err.println("Starting Task "+this);
                m_head.start(a);
                System.err.println("Started Task "+this);
                // release head so it can be garbage collected
                m_head = null;
            }
        }

        public D waitFor() throws Exception {
            return m_tail.waitFor();
        }
        
        public <E> LinkedAsyncComputation<A, B, D, E> append(Async<D, E> next) {
            Computations.link(m_tail, next);
            return new LinkedAsyncComputation<A, B, D, E>(m_head, next);
        }
        
    }
    
    public static <A, B, C> void link(Async<A, B> first, Async<B, C> next) {
        first.addCallback(starterCallback(next));
    }
    
    private static <X, Y> Action<X> starterCallback(final Async<X, Y> next) {
        return new Action<X>() {
            public void action(X x) {
                next.start(x);
            }
        };
    }
    
    public static <A, B, C> Async<A, C> composition(final Async<A, B> a1, final Async<B, C> a2) {
        Action<B> callNext = new Action<B>() {
            public void action(B b) {
                a2.start(b);
            }
        };
        
        a1.addCallback(callNext);


        return new Async<A, C>() {

            public void addCallback(Action<C> cb) {
                a2.addCallback(cb);
            }

            public void start(A a) {
                System.err.println("Starting Task "+this);
                a1.start(a);
                System.err.println("Started Task "+this);
            }

            public C waitFor() throws Exception {
                return a2.waitFor();
            }
            
            public String toString() {
                StringBuilder buf = new StringBuilder();
                buf.append(getClass()).append('@').append(hashCode());
                buf.append('[');
                buf.append("first=").append(a1);
                buf.append(", ");
                buf.append("next=").append(a2);
                buf.append(']');
                return buf.toString();
            }

            
        };
        
    }
    
    public static interface AsyncSequenceBuilder<A, B> {
            
        public <C> AsyncSequenceBuilder<A, C> add(Async<B, C> next);
        
        public <C> AsyncSequenceBuilder<A, C> add(Computation<B, C> next);
        
        public void start(A a);
        
        public Async<A, B> async();
    }
    
    public static class LinkedAsyncSequenceBuilder<A, B, C, D> implements AsyncSequenceBuilder<A, D> {

        ExecutorService m_executor;
        LinkedAsyncComputation<A, B, C, D> m_seq;
        
        public LinkedAsyncSequenceBuilder(ExecutorService executor, LinkedAsyncComputation<A, B, C, D> seq) {
            m_executor = executor;
            m_seq = seq;
        }

        public <E> AsyncSequenceBuilder<A, E> add(Async<D, E> next) {
            LinkedAsyncComputation<A, B, D, E> seq = m_seq.append(next);
            return new LinkedAsyncSequenceBuilder<A, B, D, E>(m_executor, seq);
        }

        public <E> AsyncSequenceBuilder<A, E> add(Computation<D, E> next) {
            return add(Computations.async(m_executor, next));
        }
        
        public void start(A a) {
            m_seq.start(a);
        }

        public Async<A, D> async() {
            return m_seq;
        }
        
    }
    
    public static class SingletonAsyncSequenceBuilder<A, B> implements AsyncSequenceBuilder<A, B> {
        
        private ExecutorService m_executor;
        private Async<A, B> m_async;
        
        public SingletonAsyncSequenceBuilder(ExecutorService executor, Async<A, B> a) {
            m_executor = executor;
            m_async = a;
        }

        public <C> AsyncSequenceBuilder<A, C> add(Async<B, C> next) {
            Computations.link(m_async, next);
            LinkedAsyncComputation<A, B, B, C> seq = new LinkedAsyncComputation<A, B, B, C>(m_async, next);
            return new LinkedAsyncSequenceBuilder<A, B, B, C>(m_executor, seq);
        }

        public <C> AsyncSequenceBuilder<A, C> add(Computation<B, C> next) {
            return add(Computations.async(m_executor, next));
        }
        
        public void start(A a) {
            m_async.start(a);
        }

        public Async<A, B> async() {
            return m_async;
        }
        
    }

    public static <A, B> AsyncSequenceBuilder<A, B> asyncSeq(ExecutorService executor, Computation<A, B> computation) {
        return asyncSeq(executor, async(executor, computation));
    }
    
    public static <A, B> AsyncSequenceBuilder<A, B> asyncSeq(ExecutorService executor, Async<A, B> async) {
        return new SingletonAsyncSequenceBuilder<A, B>(executor, async);
    }

}
