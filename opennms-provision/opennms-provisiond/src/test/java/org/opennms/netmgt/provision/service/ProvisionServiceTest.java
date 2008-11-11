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
package org.opennms.netmgt.provision.service;

import org.junit.Test;


/**
 * ProvisionServiceTest
 *
 * @author brozow
 */
public class ProvisionServiceTest {
    
    /*
     *  A list of use cases/todos for a Provision Service
     * - provide a set of scanners to be called
     * - configure the scanners per foreign source
     * - be able to 'merge' scan results and 'import' data
     * - need to be able to scan for each type of resource
     * - need a layer that takes the 'change set' and then persists it including sending events
     * - how do I track how to call the various scanners...
     *   - some are per interface (e.g. service scanner)
     *   - some are per node (e.g. service scanner)
     *   - some run once (ie per node) but update data many resources (eg interfaces)
     *   - some just look at interface attributes and update 
     * - what do I do about scanners that want to lookup information from the database...
     *   - how do I deal with the transactions?
     * - I could have scan phases - some that collect and then others that update
     * - it would be nice to be able to 'merge' all of the snmpdata that needs to be
     *     collected into a single walker for efficiency
     * - how do I know about SNMP enabled and the Preferred SNMP interface etc.
     * 
     */
    
    /*
     *  IDEAS:
     *  - a multiphase scanner (are phases configurable?)
     *  - a phase for collecting data and a phase for updating data (maybe pre and post update?)
     *  - maybe provide 'triggers' for each kind of resource added and be able to associate
     *      a scanner with a trigger
     *  - as far as 'provided' data vs 'discovered' data we could have wrappers that prevent
     *    the data that should 'win'
     *  - maybe a phase for 'agent' detection
     *  
     */
    
    /*
     *  Notes about current Importer Phases:
     * 1. parse spec file and build node representation based on data from file
     * 
     * 2. diff spec file nodelist with current node list to find
     *  A. nodes that have disappeared from spec file
     *  B. nodes that have only just appeared in spec file
     *  C. the remaining nodes
     *  
     * 3. Delete all nodes in group A from Database
     *  
     * 4. for each node in group C
     *    
     *     A. 'preprocess' == gatherAdditionalData == get snmp data
     *     B. 'persist' == store spec file data + gathered data to db
     *     
     * 5. for each node in group B
     *     A. 'preprocess'
     *     B. 'persist'
     *     
     * 6. for each node in group B or C
     *     A. 'relate' set the parent node to a reference to the appropriate node
     */
    
    /*
     *  Phase Ideas
     * 1. agent scan phase
     * 2. phase for 
     * 
     * I had the idea that we should have the scanners define new resources and then
     * rework the collector to collect data for the resources that exist.
     * 
     * We can do scanners in multiple protocols SNMP, WMI, NRPE/NsClient, others?  
     * 
     * Two possible ideas related to this:  
     *   1. We can make the scanners be responsible for finding not the collectors.
     *   2. We could have a service that would allow the collector to push a scan 
     *      through when it collected the data.
     *   Neither of these are perfect.  It may be that the scanning and collection
     *     are just variations of the same theme 
     * 
     */
     
    /*
     * More Ideas:
     * 
     * Class are created that represent Scanners...
     * 
     * The Scanners use the ScanProvider annotation 
     * Scopes can be 'Network/ImportSet', 'Node', 'Agent' or 'Resource' 
     *  - agent and resource provide a type indicator
     * A ScanProvider has a 'Lifetime' that matches one of the Scopes
     * The ScanProviders have methods that define a Scope and a Phase.
     * The methods are called for each element at the appropriate scope
     * 
     * possible phase list for a single node
     * 
     * - triggerValidation
     * - network comparison
     * - defunct node deletion
     * - agentDetection - 
     * - resourceDiscovery - scope Agent (calls Scope network, then node for each node, then agent for matching agents)
     * - resourceScan - scope Resource (calls networ,
     * - persist - scope Node
     */
    
    /*
     * We need to define idea of a life cycle.  A lifecycle would define a set of phases
     * that it would go through.  Each phase could be a single call or a pile of calls executed
     * in an ExecutorService.  This would be up to the phase.
     * 
     * Lifecycles would be triggered by various events.  
     * 1.  New Suspect would trigger a Node Scanning Lifecyle
     * 2.  Rescan would trigger a Node Scanning Lifecycle
     * 3.  Import would trigger an Import Lifecycle
     * 4.  There may be other lifecycles like resource discovered on collection
     * 5.  Maybe Collection itself could use this Lifecycle model
     * 
     * Lifecycle phases could contain embedded Lifecycles.  For example the Import lifecycle 
     * would embed a Node Scanning Lifecycle for each node being inserted or updated.
     * 
     * We need a way to define input and output of a phase...
     * 
     * Maybe the argument and the return value of a 'scanMethod'
     * 
     * Also need an indication of where transactions should live 
     * 
     */
    
    @Test
    public void testLifeCycle() throws Exception {
        
//        ProcessBuilder bldr = new ProcessBuilder();
//        
//        bldr.sequence("import") {
//            SpecFile file = phase("validateFile", validateFile(url))
//            UpdatePlan plan = phase("createUpdatePlan", createUpdatePlan(file))
//            batch("updateNodes") {
//                forEach(node : plan.nodesToDelete) task("deleteNode", node);
//                
//                for(node : plan.nodesToUpdate) sequence {
//                    phase("detectAgents" ,detectAgents());
//                    phase("discoverResources", discoverResources())
//                    phase("updatedNode")
//                }
//                
//                for(node : plan.nodesToInsert) sequence {
//                    phase("detectAgents" ,detectAgents());
//                    phase("discoverResources", discoverResources())
//                    phase("insertNode") {
//                        
//                    }
//                }
//            }
//        }

        
    }

}
