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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.drools.FactHandle;
import org.junit.Before;
import org.junit.Test;
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

public class ImpactProgagationRulesTest extends CorrelationRulesTestCase {
	
	@Autowired
	private NCSComponentRepository m_repository;
	
	@Autowired
	private DistPollerDao m_distPollerDao;
	
	@Autowired
	private NodeDao m_nodeDao;

	private int m_pe1NodeId;
	
	private int m_pe2NodeId;

	private long m_pwCompId;
	
	private DroolsCorrelationEngine m_engine;
	
	private List<Object> m_anticipatedWorkingMemory = new ArrayList<Object>();
	
	@Before
	public void setUp() throws JAXBException, UnsupportedEncodingException {
		
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
		
		m_pwCompId = svc.getSubcomponent("NA-ServiceElement", "9876")
		                 .getSubcomponent("NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)")
		                 .getId();
		
		// Get engine
        m_engine = findEngineByName("impactPropagationRules");
        
//        // Create a Marshaller
//        JAXBContext context = JAXBContext.newInstance(NCSComponent.class);
//        Marshaller marshaller = context.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        
//        // save the output in a byte array
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        // marshall the output
//        marshaller.marshal(svc, out);
//
//        // verify its matches the expected results
//        byte[] utf8 = out.toByteArray();
//
//        String result = new String(utf8, "UTF-8");
//        
//        System.err.println(result);



	}
	
