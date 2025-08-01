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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Request Tracker Design
 *
 * The request tracker has four components that are all static
 *
 * a messenger
 * a pending requests map
 * a callback queue (LinkedBlockingQueue)
 * a timeout queue (DelayQueue)
 *
 * It also has two threads:
 *
 * a thread to process the callbacks (Callback-Processor)
 * a thread to process the timeouts (Timeout-Processor)
 *
 * Thread Details:
 *
 * 1.  The callback processor thread is responsible for handling all of the callbacks to
 *     the request object. This thread will pull callbacks off the linked blocking queue
 *     and issue the call in the order in which they were added.
 *
 *     All of the callback are handled from a single thread in order to avoid synchronization
 *     issue in processing the replies and responses. In the versions of the tracker before 0.7,
 *     it was possible for RequestTracker to receive a reply, but issue the timeout before it had
 *     a chance to process it.
 *
 * 2.  The timeout processor is only responsible creating callbacks for timeouts, and adding these
 *     to the callback queue when timeouts occur. The timeout processor pulls the requests off a DelayQueue
 *     and creates a callback if the request had not yet been processed. Note that a DelayQueue does not allow
 *     things to be removed until the timeout has expired.
 *
 * Processing:
 *
 * All requests are asynchronous (if synchronous requests are need that
 * are implemented using asynchronous requests and blocking callbacks)
 *
 * Making a request: (client thread)
 * - create a request (client does this)
 * - send the request (via the Messenger)
 * - add it to the timeout queue
 *
 * Replies come from the messenger:
 * - as replies come in, the messenger invokes the handleReply method on the request tracker
 *   (which was passed during initializing)
 * - the handleReply method adds a callback that will process the reply to the callback queue
 * - when called, this callback will:
 * -- look up and remove the matching request in the pendingRequest map
 * -- call request.processReply(reply) - this will store the reply and
 * -- call the handleReply call back
 * -- pending request sets completed to true
 *
 * Processing a timeout: (Timeout-Processor)
 * - take a request from the timeout queue
 * - if the request is completed discard it
 * - add a callback to the callback queue process the timedout request:
 * - when called, this callback will:
 * -- discard the request if it was completed
 * -- call request.processTimeout(), this will check the number
 *    of retries and either return a new request with fewer retries or
 *    call the handleTimeout call back
 * -- if processTimeout returns a new request than process it as in Making a request
 *
 * Processing a callback: (Callback-Processor)
 * - take a callback from the callbackQueue queue
 * - issue the callback
 */

