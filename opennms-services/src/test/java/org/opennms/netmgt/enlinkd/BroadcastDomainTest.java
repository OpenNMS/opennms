/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.config.EnhancedLinkdConfigManager;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
public class BroadcastDomainTest extends EnLinkdTestHelper {

    EnhancedLinkd linkd;

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.model.topology", "DEBUG");
        MockLogAppender.setupLogging(p);
        linkd = new EnhancedLinkd();
        EnhancedLinkdConfig config = new EnhancedLinkdConfigManager() {
            
            @Override
            public void save() throws MarshalException, IOException,
                    ValidationException {
                
            }
            
            @Override
            public void reload() throws IOException, MarshalException,
                    ValidationException {
                m_config = new EnlinkdConfiguration();
                m_config.setInitial_sleep_time(1000);
                m_config.setRescan_interval(10000);
            }
            
            @Override
            protected void saveXml(String xml) throws IOException {
            }
        };
        config.reload();
        linkd.setLinkdConfig(config);
    }

    @Test
    public void testLock() throws Exception {
        Object locker = new Object();
        Object notlocker = new Object();
        BroadcastDomain domain = new BroadcastDomain();
        assertTrue(domain.getLock(locker));
        assertTrue(!domain.getLock(notlocker));
        assertTrue(!domain.releaseLock(notlocker));
        assertTrue(domain.releaseLock(locker));
    }

    @Test
    public void testOneBridgeOnePortOneMac() throws Exception {
        OneBridgeOnePortOneMacTopology topology = new OneBridgeOnePortOneMacTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        OneBridgeMoreMacOnePortTopology topology = new OneBridgeMoreMacOnePortTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testOneBridgeComplete() throws Exception {

        OneBridgeCompleteTopology topology = new OneBridgeCompleteTopology();        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testTwoConnectedBridge() throws Exception {

        TwoConnectedBridgeTopology topology = new TwoConnectedBridgeTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testTwoMergeBridge() throws Exception {
        TwoMergeBridgeTopology topology = new TwoMergeBridgeTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test 
    public void testTwoBridgeWithBackbonePorts() {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test 
    public void testTwoBridgeWithBackbonePortsUsingBridgeAddressInBft() {
        TwoBridgeWithBackbonePortsTopologyWithBridgeinBft topology = new TwoBridgeWithBackbonePortsTopologyWithBridgeinBft();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check(ndbt.getDomain().getTopology());
    }

    @Test 
    public void testTwoBridgeOneCalculation() {

        TwoNodeTopology topology = new TwoNodeTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.check2nodeTopology(ndbt.getDomain().getTopology(),false);
        assertEquals(topology.nodeBId, ndbt.getDomain().getRootBridgeId());
    }
    

    @Test 
    public void testTwoBridgeTwoCalculation() {

        TwoNodeTopology topology = new TwoNodeTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        List<SharedSegment> shsegs = ndbt.getDomain().getTopology();
        printBridgeTopology(shsegs);
        assertEquals(3, shsegs.size());
        
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain().getTopology(),false);
        assertEquals(topology.nodeBId, domain.getRootBridgeId());
    }

    @Test 
    public void testTwoBridgeTwoCalculationReverse() {

        TwoNodeTopology topology = new TwoNodeTopology();
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        List<SharedSegment> shsegs = ndbt.getDomain().getTopology();
        printBridgeTopology(shsegs);
        assertEquals(3, shsegs.size());
        
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain().getTopology(),true);
        assertEquals(topology.nodeBId, domain.getRootBridgeId());
    }

    @Test
    public void testAB() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain().getTopology());

    }

    @Test
    public void testBA() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain().getTopology());
    }

    @Test
    public void testAC() {

        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain().getTopology());
    }

    @Test
    public void testCA() {

        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain().getTopology());
    }

    @Test
    public void testBC() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain().getTopology());
    }

    @Test
    public void testCB() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain().getTopology());
    }

    @Test
    public void testABC() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());

    }

    @Test
    public void testAThenBC() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testACThenB() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain().getTopology());

        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());

    }

    @Test
    public void testBAThenC() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.checkAB(ndbt.getDomain().getTopology());

        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());

    }

    @Test
    public void testBThenCA() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testCThenAB() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();

        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());
    }

    @Test
    public void testCBThenA() {
        ABCTopology topology = new ABCTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeBId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        topology.checkBC(ndbt.getDomain().getTopology());

        domain.addBridge(new Bridge(topology.nodeAId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.check(ndbt.getDomain().getTopology());

    }

    @Test
    public void testDE() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkDE(ndbt.getDomain().getTopology());
    }

    @Test
    public void testDF() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeFId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain().getTopology());
    }

    @Test
    public void testEF() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkEF(ndbt.getDomain().getTopology());
    }

    @Test
    public void testDG() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeGId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkDG(ndbt.getDomain().getTopology());
    }

    @Test
    public void testDEF() {

        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain().getTopology());

    }

    @Test
    public void testDFThenE() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeFId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain().getTopology());
        
        domain.addBridge(new Bridge(topology.nodeEId));
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain().getTopology());

    }

    @Test 
    public void testDEFG() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.addBridge(new Bridge(topology.nodeGId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
               ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.checkDEFG(ndbt.getDomain().getTopology());

    }

    @Test 
    public void testDEFGHIL() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.addBridge(new Bridge(topology.nodeGId));
        domain.addBridge(new Bridge(topology.nodeHId));
        domain.addBridge(new Bridge(topology.nodeIId));
        domain.addBridge(new Bridge(topology.nodeLId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeLId),topology.bftL);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain().getTopology());

    }

    //FIXME test Clean topology for bridge
    @Test
    public void testCleanTopology() {
        
    }
    
    //FIXME test Clean topology for root
    @Test 
    public void testCleanTopologyRoot() {
    }

    @Test 
    public void testHierarchySetUp() {
        DEFGHILTopology topology = new DEFGHILTopology();

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, null);
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.addBridge(new Bridge(topology.nodeGId));
        domain.addBridge(new Bridge(topology.nodeHId));
        domain.addBridge(new Bridge(topology.nodeIId));
        domain.addBridge(new Bridge(topology.nodeLId));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeLId),topology.bftL);
        ndbt.setBridgeElements(topology.elemlist);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain().getTopology());
        
        domain.hierarchySetUp(domain.getBridge(topology.nodeGId));
        assertEquals(topology.nodeGId, ndbt.getDomain().getRootBridgeId());
        assertEquals(true, ndbt.getDomain().getBridge(topology.nodeGId).isRootBridge());
        assertEquals(null, ndbt.getDomain().getBridge(topology.nodeGId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeDId).isRootBridge());
        assertEquals(topology.portDD, ndbt.getDomain().getBridge(topology.nodeDId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeEId).isRootBridge());
        assertEquals(topology.portEE, ndbt.getDomain().getBridge(topology.nodeEId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeFId).isRootBridge());
        assertEquals(topology.portFF, ndbt.getDomain().getBridge(topology.nodeFId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeHId).isRootBridge());
        assertEquals(topology.portHH, ndbt.getDomain().getBridge(topology.nodeHId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeIId).isRootBridge());
        assertEquals(topology.portII, ndbt.getDomain().getBridge(topology.nodeIId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeLId).isRootBridge());
        assertEquals(topology.portLL, ndbt.getDomain().getBridge(topology.nodeLId).getRootPort());

        printBridgeTopology(ndbt.getDomain().getTopology());

    }

}
