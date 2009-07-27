/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.protocols.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;


/**
 * RequestTrackerTest
 *
 * @author brozow
 */
public class RequestTrackerTest {
    
    public static long TIMEOUT = 100;
    
    private static class TestReply implements Reply<Integer> {
        
        private TestRequest m_request;
        
        public TestReply(TestRequest request) {
            m_request = request;
            m_request.setSentTimestamp(System.currentTimeMillis());
        }

        public Integer getRequestId() {
            return m_request.getId();
        }
    }
    
    private static interface Callback<ReqT, RespT> {
        public void processError(ReqT request, Throwable t);
        public void processTimeout(ReqT request);
        public void processResponse(ReqT request, RespT response);
    }
    
    private static class TestRequest implements Request<Integer, TestRequest, TestReply> {
        
        private Integer m_id;
        private long m_timeout;
        private int m_retries;
        private Callback<TestRequest, TestReply> m_cb;
        private Long m_expirationTimestamp;
        private Long m_sentTimestamp;

        public TestRequest(Integer id, long timeout, int retries, Callback<TestRequest, TestReply> cb) {
            m_id = id;
            m_timeout = timeout;
            m_retries = retries;
            m_expirationTimestamp = System.currentTimeMillis() + timeout;
            m_cb = cb;
        }
        

        public void setSentTimestamp(long sentTimestamp) {
            m_sentTimestamp = sentTimestamp;
        }
        
        public Long getSentTimestamp() {
            return m_sentTimestamp;
        }


        public long getDelay(TimeUnit unit) {
            return unit.convert(m_expirationTimestamp - System.currentTimeMillis(), TimeUnit.MILLISECONDS); 
        }

        public Integer getId() {
            return m_id;
        }

        public void processError(Throwable t) {
            if (m_cb != null) m_cb.processError(this, t);
        }

        public void processResponse(TestReply reply) {
            if (m_cb != null) m_cb.processResponse(this, reply);
        }

        public TestRequest processTimeout() {
            if (m_retries > 0) {
                return new TestRequest(m_id, m_timeout, m_retries-1, m_cb);
            } else {
                if (m_cb != null) m_cb.processTimeout(this);
                return null;
            }
                
        }

        public int compareTo(Delayed o) {

            long thisVal = getDelay(TimeUnit.NANOSECONDS);
            long anotherVal = o.getDelay(TimeUnit.NANOSECONDS);
            
            return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
            
        }
        
    }
    
    private class TestCallback implements Callback<TestRequest, TestReply> {
        
        public TestRequest request;

        public Throwable error;
        public Long errorTimestamp;

        
        public TestReply response;
        public long responseTimestamp;
        
        public Long timeoutTimestamp;
        
        public AtomicInteger callbackCount = new AtomicInteger();
        

        public void processError(TestRequest request, Throwable t) {
            this.request = request;
            this.error = t;
            this.errorTimestamp = System.currentTimeMillis();
            this.callbackCount.incrementAndGet();
        }

        public void processResponse(TestRequest request, TestReply response) {
            this.request = request;
            this.response = response;
            this.responseTimestamp = System.currentTimeMillis();
            this.callbackCount.incrementAndGet();
        }

        public void processTimeout(TestRequest request) {
            this.request = request;
            this.timeoutTimestamp = System.currentTimeMillis();
            this.callbackCount.incrementAndGet();
        }
        
        public int getCallbackCount() {
            return this.callbackCount.get();
        }
        
    }
    
    private abstract static class TestMessenger implements Messenger<TestRequest, TestReply> {
        AtomicReference<Queue<TestReply>> m_queue = new AtomicReference<Queue<TestReply>>();
        AtomicInteger m_sendsRequested = new AtomicInteger();
        AtomicInteger m_sendsPerformed = new AtomicInteger();
        
        public void start(Queue<TestReply> q) {
            if (!m_queue.compareAndSet(null, q)) {
                throw new IllegalStateException(getClass()+" is already started!");
            }
        }

