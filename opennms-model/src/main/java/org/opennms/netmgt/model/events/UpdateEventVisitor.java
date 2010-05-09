package org.opennms.netmgt.model.events;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.xml.event.Event;

public class UpdateEventVisitor extends AbstractEntityVisitor {
    
    private static final String m_eventSource = "Provisiond";
    private EventForwarder m_eventForwarder;

    public UpdateEventVisitor(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }
    
    @Override
    public void visitNode(OnmsNode node) {
        LogUtils.infof(this, "Sending nodeAdded Event for %s\n", node);
        m_eventForwarder.sendNow(createNodeUpdatedEvent(node));
    }

    @Override
    public void visitIpInterface(OnmsIpInterface iface) {
        //TODO decide what to do here and when to do it
    }

    @Override
    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        //TODO decide what to do here and when to do it
    }
    
    @Override
    public void visitSnmpInterface(org.opennms.netmgt.model.OnmsEntity snmpIface) {
        //TODO decide what to do here and when to do it
    }

    private Event createNodeUpdatedEvent(OnmsNode node) {
        return EventUtils.createNodeUpdatedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabelSource());
    }

    @SuppressWarnings("unused")
    private Event createIpInterfaceUpdatedEvent(OnmsIpInterface iface) {
        return null;
    }
    
    @SuppressWarnings("unused")
    private Event createSnmpInterfaceUpdatedEvent(OnmsSnmpInterface iface) {
        return null;
    }

    @SuppressWarnings("unused")
    private Event createMonitoredServiceUpdatedEvent(OnmsMonitoredService monSvc) {
        return null;
    }

}
