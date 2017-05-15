/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.model.requisition.RequisitionInterfaceEntity;
import org.opennms.netmgt.model.requisition.RequisitionMonitoredServiceEntity;
import org.opennms.netmgt.model.requisition.RequisitionNodeEntity;
import org.opennms.netmgt.provision.service.ProvisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tracks nodes that need to be deleted, inserted, or updated during
 * provisioning import operations.
 *
 * @author david
 */
public class ImportOperationsManager {
    private static final Logger LOG = LoggerFactory.getLogger(ImportOperationsManager.class);

    private final List<ImportOperation> m_inserts = new LinkedList<>();
    private final List<ImportOperation> m_updates = new LinkedList<>();
    
    private final ProvisionService m_provisionService;
    private final Map<String, Integer> m_foreignIdToNodeMap;
    private boolean m_rescanExisting;
    
    private String m_foreignSource;
    
    public ImportOperationsManager(Map<String, Integer> foreignIdToNodeMap, ProvisionService provisionService, final boolean rescanExisting) {
        m_provisionService = provisionService;
        m_foreignIdToNodeMap = new HashMap<>(foreignIdToNodeMap);
        m_rescanExisting = rescanExisting;
    }

    public SaveOrUpdateOperation foundNode(String foreignId, String nodeLabel, String location, String building, String city) {
        SaveOrUpdateOperation ret;
        if (nodeExists(foreignId)) {
            ret = updateNode(foreignId, nodeLabel, location, building, city);
        } else {
            ret = insertNode(foreignId, nodeLabel, location, building, city);
        }        
        return ret;
    }

    private boolean nodeExists(String foreignId) {
        return m_foreignIdToNodeMap.containsKey(foreignId);
    }
    
    private SaveOrUpdateOperation insertNode(final String foreignId, final String nodeLabel, final String location, final String building, final String city) {
        SaveOrUpdateOperation insertOperation = new InsertOperation(getForeignSource(), foreignId, nodeLabel, location, building, city, m_provisionService);
        m_inserts.add(insertOperation);
        return insertOperation;
    }

    private SaveOrUpdateOperation updateNode(final String foreignId, final String nodeLabel, final String location, final String building, final String city) {
        final Integer nodeId = processForeignId(foreignId);
        final UpdateOperation updateOperation;
        if (m_rescanExisting) {
            updateOperation = new UpdateOperation(nodeId, getForeignSource(), foreignId, nodeLabel, location, building, city, m_provisionService, m_rescanExisting);
        } else {
            updateOperation = new NullUpdateOperation(nodeId, getForeignSource(), foreignId, nodeLabel, location, building, city, m_provisionService, m_rescanExisting);
        }
        m_updates.add(updateOperation);
        return updateOperation;
    }

    /**
     * Return NodeId and remove it from the Map so we know which nodes have been operated on thereby
     * tracking nodes to be deleted.
     * @param foreignId
     * @return a nodeId
     */
    private Integer processForeignId(String foreignId) {
        return m_foreignIdToNodeMap.remove(foreignId);
    }
    
    public int getOperationCount() {
        return m_inserts.size() + m_updates.size() + m_foreignIdToNodeMap.size();
    }
    
    public int getInsertCount() {
    	return m_inserts.size();
    }

    public int  getUpdateCount() {
        return m_updates.size();
    }

    public int getDeleteCount() {
    	return m_foreignIdToNodeMap.size();
    }

    public void shutdownAndWaitForCompletion(ExecutorService executorService, String msg) {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // loop util the await returns false
            }
        } catch (final InterruptedException e) {
            LOG.error(msg, e);
            Thread.currentThread().interrupt();
        }
    }
    
    public Collection<ImportOperation> getOperations() {
        final List<DeleteOperation> deletes = m_foreignIdToNodeMap.entrySet()
                .stream()
                .map(entry -> new DeleteOperation(entry.getValue(), m_provisionService))
                .collect(Collectors.toList());

        final List<ImportOperation> operations = new ArrayList<>();
        operations.addAll(deletes);
        operations.addAll(m_updates);
        operations.addAll(m_inserts);

        return operations;
    }
    
    @SuppressWarnings("unused")
    private Runnable sequence(final Executor pool, final Runnable a, final Runnable b) {
        return new Runnable() {
            @Override
            public void run() {
                a.run();
                pool.execute(b);
            }
        };
    }

    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public boolean getRescanExisting() {
        return m_rescanExisting;
    }

    public void auditNodes(RequisitionEntity requisition) {
        for (RequisitionNodeEntity node : requisition.getNodes()) {
            final SaveOrUpdateOperation importOperation = foundNode(node.getForeignId(), node.getNodeLabel(), node.getLocation(), node.getBuilding(), node.getCity());

            for (RequisitionInterfaceEntity eachInterface : node.getInterfaces()) {
                importOperation.foundInterface(
                        eachInterface.getIpAddress().trim(),
                        eachInterface.getDescription(),
                        eachInterface.getSnmpPrimary(),
                        eachInterface.isManaged(),
                        eachInterface.getStatus());

                for (RequisitionMonitoredServiceEntity eachService : eachInterface.getMonitoredServices()) {
                    importOperation.foundMonitoredService(eachService.getServiceName());
                }
            }
            node.getCategories().forEach(c -> importOperation.foundCategory(c));
            node.getAssets().entrySet().forEach(e -> importOperation.foundAsset(e.getKey(), e.getValue()));
        }
    }

    @SuppressWarnings("unused")
    private Runnable persister(final ImportOperation oper) {
        Runnable r = new Runnable() {
                @Override
        	public void run() {
        		oper.persist();
        	}
        };
        return r;
    }
    
    @SuppressWarnings("unused")
    private Runnable scanner(final ImportOperation oper) {
        return new Runnable() {
            @Override
            public void run() {
                LOG.info("Preprocess: {}", oper);
                oper.scan();
            }
        };
    }
}
