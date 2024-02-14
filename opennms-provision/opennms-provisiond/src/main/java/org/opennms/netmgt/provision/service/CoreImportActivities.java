/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.service;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.tasks.BatchTask;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.events.api.EventConstants;
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

import static org.opennms.netmgt.provision.service.ImportJob.MONITOR;

/**
 * CoreImportActivities
 *
 * @author brozow
 * @version $Id: $
 */
@ActivityProvider
public class CoreImportActivities {
    private static final Logger LOG = LoggerFactory.getLogger(CoreImportActivities.class);
    
    private final ProvisionService m_provisionService;
    
    public CoreImportActivities(final ProvisionService provisionService) {
        m_provisionService = provisionService;
    }

    @Activity( lifecycle = "import", phase = "validate", schedulingHint="import")
    public RequisitionImport loadSpecFile(final Resource resource, final ProvisionMonitor monitor) {
        Objects.requireNonNull(monitor);
        final RequisitionImport ri = new RequisitionImport();

        info("Loading requisition from resource {}", resource);
        monitor.beginLoadingResource(resource);
        try {
            final Requisition specFile = m_provisionService.loadRequisition(resource);
            ri.setRequisition(specFile);
            monitor.finishLoadingResource(resource, specFile.getNodeCount());
            debug("Finished loading requisition.");
        } catch (final Throwable t) {
            ri.abort(t);
        }
        return ri;
    }
    
    @Activity( lifecycle = "import", phase = "audit", schedulingHint="import" )
    public ImportOperationsManager auditNodes(final RequisitionImport ri, final String rescanExisting, final ProvisionMonitor monitor) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping audit phase import.");
            return null;
        }
        Objects.requireNonNull(monitor);

        final Requisition specFile = ri.getRequisition();

        info("Auditing nodes for requisition {}. The parameter {} was set to {} during import.", specFile, EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
        monitor.beginAuditNodes();

        final String foreignSource = specFile.getForeignSource();
        final Map<String, Integer> foreignIdsToNodes = m_provisionService.getForeignIdToNodeIdMap(foreignSource);

        final ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, m_provisionService, rescanExisting);
        
        opsMgr.setForeignSource(foreignSource);
        opsMgr.auditNodes(specFile, monitor.getName());

        monitor.finishAuditNodes();
        debug("Finished auditing nodes.");
        return opsMgr;
    }
    
    @Activity( lifecycle = "import", phase = "scan", schedulingHint="import" )
    public static void scanNodes(final Phase currentPhase, final ImportOperationsManager opsMgr, final RequisitionImport ri, final String rescanExisting, final ProvisionMonitor monitor) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping scan phase import.");
            return;
        }
        Objects.requireNonNull(monitor);

        info("Scheduling nodes for phase {}", currentPhase);
        monitor.beginScheduling();
        final Collection<ImportOperation> operations = opsMgr.getOperations();
        
        for(final ImportOperation op : operations) {
            final LifeCycleInstance nodeScan = currentPhase.createNestedLifeCycle("nodeImport");

            debug("Created lifecycle {} for operation {}", nodeScan, op);
            
            nodeScan.setAttribute("operation", op);
            nodeScan.setAttribute("requisitionImport", ri);
            nodeScan.setAttribute("rescanExisting", rescanExisting);
            nodeScan.setAttribute(MONITOR, monitor);
            nodeScan.trigger();
        }
        monitor.finishScheduling();
    }
    
    
    @Activity( lifecycle = "nodeImport", phase = "scan", schedulingHint="import" )
    public static void scanNode(final ImportOperation operation, final RequisitionImport ri, final String rescanExisting, final ProvisionMonitor monitor) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping scan phase nodeImport.");
            return;
        }
        Objects.requireNonNull(monitor);

        if (rescanExisting == null || Boolean.valueOf(rescanExisting) ||
                // scan at import should always be performed for new nodes irrespective of rescanExisting flag.
                operation.getOperationType().equals(ImportOperation.OperationType.INSERT)) {
            info("Running scan phase of {}", operation);
            monitor.beginScanEvent(operation);
            operation.scan();
            monitor.finishScanEvent(operation);
            info("Finished Running scan phase of {}", operation);
        } else {
            info("Skipping scan phase of {}, because the parameter {} was set to {} during import.", operation, EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
        }
    }
    
    @Activity( lifecycle = "nodeImport", phase = "persist" , schedulingHint = "import" )
    public static void persistNode(final ImportOperation operation, final RequisitionImport ri, final ProvisionMonitor monitor) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping persist phase.");
            return;
        }
        Objects.requireNonNull(monitor);

        info("Running persist phase of {}", operation);
        monitor.beginPersisting(operation);
        operation.persist();
        monitor.finishPersisting(operation);
        info("Finished Running persist phase of {}", operation);
    }
    
    @Activity( lifecycle = "import", phase = "relate" , schedulingHint = "import" )
    public void relateNodes(final BatchTask currentPhase, final RequisitionImport ri, final ProvisionMonitor monitor) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping relate phase.");
            return;
        }
        Objects.requireNonNull(monitor);

        info("Running relate phase");
        monitor.beginRelateNodes();
        final Requisition requisition = ri.getRequisition();
        RequisitionVisitor visitor = new AbstractRequisitionVisitor() {
            @Override
            public void visitNode(final OnmsNodeRequisition nodeReq) {
                LOG.debug("Scheduling relate of node {}", nodeReq);
                currentPhase.add(parentSetter(m_provisionService, nodeReq, requisition.getForeignSource()));
            }
        };
        
        requisition.visit(visitor);
        monitor.finishRelateNodes();
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

    protected static void info(String format, Object... args) {
    	LOG.info(format, args);
    }

    protected static void debug(String format, Object... args) {
        LOG.debug(format, args);
    }

    protected static void warn(String format, Object... args) {
        LOG.warn(format, args);
    }
}
