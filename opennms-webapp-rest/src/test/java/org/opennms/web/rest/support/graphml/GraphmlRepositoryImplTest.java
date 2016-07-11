package org.opennms.web.rest.support.graphml;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Properties;

import javax.xml.bind.JAXB;

import org.graphdrawing.graphml.GraphmlType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;

public class GraphmlRepositoryImplTest {

    @Before
    public void setup() {
        System.setProperty("opennms.home", "target");
    }

    @Test
    public void testCreateReadDelete() throws Exception {
        final String NAME = "test-graph";

        // Create
        GraphmlRepositoryImpl graphmlRepository = new GraphmlRepositoryImpl();
        GraphmlType graphmlType = JAXB.unmarshal(getClass().getResource("/v1/test-graph.xml"), GraphmlType.class);
        graphmlRepository.save(NAME, "Label *yay*", graphmlType);

        // Verify that xml was generated
        Assert.assertEquals(true, graphmlRepository.exists(NAME));

        // Verify cfg
        Properties properties = new Properties();
        properties.load(new FileInputStream(GraphmlRepositoryImpl.buildCfgFilepath(NAME)));
        Assert.assertEquals("Label *yay*", properties.get(GraphmlRepositoryImpl.LABEL));
        Assert.assertEquals(GraphmlRepositoryImpl.buildGraphmlFilepath(NAME), properties.get(GraphmlRepositoryImpl.TOPOLOGY_LOCATION));

        // Read
        GraphmlType byName = graphmlRepository.findByName(NAME);

        // Verify Read
        ByteArrayOutputStream graphmlTypeOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byNameOutputStream = new ByteArrayOutputStream();
        JAXB.marshal(graphmlType, graphmlTypeOutputStream);
        JAXB.marshal(byName, byNameOutputStream);

        // The GraphML java classes are generated and do not
        // overwrite equals() and hashCode() methods.
        // We have to check for equality like this
        XmlTest.initXmlUnit();
        XmlTest.assertXmlEquals(
                new String(graphmlTypeOutputStream.toByteArray()),
                new String(byNameOutputStream.toByteArray())
        );

        // Delete
        graphmlRepository.delete(NAME);
        Assert.assertEquals(false, graphmlRepository.exists(NAME));
        Assert.assertEquals(false, graphmlRepository.exists(NAME));
    }
}