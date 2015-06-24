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

package org.opennms.sms.ping;

import org.opennms.sms.ping.internal.PingRequest;
import org.smslib.Message;

/**
 * <p>PingResponseCallback interface.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @version $Id: $
 */
public interface PingResponseCallback {

	/**
	 * <p>handleResponse</p>
	 *
	 * @param request a {@link org.opennms.sms.ping.internal.PingRequest} object.
	 * @param packet a {@link org.smslib.Message} object.
	 */
	public void handleResponse(PingRequest request, Message packet);
	/**
	 * <p>handleTimeout</p>
	 *
	 * @param request a {@link org.opennms.sms.ping.internal.PingRequest} object.
	 * @param packet a {@link org.smslib.Message} object.
	 */
	public void handleTimeout(PingRequest request, Message packet);
    /**
     * <p>handleError</p>
     *
     * @param request a {@link org.opennms.sms.ping.internal.PingRequest} object.
     * @param packet a {@link org.smslib.Message} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void handleError(PingRequest request, Message packet, Throwable t);

}
