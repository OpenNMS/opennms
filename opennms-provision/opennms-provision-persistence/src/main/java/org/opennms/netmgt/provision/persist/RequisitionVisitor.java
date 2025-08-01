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

public interface RequisitionVisitor {
    
    /**
     * <p>visitModelImport</p>
     *
     * @param req a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    public void visitModelImport(Requisition req);
    /**
     * <p>completeModelImport</p>
     *
     * @param req a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    public void completeModelImport(Requisition req);
    /**
     * <p>visitNode</p>
     *
     * @param nodeReq a {@link org.opennms.netmgt.provision.persist.OnmsNodeRequisition} object.
     */
    public void visitNode(OnmsNodeRequisition nodeReq);
    /**
     * <p>completeNode</p>
     *
     * @param nodeReq a {@link org.opennms.netmgt.provision.persist.OnmsNodeRequisition} object.
     */
    public void completeNode(OnmsNodeRequisition nodeReq);
    /**
     * <p>visitInterface</p>
     *
     * @param ifaceReq a {@link org.opennms.netmgt.provision.persist.OnmsIpInterfaceRequisition} object.
     */
    public void visitInterface(OnmsIpInterfaceRequisition ifaceReq);
    /**
     * <p>completeInterface</p>
     *
     * @param ifaceReq a {@link org.opennms.netmgt.provision.persist.OnmsIpInterfaceRequisition} object.
     */
    public void completeInterface(OnmsIpInterfaceRequisition ifaceReq);
    /**
     * <p>visitMonitoredService</p>
     *
     * @param monSvcReq a {@link org.opennms.netmgt.provision.persist.OnmsMonitoredServiceRequisition} object.
     */
    public void visitMonitoredService(OnmsMonitoredServiceRequisition monSvcReq);
    /**
     * <p>completeMonitoredService</p>
     *
     * @param monSvcReq a {@link org.opennms.netmgt.provision.persist.OnmsMonitoredServiceRequisition} object.
     */
    public void completeMonitoredService(OnmsMonitoredServiceRequisition monSvcReq);
    /**
     * <p>visitNodeCategory</p>
     *
     * @param catReq a {@link org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition} object.
     */
    public void visitNodeCategory(OnmsNodeCategoryRequisition catReq);
    /**
     * <p>completeNodeCategory</p>
     *
     * @param catReq a {@link org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition} object.
     */
    public void completeNodeCategory(OnmsNodeCategoryRequisition catReq);
    /**
     * <p>visitServiceCategory</p>
     *
     * @param catReq a {@link org.opennms.netmgt.provision.persist.OnmsServiceCategoryRequisition} object.
     */
    public void visitServiceCategory(OnmsServiceCategoryRequisition catReq);
    /**
     * <p>completeServiceCategory</p>
     *
     * @param catReq a {@link org.opennms.netmgt.provision.persist.OnmsServiceCategoryRequisition} object.
     */
    public void completeServiceCategory(OnmsServiceCategoryRequisition catReq);
    /**
     * <p>visitAsset</p>
     *
     * @param assetReq a {@link org.opennms.netmgt.provision.persist.OnmsAssetRequisition} object.
     */
    public void visitAsset(OnmsAssetRequisition assetReq);
    /**
     * <p>completeAsset</p>
     *
     * @param assetReq a {@link org.opennms.netmgt.provision.persist.OnmsAssetRequisition} object.
     */
    public void completeAsset(OnmsAssetRequisition assetReq);

    public void visitNodeMetaData(OnmsNodeMetaDataRequisition metaDataReq);
    public void completeNodeMetaData(OnmsNodeMetaDataRequisition metaDataReq);
    public void visitInterfaceMetaData(OnmsInterfaceMetaDataRequisition metaDataReq);
    public void completeInterfaceMetaData(OnmsInterfaceMetaDataRequisition metaDataReq);
    public void visitServiceMetaData(OnmsServiceMetaDataRequisition metaDataReq);
    public void completeServiceMetaData(OnmsServiceMetaDataRequisition metaDataReq);
}
