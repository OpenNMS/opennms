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

package org.opennms.netmgt.provision.service.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Vector;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;



/**
 * LifecycleTest
 *
 * @author brozow
 */
public class LifeCycleInstanceTest {
    
    /*
     * TODO
     * - Use parameter type info to inject the appropriate arguments
     * - Add return values to attribute lists
     * - Use annotations to disambiguate attribute parameters
     * - Use annotations to assign attribute name to return values
     * - LifeCycle return value automatically triggered 
     * - Task return value automatically scheduled
     * - Use a CompletionService and Executor to run phases in the background
     * - Make waitFor really work for things in the background
     * - Run phases asynchronously
     * - Make phases depend-on previous phases
     * - Make a lifeCycle definition DAO
     * - provide a way to locate ActivityProviders
     * - Wrap the annotation driven strategy in an ActitivyProvider class
     *     programmatic providers can all be used.  This will enable
     *     'publishing' a provider as an interface in OSGI
     *  
     * 
     */
    
    // activity(LifeCycle lifeCycle, OnmsIpInterface iface)
    // if there is only one attribute that contains that type then
    // pass it in as an argument.  Should be able to annotate the 
    // type as well
    
    // if would be good if return values could be automatically added
    // to the lifecycle don't know what attribute that would use but
    // we could define that with an annotation... if it matters.. could
    // be that type info is enough
    
    // need a way to handle nested lifecycles without waiting...
    // 
    // mark an activity as 'asynchronous=true' this would run
    // without waiting...
    
    // mark an activity as 'depends-on='previous task' this would
    // force it to wait for other activities that are running in the background
    
    // Activities can return LifeCycles (or maybe more generic Tasks??). If the
    // activities are synchronous then the next phase doesn't run into those tasks
    // are all complete.
    // If they are asynchronous then activities that depend on the phase cannot
    // run until all the return LifeCycles/Tasks are completed
    
     // Does having subphases make sense?  Or having 'foreach' tasks that get fired 
     // when something gets added?
    
    
    
    // Another NOTE:  Callbacks and task scheduling implemented around a CompletionService
    // The CompletionService has and executor behind it running tasks and a thread whose job 
    // it is to process completed tasks and schedule the next.  This would make the schronization
    // requirements of the task tracking much easy to keep thread safe.
    
    // Can the importer be implemented with this mechanism?
    
    /* class ImportActitivies {
     * 
     *  public SpecFile parseResource(OnmsResource resource) {
     *       // parse and create SpecFile
     *       // assert SpecFile
     * }
     * 
     * public void auditNode(SpecFile specFile) {
     *      // retrieve ForeignId -> NodeId Map
     *      // as I go thru the nodes in the specFile
     *      // I end up with three groups of nodes
     *      // one set to delete
     *      // one set to scan and then update
     *      // one set to scan and then insert
     *      //
     *      // I could make each of these a 'lifecycle' of their own
     *      // a trick is that deletes have to complete, then updates, then inserts
     *      // but... scanning for the nodes can happen anytime...
     *      //
     *      // also... the scan/update or scan/insert should be a 'scanNode' lifecycle
     *      // for sure
     *      //
     *      // so.. we could do this:
     *      //
     *      // for each node
     *      // /---
     *      // |
     *      // |-- scan phase... start scans for update and insert nodes... make it asynchronous
     *      // |
     *      // |-- delete phase... delete the nodes that need to be deleted (synchronous)
     *      // |
     *      // |-- update phase... update the nodes that need to be updated... depends on the scan for the node (synchronous)
     *      // |
     *      // |-- insert phase... insert the nodes that need to be inserted... depends on the scan for the node (synchronous)
     *      // |
     *      // \---
     *      // 
     *      // relateNodes... relate the nodes to each other.. after they have been committed to the DB.
     * }
     */
    
    
    
   
    
    // how do we build scanners when we need to run them
    
    // how do we find the scanners
    
    // lifecycles should be defined in configuration
    
    // how do we pass data into the scanner methods
    
    // scanners should be dependency injected
    
    // resource scanners will take the 'resource' in the scan method
    
    // node scanners will take the 'node' in the scan method
    
    // import scanner can take the foreign source.... how do I get the URL?
    
    // persist lifecycles that are in progress?
    
