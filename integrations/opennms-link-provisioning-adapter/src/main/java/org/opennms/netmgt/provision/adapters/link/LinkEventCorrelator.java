/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkStateTransition;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.xml.event.Event;

public class LinkEventCorrelator {
    private EventForwarder m_forwarder;
    private NodeLinkService m_nodeLinkService;
    private EndPointTypeValidator m_endPointTypeValidator;

    public LinkEventCorrelator() {
    }
    
    public boolean isLinkUp(Event e) {
        return false;
    }

    @EventHandler(uei = EventConstants.NODE_DOWN_EVENT_UEI)
    public void handleNodeDown(Event e) {
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkDown(nodeId); 
    }

    @EventHandler(uei = EventConstants.NODE_UP_EVENT_UEI)
    public void handleNodeUp(Event e) {
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkUp(nodeId); 
    }

    @EventHandler(uei = EventConstants.INTERFACE_DOWN_EVENT_UEI)
    public void handleInterfaceDown(Event e) {
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkDown(nodeId); 
    }

    @EventHandler(uei = EventConstants.INTERFACE_UP_EVENT_UEI)
    public void handleInterfaceUp(Event e) {
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkUp(nodeId); 
    }

    @EventHandler(uei = EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)
    public void handleServiceUnresponsive(Event e) {
        if (e.getService() != null && !e.getService().equals(m_endPointTypeValidator.getServiceName())) {
            return;
        }
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkDown(nodeId); 
    }

    @EventHandler(uei = EventConstants.SERVICE_RESPONSIVE_EVENT_UEI)
    public void handleServiceResponsive(Event e) {
        if (e.getService() != null && !e.getService().equals(m_endPointTypeValidator.getServiceName())) {
            return;
        }
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkUp(nodeId); 
    }

    @EventHandler(uei = EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(Event e) {
        if (e.getService() != null && !e.getService().equals(m_endPointTypeValidator.getServiceName())) {
            return;
        }
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkUp(nodeId); 
    }

    @EventHandler(uei = EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleNodeLostService(Event e) {
        if (e.getService() != null && !e.getService().equals(m_endPointTypeValidator.getServiceName())) {
            return;
        }
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkDown(nodeId); 
    }

    @EventHandler(uei = EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void handleNodeRegainedService(Event e) {
        if (e.getService() != null && !e.getService().equals(m_endPointTypeValidator.getServiceName())) {
            return;
        }
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkUp(nodeId); 
    }

    @EventHandler(uei = EventConstants.SERVICE_UNMANAGED_EVENT_UEI)
    public void handleServiceUnmanaged(Event e) {
        if (e.getService() != null && !e.getService().equals(m_endPointTypeValidator.getServiceName())) {
            return;
        }
        int nodeId = Long.valueOf(e.getNodeid()).intValue();
        linkUp(nodeId); 
    }

    private void linkDown(int nodeId) {
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

    public void updateLinkStatus(Event e) {
        throw new UnsupportedOperationException("boo!");
    }

    public void setEventForwarder(EventForwarder forwarder) {
        m_forwarder = forwarder;
    }

    public void setNodeLinkService(NodeLinkService nodeLinkService) {
        m_nodeLinkService = nodeLinkService;
    }

    public void setEndPointTypeValidator(EndPointTypeValidator endPointTypeValidator) {
        m_endPointTypeValidator = endPointTypeValidator;
    }
}