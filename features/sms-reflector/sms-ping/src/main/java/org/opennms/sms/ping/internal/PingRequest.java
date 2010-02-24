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
 * Created August 22, 2007
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
package org.opennms.sms.ping.internal;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.opennms.protocols.rt.Request;
import org.opennms.sms.ping.PingRequestId;
import org.opennms.sms.ping.PingResponseCallback;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 * 
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
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
    
    public PingRequest(PingRequestId id, long timeout, int retries, PingResponseCallback cb) {
        this(id, timeout, retries, Logger.getLogger(PingRequest.class), cb);
    }

    public PingRequestId getId() {
        return m_id;
    }

    public int getRetries() {
        return m_retries;
    }

    public long getTimeout() {
        return m_timeout;
    }
    
    public OutboundMessage getRequest() {
        return m_request;
    }

    public InboundMessage getResponse() {
        return m_response;
    }


    public long getExpiration() {
        return m_expiration;
    }
    
    private Logger log() {
        return m_log;
    }

    public boolean processResponse(PingReply reply) {
    	setResponseTimestamp(reply.getReceiveTimestamp());
    	processResponse(reply.getPacket());
    	return true;
    }

    private void processResponse(InboundMessage packet) {
        m_response = packet;
        log().debug(System.currentTimeMillis()+": Ping Response Received "+this);
        m_callback.handleResponse(this, packet);
    }

    public PingRequest processTimeout() {
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
    }
    
    public boolean isExpired() {
        return (System.currentTimeMillis() >= getExpiration());
    }

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

    public long getDelay(TimeUnit unit) {
        return unit.convert(getExpiration() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed request) {
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        long otherDelay = request.getDelay(TimeUnit.MILLISECONDS);
        if (myDelay < otherDelay) return -1;
        if (myDelay == otherDelay) return 0;
        return 1;
    }

    public void processError(Throwable t) {
        m_callback.handleError(this, getRequest(), t);
    }
    
    public void setSentTimestamp(Long millis){
    	m_sentTimestamp = millis;
    }
    
    public void setResponseTimestamp(Long millis){
    	m_responseTimestamp = millis;
    }
    
    public long getRoundTripTime(){
    	return m_responseTimestamp - m_sentTimestamp;
    }

}
