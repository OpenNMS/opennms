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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class NodeRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(NodeRestServiceIT.class);

    public NodeRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }
    @Autowired
    private DatabasePopulator m_databasePopulator;
    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    @JUnitTemporaryDatabase
    public void testFiqlSearch() throws Exception {
        // Add 5 nodes
        for (int i = 0; i < 5; i++) {
            createNode(201);
        }

        String url = "/nodes";

        LOG.warn(sendRequest(GET, url, parseParamData("limit=2&offset=2&_s=node.label==*Test*"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=node.label==*1"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=node.label==*2"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=assetRecord.id==2"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=node.label==*2;assetRecord.id==2"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=(node.label==*2;assetRecord.id==2),(node.label==*1)"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=ipInterface.ipAddress==10.10.10.10"), 204));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=ipInterface.snmpPrimary==P"), 204));
        LOG.warn(sendRequest(GET, url + "/1/ipinterfaces", Collections.emptyMap(), 204));
        LOG.warn(sendRequest(GET, url + "/1/ipinterfaces", parseParamData("_s=snmpPrimary==P"), 204));

        // Use "Hello, Handsome" as a value to test CXF 'search.decode.values' property which will
        // URL-decode FIQL search values
        LOG.warn(sendRequest(GET, url, parseParamData("_s=node.label==Hello%252C+Handsome"), 204));

        // Put all of the FIQL reserved characters into a string which should equal:
        // !$'()+,;=
        LOG.warn(sendRequest(GET, url, parseParamData("_s=node.label==%2521%2524%2527%2528%2529%252B%252C%253B%253D"), 204));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAllEndPoints() throws Exception {
        String node = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"JUnit\" foreignId=\"TestMachine1\">" +
                "<location>Default</location>" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/nodes", node, 201);
        LOG.warn(sendRequest(GET, "/nodes", 200));
        LOG.warn(sendRequest(GET, "/nodes/1", 200)); // By ID
        LOG.warn(sendRequest(GET, "/nodes/JUnit:TestMachine1", 200)); // By foreignSource/foreignId combination

        String ipInterface = "<ipInterface snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>TestMachine</hostName>" +
                "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface, 201);
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces", 200));
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10", 200)); // By IP Address

        String service = "<service status=\"A\">" +
                "<serviceType>" +
                "<name>ICMP</name>" +
                "</serviceType>" +
                "</service>";
        sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", service, 201);
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10/services", 200));
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP", 200)); // By Name

        String snmpInterface = "<snmpInterface ifIndex=\"6\">" +
                "<ifAdminStatus>1</ifAdminStatus>" +
                "<ifDescr>en1</ifDescr>" +
                "<ifName>en1</ifName>" +
                "<ifOperStatus>1</ifOperStatus>" +
                "<ifSpeed>10000000</ifSpeed>" +
                "<ifType>6</ifType>" +
                "<physAddr>001e5271136d</physAddr>" +
                "</snmpInterface>";
        sendPost("/nodes/1/snmpinterfaces", snmpInterface, 201);
        LOG.warn(sendRequest(GET, "/nodes/1/snmpinterfaces", 200));
        LOG.warn(sendRequest(GET, "/nodes/1/snmpinterfaces/6", 200)); // By ifIndex

        LOG.warn(sendRequest(GET, "/nodes/1/hardwareInventory", 404));
        byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/hardware-inventory.xml"));
        String entity = new String(encoded, StandardCharsets.UTF_8);
        sendPost("/nodes/1/hardwareInventory", entity, 204, null);
        String xml = sendRequest(GET, "/nodes/1/hardwareInventory", 200);
        assertTrue(xml, xml.contains("Cisco 7206VXR, 6-slot chassis"));

        String category = "<category name=\"Production\"/>";
        sendPost("/nodes/1/categories", category, 201);
        LOG.warn(sendRequest(GET, "/nodes/1/categories", 200));

        // UPDATE

        LOG.warn(sendRequest(PUT, "/nodes/1", parseParamData("sysLocation=USA"), 204));
        LOG.warn(sendRequest(PUT, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP", parseParamData("status=F"), 204));

        // DELETE

        LOG.warn(sendRequest(DELETE, "/nodes/1/snmpinterfaces/6", 204));
        LOG.warn(sendRequest(GET, "/nodes/1/snmpinterfaces/6", 404));
        LOG.warn(sendRequest(DELETE, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP", 204));
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP", 404));
        LOG.warn(sendRequest(DELETE, "/nodes/1/ipinterfaces/10.10.10.10", 204));
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10", 404));
        LOG.warn(sendRequest(DELETE, "/nodes/1", 204));
        LOG.warn(sendRequest(GET, "/nodes/1", 404));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAllEndPointsWithJSON() throws Exception {
        JSONObject node = new JSONObject();
        node.put("type", "A");
        node.put("label", "TestMachine1");
        node.put("foreignSource", "JUnit");
        node.put("foreignId", "TestMachine1");
        node.put("location", "Default");
        node.put("labelSource", "H");
        node.put("sysContact", "The Owner");
        node.put("sysDescription", "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386");
        node.put("sysLocation", "Earth");
        node.put("sysName", "TestMachine1");
        node.put("sysObjectId", ".1.3.6.1.4.1.8072.3.2.255");

        sendData(POST, MediaType.APPLICATION_JSON, "/nodes", node.toString(), 201);
        LOG.warn(sendRequest(GET, "/nodes", 200));
        LOG.warn(sendRequest(GET, "/nodes/1", 200)); // By ID
        LOG.warn(sendRequest(GET, "/nodes/JUnit:TestMachine1", 200)); // By foreignSource/foreignId combination

        JSONObject ipInterface = new JSONObject();
        ipInterface.put("snmpPrimary", "P");
        ipInterface.put("ipAddress", "10.10.10.10");
        ipInterface.put("hostName", "TestMachine");

        sendData(POST, MediaType.APPLICATION_JSON, "/nodes/1/ipinterfaces", ipInterface.toString(), 201);
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces", 200));
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10", 200)); // By IP Address

        JSONObject serviceType = new JSONObject();
        serviceType.put("name", "ICMP");
        JSONObject service = new JSONObject();
        service.put("status", "A");
        service.put("serviceType", serviceType);
        sendData(POST, MediaType.APPLICATION_JSON, "/nodes/1/ipinterfaces/10.10.10.10/services", service.toString(), 201);
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10/services", 200));
        LOG.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP", 200)); // By Name

        JSONObject snmpInterface = new JSONObject();
        snmpInterface.put("ifIndex", 6);
        snmpInterface.put("ifAdminStatus", 1);
        snmpInterface.put("ifOperStatus", 1);
        snmpInterface.put("ifDescr", "en1");
        snmpInterface.put("ifName", "en1");
        snmpInterface.put("ifSpeed", 10000000);
        snmpInterface.put("ifType", 6);
        snmpInterface.put("physAddr", "001e5271136d");

        sendData(POST, MediaType.APPLICATION_JSON, "/nodes/1/snmpinterfaces", snmpInterface.toString(), 201);
        LOG.warn(sendRequest(GET, "/nodes/1/snmpinterfaces", 200));
        LOG.warn(sendRequest(GET, "/nodes/1/snmpinterfaces/6", 200)); // By ifIndex

        JSONObject category = new JSONObject();
        category.put("name", "Production");
        sendData(POST, MediaType.APPLICATION_JSON, "/nodes/1/categories", category.toString(), 201);
        LOG.warn(sendRequest(GET, "/nodes/1/categories", 200));
    }
    @Test
    @JUnitTemporaryDatabase
    public void testSnmpInterfaceStringSearchIsCaseInsensitive() throws Exception {
        // DatabasePopulator seeds node1 with physAddr="34E45604BB69" and ifAlias="Initial ifAlias value".
        // Both must be findable via lowercase wildcard FIQL (ilike, not like).
        m_databasePopulator.populateDatabase();

        // MAC stored uppercase, search lowercase — without ilike this returns 204 (no match)
        sendRequest(GET, "/nodes", parseParamData("_s=snmpInterface.physAddr==*34e456*"), 200);

        // Exact-case MAC search still works
        sendRequest(GET, "/nodes", parseParamData("_s=snmpInterface.physAddr==*34E456*"), 200);

        // ifAlias stored mixed-case, search all-lowercase
        sendRequest(GET, "/nodes", parseParamData("_s=snmpInterface.ifAlias==*ifalias*"), 200);

        // Negative: unknown MAC returns no results
        sendRequest(GET, "/nodes", parseParamData("_s=snmpInterface.physAddr==*000000000000*"), 204);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testIplikeSearch() throws Exception {
        // Create a node with a known IP address via the REST API
        String node = "<node type=\"A\" label=\"IplikeTestNode\" foreignSource=\"JUnit\" foreignId=\"IplikeTestNode\">" +
                "<location>Default</location>" +
                "<labelSource>H</labelSource>" +
                "</node>";
        sendPost("/nodes", node, 201);

        String ipInterface = "<ipInterface snmpPrimary=\"P\">" +
                "<ipAddress>192.168.1.100</ipAddress>" +
                "<hostName>iplike-host</hostName>" +
                "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface, 201);

        String url = "/nodes";

        // Exact IP match via iplike
        sendRequest(GET, url, parseParamData("_s=iplike==192.168.1.100"), 200);

        // Wildcard subnet match (* in URL becomes % by FIQL; the behavior reverses it)
        sendRequest(GET, url, parseParamData("_s=iplike==192.168.1.*"), 200);

        // Wrong subnet — no match, expect 204 No Content
        sendRequest(GET, url, parseParamData("_s=iplike==10.0.0.*"), 204);

        // NOT_EQUALS: the node is NOT in the 10.x range, so it appears in results
        sendRequest(GET, url, parseParamData("_s=iplike!=10.0.0.*"), 200);

        // --- IPv6 iplike coverage ---
        // Add a second node with an IPv6 address
        String node2 = "<node type=\"A\" label=\"IplikeTestNodeV6\" foreignSource=\"JUnit\" foreignId=\"IplikeTestNodeV6\">" +
                "<location>Default</location>" +
                "<labelSource>H</labelSource>" +
                "</node>";
        sendPost("/nodes", node2, 201);

        String ipv6Interface = "<ipInterface snmpPrimary=\"P\">" +
                "<ipAddress>2001:db8::1</ipAddress>" +
                "<hostName>iplike-v6-host</hostName>" +
                "</ipInterface>";
        // The second node gets id=2 in a fresh @JUnitTemporaryDatabase
        sendPost("/nodes/2/ipinterfaces", ipv6Interface, 201);

        // IPv6 wildcard pattern matches the 2001:db8:: range
        sendRequest(GET, url, parseParamData("_s=iplike==2001:db8:*:*:*:*:*:*"), 200);

        // Wrong IPv6 prefix — no match
        sendRequest(GET, url, parseParamData("_s=iplike==fe80:*:*:*:*:*:*:*"), 204);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodesWithDownAggregateStatusSearch() throws Exception {
        // DatabasePopulator seeds node1 with an unresolved (open) outage on its active SNMP service,
        // so node1 has a "down" aggregate status; the other nodes do not.
        m_databasePopulator.populateDatabase();

        String url = "/nodes";

        // Down-only: at least node1 matches
        sendRequest(GET, url, parseParamData("_s=nodesWithDownAggregateStatus==true"), 200);

        // Precise: node1 IS down, node2 is NOT
        sendRequest(GET, url, parseParamData("_s=nodesWithDownAggregateStatus==true;node.label==node1"), 200);
        sendRequest(GET, url, parseParamData("_s=nodesWithDownAggregateStatus==true;node.label==node2"), 204);

        // Excluding down nodes: node1 drops out, node2 remains
        sendRequest(GET, url, parseParamData("_s=nodesWithDownAggregateStatus==false;node.label==node1"), 204);
        sendRequest(GET, url, parseParamData("_s=nodesWithDownAggregateStatus==false;node.label==node2"), 200);

        // NOT_EQUALS true is equivalent to ==false (exclude down nodes)
        sendRequest(GET, url, parseParamData("_s=nodesWithDownAggregateStatus!=true;node.label==node1"), 204);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodesWithAssetsSearch() throws Exception {
        // DatabasePopulator seeds alternate-node1 with asset info (assetNumber, plus building "HQ"
        // carried over by NetworkBuilder). A bare node created via REST has no asset fields set
        // (its asset 'category' defaults to "Unspecified", which is intentionally NOT in the query).
        m_databasePopulator.populateDatabase();

        String bareNode = "<node type=\"A\" label=\"AssetEmptyNode\" foreignSource=\"JUnit\" foreignId=\"AssetEmptyNode\">" +
                "<location>Default</location>" +
                "<labelSource>H</labelSource>" +
                "</node>";
        sendPost("/nodes", bareNode, 201);

        String url = "/nodes";

        // Has-asset-info: alternate-node1 matches, the bare node does not
        sendRequest(GET, url, parseParamData("_s=nodesWithAssets==true;node.label==alternate-node1"), 200);
        sendRequest(GET, url, parseParamData("_s=nodesWithAssets==true;node.label==AssetEmptyNode"), 204);

        // Inverse: alternate-node1 drops out, the bare node remains
        sendRequest(GET, url, parseParamData("_s=nodesWithAssets==false;node.label==alternate-node1"), 204);
        sendRequest(GET, url, parseParamData("_s=nodesWithAssets==false;node.label==AssetEmptyNode"), 200);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAssetFilterValueWithFiqlCharacters() throws Exception {
        // A node whose asset 'building' contains FIQL-structural characters (comma + semicolon).
        // The Vue UI double-encodes such values (',' -> %252C, ';' -> %253B); the servlet container
        // and CXF (search.decode.values=true) then decode them back to the exact literal before the
        // value reaches the criteria, so an exact match must still work.
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("AssetCommaNode").setForeignSource("JUnit").setForeignId("AssetComma").setType(OnmsNode.NodeType.ACTIVE);
        builder.setBuilding("A,B;C");
        final OnmsNode node = builder.getCurrentNode();
        m_databasePopulator.getNodeDao().save(node);
        m_databasePopulator.getNodeDao().flush();

        String url = "/nodes";

        // Exact match via the double-encoded value the UI emits
        sendRequest(GET, url, parseParamData("_s=assetRecord.building==A%252CB%253BC"), 200);

        // A different (also double-encoded) value must not match
        sendRequest(GET, url, parseParamData("_s=assetRecord.building==A%252CX"), 204);
    }

    @Test
    public void testMaclikeSearch() throws Exception {
        // Create a node with a known SNMP interface MAC (physical) address via the REST API
        String node = "<node type=\"A\" label=\"MaclikeTestNode\" foreignSource=\"JUnit\" foreignId=\"MaclikeTestNode\">" +
                "<location>Default</location>" +
                "<labelSource>H</labelSource>" +
                "</node>";
        sendPost("/nodes", node, 201);

        // physAddr is stored stripped of separators and (typically) upper-cased
        String snmpInterface = "<snmpInterface ifIndex=\"1\">" +
                "<ifType>6</ifType>" +
                "<physAddr>AABBCCDDEEFF</physAddr>" +
                "</snmpInterface>";
        sendPost("/nodes/1/snmpinterfaces", snmpInterface, 201);

        String url = "/nodes";

        // Exact MAC match via maclike (case-insensitive)
        sendRequest(GET, url, parseParamData("_s=maclike==aabbccddeeff"), 200);

        // Separators in the search term are stripped before matching
        sendRequest(GET, url, parseParamData("_s=maclike==AA:BB:CC:DD:EE:FF"), 200);
        sendRequest(GET, url, parseParamData("_s=maclike==aa-bb-cc-dd-ee-ff"), 200);

        // Partial (manufacturer prefix) match
        sendRequest(GET, url, parseParamData("_s=maclike==aabbcc"), 200);

        // Non-matching MAC — no match, expect 204 No Content
        sendRequest(GET, url, parseParamData("_s=maclike==112233445566"), 204);

        // NOT_EQUALS: the node does NOT have this MAC, so it appears in results
        sendRequest(GET, url, parseParamData("_s=maclike!=112233445566"), 200);
    }

    @Test
    public void createNodeWithParent() throws Exception{

        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("Parent").setForeignSource("JUnit").setForeignId("Parent").setType(OnmsNode.NodeType.ACTIVE);
        final OnmsNode parent = builder.getCurrentNode();
        m_databasePopulator.getNodeDao().save(parent);

        builder.addNode("Child").setForeignSource("Junit").setForeignId("Child").setType(OnmsNode.NodeType.ACTIVE)
                .setParent(parent).setNodeParentId(parent.getId());
        final OnmsNode child = builder.getCurrentNode();
        m_databasePopulator.getNodeDao().save(child);
        m_databasePopulator.getNodeDao().flush();

        Assert.assertNotNull(child.getId());
        Assert.assertNotNull(parent.getId());
        sendRequest(GET, "/nodes/"+parent.getId(), 200);
        final String response = sendRequest(GET, "/nodes/"+child.getId(), 200);

        final JSONObject object = new JSONObject(response);
        Assert.assertEquals(Optional.ofNullable(parent.getId()), Optional.of(object.getInt("nodeParentID")));
    }
}
