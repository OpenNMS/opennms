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

package org.opennms.netmgt.correlation.ncs;

import static org.opennms.core.utils.InetAddressUtils.addr;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.drools.DroolsCorrelationEngine;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

public class MonolithicDependencyRulesTest extends CorrelationRulesTestCase {
	
	@Autowired
	NCSComponentRepository m_repository;
	
	@Autowired
	DistPollerDao m_distPollerDao;
	
	@Autowired
	NodeDao m_nodeDao;

	int m_pe1NodeId;
	
	int m_pe2NodeId;

	@Before
	public void setUp() {
		
		OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
		
		m_distPollerDao.save(distPoller);
		
		
		NetworkBuilder bldr = new NetworkBuilder(distPoller);
		bldr.addNode("PE1").setForeignSource("space").setForeignId("1111-PE1");
		
		m_nodeDao.save(bldr.getCurrentNode());
		
		m_pe1NodeId = bldr.getCurrentNode().getId();
		
		bldr.addNode("PE2").setForeignSource("space").setForeignId("2222-PE2");
		
		m_nodeDao.save(bldr.getCurrentNode());
		
		m_pe2NodeId = bldr.getCurrentNode().getId();
		
		NCSComponent svc = new NCSBuilder("Service", "NA-Service", "123")
		.setName("CokeP2P")
		.pushComponent("ServiceElement", "NA-ServiceElement", "8765")
			.setName("PE1,SE1")
			.setNodeIdentity("space", "1111-PE1")
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,jnxVpnIf")
				.setName("jnxVpnIf")
				.setNodeIdentity("space", "1111-PE1")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
				.setAttribute("jnxVpnIfVpnType", "5")
				.setAttribute("jnxVpnIfVpnName", "ge-1/0/2.50")
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,link")
					.setName("link")
					.setNodeIdentity("space", "1111-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
					.setAttribute("linkName", "ge-1/0/2")
				.popComponent()
			.popComponent()
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)")
				.setName("jnxVpnPw-vcid(50)")
				.setNodeIdentity("space", "1111-PE1")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
				.setAttribute("jnxVpnPwVpnType", "5")
				.setAttribute("jnxVpnPwVpnName", "ge-1/0/2.50")
				.setDependenciesRequired(DependencyRequirements.ANY)
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,lspA-PE1-PE2")
					.setName("lspA-PE1-PE2")
					.setNodeIdentity("space", "1111-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspA-PE1-PE2")
				.popComponent()
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,lspB-PE1-PE2")
					.setName("lspB-PE1-PE2")
					.setNodeIdentity("space", "1111-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspB-PE1-PE2")
				.popComponent()
			.popComponent()
		.popComponent()
		.pushComponent("ServiceElement", "NA-ServiceElement", "9876")
			.setName("PE2,SE1")
			.setNodeIdentity("space", "2222-PE2")
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnIf")
				.setName("jnxVpnIf")
				.setNodeIdentity("space", "2222-PE2")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
				.setAttribute("jnxVpnIfVpnType", "5")
				.setAttribute("jnxVpnIfVpnName", "ge-3/1/4.50")
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,link")
					.setName("link")
					.setNodeIdentity("space", "2222-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
					.setAttribute("linkName", "ge-3/1/4")
				.popComponent()
			.popComponent()
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)")
				.setName("jnxVpnPw-vcid(50)")
				.setNodeIdentity("space", "2222-PE2")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
				.setAttribute("jnxVpnPwVpnType", "5")
				.setAttribute("jnxVpnPwVpnName", "ge-3/1/4.50")
				.setDependenciesRequired(DependencyRequirements.ANY)
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,lspA-PE2-PE1")
					.setName("lspA-PE2-PE1")
					.setNodeIdentity("space", "2222-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspA-PE2-PE1")
				.popComponent()
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,lspB-PE2-PE1")
					.setName("lspB-PE2-PE1")
					.setNodeIdentity("space", "2222-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspB-PE2-PE1")
				.popComponent()
			.popComponent()
		.popComponent()
		.get();
		
		m_repository.save(svc);

	}
    
	
	@Test
    @DirtiesContext
    @Ignore("Non Deterministic!!!")
    public void testDependencyAnyRules() throws Exception {
        
        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("monolithicDependencyRules");
        
        // Anticipate component lspA down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( "ServiceElementComponent", "lspA-PE1-PE2", "NA-SvcElemComp", "8765,lspA-PE1-PE2", 17 ) );
        // Generate down event
		Event event = createMplsLspPathDownEvent( m_pe1NodeId, "10.1.1.1", "lspA-PE1-PE2" );
		event.setDbid(17);
		System.err.println("SENDING MplsLspPathDown on LspA EVENT!!");
		engine.correlate( event );
		// Check down event
		getAnticipator().verifyAnticipated();
		
		
		// Anticipate component lspB down event
		// Parent should go down too
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( "ServiceElementComponent", "lspB-PE1-PE2", "NA-SvcElemComp", "8765,lspB-PE1-PE2", 18 ) );
        anticipate(  createComponentImpactedEvent( "ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)", 18 ) );
        anticipate(  createComponentImpactedEvent( "ServiceElement", "PE1,SE1", "NA-ServiceElement", "8765", 18 ) );
        anticipate(  createComponentImpactedEvent( "Service", "CokeP2P", "NA-Service", "123", 18) );
        
