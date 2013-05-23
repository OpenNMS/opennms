/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseCallback;
import org.opennms.sms.reflector.smsservice.SmsResponse;
import org.opennms.sms.reflector.smsservice.UssdResponse;
import org.smslib.InboundMessage;
import org.smslib.USSDResponse;

public class TestCallback implements MobileMsgResponseCallback {
    
    CountDownLatch m_latch = new CountDownLatch(1);
    AtomicReference<MobileMsgResponse> m_response = new AtomicReference<MobileMsgResponse>(null);

    
    MobileMsgResponse getResponse() throws InterruptedException {
        m_latch.await();
        return m_response.get();
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleError(org.opennms.sms.reflector.smsservice.SmsRequest, java.lang.Throwable)
     */
    @Override
    public void handleError(MobileMsgRequest request, Throwable t) {
        System.err.println("Error processing SmsRequest: " + request);
        m_latch.countDown();
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleResponse(org.opennms.sms.reflector.smsservice.SmsRequest, org.opennms.sms.reflector.smsservice.SmsResponse)
     */
    @Override
    public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse response) {
        m_response.set(response);
        m_latch.countDown();
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleTimeout(org.opennms.sms.reflector.smsservice.SmsRequest)
     */
    @Override
    public void handleTimeout(MobileMsgRequest request) {
        System.err.println("Timeout waiting for SmsRequest: " + request);
        m_latch.countDown();
    }

    /**
     * @return
     * @throws InterruptedException 
     */
    public InboundMessage getMessage() throws InterruptedException {
        MobileMsgResponse response = getResponse();
        if (response instanceof SmsResponse) {
            return ((SmsResponse)response).getMessage();
        }
        return null;
        
    }
    
    public USSDResponse getUSSDResponse() throws InterruptedException{
        MobileMsgResponse response = getResponse();
        if (response instanceof UssdResponse) {
            return ((UssdResponse)response).getMessage();
        }
        return null;
    }
    
}