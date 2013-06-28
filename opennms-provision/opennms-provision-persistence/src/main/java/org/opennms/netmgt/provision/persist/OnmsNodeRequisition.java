/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.NetworkBuilder.NodeBuilder;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OnmsNodeRequistion
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsNodeRequisition {
    
    private static final Logger LOG = LoggerFactory.getLogger(OnmsNodeRequisition.class);
    
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
    public OnmsNodeRequisition(final String foreignSource, final RequisitionNode node) {
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
    	final List<OnmsAssetRequisition> reqs = new ArrayList<OnmsAssetRequisition>(m_node.getAssets().size());
        for(final RequisitionAsset asset : m_node.getAssets()) {
            reqs.add(new OnmsAssetRequisition(asset));
        }
        return reqs;
    }

    private List<OnmsIpInterfaceRequisition> constructIpInterfaceRequistions() {
    	final List<OnmsIpInterfaceRequisition> reqs = new ArrayList<OnmsIpInterfaceRequisition>(m_node.getInterfaces().size());
        for(final RequisitionInterface iface : m_node.getInterfaces()) {
            reqs.add(new OnmsIpInterfaceRequisition(iface));
        }
        return reqs;
    }

    private List<OnmsNodeCategoryRequisition> constructCategoryRequistions() {
    	final List<OnmsNodeCategoryRequisition> reqs = new ArrayList<OnmsNodeCategoryRequisition>(m_node.getCategories().size());
        for(final RequisitionCategory category : m_node.getCategories()) {
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
    public void visit(final RequisitionVisitor visitor) {
        visitor.visitNode(this);
        for (final OnmsNodeCategoryRequisition catReq : m_categoryReqs) {
            catReq.visit(visitor);
        }
        for(final OnmsIpInterfaceRequisition ipReq : m_ifaceReqs) {
            ipReq.visit(visitor);
        }
        for(final OnmsAssetRequisition assetReq : m_assetReqs) {
            assetReq.visit(visitor);
        }
        visitor.completeNode(this);
    }
    
    private static class OnmsNodeBuilder extends AbstractRequisitionVisitor {
        private NetworkBuilder bldr = new NetworkBuilder();
        
        public OnmsNode getNode() {
            return bldr.getCurrentNode();
        }

        @Override
        public void visitAsset(final OnmsAssetRequisition assetReq) {
            bldr.setAssetAttribute(assetReq.getName(), assetReq.getValue());
        }

        @Override
        public void visitNodeCategory(final OnmsNodeCategoryRequisition catReq) {
            bldr.addCategory(catReq.getName());
        }

        @Override
        public void visitInterface(final OnmsIpInterfaceRequisition ifaceReq) {
        	final String ipAddr = ifaceReq.getIpAddr();
            if (ipAddr == null || "".equals(ipAddr)) {
                bldr.clearInterface();
                LOG.error("Found interface on node {} with an empty ipaddr! Ignoring!", bldr.getCurrentNode().getLabel());
                return;
            }

            final InterfaceBuilder ifBldr = bldr.addInterface(ipAddr);
            ifBldr.setIsManaged(ifaceReq.getStatus() == 3 ? "U" : "M");
            ifBldr.setIsSnmpPrimary(ifaceReq.getSnmpPrimary().getCode());
            
        }

        @Override
        public void visitMonitoredService(final OnmsMonitoredServiceRequisition monSvcReq) {
            bldr.addService(monSvcReq.getServiceName());
        }

        @Override
        public void visitNode(final OnmsNodeRequisition nodeReq) {
        	final NodeBuilder nodeBldr = bldr.addNode(nodeReq.getNodeLabel());
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
    	final OnmsNodeBuilder visitor = new OnmsNodeBuilder();
        visit(visitor);
        return visitor.getNode();
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
     * @see org.opennms.netmgt.provision.persist.NodeRequisition#getParentForeignSource()
     */
    /**
     * <p>getParentForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentForeignSource() {
        return m_node.getParentForeignSource();
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
