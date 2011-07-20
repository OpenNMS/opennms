package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.gwt.graph.resource.list.client.view.styles.CustomCellTreeResource;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class ReportSelectListCellTree extends CellTree {
    
    private static class ResourceType{
        private final String m_name;
        private final List<ResourceListItem> m_resourceList = new ArrayList<ResourceListItem>();
        
        public ResourceType(String name) {
            m_name = name;
        }
        
        public void addResourceListItem(ResourceListItem item) {
            m_resourceList.add(item);
        }
        
        public String getName() {
            return m_name;
        }
        
        public List<ResourceListItem> getResourceList(){
            return m_resourceList;
        }
    }
    
    
    private static class CustomTreeModel implements TreeViewModel {
        
        private final class ResourceListItemCell extends AbstractCell<ResourceListItem> {
            @Override
            public void render(Context context, ResourceListItem value, SafeHtmlBuilder sb) {
                if(value != null) {
                    //sb.appendHtmlConstant("<div style='font-size: 70%;'>");
                    sb.appendEscaped(value.getValue());
                    //sb.appendHtmlConstant("</div>");
                }
                
            }
        }

        private final class ResourceTypCell extends AbstractCell<ResourceType> {
            @Override
            public void render(Context context, ResourceType value, SafeHtmlBuilder sb) {
                if(value != null) {
                    //sb.appendHtmlConstant("<div style='font-size: 70%'>");
                    sb.appendEscaped(value.getName());
                    //sb.appendHtmlConstant("</div>");
                }
            }
        }

        private final List<ResourceType> m_resourceTypes;
        private final MultiSelectionModel<ResourceListItem> m_multipleSelectionModel;
        private final Cell<ResourceListItem> m_resourceListItemCell;
        private final DefaultSelectionEventManager<ResourceListItem> m_selectionManager = DefaultSelectionEventManager.createCheckboxManager();
        
        public CustomTreeModel(List<ResourceListItem> resourceList, MultiSelectionModel<ResourceListItem> selectionModel) {
            m_resourceTypes = new ArrayList<ResourceType>();
            organizeList(resourceList);
            
            m_multipleSelectionModel = selectionModel;
            
            List<HasCell<ResourceListItem, ?>> hasCells = new ArrayList<HasCell<ResourceListItem, ?>>();
            hasCells.add(new HasCell<ResourceListItem, Boolean>(){
                
                private CheckboxCell m_cell = new CheckboxCell(true, false);
                
                @Override
                public Cell<Boolean> getCell() {
                    return m_cell;
                }

                @Override
                public FieldUpdater<ResourceListItem, Boolean> getFieldUpdater() {
                    return null;
                }

                @Override
                public Boolean getValue(ResourceListItem object) {
                    return m_multipleSelectionModel.isSelected(object);
                }
            });
            
            hasCells.add(new HasCell<ResourceListItem, ResourceListItem>(){
                private ResourceListItemCell m_cell = new ResourceListItemCell();
                @Override
                public Cell<ResourceListItem> getCell() {
                    return m_cell;
                }

                @Override
                public FieldUpdater<ResourceListItem, ResourceListItem> getFieldUpdater() {
                    return null;
                }

                @Override
                public ResourceListItem getValue(ResourceListItem object) {
                    return object;
                }
                
            });
            
            m_resourceListItemCell = new CompositeCell<ResourceListItem>(hasCells) {

                @Override
                public void render(Context context, ResourceListItem value, SafeHtmlBuilder sb) {
                    sb.appendHtmlConstant("<table><tbody><tr>");
                    super.render(context, value, sb);
                    sb.appendHtmlConstant("</tr></tbody></table>");
                }

                @Override
                protected Element getContainerElement(Element parent) {
                    return super.getContainerElement(parent);
                }

                @Override
                protected <X> void render(Context context, ResourceListItem value, SafeHtmlBuilder sb, 
                        HasCell<ResourceListItem, X> hasCell) {
                    super.render(context, value, sb, hasCell);
                }
                
                
            };
        }

        private void organizeList(List<ResourceListItem> resourceList) {
            
            Map<String, String> types = new HashMap<String, String>();
            
            for(ResourceListItem item : resourceList) {
                if(!types.containsKey(item.getType())) {
                    types.put(item.getType(), item.getType());
                }
            }
            
            for(String typeName : types.keySet()) {
                
                for(ResourceListItem r : resourceList) {
                    if(r.getType().equals(typeName)) {
                        ResourceType rType = new ResourceType(typeName);
                        rType.addResourceListItem(r);
                        m_resourceTypes.add(rType);
                    }
                }
            }
        }

        /**
         * Get the {@link NodeInfo} that provides the children of the specified
         * value.
         */
        public <T> NodeInfo<?> getNodeInfo(T value) {
          if(value == null) {
              ListDataProvider<ResourceType> dataProvider = new ListDataProvider<ResourceType>(m_resourceTypes);
              
              Cell<ResourceType> cell = new ResourceTypCell();
              
              return new DefaultNodeInfo<ResourceType>(dataProvider, cell);
              
          }else if(value instanceof ResourceType) {
              ListDataProvider<ResourceListItem> dataProvider = new ListDataProvider<ResourceListItem>(((ResourceType) value).getResourceList());
              
              return new DefaultNodeInfo<ResourceListItem>(dataProvider, m_resourceListItemCell, m_multipleSelectionModel, m_selectionManager, null);
          }
          return null;
        }

        /**
         * Check if the specified value represents a leaf node. Leaf nodes cannot be
         * opened.
         */
        public boolean isLeaf(Object value) {
            if(value instanceof ResourceListItem) {
                return true;
            }else {
                return false;
            }
        }
      }
    
    
    public ReportSelectListCellTree(List<ResourceListItem> resourceList, MultiSelectionModel<ResourceListItem> selectionModel) {
        super(new CustomTreeModel(resourceList, selectionModel), null, (CellTree.Resources)GWT.create(CustomCellTreeResource.class));
    }


}
