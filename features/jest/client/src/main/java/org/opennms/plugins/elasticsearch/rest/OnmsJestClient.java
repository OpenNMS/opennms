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

package org.opennms.plugins.elasticsearch.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.TimeoutTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;

class OnmsJestClient implements JestClient {

    private static final Logger LOG = LoggerFactory.getLogger(OnmsJestClient.class);

    private final JestClient m_delegate;

    private final int m_retries;

    private final int m_timeout;

    public OnmsJestClient(JestClient delegate) {
        this(delegate, 0, 0);
    }

    public OnmsJestClient(JestClient delegate, int timeout, int retries) {
        m_delegate = delegate;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * Perform the REST operation and retry in case of exceptions.
     */
    @Override
    public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
        // if there are no retries or no timeout, there is no need to use the TimeoutTracker
        if (m_retries == 0 && m_timeout == 0) {
            return m_delegate.execute(clientRequest);
        }

        // 'strict-timeout' will enforce that the timeout time elapses between subsequent
        // attempts even if the operation returns more quickly than the timeout
        final Map<String,Object> params = new HashMap<>();
        params.put("strict-timeout", Boolean.TRUE);

        final TimeoutTracker timeoutTracker = new TimeoutTracker(params, m_retries, m_timeout);
        for (timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
            timeoutTracker.startAttempt();
            try {
                return m_delegate.execute(clientRequest);
            } catch (Exception e) {
                if (timeoutTracker.shouldRetry()) {
                    LOG.warn("Exception while trying to execute REST operation (attempt {}/{}). Retrying.", timeoutTracker.getAttempt() + 1, m_retries + 1, e);
                } else {
                    throw e; // we are out of retries, forward exception
                }
            }
        }
        // In order to proper handle error cases, we must bail in this case, as m_delegate was never invoked.
        // In theory this should never happen.
        throw new IllegalStateException("Execution did not provide a JestResult. This should not have happened.");
    }

    @Override
    public <T extends JestResult> void executeAsync(Action<T> clientRequest, JestResultHandler<? super T> jestResultHandler) {
        m_delegate.executeAsync(clientRequest, jestResultHandler);
    }

    /**
     * @deprecated Use {@link #close()} instead.
     */
    @Deprecated
    @Override
    public void shutdownClient() {
        m_delegate.shutdownClient();
    }

    @Override
    public void setServers(Set<String> servers) {
        m_delegate.setServers(servers);
    }

    @Override
    public void close() throws IOException {
        m_delegate.close();
    }
}