	@Test
    @DirtiesContext
    public void testSimpleDownUpCase() throws Exception {

		// 1. Assert empty workspace
        resetFacts();
        verifyFacts();
        
        
        // 2. verify Impact on ComponentDownEvent
        resetFacts();
        resetEvents();
        
        // component to request dependencies for
        Component c = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        Event downEvent = createVpnPwDownEvent(17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50");
        
        ComponentDownEvent cde = new ComponentDownEvent(c, downEvent);
        
        anticipateFacts(cde, new ComponentImpacted(c, cde), new DependenciesNeeded(c, cde), new ImpactEventSent(c, cde));
        
        anticipateEvent(createComponentImpactedEvent("ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17));
        
        // pretend to be a using rule that inserts the DependenciesNeeded fact
		insertFactAndFireRules(cde);
        
		verifyFacts();
		verifyEvents();
		
		
		// 3. Verify resolution and memory clean up on ComponentUpEvent
		resetFacts();
		resetEvents();
		
		anticipateEvent(createComponentResolvedEvent("ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17));
		
		// expect all facts to be resolved
		anticipateFacts();
		
        Event upEvent = createVpnPwUpEvent(17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50");
        ComponentUpEvent cue = new ComponentUpEvent(c, upEvent);
        
        insertFactAndFireRules(cue);
		
		verifyFacts();
		verifyEvents();
	
    }
	
	
	
	@Test
    @DirtiesContext
    public void testSimpleALLRulesPropagation() throws Exception {
		// 1. Assert empty workspace
        resetFacts();
        verifyFacts();
        
        
        // 2. verify Impact on ComponentDownEvent
        resetFacts();
        resetEvents();
        
        // component to request dependencies for
        Component c = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        Event downEvent = createVpnPwDownEvent(17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50");
        
        ComponentDownEvent cde = new ComponentDownEvent(c, downEvent);
        
        // this component depends on c
        Component parent = createComponent("ServiceElement", "NA-ServiceElement", "9876");
        
        DependsOn dep = new DependsOn( parent, c );
        ComponentImpacted componentImpacted = new ComponentImpacted(c, cde);
        ImpactEventSent eventSent = new ImpactEventSent( c, cde);
        
		anticipateFacts( dep, componentImpacted, eventSent, new ComponentImpacted( parent, cde ), new DependenciesNeeded(parent, cde), new ImpactEventSent(parent, cde));
        
        anticipateEvent(createComponentImpactedEvent("ServiceElement", "PE2,SE1", "NA-SvcElement", "9876", 17));
        
        // Insert facts and fire rules
		FactHandle impactHandle = m_engine.getWorkingMemory().insert( componentImpacted );
		FactHandle depHandle = m_engine.getWorkingMemory().insert( dep );
		FactHandle eventSentHandle = m_engine.getWorkingMemory().insert( eventSent );
		m_engine.getWorkingMemory().fireAllRules();
        
        // pretend to be a using rule that inserts the DependenciesNeeded fact
		verifyFacts();
		verifyEvents();
		
		
		// 3. Verify resolution and memory clean up on ComponentUpEvent
		resetFacts();
		resetEvents();
		
		//anticipateEvent(createComponentResolvedEvent("ServiceElementComponent", "jnxVpnPw-vcid(50)", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)", 17));
		anticipateEvent(createComponentResolvedEvent("ServiceElement", "PE2,SE1", "NA-SvcElement", "9876", 17));
		
		// expect all facts to be resolved
		anticipateFacts();
		
        Event upEvent = createVpnPwUpEvent(18, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50");
        ComponentUpEvent cue = new ComponentUpEvent(c, upEvent);
        
        m_engine.getWorkingMemory().retract(impactHandle);
        m_engine.getWorkingMemory().retract(depHandle);
        m_engine.getWorkingMemory().retract(eventSentHandle);
        m_engine.getWorkingMemory().insert(new ComponentEventResolved(cde, cue) );
        
        m_engine.getWorkingMemory().fireAllRules();
        
       // insertFactAndFireRules(cue);
		
		verifyFacts();
		verifyEvents();
		

	}
	
	// add test for two different outages on the same component
    
	
	private Component createComponent(String type, String foreignSource, String foreignId) {
		NCSComponent ncsComp = m_repository.findByTypeAndForeignIdentity(type, foreignSource, foreignId);
		return new Component(ncsComp);
	}
	
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

	@SuppressWarnings("unused")
    private Event createMplsLspPathDownEvent( int dbId, int nodeid, String ipaddr, String lspname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
        
        event.setDbid(dbId);
		return event;
    }
    
	@SuppressWarnings("unused")
    private Event createMplsLspPathUpEvent( int dbId, int nodeid, String ipaddr, String lspname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp", "Drools")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
        event.setDbid(dbId);
		return event;
    }


    private Event createVpnPwDownEvent( int dbId, int nodeid, String ipaddr, String pwtype, String pwname ) {
		
		Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown", "Test")
				.setNodeid(nodeid)
				.setInterface( addr( ipaddr ) )
				.addParam("jnxVpnPwVpnType", pwtype )
				.addParam("jnxVpnPwVpnName", pwname )
				.getEvent();
		event.setDbid(dbId);
		return event;
	}

    private Event createVpnPwUpEvent( int dbId, int nodeid, String ipaddr, String pwtype, String pwname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("jnxVpnPwVpnType", pwtype )
                .addParam("jnxVpnPwVpnName", pwname )
                .getEvent();
        event.setDbid(dbId);
		return event;
    }

    private void resetFacts() {
		m_anticipatedWorkingMemory.clear();
	}
    
	private void anticipateFacts(Object... facts) {
		m_anticipatedWorkingMemory.addAll(Arrays.asList(facts));
	}
	
	private FactHandle insertFactAndFireRules(Object fact) {
		FactHandle handle = m_engine.getWorkingMemory().insert( fact );
        m_engine.getWorkingMemory().fireAllRules();
		return handle;
	}
	
	private void retractFactAndFireRules(FactHandle fact) {
		m_engine.getWorkingMemory().retract( fact );
		m_engine.getWorkingMemory().fireAllRules();
	}
    
	
	private void verifyFacts() {
		List<Object> memObjects = m_engine.getMemoryObjects();
		
		String memContents = memObjects.toString();
		
		for(Object anticipated : m_anticipatedWorkingMemory) {
			assertTrue("Expected "+anticipated+" in memory but memory was "+memContents, memObjects.contains(anticipated));
			memObjects.remove(anticipated);
		}
		
		assertEquals("Unexpected objects in working memory " + memObjects, 0, memObjects.size());
		
	}
	
	private void resetEvents() {
		getAnticipator().reset();
	}
    
	private void anticipateEvent(Event... events) {
		for(Event event : events) {
			getAnticipator().anticipateEvent(event);
		}
	}
	
	private void verifyEvents() {
		getAnticipator().verifyAnticipated();
	}
    
}
