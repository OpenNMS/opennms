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

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.NetworkBuilder.NodeBuilder;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

/**
 * OnmsNodeRequistion
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsNodeRequisition {
    
    private String m_foreignSource;
    private RequisitionNode m_node;
    private List<OnmsAssetRequisition> m_assetReqs;
    private List<OnmsIpInterfaceRequisition> m_ifaceReqs;
    private List<OnmsNodeCategoryRequisition> m_categoryReqs;

    /**
     * <p>Constructor for OnmsNodeRequisition.</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNode} object.
     */
    public OnmsNodeRequisition(String foreignSource, RequisitionNode node) {
        m_foreignSource = foreignSource;
        m_node = node;
        m_assetReqs = constructAssetRequistions();
        m_ifaceReqs = constructIpInterfaceRequistions();
        m_categoryReqs = constructCategoryRequistions();
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getForeignSource()
     */
    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }
    
    private List<OnmsAssetRequisition> constructAssetRequistions() {
        List<OnmsAssetRequisition> reqs = new ArrayList<OnmsAssetRequisition>(m_node.getAssets().size());
        for(RequisitionAsset asset : m_node.getAssets()) {
            reqs.add(new OnmsAssetRequisition(asset));
        }
        return reqs;
    }

    private List<OnmsIpInterfaceRequisition> constructIpInterfaceRequistions() {
        List<OnmsIpInterfaceRequisition> reqs = new ArrayList<OnmsIpInterfaceRequisition>(m_node.getInterfaces().size());
        for(RequisitionInterface iface : m_node.getInterfaces()) {
            reqs.add(new OnmsIpInterfaceRequisition(iface));
        }
        return reqs;
    }

    private List<OnmsNodeCategoryRequisition> constructCategoryRequistions() {
        List<OnmsNodeCategoryRequisition> reqs = new ArrayList<OnmsNodeCategoryRequisition>(m_node.getCategories().size());
        for(RequisitionCategory category : m_node.getCategories()) {
            reqs.add(new OnmsNodeCategoryRequisition(category));
        }
        return reqs;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#visit(org.opennms.netmgt.provision.persist.RequisitionVisitor)
     */
    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.provision.persist.RequisitionVisitor} object.
     */
    public void visit(RequisitionVisitor visitor) {
        visitor.visitNode(this);
        for (OnmsNodeCategoryRequisition catReq : m_categoryReqs) {
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
    
    private class OnmsNodeBuilder extends AbstractRequisitionVisitor {
        private NetworkBuilder bldr = new NetworkBuilder();
        
        public OnmsNode getNode() {
            return bldr.getCurrentNode();
        }

        @Override
        public void visitAsset(OnmsAssetRequisition assetReq) {
            bldr.setAssetAttribute(assetReq.getName(), assetReq.getValue());
        }

        @Override
        public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
            bldr.addCategory(catReq.getName());
        }

        @Override
        public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
            
            String ipAddr = ifaceReq.getIpAddr();
            if (ipAddr == null || "".equals(ipAddr)) {
                bldr.clearInterface();
                String msg = String.format("Found interface on node %s with an empty ipaddr! Ignoring!", bldr.getCurrentNode().getLabel());
                log().error(msg);
                return;
            }

            InterfaceBuilder ifBldr = bldr.addInterface(ipAddr);
            ifBldr.setIsManaged(ifaceReq.getStatus() == 3 ? "U" : "M");
            ifBldr.setIsSnmpPrimary(ifaceReq.getSnmpPrimary());
            
        }

        @Override
        public void visitMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
            bldr.addService(monSvcReq.getServiceName());
        }

        @Override
        public void visitNode(OnmsNodeRequisition nodeReq) {
            
            NodeBuilder nodeBldr = bldr.addNode(nodeReq.getNodeLabel());
            nodeBldr.setLabelSource("U");
            nodeBldr.setType("A");
            nodeBldr.setForeignSource(nodeReq.getForeignSource());
            nodeBldr.setForeignId(nodeReq.getForeignId());
            nodeBldr.getAssetRecord().setBuilding(nodeReq.getBuilding());
            nodeBldr.getAssetRecord().setCity(nodeReq.getCity());
                
        }
        
        
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#constructOnmsNodeFromRequisition()
     */
    /**
     * <p>constructOnmsNodeFromRequisition</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsNode constructOnmsNodeFromRequisition() {
        OnmsNodeBuilder visitor = new OnmsNodeBuilder();
        visit(visitor);
        return visitor.getNode();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#log()
     */
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getNodeLabel()
     */
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_node.getNodeLabel();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getForeignId()
     */
    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        return m_node.getForeignId();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getBuilding()
     */
    /**
     * <p>getBuilding</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBuilding() {
        return m_node.getBuilding();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getCity()
     */
    /**
     * <p>getCity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCity() {
        return m_node.getCity();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getParentForeignId()
     */
    /**
     * <p>getParentForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentForeignId() {
        return m_node.getParentForeignId();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getParentNodeLabel()
     */
    /**
     * <p>getParentNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentNodeLabel() {
        return m_node.getParentNodeLabel();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getNode()
     */
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNode} object.
     */
    public RequisitionNode getNode() {
        return m_node;
    }
    
}
