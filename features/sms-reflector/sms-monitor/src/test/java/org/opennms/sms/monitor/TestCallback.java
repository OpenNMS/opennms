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
    public void handleError(MobileMsgRequest request, Throwable t) {
        System.err.println("Error processing SmsRequest: " + request);
        m_latch.countDown();
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleResponse(org.opennms.sms.reflector.smsservice.SmsRequest, org.opennms.sms.reflector.smsservice.SmsResponse)
     */
    public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse response) {
        m_response.set(response);
        m_latch.countDown();
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleTimeout(org.opennms.sms.reflector.smsservice.SmsRequest)
     */
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