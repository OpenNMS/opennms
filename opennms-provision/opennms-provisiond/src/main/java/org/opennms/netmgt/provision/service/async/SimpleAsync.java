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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.Action;
import org.opennms.netmgt.provision.service.Computation;
import org.springframework.util.Assert;


/**
 * Async
 *
 * @author brozow
 */
public class SimpleAsync<A, B> implements Async<A, B> {

    private final AtomicReference<B> m_result = new AtomicReference<B>();
    private final AtomicReference<Action<B>> m_callback = new AtomicReference<Action<B>>();
    private final AtomicBoolean m_complete = new AtomicBoolean(false);

    private final Computation<A, B> m_computation;
    private final ExecutorService m_executor;
    private final CountDownLatch m_waitFor = new CountDownLatch(1);
    
    private final Action<B> COMPLETE = new Action<B>() { public void action(B a) {} };
    
    private final AtomicReference<Future<B>> m_future = new AtomicReference<Future<B>>();

    public SimpleAsync(ExecutorService executor, Computation<A, B> c) {
        this(executor, c, SimpleAsync.<B>nil());
    }

    public SimpleAsync(ExecutorService executor, Computation<A, B> c, Action<B> cb) {
        Assert.notNull(cb, "cb cannot be null");
        m_executor = executor;
        m_computation = c;
        m_callback.set(cb);
    }

    public void start(final A a) {
        //debugf("Starting Task %s with value %s", this, a);
        Callable<B> callable = new Callable<B>() {
            public B call() throws Exception {
                return doCompute(a);
            }
        };
        
        m_future.set(m_executor.submit(callable));
        //debugf("Started Task %s", this);
    }
    
    public B waitFor() throws Exception {
        m_waitFor.await();
        Future<B> future = m_future.get();
        return future.get();
    }
    
    protected B doCompute(A a) throws Exception {
        try {
            B ret = m_computation.compute(a);
            complete(ret);
            return ret;
        } finally {
            m_waitFor.countDown();
        }
    }
    
    private void complete(B b) throws Exception {
        //debugf("Setting result for %s to %s", this, b);
        
        // set this first so its available to addCallback
        // when it tries to add a callback to a completed task
        m_result.set(b);

        /* This is the tricky part.. we set this to complete
         * to signal to addCallback that we're already done so
         * it can call the callback directly.  We do this atomically
         * so no locks are needed
         */ 
        Action<B> cb = m_callback.getAndSet(COMPLETE);
        
        // now we can set this but we don't really use it
        m_complete.set(true);
        
        // ok to call the callbacks
        cb.action(b);
    }
    
    public void addCallback(Action<B> cb) {
        // we are using atomics so we retry in case someone changes things
        // out from under us
        while(true) {
            // first build the new callback chain
            Action<B> oldCb = m_callback.get();
            Action<B> newCb = (oldCb == COMPLETE ? COMPLETE : new Chain<B>(cb, oldCb));

            // now we set the callback chain ensuring that is the same as it was before
            if (m_callback.compareAndSet(oldCb, newCb)) {
                // if it was marked as completed before then we need to call our callback
                if (oldCb == COMPLETE) {
                    cb.action(m_result.get());
                }
                
                // if we go there then everything worked ok
                return;
            }
        }
    }
    
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        buf.append("@");
        buf.append(hashCode());
        buf.append('[');
        buf.append("complete=").append(m_complete.get());
        buf.append(", ");
        buf.append("result=").append(m_result.get());
        buf.append(", ");
        buf.append("computation=").append(m_computation);
        buf.append(']');
        return buf.toString();
    }
    
    private void debugf(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }
    
    private static <T> Action<T> nil() {
        return new Chain<T>();
    }
    
    private static class Chain<T> implements Action<T> {
        
        private Action<T> m_action;
        private Action<T> m_next;
        
        public Chain() {
            this(null, null);
        }
        
        public Chain(Action<T> action) {
            this(action, null);
        }
        
        public Chain(Action<T> action, Action<T> next) {
            m_action = action;
            m_next = next;
        }
        
        public void action(T a) {
            if (m_action != null) {
                m_action.action(a);
            }
            if (m_next != null) {
                m_next.action(a);
            }
        }
        
        
    }

}
