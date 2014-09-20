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

import java.net.SocketTimeoutException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.Callback;
public class MobileMsgCallbackAdapter implements MobileMsgResponseCallback {
	private final Callback<MobileMsgResponse> cb;

	/**
	 * <p>Constructor for MobileMsgCallbackAdapter.</p>
	 *
	 * @param cb a {@link org.opennms.core.tasks.Callback} object.
	 */
	public MobileMsgCallbackAdapter(Callback<MobileMsgResponse> cb) {
		this.cb = cb;
	}
	
	/**
	 * <p>Getter for the field <code>cb</code>.</p>
	 *
	 * @return a {@link org.opennms.core.tasks.Callback} object.
	 */
	public Callback<MobileMsgResponse> getCb() {
	    return cb;
	}

	/** {@inheritDoc} */
        @Override
	public void handleError(final MobileMsgRequest request, final Throwable t) {
		getCb().handleException(t);
	}

	/** {@inheritDoc} */
        @Override
	public boolean handleResponse(final MobileMsgRequest request, final MobileMsgResponse packet) {
		getCb().complete(packet);
		return true;
	}

	/** {@inheritDoc} */
        @Override
	public void handleTimeout(final MobileMsgRequest request) {
		getCb().handleException(new SocketTimeoutException("timed out processing request " + request));
	}
	
        @Override
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("callback", cb)
	        .toString();
	}
}
