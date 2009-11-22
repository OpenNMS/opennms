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
 * Modifications:
 * 
 * Created: September 24, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision.adapters.link;

import static org.opennms.core.utils.LogUtils.debugf;

import org.apache.log4j.Category;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * This adapter automatically creates links between nodes based on an expression applied
 * to the node label (hostname)
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@EventListener(name="LinkProvisioningAdapter")
public class LinkProvisioningAdapter extends SimplerQueuedProvisioningAdapter {

    private static final String ADAPTER_NAME = "LinkAdapter";
    
    @Autowired
    private LinkMatchResolver m_linkMatchResolver;
    
    @Autowired
    private NodeLinkService m_nodeLinkService;
    
    public LinkProvisioningAdapter() {
        super(ADAPTER_NAME);
    }
    
    @Override
    public void init() {
        super.init();
        Assert.notNull(m_nodeLinkService, "nodeLinkService must not be null");
        Assert.notNull(m_linkMatchResolver, "linkMatchResolver must not be null");
    }

    public void doAddNode(int endPointId) {
        String endPoint1 = m_nodeLinkService.getNodeLabel(endPointId);
        String endPoint2 = m_linkMatchResolver.getAssociatedEndPoint(endPoint1);
        
        String nodeLabel = max(endPoint1, endPoint2);
        String parentNodeLabel = min(endPoint1, endPoint2);
        
        Integer nodeId = m_nodeLinkService.getNodeId(nodeLabel);
        Integer parentNodeId = m_nodeLinkService.getNodeId(parentNodeLabel);
        
        log().info(String.format("running doAddNode on node %s nodeId: %d", nodeLabel, nodeId));
        
        if(nodeId != null && parentNodeId != null){
            log().info(String.format("Found link between parentNode %s and node %s", parentNodeLabel, nodeLabel));
            m_nodeLinkService.createLink(parentNodeId, nodeId);
        }
        
    }
    
    public void doUpdateNode(int nodeid) {
        createLinkForNodeIfNecessary(nodeid);
    }
    
    private void createLinkForNodeIfNecessary(int nodeid) {
        doAddNode(nodeid);
    }

    public void doDeleteNode(int nodeid) {
        //This is handle using cascading deletes from the node table to the datalink table
    }
    
    public void doNotifyConfigChange(int nodeid) {
        
    }
    
    @EventHandler(uei=EventConstants.DATA_LINK_FAILED_EVENT_UEI)
    public void dataLinkFailed(Event event){
        try{
            updateLinkStatus("dataLinkFailed", event, "B");
        }catch(Throwable t){
            debugf(this, t, "Caught an exception in dataLinkFailed");
        }finally{
            debugf(this, "Bailing out of dataLinkFailed handler");
        }
    }

    private void updateLinkStatus(String method, Event event, String newStatus) {
        LogUtils.infof(this, "%s: received event %s", method, event.getUei());
        String endPoint1 = EventUtils.getParm(event, EventConstants.PARM_ENDPOINT1);
        String endPoint2 = EventUtils.getParm(event, EventConstants.PARM_ENDPOINT2);
        
        Assert.notNull(endPoint1, "Param endPoint1 cannot be null");
        Assert.notNull(endPoint2, "Param endPoint2 cannot be null");
        
        String nodeLabel = max(endPoint1, endPoint2);
        String parentNodeLabel = min(endPoint1, endPoint2);
        Integer nodeId = m_nodeLinkService.getNodeId(nodeLabel);
        Integer parentNodeId = m_nodeLinkService.getNodeId(parentNodeLabel);
        
        if(nodeId != null && parentNodeId != null) {
            LogUtils.infof(this, "%s: updated link nodeLabel: %s, nodeId: %d, parentLabel: %s, parentId: %d ", method, nodeLabel, nodeId, parentNodeLabel, parentNodeId);
            m_nodeLinkService.updateLinkStatus(parentNodeId, nodeId, newStatus);
        }else {
            LogUtils.infof(this, "%s: found no link with parent: %s and node %s", method, parentNodeLabel, nodeLabel);
        }
    }
    
    @EventHandler(uei=EventConstants.DATA_LINK_RESTORED_EVENT_UEI)
    public void dataLinkRestored(Event event){
        try{
            updateLinkStatus("dataLinkRestored", event, "G");
        }catch(Throwable t){
            debugf(this, t, "Caught a throwable in dataLinkRestored");
        }finally{
            debugf(this, "Bailing out of dataLinkRestored handler");
        }
    }
    
    @EventHandler(uei=EventConstants.DATA_LINK_UNMANAGED_EVENT_UEI)
    public void dataLinkUnmanaged(Event e) {
        try{
            updateLinkStatus("dataLinkUnmanaged", e, "U");
        }catch(Throwable t){
            debugf(this, t, "Caught a throwable in dataLinkUnmanaged");
        }finally{
            debugf(this, "Bailing out of dataLinkUnmanaged handler");
        }
    }
    
    
    public static String max(String string1, String string2) {
        if(string1.compareTo(string2) < 0) {
            return string2;
        }else {
            return string1;
        }
    }
    
    public static String min(String string1, String string2) {
        if(string1.compareTo(string2) < 0) {
            return string1;
        }else {
            return string2;
        }
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(LinkProvisioningAdapter.class);
    }

    public void setLinkMatchResolver(LinkMatchResolver linkMatchResolver) {
        m_linkMatchResolver = linkMatchResolver;
    }

    public LinkMatchResolver getLinkMatchResolver() {
        return m_linkMatchResolver;
    }

    public void setNodeLinkService(NodeLinkService nodeLinkService) {
        m_nodeLinkService = nodeLinkService;
    }
    
}
