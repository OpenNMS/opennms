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
package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.Requisition;


/**
 * AbstactImportVisitor
 *
 * @author brozow
 * @version $Id: $
 */
public abstract class AbstractRequisitionVisitor implements RequisitionVisitor {

    /** {@inheritDoc} */
    @Override
    public void completeAsset(OnmsAssetRequisition assetReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeInterface(OnmsIpInterfaceRequisition ifaceReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeModelImport(Requisition req) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeNode(OnmsNodeRequisition nodeReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeNodeCategory(OnmsNodeCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeServiceCategory(OnmsServiceCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitAsset(OnmsAssetRequisition assetReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitModelImport(Requisition req) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitNode(OnmsNodeRequisition nodeReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitServiceCategory(OnmsServiceCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitNodeMetaData(OnmsNodeMetaDataRequisition metaDataReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeNodeMetaData(OnmsNodeMetaDataRequisition metaDataReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitInterfaceMetaData(OnmsInterfaceMetaDataRequisition metaDataReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeInterfaceMetaData(OnmsInterfaceMetaDataRequisition metaDataReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitServiceMetaData(OnmsServiceMetaDataRequisition metaDataReq) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeServiceMetaData(OnmsServiceMetaDataRequisition metaDataReq) {
    }
}
