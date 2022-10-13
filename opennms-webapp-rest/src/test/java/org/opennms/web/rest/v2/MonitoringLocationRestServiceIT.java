/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

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
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MonitoringLocationRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationRestServiceIT.class);
    private static final Integer DEFAULT_LIMIT = 10;
    private static final Integer LOCATION_COUNT = 15;
    @Autowired
    private MockEventIpcManager eventIpcManager;

    public MonitoringLocationRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testFiqlSearch() throws Exception {

        // Add 5 locations
        for (int i = 0; i < 5; i++) {
            String location = "<location location-name=\"hello-world-" + i + "\" monitoring-area=\"" + i + "\"/>";
            sendPost("/monitoringLocations", location, 201, null);
        }

        LOG.warn(sendRequest(GET, "/monitoringLocations/count", Collections.emptyMap(), 200));

        LOG.warn(sendRequest(GET, "/monitoringLocations", Collections.emptyMap(), 200));

        LOG.warn(sendRequest(GET, "/monitoringLocations", parseParamData("_s=monitoringArea==2"), 200));

    }

    @Test
    @Transactional
    public void testDelete() throws Exception {
        sendPost("/monitoringLocations", "<location location-name=\"Test\" monitoring-area=\"test\" priority=\"100\"/>", 201);
        sendRequest(DELETE, "/monitoringLocations/Test", 204);
    }

    @Test
    @Transactional
    public void testEventOnUpdate() throws Exception {
        this.eventIpcManager.getEventAnticipator().reset();

        final OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("location1");
        location.setMonitoringArea("monitoringarea1");
        location.setPriority(100L);

        // create a location
        sendData(POST, MediaType.APPLICATION_XML,"/monitoringLocations", JaxbUtils.marshal(location), 201);

        // modify monitoring area
        location.setMonitoringArea("monitoringarea1-modified");
        sendData(PUT, MediaType.APPLICATION_XML,"/monitoringLocations/location1", JaxbUtils.marshal(location), 204);
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        // add polling package
        sendData(PUT, MediaType.APPLICATION_XML,"/monitoringLocations/location1", JaxbUtils.marshal(location), 204);

        sendRequest(DELETE, "/monitoringLocations/location1", 204);
    }

    @Test
    @Transactional
    public void testEventOnCreationAndDeletion() throws Exception {
        this.eventIpcManager.getEventAnticipator().reset();

        final OnmsMonitoringLocation location1 = new OnmsMonitoringLocation();
        location1.setLocationName("location1");
        location1.setMonitoringArea("monitoringarea1");
        location1.setPriority(100L);

        // create location without associated polling packages
        sendData(POST, MediaType.APPLICATION_XML,"/monitoringLocations", JaxbUtils.marshal(location1), 201);
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        final OnmsMonitoringLocation location2 = new OnmsMonitoringLocation();
        location2.setLocationName("location2");
        location2.setMonitoringArea("monitoringarea2");
        location2.setPriority(100L);

        // create location with associated polling packages
        sendData(POST, MediaType.APPLICATION_XML,"/monitoringLocations", JaxbUtils.marshal(location2), 201);

        // delete the one without polling packages
        sendRequest(DELETE, "/monitoringLocations/location1", 204);
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        // delete the one with polling packages
        sendRequest(DELETE, "/monitoringLocations/location2", 204);
    }

    @Test
    @Transactional
    public void testLocationLimits() throws Exception {
        // There is one default location
        for (int i = 0; i < LOCATION_COUNT - 1; i++) {
            Location loc = new Location();
            loc.locationName = "Location-" + String.format("%05d", i);
            loc.monitoringArea = "LocationArea-" + String.format("%05d", i);

            // post it to web service
            String location = "<location location-name=\"" + loc.locationName + "\" monitoring-area=\"" + loc.monitoringArea + "\"/>";
            sendPost("/monitoringLocations", location, 201, null);
        }

        // Map contains (limit, expected count)
        Map<Integer,Integer> limits = new HashMap<Integer,Integer>(8);
        limits.put(null,DEFAULT_LIMIT); // null limit, should return default
        limits.put(DEFAULT_LIMIT - 1,DEFAULT_LIMIT - 1); // limit less than default limit
        limits.put(DEFAULT_LIMIT,DEFAULT_LIMIT); // limit equals default
        limits.put(DEFAULT_LIMIT + 1,DEFAULT_LIMIT + 1); // limit greater than default
        limits.put(LOCATION_COUNT + 1,LOCATION_COUNT); // limit greater than count
        limits.put(0,LOCATION_COUNT); // unlimited, should return all

        for (Integer limit: limits.keySet()) {
            Map<String, String> parameters = new HashMap<String, String>(1);
            if (limit != null) {
                parameters.put("limit", limit.toString());
            }
            String fetchedJson = sendRequest(GET, "/monitoringLocations", parameters, 200);
            Integer count = (new ObjectMapper()).readValue(fetchedJson, Locations.class).count;
            LOG.info("limit="+limit+" expected="+limits.get(limit)+" count="+count);
            if (limit == null) {
                assertEquals(DEFAULT_LIMIT, count);
            } else {
                assertEquals(limits.get(limit),count);
            }
        }
    }

    /**
     * Used for mapper to load Json from rest service
     */
    private static class Location {
        public int priority;
        public ArrayList<Object> tags;
        public Object geolocation;
        public Object latitude;
        public Object longitude;
        @JsonProperty("location-name")
        public String locationName;
        @JsonProperty("monitoring-area")
        public String monitoringArea;
    }

    /**
     * Used for mapper to load Json from rest service
     */
    private static class Locations {
        public int offset;
        public int count;
        public int totalCount;
        public ArrayList<Location> location;
    }
}
