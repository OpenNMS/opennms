package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.ListDataProvider;
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
        
        private final List<ResourceType> m_resourceTypes;
        
        public CustomTreeModel(List<ResourceListItem> resourceList) {
            m_resourceTypes = new ArrayList<ResourceType>();
            
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
              
              Cell<ResourceType> cell = new AbstractCell<ResourceType>() {

                @Override
                public void render(Context context, ResourceType value, SafeHtmlBuilder sb) {
                    if(value != null) {
                        sb.appendEscaped(value.getName());
                    }
                }
                  
              };
              
              return new DefaultNodeInfo<ResourceType>(dataProvider, cell);
              
          }else if(value instanceof ResourceType) {
              ListDataProvider<ResourceListItem> dataProvider = new ListDataProvider<ResourceListItem>(((ResourceType) value).getResourceList());
              
              Cell<ResourceListItem> cell = new AbstractCell<ResourceListItem>() {

                @Override
                public void render(Context context, ResourceListItem value, SafeHtmlBuilder sb) {
                    if(value != null) {
                        sb.appendEscaped(value.getValue());
                    }
                    
                }
                  
              };
              
              return new DefaultNodeInfo<ResourceListItem>(dataProvider, cell);
          }
          // Return a node info that pairs the data with a cell.
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
    
    public ReportSelectListCellTree(List<ResourceListItem> resourceList) {
        super(new CustomTreeModel(resourceList), null);
    }

}
