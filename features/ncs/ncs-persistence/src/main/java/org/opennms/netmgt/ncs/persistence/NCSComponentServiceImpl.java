package org.opennms.netmgt.ncs.persistence;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.rest.NCSRestService.ComponentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

public class NCSComponentServiceImpl implements NCSComponentService {
	@Autowired
	NCSComponentDao m_componentDao;

	@Autowired
	AlarmDao m_alarmDao;
	
	@Autowired
	EventDao m_eventDao;
	
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
		return m_componentDao.findByTypeAndForeignIdentity(type, foreignSource, foreignId);
	}

	@Override
	@Transactional
	public ComponentList findComponentsWithAttribute(final String attrKey, final String attrValue) {
		return new ComponentList(m_componentDao.findComponentsWithAttribute(attrKey, attrValue));
	}

	@Override
	@Transactional
	public NCSComponent addOrUpdateComponents(final NCSComponent component) {
		LogUtils.debugf(this, "addOrUpdateComponents: %s", component);

		final Set<NCSComponent> subcomponents = new LinkedHashSet<NCSComponent>();

		for (final NCSComponent subcomponent : component.getSubcomponents()) {
			subcomponents.add(addOrUpdateComponents(subcomponent));
		}

		final NCSComponent existing = getComponent(component.getType(), component.getForeignSource(), component.getForeignId());

		if (existing == null) {
			component.setSubcomponents(subcomponents);
			m_componentDao.save(component);
			try {
				sendComponentAdded(component);
			} catch (final EventProxyException e) {
				throw new RecoverableDataAccessException("update completed, but unable to send componentAdded event", e);
			}
			return component;
		} else {
			existing.setName(component.getName());
			existing.setVersion(component.getVersion());
			existing.setDependenciesRequired(component.getDependenciesRequired());
			existing.setNodeIdentification(component.getNodeIdentification());
			existing.setUpEventUei(component.getUpEventUei());
			existing.setDownEventUei(component.getDownEventUei());
			existing.setAttributes(component.getAttributes());
			existing.setSubcomponents(subcomponents);
			m_componentDao.update(existing);
			try {
				sendComponentUpdated(existing);
			} catch (final EventProxyException e) {
				throw new RecoverableDataAccessException("update completed, but unable to send componentUpdated event", e);
			}
			return existing;
		}
	}

	@Override
	@Transactional
	public NCSComponent addSubcomponent(final String type, final String foreignSource, final String foreignId, final NCSComponent subComponent) {
		LogUtils.debugf(this, "addSubcomponent: %s/%s/%s - %s", type, foreignSource, foreignId, subComponent);

		final NCSComponent component = getComponent(type, foreignSource, foreignId);
		if (component == null) {
			throw new ObjectRetrievalFailureException(NCSComponent.class, "Unable to locate component with type=" + type + ", foreignSource=" + foreignSource + ", foreignId=" + foreignId);
		}
		LogUtils.debugf(this, "found component: %s", component);

		final NCSComponent updatedComponent = addOrUpdateComponents(subComponent);
		
		LogUtils.debugf(this, "adding subcomponent: %s", updatedComponent);
		component.addSubcomponent(updatedComponent);
		
		LogUtils.debugf(this, "saving");
		m_componentDao.update(component);
		m_componentDao.flush();

		try {
			sendComponentUpdated(component);
		} catch (final EventProxyException e) {
			throw new RecoverableDataAccessException("update completed, but unable to send componentUpdated event", e);
		}

		return getComponent(type, foreignSource, foreignId);
	}

	@Override
	@Transactional
	public void deleteComponent(final String type, final String foreignSource, final String foreignId) {
		final NCSComponent component = getComponent(type, foreignSource, foreignId);
    	
        if (component == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        for(final NCSComponent parent : m_componentDao.findComponentsThatDependOn(component))
        {
            parent.getSubcomponents().remove(component);
        }

        m_componentDao.delete(component);

        final OnmsCriteria eventCriteria = new OnmsCriteria(OnmsEvent.class)
        .add(Restrictions.like("eventParms", "%componentForeignSource=" + foreignSource +"%"))
        .add(Restrictions.like("eventParms", "%componentForeignId=" + foreignId +"%"));

        for(final OnmsEvent event : m_eventDao.findMatching(eventCriteria)) {
            m_eventDao.delete(event);
        }

        m_eventDao.flush();

        final OnmsCriteria alarmCriteria = new OnmsCriteria(OnmsAlarm.class)
        .add(Restrictions.like("eventParms", "%componentForeignSource=" + foreignSource +"%"))
        .add(Restrictions.like("eventParms", "%componentForeignId=" + foreignId +"%"));

        for(final OnmsAlarm alarm : m_alarmDao.findMatching(alarmCriteria)) {
            m_alarmDao.delete(alarm);
        }

        m_alarmDao.flush();
        
        try {
			sendComponentDeleted(component);
		} catch (final EventProxyException e) {
			throw new RecoverableDataAccessException("update completed, but unable to send componentDeleted event", e);
		}
	}

	protected void sendComponentAdded(final NCSComponent component) throws EventProxyException {
		final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_ADDED_UEI, "NCSComponentService");
		builder.addParam("componentType", component.getType());
		builder.addParam("componentName", component.getName());
		builder.addParam("componentForeignSource", component.getForeignSource());
		builder.addParam("componentForeignId", component.getForeignId());
		m_eventIpcManager.send(builder.getEvent());
	}
	
	protected void sendComponentDeleted(final NCSComponent component) throws EventProxyException {
		final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_DELETED_UEI, "NCSComponentService");
		builder.addParam("componentType", component.getType());
		builder.addParam("componentName", component.getName());
		builder.addParam("componentForeignSource", component.getForeignSource());
		builder.addParam("componentForeignId", component.getForeignId());
		m_eventIpcManager.send(builder.getEvent());
	}

	protected void sendComponentUpdated(final NCSComponent component) throws EventProxyException {
		final EventBuilder builder = new EventBuilder(EventConstants.COMPONENT_UPDATED_UEI, "NCSComponentService");
		builder.addParam("componentType", component.getType());
		builder.addParam("componentName", component.getName());
		builder.addParam("componentForeignSource", component.getForeignSource());
		builder.addParam("componentForeignId", component.getForeignId());
		m_eventIpcManager.send(builder.getEvent());
	}
}
