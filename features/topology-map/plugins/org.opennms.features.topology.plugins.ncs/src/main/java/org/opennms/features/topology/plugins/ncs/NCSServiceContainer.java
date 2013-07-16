package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.support.HierarchicalBeanContainer;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

public class NCSServiceContainer extends HierarchicalBeanContainer<Long, NCSServiceItem> {

	private static final long serialVersionUID = 3245953234720320852L;
	private static final String FOREIGN_SOURCE_PROPERTY = "foreignSource";

	private final NCSComponentRepository m_dao;
	private final Set<NCSServiceItem> m_rootItems = new HashSet<NCSServiceItem>();

	public NCSServiceContainer(NCSComponentRepository dao) {
		super(NCSServiceItem.class);
		m_dao = dao;
		setBeanIdProperty("id");
		
		List<NCSComponent> services = m_dao.findByType("Service");
		createRootItems(services);
		addAll(m_rootItems);
        addAll(createListFromComponents(services));
	}
	

    private void createRootItems(List<NCSComponent> components) {
        Set<String> foreignSources = new HashSet<String>();
        for(NCSComponent component : components) {
            if(!foreignSources.contains(component.getForeignSource())) {
                foreignSources.add(component.getForeignSource());
                m_rootItems.add(new NCSRootServiceItem(component));
            }
        }
    }


    private Collection<? extends NCSServiceItem> createListFromComponents(List<NCSComponent> ncsComponents) {
        Collection<NCSServiceItem> list = new ArrayList<NCSServiceItem>();
        for(NCSComponent ncsComponent : ncsComponents) {
            list.add(new NCSServiceItem(ncsComponent));
        }
        return list;
    }

    @Override
	public boolean areChildrenAllowed(Object itemId) {
		//Assert.isInstanceOf(Long.class, itemId);
		BeanItem<NCSServiceItem> component = getItem(itemId);
		return (Boolean) component.getItemProperty("childrenAllowed").getValue();
	}
	

	@Override
	public Collection<Long> getChildren(Object itemId) {
		//Assert.isInstanceOf(Long.class, itemId);
		BeanItem<NCSServiceItem> component = getItem(itemId);
		String foreignSource = (String) component.getItemProperty( FOREIGN_SOURCE_PROPERTY ).getValue();
		System.err.println("entering method getChildren");
		List<Long> retval = new ArrayList<Long>();
		for (Long id : getAllItemIds()) {
			// Per talks with Paulo, only descend to the level of ServiceElement.
			// ServiceElementComponents have no representation on the current map
			// implementation.
		    boolean isRoot = (Boolean) getItem(id).getItemProperty("isRoot").getValue();
			Property itemProperty = getItem(id).getItemProperty( FOREIGN_SOURCE_PROPERTY );
            String fSource = (String)itemProperty.getValue();
            if(!isRoot && fSource.equals(foreignSource)) {
			    retval.add(id);
			}
		}
		return retval;
	}

	@Override
	public Long getParent(Object itemId) {
		//Assert.isInstanceOf(Long.class, itemId);
		BeanItem<NCSServiceItem> component = getItem(itemId);
		Object itemForeignSource = component.getItemProperty( FOREIGN_SOURCE_PROPERTY ).getValue();
		
		for(Long rootId : rootItemIds()) {
		    BeanItem<NCSServiceItem> rootItem = getItem(rootId);
		    
            String rootForeignSource = (String)rootItem.getItemProperty( FOREIGN_SOURCE_PROPERTY ).getValue();
            if(rootForeignSource.equals(itemForeignSource)) {
                return rootId;
            }
		}
		return null;
	}

	@Override
	public Collection<Long> rootItemIds() {
		List<Long> retval = new ArrayList<Long>();
		// Return all components of type "Service"
		for (NCSServiceItem item : m_rootItems) {
			retval.add(item.getId());
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
//		Long id = (Long)itemId;
//		Long parentId = (Long)newParentId;
//		NCSComponent component = m_dao.load(id);
//		Set<NCSComponent> parent = new HashSet<NCSComponent>();
//		parent.add(m_dao.load(parentId));
//		component.setParentComponents(parent);
		return true;
	}

	
}
