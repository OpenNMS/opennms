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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

/**
 * <p>Abstract MobileSequenceResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class MobileSequenceResponse extends MobileSequenceOperation {

    private List<SequenceResponseMatcher> m_matchers = Collections.synchronizedList(new ArrayList<SequenceResponseMatcher>());
	
	private MobileSequenceTransaction m_transaction;

	/**
	 * <p>Constructor for MobileSequenceResponse.</p>
	 */
	public MobileSequenceResponse() {
		super();
	}
	
	/**
	 * <p>Constructor for MobileSequenceResponse.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceResponse(String label) {
		super(label);
	}

	/**
	 * <p>Constructor for MobileSequenceResponse.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}
	
	/**
	 * <p>getMatchers</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@XmlElementRef
	public List<SequenceResponseMatcher> getMatchers() {
		return m_matchers;
	}
	
	/**
	 * <p>setMatchers</p>
	 *
	 * @param matchers a {@link java.util.List} object.
	 */
	public void setMatchers(List<SequenceResponseMatcher> matchers) {
		if (m_matchers == matchers) return;
		m_matchers.clear();
		m_matchers.addAll(matchers);
	}
	
	/**
	 * <p>addMatcher</p>
	 *
	 * @param matcher a {@link org.opennms.sms.monitor.internal.config.SequenceResponseMatcher} object.
	 */
	public void addMatcher(SequenceResponseMatcher matcher) {
		m_matchers.add(matcher);
	}
	
    /**
     * <p>getEffectiveLabel</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @return a {@link java.lang.String} object.
     */
    public String getEffectiveLabel(MobileSequenceSession session) {
        return getLabel() != null ? session.substitute(getLabel()) : getTransaction().getResponseLabel(session, this); 
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
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
    @Override
	public String toString() {
        return new ToStringBuilder(this)
            .append("gatewayId", getGatewayId())
            .append("label", getLabel())
            .append("matchers", getMatchers())
            .toString();
    }

	/**
	 * <p>matchesResponseType</p>
	 *
	 * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
	 * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
	 * @return a boolean.
	 */
	protected abstract boolean matchesResponseType(MobileMsgRequest request, MobileMsgResponse response);
    
    private boolean matchesCriteria(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {

        for (SequenceResponseMatcher m : getMatchers()) {
            if (!m.matches(session, request, response)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>matches</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
     * @return a boolean.
     */
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        return matchesResponseType(request, response) && matchesCriteria(session, request, response);
    }

    /**
     * <p>processResponse</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
     */
    public abstract void processResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response);

}
