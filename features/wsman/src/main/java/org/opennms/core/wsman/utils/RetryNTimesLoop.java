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
package org.opennms.core.wsman.utils;

import org.opennms.core.wsman.exceptions.HTTPException;
import org.opennms.core.wsman.exceptions.UnauthorizedException;
import org.opennms.core.wsman.exceptions.WSManException;

/**
 * Retry loop inspired by the one implemented in the Apache Curator Client.
 *
 * Example usage:
 * <pre>
 * {@code
 * final RetryNTimesLoop retryLoop = new RetryNTimesLoop(3);
 * while (retryLoop.shouldContinue()) {
 *     try {
 *         node = client.get(resourceUri, selectors);
 *         break;
 *     } catch (WSManException e) {
 *         retryLoop.takeException(e);
 *     }
 * }
 * </pre>
 * @author jwhite
 */
public class RetryNTimesLoop {

    private final int m_numberOfRetries;
    private int m_numberOfContinues = 0;

    public RetryNTimesLoop(int numberOfRetries) {
        if (numberOfRetries < 0) {
            throw new IllegalArgumentException("Number of retries must be positive.");
        }
        m_numberOfRetries = numberOfRetries;
    }

    public boolean shouldContinue() {
        final boolean shouldContinue = m_numberOfRetries >= m_numberOfContinues;
        m_numberOfContinues++;
        return shouldContinue;
    }

    public void takeException(WSManException e) throws WSManException {
        if (e instanceof HTTPException && !(e instanceof UnauthorizedException)) {
            // Retry if there was an issue with the transport, with the exception
            // of authentication/authorization failures
        }
        // Don't retry on SOAP fault, or any other WSManException
        throw e;
    }
}
