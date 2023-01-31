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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;

public class JestClientWithCircuitBreaker implements JestClient {
    public static final String CIRCUIT_BREAKER_STATE_CHANGE_EVENT_UEI = "uei.opennms.org/circuitBreaker/stateChange";
    private static final Logger LOG = LoggerFactory.getLogger(JestClientWithCircuitBreaker.class);

    private final JestClient client;
    private final CircuitBreaker circuitBreaker;

    private final EventForwarder eventForwarder;

    public JestClientWithCircuitBreaker(JestClient client, CircuitBreaker circuitBreaker, EventForwarder eventForwarder) {
        this.client = Objects.requireNonNull(client);
        this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    public void init() {
        circuitBreaker.getEventPublisher()
                .onStateTransition(e -> {
                    final String from = e.getStateTransition().getFromState().toString();
                    final String to = e.getStateTransition().getToState().toString();
                    LOG.info("CircuitBreaker with name {} changed state from {} to {}", circuitBreaker.getName(), from, to);
                    // Send an event when the circuit breaker's state changes
                    final Event event = new EventBuilder(CIRCUIT_BREAKER_STATE_CHANGE_EVENT_UEI, JestClientWithCircuitBreaker.class.getCanonicalName())
                            .addParam("name", circuitBreaker.getName())
                            .addParam("fromState", from)
                            .addParam("toState", to)
                            .getEvent();
                    this.eventForwarder.sendNow(event);
                });
    }

    @Override
    public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
        try {
            return circuitBreaker.decorateCheckedSupplier(() -> client.execute(clientRequest)).apply();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T extends JestResult> void executeAsync(Action<T> clientRequest, JestResultHandler<? super T> jestResultHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                jestResultHandler.completed(execute(clientRequest));
            } catch (IOException e) {
                jestResultHandler.failed(e);
            }
        });
    }

    @Override
    @Deprecated
    public void shutdownClient() {
        circuitBreaker.getEventPublisher().onStateTransition(event -> {});
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
}
