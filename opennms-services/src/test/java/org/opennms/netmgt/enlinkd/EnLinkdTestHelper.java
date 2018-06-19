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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgePortWithMacs;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public abstract class EnLinkdTestHelper {

    protected static void printLldpTopology(List<LldpLink> lldplinks) {
        for (LldpLink link : lldplinks)
            printLldpLink(link);
    }

    protected static void printLldpElements(List<LldpElement> lldpelements) {
        for (LldpElement element : lldpelements)
            printLldpElement(element);
    }

    protected static void printCdpElement(final CdpElement cdpElement) {
        System.err.println(cdpElement.printTopology());
    }

    protected static void printCdpLink(CdpLink link) {
        System.err.println(link.printTopology());
    }

    protected static void printLldpElement(final LldpElement lldpElement) {
        System.err.println(lldpElement.printTopology());
    }

    protected static void printLldpLink(LldpLink link) {
        System.err.println(link.printTopology());
    }

    protected static void printOspfTopology(List<OspfLink> ospflinks) {
        for (OspfLink link : ospflinks)
            printOspfLink(link);
    }

    protected static void printOspfElements(List<OspfElement> ospfelements) {
        for (OspfElement element : ospfelements)
            printOspfElement(element);
    }

    protected static void printOspfElement(final OspfElement element) {
        System.err.println(element.printTopology()); 
    }

    protected static void printOspfLink(OspfLink link) {
        System.err.println(link.printTopology());
    }

    public void setBridgeElements(BroadcastDomain domain, List<BridgeElement> elements) {
        for (BridgeElement element: elements) {
            Bridge bridge = domain.getBridge(element.getNode().getId());
            if (bridge == null) {
                continue;
            }
            if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress())) {
                bridge.getIdentifiers().add(element.getBaseBridgeAddress());
            }
            if (InetAddressUtils.
                    isValidStpBridgeId(element.getStpDesignatedRoot()) 
                    && !element.getBaseBridgeAddress().
                    equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
                bridge.setDesignated(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()));
            }
        }        
    }
    
    public BridgeForwardingTableEntry addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, String mac, Integer vlan) {
        BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
        link.setNodeId(node.getId());
        link.setBridgePort(bridgeport);
        link.setMacAddress(mac);
        link.setVlan(vlan);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        return link;
    }
    
    public BridgeForwardingTableEntry addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, String mac) {
        BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
        link.setNodeId(node.getId());
        link.setBridgePort(bridgeport);
        link.setMacAddress(mac);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        return link;
    }

    public BridgeForwardingTableEntry addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, Integer ifindex,String mac,Integer vlan) {
        BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
        link.setNodeId(node.getId());
        link.setBridgePort(bridgeport);
        link.setBridgePortIfIndex(ifindex);
        link.setMacAddress(mac);
        link.setVlan(vlan);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        return link;
    }

    public BridgeForwardingTableEntry addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, Integer ifindex,String mac) {
        BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
        link.setNodeId(node.getId());
        link.setBridgePort(bridgeport);
        link.setBridgePortIfIndex(ifindex);
        link.setMacAddress(mac);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        return link;
    }

    @SafeVarargs
    public static <T> boolean checkLinks(Iterable<T> iterable, Predicate<T>... matchers) {
        for (Predicate<T> matcher : matchers) {
            if (!Iterables.any(iterable, matcher)) {
                return false;
            }
        }
        return true;
    }

    public static Predicate<OspfLink> ospfLinkMatcher(OnmsNode node, OnmsNode parentNode, int ifIndex, int parentIfIndex) {
        return (link) -> {
           return node.equals(link.getNode()) &&
                   parentNode.getOspfElement().getOspfRouterId().equals(link.getOspfRemRouterId()) &&
                   ifIndex == link.getOspfIfIndex() &&
                   parentIfIndex == link.getOspfAddressLessIndex();
        };
    }
    
    /*
     *       spiazzomepe01:24<->24:spiazzofasw01:1<->1:comunespiazzowl1:6 
     *                                                                  |
     *                              spiasvig.asw0:1<->1:rsaspiazzowl1Id:4 
     *                                                  rsaspiazzowl1Id:5
     *                                                                  |
     * 1:vrendmunalv02:2<->11:vrendmunasw01:3<->3:comunevillarendenawl1:5           
     * |                                                               
     * 1:vigrenmualv02:2<->11:vigrenmuasw01:12<->2:vigrenmualv01:1
     *                                                           |
     *                           daremunasw0111<->2:daremunalv01:1 
     *                           
     *   villpizzasw01 this cannot be found should fail                                                                                                                                                
     */
    class TwentyNodeTopology {

        final int spiazzomepe01Id=6740; // cisco ME 
        
        final int spiazzofasw01Id=1571; // alcatel switch
        final int spiasvigasw01Id=1572; // alcatel switch
        final int villpizzasw01Id=5099; // alcatel switch
        final int vigrenmuasw01Id=1619; // alcatel switch
        final int daremunasw01Id=1396; // alcatel switch
        final int vrendmunasw01Id=1622; // alcatel switch
        
        final int daremunalv01Id=2473; // alvarion breeze
        final int vigrenmualv01Id=2673; // alvarion breeze
        final int vigrenmualv02Id=2674; // alvarion breeze
        final int vrendmunalv02Id=2676; // alvarion breeze
        
        final int rsaspiazzowl1Id=6796; // mikrotik
        final int comunespiazzowl1Id=6772; // mikrotik
        final int comunevillarendenawl1Id=6777; // mikrotik

        //bridge identifiers
        String macspiazzomepe01="0c8525e8f380";
        
        String macspiazzofasw01="00e0b1bb39b4";
        String macspiasvigasw01="00e0b1bba38e";
        String macvillpizzasw01="0012cf5d3180";
        String macdaremunasw01="00e0b1bbac4a";
        String macvrendmunasw01="00e0b1bba8f0";
        String macvigrenmuasw01="00e0b1bb3ec8";
        
        String macdaremunalv01="0010e724168b";
        String macvigrenmualv01="0010e7448f7f";
        String macvigrenmualv02="0010e744f3de";
        String macvrendmunalv02="0010e7241894";

        String macrsaspiazzowl1="d4ca6deafe24";
        String macomunespiazzowl1="000c42c54e75";
        String maccomunevillarendenawl1="000c42679d1d";


        OnmsNode spiazzomepe01=new OnmsNode();

        OnmsNode spiazzofasw01=new OnmsNode();
        OnmsNode spiasvigasw01=new OnmsNode();
        OnmsNode villpizzasw01=new OnmsNode();
        OnmsNode daremunasw01=new OnmsNode();
        OnmsNode vrendmunasw01=new OnmsNode();
        OnmsNode vigrenmuasw01=new OnmsNode();
        
        OnmsNode daremunalv01 = new OnmsNode();
        OnmsNode vigrenmualv01=new OnmsNode();
        OnmsNode vigrenmualv02=new OnmsNode();
        OnmsNode vrendmunalv02=new OnmsNode();

        OnmsNode rsaspiazzowl1=new OnmsNode();
        OnmsNode comunespiazzowl1=new OnmsNode();
        OnmsNode comunevillarendenawl1=new OnmsNode();

        BridgeElement elemspiazzomepe01=new BridgeElement();

        BridgeElement elemspiazzofasw01=new BridgeElement();
        BridgeElement elemspiasvigasw01=new BridgeElement();
        BridgeElement elemvillpizzasw01=new BridgeElement();
        BridgeElement elemvigrenmuasw01=new BridgeElement();
        BridgeElement elemdaremunasw01=new BridgeElement();
        BridgeElement elemvrendmunasw01=new BridgeElement();
        
        BridgeElement elemdaremunalv01=new BridgeElement();
        BridgeElement elemvigrenmualv01=new BridgeElement();
        BridgeElement elemvigrenmualv02=new BridgeElement();
        BridgeElement elemvrendmunalv02=new BridgeElement();

        BridgeElement elemrsaspiazzowl1=new BridgeElement();
        BridgeElement elemcomunespiazzowl1=new BridgeElement();
        BridgeElement elemcomunevillarendenawl1=new BridgeElement();
        
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

        Set<BridgeForwardingTableEntry> bftspiazzomepe01 = new HashSet<BridgeForwardingTableEntry>();

        Set<BridgeForwardingTableEntry> bftspiazzofasw01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftvrendmunasw01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftspiasvigasw01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftvillpizzasw01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftvigrenmuasw01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftdaremunasw01 = new HashSet<BridgeForwardingTableEntry>();
        
        Set<BridgeForwardingTableEntry> bftdaremunalv01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftvigrenmualv01 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftvigrenmualv02 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftvrendmunalv02 = new HashSet<BridgeForwardingTableEntry>();
        
        Set<BridgeForwardingTableEntry> bftrsaspiazzowl1 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftcomunespiazzowl1 = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftcomunevillarendenawl1 = new HashSet<BridgeForwardingTableEntry>();
        
        //5099
        String mac00e0b1bba8f2="00e0b1bba8f2";
        //6796 forwarders
        String mac4c5e0c104aac="4c5e0c104aac";
        String mac4c5e0c104bfa="4c5e0c104bfa";
        //6772
        String mac0003ea02a9b5="0003ea02a9b5";
        String mac00e0b1bb3ed4="00e0b1bb3ed4";
        String mac00e0b1bba8fc="00e0b1bba8fc";
        //spiazzofasw01
        String mac00e0b1bba390="00e0b1bba390";
        //1572
        String mac0003ea0176ca="0003ea0176ca";
        String mac00e0b1bb39b6="00e0b1bb39b6";
        String mac0003ea0175d6="0003ea0175d6";
        String mac00e0b1bba8f4="00e0b1bba8f4";
        
        //2473
        String mac0c8525e8f3c0="0c8525e8f3c0";
        String mac00e0b1bb3ed5="00e0b1bb3ed5";
        String mac00e0b1bbac56="00e0b1bbac56";
        
        //6777
        String mac00156d56ae1b="00156d56ae1b";
        String mac000c42736e85="000c42736e85";

        // port 0 6740 ?
        String mace48d8c3b04b7="e48d8c3b04b7"; 
        String mac000c29f49b8a="000c29f49b8a";

        //6740 port 3 unique well defined
        String mac0017c8288325="0017c8288325";
        String mac9cb65475cca4="9cb65475cca4";
        String mac000424ab02d9="000424ab02d9";
        String mac0011323f881d="0011323f881d";
        String mac000074f204a6="000074f204a6";
        String mac3ca82a7efb8f="3ca82a7efb8f";

        //6740 port 5/24
        String mac001ebe70cec0="001ebe70cec0";

        //6740 port 6/24
        String mac0022557fd68f="0022557fd68f";

        //6740 port 7/24
        String mace48d8c2e100c="e48d8c2e100c";

        //6740 port 8/24
        String mac001906d5cf50="001906d5cf50";

        //6740 port 24
        String mac002255362620="002255362620";
        String mace48d8cf17bcb="e48d8cf17bcb";
        String macc067af3ce614="c067af3ce614";
        String mac4c0082245938="4c0082245938";
        String mac00804867f17e="00804867f17e";
        String mac008048676c89="008048676c89";
        String mac4c5e0c8076b0="4c5e0c8076b0";
        String macc067af3ce59d="c067af3ce59d";
        String macd4ca6d4f1ab2="d4ca6d4f1ab2";
        String mac4c5e0c8007a1="4c5e0c8007a1";
        String mac001763010eb7="001763010eb7";
        String mac00804867f4e7="00804867f4e7";
        String mac00e0b1bb39cd="00e0b1bb39cd";
        String mac0012cf68d780="0012cf68d780";
        String mac001763010f41="001763010f41";
        String mac00176301092d="00176301092d";
        String macd4ca6dff4217="d4ca6dff4217";
        String mac000d48160cd3="000d48160cd3";
        String mac00804864187d="00804864187d";
        String macc067af3ce623="c067af3ce623";
        String macd4ca6d7953fe="d4ca6d7953fe";
        String mac0001a9019875="0001a9019875";
        String mac002255362ba7="002255362ba7";
        String macc067af3ce581="c067af3ce581";
        String mac00176301057d="00176301057d";
        
        public TwentyNodeTopology() {
        
            spiazzomepe01.setId(spiazzomepe01Id);

            spiazzofasw01.setId(spiazzofasw01Id);
            spiasvigasw01.setId(spiasvigasw01Id);
            villpizzasw01.setId(villpizzasw01Id);
            vigrenmuasw01.setId(vigrenmuasw01Id);
            daremunasw01.setId(daremunasw01Id);
            vrendmunasw01.setId( vrendmunasw01Id);

            daremunalv01.setId(daremunalv01Id);
            vigrenmualv01.setId(vigrenmualv01Id);
            vigrenmualv02.setId(vigrenmualv02Id);
            vrendmunalv02.setId(vrendmunalv02Id);
            
            rsaspiazzowl1.setId(rsaspiazzowl1Id);
            comunespiazzowl1.setId(comunespiazzowl1Id);
            comunevillarendenawl1.setId(comunevillarendenawl1Id);
            
            elemspiazzomepe01.setNode(spiazzomepe01);

            elemspiazzofasw01.setNode(spiazzofasw01);
            elemspiasvigasw01.setNode(spiasvigasw01);
            elemvillpizzasw01.setNode(villpizzasw01);
            elemvigrenmuasw01.setNode(vigrenmuasw01);
            elemvrendmunasw01.setNode(vrendmunasw01);
            elemdaremunasw01.setNode(daremunasw01);

            elemdaremunalv01.setNode(daremunalv01);
            elemvigrenmualv01.setNode(vigrenmualv01);
            elemvigrenmualv02.setNode(vigrenmualv02);
            elemvrendmunalv02.setNode(vrendmunalv02);

            elemrsaspiazzowl1.setNode(rsaspiazzowl1);
            elemcomunespiazzowl1.setNode(comunespiazzowl1);
            elemcomunevillarendenawl1.setNode(comunevillarendenawl1);
            
            elemspiazzomepe01.setBaseBridgeAddress(macspiazzomepe01);

            elemspiazzofasw01.setBaseBridgeAddress(macspiazzofasw01);
            elemspiasvigasw01.setBaseBridgeAddress(macspiasvigasw01);
            elemvillpizzasw01.setBaseBridgeAddress(macvillpizzasw01);
            elemvigrenmuasw01.setBaseBridgeAddress(macvigrenmuasw01);
            elemvrendmunasw01.setBaseBridgeAddress( macvrendmunasw01);
            elemdaremunasw01.setBaseBridgeAddress(macdaremunasw01);
            
            elemdaremunalv01.setBaseBridgeAddress(macdaremunalv01);
            elemvigrenmualv01.setBaseBridgeAddress(macvigrenmualv01);
            elemvigrenmualv02.setBaseBridgeAddress(macvigrenmualv02);
            elemvrendmunalv02.setBaseBridgeAddress(macvrendmunalv02);

            elemrsaspiazzowl1.setBaseBridgeAddress(macrsaspiazzowl1);
            elemcomunespiazzowl1.setBaseBridgeAddress(macomunespiazzowl1);
            elemcomunevillarendenawl1.setBaseBridgeAddress(maccomunevillarendenawl1);
            
            elemlist.add(elemspiazzomepe01);

            elemlist.add(elemdaremunasw01);
            elemlist.add(elemspiazzofasw01);
            elemlist.add(elemspiasvigasw01);
            elemlist.add(elemvillpizzasw01);
            elemlist.add(elemvigrenmuasw01);
            elemlist.add(elemvrendmunasw01);
            
            elemlist.add(elemdaremunalv01);
            elemlist.add(elemvigrenmualv01);
            elemlist.add(elemvigrenmualv02);
            elemlist.add(elemvrendmunalv02);
            
            elemlist.add(elemrsaspiazzowl1);
            elemlist.add(elemcomunespiazzowl1);
            elemlist.add(elemcomunevillarendenawl1);
            
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 0,  10100, mac000c29f49b8a,2204)); 
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 0,  10100, mace48d8c3b04b7,2204));
            
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 3,  10103, mac0017c8288325,3001));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 3,  10103, mac9cb65475cca4,3001));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 3,  10103, mac000424ab02d9,3001));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 3,  10103, mac0011323f881d,3001));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 3,  10103, mac000074f204a6,3001));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 3,  10103, mac3ca82a7efb8f,3001));

            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 5,  10105, macdaremunasw01,3050));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 5,  10105, mac001ebe70cec0,3050));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macdaremunasw01,25)); //duplicated entry see port 5
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac001ebe70cec0,25)); //duplicated entry see port 5
            
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 6,  10106, mac0022557fd68f,3060));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac0022557fd68f,803));

            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 7,  10107, mace48d8c2e100c,3072));
            
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 8,  10108, macvrendmunasw01,3080));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 8,  10108, mac001906d5cf50,3080));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macvrendmunasw01,25)); //duplicated entry see port 8
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac001906d5cf50, 25)); //duplicated entry see port 8

            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macspiazzofasw01,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macdaremunalv01,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macvigrenmuasw01,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macspiasvigasw01,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macvigrenmualv01,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, maccomunevillarendenawl1,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macvigrenmualv02,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macomunespiazzowl1,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macvillpizzasw01,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macrsaspiazzowl1,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macvrendmunalv02,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac0003ea0175d6,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac00804867f4e7,2204));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macd4ca6dff4217,2204));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac00804867f17e,2204));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac001763010f41,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac001763010eb7,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac00176301092d,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac4c5e0c8007a1,2204));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macc067af3ce581,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac008048676c89,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac4c5e0c8076b0,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macc067af3ce614,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac000d48160cd3,2204));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac00176301057d,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mace48d8cf17bcb,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac002255362620,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macc067af3ce59d,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac00804864187d,2204));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac0012cf68d780,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac4c0082245938,811));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macc067af3ce623,811));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac0003ea0176ca,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac002255362ba7,800));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, mac00e0b1bb39cd,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macd4ca6d4f1ab2,25));
            bftspiazzomepe01.add(addBridgeForwardingTableEntry(spiazzomepe01, 24, 10124, macd4ca6d7953fe,25));

            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macspiasvigasw01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, maccomunevillarendenawl1));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001,  macvrendmunasw01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macdaremunasw01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macvillpizzasw01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macvrendmunalv02));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macvigrenmualv02));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macomunespiazzowl1));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macvigrenmualv01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macvigrenmuasw01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macdaremunalv01));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macrsaspiazzowl1));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac001763010eb7));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00176301092d));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac001ebe70cec0));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00804867f4e7));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macc067af3ce623));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac4c5e0c8007a1));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00e0b1bba8f4));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac0003ea0176ca));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac001763010f41));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00804867f17e));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00e0b1bba390));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac008048676c89));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac000d48160cd3));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac002255362ba7));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macd4ca6d7953fe));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macc067af3ce581));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac0003ea0175d6));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macd4ca6dff4217));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macc067af3ce614));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac001906d5cf50));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mace48d8cf17bcb));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00176301057d));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macd4ca6d4f1ab2));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac0022557fd68f));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac0012cf68d780));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac00804864187d));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, macc067af3ce59d));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 1, 1001, mac4c5e0c8076b0));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 19, 1019, mac4c0082245938));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 23, 1023, mac002255362620));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 24, 1024, mace48d8c3b04b7));
            bftspiazzofasw01.add(addBridgeForwardingTableEntry(spiazzofasw01, 24, 1024, mac0c8525e8f3c0));

            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macvrendmunalv02));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macvigrenmualv02));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001,  macvrendmunasw01));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macdaremunasw01));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macomunespiazzowl1));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, maccomunevillarendenawl1));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macspiazzofasw01));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macvigrenmuasw01));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macdaremunalv01));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macrsaspiazzowl1));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac001ebe70cec0));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac00804867f4e7));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac4c5e0c8007a1));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macc067af3ce623));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac00e0b1bba8f4));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac00804867f17e));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macc067af3ce581));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac00e0b1bb39b6));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macd4ca6d7953fe));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macd4ca6dff4217));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac0003ea0175d6));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac001906d5cf50));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mace48d8cf17bcb));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macc067af3ce614));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac0c8525e8f3c0));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac4c0082245938));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macd4ca6d4f1ab2));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mace48d8c3b04b7));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac00804864187d));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac0022557fd68f));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, macc067af3ce59d));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 1, 1001, mac4c5e0c8076b0));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 15, 1015, mac0003ea0176ca));
            bftspiasvigasw01.add(addBridgeForwardingTableEntry(spiasvigasw01, 24, 1024, mac002255362ba7));

            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 2, 2, mac001ebe70cec0));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac00e0b1bb3ed5));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac00804867f17e));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macc067af3ce623));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 2, 2, mac001763010eb7));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1,  macvrendmunasw01));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac0003ea0176ca));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macomunespiazzowl1));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 2, 2, mac4c5e0c8007a1));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac00804867f4e7));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macvigrenmuasw01));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac4c5e0c8076b0));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macspiazzofasw01));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 2, 2, mac00e0b1bbac56));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macd4ca6d7953fe));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 2, 2, macc067af3ce581));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macd4ca6dff4217));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macrsaspiazzowl1));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac0003ea0175d6));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macspiasvigasw01));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macc067af3ce614));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 2, 2, macdaremunasw01));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macc067af3ce59d));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac00804864187d));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac0022557fd68f));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac4c0082245938));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mace48d8cf17bcb));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mace48d8c3b04b7));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac001906d5cf50));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, macd4ca6d4f1ab2));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, maccomunevillarendenawl1));
            bftdaremunalv01.add(addBridgeForwardingTableEntry(daremunalv01, 1, 1, mac0c8525e8f3c0));
            
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac00804867f17e));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macc067af3ce623));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1,  macvrendmunasw01));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macvigrenmualv02));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac0003ea0176ca));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac00e0b1bba8f2));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macomunespiazzowl1));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac00804867f4e7));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac4c5e0c8007a1));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macvigrenmuasw01));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macrsaspiazzowl1));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macc067af3ce581));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac4c5e0c8076b0));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macd4ca6dff4217));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macc067af3ce614));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mace48d8cf17bcb));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macspiasvigasw01));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macvigrenmualv01));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macdaremunasw01));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macspiazzofasw01));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac0022557fd68f));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac00804864187d));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac4c0082245938));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macc067af3ce59d));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac001906d5cf50));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macd4ca6d4f1ab2));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mace48d8c3b04b7));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, maccomunevillarendenawl1));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, mac0c8525e8f3c0));
            bftvillpizzasw01.add(addBridgeForwardingTableEntry(villpizzasw01, 1, 1, macd4ca6d7953fe));
            
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macdaremunalv01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac008048676c89));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, macd4ca6dff4217));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac001906d5cf50));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macvillpizzasw01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac00176301057d));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macc067af3ce614));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 1, 1, macrsaspiazzowl1));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, mac000c29f49b8a));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macvigrenmuasw01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macdaremunasw01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, macspiazzofasw01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macc067af3ce59d));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 1, 1, macspiasvigasw01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac4c5e0c8076b0));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mace48d8cf17bcb));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, mac00e0b1bb39b6));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac001763010eb7));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac0012cf68d780));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, mac0c8525e8f3c0));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, mac4c0082245938));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, macd4ca6d7953fe));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, mac4c5e0c104aac));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, macd4ca6d4f1ab2));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, mace48d8c3b04b7));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac00804867f4e7));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, maccomunevillarendenawl1));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac4c5e0c104bfa));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macc067af3ce623));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5,  macvrendmunasw01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac001763010f41));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac00804864187d));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac0022557fd68f));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac00e0b1bba8f4));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac00804867f17e));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 1, 1, mac0003ea0176ca));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac00176301092d));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 1, 1, mac00e0b1bba390));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 4, 4, macomunespiazzowl1));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macvigrenmualv01));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macvigrenmualv02));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macvrendmunalv02));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac001ebe70cec0));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 1, 1, mac002255362ba7));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac0003ea0175d6));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, mac4c5e0c8007a1));
            bftrsaspiazzowl1.add(addBridgeForwardingTableEntry(rsaspiazzowl1, 5, 5, macc067af3ce581));
            
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, mac4c5e0c8007a1));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macc067af3ce623));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac00804867f4e7));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mace48d8c3b04b7));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macd4ca6d4f1ab2));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, maccomunevillarendenawl1));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1,  macvrendmunasw01));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac0003ea0176ca));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac00804864187d));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac0022557fd68f));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac00804867f17e));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macomunespiazzowl1));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, macvigrenmualv02));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, mac001ebe70cec0));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, macc067af3ce581));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac0003ea0175d6));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac00e0b1bba8fc));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macspiasvigasw01));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac001906d5cf50));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, macdaremunasw01));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac0c8525e8f3c0));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, macc067af3ce614));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macc067af3ce59d));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac4c5e0c8076b0));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macspiazzofasw01));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macrsaspiazzowl1));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mace48d8cf17bcb));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, mac001763010eb7));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, macvigrenmuasw01));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macd4ca6d7953fe));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, macdaremunalv01));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, macd4ca6dff4217));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 2, 2, mac00e0b1bb3ed4));
            bftvigrenmualv01.add(addBridgeForwardingTableEntry(vigrenmualv01, 1, 1, mac4c0082245938));
            
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac00804867f4e7));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macc067af3ce623));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2,  macvrendmunasw01));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mace48d8c3b04b7));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, maccomunevillarendenawl1));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macd4ca6d4f1ab2));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 32769, 32769, macdaremunasw01));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac0022557fd68f));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac0003ea0176ca));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac00804867f17e));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac00804864187d));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 32769, 32769, mac001763010eb7));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macvigrenmualv01));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macspiazzofasw01));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 32769, 32769, mac00e0b1bbac56));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac0003ea0175d6));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac00e0b1bb3ed5));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 32769, 32769, mac4c5e0c8007a1));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macc067af3ce614));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac001906d5cf50));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mace48d8cf17bcb));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macspiasvigasw01));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac0c8525e8f3c0));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macrsaspiazzowl1));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macomunespiazzowl1));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macc067af3ce59d));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac4c5e0c8076b0));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macd4ca6d7953fe));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macvigrenmuasw01));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 32769, 32769, macc067af3ce581));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, mac4c0082245938));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 32769, 32769, mac001ebe70cec0));
            bftvigrenmualv02.add(addBridgeForwardingTableEntry(vigrenmualv02, 2, 2, macd4ca6dff4217));
            
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, maccomunevillarendenawl1));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macomunespiazzowl1));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011,  macvrendmunasw01));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macrsaspiazzowl1));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macspiasvigasw01));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macspiazzofasw01));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac0003ea0175d6));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac00e0b1bba8fc));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac4c5e0c8076b0));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac001906d5cf50));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mace48d8cf17bcb));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac4c0082245938));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macd4ca6d4f1ab2));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mace48d8c3b04b7));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macd4ca6d7953fe));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macc067af3ce59d));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac0c8525e8f3c0));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macd4ca6dff4217));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac00804867f17e));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, macc067af3ce623));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac00804867f4e7));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac0003ea0176ca));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac00804864187d));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 11, 1011, mac0022557fd68f));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, macdaremunalv01));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, macvigrenmualv02));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, macdaremunasw01));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, mac4c5e0c8007a1));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, mac00e0b1bbac56));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, macc067af3ce581));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 12, 1012, mac001763010eb7));
            bftvigrenmuasw01.add(addBridgeForwardingTableEntry(vigrenmuasw01, 19, 1019, macc067af3ce614));
            
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, macc067af3ce614));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac4c0082245938));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, maccomunevillarendenawl1));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, macdaremunasw01));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2,  macvrendmunasw01));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mace48d8c3b04b7));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac0003ea0176ca));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macd4ca6d4f1ab2));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac00804864187d));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac0022557fd68f));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac00804867f17e));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac00804867f4e7));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, mac001763010eb7));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, mac00e0b1bb3ed4));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macspiazzofasw01));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac0003ea0175d6));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, macvigrenmuasw01));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac00e0b1bba8fc));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, macdaremunalv01));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, mac4c5e0c8007a1));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac0c8525e8f3c0));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mace48d8cf17bcb));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macspiasvigasw01));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macrsaspiazzowl1));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macomunespiazzowl1));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macc067af3ce59d));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac4c5e0c8076b0));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, mac001906d5cf50));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, macc067af3ce581));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, macvigrenmualv02));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macd4ca6d7953fe));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 32769, 32769, mac001ebe70cec0));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macd4ca6dff4217));
            bftvrendmunalv02.add(addBridgeForwardingTableEntry(vrendmunalv02, 2, 2, macc067af3ce623));
            

            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 1, 1001, mac4c5e0c8007a1));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 1, 1001, mac001763010eb7));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 4, 1004, mac0c8525e8f3c0));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macspiazzofasw01));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macomunespiazzowl1));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macspiasvigasw01));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macvigrenmualv02));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macrsaspiazzowl1));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, maccomunevillarendenawl1));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macvigrenmuasw01));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011,  macvrendmunasw01));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac0022557fd68f));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac4c0082245938));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macd4ca6d7953fe));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac0003ea0175d6));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macd4ca6d4f1ab2));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mace48d8c3b04b7));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac4c5e0c8076b0));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac001906d5cf50));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mace48d8cf17bcb));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macc067af3ce59d));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macc067af3ce614));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac00804864187d));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac00e0b1bb3ed5));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macd4ca6dff4217));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, macc067af3ce623));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac00804867f4e7));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac00804867f17e));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 11, 1011, mac0003ea0176ca));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 18, 1018, mac001ebe70cec0));
            bftdaremunasw01.add(addBridgeForwardingTableEntry(daremunasw01, 19, 1019, macc067af3ce581));

            bftcomunespiazzowl1.add(addBridgeForwardingTableEntry(comunespiazzowl1, 6, 6, mac0003ea0176ca));
            bftcomunespiazzowl1.add(addBridgeForwardingTableEntry(comunespiazzowl1, 6, 6, mac0003ea0175d6));
            bftcomunespiazzowl1.add(addBridgeForwardingTableEntry(comunespiazzowl1, 1, 1, mac000c29f49b8a));
            bftcomunespiazzowl1.add(addBridgeForwardingTableEntry(comunespiazzowl1, 1, 1, macomunespiazzowl1));
            bftcomunespiazzowl1.add(addBridgeForwardingTableEntry(comunespiazzowl1, 6, 6, maccomunevillarendenawl1));
            
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, macvillpizzasw01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mace48d8cf17bcb));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, macc067af3ce59d));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac4c5e0c8076b0));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac00804867f17e));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac00804864187d));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac001763010f41));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac0022557fd68f));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac00176301092d));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac0003ea0175d6));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac00176301057d));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac0012cf68d780));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 1, 1001, mac00804867f4e7));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macspiazzofasw01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, maccomunevillarendenawl1));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macspiasvigasw01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macomunespiazzowl1));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macrsaspiazzowl1));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, mac0c8525e8f3c0));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macd4ca6d7953fe));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macd4ca6dff4217));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, mac0003ea0176ca));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, mac4c0082245938));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, macd4ca6d4f1ab2));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, mace48d8c3b04b7));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, mac00e0b1bba390));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 3, 1003, mac00e0b1bb39b6));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macdaremunasw01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macdaremunalv01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macvigrenmualv02));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macvrendmunalv02));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macvigrenmuasw01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macvigrenmualv01));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macc067af3ce581));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, macc067af3ce614));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, mac4c5e0c8007a1));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, mac00e0b1bb3ed4));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 11, 1011, mac001763010eb7));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 19, 1019, macc067af3ce623));
            bftvrendmunasw01.add(addBridgeForwardingTableEntry(vrendmunasw01, 24, 1024, mac001906d5cf50));
            
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac0022557fd68f));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac00e0b1bba8f4));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac0012cf68d780));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac00804864187d));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, mac00e0b1bba390));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac00804867f4e7));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, mac4c0082245938));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, maccomunevillarendenawl1));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac4c5e0c8007a1));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macd4ca6d4f1ab2));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, mace48d8c3b04b7));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macd4ca6d7953fe));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macrsaspiazzowl1));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macc067af3ce581));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac0003ea0175d6));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac4c5e0c8076b0));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macc067af3ce614));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mace48d8cf17bcb));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac00176301057d));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macvigrenmualv01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac00176301092d));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, mac00e0b1bb39b6));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macspiasvigasw01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macvigrenmualv02));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macspiazzofasw01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macomunespiazzowl1));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macc067af3ce59d));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 2, 2, mac00156d56ae1b));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 2, 2, mac008048676c89));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac001906d5cf50));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, mac0c8525e8f3c0));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macvrendmunalv02));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macvillpizzasw01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac00804867f17e));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac001763010f41));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, macd4ca6dff4217));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac000c42736e85));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macvigrenmuasw01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macdaremunalv01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macc067af3ce623));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, mac001763010eb7));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3,  macvrendmunasw01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 3, 3, macdaremunasw01));
            bftcomunevillarendenawl1.add(addBridgeForwardingTableEntry(comunevillarendenawl1, 5, 5, mac0003ea0176ca));
            
        }
        
        
    }
    /*
     *         /////////////////////////////////////////////////////////////////////////////
     *         //                               switch B                                   //
     *         //       1                              2                          3     4  //         
     *         //////////////////////////////////////////////////////////////////////////////
     *                  |                              |
     *         //////////////////////    /////////////////////////////////
     *         //      50          //    //      wireless net           //
     *         //     switch D     //    /////////////////////////////////
     *         //      49          //         |                      |
     *         //////////////////////         |                      |
     *                  |                     |                      |
     *         //////////////////////    ////////////////////    /////////////////////
     *         //      24          //    //   11           //    //  3              //
     *         //     switch A     //    //  switch C      //    //   switch E      //
     *         //  4   5   52      //    //  19      24    //    //       23        //
     *         //////////////////////    ////////////////////    /////////////////////
     */
    class FiveSwitchTopology {
        final int nodeAId = 1;
        final int nodeBId = 2;
        final int nodeCId = 3;
        final int nodeDId = 6;
        final int nodeEId = 8;
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        OnmsNode nodeC= new OnmsNode();
        OnmsNode nodeD= new OnmsNode();
        OnmsNode nodeE= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        BridgeElement elementC = new BridgeElement();
        BridgeElement elementD = new BridgeElement();
        BridgeElement elementE = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<>();

        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftC = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftD = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftE = new HashSet<BridgeForwardingTableEntry>();

        String[] macsonAport4= {
                "a04aee425191"
        };

        String[] macsonAport5= {
                "a05a99b52703",
                "a05a99b52749"
        };

        String[] macsonAport52= {
                "a52a01030104",
                "a52a84f84401",
                "a52a29f49b80",
                "a52aed30816a",
                "a52a6d0e9976"
        };

        String[] macsonBport3= {
                "b03b63010d4f"
        };

        String[] macsonBport4= {
                "b04b427bfee3",
                "b04b42f213af",
                "b04b6301050f"
        };
        
        String[] macsonCport19 = {
                "c19c822458d2"
        };

        String[] macsonCport24 = {
                "c24c83f6120a"
        };

        String[] macsonEport23 = {
                "e23e454ac907"
        };

        String[] macsOnWirelessSegment = {
                "000c429e3f3d",
                "000c42ef1df6",
                "000c42ef1e02",
                "000c42f5d30a",
                "0012cf68f80f",
                "001d454777dc",
                "001d71d5e4e7",
                "0021a4357254",
                "00641393f352",
                "00ca6d147c96",
                "00ca6d69c484",
                "00ca6d82ab08",
                "00ca6d88234f",
                "00ca6d954b3b",
                "00ca6da2d626",
                "00ca6ded84c8",
                "00ca6ded84d6",
                "00ca6dedd059",
                "00ca6df7f801",
                "008d8cf63372"
        };

        String[] forwardersABCD = {
                "fabcd57fd894"      
        };

        String[] forwardersABD = {
                "fabd0f68f800",
                "fabd03010792"
        };

        String[] forwardersBE = {
                "fbe0b1bd265e",
        };

        String[] forwardersD = {
                "fd00b1bd2f5e"
        };

        String[] forwardersE = {
                "fe00b1bd2f5f",
                "fe0042db4e11"
        };

        String macA="aaaa005b2980";
        String macB="bbbb00bd2f5c";
        String macC="cccc00bd2652";
        String macD="dddd005d2120";
        String macE="eeee03f4ee00";
        
        public FiveSwitchTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress(macA);
            elemlist.add(elementA);
    
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);
    
            nodeC.setId(nodeCId);
            elementC.setNode(nodeC);
            elementC.setBaseBridgeAddress(macC);
            elemlist.add(elementC);

            nodeD.setId(nodeDId);
            elementD.setNode(nodeD);
            elementD.setBaseBridgeAddress(macD);
            elemlist.add(elementD);

            nodeE.setId(nodeEId);
            elementE.setNode(nodeE);
            elementE.setBaseBridgeAddress(macE);
            elemlist.add(elementE);

            //BFT B port 1
            bftB.add(addBridgeForwardingTableEntry(nodeB, 1, 1001, macsonAport52[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 1, 1001, macsonAport52[2]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 1, 1001, macsonAport52[4]));

            //BFT B port 2 bridge address forwarding rules
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macE));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macC));
            //BFT B port 2 forwarders
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersABD[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersABD[1]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersABCD[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersBE[0]));            

            //BFT B port 2 wireless segment
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[1]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[2]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[3]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[4]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[5]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[6]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[7]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[8]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[9]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[10]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[11]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[12]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[13]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[14]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[15]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[16]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[17]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[18]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[19]));
            
            //BFT B port 2 forwarding C
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsonCport19[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsonCport24[0]));
            
            //BFT B port 2 forwarding E
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsonEport23[0]));

            //BFT B port 3 
            bftB.add(addBridgeForwardingTableEntry(nodeB, 3, 1003, macsonBport3[0]));
            
            //BFT B port 4 
            bftB.add(addBridgeForwardingTableEntry(nodeB, 4, 1004, macsonBport4[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 4, 1004, macsonBport4[1]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 4, 1004, macsonBport4[2]));

            //BFT C port 11 bridge address forwarding rules
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macB));
            //BFT C port 11 forwarder
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, forwardersABCD[0]));
            
            //BFT C port 11 forwarding E
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonEport23[0]));

            //BFT C port 11 forwarding A
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonAport52[0]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonAport52[2]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonAport52[4]));
            
            //BFT C port 11 wireless
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[0]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[1]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[2]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[3]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[4]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[5]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[6]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[7]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[8]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[9]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[10]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[11]));            
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[12]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[13]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[14]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[15]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[16]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[17]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[18]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[19]));

            //BFT C port 19
            bftC.add(addBridgeForwardingTableEntry(nodeC, 19, 1019, macsonCport19[0]));
            
            //BFT C port 24
            bftC.add(addBridgeForwardingTableEntry(nodeC, 24, 1024, macsonCport24[0]));
            
            //BFT A port 4
            bftA.add(addBridgeForwardingTableEntry(nodeA,  4, 10104, macsonAport4[0]));
            //BFT A port 5
            bftA.add(addBridgeForwardingTableEntry(nodeA,  5, 10105, macsonAport5[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA,  5, 10105, macsonAport5[1]));

            //BFT A port 24 forwarding rules for base address
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macB));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macC));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macD));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macE));
            //BFT A port 24 forwarders
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, forwardersABD[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, forwardersABD[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, forwardersABCD[0]));

            //BFT A port 24 forwarding B
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport4[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport4[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport4[2]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport3[0]));
            //BFT A port 24 forwarding C
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonCport19[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonCport24[0]));

            //BFT A port 24 forwarding E
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonEport23[0]));

            //BFT A port 24 wireless
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[2]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[3]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[5]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[6]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[7]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[8]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[9]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[10]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[11]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[12]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[13]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[14]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[15]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[16]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[17]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[18]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[19]));
            
            //BFT A port 52 
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[2]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[3]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[4]));
            
            //BFT D port 49 forward A 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 49, 49, macsonAport52[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 49, 49, macsonAport52[2]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 49, 49, macsonAport52[4]));

            //BFT D port 50 base address 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macB));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macC));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macE));

            //BFT D port 50 forwarders 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersABD[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersABD[1]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersABCD[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersD[0]));
            
            //BFT D port 50 forward B 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport3[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport4[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport4[1]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport4[2]));

            //BFT D port 50 forward C 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonCport19[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonCport24[0]));

            //BFT D port 50 forward E 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonEport23[0]));

            //BFT D port 50 wireless 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[1]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[2]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[3]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[5]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[6]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[7]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[8]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[9]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[10]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[11]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[12]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[13]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[14]));
            

            //BFT E port 3 base address
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macB));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macC));
            //BFT E port 3 forwarders
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, forwardersE[0]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, forwardersE[1]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, forwardersBE[0]));
            //BFT E port 3 forward A
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsonAport52[4]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsonAport52[0]));
            //BFT E port 3 forward C
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsonCport19[0]));
            
            //BFT E port 3 wireless 
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[0]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[1]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[2]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[3]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[4]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[5]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[6]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[7]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[8]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[9]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[10]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[11]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[12]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[13]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[14]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[15]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[16]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[17]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[18]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[19]));
            
            //BFT E port 23 wireless 
            bftE.add(addBridgeForwardingTableEntry(nodeE, 23, 23, macsonEport23[0]));
        }
        
        public void checkBC(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(5, domain.getBridgeNodesOnDomain().size());
            assertEquals(nodeBId, domain.getRootBridge().getNodeId().intValue());
            assertEquals(6, domain.getSharedSegments().size());
            assertEquals(4, domain.getSharedSegments(nodeBId).size());
            assertEquals(3, domain.getSharedSegments(nodeCId).size());
            for (Bridge bridge: domain.getBridges()) {
                if (bridge.isRootBridge()) {
                    assertEquals(nodeBId, bridge.getNodeId().intValue());
                    assertNull(bridge.getRootPort());
                } else {
                    switch (bridge.getNodeId().intValue()) {
                    case nodeCId:
                        assertNotNull(bridge.getRootPort());
                        break;
                    case nodeAId:
                        assertNull(bridge.getRootPort());
                        break;
                    case nodeDId:
                        assertNull(bridge.getRootPort());
                        break;
                    case nodeEId:
                        assertNull(bridge.getRootPort());
                        break;
                    default:
                        assertTrue(false);
                        break;

                    }
                }
            }
            for (SharedSegment segment: domain.getSharedSegments()) {
                assertNotNull(segment.getDesignatedBridge());
                assertNotNull(segment.getDesignatedPort());
                switch (segment.getDesignatedBridge().intValue()) {
                case nodeBId:
                    checkBcalcBC(segment);
                    break;
                case nodeCId:
                    checkCcalcBC(segment);
                    break;

                default:
                    assertTrue(false);
                    break;
                }
                for (BridgeMacLink bridgeMacLink : SharedSegment.getBridgeMacLinks(segment)) {
                    assertNotNull(bridgeMacLink.getBridgePort());
                    assertNotNull(bridgeMacLink.getBridgePortIfIndex());
                    assertNotNull(bridgeMacLink.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, bridgeMacLink.getLinkType());
               }
            }
        }
                
        public void checkCcalcBC(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeCId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 19:
                assertEquals(macsonCport19.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport19) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 24:
                assertEquals(macsonCport24.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport24) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }

        }

        public void checkBcalcBC(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeBId, segment.getDesignatedBridge().intValue());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 1:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(segment).size());
                assertEquals(3, segment.getMacsOnSegment().size());
                assertEquals(3, SharedSegment.getBridgeMacLinks(segment).size());
                break;
            case 2:
                assertEquals(2, segment.getBridgePortsOnSegment().size());
                assertEquals(2, segment.getBridgePort(nodeBId).getBridgePort().intValue());
                assertEquals(11, segment.getBridgePort(nodeCId).getBridgePort().intValue());
                assertEquals(macsOnWirelessSegment.length+forwardersABCD.length+macsonEport23.length, segment.getMacsOnSegment().size());
                for (String mac: macsOnWirelessSegment) {
                    assertTrue(segment.containsMac(mac));
                }
                for (String mac: forwardersABCD) {
                    assertTrue(segment.containsMac(mac));
                }
                for (String mac: macsonEport23) {
                    assertTrue(segment.containsMac(mac));
                }
                assertEquals(macsOnWirelessSegment.length+forwardersABCD.length+macsonEport23.length, SharedSegment.getBridgeMacLinks(segment).size());
                assertEquals(1, SharedSegment.getBridgeBridgeLinks(segment).size());
                BridgeBridgeLink blink = SharedSegment.getBridgeBridgeLinks(segment).iterator().next();  
                assertEquals(nodeBId, blink.getDesignatedNode().getId().intValue());
                assertEquals(2, blink.getDesignatedPort().intValue());
                assertEquals(11, blink.getBridgePort().intValue());
                break;
            case 3:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport3.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport3) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 4:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport4.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport4) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }
            
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(5, domain.getBridgeNodesOnDomain().size());
            assertEquals(nodeBId, domain.getRootBridge().getNodeId().intValue());
            assertEquals(11, domain.getSharedSegments().size());
            assertEquals(4, domain.getSharedSegments(nodeAId).size());
            assertEquals(4, domain.getSharedSegments(nodeBId).size());
            assertEquals(3, domain.getSharedSegments(nodeCId).size());
            assertEquals(2, domain.getSharedSegments(nodeDId).size());
            assertEquals(2, domain.getSharedSegments(nodeEId).size());
            for (Bridge bridge: domain.getBridges()) {
                if (bridge.isRootBridge())
                    assertNull(bridge.getRootPort());
                else
                    assertNotNull(bridge.getRootPort());
            }
            for (SharedSegment segment: domain.getSharedSegments()) {
                assertNotNull(segment.getDesignatedBridge());
                assertNotNull(segment.getDesignatedPort());
                switch (segment.getDesignatedBridge().intValue()) {
                case nodeAId:
                    checkA(segment);
                    break;
                case nodeBId:
                    checkB(segment);
                    break;
                case nodeCId:
                    checkC(segment);
                    break;
                case nodeDId:
                    checkD(segment);
                    break;
                case nodeEId:
                    checkE(segment);
                    break;

                default:
                    assertTrue(false);
                    break;
                }
                for (BridgeMacLink bridgeMacLink : SharedSegment.getBridgeMacLinks(segment)) {
                    assertNotNull(bridgeMacLink.getBridgePort());
                    assertNotNull(bridgeMacLink.getBridgePortIfIndex());
                    assertNotNull(bridgeMacLink.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, bridgeMacLink.getLinkType());
               }
            }
            checkA(domain.getForwarders(nodeAId));
            checkB(domain.getForwarders(nodeBId));
            checkC(domain.getForwarders(nodeCId));
            checkD(domain.getForwarders(nodeDId));
            checkE(domain.getForwarders(nodeEId));
            
        }
        
        public void checkEcalcBCAE(Set<BridgePortWithMacs> forwarders) {
            checkE(forwarders);
        }

        public void checkE(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            BridgePortWithMacs forward = forwarders.iterator().next(); 
            assertEquals(nodeEId, forward.getPort().getNodeId().intValue());
            assertEquals(3, forward.getPort().getBridgePort().intValue());
            assertEquals(3, forward.getPort().getBridgePortIfIndex().intValue());
            assertEquals(5, forward.getMacs().size());
            assertTrue(forward.getMacs().contains(macB));
            assertTrue(forward.getMacs().contains(macC));
            for (String mac: forwardersBE) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersE) {
                assertTrue(forward.getMacs().contains(mac));                
            }
        }        

        public void checkDcalcBCAD(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            BridgePortWithMacs forward = forwarders.iterator().next(); 
            assertEquals(nodeDId, forward.getPort().getNodeId().intValue());
            assertEquals(50, forward.getPort().getBridgePort().intValue());
            assertEquals(50, forward.getPort().getBridgePortIfIndex().intValue());

            assertEquals(6, forward.getMacs().size());
            assertTrue(forward.getMacs().contains(macB));
            assertTrue(forward.getMacs().contains(macC));
            assertTrue(forward.getMacs().contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
       
        }
        
        public void checkD(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            BridgePortWithMacs forward = forwarders.iterator().next(); 
            assertEquals(nodeDId, forward.getPort().getNodeId().intValue());
            assertEquals(50, forward.getPort().getBridgePort().intValue());
            assertEquals(50, forward.getPort().getBridgePortIfIndex().intValue());

            switch (forward.getMacs().size()) {
            case 7: assertTrue(true);
                    break;
            
            case 8: for (String fbe: forwardersBE) {
                    assertTrue(forward.getMacs().contains(fbe));
                };
                break;
            
            default: assertTrue(false);
            break;
                    
        }
            assertTrue(forward.getMacs().contains(macB));
            assertTrue(forward.getMacs().contains(macC));
            assertTrue(forward.getMacs().contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersABCD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
        }        

        public void checkCcalcBC(Set<BridgePortWithMacs> forwarders) {
            checkCcalcBCAD(forwarders);
        }

        public void checkCcalcBCA(Set<BridgePortWithMacs> forwarders) {
            checkCcalcBCAD(forwarders);
        }

        public void checkCcalcBCAD(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            BridgePortWithMacs forward = forwarders.iterator().next();
            assertEquals(nodeCId, forward.getPort().getNodeId().intValue());
            assertEquals(11, forward.getPort().getBridgePort().intValue());
            assertEquals(1011, forward.getPort().getBridgePortIfIndex().intValue());
            assertEquals(1, forward.getMacs().size());
            assertTrue(macB.equals(forward.getMacs().iterator().next()));
        }        

        public void checkCcalcBCAE(Set<BridgePortWithMacs> forwarders) {
            checkC(forwarders);
        }

        public void checkC(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            BridgePortWithMacs forward = forwarders.iterator().next(); 
            assertEquals(nodeCId, forward.getPort().getNodeId().intValue());
            assertEquals(11, forward.getPort().getBridgePort().intValue());
            assertEquals(1011, forward.getPort().getBridgePortIfIndex().intValue());

            assertEquals(2, forward.getMacs().size());
            assertTrue(forward.getMacs().contains(macB));
            for (String mac: forwardersABCD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
        }        

        public void checkBcalcBC(Set<BridgePortWithMacs> forwarders) {
            checkBcalcBCAD(forwarders);
        }     

        public void checkBcalcBCA(Set<BridgePortWithMacs> forwarders) {
            checkBcalcBCAD(forwarders);
        }        

        public void checkBcalcBCAD(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            
            BridgePortWithMacs forward = forwarders.iterator().next();
            assertEquals(nodeBId, forward.getPort().getNodeId().intValue());
            assertEquals(2, forward.getPort().getBridgePort().intValue());
            assertEquals(1002, forward.getPort().getBridgePortIfIndex().intValue());
        
            assertEquals(5, forward.getMacs().size());
            assertTrue(forward.getMacs().contains(macC));
            assertTrue(forward.getMacs().contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersBE) {
                assertTrue(forward.getMacs().contains(mac));                
            }
        }        

        public void checkBcalcBCAE(Set<BridgePortWithMacs> forwarders) {
            checkB(forwarders);
        }

        public void checkB(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            BridgePortWithMacs forward = forwarders.iterator().next();
            assertEquals(nodeBId, forward.getPort().getNodeId().intValue());
            assertEquals(2, forward.getPort().getBridgePort().intValue());
            assertEquals(1002, forward.getPort().getBridgePortIfIndex().intValue());
        
            assertEquals(6, forward.getMacs().size());
            assertTrue(forward.getMacs().contains(macC));
            assertTrue(forward.getMacs().contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersBE) {
                assertTrue(forward.getMacs().contains(mac));                
            }
            for (String mac: forwardersABCD) {
                assertTrue(forward.getMacs().contains(mac));                
            }
        }     

        public void checkAcalcBCA(Set<BridgePortWithMacs> forwarders) {
            checkAcalcBCAD(forwarders);
        }

        public void checkAcalcBCAD(Set<BridgePortWithMacs> forwarders) {
            assertEquals(1, forwarders.size());
            
            BridgePortWithMacs forward = forwarders.iterator().next();
            assertEquals(nodeAId, forward.getPort().getNodeId().intValue());
            assertEquals(24, forward.getPort().getBridgePort().intValue());
            assertEquals(10124, forward.getPort().getBridgePortIfIndex().intValue());

            assertEquals(6, forward.getMacs().size());
            assertTrue(forward.getMacs().contains(macB));
            assertTrue(forward.getMacs().contains(macC));
            assertTrue(forward.getMacs().contains(macD));
            assertTrue(forward.getMacs().contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(forward.getMacs().contains(mac));
            }
        }

        public void checkAcalcBCAE(Set<BridgePortWithMacs> forwarders) {
            checkA(forwarders);
        }

        public void checkA(Set<BridgePortWithMacs> forwarders) {
            BridgePortWithMacs forward = forwarders.iterator().next();
            assertEquals(nodeAId, forward.getPort().getNodeId().intValue());
            assertEquals(24, forward.getPort().getBridgePort().intValue());
            assertEquals(10124, forward.getPort().getBridgePortIfIndex().intValue());

            switch (forward.getMacs().size()) {
                case 7: assertTrue(true);
                        break;
                
                case 8: for (String fbe: forwardersBE) {
                        assertTrue(forward.getMacs().contains(fbe));
                    };
                    break;
                
                default: assertTrue(false);
                break;
                        
            }
            assertTrue(forward.getMacs().contains(macB));
            assertTrue(forward.getMacs().contains(macC));
            assertTrue(forward.getMacs().contains(macD));
            assertTrue(forward.getMacs().contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(forward.getMacs().contains(mac));
            }
            for (String mac: forwardersABCD) {
                assertTrue(forward.getMacs().contains(mac));
            }
        }
        
        public void checkA(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeAId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 4:
                assertEquals(macsonAport4.length, segment.getMacsOnSegment().size());
                for (String mac: macsonAport4) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 5:
                assertEquals(macsonAport5.length, segment.getMacsOnSegment().size());
                for (String mac: macsonAport5) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 52:
                assertEquals(macsonAport52.length, segment.getMacsOnSegment().size());
                for (String mac: macsonAport52) {
                    assertTrue(segment.containsMac(mac));
                }

             break;

            default:
                assertTrue(false);
                break;
            }
        }
        public void checkE(SharedSegment segment) throws BridgeTopologyException  {
            assertEquals(nodeEId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 23:
                assertEquals(macsonEport23.length, segment.getMacsOnSegment().size());
                for (String mac: macsonEport23) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }
            
        }
        
        public void checkC(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeCId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 19:
                assertEquals(macsonCport19.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport19) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 24:
                assertEquals(macsonCport24.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport24) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }

        }
        public void checkB(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeBId, segment.getDesignatedBridge().intValue());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 1:
                assertEquals(2, segment.getBridgePortsOnSegment().size());
                assertEquals(1, SharedSegment.getBridgeBridgeLinks(segment).size());
                assertEquals(0, segment.getMacsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeMacLinks(segment).size());
                BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(segment).iterator().next();
                assertEquals(nodeDId, link.getNode().getId().intValue());
                assertEquals(50, link.getBridgePort().intValue());
                assertEquals(nodeBId, link.getDesignatedNode().getId().intValue());
                assertEquals(1, link.getDesignatedPort().intValue());
                break;
            case 2:
                assertEquals(3, segment.getBridgePortsOnSegment().size());
                assertEquals(2, segment.getBridgePort(nodeBId).getBridgePort().intValue());
                assertEquals(3, segment.getBridgePort(nodeEId).getBridgePort().intValue());
                assertEquals(11, segment.getBridgePort(nodeCId).getBridgePort().intValue());
                assertEquals(macsOnWirelessSegment.length, segment.getMacsOnSegment().size());
                for (String mac: macsOnWirelessSegment) {
                    assertTrue(segment.containsMac(mac));
                }
                assertEquals(macsOnWirelessSegment.length, SharedSegment.getBridgeMacLinks(segment).size());
                assertEquals(2, SharedSegment.getBridgeBridgeLinks(segment).size());
                for (BridgeBridgeLink blink: SharedSegment.getBridgeBridgeLinks(segment))  {
                    assertEquals(nodeBId, blink.getDesignatedNode().getId().intValue());
                    assertEquals(2, blink.getDesignatedPort().intValue());
                    
                    switch (blink.getNode().getId().intValue()) {
                    case nodeEId:
                        assertEquals(3, blink.getBridgePort().intValue());
                        break;
                    case nodeCId:
                        assertEquals(11, blink.getBridgePort().intValue());
                        break;

                    default:
                        assertTrue(false);
                        break;
                    }
                }
                break;
            case 3:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport3.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport3) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 4:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport4.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport4) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }
            
        }

        public void checkD(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeDId, segment.getDesignatedBridge().intValue());
            assertEquals(2, segment.getBridgePortsOnSegment().size());
            assertEquals(1, SharedSegment.getBridgeBridgeLinks(segment).size());
            assertEquals(0, segment.getMacsOnSegment().size());
            assertEquals(0, SharedSegment.getBridgeMacLinks(segment).size());
            assertEquals(49, segment.getDesignatedPort().getBridgePort().intValue());
            BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(segment).iterator().next();
            assertEquals(nodeAId, link.getNode().getId().intValue());
            assertEquals(24, link.getBridgePort().intValue());
            assertEquals(nodeDId, link.getDesignatedNode().getId().intValue());
            assertEquals(49, link.getDesignatedPort().intValue());
        }

    }
    
    class ABCTopology {
        Integer nodeAId = 101;
        Integer nodeBId = 102;
        Integer nodeCId = 103;
        
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        OnmsNode nodeC= new OnmsNode();

        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        BridgeElement elementC = new BridgeElement();

        List<BridgeElement> elemlist = new ArrayList<>();

        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftC = new HashSet<BridgeForwardingTableEntry>();

        Integer portA = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB  = 2;
        Integer portBC = 23;
        Integer portCB = 32;
        Integer portC  = 3;

        String mac1 = "000daaaa0101"; // port A  ---port BA ---port CB
        String mac2 = "000daaaa0202"; // port AB ---port B  ---port CB
        String mac3 = "000daaaa0303"; // port AB ---port BC ---port C
        String shar = "000daaaa1111"; // to test loadTopology

        String macA = "aaaaaaaaaaaa";
        String macB = "bbbbbbbbbbbb";
        String macC = "cccccccccccc";
        /*
         *              -----------------
         *     mac1 --  ||portA|        |
         *              |   "Bridge A"  |
         *              |   |portAB|    |
         *              -----------------
         *                      |
         *                      |
         *              -----------------
         *              |   |portBA|    |
         *              |   "Bridge B"  |
         *     mac2 --  ||portB|        |
         *              |   |portBC|    |
         *              -----------------
         *                      |
         *                      |
         *              -----------------
         *              |   |portCB|    |
         *              |   "Bridge C"  |
         *     mac3 --  ||portC|        |
         *              -----------------
         *               
         */  

        public ABCTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress(macA);
            elemlist.add(elementA);
    
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);
    
            nodeC.setId(nodeCId);
            elementC.setNode(nodeC);
            elementC.setBaseBridgeAddress(macC);
            elemlist.add(elementC);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac3));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB, mac2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBC, mac3));

            bftC.add(addBridgeForwardingTableEntry(nodeC,portCB, mac1));
            bftC.add(addBridgeForwardingTableEntry(nodeC,portCB, mac2));
            bftC.add(addBridgeForwardingTableEntry(nodeC,portC, mac3));
        }

        public void checkAC(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeAId).size());
            assertEquals(0, domain.getForwarders(nodeCId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments();        	
            assertEquals(3, shsegms.size());

            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(0, bblinks.size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, bblinks.size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(nodeAId,link.getNode().getId());
                    assertEquals(portAB,link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(0, bblinks.size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void checkAB(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeAId).size());
            assertEquals(0, domain.getForwarders(nodeBId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments(); 
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    assertEquals(nodeAId, bblink.getDesignatedNode().getId());
                    assertEquals(nodeBId, bblink.getNode().getId());
                    assertEquals(portAB, bblink.getDesignatedPort());
                    assertEquals(portBA, bblink.getBridgePort());
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portBC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkBC(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeCId).size());
            assertEquals(0, domain.getForwarders(nodeBId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments();
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                    assertEquals(nodeCId, bblink.getNode().getId());
                    assertEquals(portBC, bblink.getDesignatedPort());
                    assertEquals(portCB, bblink.getBridgePort());
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portBA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeCId).size());
            assertEquals(0, domain.getForwarders(nodeBId).size());
            assertEquals(0, domain.getForwarders(nodeAId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments();
            assertEquals(5, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    if (bblink.getDesignatedNode().getId() == nodeAId) {
                        assertEquals(nodeBId, bblink.getNode().getId());
                        assertEquals(nodeAId, bblink.getDesignatedNode().getId());
                        assertEquals(portBA, bblink.getBridgePort());
                        assertEquals(portAB, bblink.getDesignatedPort());
                    } else if (bblink.getDesignatedNode().getId() == nodeBId) {
                        assertEquals(nodeCId, bblink.getNode().getId());
                        assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                        assertEquals(portCB, bblink.getBridgePort());
                        assertEquals(portBC, bblink.getDesignatedPort());
                    } else {
                        assertEquals(false, true);
                    }
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }
        
        public void checkwithshared(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeAId).size());
            assertEquals(0, domain.getForwarders(nodeCId).size());
            assertEquals(1, domain.getForwarders(nodeBId).size());
            BridgePortWithMacs forwentry = domain.getForwarders(nodeBId).iterator().next();
            assertEquals(nodeBId.intValue(), forwentry.getPort().getNodeId().intValue());
            assertEquals(portBA.intValue(), forwentry.getPort().getBridgePort().intValue());
            assertEquals(1, forwentry.getMacs().size());
            assertEquals(macA, forwentry.getMacs().iterator().next());
            List<SharedSegment> shsegms = domain.getSharedSegments();
            assertEquals(5, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    if (bblink.getDesignatedNode().getId() == nodeAId) {
                        assertEquals(nodeAId, bblink.getDesignatedNode().getId());
                        assertEquals(nodeBId, bblink.getNode().getId());
                        assertEquals(portBA, bblink.getBridgePort());
                        assertEquals(portAB, bblink.getDesignatedPort());
                     } else {
                        assertEquals(false, true);
                     }
               } else if (shared.getMacsOnSegment().contains(shar)) {
                   assertEquals(2, shared.getBridgeIdsOnSegment().size());
                   assertEquals(1, links.size());
                   assertEquals(1, bblinks.size());
                   assertEquals(1, shared.getMacsOnSegment().size());
                   BridgeBridgeLink bblink = bblinks.iterator().next();
                   assertEquals(nodeCId, bblink.getNode().getId());
                   assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                   assertEquals(portCB, bblink.getBridgePort());
                   assertEquals(portBC, bblink.getDesignatedPort());
                   
                   BridgeMacLink link = links.iterator().next();
                   assertEquals(nodeBId, link.getNode().getId());
                   assertEquals(portBC,link.getBridgePort());
                   assertEquals(shar, link.getMacAddress());
                   assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                   
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(0, bblinks.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(0, bblinks.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(0, bblinks.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }

    }
    
    class DEFGHILTopology {
        Integer nodeDId = 104;
        Integer nodeEId = 105;
        Integer nodeFId = 106;
        Integer nodeGId = 107;
        Integer nodeHId = 108;
        Integer nodeIId = 109;
        Integer nodeLId = 110;

        Integer portD  = 44;
        Integer portDD = 40;
        
        Integer portE  = 55;
        Integer portEE = 50;

        Integer portF  = 66;
        Integer portFF = 60;

        Integer portG7 = 7;
        Integer portG8 = 8;
        Integer portGD = 74;
        Integer portGE = 75;
        Integer portGF = 76;
        
        Integer portH1 = 1;
        Integer portH2 = 2;
        Integer portHH = 80;

        Integer portI3 = 3;
        Integer portI4 = 4;
        Integer portII = 90;

        Integer portL5 = 3;
        Integer portL6 = 4;
        Integer portLL = 20;

        String mac1 = "000daaaa0441"; // port D  ---port EE ---port FF ---portGD --portH1 --portII --portLL
        String mac2 = "000daaaa0442"; // port D  ---port EE ---port FF ---portGD --portH2 --portII --portLL
        String mac3 = "000daaaa0663"; // port DD ---port EE ---port F  ---portGF --portHH --portI3 --portLL
        String mac4 = "000daaaa0664"; // port DD ---port EE ---port F  ---portGF --portHH --portI4 --portLL
        String mac5 = "000daaaa0555"; // port DD ---port E ---port FF  ---portGE --portHH --portII --portL5
        String mac6 = "000daaaa0556"; // port DD ---port E ---port FF  ---portGE --portHH --portII --portL6
        String mac7 = "000daaaa0707"; // port DD ---port EE ---port FF ---portG7 --portHH --portII --portLL
        String mac8 = "000daaaa0808"; // port DD ---port EE ---port FF ---portG8 --portHH --portII --portLL

        OnmsNode nodeD= new OnmsNode();
        OnmsNode nodeE= new OnmsNode();
        OnmsNode nodeF= new OnmsNode();
        OnmsNode nodeG= new OnmsNode();
        OnmsNode nodeH= new OnmsNode();
        OnmsNode nodeI= new OnmsNode();
        OnmsNode nodeL= new OnmsNode();

        BridgeElement elementD = new BridgeElement();
        BridgeElement elementE = new BridgeElement();
        BridgeElement elementF = new BridgeElement();
        BridgeElement elementG = new BridgeElement();
        BridgeElement elementH = new BridgeElement();
        BridgeElement elementI = new BridgeElement();
        BridgeElement elementL = new BridgeElement();

        List<BridgeElement> elemlist = new ArrayList<>();

        Set<BridgeForwardingTableEntry> bftD = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftE = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftF = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftG = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftH = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftI = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftL = new HashSet<BridgeForwardingTableEntry>();

        /*
         *         -----------------     -----------------
         *  mac1---||portH1| portHH| --  ||portD|        |
         *         |   "Bridge H"  |     |   "Bridge D"  |     |-------------
         *  mac2---||portH2|       |     |       |portDD||-----||portGD|     |
         *         -----------------     -----------------     |             |
         *                                                     |             |
         *         -----------------     -----------------     |    |port G7||---mac7
         *  mac3---||portI3| portII| --  ||portF|        |     |             |
         *         |   "Bridge I"  |     |   "Bridge F"  |     |             |
         *  mac4---||portI4|       |     |       |portFF||-----||portGF|     |
         *         -----------------     -----------------     |             |
         *                                                     | "Bridge G"  |
         *         -----------------     -----------------     |             |
         *  mac5---||portL5| portLL| --  ||portE|        |     |    |port G8||---mac8
         *         |   "Bridge L"  |     |   "Bridge E"  |     |             |
         *  mac6---||portL6|       |     |       |portEE||-----||portGE|     |
         *         -----------------     -----------------     |-------------|
         */
    
        public DEFGHILTopology() {
            nodeD.setId(nodeDId);
            elementD.setNode(nodeD);
            elementD.setBaseBridgeAddress("dddddddddddd");
            elemlist.add(elementD);
    
            nodeE.setId(nodeEId);
            elementE.setNode(nodeE);
            elementE.setBaseBridgeAddress("ddddddddeddd");
            elemlist.add(elementE);
    
            nodeF.setId(nodeFId);
            elementF.setNode(nodeF);
            elementF.setBaseBridgeAddress("ddddddddfddd");
            elemlist.add(elementF);

            nodeG.setId(nodeGId);
            elementG.setNode(nodeG);
            elementG.setBaseBridgeAddress("ddddddd1dddd");
            elemlist.add(elementG);

            nodeI.setId(nodeIId);
            elementI.setNode(nodeI);
            elementI.setBaseBridgeAddress("ddddddd2dddd");
            elemlist.add(elementI);

            nodeH.setId(nodeHId);
            elementH.setNode(nodeH);
            elementH.setBaseBridgeAddress("ddddddd3dddd");
            elemlist.add(elementH);
            
            nodeL.setId(nodeLId);
            elementL.setNode(nodeL);
            elementL.setBaseBridgeAddress("ddddddd4dddd");
            elemlist.add(elementL);

            bftD.add(addBridgeForwardingTableEntry(nodeD,portD,  mac1));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portD,  mac2));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac3));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac4));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac5));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac6));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac7));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac8));

            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac1));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac2));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac3));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac4));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portE,  mac5));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portE,  mac6));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac7));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac8));

            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac1));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac2));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portF,  mac3));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portF,  mac4));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac5));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac6));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac7));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac8));

            bftG.add(addBridgeForwardingTableEntry(nodeG,portGD, mac1));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGD, mac2));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGF, mac3));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGF, mac4));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGE, mac5));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGE, mac6));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portG7, mac7));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portG8, mac8));

            bftH.add(addBridgeForwardingTableEntry(nodeH,portH1, mac1));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portH2, mac2));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac3));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac4));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac5));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac6));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac7));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac8));

            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac1));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac2));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portI3, mac3));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portI4, mac4));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac5));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac6));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac7));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac8));

            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac1));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac2));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac3));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac4));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portL5, mac5));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portL6, mac6));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac7));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac8));
          }
        
        public void checkDE(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeEId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portE,link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac3));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(4, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeBridgeLink bblink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeDId.intValue(), bblink.getDesignatedNode().getId().intValue());
                    assertEquals(portDD.intValue(), bblink.getDesignatedPort().intValue());
                    assertEquals(nodeEId.intValue(), bblink.getNode().getId().intValue());
                    assertEquals(portEE.intValue(), bblink.getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeDId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portDD,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac3));
                    assertTrue(macs.contains(mac4));
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDF(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
             List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac5));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(4, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink bblink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeDId.intValue(), bblink.getDesignatedNode().getId().intValue());
                    assertEquals(portDD.intValue(), bblink.getDesignatedPort().intValue());
                    assertEquals(nodeFId.intValue(), bblink.getNode().getId().intValue());
                    assertEquals(portFF.intValue(), bblink.getBridgePort().intValue());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeDId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portDD,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac5));
                    assertTrue(macs.contains(mac6));
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDG(BroadcastDomain domain) throws BridgeTopologyException  {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeGId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(6, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeDId, link.getDesignatedNode().getId());
                    assertEquals(nodeGId, link.getNode().getId());
                    assertEquals(portDD, link.getDesignatedPort());
                    assertEquals(portGD, link.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portGF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portGF,link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                         }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portGE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portGE,link.getBridgePort());
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                     }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portG7.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(portG7,link.getBridgePort());
                            assertEquals(nodeGId, link.getNode().getId());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                     assertEquals(portG8.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                     for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                             assertEquals(portG8,link.getBridgePort());
                             assertEquals(nodeGId, link.getNode().getId());
                             assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                     }
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkEF(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeEId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portF,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac3));
                    assertTrue(macs.contains(mac4));
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        Set<String> macs = new HashSet<String>();
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portE,link.getBridgePort());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            macs.add(link.getMacAddress());
                         }
                        assertTrue(macs.contains(mac5));
                        assertTrue(macs.contains(mac6));
                 } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac7));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeBridgeLink bblink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeEId.intValue(), bblink.getDesignatedNode().getId().intValue());
                    assertEquals(portEE, bblink.getDesignatedPort());
                    assertEquals(nodeFId.intValue(), bblink.getNode().getId().intValue());
                    assertEquals(portFF, bblink.getBridgePort());
                    assertEquals(4, SharedSegment.getBridgeMacLinks(shared).size());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac1));
                    assertTrue(macs.contains(mac2));
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));

                } else {
                    assertEquals(false, true);
                }

            }
        }

        public void checkDEF(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeEId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(4, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertTrue(!macs.contains(link.getMacAddress()));
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                       macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac1));
                    assertTrue(macs.contains(mac2));
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portE,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                     }
                    assertTrue(macs.contains(mac5));
                    assertTrue(macs.contains(mac6));
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        Set<String> macs = new HashSet<String>();
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            macs.add(link.getMacAddress());
                         }
                        assertTrue(macs.contains(mac3));
                        assertTrue(macs.contains(mac4));
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(3, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(2,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(2, SharedSegment.getBridgeBridgeLinks(shared).size());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portDD,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));
                } else {
                    assertEquals(false, true);
                }            }
        }

        public void checkDEFG(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeEId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
            assertEquals(0, domain.getForwarders(nodeGId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(8, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    if (shared.getBridgeIdsOnSegment().contains(nodeDId)) {
                        assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portDD,link.getDesignatedPort());
                        assertEquals(nodeGId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portGD,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeEId)) {
                        assertEquals(nodeEId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGE,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeFId)) {
                        assertEquals(nodeFId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portFF,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGF,link.getDesignatedPort());
                    } else {
                        assertEquals(false, true);
                    }
                } else {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertTrue(!macs.contains(link.getMacAddress()));
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                       macs.add(link.getMacAddress());
                   }
                   assertTrue(macs.contains(mac1));
                   assertTrue(macs.contains(mac2));

                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portE,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac5));
                    assertTrue(macs.contains(mac6));
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        Set<String> macs = new HashSet<String>();
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            macs.add(link.getMacAddress());
                        }
                        assertTrue(macs.contains(mac3));
                        assertTrue(macs.contains(mac4));
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portG7.intValue(), link.getBridgePort().intValue());
                    assertEquals(mac7, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                     assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                     BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                     assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                     assertEquals(portG8.intValue(), link.getBridgePort().intValue());
                     assertEquals(mac8, link.getMacAddress());
                     assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else {
                    assertEquals(false, true);
                }            
                }
            }
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(14, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    if (shared.getBridgeIdsOnSegment().contains(nodeDId) && shared.getDesignatedPort().getBridgePort().intValue() == portDD.intValue()) {
                        assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portDD,link.getDesignatedPort());
                        assertEquals(nodeGId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portGD,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeDId) && shared.getDesignatedPort().getBridgePort().intValue() == portD.intValue()) {
                            assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                            assertEquals(portD,link.getDesignatedPort());
                            assertEquals(nodeHId.intValue(),link.getNode().getId().intValue());
                            assertEquals(portHH,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeGId) && shared.getDesignatedPort().getBridgePort().intValue() == portGE.intValue()) {
                        assertEquals(nodeEId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGE,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeEId) && shared.getDesignatedPort().getBridgePort().intValue() == portE.intValue()) {
                        assertEquals(nodeEId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portE,link.getDesignatedPort());
                        assertEquals(nodeLId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portLL,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeGId) && shared.getDesignatedPort().getBridgePort().intValue() == portGF.intValue()) {
                        assertEquals(nodeFId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portFF,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGF,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeFId) && shared.getDesignatedPort().getBridgePort().intValue() == portF.intValue()) {
                        assertEquals(nodeFId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portF,link.getDesignatedPort());
                        assertEquals(nodeIId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portII,link.getBridgePort());
                    } else {
                        assertEquals(false, true);
                    }
                } else {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeHId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeHId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portH1.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeHId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portH1,link.getBridgePort());
                    assertEquals(mac1,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeHId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeHId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portH2.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeHId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portH2,link.getBridgePort());
                    assertEquals(mac2,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeIId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeIId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portI3.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeIId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portI3,link.getBridgePort());
                    assertEquals(mac3,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac4)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeIId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeIId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portI4.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeIId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portI4,link.getBridgePort());
                    assertEquals(mac4,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeLId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeLId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portL5.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeLId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portL5,link.getBridgePort());
                    assertEquals(mac5,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac6)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeLId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeLId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portL6.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeLId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portL6,link.getBridgePort());
                    assertEquals(mac6,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portG7.intValue(), link.getBridgePort().intValue());
                    assertEquals(mac7, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                     assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                     BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                     assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                     assertEquals(portG8.intValue(), link.getBridgePort().intValue());
                     assertEquals(mac8, link.getMacAddress());
                     assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else {
                    assertEquals(false, true);
                }            
                }
            }
        }

    }
    
    class TwoNodeTopology {
        Integer nodeAId = 22101;
        Integer nodeBId = 22102;
        Integer portA1 = 1;
        Integer portA6 = 6;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;
        Integer portB7 = 7 ;

        
        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 
        String macA13 = "000daa000a13"; // port A1  
        String macA14 = "000daa000a14"; // port A1          

        String macA61 = "000daa000a61"; // port A6 ---port BA 
        String macA62 = "000daa000a62"; // port A6 ---port BA 
        String macA63 = "000daa000a63"; // port A6 ---port BA  
        String macA64 = "000daa000a64"; // port A6          

        String macshared1  = "000daa000ab1"; // port AB ---port BA 
        String macshared2  = "000daa000ab2"; // port AB ---port BA 
        String macshared3  = "000daa000ab3"; // port AB ---port BA 
        String macAforwd4  = "000daa000ab4"; // port AB 
        String macAforwd5  = "000daa000ab5"; // port AB 
        String macAforwd6  = "000daa000ab6"; // port AB 

        String macBforwd1  = "000daa000ba1"; //          ---port BA 
        String macBforwd2  = "000daa000ba2"; //          ---port BA 
        String macBforwd3  = "000daa000ba3"; //          ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2
        String macB23 = "000daa000b23"; //         ---port B2

        String macB71 = "000daa000b71"; //         ---port B7 
        String macB72 = "000daa000b72"; //         ---port B7
        String macB73 = "000daa000b73"; //         ---port B7

        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();

        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        
        public TwoNodeTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);
        
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA11));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA12));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA13));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA14));
            
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA61));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA62));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA63));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA64));

            //3 Shared 
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macshared1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macshared2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macshared3));

            //3 forwarders
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAforwd4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAforwd5));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAforwd6));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB21));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB22));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA11));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA12));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA61));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA62));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA63));
 
            //3 shared
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macshared1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macshared2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macshared3));
            
            //3 forwarders
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macBforwd1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macBforwd2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macBforwd3));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB21));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB22));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB23));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, macB71));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, macB72));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, macB73));

        }
        
    public void check2nodeTopology(BroadcastDomain domain, boolean revertedbblink) throws BridgeTopologyException {
        Set<BridgePortWithMacs> forwardersA = domain.getForwarders(nodeAId);
        Set<BridgePortWithMacs> forwardersB = domain.getForwarders(nodeBId);
        assertEquals(1, forwardersA.size());
        assertEquals(1, forwardersB.size());
        Set<String> formacsA = new HashSet<String>();
        BridgePortWithMacs forwardA = forwardersA.iterator().next();
        assertEquals(nodeAId.intValue(), forwardA.getPort().getNodeId().intValue());
        assertEquals(portAB, forwardA.getPort().getBridgePort());
        formacsA.addAll(forwardA.getMacs());
        
        Set<String> formacsB = new HashSet<String>();
        BridgePortWithMacs forwardB = forwardersB.iterator().next();
        assertEquals(nodeBId.intValue(), forwardB.getPort().getNodeId().intValue());
        assertEquals(portBA, forwardB.getPort().getBridgePort());
        formacsB.addAll(forwardB.getMacs());

        assertTrue(formacsA.contains(macAforwd4));
        assertTrue(formacsA.contains(macAforwd5));
        assertTrue(formacsA.contains(macAforwd6));
        assertTrue(formacsB.contains(macBforwd1));
        assertTrue(formacsB.contains(macBforwd2));
        assertTrue(formacsB.contains(macBforwd3));

    	List<SharedSegment> shsegs = domain.getSharedSegments();
        assertEquals(5, shsegs.size());
        for (SharedSegment shared: shsegs) {
            assertTrue(!shared.noMacsOnSegment());
            Set<Integer> nodeidsOnSegment = shared.getBridgeIdsOnSegment();
            List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
            List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
            if (shared.getMacsOnSegment().contains(macshared1)) {
                assertEquals(2, nodeidsOnSegment.size());
                assertEquals(3, shared.getMacsOnSegment().size());
                assertEquals(3, links.size());
                assertEquals(1, bblinks.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertTrue(shared.getMacsOnSegment().contains(macshared2));
                assertTrue(shared.getMacsOnSegment().contains(macshared3));
                assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                Set<String> macs = new HashSet<String>();
                if (revertedbblink) {
                	assertEquals(nodeBId, dlink.getDesignatedNode().getId());
                	assertEquals(portBA, dlink.getDesignatedPort());
                	assertEquals(nodeAId, dlink.getNode().getId());
                	assertEquals(portAB, dlink.getBridgePort());                   
                        for (BridgeMacLink link: links) {
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                            assertEquals(link.getNode().getId().intValue(), nodeBId.intValue());
                            assertEquals(portBA, link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            macs.add(link.getMacAddress());
                        }
                } else {
                	assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                	assertEquals(portAB, dlink.getDesignatedPort());
                	assertEquals(nodeBId, dlink.getNode().getId());
                	assertEquals(portBA, dlink.getBridgePort());                                   	
                        for (BridgeMacLink link: links) {
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                            assertEquals(link.getNode().getId().intValue(), nodeAId.intValue());
                            assertEquals(portAB, link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            macs.add(link.getMacAddress());
                        }
                }
                assertTrue(macs.contains(macshared1));
                assertTrue(macs.contains(macshared2));
                assertTrue(macs.contains(macshared3));

            } else if (shared.getMacsOnSegment().contains(macB21)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertEquals(3, shared.getMacsOnSegment().size());
                assertEquals(3, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macB22));
                assertTrue(shared.getMacsOnSegment().contains(macB23));
                Set<String> macs = new HashSet<String>();
               for (BridgeMacLink link: links) {
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB2, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macB21));
               assertTrue(macs.contains(macB22));
               assertTrue(macs.contains(macB23));
            } else if (shared.getMacsOnSegment().contains(macB71)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertEquals(3, shared.getMacsOnSegment().size());
                assertEquals(3, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macB72));
                assertTrue(shared.getMacsOnSegment().contains(macB73));
                Set<String> macs = new HashSet<String>();
                for (BridgeMacLink link: links) {
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB7, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macB71));
               assertTrue(macs.contains(macB72));
               assertTrue(macs.contains(macB73));
            } else if (shared.getMacsOnSegment().contains(macA11)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertEquals(4, shared.getMacsOnSegment().size());
                assertEquals(4, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macA12));
                assertTrue(shared.getMacsOnSegment().contains(macA13));
                assertTrue(shared.getMacsOnSegment().contains(macA14));
                Set<String> macs = new HashSet<String>();
                for (BridgeMacLink link: links) {
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA1, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macA11));
               assertTrue(macs.contains(macA12));
               assertTrue(macs.contains(macA13));
               assertTrue(macs.contains(macA14));
              } else if (shared.getMacsOnSegment().contains(macA61)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertEquals(4, shared.getMacsOnSegment().size());
                assertEquals(4, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macA62));
                assertTrue(shared.getMacsOnSegment().contains(macA63));
                assertTrue(shared.getMacsOnSegment().contains(macA64));
                Set<String> macs = new HashSet<String>();
                for (BridgeMacLink link: links) {
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA6, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macA61));
               assertTrue(macs.contains(macA62));
               assertTrue(macs.contains(macA63));
               assertTrue(macs.contains(macA64));
            } else {
                assertTrue(false);
            }
        }
    }
    
    }

    class OneBridgeOnePortOneMacTopology {
        Integer nodeAId  = 10;
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;
        Integer portA5 = 5;

        String mac1 = "000daaaa0001"; // learned on port A1
        String mac2 = "000daaaa0002"; // learned on port A2 
        String mac3 = "000daaaa0003"; // learned on port A3 
        String mac4 = "000daaaa0004"; // learned on port A4 
        String mac5 = "000daaaa0005"; // learned on port A5 

        OnmsNode nodeA= new OnmsNode();
        BridgeElement element = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        
        public OneBridgeOnePortOneMacTopology() {
            nodeA.setId(nodeAId);
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA2, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA3, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA4, mac4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA5, mac5));
            
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> links = domain.getSharedSegments();
            assertEquals(5, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(1, shared.getMacsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                    assertEquals(link.getBridgePort(),shared.getDesignatedPort().getBridgePort());
                if (link.getBridgePort() == portA1) {
                    assertEquals(mac1, link.getMacAddress());
                } else if (link.getBridgePort() == portA2) {
                    assertEquals(mac2, link.getMacAddress());
                } else if (link.getBridgePort() == portA3) {
                    assertEquals(mac3, link.getMacAddress());
                } else if (link.getBridgePort() == portA4) {
                    assertEquals(mac4, link.getMacAddress());
                } else if (link.getBridgePort() == portA5) {
                    assertEquals(mac5, link.getMacAddress());
                } else {
                    assertEquals(-1, 1);
                }
                }
            }

        }
    }
    
    class OneBridgeMoreMacOnePortTopology {
        Integer nodeAId  = 20;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement element = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();

        Integer portA1 = 1;

        String mac1 = "000daaaa0001"; // port A1 
        String mac2 = "000daaaa0002"; // port A1
        String mac3 = "000daaaa0003"; // port A1
        String mac4 = "000daaaa0004"; // port A1

        
        public OneBridgeMoreMacOnePortTopology() {
            nodeA.setId(nodeAId);
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);


            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac4));

        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> links = domain.getSharedSegments();

            assertEquals(1, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(portA1,shared.getDesignatedPort().getBridgePort());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(4, shared.getMacsOnSegment().size());
                final Set<String> macs = shared.getMacsOnSegment();
                assertTrue(macs.contains(mac1));
                assertTrue(macs.contains(mac2));
                assertTrue(macs.contains(mac3));
                assertTrue(macs.contains(mac4));
                for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                    assertEquals(portA1, link.getBridgePort());
                }
            }

        }
    }

    class OneBridgeCompleteTopology {
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;

        Integer portA23 = 23;
        Integer portA24 = 24;
        Integer portA25 = 25;

        String mac1 = "000daaaa0001"; // port A1
        String mac2 = "000daaaa0002"; // port A2
        String mac3 = "000daaaa0003"; // port A3
        String mac4 = "000daaaa0004"; // port A4

        String mac231 = "000daaaa0231"; // port A23
        String mac232 = "000daaaa0232"; // port A23
        String mac233 = "000daaaa0233"; // port A23
        String mac234 = "000daaaa0234"; // port A23

        String mac241 = "000daaaa0241"; // port A24
        String mac242 = "000daaaa0242"; // port A24
        String mac243 = "000daaaa0243"; // port A24
        String mac244 = "000daaaa0244"; // port A24
        String mac245 = "000daaaa0245"; // port A24

        String mac251 = "000daaaa0251"; // port A25
        String mac252 = "000daaaa0252"; // port A25
        String mac253 = "000daaaa0253"; // port A25

        Integer nodeAId = 30;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement element = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();

        public OneBridgeCompleteTopology() {
            nodeA.setId(nodeAId);
            nodeA.setLocation(new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID));
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);


            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA2, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA3, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA4, mac4));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac231));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac232));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac233));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac234));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac241));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac242));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac243));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac244));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac245));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA25, mac251));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA25, mac252));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA25, mac253));



        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> links = domain.getSharedSegments();
            assertEquals(7, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                    assertEquals(link.getBridgePort(),shared.getDesignatedPort().getBridgePort());
                    if (link.getBridgePort() == portA1) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac1, link.getMacAddress());
                    } else if (link.getBridgePort() == portA2) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac2, link.getMacAddress());
                    } else if (link.getBridgePort() == portA3) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac3, link.getMacAddress());
                    } else if (link.getBridgePort() == portA4) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac4, link.getMacAddress());
                    } else if (link.getBridgePort() == portA23) {
                        final Set<String> macs = shared.getMacsOnSegment();
                        assertEquals(4, macs.size());
                        assertTrue(macs.contains(mac231));
                        assertTrue(macs.contains(mac232));
                        assertTrue(macs.contains(mac233));
                        assertTrue(macs.contains(mac234));
                    } else if (link.getBridgePort() == portA24) {
                        final Set<String> macs = shared.getMacsOnSegment();
                        assertEquals(5, macs.size());
                        assertTrue(macs.contains(mac241));
                        assertTrue(macs.contains(mac242));
                        assertTrue(macs.contains(mac243));
                        assertTrue(macs.contains(mac244));
                        assertTrue(macs.contains(mac245));
                    } else if (link.getBridgePort() == portA25) {
                        final Set<String> macs = shared.getMacsOnSegment();
                        assertEquals(3, macs.size());
                        assertTrue(macs.contains(mac251));
                        assertTrue(macs.contains(mac252));
                        assertTrue(macs.contains(mac253));
                    } else {
                        assertEquals(-1, 1);
                    }
                }
            }

        }        
    }
    
    class TwoConnectedBridgeTopology {
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;
        Integer portA5 = 5;
        Integer portAB = 16;
        Integer portBA = 24;
        Integer portB6 = 6;
        Integer portB7 = 7;
        Integer portB8 = 8;
        Integer portB9 = 9;

        String mac1 = "000daaaa0001"; // port A1 ---port BA
        String mac2 = "000daaaa0002"; // port A2 ---port BA
        String mac3 = "000daaaa0003"; // port A3 ---port BA
        String mac4 = "000daaaa0004"; // port A4 ---port BA
        String mac5 = "000daaaa0005"; // port A5 ---port BA
        String mac6 = "000daaaa0006"; // port AB ---port B6 
        String mac7 = "000daaaa0007"; // port AB ---port B7
        String mac8 = "000daaaa0008"; // port AB ---port B8
        String mac9 = "000daaaa0009"; // port AB ---port B9

        Integer nodeAId  = 1111;
        Integer nodeBId = 2222;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();

        public TwoConnectedBridgeTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA2, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA3, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA4, mac4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA5, mac5));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac6));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac7));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac8));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac9));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac3));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac4));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac5));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB6, mac6));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, mac7));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB8, mac8));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB9, mac9));

        }
        
        public void check(BroadcastDomain domain, boolean reverse) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();

            assertEquals(10, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeBridgeLink link=SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    if (reverse) {
                        assertEquals(nodeBId, link.getDesignatedNode().getId());
                        assertEquals(portBA,link.getDesignatedPort());
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portAB,link.getBridgePort());
                    	
                    } else {
                    	assertEquals(nodeAId, link.getDesignatedNode().getId());
                    	assertEquals(portAB,link.getDesignatedPort());
                    	assertEquals(nodeBId, link.getNode().getId());
                    	assertEquals(portBA,link.getBridgePort());
                    }
                } else {
                    assertEquals(1, shared.getMacsOnSegment().size());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    if (link.getMacAddress().equals(mac1)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA1,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac2)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA2,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac3)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA3,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac4)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA4,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac5)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA5,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac6)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB6,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac7)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB7,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac8)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB8,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac9)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB9,link.getBridgePort());
                    } else {
                        assertEquals(false, true);
                    }
                }
            }

        }
    }
    
    class TwoMergeBridgeTopology {
        Integer portA8 = 8;
        Integer portAB = 16;
        Integer portBA = 24;
        Integer portB6 = 6;

        String mac1 = "000daaaa0001"; // port AB ---port BA
        String mac2 = "000daaaa0002"; // port AB ---port BA
        String mac3 = "000daaaa0003"; // port AB ---port BA
        String mac4 = "000daaaa0004"; // port AB ---port BA
        String mac5 = "000daaaa0005"; // port AB ---port BA
        String mac6 = "000daaaa0006"; // port AB ---port B6
        String mac7 = "000daaaa0007"; // port AB ---port BA
        String mac8 = "000daaaa0008"; // port A8 ---port BA
        String mac9 = "000daaaa0009"; // port AB ---port BA
        Integer nodeAId  = 1111;
        Integer nodeBId  = 2222;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();

        public TwoMergeBridgeTopology() {

            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);


            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac5));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac6));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac7));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA8, mac8));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac9));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac3));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac4));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac5));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB6, mac6));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac7));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac8));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac9));


        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                assertEquals(false, shared.noMacsOnSegment());
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portAB,shared.getDesignatedPort().getBridgePort());
                    assertEquals(7, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(7, shared.getMacsOnSegment().size());
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac2));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac3));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac4));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac5));
                    assertEquals(false, shared.getMacsOnSegment().contains(mac6));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac7));
                    assertEquals(false, shared.getMacsOnSegment().contains(mac8));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac9));
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                        assertEquals(link.getNode().getId().intValue(),nodeAId.intValue());
                        assertEquals(portAB,link.getBridgePort());
                    }
                    BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                    assertEquals(portAB, dlink.getDesignatedPort());
                    assertEquals(nodeBId, dlink.getNode().getId());
                    assertEquals(portBA, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac6)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB6,shared.getDesignatedPort().getBridgePort());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(mac6, link.getMacAddress());
                    assertEquals(nodeBId,link.getNode().getId());
                    assertEquals(portB6,link.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac8)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA8,shared.getDesignatedPort().getBridgePort());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(mac8, link.getMacAddress());
                    assertEquals(nodeAId,link.getNode().getId());
                    assertEquals(portA8,link.getBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }
    }
    
    class TwoBridgeWithBackbonePortsTopology {
        Integer nodeAId = 1101;
        Integer nodeBId = 1102;
        Integer portA1 = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;

        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; // port AB ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2

        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();

        public TwoBridgeWithBackbonePortsTopology() {

            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);



            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA11));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA12));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAB));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB21));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB22));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA11));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA12));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macAB));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB21));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB22));


        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (Integer nodeid: domain.getBridgeNodesOnDomain()) {
                assertEquals(0, domain.getForwarders(nodeid).size());
            }
            for (SharedSegment shared: shsegs) {
                assertEquals(false, shared.noMacsOnSegment());
                if (shared.getMacsOnSegment().contains(macAB)) {
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(1, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(macAB, link.getMacAddress());
                        assertEquals(nodeA.getId().intValue(),link.getNode().getId().intValue());
                        assertEquals(portAB,link.getBridgePort());
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                    BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                    assertEquals(portAB, dlink.getDesignatedPort());
                    assertEquals(nodeBId, dlink.getNode().getId());
                    assertEquals(portBA, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(macA11)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA1,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA1, link.getBridgePort());
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macA11)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macA12)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else if (shared.getMacsOnSegment().contains(macB21)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB2,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB2, link.getBridgePort());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macB21)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macB22)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else {
                    assertEquals(false, true);
                }
            }


        }
    }
    
    class TwoBridgeWithBackbonePortsTopologyWithBridgeinBft {
        Integer nodeAId = 1101;
        Integer nodeBId = 1102;
        Integer portA1 = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;

        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; //         ---port BA 

        String macB21 = "000daa000b21"; //         ---port B2 
        String macB22 = "000daa000b22"; //         ---port B2
        String macB   = "bbbbbbbbbbbb"; // portAB
        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();

        public TwoBridgeWithBackbonePortsTopologyWithBridgeinBft() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA11));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA12));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA11));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA12));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macAB));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB21));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB22));
            
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().size() == 0) {
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, shared.getMacsOnSegment().size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(0, links.size());
                    BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeBId, dlink.getDesignatedNode().getId());
                    assertEquals(portBA, dlink.getDesignatedPort());
                    assertEquals(nodeAId, dlink.getNode().getId());
                    assertEquals(portAB, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(macA11)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA1,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA1, link.getBridgePort());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macA11)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macA12)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else if (shared.getMacsOnSegment().contains(macB21)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB2,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB2, link.getBridgePort());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macB21)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macB22)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else {
                    assertEquals(false, true);
                }
            }

        }
    }

}
