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

package org.opennms.sms.monitor.internal.config;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler;

/**
 * <p>SmsSequenceRequest class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="sms-request")
public class SmsSequenceRequest extends MobileSequenceRequest {
	private String m_recipient;
	private int m_validityPeriodInHours = 1;

	/**
	 * <p>Constructor for SmsSequenceRequest.</p>
	 */
	public SmsSequenceRequest() {
	}

	/**
	 * <p>Constructor for SmsSequenceRequest.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public SmsSequenceRequest(String label, String text) {
		super(label, text);
	}
	
	/**
	 * <p>Constructor for SmsSequenceRequest.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public SmsSequenceRequest(String gatewayId, String label, String text) {
		super(gatewayId, label, text);
	}

	/**
	 * <p>getRecipient</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="recipient")
	public String getRecipient() {
		return m_recipient;
	}
	
	/**
	 * <p>setRecipient</p>
	 *
	 * @param recipient a {@link java.lang.String} object.
	 */
	public void setRecipient(String recipient) {
		m_recipient = recipient;
	}

	/**
	 * <p>getValidityPeriodInHours</p>
	 *
	 * @return a int.
	 */
	@XmlAttribute(name="validity-in-hours", required=false)
    public int getValidityPeriodInHours() {
        return m_validityPeriodInHours;
    }

    /**
     * <p>setValidityPeriodInHours</p>
     *
     * @param validityPeriodInHours a int.
     */
    public void setValidityPeriodInHours(int validityPeriodInHours) {
        m_validityPeriodInHours = validityPeriodInHours;
    }

    /** {@inheritDoc} */
    @Override
    public void send(MobileSequenceSession session, MobileMsgResponseHandler responseHandler) {
        session.sendSms(getGatewayIdForRequest(), getRecipient(), getText(), getValidityPeriodInHours(), responseHandler);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("gatewayId", getGatewayId())
            .append("label", getLabel())
            .append("recipient", getRecipient())
            .append("text", getText())
            .toString();
    }

}
