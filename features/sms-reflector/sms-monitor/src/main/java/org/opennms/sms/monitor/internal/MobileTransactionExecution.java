/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.sms.monitor.internal;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.core.tasks.Callback;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.monitor.internal.config.MobileSequenceResponse;
import org.opennms.sms.monitor.internal.config.MobileSequenceTransaction;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler;

/**
 * MobileTransactionExecution
 *
 * @author brozow
 * @version $Id: $
 */
public class MobileTransactionExecution {
    
    /**
     * TransactionResponseHandler
     *
     * @author brozow
     */
    private final class TransactionResponseHandler implements MobileMsgResponseHandler {
        private final Callback<MobileMsgResponse> m_cb;
        private final MobileSequenceSession m_session;
        private final Set<MobileSequenceResponse> m_pendingResponses;

        private TransactionResponseHandler(MobileSequenceSession session, Callback<MobileMsgResponse> cb) {
            m_cb = cb;
            m_session = session;
            m_pendingResponses = Collections.synchronizedSet(new LinkedHashSet<MobileSequenceResponse>(getTransaction().getResponses()));
        }

        public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {

            synchronized (m_pendingResponses) {

                for(MobileSequenceResponse r : m_pendingResponses) {
                    if (r.matches(m_session, request, response)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public void handleTimeout(MobileMsgRequest request) {
            SocketTimeoutException err = new SocketTimeoutException("timed out processing request " + request);
            setError(err);
            m_cb.handleException(err);
        }

        public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse response) {
            if (request != null) setSendTime(request.getSentTime());
            if (response != null) setReceiveTime(response.getReceiveTime());
            
            synchronized (m_pendingResponses) {
                // remove processing response
                for(Iterator<MobileSequenceResponse> it = m_pendingResponses.iterator(); it.hasNext(); ) {
                    MobileSequenceResponse r = it.next();
                    if (r.matches(m_session, request, response)) {
                        
                        r.processResponse(m_session, request, response);
                        
                        it.remove();
                    }
                }

            }
            m_cb.complete(response);
            
            // return true only if all of the expected responses have been processed
            return !m_pendingResponses.isEmpty();
        }

        public void handleError(MobileMsgRequest request, Throwable t) {
            setError(t);
            m_cb.handleException(t);
        }
    }

    private MobileSequenceTransaction m_transaction;
    private Long m_sendTime;
    private Long m_receiveTime;
    
    private Throwable m_error;
    

    /**
     * <p>Constructor for MobileTransactionExecution.</p>
     *
     * @param transaction a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
     */
    public MobileTransactionExecution(MobileSequenceTransaction transaction) {
        m_transaction = transaction;
    }
    
    private void setSendTime(Long sendTime) {
        m_sendTime = sendTime;
    }
    
    private void setReceiveTime(Long receiveTime) {
        m_receiveTime = receiveTime;
    }
    
    /**
     * <p>getLatency</p>
     *
     * @return the latency
     */
    public Long getLatency() {
        return m_sendTime == null || m_receiveTime == null ? null : m_receiveTime - m_sendTime;
    }


    /**
     * <p>getError</p>
     *
     * @return the error
     */
    public Throwable getError() {
        return m_error;
    }

    /**
     * <p>setError</p>
     *
     * @param error the error to set
     */
    public void setError(Throwable error) {
        m_error = error;
    }

    /**
     * <p>getTransaction</p>
     *
     * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
     */
    public MobileSequenceTransaction getTransaction() {
        return m_transaction;
    }
    
    Callback<MobileMsgResponse> getCallback() {
        return new Callback<MobileMsgResponse>() {
            public void complete(MobileMsgResponse t) {
                if (t != null) {
                    setSendTime(t.getRequest().getSentTime());
                    setReceiveTime(t.getReceiveTime());
                }
            }
        
            public void handleException(Throwable t) {
                setError(t);
            }
        };
    }

    void sendRequest(MobileSequenceSession session, Callback<MobileMsgResponse> cb) {
        getTransaction().sendRequest(session, new TransactionResponseHandler(session, cb));
    }

}
