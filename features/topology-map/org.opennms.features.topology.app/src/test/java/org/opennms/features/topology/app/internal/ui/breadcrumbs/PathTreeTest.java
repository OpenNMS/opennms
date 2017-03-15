/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.support.breadcrumbs.Breadcrumb;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

public class PathTreeTest {

    @Test
    public void testGetLeafs() {
        PathTree pathTree = new PathTree();
        pathTree.addPath(Lists.newArrayList(vertex("a", "1"), vertex("b", "1")));
        pathTree.addPath(Lists.newArrayList(vertex("a", "1"), vertex("b", "2")));
        pathTree.addPath(Lists.newArrayList(vertex("a", "1"), vertex("b", "3"), vertex("c", "1")));
        pathTree.addPath(Lists.newArrayList(vertex("z", "1"), vertex("y", "1")));
        List<Node> leafs = pathTree.getLeafs();
        Assert.assertEquals(4, leafs.size());
        Assert.assertEquals(
                Lists.newArrayList(new DefaultVertexRef("b", "1"), new DefaultVertexRef("b", "2"), new DefaultVertexRef("c", "1"), new DefaultVertexRef("y", "1")),
                leafs.stream().map(l -> l.getVertexRef()).sorted().collect(Collectors.toList()));
    }

    @Test
    public void testCreateBreadcrumbs() {
        PathTree pathTree = new PathTree();
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Minneapolis")));
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Chicago")));
        Assert.assertEquals(1, pathTree.getNumberOfPaths());
        Assert.assertEquals("Regions > Central", toString(pathTree.toBreadcrumbs()));

        pathTree.clear();
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Chicago"), vertex("site", "1")));
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Chicago"), vertex("site", "2")));
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Chicago"), vertex("site", "3")));
        Assert.assertEquals(1, pathTree.getNumberOfPaths());
        Assert.assertEquals("Regions > Central > Chicago", toString(pathTree.toBreadcrumbs()));

        pathTree.clear();
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Minneapolis")));
        pathTree.addPath(Lists.newArrayList(vertex("regions", "East"), vertex("markets", "New York")));
        Assert.assertEquals(2, pathTree.getNumberOfPaths());
        Assert.assertEquals("Regions > Central, East", toString(pathTree.toBreadcrumbs()));

        pathTree.clear();
        pathTree.addPath(Lists.newArrayList(vertex("regions", "East"), vertex("markets", "New York"), vertex("sites", "'NY-Site1")));
        pathTree.addPath(Lists.newArrayList(vertex("regions", "Central"), vertex("markets", "Minneapolis"), vertex("sites", "MN-Site1")));
        Assert.assertEquals(2, pathTree.getNumberOfPaths());
        Assert.assertEquals("Regions > Central, East > Minneapolis, New York", toString(pathTree.toBreadcrumbs()));

    }

    private static VertexRef vertex(String ns, String id) {
        return new DefaultVertexRef(ns, id, id);
    }

    private static String toString(List<Breadcrumb> breadcrumbs) {
        return breadcrumbs.stream()
                .map(b -> {
                    if (b.getSourceVertices().isEmpty()) {
                        return b.getTargetNamespace().toUpperCase().charAt(0) + b.getTargetNamespace().substring(1);
                    }
                    return b.getSourceVertices().stream().map(v -> v.getLabel()).collect(Collectors.joining(", "));
                })
                .collect(Collectors.joining(" > "));
    }

}