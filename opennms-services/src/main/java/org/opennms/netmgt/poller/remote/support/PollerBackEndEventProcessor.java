package org.opennms.netmgt.poller.remote.support;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

@EventListener(name="pollerBackEnd")
public class PollerBackEndEventProcessor {

    private PollerBackEnd m_pollerBackEnd;
    
    public void setPollerBackEnd(PollerBackEnd pollerBackEnd) {
        m_pollerBackEnd = pollerBackEnd;
    }

    @EventHandler(uei=EventConstants.SNMPPOLLERCONFIG_CHANGED_EVENT_UEI)
    public void handleSnmpPollerConfigChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleDaemonConfigChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAdded(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)
    public void handleNodeGainedInterface(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.NODE_CONFIG_CHANGE_UEI)
    public void handleNodeConfigChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.NODE_INFO_CHANGED_EVENT_UEI)
    public void handleNodeInfoChanged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.SERVICE_DELETED_EVENT_UEI)
    public void handleServiceDeleted(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.SERVICE_UNMANAGED_EVENT_UEI)
    public void handleServiceUnmanaged(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void handleInterfaceDeleted(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeleted(final Event event) {
        m_pollerBackEnd.configurationUpdated();
    }

}
