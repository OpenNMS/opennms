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

package org.opennms.netmgt.provision.adapters.link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.core.utils.BeanUtils;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * This adapter automatically creates links between nodes based on an expression applied
 * to the node label (hostname)
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@EventListener(name="LinkProvisioningAdapter")
public class LinkProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(LinkProvisioningAdapter.class);

    private static final String ADAPTER_NAME = "LinkAdapter";
    
    @Autowired
    private LinkMatchResolver m_linkMatchResolver;
    
    @Autowired
    private NodeLinkService m_nodeLinkService;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    /**
     * <p>Constructor for LinkProvisioningAdapter.</p>
     */
    public LinkProvisioningAdapter() {
        super(ADAPTER_NAME);
    }
    
    /** {@inheritDoc} */
    @Override
    public void init() {
        super.init();
        Assert.notNull(m_nodeLinkService, "nodeLinkService must not be null");
        Assert.notNull(m_linkMatchResolver, "linkMatchResolver must not be null");
    }

    /** {@inheritDoc} */
    @Override
    public void doAddNode(final int endPointId) {
        final String endPoint1 = m_nodeLinkService.getNodeLabel(endPointId);
        final String endPoint2 = m_linkMatchResolver.getAssociatedEndPoint(endPoint1);
        
        final String nodeLabel = max(endPoint1, endPoint2);
        final String parentNodeLabel = min(endPoint1, endPoint2);
        
        final Integer nodeId = m_nodeLinkService.getNodeId(nodeLabel);
        final Integer parentNodeId = m_nodeLinkService.getNodeId(parentNodeLabel);
        
        LOG.info("running doAddNode on node {} nodeId: {}", nodeLabel, nodeId);
        
        if(nodeId != null && parentNodeId != null){
            LOG.info("Found link between parentNode {} and node {}", parentNodeLabel, nodeLabel);
            m_nodeLinkService.createLink(parentNodeId, nodeId);
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void doUpdateNode(final int nodeid) {
        createLinkForNodeIfNecessary(nodeid);
    }
    
    private void createLinkForNodeIfNecessary(final int nodeid) {
        doAddNode(nodeid);
    }

    /** {@inheritDoc} */
    @Override
    public void doDeleteNode(final int nodeid) {
        //This is handle using cascading deletes from the node table to the datalink table
    }
    
    /** {@inheritDoc} */
    @Override
    public void doNotifyConfigChange(final int nodeid) {
    }
    
    /**
     * <p>dataLinkFailed</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DATA_LINK_FAILED_EVENT_UEI)
    public void dataLinkFailed(final Event event) {
        try{
            updateLinkStatus("dataLinkFailed", event, "B");
        }catch(final Throwable t){
            LOG.debug("Caught an exception in dataLinkFailed", t);
        }finally{
            LOG.debug("Bailing out of dataLinkFailed handler");
        }
    }

    private void updateLinkStatus(final String method, final Event event, final String newStatus) {
        LOG.info("{}: received event {}", method, event.getUei());
        final String endPoint1 = EventUtils.getParm(event, EventConstants.PARM_ENDPOINT1);
        final String endPoint2 = EventUtils.getParm(event, EventConstants.PARM_ENDPOINT2);
        
        Assert.notNull(endPoint1, "Param endPoint1 cannot be null");
        Assert.notNull(endPoint2, "Param endPoint2 cannot be null");
        
        final String nodeLabel = max(endPoint1, endPoint2);
        final String parentNodeLabel = min(endPoint1, endPoint2);
        final Integer nodeId = m_nodeLinkService.getNodeId(nodeLabel);
        final Integer parentNodeId = m_nodeLinkService.getNodeId(parentNodeLabel);
        
        if(nodeId != null && parentNodeId != null) {
            LOG.info("{}: updated link nodeLabel: {}, nodeId: {}, parentLabel: {}, parentId: {} ", method, nodeLabel, nodeId, parentNodeLabel, parentNodeId);
            m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, newStatus);
        }else {
            LOG.info("{}: found no link with parent: {} and node {}", method, parentNodeLabel, nodeLabel);
        }
    }
    
    /**
     * <p>dataLinkRestored</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DATA_LINK_RESTORED_EVENT_UEI)
    public void dataLinkRestored(final Event event){
        try{
            updateLinkStatus("dataLinkRestored", event, "G");
        }catch(final Throwable t){
            LOG.debug("Caught a throwable in dataLinkRestored", t);
        }finally{
            LOG.debug("Bailing out of dataLinkRestored handler");
        }
    }
    
    /**
     * <p>dataLinkUnmanaged</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DATA_LINK_UNMANAGED_EVENT_UEI)
    public void dataLinkUnmanaged(final Event e) {
        try{
            updateLinkStatus("dataLinkUnmanaged", e, "U");
        }catch(final Throwable t){
            LOG.debug("Caught a throwable in dataLinkUnmanaged", t);
        }finally{
            LOG.debug("Bailing out of dataLinkUnmanaged handler");
        }
    }
    
    /**
     * <p>max</p>
     *
     * @param string1 a {@link java.lang.String} object.
     * @param string2 a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String max(final String string1, final String string2) {
        if(string1 == null || (string2 != null && string1.compareTo(string2) < 0)) {
            return string2;
        }else {
            return string1;
        }
    }
    
    /**
     * <p>min</p>
     *
     * @param string1 a {@link java.lang.String} object.
     * @param string2 a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String min(final String string1, final String string2) {
        if(string1 == null || (string2 != null && string1.compareTo(string2) < 0)) {
            return string1;
        }else {
            return string2;
        }
    }
    
    /**
     * <p>setLinkMatchResolver</p>
     *
     * @param linkMatchResolver a {@link org.opennms.netmgt.provision.adapters.link.LinkMatchResolver} object.
     */
    public void setLinkMatchResolver(final LinkMatchResolver linkMatchResolver) {
        m_linkMatchResolver = linkMatchResolver;
    }

    /**
     * <p>getLinkMatchResolver</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.LinkMatchResolver} object.
     */
    public LinkMatchResolver getLinkMatchResolver() {
        return m_linkMatchResolver;
    }

    /**
     * <p>setNodeLinkService</p>
     *
     * @param nodeLinkService a {@link org.opennms.netmgt.provision.adapters.link.NodeLinkService} object.
     */
    public void setNodeLinkService(final NodeLinkService nodeLinkService) {
        m_nodeLinkService = nodeLinkService;
    }
    
}
