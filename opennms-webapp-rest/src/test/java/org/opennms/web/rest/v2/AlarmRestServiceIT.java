/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opennms.web.svclayer.support.DefaultTroubleTicketProxy.createEventBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
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
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
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
    private static final String SERVER3_NAME = "w\u00EAird%20server+name";
    private static final AtomicInteger ALARM_COUNTER = new AtomicInteger();

    public AlarmRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    private OnmsNode node1;
    private OnmsNode node2;
    private OnmsNode node3;

    @Override
    protected void afterServletStart() throws Exception {
        ALARM_COUNTER.set(0);

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

        // simpler node to have multi-node matching
        node2 = createNode(builder, "server02", "192.168.1.2", new OnmsCategory[] { macOS });

        // node with strange values to test double-decoding of the FIQL engine
        node3 = createNode(builder, SERVER3_NAME, "192.168.1.3", new OnmsCategory[] {});

        createAlarm(node1, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR, 0);
        createAlarm(node1, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING, 1);
        createAlarm(node1, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL, 2);

        createAlarm(node2, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR, 10);
        createAlarm(node2, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING, 11);
        createAlarm(node2, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL, 12);
        createAlarm(node2, "uei.opennms.org/test/somethingIsStillOk", OnmsSeverity.NORMAL, 13);

        createAlarm(node3, "uei.opennms.org/test/somethingIsStillOk", OnmsSeverity.NORMAL, 20);
    }

    /**
     * General query test.
     * 
     * @throws Exception
     */
    @Test
    public void testAlarms() throws Exception {
        String url = "/alarms";

        executeQueryAndVerify("limit=0", 8);

        executeQueryAndVerify("limit=0&_s=alarm.severity==NORMAL", 4);

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
        executeQueryAndVerify("_s=category.id!=" + categoryId, 5);

        categoryId = m_databasePopulator.getCategoryDao().findByName("LinuxServers").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 3);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 5);

        categoryId = m_databasePopulator.getCategoryDao().findByName("macOS").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 4);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 4);

        // Category that doesn't exist
        categoryId = Integer.MAX_VALUE;
        executeQueryAndVerify("_s=category.id==" + categoryId, 0);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 8);

        executeQueryAndVerify("_s=category.name==Linux", 3);
        executeQueryAndVerify("_s=category.name!=Linux", 5);
        executeQueryAndVerify("_s=category.name==LinuxServers", 3);
        executeQueryAndVerify("_s=category.name!=LinuxServers", 5);
        executeQueryAndVerify("_s=category.name==Linux*", 3);
        executeQueryAndVerify("_s=category.name!=Linux*", 5);
        executeQueryAndVerify("_s=category.name==macOS", 4);
        executeQueryAndVerify("_s=category.name!=macOS", 4);
        executeQueryAndVerify("_s=category.name==mac*", 4);
        executeQueryAndVerify("_s=category.name!=mac*", 4);
        executeQueryAndVerify("_s=category.name==ma*S", 4);
        executeQueryAndVerify("_s=category.name!=ma*S", 4);
        executeQueryAndVerify("_s=category.name==DoesntExist", 0);
        executeQueryAndVerify("_s=category.name!=DoesntExist", 8);
    }

    /**
     * Test filtering for properties of {@link OnmsEventParameter}. The implementation
     * for this filtering is different because the event-to-parameter relationship
     * is a one-to-many relationship.
     * 
     * @throws Exception
     */
    @Test
    public void testEventParameterFiltering() throws Exception {
        executeQueryAndVerify("_s=eventParameter.name==testParm1", 4);
        executeQueryAndVerify("_s=eventParameter.name!=testParm1", 4);

        executeQueryAndVerify("_s=eventParameter.name==testParm2", 4);
        executeQueryAndVerify("_s=eventParameter.name!=testParm2", 4);

        executeQueryAndVerify("_s=eventParameter.name==doesntExist", 0);
        executeQueryAndVerify("_s=eventParameter.name!=doesntExist", 8);

        executeQueryAndVerify("_s=eventParameter.value==This is an awesome parm%21", 4);
        executeQueryAndVerify("_s=eventParameter.value!=This is an awesome parm%21", 4);
        executeQueryAndVerify("_s=eventParameter.value!=This is an awesome parm%21;eventParameter.value==This is a weird parm", 4);

        executeQueryAndVerify("_s=eventParameter.value==This is a weird parm", 4);
        executeQueryAndVerify("_s=eventParameter.value!=This is a weird parm", 4);
        executeQueryAndVerify("_s=eventParameter.value!=This is a weird parm;eventParameter.value==This is an awesome parm%21", 4);

        executeQueryAndVerify("_s=eventParameter.value!=This is an awesome parm%21;eventParameter.value!=This is a weird parm", 0);

        executeQueryAndVerify("_s=eventParameter.value==doesntExist", 0);
        executeQueryAndVerify("_s=eventParameter.value!=doesntExist", 8);

        // This doesn't work because eventParameter.type is a non-unique field
        //executeQueryAndVerify("_s=eventParameter.type==string", 8);
        executeQueryAndVerify("_s=eventParameter.type!=string", 0);

        executeQueryAndVerify("_s=eventParameter.type==doesntExist", 0);
        executeQueryAndVerify("_s=eventParameter.type!=doesntExist", 8);

        // Query with every property of eventParameter
        executeQueryAndVerify("_s=eventParameter.name==testParm1;eventParameter.value==This is an awesome parm%21;eventParameter.type==string", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm2;eventParameter.value==This is a weird parm;eventParameter.type==string", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm3;eventParameter.value==Here's another parm;eventParameter.type==string", 8);

        executeQueryAndVerify("_s=eventParameter.name==testParm1;eventParameter.value==This is an awesome parm%21", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm1;eventParameter.value==This is a weird parm", 0);
        executeQueryAndVerify("_s=eventParameter.name==testParm1;eventParameter.value==Here's another parm", 0);

        executeQueryAndVerify("_s=eventParameter.name==testParm2;eventParameter.value==This is an awesome parm%21", 0);
        executeQueryAndVerify("_s=eventParameter.name==testParm2;eventParameter.value==This is a weird parm", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm2;eventParameter.value==Here's another parm", 0);

        executeQueryAndVerify("_s=eventParameter.name==testParm3;eventParameter.value==This is an awesome parm%21", 0);
        executeQueryAndVerify("_s=eventParameter.name==testParm3;eventParameter.value==This is a weird parm", 0);
        executeQueryAndVerify("_s=eventParameter.name==testParm3;eventParameter.value==Here's another parm", 8);

        executeQueryAndVerify("_s=eventParameter.type==string;eventParameter.value==This is an awesome parm%21", 4);
        executeQueryAndVerify("_s=eventParameter.type==string;eventParameter.value==This is a weird parm", 4);
        executeQueryAndVerify("_s=eventParameter.type==string;eventParameter.value==Here's another parm", 8);

        executeQueryAndVerify("_s=eventParameter.name==testParm*", 8);

        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value==*awesome*", 4);
        // Negative filter eliminates half of the results
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value!=*awesome*", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value==*weird*", 4);
        // Negative filter eliminates half of the results
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value!=*weird*", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value==*another*", 8);
        // All events have testParm3 so the negative filter will eliminate all results
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value!=*another*", 0);

        // Wildcard value paired with specific non-unique value
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value==This is an awesome parm%21", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value==This is a weird parm", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.value==Here's another parm", 8);


        executeQueryAndVerify("_s=eventParameter.type==*ring*;eventParameter.value==*awesome*", 4);
        executeQueryAndVerify("_s=eventParameter.type==*ring*;eventParameter.value!=*weird*", 4);


        // This does not work properly because:
        // - eventParameter.type is not a wildcard value so an alias with a JOIN condition will be used 
        //   for querying
        // - eventParameter.type is a non-unique field so the JOIN condition will return multiple rows
        //executeQueryAndVerify("_s=eventParameter.type==string;eventParameter.value!=*weird*", 4); // 8
        //executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.type==string", 8); // 16

        // A workaround is to use a wildcard value instead so that the query doesn't use the alias
        executeQueryAndVerify("_s=eventParameter.type==string*;eventParameter.value!=*weird*", 4);
        executeQueryAndVerify("_s=eventParameter.name==testParm*;eventParameter.type==string*", 8);

        // Many parenthetical queries will work
        executeQueryAndVerify("_s=(eventParameter.name==testParm2*),(eventParameter.name==testParm1*)", 8);
        executeQueryAndVerify("_s=(eventParameter.name==testParm2*),(eventParameter.value!=*awesome*)", 4);
        executeQueryAndVerify("_s=(eventParameter.name==testParm1),(eventParameter.value==*weird*)", 8);
        executeQueryAndVerify("_s=(eventParameter.name==testParm1),(eventParameter.value!=*weird*)", 4);
        executeQueryAndVerify("_s=(eventParameter.name==testParm2),(eventParameter.value==*weird*)", 4);
        executeQueryAndVerify("_s=(eventParameter.name==testParm2),(eventParameter.value!=*weird*)", 8);
        executeQueryAndVerify("_s=(eventParameter.name==testParm3),(eventParameter.value==*weird*)", 8);
        executeQueryAndVerify("_s=(eventParameter.name==testParm3),(eventParameter.value!=*weird*)", 8);

        // Doesn't work because join conditions for each search property combine spuriously with each other
        // across the FIQL OR restriction (',')
        //executeQueryAndVerify("_s=(eventParameter.name==testParm1),(eventParameter.name==testParm2)", 8);
        //executeQueryAndVerify("_s=(eventParameter.name==testParm1),(eventParameter.name==testParm3)", 8);

        // Workaround by using wildcards for the values
        executeQueryAndVerify("_s=(eventParameter.name==testParm1*),(eventParameter.name==testParm2*)", 8);
        executeQueryAndVerify("_s=(eventParameter.name==testParm1*);(eventParameter.name==testParm2*)", 0);
        executeQueryAndVerify("_s=(eventParameter.name==testParm1*),(eventParameter.name==testParm3*)", 8);
        executeQueryAndVerify("_s=(eventParameter.name==testParm1*);(eventParameter.name==testParm3*)", 4);
    }

    @Test
    public void testRootAliasFiltering() throws Exception {
        executeQueryAndVerify("_s=uei==*somethingWentWrong", 2);
        executeQueryAndVerify("_s=uei==*somethingWentWrong;category.name==Linux", 1);

        executeQueryAndVerify("_s=uei==*something*", 8);
        executeQueryAndVerify("_s=uei==*somethingIs*", 6);
        executeQueryAndVerify("_s=uei!=*somethingIs*", 2);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=ipAddr==192.168.1.1", 3);
        executeQueryAndVerify("_s=ipAddr==192.168.1.2", 4);
        executeQueryAndVerify("_s=ipAddr==192.*.*.2", 4);
        executeQueryAndVerify("_s=ipAddr==192.168.1.1-2", 7);
        executeQueryAndVerify("_s=ipAddr==127.0.0.1", 0);
        executeQueryAndVerify("_s=ipAddr!=127.0.0.1", 8);
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

        executeQueryAndVerify("_s=alarm.uei==*something*", 8);
        executeQueryAndVerify("_s=alarm.uei==*somethingIs*", 6);
        executeQueryAndVerify("_s=alarm.uei!=*somethingIs*", 2);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=alarm.ipAddr==192.168.1.1", 3);
        executeQueryAndVerify("_s=alarm.ipAddr==192.168.1.2", 4);
        executeQueryAndVerify("_s=alarm.ipAddr==192.*.*.2", 4);
        executeQueryAndVerify("_s=alarm.ipAddr==192.168.1.1-2", 7);
        executeQueryAndVerify("_s=alarm.ipAddr==127.0.0.1", 0);
        executeQueryAndVerify("_s=alarm.ipAddr!=127.0.0.1", 8);
    }

    @Test
    public void testCXFDoubleDecoding() throws Exception {
        sendRequest(GET, "/alarms", parseParamData("_s=alarm.lastEventTime==1970-01-01T00:00:00.000+0000"), 500); // + turns to space
        sendRequest(GET, "/alarms", parseParamData("_s=alarm.lastEventTime==1970-01-01T00:00:00.000%2B0000"), 500); // %2B turns to space in the servlet handler
        executeQueryAndVerify("_s=alarm.lastEventTime==1970-01-01T00:00:00.000%252B0000", 0);
        executeQueryAndVerify("_s=alarm.lastEventTime=ge=1970-01-01T00:00:00.000%252B0000", 8);
        executeQueryAndVerify("_s=alarm.lastEventTime=lt=1970-01-01T00:00:00.003%252B0000", 3);
        executeQueryAndVerify("_s=node.label==*%C3%AA*", 1); // "ê" url-encoded
        executeQueryAndVerify("_s=node.label==*ê*", 1);
        executeQueryAndVerify("_s=node.label==*%20*", 0); // this will turn to space in the servlet handler
        executeQueryAndVerify("_s=node.label==*%2520*", 0); // this will turn to space in CXF
        executeQueryAndVerify("_s=node.label==*%252520*", 1); // the string "%20"
        executeQueryAndVerify("_s=node.label==*+*", 0); // this will turn to space in the servlet handler
        executeQueryAndVerify("_s=node.label==*%252B*", 1); // this will turn to +
        executeQueryAndVerify("_s=node.label!=%00", 8); // null is url-encoded as %00
        executeQueryAndVerify("_s=id!=%00", 8); // null is url-encoded as %00
    }

    /**
     * Test filtering for properties of {@link OnmsServiceType}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceFiltering() throws Exception {
        // Verify service queries
        executeQueryAndVerify("_s=serviceType.name==ICMP", 8);
        executeQueryAndVerify("_s=serviceType.name!=ICMP", 0);
        executeQueryAndVerify("_s=serviceType.name==SNMP", 0);
        executeQueryAndVerify("_s=serviceType.name==*MP", 8);
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
        executeQueryAndVerify("_s=ipInterface.ipAddress!=127.0.0.1", 8);
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
        executeQueryAndVerify("_s=node.label!=server01", 5);
        executeQueryAndVerify("_s=node.label==server02", 4);
        executeQueryAndVerify("_s=node.label!=server02", 4);
        executeQueryAndVerify("_s=(node.label==server01,node.label==server02)", 7);
        executeQueryAndVerify("_s=node.label!=\u0000", 8);
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
     * Test filtering for netmask property of {@link OnmsIpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testNetmaskFiltering() throws Exception {
        executeQueryAndVerify("_s=ipInterface.netMask==255.255.255.0", 8);
        executeQueryAndVerify("_s=ipInterface.netMask==\u0000", 0);
        executeQueryAndVerify("_s=ipInterface.netMask!=255.255.255.0", 0);
        executeQueryAndVerify("_s=ipInterface.netMask==255.255.127.0", 0);
    }

    /**
     * Test filtering for properties of {@link OnmsSnmpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testSnmpFiltering() throws Exception {
        executeQueryAndVerify("_s=snmpInterface.ifSpeed==10000000", 8);
        executeQueryAndVerify("_s=snmpInterface.ifSpeed==\u0000", 0);
        executeQueryAndVerify("_s=snmpInterface.ifSpeed!=500", 8);
        executeQueryAndVerify("_s=snmpInterface.ifSpeed==500", 0);
        executeQueryAndVerify("_s=snmpInterface.ifName==eth0", 8);
        executeQueryAndVerify("_s=snmpInterface.ifName==\u0000", 0);
        executeQueryAndVerify("_s=snmpInterface.ifName!=eth1", 8);
        executeQueryAndVerify("_s=snmpInterface.ifName==eth1", 0);
    }

    /**
     * Test filtering for properties of {@link OnmsMonitoringLocation}.
     * 
     * @throws Exception
     */
    @Test
    public void testLocationFiltering() throws Exception {
        executeQueryAndVerify("_s=location.locationName==Default", 8);
        executeQueryAndVerify("_s=location.locationName!=Default", 0);
    }

    /**
     * Test metadata autocompletion.
     * 
     * @throws Exception
     */
    @Test
    public void testNodeLabelPropertyValues() throws Exception {
        JSONObject object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", Collections.emptyMap(), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));
        JSONArray values = object.getJSONArray("value");
        // Values should be sorted alphabetically so this order is deterministic
        assertEquals("server01", values.getString(0));
        assertEquals("server02", values.getString(1));
        assertEquals(SERVER3_NAME, values.getString(2));

        object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", Collections.singletonMap("limit", "1"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        values = object.getJSONArray("value");
        assertEquals("server01", values.getString(0));

        // Using a limit less than 1 should result in an unlimited return value
        object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", Collections.singletonMap("limit", "0"), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));
        values = object.getJSONArray("value");
        assertEquals("server01", values.getString(0));
        assertEquals("server02", values.getString(1));
        assertEquals(SERVER3_NAME, values.getString(2));
        object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", Collections.singletonMap("limit", "-2"), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));
        values = object.getJSONArray("value");
        assertEquals("server01", values.getString(0));
        assertEquals("server02", values.getString(1));
        assertEquals(SERVER3_NAME, values.getString(2));

        // Test a query
        object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", Collections.singletonMap("q", "02"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        values = object.getJSONArray("value");
        assertEquals("server02", values.getString(0));

        object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", Collections.singletonMap("q", "server"), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));
        values = object.getJSONArray("value");
        assertEquals("server01", values.getString(0));
        assertEquals("server02", values.getString(1));
        assertEquals(SERVER3_NAME, values.getString(2));

        // Test a query with a limit
        object = new JSONObject(sendRequest(GET, "/alarms/properties/node.label", ImmutableMap.of(
            "q", "server",
            "limit", "1"
        ), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        values = object.getJSONArray("value");
        assertEquals("server01", values.getString(0));
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
        .addIpInterface(ipAddress).setIsManaged("M").setIsSnmpPrimary("S").setNetMask("255.255.255.0");
        builder.addService(m_databasePopulator.getServiceTypeDao().findByName("ICMP"));
        final OnmsNode node = builder.getCurrentNode();
        m_databasePopulator.getNodeDao().save(node);
        LOG.debug("ifspeed={}", node.getSnmpInterfaceWithIfIndex(1).getIfSpeed());
        return node;
    }

    private OnmsCategory createCategory(final String categoryName) {
        final OnmsCategory cat = new OnmsCategory(categoryName);
        m_databasePopulator.getCategoryDao().save(cat);
        m_databasePopulator.getCategoryDao().flush();
        return cat;
    }

    private void createAlarm(final OnmsNode node, final String eventUei, final OnmsSeverity severity, final long epoch) {
        final OnmsIpInterface alarmNode = node.getIpInterfaces().iterator().next();

        final OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_databasePopulator.getDistPollerDao().whoami());
        event.setEventCreateTime(new Date(epoch));
        event.setEventDisplay("Y");
        event.setEventHost("127.0.0.1");
        event.setEventLog("Y");
        event.setEventSeverity(OnmsSeverity.INDETERMINATE.getId());
        event.setEventSource("JUnit");
        event.setEventTime(new Date(epoch));
        event.setEventUei(eventUei);
        if (ALARM_COUNTER.getAndIncrement() % 2 == 0) {
            event.addEventParameter(new OnmsEventParameter(event, "testParm1", "This is an awesome parm!", "string"));
        } else {
            event.addEventParameter(new OnmsEventParameter(event, "testParm2", "This is a weird parm", "string"));
        }
        event.addEventParameter(new OnmsEventParameter(event, "testParm3", "Here's another parm", "string"));
        event.setIpAddr(alarmNode.getIpAddress());
        event.setNode(node);
        event.setServiceType(m_databasePopulator.getServiceTypeDao().findByName("ICMP"));
        event.setEventSeverity(severity.getId());
        event.setIfIndex(alarmNode.getIfIndex());
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
        alarm.setIpAddr(alarmNode.getIpAddress());
        alarm.setSeverity(severity);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setServiceType(m_databasePopulator.getServiceTypeDao().findByName("ICMP"));
        alarm.setIfIndex(alarmNode.getIfIndex());
        m_databasePopulator.getAlarmDao().save(alarm);
        m_databasePopulator.getAlarmDao().flush();
    }

    private void executeQueryAndVerify(final String query, final int totalCount) throws Exception {
        final Map<String, String> params = parseParamData(query);
        int expectedStatus = 200;
        if (totalCount == 0) {
            expectedStatus = 204;
        }

        LOG.debug("executeQueryAndVerify: GET /alarms = {}, params={}", expectedStatus, params);
        final String response = sendRequest(GET, "/alarms", params, expectedStatus);

        if (totalCount > 0) {
            final JSONObject object = new JSONObject(response);
            Assert.assertEquals(totalCount, object.getInt("totalCount"));
        }
    }

}
