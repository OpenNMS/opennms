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

package org.opennms.features.graphml.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

public class GraphMLWriterTest {

    @Test
    public void verifyWrite() throws InvalidGraphException, FileNotFoundException {
        GraphML graphML = new GraphML();
        GraphMLGraph graph = new GraphMLGraph();

        GraphMLNode node1 = new GraphMLNode();
        node1.setId("node1");

        GraphMLNode node2 = new GraphMLNode();
        node2.setId("node2");

        GraphMLEdge edge1 = new GraphMLEdge();
        edge1.setId("edge1");
        edge1.setSource(node1);
        edge1.setTarget(node2);

        graphML.addGraph(graph);
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addEdge(edge1);

        GraphMLWriter.write(graphML, new File("target/output.graphml"));
        GraphML read = GraphMLReader.read(new FileInputStream("target/output.graphml"));
        Assert.assertEquals(read, graphML);
    }

}