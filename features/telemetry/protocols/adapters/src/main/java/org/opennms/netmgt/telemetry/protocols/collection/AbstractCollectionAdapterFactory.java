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
