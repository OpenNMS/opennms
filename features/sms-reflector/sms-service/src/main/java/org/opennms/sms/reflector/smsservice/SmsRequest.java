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
public class SmsRequest implements Request<String, SmsRequest, SmsResponse> {
    
    String m_originator;
    String m_recipient;
    String m_text;
    SmsResponseMatcher m_responseMatcher;

    public SmsRequest(String originator, String recipient, String text, SmsResponseMatcher responseMatcher) {
        m_originator = originator;
        m_recipient = recipient;
        m_text = text;
        m_responseMatcher = responseMatcher;
    }
    
    /**
     * @return the originator
     */
    public String getOriginator() {
        return m_originator;
    }

    /**
     * @param originator the originator to set
     */
    public void setOriginator(String originator) {
        m_originator = originator;
    }

    /**
     * @return the recipient
     */
    public String getRecipient() {
        return m_recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(String recipient) {
        m_recipient = recipient;
    }

    /**
     * @return the text
     */
    public String getText() {
        return m_text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        m_text = text;
    }

    public long getDelay(TimeUnit unit) {
        throw new UnsupportedOperationException("Request<String,SmsRequest,SmsResponse>.getDelay is not yet implemented");
    }

    public String getId() {
        throw new UnsupportedOperationException("Request<String,SmsRequest,SmsResponse>.getId is not yet implemented");
    }

    public void processError(Throwable t) {
        throw new UnsupportedOperationException("Request<String,SmsRequest,SmsResponse>.processError is not yet implemented");
    }

    public boolean processResponse(SmsResponse reply) {
        throw new UnsupportedOperationException("Request<String,SmsRequest,SmsResponse>.processResponse is not yet implemented");
    }

    public SmsRequest processTimeout() {
        throw new UnsupportedOperationException("Request<String,SmsRequest,SmsResponse>.processTimeout is not yet implemented");
    }

    public int compareTo(Delayed o) {
        throw new UnsupportedOperationException("Comparable<Delayed>.compareTo is not yet implemented");
    }

    public boolean matches(SmsResponse response) {
        return m_responseMatcher.matches(this, response);
    }


}
