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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.FactHandle;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.correlation.drools.DroolsCorrelationEngine;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

public class DependencyLoadingRulesTest extends CorrelationRulesTestCase {
	
	@Autowired
	private NCSComponentRepository m_repository;
	
	@Autowired
	private DistPollerDao m_distPollerDao;
	
	@Autowired
	private NodeDao m_nodeDao;

	private DroolsCorrelationEngine m_engine;
	
	private List<Object> m_anticipatedWorkingMemory = new ArrayList<Object>();
	
	@Before
	public void setUp() {
		
		OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
		
		m_distPollerDao.save(distPoller);
		
		
		NetworkBuilder bldr = new NetworkBuilder(distPoller);
		bldr.addNode("PE1").setForeignSource("space").setForeignId("1111-PE1");
		
		m_nodeDao.save(bldr.getCurrentNode());
		
		//m_pe1NodeId = bldr.getCurrentNode().getId();
		
		bldr.addNode("PE2").setForeignSource("space").setForeignId("2222-PE2");
		
		m_nodeDao.save(bldr.getCurrentNode());
		
	//	m_pe2NodeId = bldr.getCurrentNode().getId();
		
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
		
//		m_pwCompId = svc.getSubcomponent("NA-ServiceElement", "9876")
//		                 .getSubcomponent("NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)")
//		                 .getId();
		
		// Get engine
        m_engine = findEngineByName("dependencyLoadingRules");


	}
	
	@Test
	@DirtiesContext
	public void testSingleRequestToLoadDependenciesOfTypeAll() {
        
        resetFacts();
        
        // nothing anticipated
        verifyFacts();
        
        resetFacts();
        
        // component to request dependencies for
        Component b = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        DependenciesNeeded dependenciesNeeded = new DependenciesNeeded(b, "requestor1");

        // this component depends on b
        Component a = createComponent("ServiceElement", "NA-ServiceElement", "9876");

        anticipateFacts(b, dependenciesNeeded, new DependsOn(a, b));
        
        // pretend to be a using rule that inserts the DependenciesNeeded fact
		insertFactAndFireRules(dependenciesNeeded);
        
		verifyFacts();
        
	}

