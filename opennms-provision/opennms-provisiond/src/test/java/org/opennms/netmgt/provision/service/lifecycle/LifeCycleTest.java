/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service.lifecycle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;



/**
 * LifecycleTest
 *
 * @author brozow
 */
public class LifeCycleTest {
    
    // define a lifecyle
    
    // how do we build scanners when we need to run them
    
    // a better name for a scanner (mojo?)
    
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
    
    private static final String SAMPLE_DATA = "sampleData";

    @ActivityProvider
    public static class TestActivities {
        
        private void appendPhase(LifeCycle lifecycle, final String phase) {
            lifecycle.setAttribute(SAMPLE_DATA, lifecycle.getAttribute(SAMPLE_DATA, String.class)+phase);
        }
        
        @Activity(phase="phase1", lifecycle="sample")
        public void doPhaseOne(LifeCycle lifecycle) {
            System.err.println("Called doPhaseOne!");
            appendPhase(lifecycle, "phase1 ");
        }

        @Activity(phase="phase2", lifecycle = "sample")
        public void doPhaseTwo(LifeCycle lifecycle) {
            System.err.println("Called doPhaseTwo!");
            appendPhase(lifecycle, "phase2 ");
        }
        
        @Activity(phase="phase3", lifecycle = "sample")
        public void doPhaseThree(LifeCycle lifecycle) {
            System.err.println("Called doPhaseThree!");
            appendPhase(lifecycle, "phase3");
        }

        @Activity(phase="phase3", lifecycle = "invalidLifecycle")
        public void doPhaseInvalidLifeCycle(LifeCycle lifecycle) {
            System.err.println("Called doPhaseInvalidLifeCycle!");
            appendPhase(lifecycle, " invalidLifecycle");
        }

        @Activity(phase="invalidPhase", lifecycle = "sample")
        public void doPhaseInvalid(LifeCycle lifecycle) {
            System.err.println("Called doPhaseInvalid!");
            appendPhase(lifecycle, " invalidPhase");
        }

    }
    
    // waitFor should throw an exception if its not been triggered
    
    // if we don't call trigger then the getAttribute should return ""
    
    
    @Test
    public void testLifeCycleAttributes() {
        LifeCycle lifecycle = new LifeCycleDefinition("sample")
            .addPhases("phase1", "phase2", "phase3")
            .addProviders(new TestActivities()) // should we do addProviders(TestActivities.class)?
            .build();
        
        lifecycle.setAttribute(SAMPLE_DATA, "phase1 phase2 phase3");

        assertEquals("phase1 phase2 phase3", lifecycle.getAttribute(SAMPLE_DATA, String.class));
    }
    
    @Test
    public void testTriggerLifeCycle() {
        LifeCycle lifecycle = new LifeCycleDefinition("sample")
            .addPhases("phase1", "phase2", "phase3")
            .addProviders(new TestActivities()) // should we do addProviders(TestActivities.class)?
            .build();
        
        lifecycle.setAttribute(SAMPLE_DATA, "");
        
        lifecycle.trigger();
        
        lifecycle.waitFor();
        
        assertEquals("phase1 phase2 phase3", lifecycle.getAttribute(SAMPLE_DATA, String.class));
    }
    
    

    
}
