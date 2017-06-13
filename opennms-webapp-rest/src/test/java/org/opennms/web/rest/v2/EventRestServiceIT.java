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

import java.util.Date;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsNode.NodeType;
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
public class EventRestServiceIT extends AbstractSpringJerseyRestTestCase {

    public EventRestServiceIT() {
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

        createEvent(node1, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR);
        createEvent(node1, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING);
        createEvent(node1, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL);

        createEvent(node2, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR);
        createEvent(node2, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING);
        createEvent(node2, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL);
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testEvents() throws Exception {
        String url = "/events";

        JSONObject object = new JSONObject(sendRequest(GET, url, 200));
        Assert.assertEquals(6, object.getInt("totalCount"));

        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=nodeLabel==server01"), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));

        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=uei==*somethingWentWrong"), 200));
        Assert.assertEquals(2, object.getInt("totalCount"));

        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=categoryName==Linux"), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));

        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=categoryName==Linux;uei==*somethingWentWrong"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testAddEvent() throws Exception {
        String url = "/events";
        String event = "<event><uei>uei.opennms.org/testEvent</uei></event>";
        sendPost(url, event, 204);
    }

    @Test
    public void testOrderBy() throws Exception {
        String url = "/events";

        // Test orderby for properties of OnmsEvent
        sendRequest(GET, url, parseParamData("orderBy=alarm"), 200);
        sendRequest(GET, url, parseParamData("orderBy=associatedServiceLostOutages"), 200);
        sendRequest(GET, url, parseParamData("orderBy=associatedServiceRegainedOutages"), 200);
        sendRequest(GET, url, parseParamData("orderBy=distPoller"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventAutoAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventCorrelation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventCreateTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventDescr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventDisplay"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventForward"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventLog"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventLogGroup"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventLogMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventMouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventNotification"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventOperAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventOperActionMenuText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventOperInstruct"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=eventParameters"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventPathOutage"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventSeverity"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventSnmp"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventSnmpHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventSuppressedCount"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventTTicket"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventTTicketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventUei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipAddr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notifications"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceType"), 200);

        // Test orderby for properties of OnmsAlarm
        sendRequest(GET, url, parseParamData("orderBy=alarm.alarmAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.alarmAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.alarmType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.applicationDN"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.clearKey"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.counter"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.description"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.details"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.distPoller"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=alarm.eventParameters"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.firstAutomationTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.firstEventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.ipAddr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.lastAutomationTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.lastEvent"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.lastEventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.logMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.managedObjectInstance"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.managedObjectType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.mouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.node"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.operInstruct"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.ossPrimaryKey"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.qosAlarmState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.reductionKey"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.reductionKeyMemo"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.serviceType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.severity"), 200);
        // TODO: Figure out how to do this, OnmsSeverity is an enum
        //sendRequest(GET, url, parseParamData("orderBy=alarm.severity.id"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=alarm.severity.label"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.stickyMemo"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.suppressedTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.suppressedUntil"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.suppressedUser"), 200);
        // TODO: I can't figure out the bean property name for these properties
        //sendRequest(GET, url, parseParamData("orderBy=alarm.tticketId"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=alarm.tticketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.uei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.x733AlarmType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarm.x733ProbableCause"), 200);

        // Test orderby for properties of OnmsNode
        sendRequest(GET, url, parseParamData("orderBy=node.assetRecord"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.categories"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.cdpElement"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.createTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.foreignId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.foreignSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.isisElement"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.label"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.labelSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.lastCapsdPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.lldpElement"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.lldpLinks"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.location"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.netBiosDomain"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.netBiosName"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.operatingSystem"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.ospfElement"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.parent"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.pathElement"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.snmpInterfaces"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysContact"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysDescription"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysLocation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysName"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysObjectId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.type"), 200);
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

    private void createEvent(final OnmsNode node, final String eventUei, final OnmsSeverity severity) {
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
    }
}
