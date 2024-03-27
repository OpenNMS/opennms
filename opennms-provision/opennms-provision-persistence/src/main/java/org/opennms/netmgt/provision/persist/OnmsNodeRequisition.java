/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.net.InetAddress;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.NetworkBuilder.NodeBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OnmsNodeRequistion
 *
 * @author brozow
 */
public class OnmsNodeRequisition {
    private static final Logger LOG = LoggerFactory.getLogger(OnmsNodeRequisition.class);
    
    private String m_foreignSource;
    private RequisitionNode m_node;
    private List<OnmsAssetRequisition> m_assetReqs;
    private List<OnmsNodeMetaDataRequisition> m_metaDataReqs;
    private List<OnmsIpInterfaceRequisition> m_ifaceReqs;
    private List<OnmsNodeCategoryRequisition> m_categoryReqs;

    public OnmsNodeRequisition() {
    }

    public OnmsNodeRequisition(final String foreignSource, final RequisitionNode node) {
        m_foreignSource = foreignSource;
        m_node = node;
        m_assetReqs = constructAssetRequistions();
        m_metaDataReqs = constructMetaDataRequistions();
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

    private List<OnmsNodeMetaDataRequisition> constructMetaDataRequistions() {
        return m_node.getMetaData().stream()
                .map(OnmsNodeMetaDataRequisition::new)
                .collect(Collectors.toList());
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

        m_metaDataReqs.forEach(r -> r.visit(visitor));

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
        public void visitNodeMetaData(OnmsNodeMetaDataRequisition metaDataReq) {
            bldr.setNodeMetaDataEntry(metaDataReq.getContext(), metaDataReq.getKey(), metaDataReq.getValue());
        }

        @Override
        public void visitInterfaceMetaData(OnmsInterfaceMetaDataRequisition metaDataReq) {
            bldr.setInterfaceMetaDataEntry(metaDataReq.getContext(), metaDataReq.getKey(), metaDataReq.getValue());
        }

        @Override
        public void visitServiceMetaData(OnmsServiceMetaDataRequisition metaDataReq) {
            bldr.setServiceMetaDataEntry(metaDataReq.getContext(), metaDataReq.getKey(), metaDataReq.getValue());
        }

        @Override
        public void visitNodeCategory(final OnmsNodeCategoryRequisition catReq) {
            bldr.addCategory(catReq.getName());
        }

        @Override
        public void visitInterface(final OnmsIpInterfaceRequisition ifaceReq) {
            final InetAddress ipAddr = ifaceReq.getIpAddr();
            if (ipAddr == null) {
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
            nodeBldr.setLabelSource(NodeLabelSource.USER);
            nodeBldr.setType(NodeType.ACTIVE);
            nodeBldr.setForeignSource(nodeReq.getForeignSource());
            nodeBldr.setForeignId(nodeReq.getForeignId());
            nodeBldr.setLocation(nodeReq.getLocation());
            nodeBldr.getAssetRecord().setBuilding(nodeReq.getBuilding());
            nodeBldr.getAssetRecord().getGeolocation().setCity(nodeReq.getCity());
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

    public String getLocation() {
        return m_node.getLocation();
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
