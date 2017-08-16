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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opennms.web.svclayer.support.DefaultTroubleTicketProxy.createEventBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

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
@JUnitConfigurationEnvironment(systemProperties="org.apache.cxf.Logger=org.apache.cxf.common.logging.Slf4jLogger")
@JUnitTemporaryDatabase
@Transactional
public class AlarmRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmRestServiceIT.class);

    public AlarmRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    private OnmsNode node1;
    private OnmsNode node2;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        final OnmsCategory linux = createCategory("Linux");
        final OnmsCategory servers = createCategory("LinuxServers");
        final OnmsCategory macOS = createCategory("macOS");

        final OnmsServiceType icmp = new OnmsServiceType("ICMP");
        m_databasePopulator.getServiceTypeDao().save(icmp);
        m_databasePopulator.getServiceTypeDao().flush();

        final NetworkBuilder builder = new NetworkBuilder();

        // Add a node with 2 categories, since this will really exercise the Criteria
        // aliases since node-to-category is a many-to-many relationship
        node1 = createNode(builder, "server01", "192.168.1.1", new OnmsCategory[] { linux, servers });
        node2 = createNode(builder, "server02", "192.168.1.2", new OnmsCategory[] { macOS });

        createAlarm(node1, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR);
        createAlarm(node1, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING);
        createAlarm(node1, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL);

        createAlarm(node2, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR);
        createAlarm(node2, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING);
        createAlarm(node2, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL);
        createAlarm(node2, "uei.opennms.org/test/somethingIsStillOk", OnmsSeverity.NORMAL);
    }

    /**
     * General query test.
     * 
     * @throws Exception
     */
    @Test
    public void testAlarms() throws Exception {
        String url = "/alarms";

        executeQueryAndVerify("limit=0", 7);

        executeQueryAndVerify("limit=0&_s=alarm.severity==NORMAL", 3);

        executeQueryAndVerify("limit=0&_s=alarm.severity==WARNING", 2);

        sendRequest(GET, url, parseParamData("limit=0&_s=alarm.severity==CRITICAL"), 204);

        executeQueryAndVerify("limit=0&_s=alarm.severity=gt=NORMAL", 4);

        executeQueryAndVerify("limit=0&_s=alarm.severity=gt=NORMAL;node.label==server01", 2);
    }

    /**
     * Test filtering for properties of {@link OnmsCategory}. The implementation
     * for this filtering is different because the node-to-category relationship
     * is a many-to-many relationship.
     * 
     * @throws Exception
     */
    @Test
    public void testCategoryFiltering() throws Exception {
        int categoryId;
        categoryId = m_databasePopulator.getCategoryDao().findByName("Linux").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 3);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 4);

        categoryId = m_databasePopulator.getCategoryDao().findByName("LinuxServers").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 3);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 4);

        categoryId = m_databasePopulator.getCategoryDao().findByName("macOS").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 4);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 3);

        // Category that doesn't exist
        categoryId = Integer.MAX_VALUE;
        executeQueryAndVerify("_s=category.id==" + categoryId, 0);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 7);

        executeQueryAndVerify("_s=category.name==Linux", 3);
        executeQueryAndVerify("_s=category.name!=Linux", 4);
        executeQueryAndVerify("_s=category.name==LinuxServers", 3);
        executeQueryAndVerify("_s=category.name!=LinuxServers", 4);
        executeQueryAndVerify("_s=category.name==Linux*", 3);
        executeQueryAndVerify("_s=category.name!=Linux*", 4);
        executeQueryAndVerify("_s=category.name==macOS", 4);
        executeQueryAndVerify("_s=category.name!=macOS", 3);
        executeQueryAndVerify("_s=category.name==mac*", 4);
        executeQueryAndVerify("_s=category.name!=mac*", 3);
        executeQueryAndVerify("_s=category.name==ma*S", 4);
        executeQueryAndVerify("_s=category.name!=ma*S", 3);
        executeQueryAndVerify("_s=category.name==DoesntExist", 0);
        executeQueryAndVerify("_s=category.name!=DoesntExist", 7);
    }

    @Test
    public void testRootAliasFiltering() throws Exception {
        executeQueryAndVerify("_s=uei==*somethingWentWrong", 2);
        executeQueryAndVerify("_s=uei==*somethingWentWrong;category.name==Linux", 1);

        executeQueryAndVerify("_s=uei==*something*", 7);
        executeQueryAndVerify("_s=uei==*somethingIs*", 5);
        executeQueryAndVerify("_s=uei!=*somethingIs*", 2);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=ipAddr==192.168.1.1", 3);
        executeQueryAndVerify("_s=ipAddr==192.168.1.2", 4);
        executeQueryAndVerify("_s=ipAddr==192.*.*.2", 4);
        executeQueryAndVerify("_s=ipAddr==192.168.1.1-2", 7);
        executeQueryAndVerify("_s=ipAddr==127.0.0.1", 0);
        executeQueryAndVerify("_s=ipAddr!=127.0.0.1", 7);
    }

    /**
     * Test filtering for properties of {@link OnmsAlarm}.
     * 
     * @throws Exception
     */
    @Test
    public void testAlarmFiltering() throws Exception {
        executeQueryAndVerify("_s=alarm.uei==*somethingWentWrong", 2);
        executeQueryAndVerify("_s=alarm.uei==*somethingWentWrong;category.name==Linux", 1);

        executeQueryAndVerify("_s=alarm.uei==*something*", 7);
        executeQueryAndVerify("_s=alarm.uei==*somethingIs*", 5);
        executeQueryAndVerify("_s=alarm.uei!=*somethingIs*", 2);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=alarm.ipAddr==192.168.1.1", 3);
        executeQueryAndVerify("_s=alarm.ipAddr==192.168.1.2", 4);
        executeQueryAndVerify("_s=alarm.ipAddr==192.*.*.2", 4);
        executeQueryAndVerify("_s=alarm.ipAddr==192.168.1.1-2", 7);
        executeQueryAndVerify("_s=alarm.ipAddr==127.0.0.1", 0);
        executeQueryAndVerify("_s=alarm.ipAddr!=127.0.0.1", 7);
    }

    /**
     * Test filtering for properties of {@link OnmsServiceType}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceFiltering() throws Exception {
        // Verify service queries
        executeQueryAndVerify("_s=serviceType.name==ICMP", 7);
        executeQueryAndVerify("_s=serviceType.name!=ICMP", 0);
        executeQueryAndVerify("_s=serviceType.name==SNMP", 0);
        executeQueryAndVerify("_s=serviceType.name==*MP", 7);
    }

    /**
     * Test filtering for properties of {@link OnmsIpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testIpInterfaceFiltering() throws Exception {
        executeQueryAndVerify("_s=ipInterface.ipAddress==192.168.1.1", 3);
        executeQueryAndVerify("_s=ipInterface.ipAddress==192.168.1.2", 4);
        executeQueryAndVerify("_s=ipInterface.ipAddress==127.0.0.1", 0);
        executeQueryAndVerify("_s=ipInterface.ipAddress!=127.0.0.1", 7);
    }

    /**
     * Test filtering for properties of {@link OnmsNode}.
     * 
     * @throws Exception
     */
    @Test
    public void testNodeFiltering() throws Exception {
        executeQueryAndVerify("_s=node.id==" + node1.getId(), 3);
        executeQueryAndVerify("_s=node.id==" + node2.getId(), 4);

        executeQueryAndVerify("_s=node.label==server01", 3);
        executeQueryAndVerify("_s=node.label!=server01", 4);
        executeQueryAndVerify("_s=node.label==server02", 4);
        executeQueryAndVerify("_s=node.label!=server02", 3);
        executeQueryAndVerify("_s=(node.label==server01,node.label==server02)", 7);
        executeQueryAndVerify("_s=node.label!=\u0000", 7);
    }

    /**
     * Test filtering for properties of {@link OnmsAssetRecord}.
     * 
     * @throws Exception
     */
    @Test
    public void testAssetFiltering() throws Exception {
        executeQueryAndVerify("_s=assetRecord.description==lolol", 0);
    }

    /**
     * Test filtering for properties of {@link OnmsSnmpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testSnmpFiltering() throws Exception {
        executeQueryAndVerify("_s=snmpInterface.netMask==255.255.255.0", 0);
        executeQueryAndVerify("_s=snmpInterface.netMask==\u0000", 7);
        executeQueryAndVerify("_s=snmpInterface.netMask!=255.255.255.0", 7);
        executeQueryAndVerify("_s=snmpInterface.netMask==255.255.127.0", 0);
    }

    /**
     * Test filtering for properties of {@link OnmsMonitoringLocation}.
     * 
     * @throws Exception
     */
    @Test
    public void testLocationFiltering() throws Exception {
        executeQueryAndVerify("_s=location.locationName==Default", 7);
        executeQueryAndVerify("_s=location.locationName!=Default", 0);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsAlarm} without the alias.
     * 
     * @throws Exception
     */
    @Test
    public void testRootAliasOrderBy() throws Exception {
        String url = "/alarms";

        // Test orderby for properties of OnmsAlarm
        sendRequest(GET, url, parseParamData("orderBy=alarmAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarmAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=alarmType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=applicationDN"), 200);
        sendRequest(GET, url, parseParamData("orderBy=clearKey"), 200);
        sendRequest(GET, url, parseParamData("orderBy=counter"), 200);
        sendRequest(GET, url, parseParamData("orderBy=description"), 200);
        sendRequest(GET, url, parseParamData("orderBy=details"), 200);
        sendRequest(GET, url, parseParamData("orderBy=distPoller"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=eventParameters"), 200);
        sendRequest(GET, url, parseParamData("orderBy=eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=firstAutomationTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=firstEventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipAddr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastAutomationTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=logMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=managedObjectInstance"), 200);
        sendRequest(GET, url, parseParamData("orderBy=managedObjectType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=mouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node"), 200);
        sendRequest(GET, url, parseParamData("orderBy=operInstruct"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ossPrimaryKey"), 200);
        sendRequest(GET, url, parseParamData("orderBy=qosAlarmState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=reductionKey"), 200);
        sendRequest(GET, url, parseParamData("orderBy=reductionKeyMemo"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=severity"), 200);
        // TODO: Figure out how to do this, OnmsSeverity is an enum
        //sendRequest(GET, url, parseParamData("orderBy=severity.id"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=severity.label"), 200);
        sendRequest(GET, url, parseParamData("orderBy=stickyMemo"), 200);
        sendRequest(GET, url, parseParamData("orderBy=suppressedTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=suppressedUntil"), 200);
        sendRequest(GET, url, parseParamData("orderBy=suppressedUser"), 200);
        // TODO: I can't figure out the bean property name for these properties
        //sendRequest(GET, url, parseParamData("orderBy=tticketId"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=tticketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=uei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=x733AlarmType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=x733ProbableCause"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsAlarm}.
     * 
     * @throws Exception
     */
    @Test
    public void testAlarmAliasOrderBy() throws Exception {
        String url = "/alarms";
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
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsAssetRecord}.
     * 
     * @throws Exception
     */
    @Test
    public void testAssetAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.additionalhardware"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.address1"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.address2"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.admin"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.assetNumber"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.autoenable"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.building"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.category"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.circuitId"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.city"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.comment"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.connection"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.country"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.cpu"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.dateInstalled"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.department"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.description"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.displayCategory"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.division"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.enable"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.floor"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.geolocation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.hdd1"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.hdd2"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.hdd3"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.hdd4"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.hdd5"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.hdd6"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.inputpower"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.lastModifiedBy"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.lastModifiedDate"), 200); 
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.latitude"), 200); 
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.lease"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.leaseExpires"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.longitude"), 200); 
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.maintcontract"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.maintContractExpiration"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.managedObjectInstance"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.managedObjectType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.manufacturer"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.modelNumber"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.notifyCategory"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.numpowersupplies"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.operatingSystem"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.password"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.pollerCategory"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.port"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.rack"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.rackunitheight"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.ram"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.region"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.room"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.serialNumber"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.slot"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.snmpcommunity"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.state"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.storagectrl"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.supportPhone"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.thresholdCategory"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.username"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vendor"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vendorAssetNumber"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vendorFax"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vendorPhone"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vmwareManagedEntityType"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vmwareManagedObjectId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vmwareManagementServer"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vmwareState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=assetRecord.vmwareTopologyInfo"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=assetRecord.zip"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsDistPoller}.
     * 
     * @throws Exception
     */
    @Test
    public void testDistPollerAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=distPoller.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=distPoller.label"), 200);
        sendRequest(GET, url, parseParamData("orderBy=distPoller.location"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsEvent}.
     * 
     * @throws Exception
     */
    @Test
    public void testLastEventAliasOrderBy() throws Exception {
        String url = "/alarms";

        // Test orderby for properties of OnmsEvent
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventAutoAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventCorrelation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventCreateTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventDescr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventDisplay"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventForward"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventLog"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventLogGroup"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventLogMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventMouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventNotification"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventOperAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventOperActionMenuText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventOperInstruct"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventParameters"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventPathOutage"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventSeverity"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventSnmp"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventSnmpHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventSuppressedCount"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventTTicket"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventTTicketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.eventUei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=lastEvent.ipAddr"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsIpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testIpInterfaceAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=ipInterface.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipInterface.ipAddress"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipInterface.ipHostName"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipInterface.ipLastCapsdPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipInterface.isManaged"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsMonitoringLocation}.
     * 
     * @throws Exception
     */
    @Test
    public void testLocationAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=location.locationName"), 200);
        sendRequest(GET, url, parseParamData("orderBy=location.geolocation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=location.latitude"), 200);
        sendRequest(GET, url, parseParamData("orderBy=location.longitude"), 200);
        sendRequest(GET, url, parseParamData("orderBy=location.monitoringArea"), 200);
        sendRequest(GET, url, parseParamData("orderBy=location.priority"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsNode}.
     * 
     * @throws Exception
     */
    @Test
    public void testNodeAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=node.createTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.foreignId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.foreignSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.label"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.labelSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.lastCapsdPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.netBiosDomain"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.netBiosName"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.operatingSystem"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.parent"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.pathElement"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysContact"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysDescription"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysLocation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysName"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.sysObjectId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=node.type"), 200);
    }

    /**
     * Test searching and ordering by every {@link SearchProperty} listed in
     * {@link SearchProperties#ALARM_SERVICE_PROPERTIES}.
     * 
     * @throws Exception
     */
    @Test
    public void testAllSearchParameters() throws Exception {
        String url = "/alarms";
        for (SearchProperty prop : SearchProperties.ALARM_SERVICE_PROPERTIES) {
            System.err.println("Testing " + prop.getId());
            switch(prop.type) {
            case FLOAT:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==1.0;%s!=1.0", prop.getId(), prop.getId())), 204);
                break;
            case INTEGER:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==1;%s!=1", prop.getId(), prop.getId())), 204);
                break;
            case IP_ADDRESS:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==127.0.0.1;%s!=127.0.0.1", prop.getId(), prop.getId())), 204);
                break;
            case LONG:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==1;%s!=1", prop.getId(), prop.getId())), 204);
                break;
            case STRING:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==A;%s!=A", prop.getId(), prop.getId())), 204);
                break;
            case TIMESTAMP:
                sendRequest(GET, url, parseParamData(String.format(
                    "_s=%s==%s;%s!=%s", 
                    prop.getId(), 
                    CriteriaBehaviors.SEARCH_DATE_FORMAT.get().format(new Date(0)),
                    prop.getId(), 
                    CriteriaBehaviors.SEARCH_DATE_FORMAT.get().format(new Date(0))
                )), 204);
                break;
            default:
                throw new IllegalArgumentException();
            }
            if (prop.orderBy) {
                sendRequest(GET, url, parseParamData("orderBy=" + prop.getId()), 200);
            }
        }
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsServiceType}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceTypeAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=serviceType.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceType.name"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsSnmpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testSnmpInterfaceAliasOrderBy() throws Exception {
        String url = "/alarms";
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifAdminStatus"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifOperStatus"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifSpeed"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.lastCapsdPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.lastSnmpPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.netMask"), 200);
    }

    @Test
    public void verifyTicketerMustBeEnabled() throws Exception {
        final OnmsAlarm alarm = m_databasePopulator.getAlarmDao().findAll().stream()
                .filter(a -> a.getSeverity().isGreaterThanOrEqual(OnmsSeverity.NORMAL) && a.getAlarmAckTime() == null)
                .findFirst().orElseThrow(() -> new IllegalStateException("No unacknowledged alarm with severity >= Normal found"));
        String url = "/alarms/";

        // TroubleTicketerPlugin is disabled, therefore it should fail
        sendPost(url + alarm.getId() + "/ticket/create", "", 501);
        sendPost(url + alarm.getId() + "/ticket/update", "", 501);
        sendPost(url + alarm.getId() + "/ticket/close", "", 501);

        // enable TroubleTicketeRPlugin and try again
        System.setProperty("opennms.alarmTroubleTicketEnabled", "true");
        verifyAnticipatedEvents();

        anticipateEvent(createEventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, alarm, ImmutableMap.of("user", "ulf")));
        sendPost(url + alarm.getId() + "/ticket/create", "", 202);
        verifyAnticipatedEvents();

        anticipateEvent(createEventBuilder(EventConstants.TROUBLETICKET_UPDATE_UEI, alarm, null));
        sendPost(url + alarm.getId() + "/ticket/update", "", 202);
        verifyAnticipatedEvents();

        anticipateEvent(createEventBuilder(EventConstants.TROUBLETICKET_CLOSE_UEI, alarm, null));
        sendPost(url + alarm.getId() + "/ticket/close", "", 202);
        verifyAnticipatedEvents();
    }

    /**
     * @see https://issues.opennms.org/browse/NMS-9590
     * 
     * @throws Exception 
     */
    @Test
    public void testMultithreadedAccess() throws Exception {
        final AtomicInteger successes = new AtomicInteger();
        final AtomicInteger failures = new AtomicInteger();
        final int n = 40;

        final Executor pool = Executors.newFixedThreadPool(5);
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            CompletableFuture<?> future = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        LOG.debug("HELLO");
                        executeQueryAndVerify("_s=alarmAckTime%3D%3D1970-01-01T00:00:00.000-0000;alarm.severity%3Dge%3DNORMAL;(node.label%3D%3D*sp01*;node.label%3D%3D*sp02*);(node.label%3D%3D*.asp*,node.label%3D%3D*sbx*);(lastEventTime%3Dge%3D2017-08-15T15:33:53.610-0000;lastEventTime%3Dle%3D2017-08-22T15:33:53.610-0000)", 0);
                        successes.incrementAndGet();
                    } catch (Throwable e) {
                        LOG.error("Unexpected exception during executeQueryAndVerify: " + e.getMessage(), e);
                        failures.incrementAndGet();
                        fail(e.getMessage());
                    }
                }
            }, pool)
            .exceptionally(e -> {
                LOG.error("Unexpected exception in thread: " + e.getMessage(), e);
                fail();
                return null;
            });
            futures.add(future);
        }

        // Wait for all of the tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        assertEquals(0, failures.get());
        assertEquals(n, successes.get());
    }

    private void anticipateEvent(EventBuilder eventBuilder) {
        m_eventMgr.getEventAnticipator().anticipateEvent(eventBuilder.getEvent());
    }

    private void verifyAnticipatedEvents() {
        m_eventMgr.getEventAnticipator().verifyAnticipated(10000, 0, 0, 0, 0);
    }

    private OnmsNode createNode(final NetworkBuilder builder, final String label, final String ipAddress, final OnmsCategory[] categories) {
        builder.addNode(label).setForeignSource("JUnit").setForeignId(label).setType(NodeType.ACTIVE);
        for (OnmsCategory category : categories) {
            builder.addCategory(category);
        }
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
        event.setEventSeverity(OnmsSeverity.INDETERMINATE.getId());
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
        alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
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