        Queue<TestReply> getQueue() {
            Queue<TestReply> q = m_queue.get();
            assertNotNull(getClass()+" is not yet started!!!", q);
            return q;
        }
        
        void doSend(TestRequest request) throws IOException {
            Queue<TestReply> q = m_queue.get();
            assertNotNull(getClass()+" is not yet started!!!", q);
            sendPerformed();
            q.offer(new TestReply(request));
        }

        private void sendPerformed() {
            m_sendsPerformed.incrementAndGet();
        }
        
        void sendRequested() {
            m_sendsRequested.incrementAndGet();
        }
        
        public int getSendsRequested() {
            return m_sendsRequested.get();
        }
        
        public int getSendsPerformed() {
            return m_sendsPerformed.get();
        }
    }
    
    private static class ImmediateTestMessenger extends TestMessenger {
                
        public void sendRequest(TestRequest request) throws IOException {
            sendRequested();
            doSend(request);
        }
    }
    
    private static class NeverReplyTestMessenger extends TestMessenger {
        
        public void sendRequest(TestRequest request) throws IOException {
            // create a reply so the sent timesteamp is set.. but throw it away
            sendRequested();
            new TestReply(request);
        }
    }
    
    private static class DelayedTestMessenger extends TestMessenger implements Runnable {
        
        private class Waiter implements Delayed {

            private TestRequest m_request;
            private Long m_sendTime;
            
            Waiter(TestRequest request, long delay) {
                m_request = request;
                m_sendTime = System.currentTimeMillis() + delay;
            }
            

            public long getDelay(TimeUnit unit) {
                return unit.convert(m_sendTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS); 
            }

            public int compareTo(Delayed o) {

                long thisVal = getDelay(TimeUnit.NANOSECONDS);
                long anotherVal = o.getDelay(TimeUnit.NANOSECONDS);
                
                return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
                    

            }
            
            public void send() throws IOException {
                doSend(m_request);
            }
            
        }
        
        private DelayQueue<Waiter> m_delayedRequests = new DelayQueue<Waiter>();
        private long m_delay;
        
        public DelayedTestMessenger(long delay) {
            m_delay = delay;
        }
        
        public void start(Queue<TestReply> replyQueue) {
            super.start(replyQueue);
            
            new Thread(this,"Delayed-Replier").start();
        }
        
