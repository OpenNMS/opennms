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

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.correlation.drools.DroolsCorrelationEngine;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.ncs.AbstractNCSComponentVisitor;
import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

public class DependencyRulesTest extends CorrelationRulesTestCase {
    
    private static interface Predicate<T> {
        public boolean accept(T t);
    }
    
    private static interface Transform<A, B> {
        public B transform(A a);
    }

    @Autowired
    NCSComponentRepository m_repository;

    @Autowired
    DistPollerDao m_distPollerDao;

    @Autowired
    NodeDao m_nodeDao;

    int m_pe1NodeId;

    int m_pe2NodeId;

    private NCSComponent m_svc;

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

        m_svc = new NCSBuilder("Service", "NA-Service", "123")
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

        m_repository.save(m_svc);

    }
    

    @Test
    @DirtiesContext
    @Ignore("Non Deterministic!!!")
    public void testDependencyAnyRules() throws Exception {
        
        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");
        
        // Anticipate component lspA down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,lspA-PE1-PE2"), 17 ) );

        // Generate down event
        System.err.println("SENDING MplsLspPathDown on LspA EVENT!!");
        engine.correlate( createMplsLspPathDownEvent( 17, m_pe1NodeId, "10.1.1.1", "lspA-PE1-PE2" ) );

        // Check lspA component impacted
        getAnticipator().verifyAnticipated();

        // Anticipate component lspB down event
        // Parent should go down too
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,lspB-PE1-PE2"), 18 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)"), 18 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 18 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 18) );
        
        // Generate down event
        System.err.println("SENDING MplsLspPathDown on LspB EVENT!!");
        engine.correlate( createMplsLspPathDownEvent( 18, m_pe1NodeId, "10.1.1.1", "lspB-PE1-PE2" ) );

        // verify components were impacted
        getAnticipator().verifyAnticipated();

        // Anticipate impacted resolved when we send up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,lspA-PE1-PE2"), 18 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)"), 18 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 18 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 18) );

        // Generate up event
        System.err.println("SENDING MplsLspPathUp on LspA EVENT!!");
        engine.correlate( createMplsLspPathUpEvent( 19, m_pe1NodeId, "10.1.1.1", "lspA-PE1-PE2" ) );

        // verify components are resolved
        getAnticipator().verifyAnticipated();	


    }


    @Test
    @DirtiesContext
    public void testSimpleDownUpCase() throws Exception {

        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");


        // Antecipate down event
        getAnticipator().reset();
        
        anticipate( transform( findPathToSubcomponent(m_svc,  "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)" ), toComponentImpactedEvent(17) ) );

        // Generate down event
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) ); //  "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)"

        // Check down event
        getAnticipator().verifyAnticipated();


        // Anticipate up event
        getAnticipator().reset();
        anticipate( transform( findPathToSubcomponent(m_svc,  "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)" ), toComponentResolvedEvent(17) ) );
        
        // Generate up event
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( createVpnPwUpEvent( 19, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );
        
        // Check up event
        getAnticipator().verifyAnticipated();

        // Memory should be clean!
        assertEquals( 0, engine.getMemorySize() );

    }

    @Test
    @DirtiesContext
    public void testTwoCauseDownUpCase() throws Exception {

        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");

        // Antecipate down event
        getAnticipator().reset();
        
        anticipate( transform( findPathToSubcomponent(m_svc,  "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)" ), toComponentImpactedEvent(17) ) );

        // Generate down event
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) ); //  "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)"

        // Check down event
        getAnticipator().verifyAnticipated();
        getAnticipator().reset();

        // Second outage
        anticipate( transform( singleton( findSubcomponent(m_svc, "NA-SvcElemComp", "9876,jnxVpnIf") ), toComponentImpactedEvent(18) ) );
        
        System.err.println("SENDING VpnIfDown EVENT!!");
        engine.correlate( createVpnIfDownEvent(18, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );
        
        
        getAnticipator().verifyAnticipated();
        getAnticipator().reset();

        // expect only the resolved subelement to come back up
        anticipate( transform( singleton( findSubcomponent(m_svc, "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)") ), toComponentResolvedEvent(17) ) );
        
        // Generate up event
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( createVpnPwUpEvent( 19, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );
        
        // Check up event
        getAnticipator().verifyAnticipated();
        getAnticipator().reset();

        anticipate( transform( findPathToSubcomponent(m_svc,  "NA-SvcElemComp", "9876,jnxVpnIf" ), toComponentResolvedEvent(18) ) );
        
        System.err.println("SENDING VpnIfUp EVENT!!");
        engine.correlate( createVpnIfUpEvent(20, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );
        
        getAnticipator().verifyAnticipated();
        
        // Memory should be clean!
        assertEquals( 0, engine.getMemorySize() );

    }
    
    
 // Test two down and two up events where the initial event that caused the propagation is
    // resolved but a new component goes down. The expectation is that the service should remain
    // down instead of going back up and then down again. This was reproduced with the following :
    //echo "Send IfDown and PwDown"
    //send-event.pl --parm "jnxVpnIfVpnName ge-1/3/2.1" --parm "jnxVpnIfVpnType 5" -n 5 uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown
    //send-event.pl  --parm "jnxVpnPwVpnName ge-1/3/2.1" --parm "jnxVpnPwVpnType 5" -n 5 uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown
    //sleep 10
    //echo "Send IfUp and PwUp in same order this causes issue with clearing"
    //send-event.pl --parm "jnxVpnIfVpnName ge-1/3/2.1" --parm "jnxVpnIfVpnType 5" -n 5 uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp
    //send-event.pl --parm "jnxVpnPwVpnName ge-1/3/2.1" --parm "jnxVpnPwVpnType 5" -n 5 uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp
    @Test
    @DirtiesContext
    public void testTwoOutagesTwoResolutionsCase() throws Exception {
        // Test what happens to the parent when there are two children impacted one is resolved

        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");

        // Anticipate 1st down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnIf"), 17 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 17 ) );
        anticipate(  createComponentImpactedEvent(findSubcomponent (m_svc, "NA-Service", "123"), 17 ) );

        // Generate vpn if down event
        System.err.println("SENDING VpnIfDown EVENT!!");
        engine.correlate( createVpnIfDownEvent( 17, m_pe1NodeId, "10.1.1.1", "5", "ge-1/0/2.50" ) );

        // Check down event
        getAnticipator().verifyAnticipated();

        // Anticipate 2nd down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)"), 18 ) );
        //anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 18 ) );

        // Should we get this?
        //anticipate(  createComponentImpactedEvent( "Service", "CokeP2P", "NA-Service", "123", 18 ) );

        // Generate vpn down event
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 18, m_pe1NodeId, "10.1.1.1", "5", "ge-1/0/2.50" ) );

        // Check 2nd down event
        getAnticipator().verifyAnticipated();

        // Anticipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnIf"), 17 ) );

        // The next two should not happen until the underlying subcomponents are also resolved
        //anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 17 ) );
        //anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 17 ) );

        //anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 18 ) );
        //anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 18 ) );


        // Generate up event
        System.err.println("SENDING VpnIfUp EVENT!!");
        engine.correlate( createVpnIfUpEvent( 19, m_pe1NodeId, "10.1.1.1", "5", "ge-1/0/2.50" ) );

        // Check up event
        getAnticipator().verifyAnticipated();

        // Anticipate 2nd up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)"), 18 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 18 ) );

        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 18 ) );


        // Generate up event
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( createVpnPwUpEvent( 20, m_pe1NodeId, "10.1.1.1", "5", "ge-1/0/2.50" ) );

        // Check up event
        getAnticipator().verifyAnticipated();


        // Memory should be clean!
        assertEquals( 0, engine.getMemorySize() );


    }


    @Test
    @DirtiesContext
    public void testNodeDownUpCase() throws Exception {

        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");


        // Antecipate down event
        getAnticipator().reset();
        
        Set<NCSComponent> impactedByNodeDown = new LinkedHashSet<NCSComponent>();
        
        for(NCSComponent c : findSubcomponentsOnNode(m_svc, "space", "1111-PE1") ) {
            impactedByNodeDown.addAll( findPathToSubcomponent(m_svc, c.getForeignSource(), c.getForeignId()));
        }
        
        impactedByNodeDown = uniq( impactedByNodeDown );
        
        anticipate( transform( impactedByNodeDown, toComponentImpactedEvent(17) ) );

        // Generate down event
        System.err.println("SENDING nodeDown EVENT!!");
        engine.correlate( createNodeDownEvent( 17, m_pe1NodeId ) );

        // Check down event
        getAnticipator().verifyAnticipated();

        // Anticipate up event
        getAnticipator().reset();
        anticipate( transform( impactedByNodeDown, toComponentResolvedEvent(19) ) );
        
        // Generate up event
        System.err.println("SENDING nodeUpEvent EVENT!!");
        engine.correlate( createNodeUpEvent( 19, m_pe1NodeId ) );
        
        // Check up event
        getAnticipator().verifyAnticipated();

        // Memory should be clean!
        assertEquals( "Unexpected objects in memory" + engine.getMemoryObjects(), 0, engine.getMemorySize() );

    }

    @Test
    @DirtiesContext
    //@Ignore("not yet implemented")
    public void testMultipleDownAndSingleUpCase() throws Exception {

        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");

        // Anticipate down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)"), 17 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "9876"), 17 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 17 ) );

        // Generate down event
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );

        // Check down event
        getAnticipator().verifyAnticipated();

        // Generate additional down event - nothing should happen
        getAnticipator().reset();
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 18, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );

        getAnticipator().verifyAnticipated();

        // Anticipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)"), 17 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "9876"), 17 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 17 ) );

        // Generate up event
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( createVpnPwUpEvent( 19, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" ) );

        // Check up event
        getAnticipator().verifyAnticipated();

    }

    @Test
    @DirtiesContext
    public void testTwoOutagesCase() throws Exception {
        // Test what happens to the parent when there are two children impacted one is resolved

        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");

        // Anticipate 1st down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)"), 17 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 17 ) );
        anticipate(  createComponentImpactedEvent(findSubcomponent (m_svc, "NA-Service", "123"), 17 ) );

        // Generate down event
        System.err.println("SENDING VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 17, m_pe1NodeId, "10.1.1.1", "5", "ge-1/0/2.50" ) );

        // Check down event
        getAnticipator().verifyAnticipated();

        // Anticipate 2nd down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)"), 18 ) );
        anticipate(  createComponentImpactedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "9876"), 18 ) );
        
        // Should we get this?
        //anticipate(  createComponentImpactedEvent( "Service", "CokeP2P", "NA-Service", "123", 18 ) );

        // Generate another down event for the other PE
        System.err.println("SENDING 2nd VpnPwDown EVENT!!");
        engine.correlate( createVpnPwDownEvent( 18, m_pe2NodeId, "10.1.1.2", "5", "ge-3/1/4.50" ) );

        // Check 2nd down event
        getAnticipator().verifyAnticipated();

        // Anticipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)"), 17 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "8765"), 17 ) );

        // Generate up event
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( createVpnPwUpEvent( 19, m_pe1NodeId, "10.1.1.1", "5", "ge-1/0/2.50" ) );

        // Check up event
        getAnticipator().verifyAnticipated();

        // Anticipate 2nd up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)"), 18 ) );
        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-ServiceElement", "9876"), 18 ) );

        anticipate(  createComponentResolvedEvent( findSubcomponent (m_svc, "NA-Service", "123"), 18 ) );
        

        // Generate up event
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( createVpnPwUpEvent( 20, m_pe2NodeId, "10.1.1.2", "5", "ge-3/1/4.50" ) );

        // Check up event
        getAnticipator().verifyAnticipated();

        
        // Memory should be clean!
        assertEquals( 0, engine.getMemorySize() );

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

    private Event createMplsLspPathDownEvent( int dbId, int nodeid, String ipaddr, String lspname ) {

        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown", "Test")
        .setNodeid(nodeid)
        .setInterface( addr( ipaddr ) )
        .addParam("mplsLspName", lspname )
        .getEvent();

        event.setDbid(dbId);
        return event;
    }

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

    private Event createVpnIfDownEvent( int dbId, int nodeid, String ipaddr, String pwtype, String pwname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("1.2.3.1", pwtype )
                .addParam("1.2.3.2", pwname )
                .getEvent();
        event.setDbid(dbId);
        return event;
    }

    private Event createVpnIfUpEvent( int dbId, int nodeid, String ipaddr, String pwtype, String pwname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("1.2.3.1", pwtype )
                .addParam("1.2.3.2", pwname )
                .getEvent();
        event.setDbid(dbId);
        return event;
    }
    

    // Currently unused
    //    private Event createRootCauseEvent(int symptom, int cause) {
    //        return new EventBuilder(createNodeEvent("rootCauseEvent", cause)).getEvent();
    //    }
    
    private Event createComponentImpactedEvent( NCSComponent component, int cause ) {
        return createComponentImpactedEvent( 
                component.getType(), 
                component.getName(), 
                component.getForeignSource(), 
                component.getForeignId(), 
                cause);
    }
    
    private Event createComponentImpactedEvent( String type, String name, String foreignSource, String foreignId, int cause ) {
        return createComponentEvent(
                "uei.opennms.org/internal/ncs/componentImpacted",
                type,
                name,
                foreignSource,
                foreignId,
                cause
                );
    }

    private Event createComponentResolvedEvent( NCSComponent component, int cause ) {
        return createComponentResolvedEvent( 
                component.getType(), 
                component.getName(), 
                component.getForeignSource(), 
                component.getForeignId(), 
                cause);
    }

    private Event createComponentResolvedEvent( String type, String name, String foreignSource, String foreignId, int cause ) {
        return createComponentEvent(
                "uei.opennms.org/internal/ncs/componentResolved",
                type,
                name,
                foreignSource,
                foreignId,
                cause
                );
    }

    private Event createComponentEvent(String uei, NCSComponent component, int cause) {
        return createComponentEvent(
                uei,
                component.getType(), 
                component.getName(), 
                component.getForeignSource(), 
                component.getForeignId(), 
                cause);
    }

    private Event createComponentEvent(String uei, String type, String name, String foreignSource, String foreignId, int cause) {
        return new EventBuilder(uei, "Component Correlator")
                        .addParam("componentType", type )
                        .addParam("componentName", name)
                        .addParam("componentForeignSource", foreignSource )
                        .addParam("componentForeignId", foreignId )
                        .addParam("cause", cause )
                        .getEvent();
    }


    private Event createNodeDownEvent(int dbId, int nodeid) {
        Event event = new EventBuilder("uei.opennms.org/nodes/nodeDown", "Test")
                .setNodeid(nodeid)
                .getEvent();
        event.setDbid(dbId);
        return event;
    }
    
    private Event createNodeUpEvent(int dbId, int nodeid) {
        Event event = new EventBuilder("uei.opennms.org/nodes/nodeUp", "Test")
                .setNodeid(nodeid)
                .getEvent();
        event.setDbid(dbId);
        return event;
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
    
    private NCSComponent findSubcomponent(NCSComponent svc, String foreignSource, String foreignId) {
        Set<NCSComponent> components = findMatchingComponents(svc, byId(foreignSource, foreignId));

        if (components.size() > 1) {
            throw new IllegalStateException("Found more than one subcomponent with the same id " + foreignSource + ":" + foreignId);
        }
        
        if ( components.isEmpty() ) {
            throw new IllegalStateException("Unabled to find component with id " + foreignSource + ":" + foreignId);
        }
        
        return components.iterator().next();
    }

    private Set<NCSComponent> findSubcomponentsOnNode(NCSComponent svc, String nodeForeignSource, String nodeForeignId) {
        return findMatchingComponents(svc, byNode(nodeForeignSource, nodeForeignId));
    }
    
    private Set<NCSComponent> findMatchingComponents(NCSComponent c, final Predicate<NCSComponent> pred) {
        
        VisitorWithReturn<Set<NCSComponent>> visitor = new VisitorWithReturn<Set<NCSComponent>>() {

            @Override
            public void visitComponent(NCSComponent component) {
                if (pred.accept(component)) {
                    getRetVal().add(component);
                }
            }
            
        };
        
        return visitWithRetVal(c, visitor, new LinkedHashSet<NCSComponent>());

    }
    
    private <T> T visitWithRetVal(NCSComponent c, VisitorWithReturn<T> visitor, T initialValue) {
        visitor.setRetVal(initialValue);
        c.visit(visitor);
        return visitor.getRetVal();
    }
    
    private List<NCSComponent> findPathToSubcomponent(NCSComponent svc, final String subForeignSource, final String subForeignId) {
        
        VisitorWithReturn<List<NCSComponent>> visitor = new VisitorWithReturn<List<NCSComponent>>() {
            Stack<NCSComponent> m_stack = new Stack<NCSComponent>();

            @Override
            public void visitComponent(NCSComponent component) {
                m_stack.push(component);
                if (subForeignSource.equals(component.getForeignSource()) && subForeignId.equals(component.getForeignId())) {
                    setRetVal(new LinkedList<NCSComponent>(m_stack));
                }
            }

            @Override
            public void completeComponent(NCSComponent component) {
                m_stack.pop();
            }
        };
        
        return visitWithRetVal(svc, visitor, null);
        
    }
    
    private <A,B> Set<B> transform(Set<A> as, Transform<A, B> transformer) {
        Set<B> bs = new LinkedHashSet<B>();
        
        for(A a : as) {
            bs.add(transformer.transform(a));
        }
        
        return bs;
    }
    
    private <A,B> List<B> transform(List<A> as, Transform<A, B> transformer) {
        List<B> bs = new LinkedList<B>();
        
        for(A a : as) {
            bs.add(transformer.transform(a));
        }
        
        return bs;
    }
    
    private Predicate<NCSComponent> byNode(final String nodeForeignSource, final String nodeForeignId) {
        return new Predicate<NCSComponent>() {

            @Override
            public boolean accept(NCSComponent c) {
                return c.getNodeIdentification() != null && 
                        nodeForeignSource.equals(c.getNodeIdentification().getForeignSource()) 
                        && nodeForeignId.equals(c.getNodeIdentification().getForeignId());
            }
            
        };
    }
    
    private Predicate<NCSComponent> byId(final String foreignSource, final String foreignId) {
        return new Predicate<NCSComponent>() {

            @Override
            public boolean accept(NCSComponent c) {
                return foreignSource.equals(c.getForeignSource()) && foreignId.equals(c.getForeignId()); 
            }
            
        };
    }
    
    private Transform<NCSComponent, String> foreignIdentifiers() {
        return new Transform<NCSComponent, String>() {

            @Override
            public String transform(NCSComponent a) {
                return a.getForeignSource()+":"+a.getForeignId();
            }
            
        };
    }
    
    private Transform<NCSComponent, Event> toComponentEvent(final String uei, final int cause) {
        return new Transform<NCSComponent, Event>() {

            @Override
            public Event transform(NCSComponent component) {
                return createComponentEvent(uei, component, cause);
            }
            
        };
    }
    
    private Transform<NCSComponent, Event> toComponentImpactedEvent(final int cause) {
        return toComponentEvent("uei.opennms.org/internal/ncs/componentImpacted", cause);
    }

    private Transform<NCSComponent, Event> toComponentResolvedEvent(final int cause) {
        return toComponentEvent("uei.opennms.org/internal/ncs/componentResolved", cause);
    }
    
    private static class VisitorWithReturn<T> extends AbstractNCSComponentVisitor {
        private T retVal = null;
        
        public void setRetVal(T r) {
            retVal = r;
        }
        
        public T getRetVal() {
            return retVal;
        }
    }
    
    private Event[] ofEvents() {
        return new Event[0];
    }
    
    
    private static Set<NCSComponent> uniq(Set<NCSComponent> components) {
        Set<NCSComponent> results = new LinkedHashSet<NCSComponent>();
        Set<String> ids = new HashSet<String>();

        for(NCSComponent component : components) {
            String id = component.getForeignSource()+":"+component.getForeignId();
            if (!ids.contains(id)) {
                ids.add(id);
                results.add(component);
            }
        }
        
        return results;

    }

}
