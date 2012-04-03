package org.opennms.netmgt.ncs.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.rest.NCSRestService.ComponentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.style.ToStringCreator;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class NCSComponentServiceImpl implements NCSComponentService {
	@Autowired
	NCSComponentDao m_componentDao;

	@Autowired
	AlarmDao m_alarmDao;
	
	@Autowired
	EventDao m_eventDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

	EventIpcManager m_eventIpcManager;

	public EventIpcManager getEventIpcManager() {
		return m_eventIpcManager;
	}

	public void setEventIpcManager(final EventIpcManager manager) {
		m_eventIpcManager = manager;
	}

	@Override
	@Transactional
	public NCSComponent getComponent(final String type, final String foreignSource, final String foreignId) {
		return getComponent(new ComponentIdentifier(type, foreignSource, foreignId));
	}

	@Override
	@Transactional
	public ComponentList findComponentsWithAttribute(final String attrKey, final String attrValue) {
		return new ComponentList(m_componentDao.findComponentsWithAttribute(attrKey, attrValue));
	}

	@Override
	@Transactional
	public NCSComponent addOrUpdateComponents(final NCSComponent component, final boolean deleteOrphans) {
		return addOrUpdateComponents(getIdentifier(component), component, deleteOrphans);
	}

	@Override
	@Transactional
	public NCSComponent addSubcomponent(final String type, final String foreignSource, final String foreignId, final NCSComponent subComponent, final boolean deleteOrphans) {
		final ComponentIdentifier id = new ComponentIdentifier(type, foreignSource, foreignId);

		LogUtils.debugf(this, "addSubcomponent: %s - %s (deleteOrphans=%s)", id, subComponent, Boolean.toString(deleteOrphans));

		final NCSComponent component = getComponent(id);

		if (component == null) {
			throw new ObjectRetrievalFailureException(NCSComponent.class, "Unable to locate component with type=" + type + ", foreignSource=" + foreignSource + ", foreignId=" + foreignId);
		}
		LogUtils.debugf(this, "found component: %s", component);

		final NCSComponent updatedSubComponent = addOrUpdateComponents(getIdentifier(subComponent), subComponent, deleteOrphans);
		
		LogUtils.debugf(this, "adding subcomponent: %s", updatedSubComponent);
		component.addSubcomponent(updatedSubComponent);
		
		LogUtils.debugf(this, "saving");
		m_componentDao.update(component);

		try {
			sendComponentUpdated(component);
		} catch (final EventProxyException e) {
			throw new RecoverableDataAccessException("update completed, but unable to send componentUpdated event", e);
		}

		return getComponent(id);
	}

	@Override
	@Transactional
	public void deleteComponent(final String type, final String foreignSource, final String foreignId, final boolean deleteOrphans) {
		final ComponentIdentifier id = new ComponentIdentifier(type, foreignSource, foreignId);
		deleteComponent(id, deleteOrphans);
	}







	private Set<ComponentIdentifier> getIdentifiers(final Collection<NCSComponent> components) {
		final Set<ComponentIdentifier> identifiers = new HashSet<ComponentIdentifier>();
		for (final NCSComponent component : components) {
			identifiers.add(getIdentifier(component));
		}
		return identifiers;
	}
	
	private ComponentIdentifier getIdentifier(final NCSComponent component) {
		return new ComponentIdentifier(component.getType(), component.getForeignSource(), component.getForeignId());
	}

	private NCSComponent getComponent(final ComponentIdentifier id) {
		return m_componentDao.findByTypeAndForeignIdentity(id.getType(), id.getForeignSource(), id.getForeignId());
	}

	private NCSComponent addOrUpdateComponents(final ComponentIdentifier id, final NCSComponent component, final boolean deleteOrphans) {
		LogUtils.debugf(this, "addOrUpdateComponents: %s (deletOrphans=%s)", id, deleteOrphans);

		final Set<NCSComponent> subcomponents = new LinkedHashSet<NCSComponent>();
		
		for (final NCSComponent subcomponent : component.getSubcomponents()) {
			final NCSComponent updatedComponent = addOrUpdateComponents(getIdentifier(subcomponent), subcomponent, deleteOrphans);
			subcomponents.add(updatedComponent);
		}

		final NCSComponent existing = new UpsertTemplate<NCSComponent, NCSComponentDao>(m_transactionManager, m_componentDao) {
			@Override
			protected NCSComponent query() {
				return getComponent(id);
			}

			@Override
			protected NCSComponent doInsert() {
				component.setSubcomponents(subcomponents);
				m_componentDao.save(component);
				try {
					sendComponentAdded(component);
				} catch (final EventProxyException e) {
					throw new RecoverableDataAccessException("update completed, but unable to send componentAdded event", e);
				}
				return component;
			}
			
			@Override
			protected NCSComponent doUpdate(final NCSComponent dbObj) {
				if (deleteOrphans) deleteOrphanedComponents(getIdentifiers(dbObj.getSubcomponents()), getIdentifiers(subcomponents));

				dbObj.setName(component.getName());
				dbObj.setVersion(component.getVersion());
				dbObj.setDependenciesRequired(component.getDependenciesRequired());
				dbObj.setNodeIdentification(component.getNodeIdentification());
				dbObj.setUpEventUei(component.getUpEventUei());
				dbObj.setDownEventUei(component.getDownEventUei());
				dbObj.setAttributes(component.getAttributes());
				dbObj.setSubcomponents(subcomponents);
				m_componentDao.update(dbObj);
				try {
					sendComponentUpdated(dbObj);
				} catch (final EventProxyException e) {
					throw new RecoverableDataAccessException("update completed, but unable to send componentUpdated event", e);
				}
				return dbObj;
			}

		}.execute();

		return existing;
	}

	private void deleteComponent(final ComponentIdentifier id, final boolean deleteOrphans) {
		final NCSComponent component = getComponent(id);

        if (component == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if (deleteOrphans) {
        	for (final ComponentIdentifier subId : getIdentifiers(component.getSubcomponents())) {
	        	deleteComponent(subId, deleteOrphans);
	        }
        }

        for(final NCSComponent parent : m_componentDao.findComponentsThatDependOn(component)) {
            parent.getSubcomponents().remove(component);
            m_componentDao.update(parent);
        }

        m_componentDao.delete(component);

        deleteEvents(id.getForeignSource(), id.getForeignId());
        deleteAlarms(id.getForeignSource(), id.getForeignId());
        
        try {
			sendComponentDeleted(component);
		} catch (final EventProxyException e) {
			throw new RecoverableDataAccessException("update completed, but unable to send componentDeleted event", e);
		}
	}

	private void deleteOrphanedComponents(final Set<ComponentIdentifier> oldComponents, final Set<ComponentIdentifier> newComponents) {
		LogUtils.debugf(this, "deleteOrphanedComponents: old = %s, new = %s", oldComponents, newComponents);

		for (final ComponentIdentifier id : oldComponents) {
			if (!newComponents.contains(id)) {
				LogUtils.debugf(this, "deleteOrphanedComponents: deleting %s", id);
				deleteComponent(id, true);
			}
		}
	}

	private void deleteAlarms(final String foreignSource, final String foreignId) {
		final OnmsCriteria alarmCriteria = new OnmsCriteria(OnmsAlarm.class)
        .add(Restrictions.like("eventParms", "%componentForeignSource=" + foreignSource +"%"))
        .add(Restrictions.like("eventParms", "%componentForeignId=" + foreignId +"%"));

        for(final OnmsAlarm alarm : m_alarmDao.findMatching(alarmCriteria)) {
            m_alarmDao.delete(alarm);
        }
	}

	private void deleteEvents(final String foreignSource, final String foreignId) {
		final OnmsCriteria eventCriteria = new OnmsCriteria(OnmsEvent.class)
        .add(Restrictions.like("eventParms", "%componentForeignSource=" + foreignSource +"%"))
        .add(Restrictions.like("eventParms", "%componentForeignId=" + foreignId +"%"));

        for(final OnmsEvent event : m_eventDao.findMatching(eventCriteria)) {
            m_eventDao.delete(event);
        }
	}

	private void sendComponentAdded(final NCSComponent component) throws EventProxyException {
		final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_ADDED_UEI, "NCSComponentService");
		builder.addParam("componentType", component.getType());
		builder.addParam("componentName", component.getName());
		builder.addParam("componentForeignSource", component.getForeignSource());
		builder.addParam("componentForeignId", component.getForeignId());
		m_eventIpcManager.send(builder.getEvent());
	}
	
	private void sendComponentDeleted(final NCSComponent component) throws EventProxyException {
		final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_DELETED_UEI, "NCSComponentService");
		builder.addParam("componentType", component.getType());
		builder.addParam("componentName", component.getName());
		builder.addParam("componentForeignSource", component.getForeignSource());
		builder.addParam("componentForeignId", component.getForeignId());
		m_eventIpcManager.send(builder.getEvent());
	}

	private void sendComponentUpdated(final NCSComponent component) throws EventProxyException {
		final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_UPDATED_UEI, "NCSComponentService");
		builder.addParam("componentType", component.getType());
		builder.addParam("componentName", component.getName());
		builder.addParam("componentForeignSource", component.getForeignSource());
		builder.addParam("componentForeignId", component.getForeignId());
		m_eventIpcManager.send(builder.getEvent());
	}
	
	public static final class ComponentIdentifier {
		private final String m_type;
		private final String m_foreignSource;
		private final String m_foreignId;

		public ComponentIdentifier(final String type, final String foreignSource, final String foreignId) {
			m_type          = type;
			m_foreignSource = foreignSource;
			m_foreignId     = foreignId;
		}

		public String getType()          { return m_type; }
		public String getForeignSource() { return m_foreignSource; }
		public String getForeignId()     { return m_foreignId; }

		@Override
		public String toString() {
			return new ToStringCreator(this)
				.append("type", m_type)
				.append("foreignSource", m_foreignSource)
				.append("foreignId", m_foreignId)
				.toString();
		}
	}
}
