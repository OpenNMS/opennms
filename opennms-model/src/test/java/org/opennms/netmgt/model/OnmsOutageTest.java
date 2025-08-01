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
package org.opennms.netmgt.model;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class OnmsOutageTest {

    @Test
    public void testMarshalXml() {
        final OnmsOutage outage = createOutage();
        final String outageString = XmlTest.marshalToXmlWithJaxb(outage);

        XmlTest.assertXmlEquals("<outage id=\"1\">\n" +
                "   <ipAddress>127.0.0.1</ipAddress>\n" +
                "   <locationName>Default</locationName>\n" +
                "   <monitoredService down=\"false\" id=\"1\">\n" +
                "      <ipInterfaceId>1</ipInterfaceId>\n" +
                "      <serviceType id=\"1\">\n" +
                "         <name>Webservices</name>\n" +
                "      </serviceType>\n" +
                "   </monitoredService>\n" +
                "   <nodeId>1</nodeId>\n" +
                "   <nodeLabel>Dummy Node 1</nodeLabel>\n" +
                "   <perspective>Fulda</perspective>\n" +
                "</outage>", outageString);
    }

    @Test
    public void testMarshalJson() throws IOException {
        final OnmsOutage outage = createOutage();
        final String outageString = JsonTest.marshalToJson(outage);

        JsonTest.assertJsonEquals(
                "{\"id\" : 1,\n" +
                "  \"monitoredService\" : {\n" +
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
                "    \"down\" : false,\n" +
                "    \"ipInterfaceId\" : 1,\n" +
                "    \"id\" : 1\n" +
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
                "  \"serviceId\" : 1,\n" +
                "  \"perspective\" : \"Fulda\"\n" +
                "}\n", outageString);
    }

    private static OnmsOutage createOutage() {
        OnmsMonitoringLocation monitoringLocation = new OnmsMonitoringLocation();
        monitoringLocation.setLocationName("Fulda");

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
        outage.setPerspective(monitoringLocation);
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
