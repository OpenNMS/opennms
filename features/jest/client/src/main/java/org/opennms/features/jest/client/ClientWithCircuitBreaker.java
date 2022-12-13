/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.jest.client;

import java.io.IOException;
import java.util.Set;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;

public class ClientWithCircuitBreaker implements JestClient {

    private final JestClient client;
    private final CircuitBreaker circuitBreaker;

    public ClientWithCircuitBreaker(JestClient client, CircuitBreaker circuitBreaker) {
        this.client = client;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
        try {
            return circuitBreaker.decorateCheckedSupplier(() -> client.execute(clientRequest)).apply();
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends JestResult> void executeAsync(Action<T> clientRequest, JestResultHandler<? super T> jestResultHandler) {
        try {
            circuitBreaker.decorateCheckedRunnable(
                        () -> client.executeAsync(clientRequest, jestResultHandler)
                ).run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public void shutdownClient() {
        client.shutdownClient();
    }

    @Override
    public void setServers(Set<String> servers) {
        client.setServers(servers);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    public boolean canRetry() {
        return circuitBreaker.getState().allowPublish;
    }
}
