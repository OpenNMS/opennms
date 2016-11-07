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