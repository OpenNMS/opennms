/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import java.io.IOException;
import java.util.Date;

public class OnmsOutageTest {

    @Test
    public void testMarshalXml() {
        final OnmsOutage outage = createOutage();
        final String outageString = XmlTest.marshalToXmlWithJaxb(outage);

        XmlTest.assertXmlEquals("<outage id=\"1\">\n" +
                "   <ipAddress>127.0.0.1</ipAddress>\n" +
                "   <locationName>Default</locationName>\n" +
                "   <monitoredService down=\"false\" id=\"1\">\n" +
                "      <applications>\n" +
                "         <application id=\"100\">\n" +
                "           <monitoredServices>\n" +
                "               <monitoredServiceId>1</monitoredServiceId>\n" +
                "               <monitoredServiceId>2</monitoredServiceId>\n" +
                "           </monitoredServices>\n" +
                "           <name>Dummy</name>\n" +
                "         </application>\n" +
                "         <application id=\"102\">\n" +
                "           <name>Another Dummy</name>\n" +
                "         </application>\n" +
                "      </applications>\n" +
                "      <ipInterfaceId>1</ipInterfaceId>\n" +
                "      <serviceType id=\"1\">\n" +
                "         <name>Webservices</name>\n" +
                "      </serviceType>\n" +
                "   </monitoredService>\n" +
                "   <nodeId>1</nodeId>\n" +
                "   <nodeLabel>Dummy Node 1</nodeLabel>\n" +
                "</outage>", outageString);
    }

    @Test
    public void testMarshalJson() throws IOException {
        final OnmsOutage outage = createOutage();
        final String outageString = JsonTest.marshalToJson(outage);

        JsonTest.assertJsonEquals(
                "{\"id\" : 1,\n" +
                "  \"monitoredService\" : {\n" +
                "    \"applications\" : [ {\n" +
                "      \"name\" : \"Dummy\",\n" +
                "      \"id\" : 100\n" +
                "    }, {\n" +
                "      \"name\" : \"Another Dummy\",\n" +
                "      \"id\" : 102\n" +
                "    } ],\n" +
                "    \"serviceType\" : {\n" +
                "      \"name\" : \"Webservices\",\n" +
                "      \"id\" : 1\n" +
                "    },\n" +
                "    \"lastGood\" : null,\n" +
                "    \"lastFail\" : null,\n" +
                "    \"qualifier\" : null,\n" +
                "    \"status\" : null,\n" +
                "    \"statusLong\" : null,\n" +
                "    \"source\" : null,\n" +
                "    \"notify\" : null,\n" +
                "    \"down\" : false\n" +
                "  },\n" +
                "  \"ifLostService\" : null,\n" +
                "  \"serviceLostEvent\" : null,\n" +
                "  \"ifRegainedService\" : null,\n" +
                "  \"serviceRegainedEvent\" : null,\n" +
                "  \"suppressTime\" : null,\n" +
                "  \"suppressedBy\" : null,\n" +
                "  \"locationName\" : \"Default\",\n" +
                "  \"nodeId\" : 1,\n" +
                "  \"nodeLabel\" : \"Dummy Node 1\",\n" +
                "  \"ipAddress\" : \"127.0.0.1\",\n" +
                "  \"serviceId\" : 1\n" +
                "}\n", outageString);
    }

    private static OnmsOutage createOutage() {
        OnmsNode node1 = createNode(1, "Dummy Node 1");

        OnmsSnmpInterface snmpInterface1 = createSnmpInterface(10, 1000);
        OnmsSnmpInterface snmpInterface2 = createSnmpInterface(20, 1000);

        OnmsIpInterface interface1 = createIpInterface(1, "127.0.0.1", "localhost", node1, snmpInterface1);
        OnmsIpInterface interface2 = createIpInterface(2, "192.168.2.11", "dummy.opennms.org", node1, snmpInterface2);
        node1.getIpInterfaces().add(interface1);
        node1.getIpInterfaces().add(interface2);

        OnmsMonitoredService service1 = createService(1, createType(1, "Webservices"), interface1);
        OnmsMonitoredService service2 = createService(2, createType(1, "Webservices"), interface1);

        interface1.getMonitoredServices().add(service1);

        OnmsApplication application = new OnmsApplication();
        application.setId(100);
        application.setName("Dummy");

        OnmsApplication application2 = new OnmsApplication();
        application2.setId(102);
        application2.setName("Another Dummy");

        service1.getApplications().add(application);
        service2.getApplications().add(application);
        service1.getApplications().add(application2);

        application.getMonitoredServices().add(service1);
        application.getMonitoredServices().add(service2);

        OnmsOutage outage = new OnmsOutage();
        outage.setId(1);
        outage.setMonitoredService(service1);
        return outage;
    }


    private static OnmsNode createNode(int id, String label) {
        OnmsNode node = new OnmsNode();
        node.setId(id);
        node.setLabel(label);
        node.setCreateTime(new Date());
        OnmsMonitoringLocation location = new OnmsMonitoringLocation("Default", "Default");
        node.setLocation(location);
        return node;
    }

    private static OnmsSnmpInterface createSnmpInterface(int id, int ifIndex) {
        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface();
        snmpInterface.setId(id);
        snmpInterface.setIfIndex(ifIndex);
        return snmpInterface;
    }

    private static OnmsIpInterface createIpInterface(int id, String ipaddress, String host, OnmsNode node, OnmsSnmpInterface onmsSnmpInterface) {
        OnmsIpInterface onmsIpInterface = new OnmsIpInterface();
        onmsIpInterface.setId(id);
        onmsIpInterface.setSnmpInterface(onmsSnmpInterface);
        onmsIpInterface.setIfIndex(onmsIpInterface.getIfIndex());
        onmsIpInterface.setIpAddress(InetAddressUtils.addr(ipaddress));
        onmsIpInterface.setIpHostName(host);
        onmsIpInterface.setIpLastCapsdPoll(new Date());
        onmsIpInterface.setIsManaged("M");
        onmsIpInterface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        onmsIpInterface.setNode(node);

        onmsSnmpInterface.getIpInterfaces().add(onmsIpInterface);
        return onmsIpInterface;
    }

    private static OnmsMonitoredService createService(int serviceId, OnmsServiceType serviceType, OnmsIpInterface ipInterface) {
        OnmsMonitoredService monitoredService = new OnmsMonitoredService();
        monitoredService.setId(serviceId);
        monitoredService.setServiceType(serviceType);
        monitoredService.setIpInterface(ipInterface);
        return monitoredService;
    }

    private static OnmsServiceType createType(int id, String name) {
        OnmsServiceType type = new OnmsServiceType();
        type.setId(id);
        type.setName(name);
        return type;
    }
}
