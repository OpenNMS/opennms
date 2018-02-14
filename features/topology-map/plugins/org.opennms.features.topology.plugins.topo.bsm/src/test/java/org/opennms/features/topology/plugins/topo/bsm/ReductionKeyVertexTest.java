/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