        public void run() {
            while(true) {
                try {
                    Waiter w = m_delayedRequests.take();
                    w.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }
        
        public void sendRequest(TestRequest request) throws IOException {
            sendRequested();
            m_delayedRequests.offer(new Waiter(request, m_delay));
        }
    }
    

    @Test
    public void testRequestTrackerReply() throws Exception {
        
        
        RequestTracker<Integer, TestRequest, TestReply> rt = new RequestTracker<Integer, TestRequest, TestReply>("Immediate", new ImmediateTestMessenger());

        rt.start();
        
        TestCallback cb = new TestCallback();
        TestRequest req = new TestRequest(1, TIMEOUT, 0, cb);

        rt.sendRequest(req);
        
        // this gives the rt threads a chance to work
        Thread.sleep(50);

        // no error
        assertNull(cb.error);
        // no timeout
        assertNull(cb.timeoutTimestamp);
        
        // expect a reply
        assertNotNull(cb.response);
        assertNotNull(cb.responseTimestamp);
        
        // expect the reply very quickly (no more than 10 millis)
        long delay = cb.responseTimestamp - req.getSentTimestamp();
        assertTrue( "Response came too late. Expected delay "+delay+" to be less than 10", delay < 10);

        
        // assert reply has the same requestId
        assertEquals(cb.request.getId(), cb.response.getRequestId());
        
        // assert the replies request is the same object as the original request -- this means no retries
        assertSame(cb.request, cb.response.m_request);
        
    }
    
    @Test
    public void testTimeoutNoRetries() throws Exception {
        
        RequestTracker<Integer, TestRequest, TestReply> rt = new RequestTracker<Integer, TestRequest, TestReply>("NeverReply", new NeverReplyTestMessenger());

        rt.start();
        
        TestCallback cb = new TestCallback();
        TestRequest req = new TestRequest(1, TIMEOUT, 0, cb);
        
        rt.sendRequest(req);
        
        Thread.sleep(TIMEOUT + 20);
        
        assertNull(cb.response);
        assertNull(cb.error);
        assertNotNull(cb.timeoutTimestamp);

        long elapsedTime = cb.timeoutTimestamp - req.getSentTimestamp();
        
        // no more than two millis should pass before the timeout is processed
        assertTrue( "Timeout processed too late", elapsedTime < (TIMEOUT + 10) );
        

    }

    @Test
    public void testTimeoutOneRetry() throws Exception {
        
        RequestTracker<Integer, TestRequest, TestReply> rt = new RequestTracker<Integer, TestRequest, TestReply>("NeverReply", new NeverReplyTestMessenger());

        rt.start();
        
        TestCallback cb = new TestCallback();
        TestRequest req = new TestRequest(1, TIMEOUT, 1, cb);
        
        rt.sendRequest(req);
        
        Thread.sleep(TIMEOUT + 20);

        // after one timeout we should not have received anything
        assertNull(cb.response);
        assertNull(cb.error);
        assertNull(cb.timeoutTimestamp);
        
        Thread.sleep(TIMEOUT);

        // after a second timeout we should get the timeout message due to a retry
        assertNull(cb.response);
        assertNull(cb.error);
        assertNotNull(cb.timeoutTimestamp);
        
        long elapsedTime = cb.timeoutTimestamp - req.getSentTimestamp();
        
        // no more than two millis should pass before the timeout is processed
        assertTrue( "Timeout processed too late", elapsedTime < (2 * TIMEOUT + 10) );
        

    }


    @Test
    public void testResponseAfterOneTimeout() throws Exception {
        
        final long REPLY_DELAY = TIMEOUT + 20;
        
        DelayedTestMessenger messenger = new DelayedTestMessenger(REPLY_DELAY);
        RequestTracker<Integer, TestRequest, TestReply> rt = new RequestTracker<Integer, TestRequest, TestReply>("Delayed", messenger);

        rt.start();
        
        TestCallback cb = new TestCallback();
        TestRequest req = new TestRequest(1, TIMEOUT, 1, cb);

        rt.sendRequest(req);
        
        // this gives the rt threads a chance to work
        Thread.sleep(50);
        
        // we havent received the reponse yet
        assertNull(cb.error);
        assertNull(cb.timeoutTimestamp);
        assertNull(cb.response);
        assertEquals(0, cb.getCallbackCount());
        
        Thread.sleep(REPLY_DELAY);

        // no error
        assertNull(cb.error);
        // no timeout
        assertNull(cb.timeoutTimestamp);
        
        // expect a reply
        assertNotNull(cb.response);
        assertNotNull(cb.responseTimestamp);
        
        TestReply response = cb.response;
        
        // expect the reply very quickly (no more than 10 millis)
        long delay = cb.responseTimestamp - req.getSentTimestamp();
        assertTrue( "Response came too late. Expected delay "+delay+" to be less than 10", delay < 10);

        // assert reply has the same requestId
        assertEquals(cb.request.getId(), cb.response.getRequestId());
        
        // ensure that we send out a retry but have only received the response to the first request
        assertEquals(2, messenger.getSendsRequested());
        assertEquals(1, messenger.getSendsPerformed());
        assertEquals(1, cb.getCallbackCount());

        Thread.sleep(2*TIMEOUT);
        
        // ensure that we send out a retry and have also received the send reply
        assertEquals(2, messenger.getSendsRequested());
        assertEquals(2, messenger.getSendsPerformed());
        assertEquals(1, cb.getCallbackCount());

        // assert a new response hasn't come for the second retry;
        assertSame(response, cb.response);
    }

}
