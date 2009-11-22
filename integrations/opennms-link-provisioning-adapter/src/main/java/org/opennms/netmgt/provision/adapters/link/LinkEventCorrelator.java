/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import static org.opennms.core.utils.LogUtils.debugf;

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
    private EventForwarder m_forwarder;
    private NodeLinkService m_nodeLinkService;
    private EndPointConfigurationDao m_endPointConfigDao;

    public LinkEventCorrelator() {}
    
    public boolean isLinkUp(Event e) {
        return false;
    }
    
    public void logEvent(Event e) {
        debugf(this, "Correlating Event %s/%d/%s/%s", e.getUei(), e.getNodeid(), e.getInterface(), e.getService());
    }
    
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
            debugf(this, t, "Caught a throwable handleNodeDown");
        }finally {
            debugf(this, "Bailing out of handleNodeDown");
        }
    }
    
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
            debugf(this, t, "Caught a throwable in handleNodeUp");
        }finally {
            debugf(this, "Bailing out of handleNodeUp");
        }
    }
    
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
                debugf(this, "Discarding Event %s since ip %s is node the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            debugf(this, t, "Caught a throwable in handleInterfaceDown");
        }finally {
            debugf(this, "Bailing out of handleInterfaceDown");
        }
    }
    
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
                debugf(this, "Discarding Event %s since ip %s is not the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            debugf(this, t, "Caught a throwable in handleInterfaceUp");
        }finally {
            debugf(this, "Bailing out of handleInterfaceUp");
        }
    }
    
    @Transactional
    @EventHandler(uei = EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)
    public void handleServiceUnresponsive(Event e) {
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkDown(nodeId);
            }
            else {
                debugf(this, "Discarding Event %s since ip %s is node the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            debugf(this, t, "Caught a throwable handleServiceUnresponsive");
        }finally{
            debugf(this, "Bailing out of handleServiceUnresponsive");
        }
    }
    
    @Transactional
    @EventHandler(uei = EventConstants.SERVICE_RESPONSIVE_EVENT_UEI)
    public void handleServiceResponsive(Event e) {
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkUp(nodeId);
            }
            else {
                debugf(this, "Discarding Event %s since ip %s is node the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            debugf(this, t, "Caught a throwable in handleServiceResponsive");
        }finally {
            debugf(this, "Bailing out of handleServiceResponsive");
        }
    }
    
    @Transactional
    @EventHandler(uei = EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(Event e) {
       try { 
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                endPointFound(nodeId);
            } else {
                debugf(this, "Discarding Event %s since ip %s is node the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
       }catch(Throwable t) {
           debugf(this, t, "Caught a throwable in handleNodeGained");
       }finally {
           debugf(this, "Bailing out of handleNodeGainedService");
       }
    }
    
    @Transactional
    @EventHandler(uei = EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleNodeLostService(Event e) {
        debugf(this, "A special log msg for %s", e.getUei());
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkDown(nodeId);
            }
            else {
                debugf(this, "Discarding Event %s since ip %s is node the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
        
            
        } 
        catch(Throwable t) {
            debugf(this, t, "Caught a throwable in handleNodeLostService!");
        }
        finally {
            debugf(this, "Bailing out of handleNodeLostService");
        }
    }
    
    @Transactional
    @EventHandler(uei = EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void handleNodeRegainedService(Event e) {
        try {
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkUp(nodeId);
            }
            else {
                debugf(this, "Discarding Event %s since ip %s is node the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
        }catch(Throwable t) {
            debugf(this, t, "Caught a throwable in handleNodeRegainedService!");
        }finally {
            debugf(this, "Bailing out of handleNodeRegainedService");
        }
    }
    
    @Transactional
    @EventHandler(uei = EventConstants.SERVICE_UNMANAGED_EVENT_UEI)
    public void handleServiceUnmanaged(Event e) {
       try { 
            logEvent(e);
            if (e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                linkUp(nodeId); 
            }
            else {
                debugf(this, "Discarding Event %s since ip %s is not the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
       }catch(Throwable t) {
           debugf(this, t, "Caught a throwable in handleServiceUnmanaged!");
       }finally {
           debugf(this, "Bailing out of handleServiceUnmanaged");
       }
    }
    
    @Transactional
    @EventHandler(uei=EventConstants.SERVICE_DELETED_EVENT_UEI)
    public void handleServiceDeleted(Event e){
       try {
            if(e.getService() != null && !e.getService().equals(getEndPointTypeValidator().getServiceName())) {
                debugf(this, "Discarding Event %s since service %s does not match EndPoint service %s", e.getUei(), e.getService(), getEndPointTypeValidator().getServiceName());
                return;
            }
            
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if(isSnmpPrimary(nodeId, e.getInterface())){
                endPointDeleted(nodeId);
            }
            else {
                debugf(this, "Discarding Event %s since ip %s is not the primary interface of node %d", e.getUei(), e.getInterface(), e.getNodeid());
            }
       }catch(Throwable t) {
           debugf(this, t, "Caught a throwable in handleServiceDeleted");
       }finally {
           debugf(this, "Bailing out of handleServiceDeleted");
       }
        
    }

    private void linkDown(int nodeId) {
        debugf(this, "Processing a down for links with endpoint on node %d", nodeId);
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
            
            if (dli.getNodeId() == nodeId) {
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
            
            if (dli.getNodeId() == nodeId) {
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
            
            if (dli.getNodeId() == nodeId) {
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
            
            if (dli.getNodeId() == nodeId) {
                linkState = linkState.nodeEndPointFound(transition);
            } else {
                linkState = linkState.parentNodeEndPointFound(transition);
            }
            
            linkStateObj.setLinkState(linkState);
            m_nodeLinkService.saveLinkState(linkStateObj);
        }
    }
    
    public boolean isSnmpPrimary(int nodeId, String ipAddr) {
        String primaryAddress = m_nodeLinkService.getPrimaryAddress(nodeId);
        if(primaryAddress != null) {
            return primaryAddress.equals(ipAddr);
        }
        return false;
    }

    public boolean nodeHasEndPointService(int nodeId) {
        return m_nodeLinkService.nodeHasEndPointService(nodeId);
    }

    public void updateLinkStatus(Event e) {
        throw new UnsupportedOperationException("boo!");
    }

    public void setEventForwarder(EventForwarder forwarder) {
        m_forwarder = forwarder;
    }

    public void setNodeLinkService(NodeLinkService nodeLinkService) {
        m_nodeLinkService = nodeLinkService;
    }

    public void setEndPointConfigDao(EndPointConfigurationDao endPointConfigDao) {
        m_endPointConfigDao = endPointConfigDao;
    }

    private EndPointTypeValidator getEndPointTypeValidator() {
        return m_endPointConfigDao.getValidator();
    }
}