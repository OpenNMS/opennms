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

import java.net.InetAddress;
import java.util.Date;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
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
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AlarmRestServiceIT extends AbstractSpringJerseyRestTestCase {

    public AlarmRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        final OnmsCategory linux = createCategory("Linux");
        final OnmsCategory macOS = createCategory("macOS");

        final OnmsServiceType icmp = new OnmsServiceType("ICMP");
        m_databasePopulator.getServiceTypeDao().save(icmp);
        m_databasePopulator.getServiceTypeDao().flush();

        final NetworkBuilder builder = new NetworkBuilder();

        final OnmsNode node1 = createNode(builder, "server01", "192.168.1.1", linux);
        final OnmsNode node2 = createNode(builder, "server02", "192.168.1.2", macOS);

        createAlarm(node1, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR);
        createAlarm(node1, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING);
        createAlarm(node1, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL);

        createAlarm(node2, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR);
        createAlarm(node2, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING);
        createAlarm(node2, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL);
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testAlarms() throws Exception {
        String url = "/alarms";

        executeQueryAndVerify("limit=0", 6);

        executeQueryAndVerify("limit=0&_s=severity==NORMAL", 2);

        executeQueryAndVerify("limit=0&_s=severity==WARNING", 2);

        sendRequest(GET, url, parseParamData("limit=0&_s=severity==CRITICAL"), 204);

        executeQueryAndVerify("limit=0&_s=severity=gt=NORMAL", 4);

        executeQueryAndVerify("limit=0&_s=severity=gt=NORMAL;node.label==server01", 2);
    }

    @Test
    @Transactional
    public void testCollectionsAndMappings() throws Exception {
        executeQueryAndVerify("_s=categoryName==Linux", 3);
        executeQueryAndVerify("_s=uei==*somethingWentWrong", 2);
        executeQueryAndVerify("_s=uei==*somethingWentWrong;categoryName==Linux", 1);

        executeQueryAndVerify("_s=uei==*something*", 6);
        executeQueryAndVerify("_s=uei!=*somethingIs*", 2);

        // Verify service queries
        executeQueryAndVerify("_s=service==ICMP", 6);
        executeQueryAndVerify("_s=service!=ICMP", 0);
        executeQueryAndVerify("_s=service==SNMP", 0);
        executeQueryAndVerify("_s=service==*MP", 6);

        // Verify ip address queries
        executeQueryAndVerify("_s=ipAddr==192.168.1.1", 3);
        executeQueryAndVerify("_s=ipAddr==192.168.1.2", 3);
        executeQueryAndVerify("_s=ipAddr==127.0.0.1", 0);
        executeQueryAndVerify("_s=ipAddr!=127.0.0.1", 6);
    }

    public static void main(String[] args) {
        InetAddress inetAddress = InetAddressUtils.getInetAddress("127.0.0.*");
        System.out.println(inetAddress);
    }

    private OnmsNode createNode(final NetworkBuilder builder, final String label, final String ipAddress, final OnmsCategory category) {
        builder.addNode(label).setForeignSource("JUnit").setForeignId(label).setType(NodeType.ACTIVE);
        builder.addCategory(category);
        builder.setBuilding("HQ");
        builder.addSnmpInterface(1)
        .setCollectionEnabled(true)
        .setIfOperStatus(1)
        .setIfSpeed(10000000)
        .setIfName("eth0")
        .setIfType(6)
        .setPhysAddr("C9D2DFC7CB68")
        .addIpInterface(ipAddress).setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(m_databasePopulator.getServiceTypeDao().findByName("ICMP"));
        final OnmsNode node = builder.getCurrentNode();
        m_databasePopulator.getNodeDao().save(node);
        return node;
    }

    private OnmsCategory createCategory(final String categoryName) {
        final OnmsCategory cat = new OnmsCategory(categoryName);
        m_databasePopulator.getCategoryDao().save(cat);
        m_databasePopulator.getCategoryDao().flush();
        return cat;
    }

    private void createAlarm(final OnmsNode node, final String eventUei, final OnmsSeverity severity) {
        final OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_databasePopulator.getDistPollerDao().whoami());
        event.setEventCreateTime(new Date());
        event.setEventDisplay("Y");
        event.setEventHost("127.0.0.1");
        event.setEventLog("Y");
        event.setEventSeverity(1);
        event.setEventSource("JUnit");
        event.setEventTime(new Date());
        event.setEventUei(eventUei);
        event.setIpAddr(node.getIpInterfaces().iterator().next().getIpAddress());
        event.setNode(node);
        event.setServiceType(m_databasePopulator.getServiceTypeDao().findByName("ICMP"));
        event.setEventSeverity(severity.getId());
        m_databasePopulator.getEventDao().save(event);
        m_databasePopulator.getEventDao().flush();

        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(m_databasePopulator.getDistPollerDao().whoami());
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(1);
        alarm.setNode(node);
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(node.getIpInterfaces().iterator().next().getIpAddress());
        alarm.setSeverity(severity);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setEventParms(event.getEventParms());
        alarm.setServiceType(m_databasePopulator.getServiceTypeDao().findByName("ICMP"));
        m_databasePopulator.getAlarmDao().save(alarm);
        m_databasePopulator.getAlarmDao().flush();
    }

    private void executeQueryAndVerify(String query, int totalCount) throws Exception {
        if (totalCount == 0) {
            sendRequest(GET, "/alarms", parseParamData(query), 204);
        } else {
            JSONObject object = new JSONObject(sendRequest(GET, "/alarms", parseParamData(query), 200));
            Assert.assertEquals(totalCount, object.getInt("totalCount"));
        }
    }

}
