package org.opennms.netmgt.ncs.persistence;

import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

public final class ComponentEventQueue {
	private final Set<ComponentIdentifier> m_added   = new LinkedHashSet<ComponentIdentifier>();
	private final Set<ComponentIdentifier> m_deleted = new LinkedHashSet<ComponentIdentifier>();
	private final Set<ComponentIdentifier> m_updated = new LinkedHashSet<ComponentIdentifier>();

	public void componentAdded(final ComponentIdentifier identifier) {
		m_added.add(identifier);
		m_deleted.remove(identifier);
		m_updated.remove(identifier);
	}

	public void componentDeleted(final ComponentIdentifier identifier) {
		m_added.remove(identifier);
		m_deleted.add(identifier);
		m_updated.remove(identifier);
	}

	public void componentUpdated(final ComponentIdentifier identifier) {
		m_added.remove(identifier);
		m_deleted.remove(identifier);
		m_updated.add(identifier);
	}

	public void sendAll(final EventProxy eventProxy) throws EventProxyException {
		for (final ComponentIdentifier id : m_deleted) {
			final String uei = EventConstants.COMPONENT_DELETED_UEI;
			eventProxy.send(getEvent(uei, id));
		}
		for (final ComponentIdentifier id : m_added) {
			final String uei = EventConstants.COMPONENT_ADDED_UEI;
			eventProxy.send(getEvent(uei, id));
		}
		for (final ComponentIdentifier id : m_updated) {
			final String uei = EventConstants.COMPONENT_UPDATED_UEI;
			eventProxy.send(getEvent(uei, id));
		}
	}

	private Event getEvent(final String uei, final ComponentIdentifier id) {
		final EventBuilder builder = new EventBuilder(uei, "NCSComponentService");
		builder.addParam("componentId",            id.getId());
		builder.addParam("componentType",          id.getType());
		builder.addParam("componentName",          id.getName());
		builder.addParam("componentForeignSource", id.getForeignSource());
		builder.addParam("componentForeignId",     id.getForeignId());
		builder.addParam("dependencyRequirements", id.getDependencyRequirements().toString());
		final Event event = builder.getEvent();
		return event;
	}
}