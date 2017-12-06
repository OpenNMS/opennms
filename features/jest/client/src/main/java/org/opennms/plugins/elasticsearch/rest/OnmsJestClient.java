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
import java.util.Objects;
import java.util.Set;

import org.opennms.plugins.elasticsearch.rest.executors.RequestExecutor;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;

public class OnmsJestClient implements JestClient {

    private final JestClient m_delegate;

    private final RequestExecutor m_requestExecutor;

    public OnmsJestClient(JestClient delegate, RequestExecutor requestExecutor) {
        m_delegate = Objects.requireNonNull(delegate);
        m_requestExecutor = Objects.requireNonNull(requestExecutor);
    }

    @Override
    public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
        final T result = m_requestExecutor.execute(m_delegate, clientRequest);
        return result;
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
