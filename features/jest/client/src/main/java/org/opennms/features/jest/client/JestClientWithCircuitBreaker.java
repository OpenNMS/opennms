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
