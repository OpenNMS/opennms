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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
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
public class OutageRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(OutageRestServiceIT.class);
    
    public OutageRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    // TODO Needs some work
    @Test
    public void testOutages() throws Exception {
        String url = "/outages";

        LOG.warn(sendRequest(GET, url, parseParamData("orderBy=id"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifLostService=gt=2017-04-01T00:00:00.000-0400"), 204));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifLostService=le=2017-04-01T00:00:00.000-0400"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService==1970-01-01T00:00:00.000-0000"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService!=1970-01-01T00:00:00.000-0000"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.suppressTime==1970-01-01T00:00:00.000-0000"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=outage.suppressTime!=1970-01-01T00:00:00.000-0000"), 204));
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsOutage} without the alias.
     * 
     * @throws Exception
     */
    @Test
    public void testRootAliasOrderBy() throws Exception {
        String url = "/outages";
        sendRequest(GET, url, parseParamData("orderBy=id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ifLostService"), 200);
        sendRequest(GET, url, parseParamData("orderBy=ifRegainedService"), 200);
        sendRequest(GET, url, parseParamData("orderBy=suppressedBy"), 200);
        sendRequest(GET, url, parseParamData("orderBy=suppressTime"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsOutage}.
     * 
     * @throws Exception
     */
    @Test
    public void testOutageAliasOrderBy() throws Exception {
        String url = "/outages";
        sendRequest(GET, url, parseParamData("orderBy=outage.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=outage.ifLostService"), 200);
        sendRequest(GET, url, parseParamData("orderBy=outage.ifRegainedService"), 200);
        sendRequest(GET, url, parseParamData("orderBy=outage.suppressedBy"), 200);
        sendRequest(GET, url, parseParamData("orderBy=outage.suppressTime"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsEvent}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceLostEventAliasOrderBy() throws Exception {
        String url = "/outages";
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventAutoAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventCorrelation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventCreateTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventDescr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventDisplay"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventForward"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventLog"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventLogGroup"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventLogMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventMouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventNotification"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventOperAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventOperActionMenuText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventOperInstruct"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventParameters"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventPathOutage"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventSeverity"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventSnmp"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventSnmpHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventSuppressedCount"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventTTicket"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventTTicketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.eventUei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceLostEvent.ipAddr"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsEvent}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceRegainedEventAliasOrderBy() throws Exception {
        String url = "/outages";
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventAckTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventAckUser"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventAutoAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventCorrelation"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventCreateTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventDescr"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventDisplay"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventForward"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventLog"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventLogGroup"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventLogMsg"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventMouseOverText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventNotification"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventOperAction"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventOperActionMenuText"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventOperInstruct"), 200);
        // TODO: Cannot sort by parms since they are all stored in one database column
        //sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventParameters"), 200);
        //sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventParms"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventPathOutage"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventSeverity"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventSnmp"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventSnmpHost"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventSource"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventSuppressedCount"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventTime"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventTTicket"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventTTicketState"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.eventUei"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=serviceRegainedEvent.ipAddr"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsAssetRecord}.
     * 
     * @throws Exception
     */
    @Test
    public void testAssetAliasOrderBy() throws Exception {
        String url = "/outages";
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
        String url = "/outages";
        sendRequest(GET, url, parseParamData("orderBy=distPoller.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=distPoller.label"), 200);
        sendRequest(GET, url, parseParamData("orderBy=distPoller.location"), 200);
    }

    /**
     * Test {@code orderBy} for properties of {@link OnmsIpInterface}.
     * 
     * @throws Exception
     */
    @Test
    public void testIpInterfaceAliasOrderBy() throws Exception {
        String url = "/outages";
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
        String url = "/outages";
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
        String url = "/outages";
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
     * Test {@code orderBy} for properties of {@link OnmsServiceType}.
     * 
     * @throws Exception
     */
    @Test
    public void testServiceTypeAliasOrderBy() throws Exception {
        String url = "/outages";
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
        String url = "/outages";
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.id"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifAdminStatus"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifIndex"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifOperStatus"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.ifSpeed"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.lastCapsdPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.lastSnmpPoll"), 200);
        sendRequest(GET, url, parseParamData("orderBy=snmpInterface.netMask"), 200);
    }

}