    /*
     *  A run of a lifecycle has a 'trigger' of some kind....
     *  1.  newSuspectEvent
     *  2.  forceRescanEvent
     *  3.  'scheduled' rescan
     *  4.  'import' event
     *  5.  'scheduled' import
     *  6.  import triggers a node scan
     *  7   node scan triggers a service scan
     *  
     *  We can pass a trigger object into each
     *  
     *  Need to use generics in some way so that I end up with the correct type for triggers and/or other parameters
     *  
     *  I could pass the Lifecycle object into each phase method... and I could set attributes on the lifecycle object
     *  
     *  I would need to define a set of attributes that could be set for each lifecycle
     *  
     *  I could use annotations to pass those attributes as methods
     *  
     *  Maybe 'resourceFactories' could be annotated as well and implement a simple interface
     *  
     *  
     *   
     */
    
    private static final String PHASE_DATA = "phaseData";
    public static final String NESTED_DATA = "nestedData";
    public static final String NEST_LEVEL = "nestLevel";
    public static final String MAX_DEPTH = "maxDepth";
    
    private LifeCycleRepository m_lifeCycleFactory;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        TaskCoordinator coordinator = new DefaultTaskCoordinator("LifeCycleInstanceTest");
        coordinator.addOrUpdateExecutor(TaskCoordinator.DEFAULT_EXECUTOR, Executors.newFixedThreadPool(10));
        
        DefaultLifeCycleRepository repository = new DefaultLifeCycleRepository(coordinator);
        
        LifeCycle lifeCycle = new LifeCycle("sample")
        .addPhases("phase1", "phase2", "phase3");
        
        LifeCycle injection = new LifeCycle("injection")
        .addPhases("phase1", "phase2", "phase3");
        
        repository.addLifeCycle(lifeCycle);
        repository.addLifeCycle(injection);
        