	@Test
	@DirtiesContext
	public void testSingleRequestToLoadDependenciesOfTypeAllAndWithdrawn() {
		
		resetFacts();

        // expect empty memory to start with
        verifyFacts();
        
        resetFacts();
        
        // component to request dependencies for
        Component b = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        DependenciesNeeded dependenciesNeeded = new DependenciesNeeded(b, "requestor1");

        // this component depends on b
        Component a = createComponent("ServiceElement", "NA-ServiceElement", "9876");
        
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));

        // pretend to be a using rule that inserts the DependenciesNeeded fact
		FactHandle depsNeededHandle = insertFactAndFireRules(dependenciesNeeded);
        
		verifyFacts();
		
		resetFacts();
		
		// simulate other rules retracting the dep
		retractFactAndFireRules(depsNeededHandle);

		// nothing anticipated... everything cleaned up
		verifyFacts();
        
	}
	
	@Test
	@DirtiesContext
	public void testSingleRequestToLoadDependenciesOfTypeAllAndWithdrawnTwice() {
		
		resetFacts();

        // expect empty memory to start with
        verifyFacts();
        
        resetFacts();
        
        // component to request dependencies for
        Component b = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        DependenciesNeeded dependenciesNeeded = new DependenciesNeeded(b, "requestor1");

        // this component depends on b
        Component a = createComponent("ServiceElement", "NA-ServiceElement", "9876");
        
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));

        // pretend to be a using rule that inserts the DependenciesNeeded fact
		FactHandle depsNeededHandle = insertFactAndFireRules(dependenciesNeeded);
        
		verifyFacts();
		
		resetFacts();
		
		// simulate other rules retracting the dep
		retractFactAndFireRules(depsNeededHandle);

		// nothing anticipated... everything cleaned up
		verifyFacts();
		
		resetFacts();
		
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));
        
		depsNeededHandle = insertFactAndFireRules(dependenciesNeeded);
        
        verifyFacts();
        
        // Clean up facts
        resetFacts();
        
        // Expecting empty list
        anticipateFacts();
        
		// simulate retracting the dep
		retractFactAndFireRules(depsNeededHandle);
        
        verifyFacts();
		
		
        
	}
    

	@Test
	@DirtiesContext
	public void testMultipleRequestsToLoadDependenciesOfTypeAll() {
		
		resetFacts();
		// verify empty memory
		verifyFacts();

        resetFacts();
        
        // component to request dependencies for
        Component b = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        DependenciesNeeded dependenciesNeeded = new DependenciesNeeded(b, "requestor1");

        // this component depends on b
        Component a = createComponent("ServiceElement", "NA-ServiceElement", "9876");
        
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));

        // pretend to be a using rule that inserts the DependenciesNeeded fact
		insertFactAndFireRules(dependenciesNeeded);
        
		verifyFacts();
        
		resetFacts();
		
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));
        
		DependenciesNeeded dependenciesNeeded2 = new DependenciesNeeded(b, "requestor2");
		
		// don't expect any further dependencies to be added
		anticipateFacts(dependenciesNeeded2);
		
		insertFactAndFireRules(dependenciesNeeded2);

		verifyFacts();
	}
    

	@Test
	@DirtiesContext
	public void testMultipleRequestsToLoadDependenciesOfTypeAllAndOneWithdrawn() {

		resetFacts();
		// verify empty memory
		verifyFacts();

        resetFacts();
        
        // component to request dependencies for
        Component b = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        DependenciesNeeded dependenciesNeeded = new DependenciesNeeded(b, "requestor1");

        // this component depends on b
        Component a = createComponent("ServiceElement", "NA-ServiceElement", "9876");
        
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));

        // pretend to be a using rule that inserts the DependenciesNeeded fact
		FactHandle depsNeededHandle = insertFactAndFireRules(dependenciesNeeded);
        
		verifyFacts();
        
		resetFacts();
		
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));
        
		DependenciesNeeded dependenciesNeeded2 = new DependenciesNeeded(b, "requestor2");
		
		// don't expect any further dependencies to be added
		anticipateFacts(dependenciesNeeded2);
		
		insertFactAndFireRules(dependenciesNeeded2);

		verifyFacts();
		
		resetFacts();
		
        anticipateFacts(dependenciesNeeded2, b, new DependsOn(a, b));

        retractFactAndFireRules(depsNeededHandle);
		
		verifyFacts();
	}
    

	@Test
	@DirtiesContext
	public void testMultipleRequestsToLoadDependenciesOfTypeAllAndAllWithdrawn() {

		resetFacts();
		// verify empty memory
		verifyFacts();

        resetFacts();
        
        // component to request dependencies for
        Component b = createComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)");
        DependenciesNeeded dependenciesNeeded = new DependenciesNeeded(b, "requestor1");

        // this component depends on b
        Component a = createComponent("ServiceElement", "NA-ServiceElement", "9876");
        
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));

        // pretend to be a using rule that inserts the DependenciesNeeded fact
		FactHandle depsNeededHandle = insertFactAndFireRules(dependenciesNeeded);
        
		verifyFacts();
        
		resetFacts();
		
        anticipateFacts(dependenciesNeeded, b, new DependsOn(a, b));
        
		DependenciesNeeded dependenciesNeeded2 = new DependenciesNeeded(b, "requestor2");
		
		// don't expect any further dependencies to be added
		anticipateFacts(dependenciesNeeded2);
		
		FactHandle depsNeeded2Handle = insertFactAndFireRules(dependenciesNeeded2);

		verifyFacts();
		
		resetFacts();
		
        anticipateFacts(dependenciesNeeded2, b, new DependsOn(a, b));

        retractFactAndFireRules(depsNeededHandle);
		
		verifyFacts();
		
		resetFacts();
		
		retractFactAndFireRules(depsNeeded2Handle);

		// expect everything to be clean up
		verifyFacts();

	}
    // dependencies must be loaded when needed by propagation rules
    // loaded deps needed by multiple events should not load more than once
    // deps no longer needed by one event should remain loaded if need by others
    // deps no longer needed by any event should be unloaded


	// two kinds of needs... DependentsNeeded meaning I need to ensure the things that depend on
    // component A are loaded
    
    // also need a DependenciesNeeded meaning I need to ensure that the things that component A
    // depends on are loaded.
    
    // to imagine the use cases...
    // 1.  component A is down so ensure that DependentsNeeded is asserted for that component so
    // that all the necessary components are loaded.  After this is asserted then other rules
    // based on DependsOn with that component as a target can fire
	
	private Component createComponent(String type, String foreignSource, String foreignId) {
		NCSComponent ncsComp = m_repository.findByTypeAndForeignIdentity(type, foreignSource, foreignId);
		return new Component(ncsComp);
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
    
    
}
