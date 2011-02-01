//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.Requisition;


/**
 * <p>RequisitionVisitor interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
