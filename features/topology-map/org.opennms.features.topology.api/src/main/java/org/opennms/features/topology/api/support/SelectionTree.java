package org.opennms.features.topology.api.support;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.WidgetContext;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;

@SuppressWarnings({"serial", "unchecked"})
public abstract class SelectionTree extends Tree implements SelectionListener, IViewContribution {
    
    private class TreeItemClickTracker{
        
        private Object m_clickedItemId;
        private boolean m_remove;
        public TreeItemClickTracker() {}
        
        public void setClickedItemId(Object itemId) {
            m_clickedItemId = itemId;
            m_remove = false;
        }
        public Object getClickedItemId() {
            return m_clickedItemId;
        }
        
        public void setRemove(boolean remove) {
            m_remove = remove;
        }
        
        public boolean isRemoved() {
            return m_remove;
        }
    }
    
    private final TreeItemClickTracker m_treeItemClickTracker = new TreeItemClickTracker();
    private boolean m_itemClicked = false;
    
    public SelectionTree(FilterableHierarchicalContainer container) {
        super(null, container);
        
        this.addListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                
                if(m_itemClicked) {
                    Set<Object> selectedIds = (Set<Object>) event.getProperty().getValue();
                    
                    Collection<Object> allIds = (Collection<Object>) getContainerDataSource().getItemIds();
                    
                    Set<Object> itemsToSelect = getSelectedItemIds(selectedIds);
                    
                    Set<Object> itemsToDeselected = getItemsToDeselecte(allIds, itemsToSelect);
                    
                    deselectContainerItems(itemsToDeselected);
                    
                    selectContainerItemAndChildren(itemsToSelect);
                } 
                
            }
        });
        
        this.addListener(new ItemClickListener() {
            
            @Override
            public void itemClick(ItemClickEvent event) {
                m_itemClicked = true;
                Set<Object> selectedIds = (Set<Object>) ((SelectionTree) event.getSource()).getValue();
                
                Object itemId = event.getItemId();
                m_treeItemClickTracker.setClickedItemId(itemId);
                
                if((event.isCtrlKey() || event.isMetaKey()) && selectedIds.contains(itemId)) {
                    m_treeItemClickTracker.setRemove(true);
                } 
                
            }
        });
    }

    @Override
    public void onSelectionUpdate(Container container) {
        m_itemClicked = false;
        Collection<?> itemIds = container.getItemIds();
        
        for(Object itemId : itemIds) {
            Item item = container.getItem(itemId);
            if((Boolean) item.getItemProperty("selected").getValue()) {
                select(itemId);
            } else {
                unselect(itemId);
            }
        }
    }
    
    public void select(Collection<Object> itemIds) {
        
        for(Object itemId : itemIds) {
            select(itemId);
        }
    }
    
    public void deselect(Collection<Object> itemIds) {
        for(Object itemId : itemIds) {
            unselect(itemId);
        }
    }

    private Set<Object> getSelectedItemIds(Set<Object> selectedIds) {
        Set<Object> itemsToSelect = new LinkedHashSet<Object>(selectedIds);
        if(m_treeItemClickTracker.isRemoved()) {
            if(getParent(m_treeItemClickTracker.getClickedItemId()) != null) {
                unselect( getParent(m_treeItemClickTracker.getClickedItemId() ) );
            }
            unselect(m_treeItemClickTracker.getClickedItemId());
            itemsToSelect.remove(m_treeItemClickTracker.getClickedItemId());
        }
        return itemsToSelect;
    }

    private Set<Object> getItemsToDeselecte(Collection<Object> allIds, Set<Object> itemsToSelect) {
        Set<Object> itemsToDeselected = new LinkedHashSet<Object>(allIds);
        itemsToDeselected.removeAll(itemsToSelect);
        return itemsToDeselected;
    }

    private void deselectContainerItems(Set<Object> itemsToDeselected) {
        for(Object itemId : itemsToDeselected) {
            Property property = getContainerDataSource().getContainerProperty(itemId, "selected");
            property.setValue(false);
        }
    }

    private void selectContainerItemAndChildren(Set<Object> itemsToSelect) {
        for(Object itemId : itemsToSelect) {
            Property property = getContainerDataSource().getContainerProperty(itemId, "selected");
            property.setValue(true);
            if( hasChildren(itemId) ) {
                for(Object id : getChildren(itemId)) {
                    select(id);
                }
            }
        }
        getContainerDataSource().fireItemUpdated();
    }
    
    @Override
    public FilterableHierarchicalContainer getContainerDataSource() {
        return (FilterableHierarchicalContainer) super.getContainerDataSource();
    }

    @Override
    public abstract String getTitle();

    @Override
    public Component getView(WidgetContext widgetContext) {
        return this;
    }
}
