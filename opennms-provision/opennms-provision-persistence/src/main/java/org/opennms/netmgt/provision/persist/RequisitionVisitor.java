/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

}
