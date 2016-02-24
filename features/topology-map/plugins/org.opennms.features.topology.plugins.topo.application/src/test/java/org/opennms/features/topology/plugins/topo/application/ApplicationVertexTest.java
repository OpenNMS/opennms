/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.application;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.topo.VertexRef;

public class ApplicationVertexTest {

    @Test
    public void testHierarchy() {
        ApplicationVertex vertex = createVertex("100");
        vertex.addChildren(createVertex("service:1"));
        vertex.addChildren(createVertex("service:2"));

        Assert.assertEquals(true, vertex.isRoot());
        Assert.assertEquals(false, vertex.isLeaf());
        Assert.assertEquals(true, vertex.isPartOf("100"));
        Assert.assertEquals(false, vertex.isPartOf("101"));

        // Verify first level
        for (VertexRef eachRef : vertex.getChildren()) {
            Assert.assertEquals(ApplicationTopologyProvider.TOPOLOGY_NAMESPACE, eachRef.getNamespace());
            ApplicationVertex child = (ApplicationVertex) eachRef;

            Assert.assertEquals(false, child.isRoot());
            Assert.assertEquals(true, child.isLeaf());
            Assert.assertEquals(vertex, child.getRoot());
            Assert.assertEquals(vertex, child.getParent());
            Assert.assertEquals(true, child.isPartOf("100"));
            Assert.assertEquals(false, child.isPartOf("101"));
        }
    }

    private static ApplicationVertex createVertex(String id) {
        return new ApplicationVertex(id, id);
    }
}
