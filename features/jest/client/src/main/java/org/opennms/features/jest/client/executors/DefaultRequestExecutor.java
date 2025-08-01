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

import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.RequestExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.exception.CouldNotConnectException;

public class DefaultRequestExecutor implements RequestExecutor, RequestExecutorFactory {

    private static Logger LOG = LoggerFactory.getLogger(DefaultRequestExecutor.class);

    private final long cooldownInMs;

    public DefaultRequestExecutor(long cooldownInMs) {
        if (cooldownInMs < 0) {
            LOG.warn("Retry cooldown must be >= 0. Using a value of 0 instead.");
        }
        this.cooldownInMs = Math.max(0, cooldownInMs);
    }

    @Override
    public <T extends JestResult> T execute(JestClient client, Action<T> clientRequest) {
        do {
            LOG.debug("Executing request {}", clientRequest);
            try {
                T result = client.execute(clientRequest);
                return result;
            } catch (CouldNotConnectException connectException) {
                LOG.error("Could not connect to elastic endpoint: {}", connectException.getHost(), connectException);
            } catch (IOException ex) {
                LOG.error("Could not perform request {}: {}", clientRequest, ex.getMessage(), ex);
            } catch (com.google.gson.JsonSyntaxException gsonException) {
                LOG.error("A Json error occurred: {}", gsonException.getMessage(), gsonException);
            } catch (IllegalStateException ex) {
                // In case the connection pool was shut down, bail. See NMS-10697 for more details
                if (ex.getMessage().equals("Connection pool shut down")) {
                    LOG.error("Connection pool shut down. Nothing we can do. Bailing");
                    throw new ConnectionPoolShutdownException(ex.getMessage(), ex);
                } else {
                    LOG.error("IllegalStateException occurred: {}", ex.getMessage(), ex);
                }
            }

            // Retry-Logic
            LOG.debug("Request was not executed properly. Attempting Retry...");
            if (cooldownInMs > 0) {
                LOG.debug("Sleep " + cooldownInMs + " before retrying");
                try {
                    Thread.sleep(cooldownInMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread interrupted.", e);
                }
            }
            LOG.debug("Retrying now");
        } while (true);
    }

    @Override
    public RequestExecutor createExecutor(int timeout, int retryCount) {
        return new DefaultRequestExecutor(cooldownInMs);
    }
}

