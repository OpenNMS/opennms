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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
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
public class ApplicationRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private MockEventIpcManager eventIpcManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public ApplicationRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "INFO");
        // the temporary database is shared by the methods of this class
        if (databasePopulator.getNodeDao().countAll() == 0) {
            databasePopulator.populateDatabase();
        }
        transactionTemplate.execute(status -> {
            for (final OnmsMonitoredService service : databasePopulator.getMonitoredServiceDao().findAll()) {
                service.getApplications().clear();
                databasePopulator.getMonitoredServiceDao().update(service);
            }
            applicationDao.findAll().forEach(applicationDao::delete);
            return null;
        });
    }

    private int createApplication(final String name) throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/applications", new JSONObject().put("name", name).toString(), 201);
        final OnmsApplication application = applicationDao.findByName(name);
        assertNotNull(application);
        return application.getId();
    }

    private Set<Integer> memberServiceIds(final int applicationId) {
        return transactionTemplate.execute(status -> applicationDao.get(applicationId).getMonitoredServices().stream()
                .map(OnmsMonitoredService::getId)
                .collect(Collectors.toSet()));
    }

    private Set<String> memberLocations(final int applicationId) {
        return transactionTemplate.execute(status -> applicationDao.get(applicationId).getPerspectiveLocations().stream()
                .map(location -> location.getLocationName())
                .collect(Collectors.toSet()));
    }

    @Test
    public void createRejectsBlankLongAndDuplicateNames() throws Exception {
        createApplication("Web Store");

        assertEquals("An application name is required.",
                sendData(POST, MediaType.APPLICATION_JSON, "/applications", "{\"name\":\"   \"}", 400).getContentAsString());
        assertEquals("The application name cannot be longer than 32 characters.",
                sendData(POST, MediaType.APPLICATION_JSON, "/applications", "{\"name\":\"" + "x".repeat(33) + "\"}", 400).getContentAsString());
        assertEquals("An application named Web Store already exists.",
                sendData(POST, MediaType.APPLICATION_JSON, "/applications", "{\"name\":\" Web Store \"}", 400).getContentAsString());

        // a name is stored trimmed
        sendData(POST, MediaType.APPLICATION_JSON, "/applications", "{\"name\":\"  Billing  \"}", 201);
        assertNotNull(applicationDao.findByName("Billing"));
    }

    @Test
    public void membersRoundTripAndSendOneChangedEvent() throws Exception {
        final int id = createApplication("Web Store");
        final List<OnmsMonitoredService> services = databasePopulator.getMonitoredServiceDao().findAll();
        final int first = services.get(0).getId();
        final int second = services.get(1).getId();
        final int third = services.get(2).getId();

        eventIpcManager.getEventAnticipator().reset();
        eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.APPLICATION_CHANGED_EVENT_UEI, "Web UI").getEvent());
        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                new JSONObject().put("serviceIds", new JSONArray(List.of(first, second))).put("perspectiveLocations", new JSONArray(List.of("RDU", "Fulda"))).toString(), 204);
        eventIpcManager.getEventAnticipator().verifyAnticipated(5000, 0, 0, 0, 0);
        assertEquals(Set.of(first, second), memberServiceIds(id));
        assertEquals(Set.of("RDU", "Fulda"), memberLocations(id));

        final JSONObject members = new JSONObject(sendRequest(GET, "/applications/" + id + "/members", 200));
        assertEquals("Web Store", members.getString("name"));
        assertEquals(2, members.getJSONArray("services").length());
        final JSONObject service = members.getJSONArray("services").getJSONObject(0);
        assertNotNull(service.getString("nodeLabel"));
        assertNotNull(service.getString("ipAddress"));
        assertNotNull(service.getString("serviceName"));
        assertEquals(List.of("Fulda", "RDU"), members.getJSONArray("perspectiveLocations").toList());

        // swap one service, leave the locations alone
        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                new JSONObject().put("serviceIds", new JSONArray(List.of(second, third))).toString(), 204);
        assertEquals(Set.of(second, third), memberServiceIds(id));
        assertEquals(Set.of("RDU", "Fulda"), memberLocations(id));

        // an unchanged body sends no event
        eventIpcManager.getEventAnticipator().reset();
        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                new JSONObject().put("serviceIds", new JSONArray(List.of(third, second))).put("perspectiveLocations", new JSONArray(List.of("Fulda", "RDU"))).toString(), 204);
        assertTrue(eventIpcManager.getEventAnticipator().getUnanticipatedEvents().stream()
                .noneMatch(event -> EventConstants.APPLICATION_CHANGED_EVENT_UEI.equals(event.getUei())));

        // clearing both
        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                new JSONObject().put("serviceIds", new JSONArray()).put("perspectiveLocations", new JSONArray()).toString(), 204);
        assertEquals(Collections.emptySet(), memberServiceIds(id));
        assertEquals(Collections.emptySet(), memberLocations(id));
    }

    @Test
    public void membersRejectUnknownEntriesWithoutChangingAnything() throws Exception {
        final int id = createApplication("Web Store");
        final int known = databasePopulator.getMonitoredServiceDao().findAll().get(0).getId();
        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                new JSONObject().put("serviceIds", new JSONArray(List.of(known))).toString(), 204);

        assertEquals("Monitored service 999999 was not found.",
                sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                        new JSONObject().put("serviceIds", new JSONArray(List.of(999999))).toString(), 400).getContentAsString());
        assertEquals("Monitoring location Nowhere was not found.",
                sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                        new JSONObject().put("serviceIds", new JSONArray()).put("perspectiveLocations", new JSONArray(List.of("Nowhere"))).toString(), 400).getContentAsString());
        assertEquals(Set.of(known), memberServiceIds(id));

        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/424242/members", "{\"serviceIds\":[]}", 404);
        sendRequest(GET, "/applications/424242/members", 404);
    }

    @Test
    public void summariesCountMembers() throws Exception {
        final int id = createApplication("Web Store");
        createApplication("Billing");
        final List<OnmsMonitoredService> services = databasePopulator.getMonitoredServiceDao().findAll();
        sendData(PUT, MediaType.APPLICATION_JSON, "/applications/" + id + "/members",
                new JSONObject().put("serviceIds", new JSONArray(List.of(services.get(0).getId(), services.get(1).getId())))
                        .put("perspectiveLocations", new JSONArray(List.of("RDU"))).toString(), 204);

        final JSONArray summaries = new JSONArray(sendRequest(GET, "/applications/summaries", 200));
        assertEquals(2, summaries.length());
        assertEquals("Billing", summaries.getJSONObject(0).getString("name"));
        assertEquals(0, summaries.getJSONObject(0).getInt("serviceCount"));
        assertEquals("Web Store", summaries.getJSONObject(1).getString("name"));
        assertEquals(2, summaries.getJSONObject(1).getInt("serviceCount"));
        assertEquals(List.of("RDU"), summaries.getJSONObject(1).getJSONArray("perspectiveLocations").toList());
    }

    @Test
    public void serviceCandidatesMatchLabelAddressAndServiceName() throws Exception {
        final int total = databasePopulator.getMonitoredServiceDao().countAll();

        JSONObject page = new JSONObject(sendRequest(GET, "/applications/service-candidates", 200));
        assertEquals(total, page.getInt("totalCount"));

        page = new JSONObject(sendRequest(GET, "/applications/service-candidates?search=192.168.2.", 200));
        assertTrue(page.getInt("totalCount") > 0);
        for (final Object service : page.getJSONArray("services")) {
            assertTrue(((JSONObject) service).getString("ipAddress").startsWith("192.168.2."));
        }

        page = new JSONObject(sendRequest(GET, "/applications/service-candidates?search=http", 200));
        assertTrue(page.getInt("totalCount") > 0);
        for (final Object service : page.getJSONArray("services")) {
            assertEquals("HTTP", ((JSONObject) service).getString("serviceName"));
        }

        page = new JSONObject(sendRequest(GET, "/applications/service-candidates?search=NODE3", 200));
        assertTrue(page.getInt("totalCount") > 0);
        final Set<String> labels = new HashSet<>();
        for (final Object service : page.getJSONArray("services")) {
            labels.add(((JSONObject) service).getString("nodeLabel"));
        }
        assertEquals(Set.of("node3"), labels);

        // LIKE wildcards in the search text are literal: no label, address or service name has an underscore
        assertEquals(0, new JSONObject(sendRequest(GET, "/applications/service-candidates?search=_", 200)).getInt("totalCount"));

        page = new JSONObject(sendRequest(GET, "/applications/service-candidates?limit=2", 200));
        assertEquals(2, page.getJSONArray("services").length());
        assertEquals(total, page.getInt("totalCount"));
    }
}
