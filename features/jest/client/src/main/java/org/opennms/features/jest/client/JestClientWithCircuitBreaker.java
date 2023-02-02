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

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;

public class JestClientWithCircuitBreaker implements JestClient, ApplicationContextAware, InitializingBean {
    public static final String CIRCUIT_BREAKER_STATE_CHANGE_EVENT_UEI = "uei.opennms.org/circuitBreaker/stateChange";
    private static final Logger LOG = LoggerFactory.getLogger(JestClientWithCircuitBreaker.class);

    private final JestClient client;
    private final CircuitBreaker circuitBreaker;

    private EventForwarder eventForwarder = null;
    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            setEventForwarder(context.getBean(EventForwarder.class));
        } catch (BeansException e) {
            LOG.error("Cannot get an EventForwarder in JestClientWithCircuitBreaker", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public JestClientWithCircuitBreaker(JestClient client, CircuitBreaker circuitBreaker) {
        this.client = Objects.requireNonNull(client);
        this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
    }

    //there is a cyclic dependency JestClientWithCircuitBreaker -> EventForwarder -> EventToIndex -> JestClientWithCircuitBreaker
    //to brake it EventForwarder will be initialized later
    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        circuitBreaker.getEventPublisher()
                .onStateTransition(e -> {
                    final String from = e.getStateTransition().getFromState().toString();
                    final String to = e.getStateTransition().getToState().toString();
                    LOG.info("CircuitBreaker with name {} changed state from {} to {}", circuitBreaker.getName(), from, to);
                    // Send an event when the circuit breaker's state changes (only when eventForwarder has been initialized)
                    if (this.eventForwarder != null) {
                        final Event event = new EventBuilder(CIRCUIT_BREAKER_STATE_CHANGE_EVENT_UEI, JestClientWithCircuitBreaker.class.getCanonicalName())
                                .addParam("name", circuitBreaker.getName())
                                .addParam("fromState", from)
                                .addParam("toState", to)
                                .getEvent();
                        this.eventForwarder.sendNow(event);
                    }
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
    public <T extends JestResult> void executeAsync(final Action<T> clientRequest, final JestResultHandler<? super T> jestResultHandler) {
        if(!circuitBreaker.tryAcquirePermission()) {
            jestResultHandler.failed(new CallNotPermittedException(circuitBreaker));
            return;
        }
        long start = System.nanoTime();
        this.client.executeAsync(clientRequest, new JestResultHandler<>() {
            @Override
            public void completed(T result) {
                long durationInNanos = System.nanoTime() - start;
                circuitBreaker.onSuccess(durationInNanos);
                jestResultHandler.completed(result);
            }

            @Override
            public void failed(Exception ex) {
                long durationInNanos = System.nanoTime() - start;
                circuitBreaker.onError(durationInNanos, ex);
                jestResultHandler.failed(ex);
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
