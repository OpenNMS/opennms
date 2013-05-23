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

package org.opennms.netmgt.importer.specification;

import org.opennms.netmgt.importer.config.Asset;
import org.opennms.netmgt.importer.config.Category;
import org.opennms.netmgt.importer.config.Interface;
import org.opennms.netmgt.importer.config.ModelImport;
import org.opennms.netmgt.importer.config.MonitoredService;
import org.opennms.netmgt.importer.config.Node;

/**
 * <p>AbstractImportVisitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AbstractImportVisitor implements ImportVisitor {

    /** {@inheritDoc} */
    @Override
    public void visitModelImport(ModelImport mi) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeModelImport(ModelImport modelImport) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitNode(Node node) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeNode(Node node) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitInterface(Interface iface) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeInterface(Interface iface) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(MonitoredService svc) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeMonitoredService(MonitoredService svc) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitCategory(Category category) {
    }

    /** {@inheritDoc} */
    @Override
    public void completeCategory(Category category) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitAsset(Asset asset) {
    }
    
    /** {@inheritDoc} */
    @Override
    public void completeAsset(Asset asset) {
    }
}
