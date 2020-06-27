/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
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
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ApplicationStatusRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private ApplicationDao applicationDao;

    private OnmsMonitoredService app1Service1;
    private OnmsMonitoredService app1Service2;

    private OnmsMonitoredService app2Service1;
    private OnmsMonitoredService app2Service2;

    private int app1Id;
    private int app2Id;

    private OnmsMonitoringLocation rdu;
    private OnmsMonitoringLocation fulda;

    public ApplicationStatusRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        databasePopulator.populateDatabase();

        final OnmsMonitoringLocation onmsMonitoringLocation = new OnmsMonitoringLocation();
        onmsMonitoringLocation.setLocationName("Fulda");
        onmsMonitoringLocation.setMonitoringArea("Hessen");
        onmsMonitoringLocation.setPriority(100L);
        this.databasePopulator.getMonitoringLocationDao().save(onmsMonitoringLocation);

        rdu = this.databasePopulator.getMonitoringLocationDao().get("RDU");
        fulda = this.databasePopulator.getMonitoringLocationDao().get("Fulda");

        final List<OnmsMonitoredService> onmsMonitoredServices = this.databasePopulator.getMonitoredServiceDao().findAll();
        app1Service1 = onmsMonitoredServices.get(0);
        app1Service2 = onmsMonitoredServices.get(1);
        app2Service1 = onmsMonitoredServices.get(2);
        app2Service2 = onmsMonitoredServices.get(3);

        final OnmsApplication app1 = new OnmsApplication();
        app1.setName("APP1");
        app1Id = this.applicationDao.save(app1);

        final OnmsApplication app2 = new OnmsApplication();
        app2.setName("APP2");
        app2Id = this.applicationDao.save(app2);

        app1Service1.setApplications(Sets.newHashSet(app1));
        app1Service2.setApplications(Sets.newHashSet(app1));
        this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(app1Service1);
        this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(app1Service2);

        app2Service1.setApplications(Sets.newHashSet(app2));
        app2Service2.setApplications(Sets.newHashSet(app2));
        this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(app2Service1);
        this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(app2Service2);

        // this should be 50% for the period of 10000-19999
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 1500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 2000);
        addLocationSpecificStatus(rdu, app1Service2, PollStatus.SERVICE_AVAILABLE, 2500);
        addLocationSpecificStatus(rdu, app1Service2, PollStatus.SERVICE_UNAVAILABLE, 3000);

        addLocationSpecificStatus(rdu, app1Service2, PollStatus.SERVICE_AVAILABLE, 9000);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 9500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 10500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 11500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 12500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 13500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 14500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 15500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 16500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 17500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 18500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 19500);

        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_AVAILABLE, 21500);
        addLocationSpecificStatus(rdu, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 22000);
        addLocationSpecificStatus(rdu, app1Service2, PollStatus.SERVICE_AVAILABLE, 22500);
        addLocationSpecificStatus(rdu, app1Service2, PollStatus.SERVICE_UNAVAILABLE, 23000);

        // this should be 25% for the period of 10000-19999
        addLocationSpecificStatus(fulda, app1Service2, PollStatus.SERVICE_AVAILABLE, 9000);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_AVAILABLE, 9500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 10500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_AVAILABLE, 11500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 12500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_AVAILABLE, 13500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 14500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_AVAILABLE, 15500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 16500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_AVAILABLE, 17500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_UNAVAILABLE, 18500);
        addLocationSpecificStatus(fulda, app1Service1, PollStatus.SERVICE_AVAILABLE, 19500);
        addLocationSpecificStatus(fulda, app1Service2, PollStatus.SERVICE_UNAVAILABLE, 15000);
    }

    private void addLocationSpecificStatus(final OnmsMonitoringLocation location, final OnmsMonitoredService monitoredService, final int result, final long timestamp) {
        final OnmsLocationSpecificStatus onmsLocationSpecificStatus = new OnmsLocationSpecificStatus();
        final PollStatus pollStatus = PollStatus.get(result, String.valueOf(result));
        pollStatus.setTimestamp(new Date(timestamp));
        onmsLocationSpecificStatus.setPollResult(pollStatus);
        onmsLocationSpecificStatus.setMonitoredService(monitoredService);
        onmsLocationSpecificStatus.setLocation(location);
        this.databasePopulator.getLocationSpecificStatusDao().save(onmsLocationSpecificStatus);
    }

    @Test
    @Transactional
    public void testApplicationStatus() throws Exception {
        final Map<String, String> params = new HashMap<>();
        params.put("start", String.valueOf(10000));
        params.put("end", String.valueOf(20000));

        final JSONObject object = new JSONObject(sendRequest(GET, "/remotepoller/" + app1Id, params, 200));

        Assert.assertEquals(app1Id, object.getInt("applicationId"));

        Assert.assertEquals(10000, object.getLong("start"));

        Assert.assertEquals(20000, object.getLong("end"));

        final Map<String, Integer> locationMap = new TreeMap();
        locationMap.put(object.getJSONArray("location").getJSONObject(0).getString("name"), 0);
        locationMap.put(object.getJSONArray("location").getJSONObject(1).getString("name"), 1);

        Assert.assertEquals("RDU",
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("RDU"))
                        .getString("name"));

        Assert.assertEquals("Fulda",
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("Fulda"))
                        .getString("name"));

        Assert.assertEquals(50.0,
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("RDU"))
                        .getDouble("aggregated-status"), 0.00001);

        Assert.assertEquals(25.0,
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("Fulda"))
                        .getDouble("aggregated-status"), 0.00001);
    }

    private void checkApplicationService(int applicationId, int monitoredServiceId, double rduStatus, double fuldaStatus, String rduResourceId, String fuldaResourceId) throws Exception {
        final Map<String, String> params = new HashMap<>();
        params.put("start", String.valueOf(10000));
        params.put("end", String.valueOf(20000));

        final JSONObject object = new JSONObject(sendRequest(GET, "/remotepoller/" + applicationId + "/" + monitoredServiceId, params, 200));

        final Map<String, Integer> locationMap = new TreeMap();
        locationMap.put(object.getJSONArray("location").getJSONObject(0).getString("name"), 0);
        locationMap.put(object.getJSONArray("location").getJSONObject(1).getString("name"), 1);

        Assert.assertEquals(applicationId, object.getInt("applicationId"));
        Assert.assertEquals(monitoredServiceId, object.getInt("monitoredServiceId"));
        Assert.assertEquals(10000, object.getLong("start"));
        Assert.assertEquals(20000, object.getLong("end"));


        Assert.assertEquals("RDU",
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("RDU"))
                        .getString("name"));

        Assert.assertEquals("Fulda",
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("Fulda"))
                        .getString("name"));

        Assert.assertEquals(rduStatus,
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("RDU"))
                        .getDouble("aggregated-status"), 0.00001);

        Assert.assertEquals(fuldaStatus,
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("Fulda"))
                        .getDouble("aggregated-status"), 0.00001);

        Assert.assertEquals(rduResourceId,
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("RDU"))
                        .getString("response-resource-id"));

        Assert.assertEquals(fuldaResourceId,
                object.getJSONArray("location")
                        .getJSONObject(locationMap.get("Fulda"))
                        .getString("response-resource-id"));
    }

    @Test
    @Transactional
    public void testApplicationServiceStatus() throws Exception {
        checkApplicationService(app1Id, app1Service1.getId(), 50.0, 50.0, "192.168.1.1[ICMP]@RDU", "192.168.1.1[ICMP]@Fulda");
        checkApplicationService(app1Id, app1Service2.getId(), 100.0, 50.0, "192.168.1.1[SNMP]@RDU", "192.168.1.1[SNMP]@Fulda");
    }

    @Test
    @Transactional
    public void testDefaults() throws Exception {
        long currentTimeMs = new Date().getTime();
        long oneDayMs = 60*60*24*1000;
        final Map<String, String> params = new HashMap<>();
        final JSONObject object1 = new JSONObject(sendRequest(GET, "/remotepoller/" + app1Id, params, 200));
        final JSONObject object2 = new JSONObject(sendRequest(GET, "/remotepoller/" + app1Id + "/" + app1Service1.getId(), params, 200));

        Assert.assertTrue(object1.getLong("start")>=currentTimeMs-oneDayMs && object1.getLong("start")<=currentTimeMs-oneDayMs+2000);
        Assert.assertTrue(object1.getLong("end")>=currentTimeMs && object1.getLong("end")<=currentTimeMs+2000 );

        Assert.assertTrue(object2.getLong("start")>=currentTimeMs-oneDayMs && object2.getLong("start")<=currentTimeMs-oneDayMs+2000);
        Assert.assertTrue(object2.getLong("end")>=currentTimeMs && object2.getLong("end")<=currentTimeMs+2000 );

        long end = 10000000000L;
        params.put("end", String.valueOf(end));

        final JSONObject object3 = new JSONObject(sendRequest(GET, "/remotepoller/" + app1Id, params, 200));
        final JSONObject object4 = new JSONObject(sendRequest(GET, "/remotepoller/" + app1Id + "/" + app1Service1.getId(), params, 200));

        Assert.assertEquals(end-oneDayMs, object3.getLong("start"));
        Assert.assertEquals(end, object3.getLong("end"));

        Assert.assertEquals(end-oneDayMs, object4.getLong("start"));
        Assert.assertEquals(end, object4.getLong("end"));
    }
}