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