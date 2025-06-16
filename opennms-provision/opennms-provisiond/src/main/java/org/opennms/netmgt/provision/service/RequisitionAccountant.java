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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.provision.persist.AbstractRequisitionVisitor;
import org.opennms.netmgt.provision.persist.OnmsAssetRequisition;
import org.opennms.netmgt.provision.persist.OnmsInterfaceMetaDataRequisition;
import org.opennms.netmgt.provision.persist.OnmsIpInterfaceRequisition;
import org.opennms.netmgt.provision.persist.OnmsMonitoredServiceRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeMetaDataRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.OnmsServiceMetaDataRequisition;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequisitionAccountant extends AbstractRequisitionVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionAccountant.class);

    private final ImportOperationsManager m_opsMgr;
    private SaveOrUpdateOperation m_currentOp;
    private String monitorKey;

    private final Set<CompletableFuture<Void>> m_dnsLookups = Collections.synchronizedSet(new HashSet<>());

    /**
     * <p>Constructor for RequisitionAccountant.</p>
     *
     * @param opsMgr a {@link org.opennms.netmgt.provision.service.operations.ImportOperationsManager} object.
     */
    public RequisitionAccountant(ImportOperationsManager opsMgr, String monitorKey) {
        m_opsMgr = opsMgr;
        this.monitorKey = monitorKey;
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitNode(OnmsNodeRequisition nodeReq) {
        m_currentOp = m_opsMgr.foundNode(nodeReq.getForeignId(), nodeReq.getNodeLabel(), nodeReq.getLocation(), nodeReq.getBuilding(), nodeReq.getCity(), monitorKey);
    }

    /** {@inheritDoc} */
    @Override
    public void completeNode(OnmsNodeRequisition nodeReq) {
        m_currentOp = null;
    }

    /** {@inheritDoc} */
    @Override
    public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
        m_currentOp.foundInterface(ifaceReq.getIpAddr(), ifaceReq.getDescr(), ifaceReq.getSnmpPrimary(), ifaceReq.getManaged(), ifaceReq.getStatus(), m_dnsLookups);
        LOG.debug("{} DNS lookups scheduled, {} DNS lookups completed", dnsLookupsTotal(), dnsLookupsCompleted());
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(OnmsMonitoredServiceRequisition svcReq) {
        m_currentOp.foundMonitoredService(svcReq.getServiceName());
    }

    /** {@inheritDoc} */
    @Override
    public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
        m_currentOp.foundCategory(catReq.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void visitAsset(OnmsAssetRequisition assetReq) {
        m_currentOp.foundAsset(assetReq.getName(), assetReq.getValue());
    }

    @Override
    public void visitNodeMetaData(OnmsNodeMetaDataRequisition metaDataReq) {
        m_currentOp.foundNodeMetaData(metaDataReq.getContext(), metaDataReq.getKey(), metaDataReq.getValue());
    }

    @Override
    public void visitInterfaceMetaData(OnmsInterfaceMetaDataRequisition metaDataReq) {
        m_currentOp.foundInterfaceMetaData(metaDataReq.getContext(), metaDataReq.getKey(), metaDataReq.getValue());
    }

    @Override
    public void visitServiceMetaData(OnmsServiceMetaDataRequisition metaDataReq) {
        m_currentOp.foundServiceMetaData(metaDataReq.getContext(), metaDataReq.getKey(), metaDataReq.getValue());
    }

    int dnsLookupsCompleted() {
        return (int) m_dnsLookups.stream().filter(f -> f.isDone()).count();
    }

    int dnsLookupsPending() {
        return dnsLookupsTotal() - dnsLookupsCompleted();
    }

    int dnsLookupsTotal() {
        return m_dnsLookups.size();
    }

    @Override
    public void completeModelImport(Requisition req) {
        LOG.debug("Waiting for {} scheduled DNS lookups, {} DNS lookups pending", dnsLookupsTotal(), dnsLookupsPending());

        m_dnsLookups.stream().map(CompletableFuture::join);

        LOG.debug("All {} scheduled DNS lookups completed", dnsLookupsTotal());
    }
}
