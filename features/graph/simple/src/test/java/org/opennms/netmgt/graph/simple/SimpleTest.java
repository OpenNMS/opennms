/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.simple;

import static org.opennms.netmgt.graph.simple.TestObjectCreator.createVertex;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {

    @Test
    public void verifyVertexCopyConstructor() {
        final SimpleVertex original = createVertex("dummy", "1");
        final SimpleVertex copy = new SimpleVertex(original);
        Assert.assertEquals(original.getId(), copy.getId());
        Assert.assertEquals(original.getLabel(), copy.getLabel());
        Assert.assertEquals(original.getNamespace(), copy.getNamespace());
        Assert.assertEquals(original, copy);
    }

    @Test
    public void verifyEdgeCopyConstructor() {
        final SimpleEdge original = new SimpleEdge("dummy", createVertex("dummy", "1"), createVertex("dummy", "2"));
        original.setLabel("label");
        final SimpleEdge copy = new SimpleEdge(original);
        Assert.assertEquals(original.getId(), copy.getId());
        Assert.assertEquals(original.getLabel(), copy.getLabel());
        Assert.assertEquals(original.getSource(), copy.getSource());
        Assert.assertEquals(original.getTarget(), copy.getTarget());
        Assert.assertEquals(original, copy);
        Assert.assertNotSame(original.getSource(), copy.getSource());
        Assert.assertNotSame(original.getTarget(), copy.getTarget());
    }


}
