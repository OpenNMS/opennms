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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class OutageRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(OutageRestServiceIT.class);
    
    public OutageRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    /**
     * The lost-service time DatabasePopulator gives the two core-poller outages on
     * node1 / 192.168.1.1 / SNMP. One is resolved at the same instant, one is still open.
     */
    private static final long LOST = 1436881548292L;

    /** Lost and regained time of the outage added below, both before {@link #LOST}. */
    private static final long EARLIER_LOST = LOST - 10_000L;
    private static final long EARLIER_REGAINED = LOST - 5_000L;

    private int m_nodeId;

    /** Identity of node1's SNMP service on 192.168.1.1, captured at setup: the ids the sequences
     *  hand out are not stable across test databases, so the expectations are derived, not literal. */
    private int m_ifServiceId;
    private int m_ipInterfaceId;
    private int m_serviceTypeId;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
        m_nodeId = m_databasePopulator.getNode1().getId();

        // The populator has no outage that both began and ended before a window, which is the case
        // that proves an outage is reported with its true start rather than the window's.
        final OnmsMonitoredService svc = m_databasePopulator.getMonitoredServiceDao()
                .get(m_nodeId, InetAddressUtils.addr("192.168.1.1"), "SNMP");
        m_databasePopulator.getOutageDao()
                .save(new OnmsOutage(new Date(EARLIER_LOST), new Date(EARLIER_REGAINED), svc));
        m_databasePopulator.getOutageDao().flush();

        m_ifServiceId = svc.getId();
        m_ipInterfaceId = svc.getIpInterfaceId();
        m_serviceTypeId = svc.getServiceId();
    }

    private JSONObject timeline(final long start, final long end) throws Exception {
        final String json = sendRequest(GET, "/outages/timeline/" + m_nodeId,
                parseParamData("start=" + start + "&end=" + end), 200);
        return new JSONObject(json);
    }

    private static JSONArray outages(final JSONObject doc) {
        return doc.optJSONArray("outage") == null ? new JSONArray() : doc.getJSONArray("outage");
    }

    @Test
    public void testTimelineReturnsOverlappingOutagesAndExcludesPerspectives() throws Exception {
        // Four outages sit on this service: two core-poller (one resolved at LOST, one still open)
        // and two recorded by a remote perspective. The open perspective outage overlaps every
        // window, so a count of 2 here is also the proof that perspectives are excluded.
        final JSONObject doc = timeline(LOST - 1000L, LOST + 1000L);
        assertEquals(2, outages(doc).length());
        assertEquals(2, doc.getInt("count"));
    }

    @Test
    public void testTimelineRegainedBoundaryIsStrict() throws Exception {
        // The resolved outage regained service exactly at LOST, and the predicate is
        // ifRegainedService > start, so a window starting at LOST must not include it.
        final JSONArray rows = outages(timeline(LOST, LOST + 1000L));
        assertEquals(1, rows.length());
        assertTrue("the surviving row should be the still-open outage",
                rows.getJSONObject(0).isNull("ifRegainedService"));
    }

    @Test
    public void testTimelineIncludesStillOpenOutageStartedBeforeWindow() throws Exception {
        // Entirely before the window except that it never ended.
        final JSONArray rows = outages(timeline(LOST + 1000L, LOST + 2000L));
        assertEquals(1, rows.length());
        assertTrue(rows.getJSONObject(0).isNull("ifRegainedService"));
        assertEquals(LOST, rows.getJSONObject(0).getLong("ifLostService"));
    }

    @Test
    public void testTimelineExcludesOutagesOutsideWindow() throws Exception {
        // Both core-poller outages begin after this window ends; the perspective ones are filtered.
        assertEquals(0, outages(timeline(LOST - 2000L, LOST - 1000L)).length());
    }

    @Test
    public void testTimelineReportsUnclampedLostServiceTime() throws Exception {
        final JSONArray rows = outages(timeline(EARLIER_REGAINED - 2000L, LOST + 1000L));
        assertEquals(3, rows.length());

        JSONObject earlier = null;
        for (int i = 0; i < rows.length(); i++) {
            if (!rows.getJSONObject(i).isNull("ifRegainedService")
                    && rows.getJSONObject(i).getLong("ifRegainedService") == EARLIER_REGAINED) {
                earlier = rows.getJSONObject(i);
            }
        }
        assertTrue("expected the outage that began before the window", earlier != null);
        // Not clamped to the window start: the caller needs the true start to draw the bar.
        assertEquals(EARLIER_LOST, earlier.getLong("ifLostService"));
    }

    @Test
    public void testTimelineRowCarriesJoinKeysAndServiceIdentity() throws Exception {
        final JSONObject row = outages(timeline(LOST - 1000L, LOST + 1000L)).getJSONObject(0);
        assertEquals("192.168.1.1", row.getString("ipAddress"));
        assertEquals("SNMP", row.getString("serviceName"));
        // ifServiceId and ipInterfaceId are the monitored service and IP interface ids, which are
        // what GET /rest/availability/nodes/{id} reports as the service and interface `id`, so the
        // two documents join on them. serviceId is the service *type*, for a service detail link.
        assertEquals(m_ifServiceId, row.getInt("ifServiceId"));
        assertEquals(m_ipInterfaceId, row.getInt("ipInterfaceId"));
        assertEquals(m_serviceTypeId, row.getInt("serviceId"));
    }

    @Test
    public void testTimelineEnvelopeEchoesWindow() throws Exception {
        final long start = LOST - 1000L;
        final long end = LOST + 1000L;
        final JSONObject doc = timeline(start, end);
        assertEquals(m_nodeId, doc.getInt("nodeId"));
        assertEquals(start, doc.getLong("start"));
        assertEquals(end, doc.getLong("end"));
        assertTrue(doc.getLong("nodeCreateTime") > 0L);
    }

    @Test
    public void testTimelineDefaultsToLastTwentyFourHours() throws Exception {
        final JSONObject doc = new JSONObject(sendRequest(GET, "/outages/timeline/" + m_nodeId, 200));
        assertEquals(86_400_000L, doc.getLong("end") - doc.getLong("start"));
    }

    @Test
    public void testTimelineRejectsEmptyWindow() throws Exception {
        sendRequest(GET, "/outages/timeline/" + m_nodeId,
                parseParamData("start=" + LOST + "&end=" + LOST), 400);
    }

    @Test
    public void testTimelineUnknownNodeIsNotFound() throws Exception {
        sendRequest(GET, "/outages/timeline/999999", 404);
    }

    @Test
    public void testTimelineServesXml() throws Exception {
        // Locks the two-segment path against the inherited @Path("{id}") handler, whose dispatch
        // would otherwise depend on the requested media type.
        final MockHttpServletRequest request = createRequest(servletContext,
                GET, "/outages/timeline/" + m_nodeId);
        request.addHeader("Accept", MediaType.APPLICATION_XML);
        final String xml = sendRequest(request, 200);
        assertTrue(xml, xml.contains("<outage-timeline"));
    }

    // TODO Needs some work
    @Test
    public void testOutages() throws Exception {
        String url = "/outages";

        LOG.warn(sendRequest(GET, url, parseParamData("orderBy=id"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifLostService=gt=2017-04-01T00:00:00.000-0400"), 204));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifLostService=le=2017-04-01T00:00:00.000-0400"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService==1970-01-01T00:00:00.000-0000"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService!=1970-01-01T00:00:00.000-0000"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.suppressTime==1970-01-01T00:00:00.000-0000"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.suppressTime!=1970-01-01T00:00:00.000-0000"), 204));
    }

}
