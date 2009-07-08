/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.provision.persist.AbstractRequisitionVisitor;
import org.opennms.netmgt.provision.persist.OnmsAssetRequisition;
import org.opennms.netmgt.provision.persist.OnmsIpInterfaceRequisition;
import org.opennms.netmgt.provision.persist.OnmsMonitoredServiceRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation;

public class RequisitionAccountant extends AbstractRequisitionVisitor {
	private final ImportOperationsManager m_opsMgr;
    private SaveOrUpdateOperation m_currentOp;
        
    public RequisitionAccountant(ImportOperationsManager opsMgr) {
        m_opsMgr = opsMgr;
    }
    
    @Override
    public void visitNode(OnmsNodeRequisition nodeReq) {
        m_currentOp = m_opsMgr.foundNode(nodeReq.getForeignId(), nodeReq.getNodeLabel(), nodeReq.getBuilding(), nodeReq.getCity());        
    }
    
    @Override
    public void completeNode(OnmsNodeRequisition nodeReq) {
        m_currentOp = null;
    }

    @Override
    public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
        m_currentOp.foundInterface(ifaceReq.getIpAddr().trim(), ifaceReq.getDescr(), ifaceReq.getSnmpPrimary(), ifaceReq.getManaged(), ifaceReq.getStatus());
        
    }
    
    @Override
    public void visitMonitoredService(OnmsMonitoredServiceRequisition svcReq) {
        m_currentOp.foundMonitoredService(svcReq.getServiceName());
    }

    @Override
    public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
        m_currentOp.foundCategory(catReq.getName());
    }

    @Override
    public void visitAsset(OnmsAssetRequisition assetReq) {
        m_currentOp.foundAsset(assetReq.getName(), assetReq.getValue());
    }
}
