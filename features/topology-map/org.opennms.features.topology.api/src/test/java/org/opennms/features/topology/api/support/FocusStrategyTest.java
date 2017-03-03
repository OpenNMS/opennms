/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.GraphProvider;

import com.google.common.collect.Lists;

public class FocusStrategyTest {

    @Test
    public void testFocusStrategies() {
        GraphProvider provider = new SimpleGraphBuilder("namespace1")
                .vertex("1")
                .vertex("2")
                .vertex("3")
                .get();
        Assert.assertEquals(Lists.newArrayList(), FocusStrategy.EMPTY.getFocusCriteria(provider));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "1"),
                hopCriteria("namespace1", "2"),
                hopCriteria("namespace1", "3")), FocusStrategy.ALL.getFocusCriteria(provider));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "1")), FocusStrategy.FIRST.getFocusCriteria(provider));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "2")), FocusStrategy.SPECIFIC.getFocusCriteria(provider, "2"));
    }

    private VertexHopGraphProvider.DefaultVertexHopCriteria hopCriteria(String namespace, String id) {
        return new VertexHopGraphProvider.DefaultVertexHopCriteria(new AbstractVertex(namespace, id));
    }

}