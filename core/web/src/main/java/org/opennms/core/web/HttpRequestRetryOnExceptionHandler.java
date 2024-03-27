/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
