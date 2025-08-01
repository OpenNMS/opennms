/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.tracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * RequestTrackerTest
 *
 * @author brozow
 */
public class RequestTrackerTest {
    private static final Logger LOG = LoggerFactory.getLogger(RequestTrackerTest.class);

    public static long TIMEOUT = 100;

    private static class TestReply implements ResponseWithId<Integer> {

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

        private final AtomicBoolean m_processed = new AtomicBoolean(false);
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
            m_processed.set(true);
            if (m_cb != null) m_cb.processError(this, t);
        }

        public boolean processResponse(TestReply reply) {
            m_processed.set(true);
            if (m_cb != null) m_cb.processResponse(this, reply);
            return true;
        }

        public TestRequest processTimeout() {
            m_processed.set(true);
            if (m_retries > 0) {
                return new TestRequest(m_id, m_timeout, m_retries-1, m_cb);
            } else {
                if (m_cb != null) m_cb.processTimeout(this);
                return null;
            }

        }

        public boolean isProcessed() {
            return m_processed.get();
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

        public TestReply response;
        public long responseTimestamp;

        public Long timeoutTimestamp;

        public AtomicInteger callbackCount = new AtomicInteger();


        public void processError(TestRequest request, Throwable t) {
            this.request = request;
            this.error = t;
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
        private AtomicReference<ReplyHandler<TestReply>> m_callback = new AtomicReference<ReplyHandler<TestReply>>();
        private AtomicInteger m_sendsRequested = new AtomicInteger();
        private AtomicInteger m_sendsPerformed = new AtomicInteger();

        public void start(ReplyHandler<TestReply> callback) {
            if (!m_callback.compareAndSet(null, callback)) {
                throw new IllegalStateException(getClass()+" is already started!");
            }
        }

        private ReplyHandler<TestReply> getCallback() {
            ReplyHandler<TestReply> callback = m_callback.get();
            assertNotNull(getClass()+" is not yet started!!!", callback);
            return callback;
        }

        void doSend(TestRequest request) throws IOException {
            ReplyHandler<TestReply> callback = getCallback();
            assertNotNull(getClass()+" is not yet started!!!", callback);
            sendPerformed();
            callback.handleReply(new TestReply(request));
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

        public void start(ReplyHandler<TestReply> callback) {
            super.start(callback);

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


        RequestTracker<TestRequest, TestReply> rt = new RequestTracker<TestRequest, TestReply>("Immediate", new ImmediateTestMessenger(), new IDBasedRequestLocator<Integer, TestRequest, TestReply>());

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

        // expect the reply very quickly (no more than 50 millis)
        long delay = cb.responseTimestamp - req.getSentTimestamp();
        assertTrue( "Response came too late. Expected delay "+delay+" to be less than 50", delay < 50);


        // assert reply has the same requestId
        assertEquals(cb.request.getId(), cb.response.getRequestId());

        // assert the replies request is the same object as the original request -- this means no retries
        assertSame(cb.request, cb.response.m_request);

    }

    @Test
    public void testTimeoutNoRetries() throws Exception {

        RequestTracker<TestRequest, TestReply> rt = new RequestTracker<TestRequest, TestReply>("NeverReply", new NeverReplyTestMessenger(), new IDBasedRequestLocator<Integer, TestRequest, TestReply>());

        rt.start();

        TestCallback cb = new TestCallback();
        TestRequest req = new TestRequest(1, TIMEOUT, 0, cb);

        rt.sendRequest(req);

        Thread.sleep(TIMEOUT + 20);

        assertNull(cb.response);
        assertNull(cb.error);
        assertNotNull(cb.timeoutTimestamp);

        long elapsedTime = cb.timeoutTimestamp - req.getSentTimestamp();

        LOG.info("testTimeoutNoRetries processing took " + elapsedTime);

        // no more than two millis should pass before the timeout is processed
        assertThat( "Timeout processing elapsed time", elapsedTime, is(lessThan(TIMEOUT + 30)) );


    }

    @Test
    public void testTimeoutOneRetry() throws Exception {

        RequestTracker<TestRequest, TestReply> rt = new RequestTracker<TestRequest, TestReply>("NeverReply", new NeverReplyTestMessenger(), new IDBasedRequestLocator<Integer, TestRequest, TestReply>());

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

        LOG.info("testTimeoutOneRetry processing took " + elapsedTime);

        // no more than two millis should pass before the timeout is processed
        assertThat( "Timeout processing elapsed time", elapsedTime, is(lessThan(2 * TIMEOUT + 30)) );
    }


    @Test
    public void testResponseAfterOneTimeout() throws Exception {

        final long REPLY_DELAY = TIMEOUT + 20;

        DelayedTestMessenger messenger = new DelayedTestMessenger(REPLY_DELAY);
        RequestTracker<TestRequest, TestReply> rt = new RequestTracker<TestRequest, TestReply>("Delayed", messenger, new IDBasedRequestLocator<Integer, TestRequest, TestReply>());

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

    private class DelayedCallback extends TestCallback {

        private final long m_delay;

        public DelayedCallback(long delay) {
            m_delay = delay;
        }

        public void processResponse(TestRequest request, TestReply response) {
            try {
                Thread.sleep(m_delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            super.processResponse(request, response);
        }
    }

    /**
     * Verifies that we don't process the timeout when the reply is processing,
     * or pending processing
     */
    @Test
    public void testDelayedProcess() throws Exception {

        final long REPLY_DELAY = TIMEOUT + 20;
        final long PROCESS_TIME = TIMEOUT * 3;

        DelayedTestMessenger messenger = new DelayedTestMessenger(REPLY_DELAY);
        RequestTracker<TestRequest, TestReply> rt = new RequestTracker<TestRequest, TestReply>("Delayed", messenger, new IDBasedRequestLocator<Integer, TestRequest, TestReply>());

        rt.start();

        DelayedCallback cb = new DelayedCallback(PROCESS_TIME);
        TestRequest req = new TestRequest(1, TIMEOUT, 1, cb);

        rt.sendRequest(req);

        // this gives the rt threads a chance to work
        Thread.sleep(50);

        // we haven't received the response yet
        assertNull(cb.error);
        assertNull(cb.timeoutTimestamp);
        assertNull(cb.response);
        assertEquals(0, cb.getCallbackCount());

        // wait long enough for the delayed response to finish
        Thread.sleep(PROCESS_TIME * 2);

        // no error
        assertNull(cb.error);
        // no timeout
        assertNull(cb.timeoutTimestamp);

        // expect a reply
        assertNotNull(cb.response);
        assertNotNull(cb.responseTimestamp);

        // assert reply has the same requestId
        assertEquals(cb.request.getId(), cb.response.getRequestId());
    }
}
