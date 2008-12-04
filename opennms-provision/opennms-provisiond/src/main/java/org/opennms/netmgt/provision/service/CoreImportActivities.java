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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.specification.AbstractImportVisitor;
import org.opennms.netmgt.provision.service.specification.ImportVisitor;
import org.opennms.netmgt.provision.service.specification.SpecFile;
import org.opennms.netmgt.provision.service.tasks.BatchTask;
import org.opennms.netmgt.provision.service.tasks.Task;
import org.springframework.core.io.Resource;

/**
 * CoreImportActivities
 *
 * @author brozow
 */
public class CoreImportActivities {
    
    ProvisionService m_provisionService;
    
    public CoreImportActivities(ProvisionService provisionService) {
        m_provisionService = provisionService;
    }

    /*
     *
     *                   LifeCycle importLifeCycle = new LifeCycle("import")
        .addPhase("validate")
            .addPhase("audit")
            .addPhase("scan")
            .addPhase("delete")
            .addPhase("update")
            .addPhase("insert")
            .addPhase("relate");
            
 
     */

    /*
     *         LifeCycle nodeScanLifeCycle = new LifeCycle("nodeScan")
            .addPhase("scan")
            .addPhase("persist");

     */

    @Activity( lifecycle = "import", phase = "validate" )
    public void loadSpecFile(LifeCycleInstance lifeCycle) throws ModelImportException, IOException {
        Resource resource = lifeCycle.getAttribute("resource", Resource.class);
        String foreignSource = lifeCycle.getAttribute("foreignSource", String.class);

        System.out.println("Loading Spec File!");
        
        SpecFile specFile = new SpecFile();
        specFile.loadResource(resource);
        
        if (foreignSource != null) {
            specFile.setForeignSource(foreignSource);
        }
        
        System.out.println("Finished Loading Spec File!");

        lifeCycle.setAttribute("specFile", specFile);
    }
    
    
    
    @Activity( lifecycle = "import", phase = "audit" )
    public void auditNodes(LifeCycleInstance lifeCycle) {
        
        System.out.println("Auditing Nodes");
        
        SpecFile specFile = lifeCycle.getAttribute("specFile", SpecFile.class);
        
        m_provisionService.createDistPollerIfNecessary("localhost", "127.0.0.1");
        
        String foreignSource = specFile.getForeignSource();
        Map<String, Integer> foreignIdsToNodes = m_provisionService.getForeignIdToNodeIdMap(foreignSource);
        
        ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, m_provisionService);
        
        opsMgr.setForeignSource(foreignSource);
        
        opsMgr.auditNodes(specFile);
        
        lifeCycle.setAttribute("opsMgr", opsMgr);
        
        System.out.println("Finished Auditing Nodes");
    }
    
    @Activity( lifecycle = "import", phase = "scan")
    public Task scanNodes(LifeCycleInstance lifeCycle) {
        ImportOperationsManager opsMgr = lifeCycle.getAttribute("opsMgr", ImportOperationsManager.class);
        
        System.out.println("Scheduling Nodes");
        final Collection<ImportOperation> operations = opsMgr.getOperations();
        
        BatchTask batch = new BatchTask(lifeCycle.getCoordinator());
        
        for(final ImportOperation op : operations) {
            LifeCycleInstance nodeScan = lifeCycle.createNestedLifeCycle("nodeScan");
            System.out.printf("Created  LifeCycle %s for op %s\n", nodeScan, op);
            nodeScan.setAttribute("operation", op);
            batch.add((Task)nodeScan);
        }



        return batch;
    }
    
    @Activity( lifecycle = "nodeScan", phase = "scan" )
    public void scanNode(LifeCycleInstance lifeCycle) {
        
        ImportOperation operation = lifeCycle.getAttribute("operation", ImportOperation.class);
        

        System.out.println("Running scan phase of "+operation);
        operation.scan();
        System.out.println("Finished Running scan phase of "+operation);
    }
    
    @Activity( lifecycle = "nodeScan", phase = "persist" , schedulingHint = "write" )
    public void persistNode(LifeCycleInstance lifeCycle) {
        ImportOperation operation = lifeCycle.getAttribute("operation", ImportOperation.class);
        System.out.println("Running persist phase of "+operation);
        operation.persist();
        System.out.println("Finished Running persist phase of "+operation);
    }
    
    @Activity( lifecycle = "import", phase = "relate" , schedulingHint = "write" )
    public Task relateNodes(final LifeCycleInstance lifeCycle) {
        
        
        System.out.println("Running relate phase");
        
        final SpecFile specFile = lifeCycle.getAttribute("specFile", SpecFile.class);

        final BatchTask batch = new BatchTask(lifeCycle.getCoordinator());
        
        ImportVisitor visitor = new AbstractImportVisitor() {
            public void visitNode(Node node) {
                System.out.println("Scheduling relate of node "+node);
                batch.add(parentSetter(node, specFile.getForeignSource()));
            }
        };
        
        specFile.visitImport(visitor);
        
        System.out.println("Finished Running relate phase");

        return batch;
    }
    
    private Runnable parentSetter(final Node node, final String foreignSource) {
        return new Runnable() {
           public void run() {
               m_provisionService.setNodeParentAndDependencies(foreignSource, node.getForeignId(), node.getParentForeignId(),
                                                               node.getParentNodeLabel());

               m_provisionService.clearCache();
           }
           public String toString() {
               return "set parent for node "+node.getNodeLabel();
           }
        }; 
    }
}
