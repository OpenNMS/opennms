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
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler;

/**
 * <p>Abstract MobileSequenceRequest class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="request")
public abstract class MobileSequenceRequest extends MobileSequenceOperation {
    private MobileSequenceTransaction m_transaction;
	private String m_text;

	/**
	 * <p>Constructor for MobileSequenceRequest.</p>
	 */
	public MobileSequenceRequest() {
		super();
	}
	
	/**
	 * <p>Constructor for MobileSequenceRequest.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public MobileSequenceRequest(String label, String text) {
		super(label);
		setText(text);
	}

	/**
	 * <p>Constructor for MobileSequenceRequest.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public MobileSequenceRequest(String gatewayId, String label, String text) {
		super(gatewayId, label);
		setText(text);
	}

	/**
	 * <p>getText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="text")
	public String getText() {
		return m_text;
	}
	
	/**
	 * <p>setText</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public void setText(String text) {
		m_text = text;
	}
	
	/**
	 * <p>getTransaction</p>
	 *
	 * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
	 */
	@XmlTransient
	public MobileSequenceTransaction getTransaction() {
	    return m_transaction;
	}
	
	/**
	 * <p>setTransaction</p>
	 *
	 * @param transaction a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
	 */
	public void setTransaction(MobileSequenceTransaction transaction) {
	    m_transaction = transaction;
	}
	
	/**
	 * <p>getGatewayId</p>
	 *
	 * @param defaultGatewayId a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getGatewayId(String defaultGatewayId) {
		return getGatewayId() == null? defaultGatewayId : getGatewayId();
	}

	/**
	 * <p>getLabel</p>
	 *
	 * @param defaultLabel a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel(String defaultLabel) {
		return getLabel() == null ? defaultLabel : getLabel();
	}

    /**
     * <p>getGatewayIdForRequest</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getGatewayIdForRequest() {
        return getGatewayId(getTransaction().getDefaultGatewayId());
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
            .append("text", getText())
            .toString();
    }

    /**
     * <p>send</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param responseHandler a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler} object.
     */
    public abstract void send(MobileSequenceSession session, MobileMsgResponseHandler responseHandler);


}