        //anticipate(  createComponentImpactedEvent( "Service", "NA-Service", "123", 17 ) );
        // Generate down event
        event = createMplsLspPathDownEvent( m_pe1NodeId, "10.1.1.1", "lspB-PE1-PE2" );
        event.setDbid(18);
        System.err.println("SENDING MplsLspPathDown on LspB EVENT!!");
        engine.correlate( event );
        // Check down event
        getAnticipator().verifyAnticipated();
		
        
		// Anticipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( "ServiceElementComponent", "lspA-PE1-PE2", "NA-SvcElemComp", "8765,lspA-PE1-PE2", 18 ) );
        anticipate(  createComponentResolvedEvent( "ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)", 18 ) );
        anticipate(  createComponentResolvedEvent( "ServiceElement", "PE1,SE1", "NA-ServiceElement", "8765", 18 ) );
        anticipate(  createComponentResolvedEvent( "Service", "CokeP2P", "NA-Service", "123", 18) );
        
        //Generate up event
        event = createMplsLspPathUpEvent( m_pe1NodeId, "10.1.1.1", "lspA-PE1-PE2" );
        event.setDbid(17);
        System.err.println("SENDING MplsLspPathUp on LspA EVENT!!");
        engine.correlate( event );
        
        // Check up event
        getAnticipator().verifyAnticipated();	
        
	
    }
    

	@Test
    @DirtiesContext
    public void testSimpleUpDownCase() throws Exception {
		
        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("monolithicDependencyRules");
		
        
        // Antecipate down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( "ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17 ) );
        anticipate(  createComponentImpactedEvent( "ServiceElement", "PE2,SE1", "NA-ServiceElement", "9876", 17 ) );
        anticipate(  createComponentImpactedEvent( "Service", "CokeP2P", "NA-Service", "123", 17 ) );
		
		// Generate down event
		Event event = createVpnPwDownEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
		event.setDbid(17);
		System.err.println("SENDING VpnPwDown EVENT!!");
		engine.correlate( event );
		
		// Check down event
		getAnticipator().verifyAnticipated();
		
		// Generate additional down event - nothing should happen
//		getAnticipator().reset();
//        event = createVpnPwDownEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
//        event.setDbid(18);
//        System.err.println("SENDING VpnPwDown EVENT!!");
//        engine.correlate( event );
//        getAnticipator().verifyAnticipated();
		
		// Anticipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( "ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17 ) );
        anticipate(  createComponentResolvedEvent( "ServiceElement", "PE2,SE1", "NA-ServiceElement", "9876", 17 ) );
        anticipate(  createComponentResolvedEvent( "Service", "CokeP2P", "NA-Service", "123", 17 ) );
        
        // Generate up event
        event = createVpnPwUpEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
        event.setDbid(17);
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( event );
        
        // Check up event
        getAnticipator().verifyAnticipated();	
	
    }
    
    @Test
    @DirtiesContext
    @Ignore("not yet implemented")
    public void testMultipleDownAndSingleUpCase() throws Exception {
        
        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("monolithicDependencyRules");
        
        // Anticipate down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( "ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17 ) );
        anticipate(  createComponentImpactedEvent( "ServiceElement", "PE2,SE1", "NA-ServiceElement", "9876", 17 ) );
        anticipate(  createComponentImpactedEvent( "Service", "CokeP2P", "NA-Service", "123", 17 ) );
		
		// Generate down event
		Event event = createVpnPwDownEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
		event.setDbid(17);
		System.err.println("SENDING VpnPwDown EVENT!!");
		engine.correlate( event );
		
		// Check down event
		getAnticipator().verifyAnticipated();
		
		// Generate additional down event - nothing should happen
		getAnticipator().reset();
        event = createVpnPwDownEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
        event.setDbid(18);
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( event );
        getAnticipator().verifyAnticipated();
		
		// Anticipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( "ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17 ) );
        anticipate(  createComponentResolvedEvent( "ServiceElement", "PE2,SE1", "NA-ServiceElement", "9876", 17 ) );
        anticipate(  createComponentResolvedEvent( "Service", "CokeP2P", "NA-Service", "123", 17 ) );
        
        // Generate up event
        event = createVpnPwUpEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
        event.setDbid(17);
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( event );
        
        // Check up event
        getAnticipator().verifyAnticipated();	
	
    }
    
    // dependencies must be loaded when needed by propagation rules
    // loaded deps needed by multiple events should not load more than once
    // deps no longer needed by one event should remain loaded if need by others
    // deps no longer needed by any event should be unloaded

    // propagate outages to 'dependsOn' parents
    // propagate outages to 'dependsOnAny' parents when ALL children are down
    // resolve outages in 'dependsOn' parents when dependsOn children are resolved
    // resolve outage in 'dependsOnAny' parents when ANY child is resolved

    // map various events to outages and resolutions
    // ignore duplicate cause events
    // ignore duplicate resolution events
    
    private Event createMplsLspPathDownEvent( int nodeid, String ipaddr, String lspname ) {
        
        return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
    }
    
    private Event createMplsLspPathUpEvent( int nodeid, String ipaddr, String lspname ) {
        
        return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp", "Drools")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
    }


    private Event createVpnPwDownEvent( int nodeid, String ipaddr, String pwtype, String pwname ) {
		
		return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown", "Test")
				.setNodeid(nodeid)
				.setInterface( addr( ipaddr ) )
				.addParam("jnxVpnPwVpnType", pwtype )
				.addParam("jnxVpnPwVpnName", pwname )
				.getEvent();
	}

    private Event createVpnPwUpEvent( int nodeid, String ipaddr, String pwtype, String pwname ) {
        
        return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("jnxVpnPwVpnType", pwtype )
                .addParam("jnxVpnPwVpnName", pwname )
                .getEvent();
    }

    // Currently unused
