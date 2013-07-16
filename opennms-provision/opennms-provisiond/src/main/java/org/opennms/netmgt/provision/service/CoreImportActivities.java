/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.util.Collection;
import java.util.Map;

import org.opennms.core.tasks.BatchTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.provision.persist.AbstractRequisitionVisitor;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.RequisitionVisitor;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.RequisitionImport;
import org.springframework.core.io.Resource;

/**
 * CoreImportActivities
 *
 * @author brozow
 * @version $Id: $
 */
@ActivityProvider
public class CoreImportActivities {
    private static final Logger LOG = LoggerFactory.getLogger(CoreImportActivities.class);
    
    ProvisionService m_provisionService;
    
    public CoreImportActivities(final ProvisionService provisionService) {
        m_provisionService = provisionService;
    }

    @Activity( lifecycle = "import", phase = "validate", schedulingHint="import")
    public RequisitionImport loadSpecFile(final Resource resource) {
        final RequisitionImport ri = new RequisitionImport();

        info("Loading requisition from resource %s", resource);
        try {
            final Requisition specFile = m_provisionService.loadRequisition(resource);
            ri.setRequisition(specFile);
            debug("Finished loading requisition.");
        } catch (final Throwable t) {
            ri.abort(t);
        }

        return ri;
    }
    
    @Activity( lifecycle = "import", phase = "audit", schedulingHint="import" )
    public ImportOperationsManager auditNodes(final RequisitionImport ri, final Boolean rescanExisting) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping audit phase import.");
            return null;
        }
        
        final Requisition specFile = ri.getRequisition();

        info("Auditing nodes for requisition %s", specFile);

        // @ipv6
        m_provisionService.createDistPollerIfNecessary("localhost", "127.0.0.1");
        
        final String foreignSource = specFile.getForeignSource();
        final Map<String, Integer> foreignIdsToNodes = m_provisionService.getForeignIdToNodeIdMap(foreignSource);

        final ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, m_provisionService, rescanExisting);
        
        opsMgr.setForeignSource(foreignSource);
        opsMgr.auditNodes(specFile);

        debug("Finished auditing nodes.");
        
        return opsMgr;
    }
    
    @Activity( lifecycle = "import", phase = "scan", schedulingHint="import" )
    public void scanNodes(final Phase currentPhase, final ImportOperationsManager opsMgr, final RequisitionImport ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping scan phase import.");
            return;
        }

        info("Scheduling nodes for phase %s", currentPhase);
        
        final Collection<ImportOperation> operations = opsMgr.getOperations();
        
        for(final ImportOperation op : operations) {
            final LifeCycleInstance nodeScan = currentPhase.createNestedLifeCycle("nodeImport");

            debug("Created lifecycle %s for operation %s", nodeScan, op);
            
            nodeScan.setAttribute("operation", op);
            nodeScan.setAttribute("requisitionImport", ri);
            nodeScan.trigger();
        }


    }
    
    
    @Activity( lifecycle = "nodeImport", phase = "scan", schedulingHint="import" )
    public void scanNode(final ImportOperation operation, final RequisitionImport ri, final Boolean rescanExisting) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping scan phase nodeImport.");
            return;
        }

        if (rescanExisting == null || rescanExisting) {
            info("Running scan phase of %s", operation);
            operation.scan();
    
            info("Finished Running scan phase of %s", operation);
        } else {
            info("Skipping scan phase of %s, because the %s parameter was set during import.", operation, EventConstants.PARM_IMPORT_RESCAN_EXISTING);
        }
    }
    
    @Activity( lifecycle = "nodeImport", phase = "persist" , schedulingHint = "import" )
    public void persistNode(final ImportOperation operation, final RequisitionImport ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping persist phase.");
            return;
        }

        info("Running persist phase of %s", operation);
        operation.persist();
        info("Finished Running persist phase of %s", operation);

    }
    
    @Activity( lifecycle = "import", phase = "relate" , schedulingHint = "import" )
    public void relateNodes(final BatchTask currentPhase, final RequisitionImport ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping relate phase.");
            return;
        }

        info("Running relate phase");
        
        final Requisition requisition = ri.getRequisition();
        RequisitionVisitor visitor = new AbstractRequisitionVisitor() {
            @Override
            public void visitNode(final OnmsNodeRequisition nodeReq) {
                LOG.debug("Scheduling relate of node {}", nodeReq);
                currentPhase.add(parentSetter(m_provisionService, nodeReq, requisition.getForeignSource()));
            }
        };
        
        requisition.visit(visitor);
        
        LOG.info("Finished Running relate phase");

    }
    
    private static Runnable parentSetter(final ProvisionService provisionService, final OnmsNodeRequisition nodeReq, final String foreignSource) {
        return new Runnable() {
            @Override
            public void run() {
                provisionService.setNodeParentAndDependencies(
                    foreignSource,
                    nodeReq.getForeignId(),
                    // If the node requisition does not include a parent foreign source
                    // name, then use the foreign source of the current requisition
                    // as the default value
                    //
                    // @see http://issues.opennms.org/browse/NMS-4109
                    //
                    nodeReq.getParentForeignSource() == null ? 
                        foreignSource : nodeReq.getParentForeignSource(),
                    nodeReq.getParentForeignId(),
                    nodeReq.getParentNodeLabel()
                );

                provisionService.clearCache();
            }

            @Override
            public String toString() {
                return "set parent for node "+nodeReq.getNodeLabel();
            }
        }; 
    }

    protected void info(String format, Object... args) {
    	LOG.info(format, args);
    }

    protected void debug(String format, Object... args) {
        LOG.debug(format, args);
    }

    protected void warn(String format, Object... args) {
        LOG.warn(format, args);
    }
}
