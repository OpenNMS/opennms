package org.opennms.smoketest.topo;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.TopologyIT;
import org.opennms.smoketest.graphml.GraphmlDocument;
import org.opennms.smoketest.utils.RestClient;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NMS14379_IT extends OpenNMSSeleniumIT {

    public static final String LABEL = "GraphML Topology Provider (test-graph)";

    private final GraphmlDocument graphmlDocument = new GraphmlDocument("test-topology.xml", "/topology/graphml/test-topology.xml");

    private TopologyIT.TopologyUIPage topologyUIPage;

    private RestClient restClient;

    @Before
    public void setUp() throws IOException, InterruptedException {
        restClient = stack.opennms().getRestClient();

        if (existsGraph()) {
            deleteGraph();
        }
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        deleteGraph();
    }

    @Test
    public void testDeletion() throws Exception {
        importGraph();
        topologyUIPage = new TopologyIT.TopologyUIPage(this, getBaseUrlInternal());
        topologyUIPage.open();
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.defaultFocus();
        frontPage();
        deleteGraph();
        topologyUIPage.open();
        findElementByXpath("//div[text()='Clicking okay will switch to the default topology provider.']");
    }

    private boolean existsGraph() {
        return graphmlDocument.exists(restClient);
    }

    private void importGraph() throws InterruptedException {
        graphmlDocument.create(restClient);
        Thread.sleep(20000);
    }

    private void deleteGraph() throws InterruptedException {
        graphmlDocument.delete(restClient);
        Thread.sleep(20000);
    }
}