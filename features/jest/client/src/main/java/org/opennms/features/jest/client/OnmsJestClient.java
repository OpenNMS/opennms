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
package org.opennms.features.jest.client;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.jest.client.executors.RequestExecutor;

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
