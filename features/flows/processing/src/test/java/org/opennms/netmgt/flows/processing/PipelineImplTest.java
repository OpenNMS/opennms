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
package org.opennms.netmgt.flows.processing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.flows.processing.impl.FlowThresholdingImpl;
import org.opennms.netmgt.flows.processing.impl.InterfaceMarkerImpl;
import org.opennms.netmgt.flows.processing.impl.PipelineImpl;

import com.codahale.metrics.MetricRegistry;

public class PipelineImplTest {

    private PipelineImpl pipeline;

    private final List<Flow> flows = List.of(mock(Flow.class));
    private final FlowSource source = new FlowSource("Default", "127.0.0.1", null);
    private final ProcessingOptions options = ProcessingOptions.builder().build();

    @Before
    public void setUp() throws Exception {
        final var documentEnricher = mock(DocumentEnricherImpl.class);
        when(documentEnricher.enrich(anyList(), any(FlowSource.class)))
                .thenReturn(List.of(new EnrichedFlow()));

        this.pipeline = new PipelineImpl(new MetricRegistry(),
                                         documentEnricher,
                                         mock(InterfaceMarkerImpl.class),
                                         mock(FlowThresholdingImpl.class));
    }

    private FlowRepository bindRepository(final String id) {
        final FlowRepository repository = mock(FlowRepository.class);
        this.pipeline.onBind(repository, Map.of(PipelineImpl.REPOSITORY_ID, id));
        return repository;
    }

    @Test
    public void failingPersisterDoesNotStarveTheOthers() throws Exception {
        final FlowRepository failing = bindRepository("failing");
        final FlowRepository working = bindRepository("working");

        final FlowException boom = new FlowException("boom");
        doThrow(boom).when(failing).persist(anyList());

        final FlowException thrown = assertThrows(FlowException.class,
                () -> this.pipeline.process(this.flows, this.source, this.options));

        // Both repositories saw the flows, regardless of persister iteration order
        verify(failing).persist(anyList());
        verify(working).persist(anyList());

        // The original failure is what reaches the caller
        assertThat(thrown, sameInstance(boom));
    }

    @Test
    public void allPersisterFailuresAreReported() throws Exception {
        final FlowRepository first = bindRepository("first");
        final FlowRepository second = bindRepository("second");

        doThrow(new FlowException("first down")).when(first).persist(anyList());
        doThrow(new RuntimeException("second down")).when(second).persist(anyList());

        final FlowException thrown = assertThrows(FlowException.class,
                () -> this.pipeline.process(this.flows, this.source, this.options));

        // One failure is rethrown, the other rides along as suppressed
        assertThat(thrown.getSuppressed(), arrayWithSize(1));
    }

    @Test
    public void healthyPersistersDoNotThrow() throws Exception {
        final FlowRepository working = bindRepository("working");

        try {
            this.pipeline.process(this.flows, this.source, this.options);
        } catch (final FlowException e) {
            fail("process() must not throw when all persisters succeed: " + e);
        }

        verify(working).persist(anyList());
    }
}
