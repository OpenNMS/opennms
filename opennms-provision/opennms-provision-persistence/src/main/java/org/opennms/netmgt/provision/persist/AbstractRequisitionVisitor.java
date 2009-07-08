/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.Requisition;


/**
 * AbstactImportVisitor
 *
 * @author brozow
 */
public class AbstractRequisitionVisitor implements RequisitionVisitor {

    public void completeAsset(OnmsAssetRequisition assetReq) {
    }

    public void completeInterface(OnmsIpInterfaceRequisition ifaceReq) {
    }

    public void completeModelImport(Requisition req) {
    }

    public void completeMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
    }

    public void completeNode(OnmsNodeRequisition nodeReq) {
    }

    public void completeNodeCategory(OnmsNodeCategoryRequisition catReq) {
    }

    public void completeServiceCategory(OnmsServiceCategoryRequisition catReq) {
    }

    public void visitAsset(OnmsAssetRequisition assetReq) {
    }

    public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
    }

    public void visitModelImport(Requisition req) {
    }

    public void visitMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
    }

    public void visitNode(OnmsNodeRequisition nodeReq) {
    }

    public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
    }

    public void visitServiceCategory(OnmsServiceCategoryRequisition catReq) {
    }
}
