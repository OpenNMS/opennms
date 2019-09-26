/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.executors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;

public class LimitedRetriesRequestExecutor implements RequestExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(LimitedRetriesRequestExecutor.class);
    private int timeout;
    private int retryCount;

    public LimitedRetriesRequestExecutor(int timeout, int retryCount) {
        this.timeout = timeout;
        this.retryCount = retryCount;
    }

    /**
     * Perform the REST operation and retry in case of exceptions.
     */
    @Override
    public <T extends JestResult> T execute(JestClient client, Action<T> clientRequest) throws IOException {
        // 'strict-timeout' will enforce that the timeout time elapses between subsequent
        // attempts even if the operation returns more quickly than the timeout
        final Map<String,Object> params = new HashMap<>();
        params.put("strict-timeout", Boolean.TRUE);

        final TimeoutTracker timeoutTracker = new TimeoutTracker(params, retryCount, timeout);
        for (timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
            timeoutTracker.startAttempt();
            try {
                T result = client.execute(clientRequest);
                return result;
            } catch (Exception exception) {
                // shouldRetry would return true because nextAttempt has not yet been invoked.
                // Therefore we manually verify instead of calling shouldRetry()
                if (timeoutTracker.getAttempt() + 1 <= retryCount) {
                    LOG.warn("Exception while trying to execute REST operation (attempt {}/{}). Retrying.", timeoutTracker.getAttempt() + 1, retryCount + 1, exception);
                } else {
                    // we are out of retries, forward exception
                    throw new IOException("Could not perform request. Tried " + (timeoutTracker.getAttempt() + 1) + " times and gave up", exception);
                }
            }
        }

        // In order to proper handle error cases, we must bail in this case, as m_delegate was never invoked.
        // In theory this should never happen.
        throw new IllegalStateException("The request never produced a valid result. This should not have happened. Bailing.");
    }
}
