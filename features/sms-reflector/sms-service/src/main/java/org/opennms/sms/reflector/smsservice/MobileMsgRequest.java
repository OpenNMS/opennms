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
package org.opennms.sms.reflector.smsservice;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.opennms.protocols.rt.Request;

/**
 * SmsRequest
 *
 * @author brozow
 */
public abstract class MobileMsgRequest implements Request<String, MobileMsgRequest, MobileMsgResponse> {
    
    private long m_timeout;
    private int m_retries;
    private MobileMsgResponseCallback m_cb;
    private MobileMsgResponseMatcher m_responseMatcher;
    
    private long m_expiration;
	private long m_sentTime;
	
	private volatile boolean m_processed = false;

    public MobileMsgRequest(long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher responseMatcher) {
        m_timeout = timeout;
        m_retries = retries;
        m_cb = cb;
        m_responseMatcher = responseMatcher;
    }
    
    public long getSentTime() {
    	return m_sentTime;
    }
    
    /**
     * @return the timeout
     */
    public long getTimeout() {
        return m_timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(long timeout) {
        m_timeout = timeout;
    }

    /**
     * @return the retries
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * @return the cb
     */
    public MobileMsgResponseCallback getCb() {
        return m_cb;
    }

    /**
     * @param cb the cb to set
     */
    public void setCb(MobileMsgResponseCallback cb) {
        m_cb = cb;
    }

    /**
     * @return the responseMatcher
     */
    public MobileMsgResponseMatcher getResponseMatcher() {
        return m_responseMatcher;
    }

    /**
     * @param responseMatcher the responseMatcher to set
     */
    public void setResponseMatcher(MobileMsgResponseMatcher responseMatcher) {
        m_responseMatcher = responseMatcher;
    }

    public void setSendTimestamp(long timeInMillis) {
    	m_sentTime = timeInMillis;
        m_expiration = timeInMillis + m_timeout;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(m_expiration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public abstract String getId();

    public void processError(Throwable t) {
        try {
            m_cb.handleError(this, t);
        } finally {
            m_processed = true;
        }
    }

    public boolean processResponse(MobileMsgResponse response) {
        try {
            response.setRequest(this);
            return m_cb.handleResponse(this, response);
        } finally {
            m_processed = true;
        }
    }

    public MobileMsgRequest processTimeout() {
        try {
            MobileMsgRequest retry = createNextRetry();
            if (retry == null) {
                m_cb.handleTimeout(this);
            }
            return retry;
        } finally {
            m_processed = true;
        }
    }
    
    public abstract MobileMsgRequest createNextRetry();

    public int compareTo(Delayed o) {
        long thisVal = this.getDelay(TimeUnit.NANOSECONDS);
        long anotherVal = o.getDelay(TimeUnit.NANOSECONDS);
        return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }

    public boolean matches(MobileMsgResponse response) {
        return m_responseMatcher.matches(this, response);
    }

    public boolean isProcessed() {
        return m_processed;
    }

    
}
