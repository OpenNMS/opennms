/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.protocols.rt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * 
 * Request Tracker Design
 * 
 * The request tracker has four components that are all static
 * 
 * a messenger
 * a pendingRequest map
 * a pendingReply queue (LinkedBlockingQueue)
 * a timeout queue (DelayQueue)
 * 
 * It also has two threads:
 * 
 * a thread to process the pendingReplyQueue - (icmp reply processor)
 * a thread to process the timeouts (icmp timeout processor)
 * 
 * Processing:
 * 
 * All requests are asynchronous (if synchronous requests are need that
 * are implemented using asynchronous requests and blocking callbacks)
 * 
 * Making a request: (client thread)
 * - create a request (client does this) 
 * - add it to a pendingRequestMap
 * - send the request (via the Messenger)
 * - add it to the timeout queue
 * 
 * Replies come from the messenger: 
 * - the messenger is 'started' by passing in the pendingReplyQueue
 * - as replies come in there are added to the pingingReplyQueue
 * 
 * Processing a reply: (reply processor)
 * - take a reply from the pendingReply queue
 * - look up and remove the matching request in the pendingRequest map
 * - call request.processReply(reply) - this will store the reply and
 *   call the handleReply call back
 * - pending request sets completed to true
 * 
 * Processing a timeout:
 * - take a request from the timeout queue
 * - if the request is completed discard it
 * - otherwise, call request.processTimeout(), this will check the number
 *   of retries and either return a new request with fewer retries or
 *   call the handleTimeout call back
 * - if processTimeout returns a new request than process it as in Making
 *   a request 
 * 
 * Thread Details:
 * 
 * 1.  The reply processor that will pull replies off the linked
 *     blocking queue and process them.  This will result in calling the
 *     PingResponseCallback handleReply method.
 * 
 * 2.  The timeout processor that will pull PingRequests off of a
 *     DelayQueue.  A DelayQueue does not allow things to be removed from
 *     them until the timeout has expired.
 * 
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
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class RequestTracker<ReqIdT, ReqT extends Request<ReqIdT, ReqT, ReplyT>, ReplyT extends Reply<ReqIdT>> {
    
    private static final Logger s_log = Logger.getLogger(RequestTracker.class);
    
    private Messenger<ReqT, ReplyT> m_messenger;
    private Map<ReqIdT, ReqT> m_pendingRequests;
    private BlockingQueue<ReplyT> m_pendingReplyQueue;
    private DelayQueue<ReqT> m_timeoutQueue;
    
    private Thread m_replyProcessor;
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
    public RequestTracker(String name, Messenger<ReqT, ReplyT> messenger) throws IOException {
        
	    m_pendingRequests = Collections.synchronizedMap(new HashMap<ReqIdT, ReqT>());
	    m_pendingReplyQueue = new LinkedBlockingQueue<ReplyT>();
	    m_timeoutQueue = new DelayQueue<ReqT>();
	    
	    m_replyProcessor = new Thread(name+"-Reply-Processor") {
	        public void run() {
	            try {
	                processReplies();
	            } catch (InterruptedException e) {
                    errorf("Thread %s interrupted!", this);
	            } catch (Throwable t) {
                    errorf(t, "Unexpected exception on Thread %s!", this);
	            }
	        }
	    };
	    
	    m_timeoutProcessor = new Thread(name+"-Timeout-Processor") {
	        public void run() {
	            try {
	                processTimeouts();
	            } catch (InterruptedException e) {
                    errorf("Thread %s interrupted!", this);
	            } catch (Throwable t) {
                    errorf(t, "Unexpected exception on Thread %s!", this);
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
            m_messenger.start(m_pendingReplyQueue);
            m_timeoutProcessor.start();
            m_replyProcessor.start();
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
    public void sendRequest(ReqT request) throws IOException {
        assertStarted();
        synchronized(m_pendingRequests) {
            ReqT oldRequest = m_pendingRequests.get(request.getId());
            if (oldRequest != null) {
            	request.processError(new IllegalStateException("Duplicate request; keeping old request: "+oldRequest+"; removing new request: "+request));
            	return;
            }
            m_pendingRequests.put(request.getId(), request);
            m_messenger.sendRequest(request);
        }
        debugf("Scheding timeout for request to %s in %d ms", request, request.getDelay(TimeUnit.MILLISECONDS));
        m_timeoutQueue.offer(request);
    }

	private void processReplies() throws InterruptedException {
	    while (true) {
	        ReplyT reply = m_pendingReplyQueue.take();
            debugf("Found a reply to process: %s", reply);
	        ReqIdT id = reply.getRequestId();
	        debugf("Looking for request with Id: %s in map %s", id, m_pendingRequests);
	        ReqT request = m_pendingRequests.remove(id);
	        if (request != null) {
	            processReply(reply, request);
	        } else {
	            debugf("No request found for reply %s", reply);
	        }
	    }
    }

    private void processReply(ReplyT reply, ReqT request) {
        try {
            debugf("Processing reply %s for request %s", reply, request);
            request.processResponse(reply);
        } catch (Throwable t) {
            errorf(t, "Unexpected error processingResponse to request: %s, reply is %s", request, reply);
        }
    }

	private void processTimeouts() throws InterruptedException {  
	    while (true) {
	        ReqT timedOutRequest = m_timeoutQueue.take();
            debugf("Found a possibly timedout request: %s", timedOutRequest);
	        ReqT pendingRequest = m_pendingRequests.remove(timedOutRequest.getId());
            if (pendingRequest == timedOutRequest) {
	            // then this request is still pending so we must time it out
	            ReqT retry = processTimeout(timedOutRequest);
	            if (retry != null) {
	                try {
                        sendRequest(retry);
                    } catch (Exception e) {
                        retry.processError(e);
                    }
	            }
	        } else if (pendingRequest != null) {
	            errorf("Uh oh! A pending request %s with the same id exists but is not the timout request %s from the queue!", pendingRequest, timedOutRequest);
	        }
	        
	    }
	}

    private ReqT processTimeout(ReqT request) {
        try {
            debugf("Processing timeout for: %s", request);
            return request.processTimeout();
        } catch (Throwable t) {
            errorf(t, "Unexpected error processingTimout to request: %s", request);
            return null;
        }
    }
	
    private Category log() {
        return s_log;
    }
    
    private void debugf(String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args));
        }
    }

    private void errorf(String format, Object... args) {
        log().error(String.format(format, args));
    }

    private void errorf(Throwable t, String format, Object... args) {
        log().error(String.format(format, args), t);
    }

}
