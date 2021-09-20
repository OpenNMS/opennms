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

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.model.v2.BridgeElementNodeDTO;
import org.opennms.web.rest.model.v2.CdpElementNodeDTO;
import org.opennms.web.rest.model.v2.BridgeLinkNodeDTO;
import org.opennms.web.rest.model.v2.CdpLinkNodeDTO;
import org.opennms.web.rest.model.v2.EnlinkdDTO;
import org.opennms.web.rest.model.v2.IsisElementNodeDTO;
import org.opennms.web.rest.model.v2.IsisLinkNodeDTO;
import org.opennms.web.rest.model.v2.LldpElementNodeDTO;
import org.opennms.web.rest.model.v2.LldpLinkNodeDTO;
import org.opennms.web.rest.model.v2.OspfElementNodeDTO;
import org.opennms.web.rest.model.v2.OspfLinkNodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
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
public class NodeLinkRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(NodeLinkRestServiceIT.class);

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private LldpLinkDao lldpLinkDao;

    @Autowired
    BridgeBridgeLinkDao bridgeBridgeLinkDao;

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

    public NodeLinkRestServiceIT() {
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
    public void testGetEnlinkd() throws Exception{
        OnmsNode node1 = createNode1();
        creatLldpLink(node1);
        createbridgeBridgeLink(node1);
        createCdpLink(node1);
        createOspfLink(node1);
        createIsIsLink(node1);
        createLldpElement(node1);
        createBridgeElement(node1);
        createCdpElement(node1);
        createOspfElement(node1);
        createIsIsElement(node1);

        String url = "/enlinkd/1";
        String resultStr = sendRequest(GET, url, 200);
        LOG.info(resultStr);
        ObjectMapper mapper = new ObjectMapper();
        EnlinkdDTO result = mapper.readValue(resultStr, EnlinkdDTO.class);
        LOG.info(result.toString());
        Assert.assertEquals(1, result.getLldpLinkNodeDTOs().size());
        Assert.assertEquals(1, result.getBridgeLinkNodeDTOS().size());
        Assert.assertEquals(1, result.getCdpLinkNodeDTOS().size());
        Assert.assertEquals(1, result.getOspfLinkNodeDTOS().size());
        Assert.assertEquals(1, result.getIsisLinkNodeDTOS().size());
        Assert.assertNotNull(result.getLldpElementNodeDTO());
        Assert.assertEquals(1, result.getBridgeElementNodeDTOS().size());
        Assert.assertNotNull(result.getCdpElementNodeDTO());
        Assert.assertNotNull(result.getOspfElementNodeDTO());
        Assert.assertNotNull(result.getIsisElementNodeDTO());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetLldpLinks() throws Exception {
        OnmsNode node1 = createNode1();
        creatLldpLink(node1);

        String url = "/enlinkd/lldp_links/1";
        String resultStr = sendRequest(GET, url, 200);
        LOG.info(resultStr);
        ObjectMapper mapper = new ObjectMapper();
        List<LldpLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, LldpLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("remSysname", result.get(0).getLldpRemInfo());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetBridgelinks() throws Exception {
        OnmsNode node1 = createNode1();
        createbridgeBridgeLink(node1);

        String url = "/enlinkd/bridge_links/1";
        String resultStr = sendRequest(GET, url, 200);
        LOG.info(resultStr);
        ObjectMapper mapper = new ObjectMapper();
        List<BridgeLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, BridgeLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("port(bridgeport:80)", result.get(0).getBridgeLocalPort());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetCdpLinks() throws Exception {
        OnmsNode node1 = createNode1();
        createCdpLink(node1);

        String url = "/enlinkd/cdp_links/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        List<CdpLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, CdpLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("123",  result.get(0).getCdpCacheDevice());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetOspfLinks() throws Exception {
        OnmsNode node1 = createNode1();
        createOspfLink(node1);

        String url = "/enlinkd/ospf_links/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        List<OspfLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, OspfLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("(127.0.0.1)",  result.get(0).getOspfRemPort());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetIsisLinks() throws Exception {
        OnmsNode node1 = createNode1();
        createIsIsLink(node1);

        String url = "/enlinkd/isis_links/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        List<IsisLinkNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, IsisLinkNodeDTO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("off",  result.get(0).getIsisCircAdminState());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetLldpelem() throws Exception {
        OnmsNode node1 = createNode1();
        createLldpElement(node1);

        String url = "/enlinkd/lldp_elems/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        LldpElementNodeDTO result = mapper.readValue(resultStr, LldpElementNodeDTO.class);
        LOG.info(result.toString());
        Assert.assertEquals("lldpSysname", result.getLldpSysName());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetBridgeelem() throws Exception {
        OnmsNode node1 = createNode1();
        createBridgeElement(node1);

        String url = "/enlinkd/bridge_elems/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        List<BridgeElementNodeDTO> result = mapper.readValue(resultStr, mapper.getTypeFactory().constructCollectionType(List.class, BridgeElementNodeDTO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("srt", result.get(0).getBaseType());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetCdpelem() throws Exception {
        OnmsNode node1 = createNode1();
        createCdpElement(node1);

        String url = "/enlinkd/cdp_elems/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        CdpElementNodeDTO result = mapper.readValue(resultStr, CdpElementNodeDTO.class);
        Assert.assertEquals("cdpGlobalDeviceId", result.getCdpGlobalDeviceId());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetOspfelem() throws Exception {
        OnmsNode node1 = createNode1();
        createOspfElement(node1);

        String url = "/enlinkd/ospf_elems/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        OspfElementNodeDTO result = mapper.readValue(resultStr, OspfElementNodeDTO.class);
        Assert.assertEquals("enabled", result.getOspfAdminStat());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetIsiselem() throws Exception {
        OnmsNode node1 = createNode1();
        createIsIsElement(node1);

        String url = "/enlinkd/isis_elems/1";
        String resultStr = sendRequest(GET, url, 200);
        ObjectMapper mapper = new ObjectMapper();
        IsisElementNodeDTO result = mapper.readValue(resultStr, IsisElementNodeDTO.class);
        Assert.assertEquals("off", result.getIsisSysAdminState());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testGetEnlinkdNodeNotExist() throws Exception{
        OnmsNode node1 = createNode1();
        creatLldpLink(node1);
        createbridgeBridgeLink(node1);
        createCdpLink(node1);
        createOspfLink(node1);
        createIsIsLink(node1);
        createLldpElement(node1);
        createCdpElement(node1);
        createOspfElement(node1);
        createIsIsElement(node1);

        String url = "/enlinkd/789";
        sendRequest(GET, url, 404);
    }

    private void createIsIsElement(OnmsNode node){
        IsIsElement isIsElement = new IsIsElement();
        isIsElement.setId(2);
        isIsElement.setNode(node);
        isIsElement.setIsisSysID("isisSysID");
        isIsElement.setIsisSysAdminState(IsIsElement.IsisAdminState.off);
        isIsElement.setIsisNodeCreateTime(new Date());
        isIsElement.setIsisNodeLastPollTime(new Date());

        isisElementDao.save(isIsElement);
        isisElementDao.flush();
    }

    private OnmsNode createNode1(){
        OnmsNode node = new OnmsNode();
        node.setNodeId("1");
        node.setLabel("lable");
        return node;
    }

    private void creatLldpLink(OnmsNode node){
        LldpLink link = new LldpLink();
        link.setId(11);
        link.setNode(node);
        link.setLldpLocalPortNum(123);
        link.setLldpPortId("1234");
        link.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
        link.setLldpPortDescr("portDescr");
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
    }

    private void createbridgeBridgeLink(OnmsNode node){
        BridgeBridgeLink bridgeBridgeLink = new BridgeBridgeLink();
        bridgeBridgeLink.setId(2);
        bridgeBridgeLink.setNode(node);
        bridgeBridgeLink.setDesignatedNode(node);
        bridgeBridgeLink.setDesignatedPort(80);
        bridgeBridgeLink.setBridgePort(80);
        bridgeBridgeLink.setBridgeBridgeLinkCreateTime(new Date());
        bridgeBridgeLink.setBridgeBridgeLinkLastPollTime(new Date());

        bridgeBridgeLinkDao.save(bridgeBridgeLink);
        bridgeBridgeLinkDao.flush();
    }

    private void createCdpLink(OnmsNode node){
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
    }

    private void createOspfLink(OnmsNode node) throws UnknownHostException {
        OspfLink ospfLink = new OspfLink();
        ospfLink.setId(123);
        ospfLink.setNode(node);
        ospfLink.setOspfRemRouterId(InetAddress.getByName("127.0.0.1"));
        ospfLink.setOspfRemIpAddr(InetAddress.getByName("127.0.0.1"));
        ospfLink.setOspfRemAddressLessIndex(0);
        ospfLink.setOspfAddressLessIndex(0);
        ospfLink.setOspfLinkCreateTime(new Date());
        ospfLink.setOspfLinkLastPollTime(new Date());

        ospfLinkDao.save(ospfLink);
        ospfLinkDao.flush();
    }

    private void createIsIsLink(OnmsNode node){
        IsIsLink isIsLink = new IsIsLink();
        isIsLink.setId(123);
        isIsLink.setNode(node);
        isIsLink.setIsisCircIndex(1);
        isIsLink.setIsisISAdjIndex(1);
        isIsLink.setIsisISAdjState(IsIsLink.IsisISAdjState.down);
        isIsLink.setIsisISAdjNeighSNPAAddress("snpAddress");
        isIsLink.setIsisISAdjNeighSysType(IsIsLink.IsisISAdjNeighSysType.l2IntermediateSystem);
        isIsLink.setIsisISAdjNeighSysID("adjNeighSysID");
        isIsLink.setIsisISAdjNbrExtendedCircID(2);
        isIsLink.setIsisCircAdminState(IsIsElement.IsisAdminState.off);
        isIsLink.setIsisLinkCreateTime(new Date());
        isIsLink.setIsisLinkLastPollTime(new Date());

        isisLinkDao.save(isIsLink);
        isisLinkDao.flush();
    }

    private void createLldpElement(OnmsNode node){
        LldpElement lldpElement = new LldpElement();
        lldpElement.setId(1);
        lldpElement.setNode(node);
        lldpElement.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS);
        lldpElement.setLldpSysname("lldpSysname");
        lldpElement.setLldpChassisId("lldpChassisId");
        lldpElement.setLldpNodeCreateTime(new Date());
        lldpElement.setLldpNodeLastPollTime(new Date());

        lldpElementDao.save(lldpElement);
        lldpElementDao.flush();
    }

    private void createBridgeElement(OnmsNode node){
        BridgeElement bridgeElement = new BridgeElement();
        bridgeElement.setId(1);
        bridgeElement.setNode(node);
        bridgeElement.setBaseBridgeAddress("address");
        bridgeElement.setBaseNumPorts(0);
        bridgeElement.setBaseType(BridgeElement.BridgeDot1dBaseType.DOT1DBASETYPE_SRT);
        bridgeElement.setVlan(3);
        bridgeElement.setVlanname("vlanname");
        bridgeElement.setBridgeNodeCreateTime(new Date());
        bridgeElement.setBridgeNodeLastPollTime(new Date());

        bridgeElementDao.save(bridgeElement);
        bridgeElementDao.flush();
    }

    private void createCdpElement(OnmsNode node){
        CdpElement cdpElement = new CdpElement();
        cdpElement.setId(1);
        cdpElement.setNode(node);
        cdpElement.setCdpGlobalRun(OspfElement.TruthValue.FALSE);
        cdpElement.setCdpGlobalDeviceId("cdpGlobalDeviceId");
        cdpElement.setCdpGlobalDeviceIdFormat(CdpElement.CdpGlobalDeviceIdFormat.macAddress);
        cdpElement.setCdpNodeCreateTime(new Date());
        cdpElement.setCdpNodeLastPollTime(new Date());

        cdpElementDao.save(cdpElement);
        cdpElementDao.flush();
    }

    private void createOspfElement(OnmsNode node) throws UnknownHostException {
        OspfElement ospfElement = new OspfElement();
        ospfElement.setId(1);
        ospfElement.setNode(node);
        ospfElement.setOspfRouterId(InetAddress.getByName("127.0.0.1"));
        ospfElement.setOspfAdminStat(OspfElement.Status.enabled);
        ospfElement.setOspfVersionNumber(0);
        ospfElement.setOspfBdrRtrStatus(OspfElement.TruthValue.FALSE);
        ospfElement.setOspfASBdrRtrStatus(OspfElement.TruthValue.FALSE);
        ospfElement.setOspfRouterIdNetmask(InetAddress.getByName("127.0.0.1"));
        ospfElement.setOspfRouterIdIfindex(1);
        ospfElement.setOspfNodeCreateTime(new Date());
        ospfElement.setOspfNodeLastPollTime(new Date());

        ospfElementDao.save(ospfElement);
        ospfElementDao.flush();
    }
}