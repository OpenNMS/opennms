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

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class NotificationRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRestServiceIT.class);

    public NotificationRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void testFiql() throws Exception {
        String url = "/notifications";

        LOG.warn(sendRequest(GET, url, parseParamData("_s=notification.answeredBy==\u0000"), 200));
    }

    @Test
    public void testRootAliasFiltering() throws Exception {
        // ID that doesn't exist
        int id = Integer.MAX_VALUE;
        executeQueryAndVerify("_s=notification.notifyId==" + id, 0);
        executeQueryAndVerify("_s=notification.notifyId!=" + id, 1);
        executeQueryAndVerify("_s=notification.answeredBy==root", 0);
        executeQueryAndVerify("_s=notification.answeredBy!=root", 1);
        executeQueryAndVerify("_s=notification.numericMsg==Message", 0);
        executeQueryAndVerify("_s=notification.numericMsg!=Message", 1);
        executeQueryAndVerify("_s=notification.pageTime==1970-01-01T00:00:00.000-0000", 1);
        executeQueryAndVerify("_s=notification.pageTime!=1970-01-01T00:00:00.000-0000", 0);
        executeQueryAndVerify("_s=notification.queueId==Message", 0);
        executeQueryAndVerify("_s=notification.queueId!=Message", 1);
        executeQueryAndVerify("_s=notification.respondTime==1970-01-01T00:00:00.000-0000", 1);
        executeQueryAndVerify("_s=notification.respondTime!=1970-01-01T00:00:00.000-0000", 0);
        executeQueryAndVerify("_s=notification.subject==Message", 0);
        executeQueryAndVerify("_s=notification.subject!=Message", 1);
        executeQueryAndVerify("_s=notification.textMsg==Message", 0);
        executeQueryAndVerify("_s=notification.textMsg!=Message", 1);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=ipAddress==192.168.1.1", 1);
        executeQueryAndVerify("_s=ipAddress!=192.168.1.1", 0);
        executeQueryAndVerify("_s=ipAddress==192.168.1.2", 0);
        executeQueryAndVerify("_s=ipAddress==192.*.*.1", 1);
        executeQueryAndVerify("_s=ipAddress==192.*.*.2", 0);
        executeQueryAndVerify("_s=ipAddress==192.168.1.1-2", 1);
        executeQueryAndVerify("_s=ipAddress==127.0.0.1", 0);
        executeQueryAndVerify("_s=ipAddress!=127.0.0.1", 1);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=notification.ipAddress==192.168.1.1", 1);
        executeQueryAndVerify("_s=notification.ipAddress!=192.168.1.1", 0);
        executeQueryAndVerify("_s=notification.ipAddress==192.168.1.2", 0);
        executeQueryAndVerify("_s=notification.ipAddress==192.*.*.1", 1);
        executeQueryAndVerify("_s=notification.ipAddress==192.*.*.2", 0);
        executeQueryAndVerify("_s=notification.ipAddress==192.168.1.1-2", 1);
        executeQueryAndVerify("_s=notification.ipAddress==127.0.0.1", 0);
        executeQueryAndVerify("_s=notification.ipAddress!=127.0.0.1", 1);
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
        categoryId = m_databasePopulator.getCategoryDao().findByName("Routers").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 1);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 0);

        // Category that doesn't exist
        categoryId = Integer.MAX_VALUE;
        executeQueryAndVerify("_s=category.id==" + categoryId, 0);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 1);

        executeQueryAndVerify("_s=category.name==Routers", 1);
        executeQueryAndVerify("_s=category.name!=Routers", 0);
        executeQueryAndVerify("_s=category.name==Rou*", 1);
        executeQueryAndVerify("_s=category.name!=Rou*", 0);
        executeQueryAndVerify("_s=category.name==Ro*ers", 1);
        executeQueryAndVerify("_s=category.name!=Ro*ers", 0);
        executeQueryAndVerify("_s=category.name==DoesntExist", 0);
        executeQueryAndVerify("_s=category.name!=DoesntExist", 1);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsNotification} without the alias.
     * 
     * @throws Exception
     */
    @Test
    public void testRootAliasOrderBy() throws Exception {
        String url = "/notifications";
        sendRequest(GET, url, parseParamData("orderBy=notifyId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=answeredBy"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ipAddress"), 200);
        sendRequest(GET, url, parseParamData("orderBy=numericMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=pageTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=queueId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=respondTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=subject"), 200);
        sendRequest(GET, url, parseParamData("orderBy=textMsg"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsAssetRecord}.
     * 
     * @throws Exception
     */
    @Test
    public void testAssetAliasOrderBy() throws Exception {
        String url = "/notifications";
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
        String url = "/notifications";
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
    public void testEventAliasOrderBy() throws Exception {
        String url = "/notifications";

        // Test orderby for properties of OnmsEvent
        sendRequest(GET, url, parseParamData("orderBy=event.eventAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventAutoAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventCorrelation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventCreateTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventDescr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventDisplay"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventForward"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventLog"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventLogGroup"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventLogMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventMouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventNotification"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventOperAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventOperActionMenuText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventOperInstruct"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=event.eventParameters"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=event.eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventPathOutage"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventSeverity"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventSnmp"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventSnmpHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventSuppressedCount"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventTTicket"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventTTicketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.eventUei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=event.ipAddr"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsIpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testIpInterfaceAliasOrderBy() throws Exception {
        String url = "/notifications";
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
        String url = "/notifications";
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
        String url = "/notifications";
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
     * Test {@code orderBy} for properties of {@link OnmsNotification}.
     * 
     * @throws Exception
     */
    @Test
    public void testNotificationAliasOrderBy() throws Exception {
        String url = "/notifications";
        sendRequest(GET, url, parseParamData("orderBy=notification.notifyId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.answeredBy"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.ipAddress"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.numericMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.pageTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.queueId"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.respondTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.subject"), 200);
        sendRequest(GET, url, parseParamData("orderBy=notification.textMsg"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsServiceType}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceTypeAliasOrderBy() throws Exception {
        String url = "/notifications";
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
        String url = "/notifications";
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifAdminStatus"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifOperStatus"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifSpeed"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.lastCapsdPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.lastSnmpPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.netMask"), 200);
    }

    private void executeQueryAndVerify(String query, int totalCount) throws Exception {
        if (totalCount == 0) {
            sendRequest(GET, "/notifications", parseParamData(query), 204);
        } else {
            JSONObject object = new JSONObject(sendRequest(GET, "/notifications", parseParamData(query), 200));
            Assert.assertEquals(totalCount, object.getInt("totalCount"));
        }
    }

}
