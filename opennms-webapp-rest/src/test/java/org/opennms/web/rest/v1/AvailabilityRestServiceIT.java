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
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
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
}
