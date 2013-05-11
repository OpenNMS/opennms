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
}