/**
 * A class for tracking sending and received of arbitrary messages. The
 * transport mechanism is irrelevant and is encapsulated in the Messenger
 * request. Timeouts and Retries are handled by this mechanism and provided to
 * the request object so they can be processed. A request is guaranteed to
 * have one of its process method called no matter what happens. This makes it
 * easier to write code because some kind of indication is always provided and
 * so timing out is not needed in the client.
 *
 * @author jwhite
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class RequestTracker<ReqT extends Request<?, ReqT, ReplyT>, ReplyT extends Response> implements ReplyHandler<ReplyT> {

    private static final Logger s_log = LoggerFactory.getLogger(RequestTracker.class);

    private RequestLocator<ReqT, ReplyT> m_requestLocator;
    private Messenger<ReqT, ReplyT> m_messenger;
    private final BlockingQueue<Callable<Void>> m_callbackQueue;
    private DelayQueue<ReqT> m_timeoutQueue;

    private Thread m_callbackProcessor;
    private Thread m_timeoutProcessor;

    private static final int NEW = 0;
    private static final int STARTING = 1;
    private static final int STARTED = 2;

    private AtomicInteger m_state = new AtomicInteger(NEW);

    /**
     * Construct a RequestTracker that sends and received messages using the
     * indicated messenger. The name is using to name the threads created by
     * the tracker.
     */
    public RequestTracker(String name, Messenger<ReqT, ReplyT> messenger, RequestLocator<ReqT, ReplyT> requestLocator) throws IOException {

        m_requestLocator = requestLocator;
        m_callbackQueue = new LinkedBlockingQueue<Callable<Void>>();
            m_timeoutQueue = new DelayQueue<ReqT>();

            m_callbackProcessor = new Thread(name+"-Callback-Processor") {
                public void run() {
                    try {
                        processCallbacks();
                    } catch (InterruptedException e) {
                        s_log.error("Thread {} interrupted!", this);
                    } catch (Throwable t) {
                        s_log.error("Unexpected exception on Thread " + this + "!", t);
                    }
                }
            };

            m_timeoutProcessor = new Thread(name+"-Timeout-Processor") {
                public void run() {
                    try {
                        processTimeouts();
                    } catch (InterruptedException e) {
                        s_log.error("Thread {} interrupted!", this);
                    } catch (Throwable t) {
                        s_log.error("Unexpected exception on Thread " + this + "!", t);
                    }
                }
            };

        m_messenger = messenger;
        }

    /**
     * This method starts all the threads that are used to process the
     * messages and also starts the messenger.
     */
    public synchronized void start() {
        boolean startNeeded = m_state.compareAndSet(NEW, STARTING);
        if (startNeeded) {
            m_messenger.start(this);
            m_timeoutProcessor.start();
            m_callbackProcessor.start();
            m_state.set(STARTED);
        }
    }

    public void assertStarted() {
        boolean started = m_state.get() == STARTED;
        if (!started) throw new IllegalStateException("RequestTracker not started!");
    }

    /**
     * Send a tracked request via the messenger. The request is tracked for
     * timeouts and retries. Retries are sent if the timeout processing
     * indicates that they should be.
     */
    public void sendRequest(ReqT request) throws Exception {
        assertStarted();
        if (!m_requestLocator.trackRequest(request)) return;
        m_messenger.sendRequest(request);
        s_log.debug("Scheduling timeout for request to {} in {} ms", request, request.getDelay(TimeUnit.MILLISECONDS));
        m_timeoutQueue.offer(request);
    }

    public void handleReply(final ReplyT reply) {
        m_callbackQueue.add(new ReplyCallback<ReqT, ReplyT>(m_requestLocator, reply));
    }

    private void processTimeouts() throws InterruptedException {
        while (true) {
            final ReqT timedOutRequest = m_timeoutQueue.take();

            // do nothing is the request has already been processed
            if (timedOutRequest.isProcessed()) {
                continue;
            }

            // the request hasn't been processed yet, but we'll
            // check again when the callback is issued
            m_callbackQueue.add(new TimedOutRequestCallback<ReqT, ReplyT>(this, m_requestLocator, timedOutRequest));
        }
    }

    private void processCallbacks() throws InterruptedException {
        while (true) {
            Callable<Void> callback = m_callbackQueue.take();
            try {
                callback.call();
            } catch (Exception e) {
                s_log.error("Failed to issue callback {}.", callback, e);
            }
        }
    }

    public static class ReplyCallback<ReqT extends Request<?, ?, ReplyT>, ReplyT> implements Callable<Void> {

        private final RequestLocator<ReqT, ReplyT> m_requestLocator;
        private final ReplyT m_reply;

        public ReplyCallback(RequestLocator<ReqT, ReplyT> requestLocator, ReplyT reply) {
            m_requestLocator = requestLocator;
            m_reply = reply;
        }

        public Void call() throws Exception {
            s_log.debug("Processing reply: {}", m_reply);

            ReqT request = locateMatchingRequest(m_reply);

            if (request != null) {
                boolean isComplete;

                try {
                    s_log.debug("Processing reply {} for request {}", m_reply, request);
                    isComplete = request.processResponse(m_reply);
                } catch (Throwable t) {
                    s_log.error("Unexpected error processingResponse to request: {}, reply is {}", request, m_reply, t);
                    // we should throw away the request if this happens
                    isComplete = true;
                }

                if (isComplete) {
                    m_requestLocator.requestComplete(request);
                }
            } else {
                s_log.info("No request found for reply {}", m_reply);
            }

            return null;
        }

        private ReqT locateMatchingRequest(ReplyT reply) {
            try {
                return m_requestLocator.locateMatchingRequest(reply);
            } catch (Throwable t) {
                s_log.error("Unexpected error locating response to request " + reply + ". Discarding response!", t);
                return null;
            }
        }
    }

    public static class TimedOutRequestCallback<ReqT extends Request<?, ReqT, ?>, ReplyT> implements Callable<Void> {

        private final RequestTracker<ReqT, ?> m_tracker;
        private final RequestLocator<ReqT, ReplyT> m_requestLocator;
        private final ReqT m_timedOutRequest;

        public TimedOutRequestCallback(RequestTracker<ReqT, ?> tracker, RequestLocator<ReqT, ReplyT> requestLocator, ReqT timedOutRequest) {
            m_tracker = tracker;
            m_requestLocator = requestLocator;
            m_timedOutRequest = timedOutRequest;
        }

        public Void call() throws Exception {
            // do nothing is the request has already been processed.
            if (m_timedOutRequest.isProcessed()) {
                return null;
            }

            s_log.debug("Processing a possibly timed-out request: {}", m_timedOutRequest);
            ReqT pendingRequest = m_requestLocator.requestTimedOut(m_timedOutRequest);

            if (pendingRequest == m_timedOutRequest) {
                // the request is still pending, we must time it out
                ReqT retry = null;
                try {
                    s_log.debug("Processing timeout for: {}", m_timedOutRequest);
                    retry = m_timedOutRequest.processTimeout();
                } catch (Throwable t) {
                    s_log.error("Unexpected error processingTimout to request: {}", m_timedOutRequest, t);
                    retry = null;
                }

                if (retry != null) {
                    try {
                        m_tracker.sendRequest(retry);
                    } catch (Exception e) {
                        retry.processError(e);
                    }
                }
            } else if (pendingRequest != null) {
                String msg = String.format("A pending request %s with the same id exists but is not the timeout request %s from the queue!", pendingRequest, m_timedOutRequest);
                s_log.error(msg);
                m_timedOutRequest.processError(new IllegalStateException(msg));
            }

            return null;
        }
    }
}
