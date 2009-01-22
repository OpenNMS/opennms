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
package org.opennms.netmgt.provision.persist;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.springframework.core.io.Resource;

public class OnmsRequisition {

    private ModelImport m_mi;
    
    private Map<String, OnmsNodeRequisition> m_nodeReqs = new LinkedHashMap<String, OnmsNodeRequisition>();

    public void loadResource(Resource resource) {
        m_mi = CastorUtils.unmarshalWithTranslatedExceptions(ModelImport.class, resource);
        
        for(Node node : m_mi.getNodeCollection()) {
            m_nodeReqs.put(node.getForeignId(), new OnmsNodeRequisition(node));
        }
    }

    public void saveResource(Resource resource) {
        CastorUtils.marshalWithTranslatedExceptionsViaString(m_mi, resource);
    }

    public void visit(RequisitionVisitor visitor) {
        visitor.visitModelImport(this);
        
        for(OnmsNodeRequisition nodeReq : m_nodeReqs.values()) {
            nodeReq.visit(visitor);
        }
        
        visitor.completeModelImport(this);
    }

    public String getForeignSource() {
        return m_mi.getForeignSource();
    }

    public void setForeignSource(String foreignSource) {
        m_mi.setForeignSource(foreignSource);
    }

    public OnmsNodeRequisition getNodeRequistion(String foreignId) {
        return m_nodeReqs.get(foreignId);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("foreign-source", getForeignSource())
            .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OnmsRequisition) {
            OnmsRequisition other = (OnmsRequisition) obj;
            return new EqualsBuilder()
                .append(getForeignSource(), other.getForeignSource())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getForeignSource())
            .append(m_mi)
            .toHashCode();
      }



}
