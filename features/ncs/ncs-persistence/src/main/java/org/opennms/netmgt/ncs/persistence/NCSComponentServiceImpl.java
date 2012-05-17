package org.opennms.netmgt.ncs.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.rest.NCSRestService.ComponentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class NCSComponentServiceImpl implements NCSComponentService {
	private static final Set<NCSComponent> EMPTY_COMPONENT_SET = Collections.unmodifiableSet(new HashSet<NCSComponent>());

	@Autowired
	NCSComponentDao m_componentDao;

	@Autowired
	AlarmDao m_alarmDao;
	
	@Autowired
	EventDao m_eventDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    EventProxy m_eventProxy;

	public void setEventProxy(final EventProxy proxy) throws Exception {
		m_eventProxy = proxy;
	}

	@Override
	@Transactional
	public NCSComponent getComponent(final String type, final String foreignSource, final String foreignId) {
		LogUtils.debugf(this, "getComponent(%s, %s, %s)", type, foreignSource, foreignId);
		return getComponent(new ComponentIdentifier(null, type, foreignSource, foreignId, null, null));
	}

	@Override
	@Transactional
	public ComponentList findComponentsWithAttribute(final String attrKey, final String attrValue) {
		LogUtils.debugf(this, "findComponentsWithAttribute(%s, %s)", attrKey, attrValue);
		return new ComponentList(m_componentDao.findComponentsWithAttribute(attrKey, attrValue));
	}

	@Override
	@Transactional
	public NCSComponent addOrUpdateComponents(final NCSComponent component, final boolean deleteOrphans) {
		final ComponentIdentifier componentId = getIdentifier(component);
		LogUtils.debugf(this, "addOrUpdateComponents(%s, %s)", componentId, Boolean.valueOf(deleteOrphans));
		final ComponentEventQueue ceq = new ComponentEventQueue();
		final NCSComponent updatedComponent = addOrUpdateComponents(componentId, component, ceq, deleteOrphans);
		try {
			ceq.sendAll(m_eventProxy);
		} catch (final EventProxyException e) {
			LogUtils.warnf(this, e, "Component %s added, but an error occured while sending add/delete/update events.", componentId);
		}
		return updatedComponent;
	}

	@Override
	@Transactional
	public NCSComponent addSubcomponent(final String type, final String foreignSource, final String foreignId, final NCSComponent subComponent, final boolean deleteOrphans) {
		final ComponentIdentifier subComponentId = getIdentifier(subComponent);
		
		LogUtils.debugf(this, "addSubcomponent(%s, %s, %s, %s, %s)", type, foreignSource, foreignId, subComponentId, Boolean.valueOf(deleteOrphans));

		final NCSComponent component = getComponent(type, foreignSource, foreignId);
		final ComponentIdentifier id = getIdentifier(component);
		final ComponentEventQueue ceq = new ComponentEventQueue();

		if (component == null) {
			throw new ObjectRetrievalFailureException(NCSComponent.class, "Unable to locate component with type=" + type + ", foreignSource=" + foreignSource + ", foreignId=" + foreignId);
		}

		final NCSComponent updatedSubComponent = addOrUpdateComponents(subComponentId, subComponent, ceq, deleteOrphans);
		component.addSubcomponent(updatedSubComponent);
		
		m_componentDao.update(component);
		ceq.componentUpdated(id);

		try {
			ceq.sendAll(m_eventProxy);
		} catch (final EventProxyException e) {
			LogUtils.warnf(this, e, "Component %s added to %s, but an error occured while sending add/delete/update events.", subComponentId, id);
		}

		return getComponent(id);
	}

	@Override
	@Transactional
	public void deleteComponent(final String type, final String foreignSource, final String foreignId, final boolean deleteOrphans) {
		LogUtils.debugf(this, "deleteSubcomponent(%s, %s, %s, %s)", type, foreignSource, foreignId, Boolean.valueOf(deleteOrphans));

		final NCSComponent component = getComponent(type, foreignSource, foreignId);
		final ComponentIdentifier id = getIdentifier(component);
		final ComponentEventQueue ceq = new ComponentEventQueue();
		deleteComponent(id, ceq, deleteOrphans);
		try {
			ceq.sendAll(m_eventProxy);
		} catch (final EventProxyException e) {
			LogUtils.warnf(this, e, "Component %s deleted, but an error occured while sending delete/update events.", id);
		}
	}







	private Set<ComponentIdentifier> getIdentifiers(final Collection<NCSComponent> components) {
		final Set<ComponentIdentifier> identifiers = new HashSet<ComponentIdentifier>();
		for (final NCSComponent component : components) {
			identifiers.add(getIdentifier(component));
		}
		return identifiers;
	}
	
	private ComponentIdentifier getIdentifier(final NCSComponent component) {
		return new ComponentIdentifier(component.getId(), component.getType(), component.getForeignSource(), component.getForeignId(), component.getName(), component.getDependenciesRequired());
	}

	private NCSComponent getComponent(final ComponentIdentifier id) {
		return m_componentDao.findByTypeAndForeignIdentity(id.getType(), id.getForeignSource(), id.getForeignId());
	}

	private NCSComponent addOrUpdateComponents(final ComponentIdentifier id, final NCSComponent component, final ComponentEventQueue ceq, final boolean deleteOrphans) {
		final Set<NCSComponent> subcomponents = new LinkedHashSet<NCSComponent>();
		
		final NCSComponent existing = new UpsertTemplate<NCSComponent, NCSComponentDao>(m_transactionManager, m_componentDao) {
			@Override
			protected NCSComponent query() {
				return getComponent(id);
			}

			@Override
			protected NCSComponent doInsert() {
				for (final NCSComponent subcomponent : component.getSubcomponents()) {
					final NCSComponent updatedComponent = addOrUpdateComponents(getIdentifier(subcomponent), subcomponent, ceq, deleteOrphans);
					subcomponents.add(updatedComponent);
				}

				component.setSubcomponents(subcomponents);
				m_componentDao.save(component);
				ceq.componentAdded(getIdentifier(component));
				return component;
			}
			
			@Override
			protected NCSComponent doUpdate(final NCSComponent dbObj) {
				for (final NCSComponent subcomponent : component.getSubcomponents()) {
					final NCSComponent updatedComponent = addOrUpdateComponents(getIdentifier(subcomponent), subcomponent, ceq, deleteOrphans);
					subcomponents.add(updatedComponent);
				}

				if (deleteOrphans) deleteOrphanedComponents(getIdentifiers(dbObj.getSubcomponents()), getIdentifiers(subcomponents), ceq);

				dbObj.setName(component.getName());
				dbObj.setVersion(component.getVersion());
				dbObj.setDependenciesRequired(component.getDependenciesRequired());
				dbObj.setNodeIdentification(component.getNodeIdentification());
				dbObj.setUpEventUei(component.getUpEventUei());
				dbObj.setDownEventUei(component.getDownEventUei());
				dbObj.setAttributes(component.getAttributes());
				dbObj.setSubcomponents(subcomponents);
				m_componentDao.update(dbObj);
				ceq.componentUpdated(getIdentifier(dbObj));
				return dbObj;
			}

		}.execute();

		return existing;
	}

	private void deleteComponent(final ComponentIdentifier id, final ComponentEventQueue ceq, final boolean deleteOrphans) {
		final NCSComponent component = getComponent(id);

        if (component == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        final Set<NCSComponent> parentComponents = component.getParentcomponents();
    	final Set<ComponentIdentifier> childrenIdentifiers = getIdentifiers(component.getSubcomponents());

        // first, we deal with orphans
        if (deleteOrphans) {
			for (final ComponentIdentifier subId : childrenIdentifiers) {
				handleOrphanedComponents(component, subId, ceq, deleteOrphans);
	        }
        }

        // first, we remove this component from each of its parents
        for(final NCSComponent parent : parentComponents) {
            parent.getSubcomponents().remove(component);
            m_componentDao.update(parent);
        }

        // then we delete this component
    	component.setSubcomponents(EMPTY_COMPONENT_SET);
        m_componentDao.delete(component);

        // and any events or alarms depending on it
        deleteEvents(id.getForeignSource(), id.getForeignId());
        deleteAlarms(id.getForeignSource(), id.getForeignId());
        
        // alert that the component is deleted
		ceq.componentDeleted(getIdentifier(component));

        // then alert about the parents
        sendUpdateEvents(ceq, getIdentifiers(parentComponents));

	}

	private void handleOrphanedComponents(final NCSComponent parent, final ComponentIdentifier child, final ComponentEventQueue ceq, final boolean deleteOrphans) {
		final ComponentIdentifier parentId = getIdentifier(parent);
		final NCSComponent childComponent = getComponent(child);

		final Set<ComponentIdentifier> childChildren = getIdentifiers(childComponent.getSubcomponents());
		final Set<ComponentIdentifier> childParents  = getIdentifiers(childComponent.getParentcomponents());

		LogUtils.tracef(this, "handleOrphanedComponents: parent: %s", parentId);
		LogUtils.tracef(this, "handleOrphanedComponents: child: %s", child);
		LogUtils.tracef(this, "handleOrphanedComponents: child's children: %s", childChildren);
		LogUtils.tracef(this, "handleOrphanedComponents: child's parents: %s", childParents);

		if (childParents.size() == 1) {
			final ComponentIdentifier childParent = childParents.iterator().next();
			if (childParent.equals(parentId)) {
				LogUtils.tracef(this, "handleOrphanedComponents: child (%s) has only one parent (%s) and it's being deleted.", child, childParent);
				deleteComponent(child, ceq, deleteOrphans);
			} else {
				LogUtils.tracef(this, "handleOrphanedComponents: child (%s) has only one parent (%s) but it's not the one we expected. This is weird.", child, childParent);
				ceq.componentUpdated(childParent);
			}
		} else {
			LogUtils.tracef(this, "handleOrphanedComponents: child (%s) has more than one parent, sending updates for remaining parents.", child);
			for (final ComponentIdentifier childParent : childParents) {
				ceq.componentUpdated(childParent);
			}
		}
	}

	private void sendUpdateEvents(final ComponentEventQueue ceq, final Collection<ComponentIdentifier> parentIds) {
		LogUtils.debugf(this, "sendUpdateEvents: parents = %s", parentIds);
		for (final ComponentIdentifier parentId : parentIds) {
        	ceq.componentUpdated(parentId);
        }
	}
	
	private void deleteOrphanedComponents(final Set<ComponentIdentifier> oldComponents, final Set<ComponentIdentifier> newComponents, final ComponentEventQueue ceq) {
		for (final ComponentIdentifier id : oldComponents) {
			if (!newComponents.contains(id)) {
				deleteComponent(id, ceq, true);
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
}
