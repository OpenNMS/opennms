package org.opennms.netmgt.ncs.persistence;

import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;

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

	public void sendAll(final EventForwarder forwarder) {
		for (final ComponentIdentifier id : m_deleted) {
			final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_DELETED_UEI, "NCSComponentService");
			builder.addParam("componentType", id.getType());
			builder.addParam("componentName", id.getName());
			builder.addParam("componentForeignSource", id.getForeignSource());
			builder.addParam("componentForeignId", id.getForeignId());
			forwarder.sendNow(builder.getEvent());
		}
		for (final ComponentIdentifier id : m_added) {
			final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_ADDED_UEI, "NCSComponentService");
			builder.addParam("componentType", id.getType());
			builder.addParam("componentName", id.getName());
			builder.addParam("componentForeignSource", id.getForeignSource());
			builder.addParam("componentForeignId", id.getForeignId());
			forwarder.sendNow(builder.getEvent());
		}
		for (final ComponentIdentifier id : m_updated) {
			final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_UPDATED_UEI, "NCSComponentService");
			builder.addParam("componentType", id.getType());
			builder.addParam("componentName", id.getName());
			builder.addParam("componentForeignSource", id.getForeignSource());
			builder.addParam("componentForeignId", id.getForeignId());
			forwarder.sendNow(builder.getEvent());
		}
	}
}