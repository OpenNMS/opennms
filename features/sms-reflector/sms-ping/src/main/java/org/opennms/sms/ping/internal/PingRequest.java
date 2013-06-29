/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.ping.internal;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.opennms.protocols.rt.Request;
import org.opennms.sms.ping.PingRequestId;
import org.opennms.sms.ping.PingResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
final public class PingRequest implements Request<PingRequestId, PingRequest, PingReply> {

    /**
     * The id representing the packet
     */
    private PingRequestId m_id;

	/**
	 * the request packet
	 */
	private OutboundMessage m_request = null;
	
	/**
	 * the response packet
	 */
	private InboundMessage m_response = null;

    /**
     * The callback to use when this object is ready to do something
     */
    private PingResponseCallback m_callback = null;
    
    /**
     * How many retries
     */
    private int m_retries;
    
    /**
     * how long to wait for a response
     */
    private long m_timeout;
    
    /**
     * The expiration time of this request
     */
    private long m_expiration = -1L;
    
    /**
     * The thread logger associated with this request.
     */
    private Logger m_log;

	private Long m_sentTimestamp;

	private Long m_responseTimestamp;
	
	private volatile boolean m_processed = false;
    

    PingRequest(PingRequestId id, long timeout, int retries, Logger logger, PingResponseCallback cb) {
        m_id = id;
        m_retries    = retries;
        m_timeout    = timeout;
        m_log        = logger;
        m_callback   = cb;
        
        m_expiration = System.currentTimeMillis() + timeout;
        
        m_request = new OutboundMessage(id.getDestination(), "ping");
        m_request.setSrcPort(6996);
        m_request.setValidityPeriod(1);
    }
    
    /**
     * <p>Constructor for PingRequest.</p>
     *
     * @param id a {@link org.opennms.sms.ping.PingRequestId} object.
     * @param timeout a long.
     * @param retries a int.
     * @param cb a {@link org.opennms.sms.ping.PingResponseCallback} object.
     */
    public PingRequest(PingRequestId id, long timeout, int retries, PingResponseCallback cb) {
        this(id, timeout, retries, LoggerFactory.getLogger(PingRequest.class), cb);
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link org.opennms.sms.ping.PingRequestId} object.
     */
    @Override
    public PingRequestId getId() {
        return m_id;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a long.
     */
    public long getTimeout() {
        return m_timeout;
    }
    
    /**
     * <p>getRequest</p>
     *
     * @return a {@link org.smslib.OutboundMessage} object.
     */
    public OutboundMessage getRequest() {
        return m_request;
    }

    /**
     * <p>getResponse</p>
     *
     * @return a {@link org.smslib.InboundMessage} object.
     */
    public InboundMessage getResponse() {
        return m_response;
    }


    /**
     * <p>getExpiration</p>
     *
     * @return a long.
     */
    public long getExpiration() {
        return m_expiration;
    }
    
    private Logger log() {
        return m_log;
    }

    /**
     * <p>processResponse</p>
     *
     * @param reply a {@link org.opennms.sms.ping.internal.PingReply} object.
     * @return a boolean.
     */
    @Override
    public boolean processResponse(PingReply reply) {
        try {
            setResponseTimestamp(reply.getReceiveTimestamp());
            processResponse(reply.getPacket());
            return true;
        } finally {
            m_processed = true;
        }
    }

    private void processResponse(InboundMessage packet) {
        m_response = packet;
        log().debug(System.currentTimeMillis()+": Ping Response Received "+this);
        m_callback.handleResponse(this, packet);
    }

    /**
     * <p>processTimeout</p>
     *
     * @return a {@link org.opennms.sms.ping.internal.PingRequest} object.
     */
    @Override
    public PingRequest processTimeout() {
        try {
            PingRequest returnval = null;
            if (this.isExpired()) {
                if (this.getRetries() > 0) {
                    returnval = new PingRequest(getId(), getTimeout(), getRetries() - 1, log(), m_callback);
                    log().debug(System.currentTimeMillis()+": Retrying Ping Request "+returnval);
                } else {
                    log().debug(System.currentTimeMillis()+": Ping Request Timed out "+this);
                    m_callback.handleTimeout(this, getRequest());
                }
            }
            return returnval;
        } finally {
            m_processed = true;
        }
    }
    
    /**
     * <p>isExpired</p>
     *
     * @return a boolean.
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() >= getExpiration());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("ID=").append(getId()).append(',');
        sb.append("Retries=").append(getRetries()).append(",");
        sb.append("Timeout=").append(getTimeout()).append(",");
        sb.append("Expiration=").append(getExpiration()).append(',');
        sb.append("Callback=").append(m_callback).append(',');
        sb.append("Request=").append(m_request);
        sb.append("]");
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(getExpiration() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * <p>compareTo</p>
     *
     * @param request a {@link java.util.concurrent.Delayed} object.
     * @return a int.
     */
    @Override
    public int compareTo(Delayed request) {
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        long otherDelay = request.getDelay(TimeUnit.MILLISECONDS);
        if (myDelay < otherDelay) return -1;
        if (myDelay == otherDelay) return 0;
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public void processError(Throwable t) {
        try {
            m_callback.handleError(this, getRequest(), t);
        } finally {
            m_processed = true;
        }
    }
    
    /**
     * <p>setSentTimestamp</p>
     *
     * @param millis a {@link java.lang.Long} object.
     */
    public void setSentTimestamp(Long millis){
    	m_sentTimestamp = millis;
    }
    
    /**
     * <p>setResponseTimestamp</p>
     *
     * @param millis a {@link java.lang.Long} object.
     */
    public void setResponseTimestamp(Long millis){
    	m_responseTimestamp = millis;
    }
    
    /**
     * <p>getRoundTripTime</p>
     *
     * @return a long.
     */
    public long getRoundTripTime(){
    	return m_responseTimestamp - m_sentTimestamp;
    }

    /**
     * <p>isProcessed</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isProcessed() {
        return m_processed;
    }
    
    

}
