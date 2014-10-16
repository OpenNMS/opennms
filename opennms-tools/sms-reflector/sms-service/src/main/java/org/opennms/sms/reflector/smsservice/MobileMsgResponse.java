/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector.smsservice;

import org.opennms.protocols.rt.Response;

/**
 * SmsResponse
 *
 * @author brozow
 * @version $Id: $
 */
public abstract class MobileMsgResponse implements Response {
	private MobileMsgRequest m_request;
	private long m_receiveTime;

	/**
	 * <p>Constructor for MobileMsgResponse.</p>
	 *
	 * @param receiveTime a long.
	 */
	public MobileMsgResponse(long receiveTime) {
		m_receiveTime = receiveTime;
	}

	/**
	 * <p>setRequest</p>
	 *
	 * @param req a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
	 */
	public void setRequest(MobileMsgRequest req) {
		m_request = req;
	}

	/**
	 * <p>getRequest</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
	 */
	public MobileMsgRequest getRequest() {
		return m_request;
	}

	/**
	 * <p>getReceiveTime</p>
	 *
	 * @return a long.
	 */
	public long getReceiveTime() {
		return m_receiveTime;
	}
	
    /**
     * <p>getText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getText();

}
