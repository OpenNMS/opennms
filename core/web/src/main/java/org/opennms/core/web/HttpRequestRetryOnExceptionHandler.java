/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.web;

import java.util.Collections;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

/**
 * This retry handler uses an empty list of "non-retryable" exceptions so that
 * if any exception is thrown during the HTTP operation, the operation will
 * still be retried.
 * 
 * @author Seth
 * @author Alejandro
 *
 */
public class HttpRequestRetryOnExceptionHandler extends DefaultHttpRequestRetryHandler {

	/**
	 * Calls {@link HttpRequestRetryOnExceptionHandler(int, boolean, Collection<Class<? extends IOException>>)}
	 * with {@link Collections#emptyList()} as the third argument.
	 * 
	 * @param retryCount
	 * @param requestSentRetryEnabled
	 */
	public HttpRequestRetryOnExceptionHandler(int retryCount, boolean requestSentRetryEnabled) {
		super(retryCount, requestSentRetryEnabled, Collections.emptyList());
	}
}
