/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.Requisition;


/**
 * AbstactImportVisitor
 *
 * @author brozow
 * @version $Id: $
 */
public class AbstractRequisitionVisitor implements RequisitionVisitor {

    /** {@inheritDoc} */
    public void completeAsset(OnmsAssetRequisition assetReq) {
    }

    /** {@inheritDoc} */
    public void completeInterface(OnmsIpInterfaceRequisition ifaceReq) {
    }

    /** {@inheritDoc} */
    public void completeModelImport(Requisition req) {
    }

    /** {@inheritDoc} */
    public void completeMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
    }

    /** {@inheritDoc} */
    public void completeNode(OnmsNodeRequisition nodeReq) {
    }

    /** {@inheritDoc} */
    public void completeNodeCategory(OnmsNodeCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    public void completeServiceCategory(OnmsServiceCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    public void visitAsset(OnmsAssetRequisition assetReq) {
    }

    /** {@inheritDoc} */
    public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
    }

    /** {@inheritDoc} */
    public void visitModelImport(Requisition req) {
    }

    /** {@inheritDoc} */
    public void visitMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
    }

    /** {@inheritDoc} */
    public void visitNode(OnmsNodeRequisition nodeReq) {
    }

    /** {@inheritDoc} */
    public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
    }

    /** {@inheritDoc} */
    public void visitServiceCategory(OnmsServiceCategoryRequisition catReq) {
    }
}
