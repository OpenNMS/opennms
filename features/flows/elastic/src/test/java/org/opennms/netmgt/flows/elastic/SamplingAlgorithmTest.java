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
package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.opennms.integration.api.v1.flows.Flow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
public class SamplingAlgorithmTest {

    @Test
    public void canMapAllValues() {
        for (Flow.SamplingAlgorithm samplingAlgorithm : Flow.SamplingAlgorithm.values()) {
            assertThat(SamplingAlgorithm.from(samplingAlgorithm), notNullValue());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    @PrepareForTest(Flow.SamplingAlgorithm.class)
    public void willThrowExceptionOnIllegalValue() {
        Flow.SamplingAlgorithm illegalValue = PowerMockito.mock(Flow.SamplingAlgorithm.class);

        Whitebox.setInternalState(illegalValue, "name", "illegalValue");
        Whitebox.setInternalState(illegalValue, "ordinal", 8);

        PowerMockito.mockStatic(Flow.SamplingAlgorithm.class);
        PowerMockito.when(Flow.SamplingAlgorithm.values()).thenReturn(new Flow.SamplingAlgorithm[]{
                Flow.SamplingAlgorithm.Unassigned,
                Flow.SamplingAlgorithm.SystematicCountBasedSampling,
                Flow.SamplingAlgorithm.SystematicTimeBasedSampling,
                Flow.SamplingAlgorithm.RandomNOutOfNSampling,
                Flow.SamplingAlgorithm.UniformProbabilisticSampling,
                Flow.SamplingAlgorithm.PropertyMatchFiltering,
                Flow.SamplingAlgorithm.HashBasedFiltering,
                Flow.SamplingAlgorithm.FlowStateDependentIntermediateFlowSelectionProcess,
                illegalValue});

        SamplingAlgorithm.from(illegalValue);
    }
}
