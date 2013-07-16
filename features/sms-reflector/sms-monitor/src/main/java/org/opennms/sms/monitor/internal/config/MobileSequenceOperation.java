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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract MobileSequenceOperation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class MobileSequenceOperation {
    private static final Logger LOG = LoggerFactory.getLogger(MobileSequenceOperation.class);
	/**
	 * <p>Constructor for MobileSequenceOperation.</p>
	 */
	public MobileSequenceOperation() {
	}

	/**
	 * <p>Constructor for MobileSequenceOperation.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceOperation(String label) {
		setLabel(label);
	}
	
	/**
	 * <p>Constructor for MobileSequenceOperation.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceOperation(String gatewayId, String label) {
		setGatewayId(gatewayId);
		setLabel(label);
	}

	private String m_gatewayId;
	private String m_label;
	
	/**
	 * <p>getGatewayId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="gatewayId")
	public String getGatewayId() {
		return m_gatewayId;
	}
	
	/**
	 * <p>setGatewayId</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 */
	public void setGatewayId(String gatewayId) {
		m_gatewayId = gatewayId;
	}
	
	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="label")
	public String getLabel() {
		return m_label;
	}

	/**
	 * <p>setLabel</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		m_label = label;
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
			.toString();
	}
}