        m_lifeCycleFactory = repository;

        
    }
    

    @ActivityProvider
    public static class PhaseTestActivities extends ActivityProviderSupport {
        
        private void appendPhase(LifeCycleInstance lifecycle, final String value) {
            appendToStringAttribute(lifecycle, PHASE_DATA, value);
        }

        // this should be called first
        @Activity(phase="phase1", lifecycle="sample")
        public void doPhaseOne(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, "phase1 ");

        }

        // this should be called last
        @Activity(phase="phase3", lifecycle = "sample")
        public void doPhaseThree(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, "phase3");

        }

        // this should be called in the middle
        @Activity(phase="phase2", lifecycle = "sample")
        public void doPhaseTwo(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, "phase2 ");

        }
        
        // this should not be called
        @Activity(phase="phase3", lifecycle = "invalidLifecycle")
        public void doPhaseInvalidLifeCycle(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, " invalidLifecycle");

        }

        // this should not be called
        @Activity(phase="invalidPhase", lifecycle = "sample")
        public void doPhaseInvalid(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, " invalidPhase");

        }

    }
    
    // waitFor should throw an exception if its not been triggered
    
    // if we don't call trigger then the getAttribute should return ""
    
    
    @Test
    public void testLifeCycleAttributes() {
        LifeCycleInstance lifecycle = m_lifeCycleFactory.createLifeCycleInstance("sample", new PhaseTestActivities());
        
        lifecycle.setAttribute(PHASE_DATA, "phase1 phase2 phase3");

        assertEquals("phase1 phase2 phase3", lifecycle.getAttribute(PHASE_DATA, String.class));
    }
    
    @Test
    public void testTriggerLifeCycle() throws Exception {
        LifeCycleInstance lifecycle = m_lifeCycleFactory.createLifeCycleInstance("sample", new PhaseTestActivities());
        
        lifecycle.trigger();
        
        lifecycle.waitFor();
        
        assertEquals("phase1 phase2 phase3", lifecycle.getAttribute(PHASE_DATA, String.class));
    }

    
    @ActivityProvider
    public static class InjectionTestActivities {
        
        // this should be called first
        @Activity(phase="phase1", lifecycle="injection")
        @Attribute("one")
        public Integer doPhaseOne(Phase phase1, Vector<String> dataAccumulator) {

            dataAccumulator.add(phase1.getName());
            
            return 1;
            
        }

        // this should be called in the middle
        @Activity(phase="phase2", lifecycle = "injection")
        @Attribute("two")
        public Integer doPhaseTwo(Phase phase2, Vector<String> dataAccumulator) {

            dataAccumulator.add(phase2.getName());
            
            return 2;

        }

        // this should be called last
        @Activity(phase="phase3", lifecycle = "injection")
        public void doPhaseThree(@Attribute("one") Integer one, Phase phase3, Vector<String> dataAccumulator, @Attribute("two") Integer two) {

            dataAccumulator.add(phase3.getName());
            dataAccumulator.add(one.toString());
            dataAccumulator.add(two.toString());

        }

        
    }
    
    
    @Test
    public void testInjectionLifeCycle() throws Exception {
        LifeCycleInstance lifecycle = m_lifeCycleFactory.createLifeCycleInstance("injection", new InjectionTestActivities());
        lifecycle.setAttribute("dataAccumulator", new Vector<String>());
        
        lifecycle.trigger();
        
        lifecycle.waitFor();
        
        @SuppressWarnings("unchecked")
        Vector<String> results = lifecycle.getAttribute("dataAccumulator" , Vector.class);

        assertNotNull(results);

        assertEquals(Integer.valueOf(1), lifecycle.getAttribute("one", Integer.class));
        assertEquals("phase1", results.get(0));
        assertEquals("phase2", results.get(1));
        assertEquals("phase3", results.get(2));
        assertEquals("1", results.get(3));
        assertEquals("2", results.get(4));
        
        assertEquals(5, results.size());
        
    }


    @ActivityProvider
    public static class NestedLifeCycleActivites extends ActivityProviderSupport {
        
        @SuppressWarnings("unused")
        private final LifeCycleRepository m_lifeCycleRepository;
        
        public NestedLifeCycleActivites(LifeCycleRepository lifeCycleRepository) {
            m_lifeCycleRepository = lifeCycleRepository;
        }
        
        private void appendPhase(LifeCycleInstance lifecycle, final String phase) {
            appendToStringAttribute(lifecycle, NESTED_DATA, phase);
        }

        // this should be called first
        @Activity(phase="phase1", lifecycle="sample")
        public void doPhaseOne(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, getPrefix(lifecycle)+"phase1 ");

        }

        // this should be called last
        @Activity(phase="phase3", lifecycle = "sample")
        public void doPhaseThree(LifeCycleInstance lifecycle) {

            appendPhase(lifecycle, getPrefix(lifecycle)+"phase3 ");

        }

        // this should be called in the middle
        @Activity(phase="phase2", lifecycle = "sample")
        public LifeCycleInstance doPhaseTwo(LifeCycleInstance lifecycle, Phase currentPhase) throws Exception {

            appendPhase(lifecycle, getPrefix(lifecycle)+"phase2start ");
            
            LifeCycleInstance nested = null;
            
            int nestLevel = lifecycle.getAttribute(NEST_LEVEL, 0);
            int maxDepth = lifecycle.getAttribute(MAX_DEPTH, 0);
            if (nestLevel < maxDepth) {
                nested = currentPhase.createNestedLifeCycle("sample");

                nested.setAttribute(MAX_DEPTH, maxDepth);
                nested.setAttribute(NEST_LEVEL, nestLevel+1);
                
                //fail("I'd like to have trigger by called by the framework rather than here...");
                nested.trigger();
                
                nested.waitFor();
                
                appendPhase(lifecycle, nested.getAttribute(NESTED_DATA, String.class));
            }

            appendPhase(lifecycle, getPrefix(lifecycle)+"phase2end ");

            return nested;
        }

        private String getPrefix(LifeCycleInstance lifecycle) {
            int nestLevel = lifecycle.getAttribute(NEST_LEVEL, 0);
            return buildPrefix(nestLevel);
        }
        
        private String buildPrefix(int nestLevel) {
            final StringBuilder buf = new StringBuilder();
            buildPrefixHelper(nestLevel, buf);
            return buf.toString();

        }
        
        private void buildPrefixHelper(int nestLevel, StringBuilder buf) {
            if (nestLevel == 0) {
                return;
            } else {
                buildPrefixHelper(nestLevel-1, buf);
                buf.append("level").append(nestLevel).append('.');
            }
        }
        
        

    }
    
    @Test
    public void testNestedLifeCycle() throws Exception {

        LifeCycleInstance lifecycle = m_lifeCycleFactory.createLifeCycleInstance("sample", new NestedLifeCycleActivites(m_lifeCycleFactory));
        lifecycle.setAttribute(MAX_DEPTH, 1);

        lifecycle.trigger();
        
        lifecycle.waitFor();
        
        assertEquals("phase1 phase2start level1.phase1 level1.phase2start level1.phase2end level1.phase3 phase2end phase3 ", lifecycle.getAttribute(NESTED_DATA, String.class));

    }
    
    
    public static class ActivityProviderSupport {

        protected void appendToStringAttribute(LifeCycleInstance lifecycle, String key, String value) {
                    lifecycle.setAttribute(key, lifecycle.getAttribute(key, "")+value);
                }
        
    }
    
}
