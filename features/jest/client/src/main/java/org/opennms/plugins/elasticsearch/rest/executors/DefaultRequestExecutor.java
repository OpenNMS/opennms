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

import org.opennms.plugins.elasticsearch.rest.RequestExecutorFactory;
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

