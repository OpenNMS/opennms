/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
