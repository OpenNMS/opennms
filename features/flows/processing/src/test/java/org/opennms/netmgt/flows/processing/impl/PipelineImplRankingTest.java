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
package org.opennms.netmgt.flows.processing.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;

import com.codahale.metrics.MetricRegistry;

/**
 * Verifies the pipeline's flow-repository routing: only the highest service-ranked tier of repositories
 * receives flows (so the higher-ranked opennms-flows-postgres feature overrides the default Elasticsearch
 * repository entirely, and uninstalling it falls back), and a failing repository does not starve others.
 */
public class PipelineImplRankingTest {

    private static final String RANKING = "service.ranking";
    private static final String FORWARDER = "flows.repository.forwarder";

    private PipelineImpl pipeline;

    @Before
    public void setUp() {
        final DocumentEnricherImpl enricher = mock(DocumentEnricherImpl.class);
        final InterfaceMarkerImpl marker = mock(InterfaceMarkerImpl.class);
        final FlowThresholdingImpl thresholding = mock(FlowThresholdingImpl.class);
        when(enricher.enrich(any(), any())).thenReturn(Collections.emptyList());
        pipeline = new PipelineImpl(new MetricRegistry(), enricher, marker, thresholding);
    }

    private void process() throws Exception {
        pipeline.process(List.of(mock(Flow.class)), mock(FlowSource.class), null);
    }

    @Test
    public void higherRankedRepositoryOverridesLowerRankedOnesEntirely() throws Exception {
        final FlowRepository elastic = mock(FlowRepository.class);
        final FlowRepository postgres = mock(FlowRepository.class);
        pipeline.onBind(elastic, Map.of(PipelineImpl.REPOSITORY_ID, "elastic"));                 // ranking 0
        pipeline.onBind(postgres, Map.of(PipelineImpl.REPOSITORY_ID, "postgres", RANKING, 100));  // ranking 100

        process();

        verify(postgres, times(1)).persist(any());
        verify(elastic, never()).persist(any());
    }

    @Test
    public void fallsBackToLowerRankedRepositoryWhenTopRankedUnbinds() throws Exception {
        final FlowRepository elastic = mock(FlowRepository.class);
        final FlowRepository postgres = mock(FlowRepository.class);
        pipeline.onBind(elastic, Map.of(PipelineImpl.REPOSITORY_ID, "elastic"));
        pipeline.onBind(postgres, Map.of(PipelineImpl.REPOSITORY_ID, "postgres", RANKING, 100));

        pipeline.onUnbind(postgres, Map.of(PipelineImpl.REPOSITORY_ID, "postgres", RANKING, 100));
        process();

        verify(elastic, times(1)).persist(any());
    }

    @Test
    public void allRepositoriesInTheTopTierAreWritten() throws Exception {
        final FlowRepository a = mock(FlowRepository.class);
        final FlowRepository b = mock(FlowRepository.class);
        pipeline.onBind(a, Map.of(PipelineImpl.REPOSITORY_ID, "a"));   // both default ranking 0
        pipeline.onBind(b, Map.of(PipelineImpl.REPOSITORY_ID, "b"));

        process();

        verify(a, times(1)).persist(any());
        verify(b, times(1)).persist(any());
    }

    @Test
    public void forwarderCoexistsWithTheTopRankedStore() throws Exception {
        final FlowRepository elastic = mock(FlowRepository.class);
        final FlowRepository postgres = mock(FlowRepository.class);
        final FlowRepository kafka = mock(FlowRepository.class);
        pipeline.onBind(elastic, Map.of(PipelineImpl.REPOSITORY_ID, "elastic"));                  // store, ranking 0
        pipeline.onBind(postgres, Map.of(PipelineImpl.REPOSITORY_ID, "postgres", RANKING, 100));   // store, ranking 100
        pipeline.onBind(kafka, Map.of(PipelineImpl.REPOSITORY_ID, "kafka", FORWARDER, true));       // forwarder

        process();

        verify(postgres, times(1)).persist(any()); // top-ranked store wins
        verify(kafka, times(1)).persist(any());     // forwarder always runs alongside
        verify(elastic, never()).persist(any());    // overridden store
    }

    @Test
    public void forwarderReceivesFlowsEvenWithNoStoreSelected() throws Exception {
        final FlowRepository kafka = mock(FlowRepository.class);
        pipeline.onBind(kafka, Map.of(PipelineImpl.REPOSITORY_ID, "kafka", FORWARDER, true));

        process();

        verify(kafka, times(1)).persist(any());
    }

    @Test
    public void aFailingRepositoryDoesNotStarveOthersInTheSameTier() throws Exception {
        final FlowRepository bad = mock(FlowRepository.class);
        final FlowRepository good = mock(FlowRepository.class);
        doThrow(new FlowException("boom")).when(bad).persist(any());
        pipeline.onBind(bad, Map.of(PipelineImpl.REPOSITORY_ID, "bad"));
        pipeline.onBind(good, Map.of(PipelineImpl.REPOSITORY_ID, "good"));

        process(); // must not propagate the failure

        verify(good, times(1)).persist(any());
    }
}
