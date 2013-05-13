/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.importer;

import org.opennms.netmgt.importer.config.Asset;
import org.opennms.netmgt.importer.config.Category;
import org.opennms.netmgt.importer.config.Interface;
import org.opennms.netmgt.importer.config.MonitoredService;
import org.opennms.netmgt.importer.config.Node;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.operations.SaveOrUpdateOperation;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;

/**
 * <p>ImportAccountant class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ImportAccountant extends AbstractImportVisitor {
	private final ImportOperationsManager m_opsMgr;
    private SaveOrUpdateOperation m_currentOp;
        
    /**
     * <p>Constructor for ImportAccountant.</p>
     *
     * @param opsMgr a {@link org.opennms.netmgt.importer.operations.ImportOperationsManager} object.
     */
    public ImportAccountant(ImportOperationsManager opsMgr) {
        m_opsMgr = opsMgr;
    }
    
    /** {@inheritDoc} */
        @Override
    public void visitNode(Node node) {
        m_currentOp = m_opsMgr.foundNode(node.getForeignId(), node.getNodeLabel(), node.getBuilding(), node.getCity());        
    }
    /** {@inheritDoc} */
        @Override
    public void completeNode(Node node) {
        m_currentOp = null;
    }

    /** {@inheritDoc} */
        @Override
    public void visitInterface(Interface iface) {
        m_currentOp.foundInterface(iface.getIpAddr(), iface.getDescr(), iface.getSnmpPrimary(), iface.getManaged(), iface.getStatus());
        
    }
    
    /** {@inheritDoc} */
        @Override
    public void visitMonitoredService(MonitoredService svc) {
        m_currentOp.foundMonitoredService(svc.getServiceName());
    }

    /** {@inheritDoc} */
        @Override
    public void visitCategory(Category category) {
        m_currentOp.foundCategory(category.getName());
    }

    /** {@inheritDoc} */
        @Override
    public void visitAsset(Asset asset) {
        m_currentOp.foundAsset(asset.getName(), asset.getValue());
    }
}
