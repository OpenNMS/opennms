
/**
 * <p>LinkEventSendingStateTransition class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState.LinkStateTransition;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
public class LinkEventSendingStateTransition implements LinkStateTransition {

    private DataLinkInterface m_dataLinkInterface;
    private EventForwarder m_eventForwarder;
    private NodeLinkService m_nodeLinkService;

    /**
     * <p>Constructor for LinkEventSendingStateTransition.</p>
     *
     * @param dataLinkInterface a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param nodeLinkService a {@link org.opennms.netmgt.provision.adapters.link.NodeLinkService} object.
     */
    public LinkEventSendingStateTransition(DataLinkInterface dataLinkInterface, EventForwarder eventForwarder, NodeLinkService nodeLinkService) {
        m_dataLinkInterface = dataLinkInterface;
        m_eventForwarder = eventForwarder;
        m_nodeLinkService = nodeLinkService;
    }
    
    /**
     * <p>onLinkDown</p>
     */
    public void onLinkDown() {
        sendDataLinkEvent(EventConstants.DATA_LINK_FAILED_EVENT_UEI);
    }

    private void sendDataLinkEvent(String uei) {
        String endPoint1 = m_nodeLinkService.getNodeLabel(m_dataLinkInterface.getNodeId());
        String endPoint2 = m_nodeLinkService.getNodeLabel(m_dataLinkInterface.getNodeParentId());
        
        
        Event e = new EventBuilder(uei, "EventCorrelator")
            .addParam(EventConstants.PARM_ENDPOINT1, LinkProvisioningAdapter.min(endPoint1, endPoint2))
            .addParam(EventConstants.PARM_ENDPOINT2, LinkProvisioningAdapter.max(endPoint1, endPoint2))
            .getEvent();
        m_eventForwarder.sendNow(e);
    }

    /**
     * <p>onLinkUp</p>
     */
    public void onLinkUp() {
        sendDataLinkEvent(EventConstants.DATA_LINK_RESTORED_EVENT_UEI);
    }

    /**
     * <p>onLinkUnknown</p>
     */
    public void onLinkUnknown() {
        sendDataLinkEvent(EventConstants.DATA_LINK_UNMANAGED_EVENT_UEI);
    }
    
    
}
