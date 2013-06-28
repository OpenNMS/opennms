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

/**
 * <p>LinkEventCorrelator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkStateTransition;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.annotation.Transactional;

@EventListener(name="LinkEventCorrelator")
public class LinkEventCorrelator {
    private static final Logger LOG = LoggerFactory.getLogger(LinkEventCorrelator.class);
    private EventForwarder m_forwarder;
    private NodeLinkService m_nodeLinkService;
    private EndPointConfigurationDao m_endPointConfigDao;

    /**
     * <p>Constructor for LinkEventCorrelator.</p>
     */
    public LinkEventCorrelator() {}
    
    /**
     * <p>isLinkUp</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean isLinkUp(Event e) {
        return false;
    }
    
    /**
     * <p>logEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void logEvent(Event e) {
        LOG.debug("Correlating Event {}/{}/{}/{}", e.getUei(), e.getNodeid(), e.getInterface(), e.getService());
    }
    
    /**
     * <p>handleNodeDown</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.NODE_DOWN_EVENT_UEI)
    public void handleNodeDown(Event e) {
        try {
            logEvent(e);
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(nodeHasEndPointService(nodeId)) {
                linkDown(nodeId);
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable handleNodeDown", t);
        }finally {
            LOG.debug("Bailing out of handleNodeDown");
        }
    }
    
    /**
     * <p>handleNodeUp</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.NODE_UP_EVENT_UEI)
    public void handleNodeUp(Event e) {
        try {
            logEvent(e);
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(nodeHasEndPointService(nodeId)) {
                linkUp(nodeId);
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable in handleNodeUp", t);
        }finally {
            LOG.debug("Bailing out of handleNodeUp");
        }
    }
    
    /**
     * <p>handleInterfaceDown</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.INTERFACE_DOWN_EVENT_UEI)
    public void handleInterfaceDown(Event e) {
        try {
            logEvent(e);
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(nodeHasEndPointService(nodeId)){
                linkDown(nodeId); 
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is node the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable in handleInterfaceDown", t);
        }finally {
            LOG.debug("Bailing out of handleInterfaceDown");
        }
    }
    
    /**
     * <p>handleInterfaceUp</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.INTERFACE_UP_EVENT_UEI)
    public void handleInterfaceUp(Event e) {
        try {
            logEvent(e);
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(nodeHasEndPointService(nodeId)){
                linkUp(nodeId); 
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is not the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable in handleInterfaceUp", t);
        }finally {
            LOG.debug("Bailing out of handleInterfaceUp");
        }
    }
    
    /**
     * <p>handleServiceUnresponsive</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)
    public void handleServiceUnresponsive(Event e) {
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkDown(nodeId);
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is node the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable handleServiceUnresponsive", t);
        }finally{
            LOG.debug("Bailing out of handleServiceUnresponsive");
        }
    }
    
    /**
     * <p>handleServiceResponsive</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.SERVICE_RESPONSIVE_EVENT_UEI)
    public void handleServiceResponsive(Event e) {
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkUp(nodeId);
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is node the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable in handleServiceResponsive", t);
        }finally {
            LOG.debug("Bailing out of handleServiceResponsive");
        }
    }
    
    /**
     * <p>handleNodeGainedService</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(Event e) {
       try { 
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                endPointFound(nodeId);
            } else {
                LOG.debug("Discarding Event {} since ip {} is node the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
       }catch(Throwable t) {
           LOG.debug("Caught a throwable in handleNodeGained", t);
       }finally {
           LOG.debug("Bailing out of handleNodeGainedService");
       }
    }
    
    /**
     * <p>handleNodeLostService</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleNodeLostService(Event e) {
        LOG.debug("A special log msg for {}", e.getUei());
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkDown(nodeId);
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is node the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
        
            
        } 
        catch(Throwable t) {
            LOG.debug("Caught a throwable in handleNodeLostService!", t);
        }
        finally {
            LOG.debug("Bailing out of handleNodeLostService");
        }
    }
    
    /**
     * <p>handleNodeRegainedService</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void handleNodeRegainedService(Event e) {
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkUp(nodeId);
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is node the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            LOG.debug("Caught a throwable in handleNodeRegainedService!", t);
        }finally {
            LOG.debug("Bailing out of handleNodeRegainedService");
        }
    }
    
    /**
     * <p>handleServiceUnmanaged</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei = EventConstants.SERVICE_UNMANAGED_EVENT_UEI)
    public void handleServiceUnmanaged(Event e) {
       try { 
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkUp(nodeId); 
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is not the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
       }catch(Throwable t) {
           LOG.debug("Caught a throwable in handleServiceUnmanaged!", t);
       }finally {
           LOG.debug("Bailing out of handleServiceUnmanaged");
       }
    }
    
    /**
     * <p>handleServiceDeleted</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Transactional
    @EventHandler(uei=EventConstants.SERVICE_DELETED_EVENT_UEI)
    public void handleServiceDeleted(Event e){
       try {
            if(e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                LOG.debug("Discarding Event {} since service {} does not match EndPoint service {}", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                endPointDeleted(nodeId);
            }
            else {
                LOG.debug("Discarding Event {} since ip {} is not the primary interface of node {}", e.getUei(), e.getInterface(), e.getNodeid());
            }
       }catch(Throwable t) {
           LOG.debug("Caught a throwable in handleServiceDeleted", t);
       }finally {
           LOG.debug("Bailing out of handleServiceDeleted");
       }
        
    }

    private void linkDown(int nodeId) {
        LOG.debug("Processing a down for links with endpoint on node {}", nodeId);
        for (DataLinkInterface dli : m_nodeLinkService.getLinkContainingNodeId(nodeId)) {
            boolean isParent = false;

            LinkStateTransition transition = new LinkEventSendingStateTransition(dli, m_forwarder, m_nodeLinkService);
            OnmsLinkState linkStateObj = m_nodeLinkService.getLinkStateForInterface(dli);
            if (linkStateObj == null) {
                linkStateObj = new OnmsLinkState();
                linkStateObj.setDataLinkInterface(dli);
                linkStateObj.setLinkState(LinkState.LINK_UP);
            }
            LinkState linkState = linkStateObj.getLinkState();
            
            if (dli.getNode().getId() == nodeId) {
                isParent = false;
            } else {
                isParent = true;
            }
            linkState = linkState.down(isParent, transition);
            linkStateObj.setLinkState(linkState);
            m_nodeLinkService.saveLinkState(linkStateObj);
        }
    }
    
    private void endPointDeleted(int nodeId){
        for (DataLinkInterface dli : m_nodeLinkService.getLinkContainingNodeId(nodeId)) {

            LinkStateTransition transition = new LinkEventSendingStateTransition(dli, m_forwarder, m_nodeLinkService);
            OnmsLinkState linkStateObj = m_nodeLinkService.getLinkStateForInterface(dli);
            if (linkStateObj == null) {
                linkStateObj = new OnmsLinkState();
                linkStateObj.setDataLinkInterface(dli);
                linkStateObj.setLinkState(LinkState.LINK_UP);
            }
            LinkState linkState = linkStateObj.getLinkState();
            
            if (dli.getNode().getId() == nodeId) {
                linkState = linkState.nodeEndPointDeleted(transition);
            } else {
                linkState = linkState.parentNodeEndPointDeleted(transition);
            }
            
            
            linkStateObj.setLinkState(linkState);
            m_nodeLinkService.saveLinkState(linkStateObj);
        }
    }
    
    private void linkUp(int nodeId) {
        for (DataLinkInterface dli : m_nodeLinkService.getLinkContainingNodeId(nodeId)) {
            boolean isParent = false;

            LinkStateTransition transition = new LinkEventSendingStateTransition(dli, m_forwarder, m_nodeLinkService);
            OnmsLinkState linkStateObj = m_nodeLinkService.getLinkStateForInterface(dli);
            if (linkStateObj == null) {
                linkStateObj = new OnmsLinkState();
                linkStateObj.setDataLinkInterface(dli);
                linkStateObj.setLinkState(LinkState.LINK_UP);
            }
            LinkState linkState = linkStateObj.getLinkState();
            
            if (dli.getNode().getId() == nodeId) {
                isParent = false;
            } else {
                isParent = true;
            }
            linkState = linkState.up(isParent, transition);
            linkStateObj.setLinkState(linkState);
            m_nodeLinkService.saveLinkState(linkStateObj);
        }
    }
    
    private void endPointFound(int nodeId){
        for (DataLinkInterface dli : m_nodeLinkService.getLinkContainingNodeId(nodeId)) {

            LinkStateTransition transition = new LinkEventSendingStateTransition(dli, m_forwarder, m_nodeLinkService);
            OnmsLinkState linkStateObj = m_nodeLinkService.getLinkStateForInterface(dli);
            if (linkStateObj == null) {
                linkStateObj = new OnmsLinkState();
                linkStateObj.setDataLinkInterface(dli);
                linkStateObj.setLinkState(LinkState.LINK_BOTH_UNMANAGED);
            }
            LinkState linkState = linkStateObj.getLinkState();
            
            if (dli.getNode().getId() == nodeId) {
                linkState = linkState.nodeEndPointFound(transition);
            } else {
                linkState = linkState.parentNodeEndPointFound(transition);
            }
            
            linkStateObj.setLinkState(linkState);
            m_nodeLinkService.saveLinkState(linkStateObj);
        }
    }
    
    /**
     * <p>isSnmpPrimary</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isSnmpPrimary(int nodeId, String ipAddr) {
        String primaryAddress = m_nodeLinkService.getPrimaryAddress(nodeId);
        if(primaryAddress != null) {
            return primaryAddress.equals(ipAddr);
        }
        return false;
    }

    /**
     * <p>nodeHasEndPointService</p>
     *
     * @param nodeId a int.
     * @return a boolean.
     */
    public boolean nodeHasEndPointService(int nodeId) {
        return m_nodeLinkService.nodeHasEndPointService(nodeId);
    }

    /**
     * <p>updateLinkStatus</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void updateLinkStatus(Event e) {
        throw new UnsupportedOperationException("boo!");
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param forwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder forwarder) {
        m_forwarder = forwarder;
    }

    /**
     * <p>setNodeLinkService</p>
     *
     * @param nodeLinkService a {@link org.opennms.netmgt.provision.adapters.link.NodeLinkService} object.
     */
    public void setNodeLinkService(NodeLinkService nodeLinkService) {
        m_nodeLinkService = nodeLinkService;
    }

    /**
     * <p>setEndPointConfigDao</p>
     *
     * @param endPointConfigDao a {@link org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao} object.
     */
    public void setEndPointConfigDao(EndPointConfigurationDao endPointConfigDao) {
        m_endPointConfigDao = endPointConfigDao;
    }

    private EndPointTypeValidator getEndPointTypeValidator() {
        return m_endPointConfigDao.getValidator();
    }
}
