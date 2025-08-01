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
package org.opennms.features.jest.client.executors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.features.jest.client.ConnectionPoolShutdownException;
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
                // In case the connection pool was shut down, bail. See NMS-10697 for more details
                if (exception instanceof IllegalStateException && exception.getMessage().equals("Connection pool shut down")) {
                    LOG.error("Connection pool shut down. Nothing we can do. Bailing");
                    throw new ConnectionPoolShutdownException(exception.getMessage(), exception);
                }

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
