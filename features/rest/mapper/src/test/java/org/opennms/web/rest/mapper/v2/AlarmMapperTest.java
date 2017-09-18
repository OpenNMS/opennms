/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.mapper.v2;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.model.v2.AlarmDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-rest-mappers.xml"
})
@JUnitConfigurationEnvironment
public class AlarmMapperTest {

    @Autowired
    private EventConfDao eventConfDao;

    @Autowired
    private AlarmMapper alarmMapper;

    @Before
    public void setUp() {
        alarmMapper.setTicketUrlTemplate("https://issues.opennms.org/browse/${id}");
    }

    @Test
    public void canMapAlarm() throws IOException {
        Event eventConf = new Event();
        eventConf.setUei("uei.opennms.org/nodes/interfaceDown");
        eventConf.setEventLabel("OpenNMS-defined node event: interfaceDown");
        eventConfDao.addEvent(eventConf);

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(34);
        alarm.setUei("uei.opennms.org/nodes/interfaceDown");

        OnmsMonitoringSystem monitoringSystem = new OnmsMonitoringSystem();
        monitoringSystem.setLocation("Default");
        alarm.setDistPoller(monitoringSystem);

        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("n1");
        alarm.setNode(node);

        alarm.setIpAddr(InetAddress.getByName("10.8.0.30"));
        alarm.setReductionKey("uei.opennms.org/nodes/interfaceDown::1:10.8.0.30");
        alarm.setAlarmType(1);
        alarm.setCounter(1);
        alarm.setSeverity(OnmsSeverity.MINOR);
        alarm.setFirstEventTime(new Date(1503412443118L));
        alarm.setDescription("All services are down on interface 10.8.0.30.");
        alarm.setLogMsg("Interface 10.8.0.30 is down.");
        alarm.setSuppressedUntil(new Date(1503412443118L));
        alarm.setSuppressedTime(new Date(1503412443118L));
        alarm.setLastEventTime(new Date(1503412443118L));
        alarm.setX733ProbableCause(0);

        OnmsServiceType serviceType = new OnmsServiceType();
        serviceType.setName("ICMP");
        serviceType.setId(3);
        alarm.setServiceType(serviceType);

        OnmsEvent event = new OnmsEvent();
        event.setId(2035);
        event.setEventUei("uei.opennms.org/nodes/interfaceDown");
        event.setEventTime(new Date(1503412443118L));
        event.setEventHost("noise");
        event.setEventSource("OpenNMS.Poller.DefaultPollContext");
        event.setIpAddr(InetAddress.getByName("10.8.0.30"));
        event.setEventCreateTime(new Date(1503412443118L));
        event.setEventDescr("All services are down on interface 10.8.0.30.");
        event.setEventLogMsg("Interface 10.8.0.30 is down.");
        event.setEventSeverity(OnmsSeverity.MINOR.getId());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setNode(node);
        event.setDistPoller(monitoringSystem);
        event.setEventParameters(Lists.newArrayList(new OnmsEventParameter(event, "test", "testVal", "string")));
        event.setServiceType(serviceType);
        alarm.setLastEvent(event);

        alarm.setTTicketId("NMS-9587");
        alarm.setTTicketState(TroubleTicketState.OPEN);

        AlarmDTO alarmDTO = alarmMapper.alarmToAlarmDTO(alarm);
        mapAndMarshalToFromXmlAndJson(alarmDTO,
                "alarm.34.dto.xml",
                "alarm.34.dto.json");
    }

    public void mapAndMarshalToFromXmlAndJson(Object object, String xmlResourceUrl, String jsonResourceUrl) {
        // Verify XML
        try {
            final URL xmlResource = Resources.getResource(xmlResourceUrl);
            final String expectedXmlTemplate = Resources.toString(xmlResource, StandardCharsets.UTF_8);
            final String expectedXml = expectedXmlTemplate.replaceAll("##DATE##", StringUtils.iso8601LocalOffsetString(new Date(1503412443118L)));
            final String jaxbXml = XmlTest.marshalToXmlWithJaxb(object);
            XmlTest.assertXmlEquals(expectedXml, jaxbXml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Verify JSON
        try {
            final URL jsonResource = Resources.getResource(jsonResourceUrl);
            final String expectedJson = Resources.toString(jsonResource, StandardCharsets.UTF_8);
            final String jacksonJson = JsonTest.marshalToJson(object);
            JsonTest.assertJsonEquals(expectedJson, jacksonJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
