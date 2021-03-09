/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.models.CdpLinkNodeDTO;
import org.opennms.web.rest.v2.models.LldpLinkNodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EnhanceLinkdRestServiceTestIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(EnhanceLinkdRestServiceTestIT.class);

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private LldpLinkDao lldpLinkDao;

    @Autowired
    private BridgeTopologyService bridgeTopologyService;

    @Autowired
    private CdpLinkDao cdpLinkDao;

    @Autowired
    private OspfLinkDao ospfLinkDao;

    @Autowired
    private IsIsLinkDao isisLinkDao;

    @Autowired
    private LldpElementDao lldpElementDao;

    @Autowired
    private CdpElementDao cdpElementDao;

    @Autowired
    private OspfElementDao ospfElementDao;

    @Autowired
    private IsIsElementDao isisElementDao;

    @Autowired
    private BridgeElementDao bridgeElementDao;

    @Autowired
    private IpNetToMediaDao ipNetToMediaDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private PlatformTransactionManager transactionManager;

    public EnhanceLinkdRestServiceTestIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        databasePopulator.populateDatabase();
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetLldpLinks() throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(22);
        node.setNodeId("1");

        LldpLink link = new LldpLink();
        link.setId(11);
        link.setNode(node);
        link.setLldpLocalPortNum(123);
        link.setLldpPortId("1234");
        link.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
        link.setLldpPortDescr("portDescr");
        link.setLldpRemChassisId("34");
        link.setLldpRemSysname("remSysname");
        link.setLldpRemChassisId("remChassisId");
        link.setLldpRemChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_INTERFACENAME);
        link.setLldpRemPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS);
        link.setLldpRemPortId("remportId");
        link.setLldpRemPortDescr("remPortDescr");
        link.setLldpLinkCreateTime(new Date());
        link.setLldpLinkLastPollTime(new Date());

        lldpLinkDao.save(link);
        lldpLinkDao.flush();

        String url = "/enlinkd/lldplinks/1";
        String resultStr = sendRequest(GET, url, 200);
        LOG.info(resultStr);
        ObjectMapper mapper = new ObjectMapper();
        List<LldpLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, LldpLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetBridgelinks() {

    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetCdpLinks() throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(22);
        node.setNodeId("1");

        CdpLink cdpLink = new CdpLink();
        cdpLink.setId(23);
        cdpLink.setNode(node);
        cdpLink.setCdpLinkCreateTime(new Date());
        cdpLink.setCdpCacheIfIndex(5);
        cdpLink.setCdpCacheDeviceIndex(6);
        cdpLink.setCdpCacheAddressType(CdpLink.CiscoNetworkProtocolType.cdm);
        cdpLink.setCdpCacheAddress("cdpCacheAddress");
        cdpLink.setCdpCacheVersion("1.0");
        cdpLink.setCdpCacheDeviceId("123");
        cdpLink.setCdpCacheDevicePort("80");
        cdpLink.setCdpCacheDevicePlatform("platform");
        cdpLink.setCdpLinkCreateTime(new Date());
        cdpLink.setCdpLinkLastPollTime(new Date());

        cdpLinkDao.save(cdpLink);
        cdpLinkDao.flush();

        String url = "/enlinkd/cdplinks/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        List<CdpLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, CdpLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetOspfLinks() {
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetIsisLinks() {
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetLldpelem() {
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetCdpelem() {
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetOspfelem() {
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetIsiselem() {
    }
}