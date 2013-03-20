package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.support.HierarchicalBeanContainer;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

public class NCSServiceContainer extends HierarchicalBeanContainer<Long, NCSComponent> {

	private static final long serialVersionUID = 3245953234720320852L;

	private final NCSComponentRepository m_dao;

	public NCSServiceContainer(NCSComponentRepository dao) {
		super(NCSComponent.class);
		m_dao = dao;
		setBeanIdProperty("id");
		addAll(m_dao.findByType("Service"));
		addAll(m_dao.findByType("ServiceElement"));
	}

	@Override
	public boolean areChildrenAllowed(Object itemId) {
		//Assert.isInstanceOf(Long.class, itemId);
		Long id = (Long)itemId;
		NCSComponent component = m_dao.load(id);
		if ("Service".equals(component.getType())) {
			return true;
		/**
		} else if ("ServiceElement".equals(component.getType())) {
			return true;
		*/
		} else {
			return false;
		}
	}

	@Override
	public Collection<Long> getChildren(Object itemId) {
		//Assert.isInstanceOf(Long.class, itemId);
		Long id = (Long)itemId;
		NCSComponent component = m_dao.load(id);
		List<Long> retval = new ArrayList<Long>();
		for (NCSComponent sub : component.getSubcomponents()) {
			// Per talks with Paulo, only descend to the level of ServiceElement.
			// ServiceElementComponents have no representation on the current map
			// implementation.
			if("ServiceElement".equals(component.getType())) {
				retval.add(sub.getId());
			}
		}
		return retval;
	}

	@Override
	public Long getParent(Object itemId) {
		//Assert.isInstanceOf(Long.class, itemId);
		Long id = (Long)itemId;
		NCSComponent component = m_dao.load(id);
		return component.getParentComponents().iterator().next().getId();
	}

	@Override
	public Collection<Long> rootItemIds() {
		List<Long> retval = new ArrayList<Long>();
		// Return all components of type "Service"
		for (NCSComponent sub : m_dao.findByType("Service")) {
			retval.add(sub.getId());
		}
		return retval;
	}

	@Override
	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) 
		throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot setChildredAllowed() on NCSComponent type");
	}

	@Override
	public boolean setParent(Object itemId, Object newParentId)
		throws UnsupportedOperationException {
		//Assert.isInstanceOf(Long.class, itemId);
		//Assert.isInstanceOf(Long.class, newParentId);
		Long id = (Long)itemId;
		Long parentId = (Long)newParentId;
		NCSComponent component = m_dao.load(id);
		Set<NCSComponent> parent = new HashSet<NCSComponent>();
		parent.add(m_dao.load(parentId));
		component.setParentComponents(parent);
		return true;
	}

	
}