//    private Event createRootCauseEvent(int symptom, int cause) {
//        return new EventBuilder(createNodeEvent("rootCauseEvent", cause)).getEvent();
//    }
	
	private Event createComponentImpactedEvent( String type, String name, String foreignSource, String foreignId, int cause ) {
        
        return new EventBuilder("uei.opennms.org/internal/ncs/componentImpacted", "Component Correlator")
        .addParam("componentType", type )
        .addParam("componentName", name )
        .addParam("componentForeignSource", foreignSource )
        .addParam("componentForeignId", foreignId )
        .addParam("cause", cause )
        .getEvent();
    }
	
	private Event createComponentResolvedEvent(String type, String name, String foreignSource, String foreignId, int cause) {
        return new EventBuilder("uei.opennms.org/internal/ncs/componentResolved", "Component Correlator")
        .addParam("componentType", type )
        .addParam("componentName", name)
        .addParam("componentForeignSource", foreignSource )
        .addParam("componentForeignId", foreignId )
        .addParam("cause", cause )
        .getEvent();
    }


    public Event createNodeDownEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_DOWN_EVENT_UEI, nodeid);
    }
    
    public Event createNodeUpEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_UP_EVENT_UEI, nodeid);
    }
    
    public Event createNodeLostServiceEvent(int nodeid, String ipAddr, String svcName)
    {
    	return createSvcEvent("uei.opennms.org/nodes/nodeLostService", nodeid, ipAddr, svcName);
    }
    
    public Event createNodeRegainedServiceEvent(int nodeid, String ipAddr, String svcName)
    {
    	return createSvcEvent("uei.opennms.org/nodes/nodeRegainedService", nodeid, ipAddr, svcName);
    }
    
    private Event createSvcEvent(String uei, int nodeid, String ipaddr, String svcName)
    {
    	return new EventBuilder(uei, "Test")
    		.setNodeid(nodeid)
    		.setInterface( addr( ipaddr ) )
    		.setService( svcName )
    		.getEvent();
    		
    }

    private Event createNodeEvent(String uei, int nodeid) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeid)
            .getEvent();
    }
    



}
