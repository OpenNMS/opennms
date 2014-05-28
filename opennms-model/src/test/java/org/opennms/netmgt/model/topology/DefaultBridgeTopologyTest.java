/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model.topology;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.topology.BridgeTopology.BridgeTopologyLink;
import org.opennms.netmgt.model.topology.BridgeTopology.BridgeTopologyPort;
import org.opennms.netmgt.model.topology.BridgeTopology.SwitchPort;
public class DefaultBridgeTopologyTest {
    
    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.model.topology", "DEBUG");
        MockLogAppender.setupLogging(p);
        
     }

	protected List<BridgeTopologyLink> printBridgeTopologyLinks(List<BridgeTopologyLink> paths) {
       for (final BridgeTopologyLink path: paths) {
    	   printBridgeTopologyLink(path);
       }
       return paths;
	}
	
	protected void printBridgeTopologyLink(BridgeTopologyLink path) {
       System.err.println("");
       System.err.println("------link-----");
       System.err.println("------bridge port-----");
       printBridgeTopologyPort(path.getBridgeTopologyPort());
       if (path.getDesignatebridgePort() != null ) {
	       System.err.println("------designated port-----");
    	   printBridgeTopologyPort(path.getDesignatebridgePort());
       }
       if (path.getLinkedSwitchPort() != null) {
	       System.err.println("------linked switch port-----");
    	   System.err.println(path.getLinkedSwitchPort());
       }
       System.err.println("macs on link: " + path.getMacs());
	}

	protected void printBridgeTopologyPort(BridgeTopologyPort port) {
		System.err.println("nodeid: " + port.getNodeid());
		System.err.println("bridgeport: " + port.getBridgePort());
		System.err.println("learned macs: " + port.getMacs());
	}
	
	protected void printSwitchPort(SwitchPort port) {
		System.err.println("nodeid: " + port.getNodeid());
		System.err.println("ifindex: " + port.getIfindex());
	}

		
	@Test
    public void testOneBridgeOnePortOneMac() throws Exception {

        Integer nodeA  = 10;

        LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        LinkableNode linkablenodeA = new LinkableNode(snmpnodeA, null);
        
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;
        Integer portA5 = 5;

        String mac1 = "000daaaa0001"; // learned on port A1
        String mac2 = "000daaaa0002"; // learned on port A2 
        String mac3 = "000daaaa0003"; // learned on port A2 
        String mac4 = "000daaaa0004"; // learned on port A2 
        String mac5 = "000daaaa0005"; // learned on port A2 

        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac1);
        linkablenodeA.addBridgeForwardingTableEntry(portA2, mac2);
        linkablenodeA.addBridgeForwardingTableEntry(portA3, mac3);
        linkablenodeA.addBridgeForwardingTableEntry(portA4, mac4);
        linkablenodeA.addBridgeForwardingTableEntry(portA5, mac5);

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(linkablenodeA);

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}

	@Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        Integer nodeA  = 20;
        
        Integer portA1 = 1;

        String mac1 = "000daaaa0001"; // port A1 
        String mac2 = "000daaaa0002"; // port A1
        String mac3 = "000daaaa0003"; // port A1
        String mac4 = "000daaaa0004"; // port A1

        LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        LinkableNode linkablenodeA = new LinkableNode(snmpnodeA, null);

        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac1);
        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac2);
        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac3);
        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac4);
        
        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(linkablenodeA);

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}

	@Test
	public void testOneBridgeComplete() throws Exception {

		Integer nodeA = 30;

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
		
        LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        LinkableNode linkablenodeA = new LinkableNode(snmpnodeA, null);

        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac1);
        linkablenodeA.addBridgeForwardingTableEntry(portA2, mac2);
        linkablenodeA.addBridgeForwardingTableEntry(portA3, mac3);
        linkablenodeA.addBridgeForwardingTableEntry(portA4, mac4);

        linkablenodeA.addBridgeForwardingTableEntry(portA23, mac231);
        linkablenodeA.addBridgeForwardingTableEntry(portA23, mac232);
        linkablenodeA.addBridgeForwardingTableEntry(portA23, mac233);
        linkablenodeA.addBridgeForwardingTableEntry(portA23, mac234);

        linkablenodeA.addBridgeForwardingTableEntry(portA24, mac241);
        linkablenodeA.addBridgeForwardingTableEntry(portA24, mac242);
        linkablenodeA.addBridgeForwardingTableEntry(portA24, mac243);
        linkablenodeA.addBridgeForwardingTableEntry(portA24, mac244);
        linkablenodeA.addBridgeForwardingTableEntry(portA24, mac245);

        linkablenodeA.addBridgeForwardingTableEntry(portA25, mac251);
        linkablenodeA.addBridgeForwardingTableEntry(portA25, mac252);
        linkablenodeA.addBridgeForwardingTableEntry(portA25, mac253);
        

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(linkablenodeA);

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}

	@Test
    public void testTwoConnectedBridgeTopology() throws Exception {

        Integer nodeA  = 1111;
        Integer nodeB  = 2222;
        
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

        LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        LinkableNode linkablenodeA = new LinkableNode(snmpnodeA, null);

        linkablenodeA.addBridgeForwardingTableEntry(portA1, mac1);
        linkablenodeA.addBridgeForwardingTableEntry(portA2, mac2);
        linkablenodeA.addBridgeForwardingTableEntry(portA3, mac3);
        linkablenodeA.addBridgeForwardingTableEntry(portA4, mac4);
        linkablenodeA.addBridgeForwardingTableEntry(portA5, mac5);

        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac6);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac7);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac8);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac9);
        
        LinkableSnmpNode snmpnodeB = new LinkableSnmpNode(nodeB, null, null, null);
        LinkableNode linkablenodeB = new LinkableNode(snmpnodeB, null);

        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac1);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac2);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac3);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac4);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac5);

        linkablenodeB.addBridgeForwardingTableEntry(portB6, mac6);
        linkablenodeB.addBridgeForwardingTableEntry(portB7, mac7);
        linkablenodeB.addBridgeForwardingTableEntry(portB8, mac8);
        linkablenodeB.addBridgeForwardingTableEntry(portB9, mac9);

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(linkablenodeA);
        bridgeTopology.addNodeToTopology(linkablenodeB);

        printBridgeTopologyLinks(bridgeTopology.getTopology());

  
    }

	@Test
    public void testTwoMergeBridgeTopology() throws Exception {

        Integer nodeA  = 1111;
        Integer nodeB  = 2222;
        
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

        LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        LinkableNode linkablenodeA = new LinkableNode(snmpnodeA, null);

        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac1);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac2);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac3);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac4);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac5);

        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac6);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac7);
        linkablenodeA.addBridgeForwardingTableEntry(portA8, mac8);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, mac9);
        
        LinkableSnmpNode snmpnodeB = new LinkableSnmpNode(nodeB, null, null, null);
        LinkableNode linkablenodeB = new LinkableNode(snmpnodeB, null);

        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac1);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac2);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac3);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac4);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac5);

        linkablenodeB.addBridgeForwardingTableEntry(portB6, mac6);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac7);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac8);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac9);

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(linkablenodeA);
        bridgeTopology.addNodeToTopology(linkablenodeB);

        printBridgeTopologyLinks(bridgeTopology.getTopology());

    }

	@Test 
	public void testTwoBridgeWithBackbonePorts() {
		Integer nodeA = 1101;
        Integer nodeB = 1102;
        
        Integer portA1 = 1;
		Integer portAB = 12;
		Integer portBA = 21;
		Integer portB2 = 2 ;
		
        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; // port AB ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2
        
        LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        LinkableNode linkablenodeA = new LinkableNode(snmpnodeA, null);

        linkablenodeA.addBridgeForwardingTableEntry(portA1, macA11);
        linkablenodeA.addBridgeForwardingTableEntry(portA1, macA12);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, macAB);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, macB21);
        linkablenodeA.addBridgeForwardingTableEntry(portAB, macB22);

        LinkableSnmpNode snmpnodeB = new LinkableSnmpNode(nodeB, null, null, null);
        LinkableNode linkablenodeB = new LinkableNode(snmpnodeB, null);

        linkablenodeB.addBridgeForwardingTableEntry(portBA, macA11);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, macA12);
        linkablenodeB.addBridgeForwardingTableEntry(portBA, macAB);
        linkablenodeB.addBridgeForwardingTableEntry(portB2, macB21);
        linkablenodeB.addBridgeForwardingTableEntry(portB2, macB22);

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(linkablenodeA);
        bridgeTopology.addNodeToTopology(linkablenodeB);

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}

	@Test
	public void testTwoConnectedBridgeTopologyAB() {
		ABCTopology topology = new ABCTopology();
        
		BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getA());
        bridgeTopology.addNodeToTopology(topology.getB());

        printBridgeTopologyLinks(bridgeTopology.getTopology());


	}

	@Test
	public void testTwoConnectedBridgeTopologyAC() {

		ABCTopology topology = new ABCTopology();
        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getA());
        bridgeTopology.addNodeToTopology(topology.getC());

        printBridgeTopologyLinks(bridgeTopology.getTopology());
	}

	@Test
	public void testTwoConnectedBridgeTopologyBC() {
		ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getB());
        bridgeTopology.addNodeToTopology(topology.getC());

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}
	
	@Test
	public void testTwoConnectedBridgeTopologyDE() {
		DEFTopology topology = new DEFTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getD());
        bridgeTopology.addNodeToTopology(topology.getD());

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}

	@Test
	public void testTwoConnectedBridgeTopologyDF() {
		DEFTopology topology = new DEFTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getD());
        bridgeTopology.addNodeToTopology(topology.getF());

        printBridgeTopologyLinks(bridgeTopology.getTopology());
	}

	@Test
	public void testTwoConnectedBridgeTopologyEF() {
		DEFTopology topology = new DEFTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getE());
        bridgeTopology.addNodeToTopology(topology.getF());

        printBridgeTopologyLinks(bridgeTopology.getTopology());
	}


	@Test
	public void testThreeConnectedBridgeTopologyABC() {
		ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getA());
        bridgeTopology.addNodeToTopology(topology.getB());
        bridgeTopology.addNodeToTopology(topology.getC());

        printBridgeTopologyLinks(bridgeTopology.getTopology());
	}

	@Test
	public void testThreeConnectedBridgeTopologyACB() {
		ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getA());
        bridgeTopology.addNodeToTopology(topology.getC());
        bridgeTopology.addNodeToTopology(topology.getB());

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}

	@Test
	public void testThreeConnectedBridgeTopologyDEF() {

		DEFTopology topology = new DEFTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getD());
        bridgeTopology.addNodeToTopology(topology.getE());
        bridgeTopology.addNodeToTopology(topology.getF());

        printBridgeTopologyLinks(bridgeTopology.getTopology());

	}
	
	@Test
	public void testThreeConnectedBridgeTopologyDFE() {
		DEFTopology topology = new DEFTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        
        bridgeTopology.addNodeToTopology(topology.getD());
        bridgeTopology.addNodeToTopology(topology.getF());
        bridgeTopology.addNodeToTopology(topology.getE());

        printBridgeTopologyLinks(bridgeTopology.getTopology());
	}

	private class DEFTopology {
		Integer nodeD = 104;
        Integer nodeE = 105;
        Integer nodeF = 106;

		Integer portD  = 4;
		Integer portDD = 40;
		Integer portE  = 5;
		Integer portEE = 50;
		Integer portF  = 6;
		Integer portFF = 60;
		
        String mac1 = "000daaaa0001"; // port D  ---port EE ---port FF
        String mac2 = "000daaaa0002"; // port D  ---port EE ---port FF
        String mac3 = "000daaaa0003"; // port DD ---port EE ---port F
        String mac4 = "000daaaa0004"; // port DD ---port EE ---port F
        String mac5 = "000daaaa0005"; // port DD ---port E ---port FF
        String mac6 = "000daaaa0006"; // port DD ---port E ---port FF

        LinkableNode linkablenodeD;
        LinkableNode linkablenodeE;
        LinkableNode linkablenodeF;
        
        public DEFTopology() {
	        LinkableSnmpNode snmpnodeD = new LinkableSnmpNode(nodeD, null, null, null);
	        linkablenodeD = new LinkableNode(snmpnodeD, null);
	        linkablenodeD.addBridgeForwardingTableEntry(portD, mac1);
	        linkablenodeD.addBridgeForwardingTableEntry(portD, mac2);
	        linkablenodeD.addBridgeForwardingTableEntry(portDD, mac3);
	        linkablenodeD.addBridgeForwardingTableEntry(portDD, mac4);
	        linkablenodeD.addBridgeForwardingTableEntry(portDD, mac5);
	        linkablenodeD.addBridgeForwardingTableEntry(portDD, mac6);
	
	        LinkableSnmpNode snmpnodeE = new LinkableSnmpNode(nodeE, null, null, null);
	        linkablenodeE = new LinkableNode(snmpnodeE, null);
	        linkablenodeE.addBridgeForwardingTableEntry(portEE, mac1);
	        linkablenodeE.addBridgeForwardingTableEntry(portEE, mac2);
	        linkablenodeE.addBridgeForwardingTableEntry(portEE, mac3);
	        linkablenodeE.addBridgeForwardingTableEntry(portEE, mac4);
	        linkablenodeE.addBridgeForwardingTableEntry(portE, mac5);
	        linkablenodeE.addBridgeForwardingTableEntry(portE, mac6);
	
	        LinkableSnmpNode snmpnodeF = new LinkableSnmpNode(nodeF, null, null, null);
	        linkablenodeF = new LinkableNode(snmpnodeF, null);
	        linkablenodeF.addBridgeForwardingTableEntry(portFF, mac1);
	        linkablenodeF.addBridgeForwardingTableEntry(portFF, mac2);
	        linkablenodeF.addBridgeForwardingTableEntry(portF, mac3);
	        linkablenodeF.addBridgeForwardingTableEntry(portF, mac4);
	        linkablenodeF.addBridgeForwardingTableEntry(portFF, mac5);
	        linkablenodeF.addBridgeForwardingTableEntry(portFF, mac6);
        }
        
        public LinkableNode getD() {
        	return linkablenodeD;
        }

        public LinkableNode getE() {
        	return linkablenodeE;
        }
        public LinkableNode getF() {
        	return linkablenodeF;
        }

		
	}
	private class ABCTopology {
		Integer nodeA = 101;
        Integer nodeB = 102;
        Integer nodeC = 103;

		Integer portA1 = 1;
		Integer portAB = 12;
		Integer portBA = 21;
		Integer portB  = 2;
		Integer portBC = 23;
		Integer portCB = 32;
		Integer portC  = 3;
		
        String mac1 = "000daaaa0001"; // port A  ---port BA ---port CB
        String mac2 = "000daaaa0002"; // port AB ---port B  ---port CB
        String mac3 = "000daaaa0003"; // port AB ---port BC ---port C

        LinkableNode linkablenodeA;
        LinkableNode linkablenodeB;
        LinkableNode linkablenodeC;
        
        public ABCTopology() {
        
        	LinkableSnmpNode snmpnodeA = new LinkableSnmpNode(nodeA, null, null, null);
        	linkablenodeA = new LinkableNode(snmpnodeA, null);
        	linkablenodeA.addBridgeForwardingTableEntry(portA1, mac1);
        	linkablenodeA.addBridgeForwardingTableEntry(portAB, mac2);
        	linkablenodeA.addBridgeForwardingTableEntry(portAB, mac3);

        	LinkableSnmpNode snmpnodeB = new LinkableSnmpNode(nodeB, null, null, null);
        	linkablenodeB = new LinkableNode(snmpnodeB, null);
	        linkablenodeB.addBridgeForwardingTableEntry(portBA, mac1);
	        linkablenodeB.addBridgeForwardingTableEntry(portB, mac2);
	        linkablenodeB.addBridgeForwardingTableEntry(portBC, mac3);

	        LinkableSnmpNode snmpnodeC = new LinkableSnmpNode(nodeC, null, null, null);
	        linkablenodeC = new LinkableNode(snmpnodeC, null);
	        linkablenodeC.addBridgeForwardingTableEntry(portCB, mac1);
	        linkablenodeC.addBridgeForwardingTableEntry(portCB, mac2);
	        linkablenodeC.addBridgeForwardingTableEntry(portC, mac3);
        }
        
        public LinkableNode getA() {
        	return linkablenodeA;
        }

        public LinkableNode getB() {
        	return linkablenodeB;
        }
        public LinkableNode getC() {
        	return linkablenodeC;
        }

	}

}
