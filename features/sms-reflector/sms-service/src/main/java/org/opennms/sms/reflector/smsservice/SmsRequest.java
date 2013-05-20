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


import org.smslib.OutboundMessage;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>SmsRequest class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class SmsRequest extends MobileMsgRequest {

    private OutboundMessage m_msg;

    /**
     * <p>Constructor for SmsRequest.</p>
     *
     * @param msg a {@link org.smslib.OutboundMessage} object.
     * @param timeout a long.
     * @param retries a int.
     * @param cb a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseCallback} object.
     * @param responseMatcher a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
     */
    public SmsRequest(OutboundMessage msg, long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher responseMatcher) {
        super(timeout, retries, cb, responseMatcher);
        m_msg = msg;
        
    }
    
    /**
     * <p>getValidityPeriodInHours</p>
     *
     * @return a int.
     */
    public int getValidityPeriodInHours() {
        return m_msg.getValidityPeriod();
    }
    
    /**
     * <p>setValidityPeriodInHours</p>
     *
     * @param validityPeriod a int.
     */
    public void setValidityPeriodInHours(int validityPeriod) {
        m_msg.setValidityPeriod(validityPeriod);
    }

    /**
     * <p>getOriginator</p>
     *
     * @return the originator
     */
    public String getOriginator() {
        return m_msg.getFrom();
    }

    /**
     * <p>setOriginator</p>
     *
     * @param originator the originator to set
     */
    public void setOriginator(String originator) {
        m_msg.setFrom(originator);
    }

    /**
     * <p>getRecipient</p>
     *
     * @return the recipient
     */
    public String getRecipient() {
        return m_msg.getRecipient();
    }

    /**
     * <p>setRecipient</p>
     *
     * @param recipient the recipient to set
     */
    public void setRecipient(String recipient) {
        m_msg.setRecipient(recipient);
    }

    /**
     * <p>getText</p>
     *
     * @return the text
     */
    public String getText() {
        return m_msg.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return m_msg.getRecipient();
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.MobileMsgRequest#createNextRetry()
     */
    /** {@inheritDoc} */
    @Override
    public MobileMsgRequest createNextRetry() {
        if (getRetries() > 0) {
            return new SmsRequest(m_msg, getTimeout(), getRetries()-1, getCb(), getResponseMatcher());
        } else {
            return null;
        }
        
    }

    /**
     * <p>getMessage</p>
     *
     * @return a {@link org.smslib.OutboundMessage} object.
     */
    public OutboundMessage getMessage() {
        return m_msg;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
    	return new ToStringCreator(this)
    		.append("recipient", getRecipient())
    		.append("text", getText())
    		.toString();
    }

}
