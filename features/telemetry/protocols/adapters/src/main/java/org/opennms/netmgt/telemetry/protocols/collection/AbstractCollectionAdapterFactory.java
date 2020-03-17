/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.collection;

import java.util.Objects;

import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.telemetry.api.adapter.AdapterFactory;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionOperations;

public abstract class AbstractCollectionAdapterFactory extends AbstractAdapterFactory {

    @Autowired
    private CollectionAgentFactory collectionAgentFactory;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionOperations transactionTemplate;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private PersisterFactory persisterFactory;

    @Autowired
    private ThresholdingService thresholdingService;

    public AbstractCollectionAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    public CollectionAgentFactory getCollectionAgentFactory() {
        return collectionAgentFactory;
    }

    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public TransactionOperations getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public FilterDao getFilterDao() {
        return filterDao;
    }

    public void setFilterDao(FilterDao filterDao) {
        this.filterDao = filterDao;
    }

    public PersisterFactory getPersisterFactory() {
        return persisterFactory;
    }

    public void setPersisterFactory(PersisterFactory persisterFactory) {
        this.persisterFactory = persisterFactory;
    }

    public ThresholdingService getThresholdingService() {
        return thresholdingService;
    }

    public void setThresholdingService(ThresholdingService thresholdingService) {
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
    }
}
