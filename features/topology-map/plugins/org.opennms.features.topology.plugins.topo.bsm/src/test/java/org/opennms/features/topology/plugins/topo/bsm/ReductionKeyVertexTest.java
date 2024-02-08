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
package org.opennms.features.topology.plugins.topo.bsm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReductionKeyVertexTest {

    @Test
    public void canGetLabelFromReductionKey() {
        assertEquals("nodeDown:10", ReductionKeyVertex.getLabelFromReductionKey("uei.opennms.org/nodes/nodeDown::10"));
        assertEquals("nodeLostService:DNS", ReductionKeyVertex.getLabelFromReductionKey("uei.opennms.org/nodes/nodeLostService::9:2600:5800:f2a2:0000:02d0:b7ff:fe25:3e1c:DNS"));
        assertEquals("dataCollectionFailed:48", ReductionKeyVertex.getLabelFromReductionKey("uei.opennms.org/nodes/dataCollectionFailed::48"));
        assertEquals("this_is_a_really_long_re...", ReductionKeyVertex.getLabelFromReductionKey("this_is_a_really_long_reduction_key_that_shouldnt_match_the_know_pattern"));
        assertEquals("interfaceDown:162.243.42...", ReductionKeyVertex.getLabelFromReductionKey("uei.opennms.org/nodes/interfaceDown::2:162.243.42.216"));
    }
}
