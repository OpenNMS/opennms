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

package org.opennms.netmgt.flows.victorialogs;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Dictionary;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * Whether the query service reaches the OSGi registry at all.
 *
 * <p>This is the whole safety property. {@code FlowQueryService} is bound through a singleton
 * reference, and Aries binds reluctantly — a reference re-binds when the service it holds departs,
 * not when a better one arrives. So a registration that exists is a registration that can be bound
 * by accident, whatever its ranking, the first time the Elasticsearch container reloads. The only
 * state that cannot be bound is not being registered.
 */
public class VictoriaLogsQueryServiceRegistrarTest {

    private BundleContext bundleContext;
    private FlowQueryService queryService;
    private VictoriaLogsClient client;
    @SuppressWarnings("unchecked")
    private final ServiceRegistration<FlowQueryService> registration = mock(ServiceRegistration.class);
    private VictoriaLogsQueryServiceRegistrar registrar;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        bundleContext = mock(BundleContext.class);
        queryService = mock(FlowQueryService.class);
        client = mock(VictoriaLogsClient.class);   // getConfigurationError() defaults to null
        when(bundleContext.registerService(eq(FlowQueryService.class), any(FlowQueryService.class),
                any(Dictionary.class))).thenReturn(registration);
        registrar = new VictoriaLogsQueryServiceRegistrar(bundleContext, queryService, client);
    }

    /**
     * The default. An install that has not opted in must not be able to answer a flow query, because
     * being registered at all is enough to be bound the next time Elasticsearch reloads.
     */
    @Test
    public void nothingIsRegisteredUnlessQueryingWasEnabled() {
        registrar.start();
        verifyNoInteractions(bundleContext);
    }

    @Test
    public void enablingRegistersAheadOfTheElasticsearchBackend() {
        registrar.setDisabled(false);
        registrar.start();

        @SuppressWarnings("unchecked") final ArgumentCaptor<Dictionary<String, Object>> properties =
                ArgumentCaptor.forClass(Dictionary.class);
        verify(bundleContext).registerService(eq(FlowQueryService.class), eq(queryService),
                properties.capture());
        // Elasticsearch registers with no ranking, i.e. zero, so anything positive is preferred by a
        // reference binding while both are present.
        assertEquals(1000, properties.getValue().get(Constants.SERVICE_RANKING));
    }

    /** A reload calls start() again on a fresh instance, but a stray second call must not double up. */
    @Test
    public void startingTwiceRegistersOnce() {
        registrar.setDisabled(false);
        registrar.start();
        registrar.start();

        verify(bundleContext, times(1)).registerService(eq(FlowQueryService.class),
                any(FlowQueryService.class), any(Dictionary.class));
    }

    @Test
    public void stoppingWithdrawsTheRegistration() {
        registrar.setDisabled(false);
        registrar.start();
        registrar.stop();

        verify(registration).unregister();
    }

    /** Blueprint calls the destroy method whether or not the init method registered anything. */
    @Test
    public void stoppingWithoutHavingRegisteredIsHarmless() {
        registrar.stop();
        verifyNoInteractions(registration);
    }

    /**
     * The framework unregisters everything a stopping bundle owns, so losing that race is normal.
     */
    @Test
    public void anAlreadyWithdrawnRegistrationIsNotAnError() {
        registrar.setDisabled(false);
        registrar.start();
        org.mockito.Mockito.doThrow(new IllegalStateException("already unregistered"))
                .when(registration).unregister();

        registrar.stop();   // must not propagate
    }

    /**
     * Enabled is not sufficient — the connection has to be usable.
     *
     * <p>This is the hole the two changes opened between them. Since a misconfiguration no longer
     * fails the container, registering on the flag alone publishes a backend that logged at startup
     * that it will not be used: the singleton reference binds it in preference to Elasticsearch and
     * every flow query in the UI throws. The same typo previously failed the container, registered
     * nothing, and left Elasticsearch serving.
     */
    @Test
    public void aMisconfiguredClientIsNotPublishedEvenWhenQueryingIsEnabled() {
        when(client.getConfigurationError()).thenReturn("the url must be absolute");
        registrar.setDisabled(false);

        registrar.start();

        verifyNoInteractions(bundleContext);
    }
}
