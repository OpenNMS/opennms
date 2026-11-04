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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.category.AvailabilityNode;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class AvailabilityRestServiceIT extends AbstractSpringJerseyRestTestCase {
    @Autowired
    TransactionTemplate m_template;

    @Autowired
    DatabasePopulator m_populator;

    @Autowired
    private ServletContext m_servletContext;

    @Override
    protected void afterServletStart() {
        m_template.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                m_populator.populateDatabase();
            }
        });
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailability() throws Exception {
        String xml = sendRequest(GET, "/availability", new HashMap<String,String>(), 200);
        assertNotNull(xml);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityJson() throws Exception {
        String url = "/availability";

        // GET all items
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        // TODO: The comment and last-updated fields are blank in the objects that are
        // fetched. Figure out how to get them to populate so that we can test serialization
        // of those values.
        //
        JSONObject restObject = new JSONObject(json);
        JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/availability.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);

        // GET node item
        jsonRequest = createRequest(m_servletContext, GET, url  + "/nodes/" + m_populator.getNode1().getId());
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        json = sendRequest(jsonRequest, 200);

        restObject = new JSONObject(json);
        expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/availability_node.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }

    /**
     * The window parameters are plumbing tests, not arithmetic tests. In the populated database no
     * interface is managed, so getPercentAvailabilityInWindow matches no row and every figure is
     * window-independent -- a windowed request cannot show different numbers here. The arithmetic is
     * covered against real windows by CategoryModelIT.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeWithWindowMatchesUnwindowed() throws Exception {
        final int nodeId = m_populator.getNode1().getId();
        final long end = System.currentTimeMillis();
        final long start = end - 86_400_000L;

        MockHttpServletRequest request = createRequest(m_servletContext, GET,
                "/availability/nodes/" + nodeId, parseParamData("start=" + start + "&end=" + end),
                getUser(), getUserRoles());
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        final String json = sendRequest(request, 200);

        final JSONObject expectedObject = new JSONObject(
                IOUtils.toString(new FileInputStream("src/test/resources/v1/availability_node.json")));
        JSONAssert.assertEquals(expectedObject, new JSONObject(json), true);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeWithOnlyEndDefaultsStart() throws Exception {
        final int nodeId = m_populator.getNode1().getId();
        sendRequest(GET, "/availability/nodes/" + nodeId,
                parseParamData("end=" + System.currentTimeMillis()), 200);
    }

    /**
     * 400 rather than 500 is the point of this test: it is what proves the window is validated
     * before getNode's catch-all, which rewraps everything as a 500. An empty window would otherwise
     * reach getPercentAvailabilityInWindow, which divides by the window length.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeRejectsEmptyWindow() throws Exception {
        final int nodeId = m_populator.getNode1().getId();
        final long now = System.currentTimeMillis();
        sendRequest(GET, "/availability/nodes/" + nodeId,
                parseParamData("start=" + now + "&end=" + now), 400);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeRejectsInvertedWindow() throws Exception {
        final int nodeId = m_populator.getNode1().getId();
        final long now = System.currentTimeMillis();
        sendRequest(GET, "/availability/nodes/" + nodeId,
                parseParamData("start=" + now + "&end=" + (now - 3_600_000L)), 400);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNode() throws Exception {
        final OnmsNode node = m_populator.getNode1();
        final AvailabilityRestService ars = new AvailabilityRestService();
        ars.setNodeDao(m_populator.getNodeDao());
        final AvailabilityNode an = ars.getAvailabilityNode(node.getId());
        assertNotNull(an);
        System.err.println(JaxbUtils.marshal(an));

        // Compare the object to the same node fetched via REST
        String url = "/availability/nodes/" + node.getId();
        AvailabilityNode restNode = getXmlObject(JaxbUtils.getContextFor(AvailabilityNode.class), url, 200, AvailabilityNode.class);
        Assert.assertNotNull(restNode);
        Assert.assertTrue(an.toString() + " != " + restNode.toString(), an.equals(restNode));
    }

    /**
     * Paging is over the node's interfaces in address order (IPv4 before IPv6), and
     * ipinterfaceCount is the total either way, for a paginator.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodePagesInterfaces() throws Exception {
        final int nodeId = m_populator.getNode1().getId();

        final JSONObject first = getNodeJson(nodeId, "limit=2&offset=0");
        Assert.assertEquals(4, first.getInt("ipinterfaceCount"));
        Assert.assertEquals(List.of("192.168.1.1", "192.168.1.2"), addresses(first));

        final JSONObject second = getNodeJson(nodeId, "limit=2&offset=2");
        Assert.assertEquals(4, second.getInt("ipinterfaceCount"));
        Assert.assertEquals(List.of("192.168.1.3", "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5"), addresses(second));

        // The node's own figures cover every interface, whichever page was asked for.
        Assert.assertEquals(first.getDouble("availability"), second.getDouble("availability"), 0.0);
        Assert.assertEquals(first.getInt("service-count"), second.getInt("service-count"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodePastTheLastPageIsEmpty() throws Exception {
        final JSONObject past = getNodeJson(m_populator.getNode1().getId(), "limit=2&offset=10");

        Assert.assertEquals(4, past.getInt("ipinterfaceCount"));
        Assert.assertTrue(addresses(past).isEmpty());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeRejectsNegativePaging() throws Exception {
        final int nodeId = m_populator.getNode1().getId();
        sendRequest(GET, "/availability/nodes/" + nodeId, parseParamData("limit=-1"), 400);
        sendRequest(GET, "/availability/nodes/" + nodeId, parseParamData("offset=-1"), 400);
    }

    /**
     * An interface with no monitored services has nothing for the availability panel to show, which
     * drops it. Paged over every interface, a page of such interfaces came back empty -- here the
     * unmonitored 192.168.1.0 sorts first and would fill the start of page 1 -- so withServices leaves
     * them out before counting and paging.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeWithServicesPagesOnlyMonitoredInterfaces() throws Exception {
        final int nodeId = m_populator.getNode1().getId();
        m_template.execute(status -> {
            final OnmsNode node = m_populator.getNodeDao().get(nodeId);
            m_populator.getIpInterfaceDao().save(new OnmsIpInterface("192.168.1.0", node));
            m_populator.getIpInterfaceDao().flush();
            return null;
        });

        final JSONObject all = getNodeJson(nodeId, "limit=2&offset=0");
        Assert.assertEquals(5, all.getInt("ipinterfaceCount"));
        Assert.assertEquals(List.of("192.168.1.0", "192.168.1.1"), addresses(all));

        final JSONObject monitored = getNodeJson(nodeId, "limit=2&offset=0&withServices=true");
        Assert.assertEquals(4, monitored.getInt("ipinterfaceCount"));
        Assert.assertEquals(List.of("192.168.1.1", "192.168.1.2"), addresses(monitored));
    }

    // offset + limit overflows an int here; the remaining interfaces come back, not a 500.
    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNodeWithAHugeLimit() throws Exception {
        final JSONObject node = getNodeJson(m_populator.getNode1().getId(), "limit=2147483647&offset=1");

        Assert.assertEquals(4, node.getInt("ipinterfaceCount"));
        Assert.assertEquals(List.of("192.168.1.2", "192.168.1.3", "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5"), addresses(node));
    }

    private JSONObject getNodeJson(final int nodeId, final String query) throws Exception {
        final MockHttpServletRequest request = createRequest(m_servletContext, GET,
                "/availability/nodes/" + nodeId, parseParamData(query), getUser(), getUserRoles());
        // The harness sets the parameters but not the query string, and with more than one parameter
        // CXF does not see them all without it.
        request.setQueryString(query);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return new JSONObject(sendRequest(request, 200));
    }

    private static List<String> addresses(final JSONObject node) {
        final List<String> result = new ArrayList<>();
        final JSONArray interfaces = node.optJSONArray("ipinterfaces");
        for (int i = 0; interfaces != null && i < interfaces.length(); i++) {
            result.add(interfaces.getJSONObject(i).getString("address"));
        }
        return result;
    }
}
