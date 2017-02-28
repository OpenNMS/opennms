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

package org.opennms.netmgt.provision.service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import org.opennms.core.tasks.BatchTask;
import org.opennms.netmgt.dao.api.RequisitionDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.model.requisition.RequisitionNodeEntity;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMerger;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.RequisitionImportContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionOperations;

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

    @Autowired
    private TransactionOperations m_transactionOperations;

    @Autowired
    private RequisitionService requisitionService;

    @Autowired
    private RequisitionDao requisitionDao;

    @Autowired
    private RequisitionMerger requisitionMerger;

    public CoreImportActivities(final ProvisionService provisionService) {
        m_provisionService = provisionService;
    }

    @Activity( lifecycle = "import", phase = "validate", schedulingHint="import")
    public void importRequisition(final RequisitionImportContext context) {
        // Even if this is the "validate" phase it is actually persisting the requisition
        // if there is already a requisition, it is overwritten
        m_transactionOperations.execute(status -> {
            try {
                debug("Started requisition import");

                final ImportRequest request = context.getImportRequest();
                final RequisitionProvider provider = determineRequisitionProvider(request);
                LOG.info("importRequisition: importType: {}", provider.getClass());
                final RequisitionEntity requisitionToImport = provider.getRequisition();

                info("Importing requisition {}", requisitionToImport.getName());
                requisitionToImport.updateLastImported();
                requisitionService.saveOrUpdateRequisition(requisitionToImport);
                context.setForeignSource(requisitionToImport.getName());
                debug("Finished requisition import.");
            } catch (final Throwable t) {
                context.abort(t);
            }
            return null;
        });
    }

    private RequisitionProvider determineRequisitionProvider(ImportRequest request) throws MalformedURLException, URISyntaxException {
        if (request.getForeignSource() != null) {
            return new DatabaseRequisitionProvider(requisitionDao, request.getForeignSource());
        } else {
            return new ResourceRequisitionProvider(request.getUrl(), requisitionMerger);
        }
    }

    @Activity( lifecycle = "import", phase = "audit", schedulingHint="import" )
    public ImportOperationsManager auditNodes(final RequisitionImportContext ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping audit phase import.");
            return null;
        }

        return m_transactionOperations.execute(status -> {
            final RequisitionEntity requisition = requisitionService.getRequisition(ri.getForeignSource());

            info("Auditing nodes for requisition {}. The parameter {} was set to {} during import.", requisition, EventConstants.PARM_IMPORT_RESCAN_EXISTING, ri.isRescanExisting());

            final String foreignSource = requisition.getForeignSource();
            final Map<String, Integer> foreignIdsToNodes = m_provisionService.getForeignIdToNodeIdMap(foreignSource);

            final ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, m_provisionService, ri.isRescanExisting());

            opsMgr.setForeignSource(foreignSource);
            opsMgr.auditNodes(requisition);

            debug("Finished auditing nodes.");

            return opsMgr;
        });
    }
    
    @Activity( lifecycle = "import", phase = "scan", schedulingHint="import" )
    public static void scanNodes(final Phase currentPhase, final ImportOperationsManager opsMgr, final RequisitionImportContext ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping scan phase import.");
            return;
        }

        info("Scheduling nodes for phase {}", currentPhase);
        
        final Collection<ImportOperation> operations = opsMgr.getOperations();
        
        for(final ImportOperation op : operations) {
            final LifeCycleInstance nodeScan = currentPhase.createNestedLifeCycle("nodeImport");

            debug("Created lifecycle {} for operation {}", nodeScan, op);
            
            nodeScan.setAttribute("operation", op);
            nodeScan.setAttribute("requisitionImport", ri);
            nodeScan.trigger();
        }


    }
    
    @Activity( lifecycle = "nodeImport", phase = "scan", schedulingHint="import" )
    public static void scanNode(final ImportOperation operation, final RequisitionImportContext ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping scan phase nodeImport.");
            return;
        }

        if (ri.isRescanExisting()) {
            info("Running scan phase of {}, the parameter {} was set to {} during import.", operation, EventConstants.PARM_IMPORT_RESCAN_EXISTING, ri.isRescanExisting());
            operation.scan();
    
            info("Finished Running scan phase of {}", operation);
        } else {
            info("Skipping scan phase of {}, because the parameter {} was set to {} during import.", operation, EventConstants.PARM_IMPORT_RESCAN_EXISTING, ri.isRescanExisting());
        }
    }
    
    @Activity( lifecycle = "nodeImport", phase = "persist" , schedulingHint = "import" )
    public static void persistNode(final ImportOperation operation, final RequisitionImportContext ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping persist phase.");
            return;
        }

        info("Running persist phase of {}", operation);
        operation.persist();
        info("Finished Running persist phase of {}", operation);

    }
    
    @Activity( lifecycle = "import", phase = "relate" , schedulingHint = "import" )
    public void relateNodes(final BatchTask currentPhase, final RequisitionImportContext ri) {
        if (ri.isAborted()) {
            info("The import has been aborted, skipping relate phase.");
            return;
        }

        m_transactionOperations.execute(status -> {
            LOG.info("Running relate phase");

            final RequisitionEntity requisition = requisitionService.getRequisition(ri.getForeignSource());
            requisition.getNodes().forEach(nodeReq -> {
                LOG.debug("Scheduling relate of node {}", nodeReq);
                currentPhase.add(parentSetter(m_provisionService, nodeReq, requisition.getForeignSource()));
            });

            LOG.info("Finished Running relate phase");
            return null;
        });
    }
    
    private static Runnable parentSetter(final ProvisionService provisionService, final RequisitionNodeEntity nodeReq, final String foreignSource) {
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
                    nodeReq.getParentForeignSource() == null ? foreignSource : nodeReq.getParentForeignSource(),
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
