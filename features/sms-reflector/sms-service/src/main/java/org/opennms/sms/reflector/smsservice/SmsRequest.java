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
 * Modifications:
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.sms.reflector.smsservice;


import java.util.concurrent.TimeUnit;

import org.smslib.OutboundMessage;
import org.springframework.core.style.ToStringCreator;

/**
 * @author brozow
 *
 */
public class SmsRequest extends MobileMsgRequest {

    private OutboundMessage m_msg;

    /**
     * @param msg
     * @param timeout
     * @param retries
     * @param cb
     * @param responseMatcher
     */
    public SmsRequest(OutboundMessage msg, long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher responseMatcher) {
        super(timeout, retries, cb, responseMatcher);
        m_msg = msg;
        
    }
    
    public int getValidityPeriodInHours() {
        return m_msg.getValidityPeriod();
    }
    
    public void setValidityPeriodInHours(int validityPeriod) {
        m_msg.setValidityPeriod(validityPeriod);
    }

    /**
     * @return the originator
     */
    public String getOriginator() {
        return m_msg.getFrom();
    }

    /**
     * @param originator the originator to set
     */
    public void setOriginator(String originator) {
        m_msg.setFrom(originator);
    }

    /**
     * @return the recipient
     */
    public String getRecipient() {
        return m_msg.getRecipient();
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(String recipient) {
        m_msg.setRecipient(recipient);
    }

    /**
     * @return the text
     */
    public String getText() {
        return m_msg.getText();
    }

    @Override
    public String getId() {
        return m_msg.getRecipient();
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.MobileMsgRequest#createNextRetry()
     */
    @Override
    public MobileMsgRequest createNextRetry() {
        if (getRetries() > 0) {
            return new SmsRequest(m_msg, getTimeout(), getRetries()-1, getCb(), getResponseMatcher());
        } else {
            return null;
        }
        
    }

    public OutboundMessage getMessage() {
        return m_msg;
    }
    
    public String toString() {
    	return new ToStringCreator(this)
    		.append("recipient", getRecipient())
    		.append("text", getText())
    		.toString();
    }

}
