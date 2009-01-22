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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.modelimport.Asset;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.Node;

/**
 * OnmsNodeRequistion
 *
 * @author brozow
 */
public class OnmsNodeRequisition {
    
    private Node m_node;
    private List<OnmsAssetRequisition> m_assetReqs;
    private List<OnmsIpInterfaceRequisition> m_ifaceReqs;
    private List<OnmsCategoryRequisition> m_categoryReqs;

    public OnmsNodeRequisition(Node node) {
        m_node = node;
        m_assetReqs = constructAssetRequistions();
        m_ifaceReqs = constructIpInterfaceRequistions();
        m_categoryReqs = constructCategoryRequistions();
    }
    
    private List<OnmsAssetRequisition> constructAssetRequistions() {
        List<OnmsAssetRequisition> reqs = new ArrayList<OnmsAssetRequisition>(m_node.getAssetCount());
        for(Asset asset : m_node.getAssetCollection()) {
            reqs.add(new OnmsAssetRequisition(asset));
        }
        return reqs;
    }

    private List<OnmsIpInterfaceRequisition> constructIpInterfaceRequistions() {
        List<OnmsIpInterfaceRequisition> reqs = new ArrayList<OnmsIpInterfaceRequisition>(m_node.getInterfaceCount());
        for(Interface iface : m_node.getInterfaceCollection()) {
            reqs.add(new OnmsIpInterfaceRequisition(iface));
        }
        return reqs;
    }

    private List<OnmsCategoryRequisition> constructCategoryRequistions() {
        List<OnmsCategoryRequisition> reqs = new ArrayList<OnmsCategoryRequisition>(m_node.getCategoryCount());
        for(Category category : m_node.getCategoryCollection()) {
            reqs.add(new OnmsCategoryRequisition(category));
        }
        return reqs;
    }

    void visit(RequisitionVisitor visitor) {
        visitor.visitNode(this);
        for (OnmsCategoryRequisition catReq : m_categoryReqs) {
            catReq.visit(visitor);
        }
        for(OnmsIpInterfaceRequisition ipReq : m_ifaceReqs) {
            ipReq.visit(visitor);
        }
        for(OnmsAssetRequisition assetReq : m_assetReqs) {
            assetReq.visit(visitor);
        }
        visitor.completeNode(this);
    }

    public String getNodeLabel() {
        return m_node.getNodeLabel();
    }

    public String getForeignId() {
        return m_node.getForeignId();
    }

    public String getBuilding() {
        return m_node.getBuilding();
    }

    public String getCity() {
        return m_node.getCity();
    }

    public String getParentForeignId() {
        return m_node.getParentForeignId();
    }

    public String getParentNodeLabel() {
        return m_node.getParentNodeLabel();
    }
    
    
    
    

}
