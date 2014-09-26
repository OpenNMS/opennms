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

package org.opennms.sms.reflector.smsservice;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.protocols.rt.Request;

/**
 * SmsRequest
 *
 * @author brozow
 * @version $Id: $
 */
public abstract class MobileMsgRequest implements Request<String, MobileMsgRequest, MobileMsgResponse> {
    
    private long m_timeout;
    private int m_retries;
    private MobileMsgResponseCallback m_cb;
    private MobileMsgResponseMatcher m_responseMatcher;
    
    private long m_expiration;
	private long m_sentTime;
	
	private volatile boolean m_processed = false;

    /**
     * <p>Constructor for MobileMsgRequest.</p>
     *
     * @param timeout a long.
     * @param retries a int.
     * @param cb a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseCallback} object.
     * @param responseMatcher a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
     */
    public MobileMsgRequest(long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher responseMatcher) {
        m_timeout = timeout;
        m_retries = retries;
        m_cb = cb;
        m_responseMatcher = responseMatcher;
    }
    
    /**
     * <p>getSentTime</p>
     *
     * @return a long.
     */
    public long getSentTime() {
    	return m_sentTime;
    }
    
    /**
     * <p>getTimeout</p>
     *
     * @return the timeout
     */
    public long getTimeout() {
        return m_timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout the timeout to set
     */
    public void setTimeout(long timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getRetries</p>
     *
     * @return the retries
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * <p>getCb</p>
     *
     * @return the cb
     */
    public MobileMsgResponseCallback getCb() {
        return m_cb;
    }

    /**
     * <p>setCb</p>
     *
     * @param cb the cb to set
     */
    public void setCb(MobileMsgResponseCallback cb) {
        m_cb = cb;
    }

    /**
     * <p>getResponseMatcher</p>
     *
     * @return the responseMatcher
     */
    public MobileMsgResponseMatcher getResponseMatcher() {
        return m_responseMatcher;
    }

    /**
     * <p>setResponseMatcher</p>
     *
     * @param responseMatcher the responseMatcher to set
     */
    public void setResponseMatcher(MobileMsgResponseMatcher responseMatcher) {
        m_responseMatcher = responseMatcher;
    }

    /**
     * <p>setSendTimestamp</p>
     *
     * @param timeInMillis a long.
     */
    public void setSendTimestamp(long timeInMillis) {
    	m_sentTime = timeInMillis;
        m_expiration = timeInMillis + m_timeout;
    }

    /** {@inheritDoc} */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(m_expiration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getId();

    /** {@inheritDoc} */
    @Override
    public void processError(Throwable t) {
        try {
            m_cb.handleError(this, t);
        } finally {
            m_processed = true;
        }
    }

    /**
     * <p>processResponse</p>
     *
     * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
     * @return a boolean.
     */
    @Override
    public boolean processResponse(MobileMsgResponse response) {
        try {
            response.setRequest(this);
            return m_cb.handleResponse(this, response);
        } finally {
            m_processed = true;
        }
    }

    /**
     * <p>processTimeout</p>
     *
     * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     */
    @Override
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
    
    /**
     * <p>createNextRetry</p>
     *
     * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     */
    public abstract MobileMsgRequest createNextRetry();

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link java.util.concurrent.Delayed} object.
     * @return a int.
     */
    @Override
    public int compareTo(Delayed o) {
        long thisVal = this.getDelay(TimeUnit.NANOSECONDS);
        long anotherVal = o.getDelay(TimeUnit.NANOSECONDS);
        return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }

    /**
     * <p>matches</p>
     *
     * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
     * @return a boolean.
     */
    public boolean matches(MobileMsgResponse response) {
        return m_responseMatcher.matches(this, response);
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("matcher", m_responseMatcher)
            .append("callback", m_cb)
            .append("timeout", m_timeout)
            .append("retries", m_retries)
            .append("expiration", m_expiration)
            .append("sent", m_sentTime)
            .append("processed", m_processed)
            .toString();
    }
    
}
