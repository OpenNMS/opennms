/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
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
public class OutageRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(OutageRestServiceIT.class);

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private ServletContext m_context;

    @Before
    @Override
    public void setUp() throws Throwable {
        super.setUp();
        Assert.assertNotNull(populator);
        Assert.assertNotNull(applicationDao);

        populator.addExtension(new DatabasePopulator.Extension<ApplicationDao>() {

            private OnmsOutage unresolvedOutage;

            private OnmsEvent outageEvent;

            private OnmsApplication application;

            @Override
            public DatabasePopulator.DaoSupport<ApplicationDao> getDaoSupport() {
                return new DatabasePopulator.DaoSupport<>(ApplicationDao.class, applicationDao);
            }

            @Override
            public void onPopulate(DatabasePopulator populator, ApplicationDao dao) {
                OnmsDistPoller distPoller = populator.getDistPollerDao().whoami();
                outageEvent = populator.buildEvent(distPoller);
                outageEvent.setEventSeverity(OnmsSeverity.MINOR.getId());
                outageEvent.setEventCreateTime(new Date(1436881548292L));
                outageEvent.setEventTime(new Date(1436881548292L));
                populator.getEventDao().save(outageEvent);
                populator.getEventDao().flush();

                // create the application
                application = new OnmsApplication();
                application.setName("Awesome Application");
                dao.save(application);

                // get the SNMP service from node 1 and assign the application to it
                final OnmsMonitoredService svc = populator.getMonitoredServiceDao().get(populator.getNode1().getId(), InetAddressUtils.addr("192.168.1.2"), "HTTP");
                svc.addApplication(application);
                application.addMonitoredService(svc);
                populator.getMonitoredServiceDao().saveOrUpdate(svc);
                populator.getMonitoredServiceDao().flush();

                // create a unresolved outage
                unresolvedOutage = new OnmsOutage(new Date(1436881548292L), outageEvent, svc);
                populator.getOutageDao().save(unresolvedOutage);
                populator.getOutageDao().flush();
            }

            @Override
            public void onShutdown(DatabasePopulator populator, ApplicationDao dao) {
                // All other tables have been already deleted,
                // Delete OnmsApplications
                for (OnmsApplication application : dao.findAll()) {
                    dao.delete(application);
                }
            }
        });

        populator.populateDatabase();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        populator.resetDatabase();
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAllOutages() throws Exception {
        String xml = sendRequest(GET, "/outages", 200);
        Assert.assertNotNull(xml);

        MockHttpServletRequest jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);
        Assert.assertNotNull(json);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testOutageSearches() throws Exception {
        MockHttpServletRequest jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("ipInterface.ipAddress=192.168.1.2");
        String json = sendRequest(jsonRequest, 200);
        LOG.debug(json);
        JSONObject restObject = new JSONObject(json);
        assertEquals(1, restObject.getJSONArray("outage").length());

        jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("comparator=ge&serviceLostEvent.eventSeverity=5"); // OnmsSeverity.MINOR
        json = sendRequest(jsonRequest, 200);
        LOG.debug(json);
        restObject = new JSONObject(json);
        assertEquals(1, restObject.getJSONArray("outage").length());

        jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("comparator=lt&serviceLostEvent.eventSeverity=2"); // OnmsSeverity.CLEARED
        json = sendRequest(jsonRequest, 200);
        LOG.debug(json);
        restObject = new JSONObject(json);
        assertEquals(2, restObject.getJSONArray("outage").length());

        jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("comparator=like&serviceLostEvent.eventLogMsg=Test%25");
        json = sendRequest(jsonRequest, 200);
        LOG.debug(json);
        restObject = new JSONObject(json);
        assertEquals(3, restObject.getJSONArray("outage").length());

        // Check serviceRegainedEvent filters
        jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("comparator=lt&serviceRegainedEvent.eventSeverity=2"); // OnmsSeverity.CLEARED
        json = sendRequest(jsonRequest, 200);
        LOG.debug(json);
        // There is one test outage that has been resolved
        restObject = new JSONObject(json);
        assertEquals(1, restObject.getJSONArray("outage").length());

        jsonRequest = createRequest(m_context, GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("comparator=like&serviceRegainedEvent.eventLogMsg=Test%25");
        json = sendRequest(jsonRequest, 200);
        LOG.debug(json);
        // There is one test outage that has been resolved
        restObject = new JSONObject(json);
        assertEquals(1, restObject.getJSONArray("outage").length());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetOutagesForNode() throws Exception {
        // Test last week (no outages)
        MockHttpServletRequest jsonRequest = createRequest(m_context, GET, "/outages/forNode/1");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);
        JSONObject restObject = new JSONObject(json);
        Assert.assertEquals(2, restObject.getJSONArray("outage").length()); // 2 outstanding

        // Test a range with outages
        long start = 1436846400000l;
        long end = 1436932800000l;
        jsonRequest = createRequest(m_context, GET, "/outages/forNode/1");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("start=" + start + "&end=" + end);
        json = sendRequest(jsonRequest, 200);
        restObject = new JSONObject(json);
        for (int i=0; i < restObject.getJSONArray("outage").length(); i++) {
            JSONObject obj = restObject.getJSONArray("outage").getJSONObject(i);
            Assert.assertTrue(obj.getLong("ifLostService") > start);
            Assert.assertTrue(obj.getLong("ifLostService") < end);
        }

        // Test a range without outages
        jsonRequest = createRequest(m_context, GET, "/outages/forNode/1");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("start=1436932800000&end=1437019200000");
        json = sendRequest(jsonRequest, 200);
        restObject = new JSONObject(json);
        Assert.assertEquals(2, restObject.getJSONArray("outage").length()); // 2 outstanding
    }

    @Test
    @JUnitTemporaryDatabase
    public void testIPhone() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("limit", "50");
        parameters.put("orderBy", "ifLostService");
        parameters.put("order", "desc");
        String xml = sendRequest(GET, "/outages/forNode/1", parameters, 200);
        Assert.assertTrue(xml.contains("SNMP"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testOutagesJson() throws Exception {
        String url = "/outages";

        // GET all users
        MockHttpServletRequest jsonRequest = createRequest(m_context, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("limit=1&orderBy=id");
        String json = sendRequest(jsonRequest, 200);

        JSONObject restObject = new JSONObject(json);
        JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/outages.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }
}
