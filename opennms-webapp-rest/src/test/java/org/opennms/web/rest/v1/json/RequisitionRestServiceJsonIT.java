/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestJsonTestCase;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class RequisitionRestServiceJsonIT extends AbstractSpringJerseyRestJsonTestCase {

    @Autowired
    MockEventIpcManager m_eventProxy;

    @Test
    public void testRequisition() throws Exception {
        cleanUpImports();

        createRequisition();
        String url = "/requisitions";
        String json = sendRequest(GET, url, 200);
        System.err.println("XXXXXXX>   " + json);
        JSONObject requisitions = new JSONObject(json);
        assertEquals(1, requisitions.getInt("count"));
        assertTrue(requisitions.has("model-import"));
        JSONObject req = requisitions.getJSONArray("model-import").getJSONObject(0);
        assertTrue(req.has("node"));
        JSONObject node = req.getJSONArray("node").getJSONObject(0);
        assertTrue(node.has("interface"));
        JSONArray interfaces = node.getJSONArray("interface");
        boolean mgmtFound = false;
        for (int i=0; i < interfaces.length(); i++) {
            JSONObject obj = interfaces.getJSONObject(i);
            assertTrue(obj.has("descr"));
            if ("Management interface".equals(obj.getString("descr"))) {
                mgmtFound = true;
            }
        }
        assertTrue(mgmtFound);

        url = "/requisitions/test";
        sendRequest(DELETE, url, 202);
        sendRequest(GET, url, 404);
    }

    @Test
    public void testDuplicateNodes() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        JSONObject req = new JSONObject();
        req.put("date-stamp", "2006-03-09T00:03:09");
        req.put("foreign-source", "test");
        JSONArray nodeArray = new JSONArray();
        JSONObject nodeA = new JSONObject();
        nodeA.put("node-label", "a");
        nodeA.put("foreign-id", "a");
        nodeArray.put(nodeA);
        JSONObject nodeB = new JSONObject();
        nodeB.put("node-label", "b");
        nodeB.put("foreign-id", "c");
        nodeArray.put(nodeB);
        JSONObject nodeC = new JSONObject();
        nodeC.put("node-label", "c");
        nodeC.put("foreign-id", "c");
        nodeArray.put(nodeC);
        req.put("node", nodeArray);
        final MockHttpServletResponse response = sendPost("/requisitions", req.toString(), 400, null);
        assertTrue("response should say 'c' has duplicates",  response.getContentAsString().contains("Duplicate nodes found on foreign source test: c (2 found)"));
    }

    @Test
    public void testNodes() throws Exception {
        createRequisition();

        String url = "/requisitions/test/nodes";

        // create a node
        JSONObject node = new JSONObject();
        node.put("node-label", "shoe");
        node.put("parent-node-label", "david");
        node.put("foreign-id", "1111");
        sendPost(url, node.toString(), 202, "/test/nodes/1111");

        // get list of nodes
        String json = sendRequest(GET, url, 200);
        JSONObject nodes = new JSONObject(json);
        assertNotNull(nodes.getJSONArray("node"));
        assertEquals(2, nodes.getInt("count"));
        assertEquals("david", nodes.getJSONArray("node").getJSONObject(0).getString("node-label"));
        assertEquals("shoe", nodes.getJSONArray("node").getJSONObject(1).getString("node-label"));

        // get individual node
        url = "/requisitions/test/nodes/4243";
        json = sendRequest(GET, url, 200);
        node = new JSONObject(json);
        assertEquals("apknd", node.getString("parent-node-label"));
        assertEquals("david", node.getString("node-label"));

        // set attributes
        sendPut(url, "node-label=homo+sapien", 202, "/nodes/4243");
        json = sendRequest(GET, url, 200);
        node = new JSONObject(json);
        assertEquals("homo sapien", node.getString("node-label"));

        // delete node
        json = sendRequest(DELETE, url, 202);
        json = sendRequest(GET, url, 404);
    }

    @Test
    public void testAddExistingNode() throws Exception {
        createRequisition();

        String url = "/requisitions/test/nodes";

        // attempt to add existing node
        JSONObject node = new JSONObject();
        node.put("node-label", "shoe");
        node.put("parent-node-label", "david");
        node.put("foreign-id", "4243");

        sendPost(url, node.toString(), 202, "/requisitions/test/nodes/4243");

        // get list of nodes
        String json = sendRequest(GET, url, 200);
        JSONObject nodes = new JSONObject(json);
        assertTrue(nodes.has("node"));
        assertEquals("Expected only 1 node", 1, nodes.getInt("count"));
        assertEquals("shoe", nodes.getJSONArray("node").getJSONObject(0).getString("node-label"));
    }

    @Test
    public void testNodeInterfaces() throws Exception {
        createRequisition();

        String base = "/requisitions/test/nodes/4243/interfaces";
        String json;

        // create an interface
        JSONObject intf = new JSONObject();
        intf.put("ip-addr", "192.0.2.254");
        intf.put("status", 1);
        intf.put("snmp-primary", "S");
        intf.put("descr", "Monkey");
        intf.put("monitored-service", new JSONArray());
        JSONObject icmp = new JSONObject();
        icmp.put("service-name", "ICMP");
        intf.append("monitored-service", icmp);
        sendPost(base, intf.toString(), 202, "/nodes/4243/interfaces/192.0.2.254");
        intf.put("descr", "Blah");
        sendPost(base, intf.toString(), 202, "/nodes/4243/interfaces/192.0.2.254");

        // get list of interfaces
        json = sendRequest(GET, base, 200);
        JSONObject interfaces = new JSONObject(json);
        assertEquals(3, interfaces.getInt("count"));
        assertTrue(interfaces.has("interface"));
        JSONArray intfArray = interfaces.getJSONArray("interface");
        boolean blahFound = false;
        boolean monkeyFound = false;
        for (int i=0; i < intfArray.length(); i++) {
            JSONObject obj = intfArray.getJSONObject(i);
            assertTrue(obj.has("descr"));
            if ("Monkey".equals(obj.getString("descr"))) {
                monkeyFound = true;
            }
            if ("Blah".equals(obj.getString("descr"))) {
                blahFound = true;
            }
        }
        assertTrue(blahFound);
        assertFalse(monkeyFound);

        // get individual interface
        String url = base + "/192.0.2.204";
        json = sendRequest(GET, url, 200);
        intf = new JSONObject(json);
        assertFalse("192.0.2.201".equals(intf.getString("ip-addr")));
        assertEquals("192.0.2.204", intf.getString("ip-addr"));
        assertEquals("VPN interface", intf.getString("descr"));

        // set attributes
        sendPut(url, "status=3&descr=Total+Crap&snmp-primary=P", 202, "/nodes/4243/interfaces/192.0.2.204");
        json = sendRequest(GET, url, 200);
        intf = new JSONObject(json);
        assertEquals("Total Crap", intf.getString("descr"));
        assertEquals("P", intf.getString("snmp-primary"));
        assertEquals(3, intf.getInt("status"));

        // delete interface
        json = sendRequest(DELETE, url, 202);
        json = sendRequest(GET, url, 404);

        // confirm there is one less interface
        json = sendRequest(GET, base, 200);
        interfaces = new JSONObject(json);
        assertEquals(2, interfaces.getInt("count"));
    }

    @Test
    public void testNodeInterfaceServices() throws Exception {
        createRequisition();

        String base = "/requisitions/test/nodes/4243/interfaces/192.0.2.204/services";

        // create a service
        JSONObject monkey = new JSONObject();
        monkey.put("service-name", "MONKEY");
        sendPost(base, monkey.toString(), 202, "/interfaces/192.0.2.204/services/MONKEY");

        // get list of services
        String json = sendRequest(GET, base, 200);
        JSONObject services = new JSONObject(json);
        assertEquals(3, services.getInt("count"));
        boolean hasIcmp = false;
        assertTrue(services.has("monitored-service"));
        JSONArray servicesArray = services.getJSONArray("monitored-service");
        for (int i=0; i < servicesArray.length(); i++) {
            JSONObject obj = servicesArray.getJSONObject(i);
            assertTrue(obj.has("service-name"));
            if ("ICMP".equals(obj.getString("service-name"))) {
                hasIcmp = true;
            }
        }
        assertTrue(hasIcmp);

        // get individual service
        String url = base + "/ICMP";
        json = sendRequest(GET, url, 200);
        JSONObject svc = new JSONObject(json);
        assertTrue(svc.has("service-name"));
        assertEquals("ICMP", svc.getString("service-name"));

        // delete interface
        json = sendRequest(DELETE, url, 202);
        json = sendRequest(GET, url, 404);

        // confirm there is one less interface
        json = sendRequest(GET, base, 200);
        services = new JSONObject(json);
        assertEquals(2, services.getInt("count"));
    }

    @Test
    public void testNodeCategories() throws Exception {
        createRequisition();

        String base = "/requisitions/test/nodes/4243/categories";

        // create a category
        JSONObject category = new JSONObject();
        category.put("name", "Dead Servers");
        sendPost(base, category.toString(), 202, "/nodes/4243/categories/Dead%20Servers");

        // get list of categories
        String url = base;
        String json = sendRequest(GET, url, 200);
        JSONObject categories = new JSONObject(json);
        assertEquals(4, categories.getInt("count"));
        boolean lowFound = false;
        boolean deadFound = false;
        assertTrue(categories.has("category"));
        JSONArray categoriesArray = categories.getJSONArray("category");
        for (int i=0; i < categoriesArray.length(); i++) {
            JSONObject obj = categoriesArray.getJSONObject(i);
            assertTrue(obj.has("name"));
            if ("low".equals(obj.getString("name"))) {
                lowFound = true;
            }
            if ("Dead Servers".equals(obj.getString("name"))) {
                deadFound = true;
            }
        }
        assertTrue(lowFound);
        assertTrue(deadFound);

        // get individual category
        url = "/requisitions/test/nodes/4243/categories/low";
        json = sendRequest(GET, url, 200);
        category = new JSONObject(json);
        assertEquals("low", category.getString("name"));

        // delete category
        json = sendRequest(DELETE, url, 202);
        json = sendRequest(GET, url, 404);

        // confirm there are less categories
        json = sendRequest(GET, "/requisitions/test/nodes/4243/categories", 200);
        categories = new JSONObject(json);
        assertEquals(3, categories.getInt("count"));

        // create a category on a node that is not in the requisition
        base = "/requisitions/test/nodes/4244/categories";
        category = new JSONObject();
        category.put("name", "New Category");

        sendPost(base, category.toString(), 202, "/nodes/4244/categories/New%20Category");
        json = sendRequest(GET, base + "/New%20Category", 404);
    }

    @Test
    public void testNodeAssets() throws Exception {
        createRequisition();

        String base = "/requisitions/test/nodes/4243/assets";

        // create an asset
        JSONObject asset = new JSONObject();
        asset.put("name", "manufacturer");
        asset.put("value", "Dead Servers, Inc.");
        sendPost(base, asset.toString(), 202, "/nodes/4243/assets/manufacturer");

        // get list of asset parameters
        String url = base;
        String json = sendRequest(GET, url, 200);
        JSONObject assets = new JSONObject(json);
        assertEquals(3, assets.getInt("count"));
        boolean manufFound = false;
        boolean osFound = false;
        assertTrue(assets.has("asset"));
        JSONArray assetArray = assets.getJSONArray("asset");
        for (int i=0; i < assetArray.length(); i++) {
            JSONObject obj = assetArray.getJSONObject(i);
            assertTrue(obj.has("name"));
            assertTrue(obj.has("value"));
            if ("manufacturer".equals(obj.getString("name")) && "Dead Servers, Inc.".equals(obj.getString("value"))) {
                manufFound = true;
            }
            if ("operatingSystem".equals(obj.getString("name")) && "Windows Pi".equals(obj.getString("value"))) {
                osFound = true;
            }
        }
        assertTrue(manufFound);
        assertTrue(osFound);

        // get individual asset parameter
        url = "/requisitions/test/nodes/4243/assets/operatingSystem";
        json = sendRequest(GET, url, 200);
        asset = new JSONObject(json);
        assertEquals("Windows Pi", asset.getString("value"));

        // delete asset parameter
        json = sendRequest(DELETE, url, 202);
        json = sendRequest(GET, url, 404);

        // confirm there are less assets
        json = sendRequest(GET, "/requisitions/test/nodes/4243/assets", 200);
        assets = new JSONObject(json);
        assertEquals(2, assets.getInt("count"));
    }

    @Test
    public void testBadRequisition() throws Exception {
        JSONObject reqJson = generateSampleRequisition();
        String req = reqJson.toString().replaceFirst("\"node\":", "\"node\":asdfjklasdfjioasdf");

        Exception ex = null;
        try {
            new JSONObject(req); // This should fail
        } catch (final JSONException jex) {
            ex = jex;
        }
        assertNotNull("we should have an exception", ex);

        sendPost("/requisitions", req.toString(), 500, null);
    }

    @Test
    public void testImport() throws Exception {
        createRequisition();

        EventAnticipator anticipator = m_eventProxy.getEventAnticipator();

        sendRequest(PUT, "/requisitions/test/import", 202);

        assertEquals(1, anticipator.getUnanticipatedEvents().size());
    }

    @Test
    public void testImportNoRescan() throws Exception {
        createRequisition();

        EventAnticipator anticipator = m_eventProxy.getEventAnticipator();

        sendRequest(PUT, "/requisitions/test/import", parseParamData("rescanExisting=false"), 202);

        assertEquals(1, anticipator.getUnanticipatedEvents().size());
        final Event event = anticipator.getUnanticipatedEvents().iterator().next();
        final List<Parm> parms = event.getParmCollection();
        assertEquals(2, parms.size());
        assertEquals("false", parms.get(1).getValue().getContent());
    }

    private void createRequisition() throws Exception {
        JSONObject req = generateSampleRequisition();
        sendPost("/requisitions", req.toString(), 202, "/requisitions/test");
    }

    private JSONObject generateSampleRequisition() throws JSONException {
        JSONObject req = new JSONObject();
        req.put("date-stamp", "2006-03-09T00:03:09");
        req.put("foreign-source", "test");
        req.put("node", new JSONArray());

        JSONObject icmp = new JSONObject();
        icmp.put("service-name", "ICMP");
        JSONObject snmp = new JSONObject();
        snmp.put("service-name", "SNMP");

        JSONObject nodeA = new JSONObject();
        nodeA.put("node-label", "david");
        nodeA.put("foreign-id", "4243");
        nodeA.put("parent-node-label", "apknd");
        nodeA.put("interface", new JSONArray());
        nodeA.put("category", new JSONArray());
        nodeA.put("asset", new JSONArray());

        JSONObject ipA1 = new JSONObject();
        ipA1.put("ip-addr", "192.0.2.204");
        ipA1.put("status", "1");
        ipA1.put("snmp-primary", "S");
        ipA1.put("descr", "VPN interface");
        ipA1.put("monitored-service", new JSONArray());
        ipA1.append("monitored-service", icmp);
        ipA1.append("monitored-service", snmp);
        nodeA.append("interface", ipA1);

        JSONObject ipA2 = new JSONObject();
        ipA2.put("ip-addr", "192.0.2.201");
        ipA2.put("status", "1");
        ipA2.put("snmp-primary", "P");
        ipA2.put("descr", "Management interface");
        ipA2.put("monitored-service", new JSONArray());
        ipA2.append("monitored-service", icmp);
        ipA2.append("monitored-service", snmp);
        nodeA.append("interface", ipA2);

        JSONObject catA1 = new JSONObject();
        catA1.put("name", "AC");
        nodeA.append("category", catA1);
        JSONObject catA2 = new JSONObject();
        catA2.put("name", "UK");
        nodeA.append("category", catA2);
        JSONObject catA3 = new JSONObject();
        catA3.put("name", "low");
        nodeA.append("category", catA3);

        JSONObject assetA1 = new JSONObject();
        assetA1.put("name", "manufacturer");
        assetA1.put("value", "Dell");
        nodeA.append("asset", assetA1);
        JSONObject assetA2 = new JSONObject();
        assetA2.put("name", "operatingSystem");
        assetA2.put("value", "Windows Pi");
        nodeA.append("asset", assetA2);
        JSONObject assetA3 = new JSONObject();
        assetA3.put("name", "description");
        assetA3.put("value", "Large and/or In Charge");
        nodeA.append("asset", assetA3);

        req.append("node", nodeA);
        return req;
    }
}
