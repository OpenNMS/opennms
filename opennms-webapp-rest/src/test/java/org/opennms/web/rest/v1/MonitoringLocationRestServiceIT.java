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

import javax.ws.rs.core.MediaType;

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
public class MonitoringLocationRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(org.opennms.web.rest.v2.MonitoringLocationRestServiceIT.class);

    @Autowired
    private MockEventIpcManager eventIpcManager;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
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
        sendPut("/monitoringLocations/location1", "monitoringArea=monitoringarea1-modified", 204);
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

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
}
