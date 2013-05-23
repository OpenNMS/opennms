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
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

/**
 * <p>Abstract SequenceResponseMatcher class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class SequenceResponseMatcher {
	// Forces this to be an XSD complexType instead of simpleType
	@SuppressWarnings("unused")
	@XmlAttribute(name="dummy", required=false)
	private String m_dummy;
	
	private String m_text;

	/**
	 * <p>Constructor for SequenceResponseMatcher.</p>
	 */
	public SequenceResponseMatcher() {
	}
	
	/**
	 * <p>Constructor for SequenceResponseMatcher.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public SequenceResponseMatcher(String text) {
		setText(text);
	}

	/**
	 * <p>getText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlValue
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
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("text", getText())
			.toString();
	}

	/**
	 * <p>matches</p>
	 *
	 * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
	 * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
	 * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
	 * @return a boolean.
	 */
	public abstract boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response);

}
