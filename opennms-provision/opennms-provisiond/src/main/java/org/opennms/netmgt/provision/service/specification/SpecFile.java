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
// Modifications:
//
// 2008 Jul 28: Use CastorUtils. - dj@opennms.org
// 2008 Jul 05: Indent and organize imports. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.provision.service.specification;

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.provision.service.ModelImportException;
import org.springframework.core.io.Resource;

public class SpecFile {

    private ModelImport m_mi;

    public void loadResource(Resource resource) throws ModelImportException, IOException {
        try {
            m_mi = CastorUtils.unmarshal(ModelImport.class, resource);
        } catch (MarshalException e) {
            throw new ModelImportException("Exception while marshalling import: "+e, e);
        } catch (ValidationException e) {
            throw new ModelImportException("Exception while validating import "+e);
        }
    }
    
    public void visitImport(ImportVisitor visitor) {
        doVisitImport(visitor);
    }

    private void doVisitImport(ImportVisitor visitor) {
        visitor.visitModelImport(m_mi);
        for (Node node : m_mi.getNodeCollection()) {
            visitNode(visitor, node);
        }
        visitor.completeModelImport(m_mi);
    }

    private void visitNode(final ImportVisitor visitor, final Node node) {
        doVisitNode(visitor, node);
    }

    private void doVisitNode(ImportVisitor visitor, Node node) {
        visitor.visitNode(node);
        for (Category category : node.getCategoryCollection()) {
            visitCategory(visitor, category);
        }
        for (Interface iface : node.getInterfaceCollection()) {
            visitInterface(visitor, iface);
        }
        visitor.completeNode(node);
    }

    private void visitCategory(ImportVisitor visitor, Category category) {
        doVisitCategory(visitor, category);
    }

    private void doVisitCategory(ImportVisitor visitor, Category category) {
        visitor.visitCategory(category);
        visitor.completeCategory(category);
    }

    private void visitInterface(ImportVisitor visitor, Interface iface) {
        doVisitInterface(visitor, iface);
    }

    private void doVisitInterface(ImportVisitor visitor, Interface iface) {
        visitor.visitInterface(iface);
        for (MonitoredService svc : iface.getMonitoredServiceCollection()) {
            visitMonitoredService(visitor, svc);
        }
        visitor.completeInterface(iface);
    }

    private void visitMonitoredService(ImportVisitor visitor, MonitoredService svc) {
        doVisitMonitoredService(visitor, svc);
    }

    private void doVisitMonitoredService(ImportVisitor visitor, MonitoredService svc) {
        visitor.visitMonitoredService(svc);
        visitor.completeMonitoredService(svc);
    }

    public String getForeignSource() {
        return m_mi.getForeignSource();
    }

    public void setForeignSource(String foreignSource) {
        m_mi.setForeignSource(foreignSource);
    }

}
