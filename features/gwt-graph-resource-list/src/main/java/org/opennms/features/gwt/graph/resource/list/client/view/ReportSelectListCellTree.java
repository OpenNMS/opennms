/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
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
        
        private final static class ResourceListItemCell extends AbstractCell<ResourceListItem> {
            @Override
            public void render(Context context, ResourceListItem value, SafeHtmlBuilder sb) {
                if(value != null) {
                    sb.appendEscaped(value.getValue());
                }
                
            }
        }

        private final static class ResourceTypCell extends AbstractCell<ResourceType> {
            @Override
            public void render(Context context, ResourceType value, SafeHtmlBuilder sb) {
                if(value != null) {
                    sb.appendEscaped(value.getName() + "    (" + value.getResourceList().size() + ")");
                }
            }
        }

        private final List<ResourceType> m_resourceTypes;
        private final SelectionModel<ResourceListItem> m_selectionModel;
        private final Cell<ResourceListItem> m_resourceListItemCell;
        private final DefaultSelectionEventManager<ResourceListItem> m_selectionManager = DefaultSelectionEventManager.createCheckboxManager();
        
        public CustomTreeModel(List<ResourceListItem> resourceList, SelectionModel<ResourceListItem> selectionModel) {
            m_resourceTypes = new ArrayList<ResourceType>();
            organizeList(resourceList);
            
            m_selectionModel = selectionModel;
            
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
                    return m_selectionModel.isSelected(object);
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
                    super.render(context, value, sb);
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
                ResourceType rType = new ResourceType(typeName);
                
                for(ResourceListItem r : resourceList) {
                    if(r.getType().equals(typeName)) {
                        rType.addResourceListItem(r);
                    }
                   
                }
                m_resourceTypes.add(rType);
            }
        }

        /**
         * Get the {@link NodeInfo} that provides the children of the specified
         * value.
         */
        @Override
        public <T> NodeInfo<?> getNodeInfo(T value) {
          if(value == null) {
              ListDataProvider<ResourceType> dataProvider = new ListDataProvider<ResourceType>(m_resourceTypes);
              
              Cell<ResourceType> cell = new ResourceTypCell();
              
              return new DefaultNodeInfo<ResourceType>(dataProvider, cell);
              
          }else if(value instanceof ResourceType) {
              ListDataProvider<ResourceListItem> dataProvider = new ListDataProvider<ResourceListItem>(((ResourceType) value).getResourceList());
              
              return new DefaultNodeInfo<ResourceListItem>(dataProvider, m_resourceListItemCell, m_selectionModel, m_selectionManager, null);
          }
          return null;
        }

        /**
         * Check if the specified value represents a leaf node. Leaf nodes cannot be
         * opened.
         */
        @Override
        public boolean isLeaf(Object value) {
            if(value instanceof ResourceListItem) {
                return true;
            }else {
                return false;
            }
        }
      }
    
    public ReportSelectListCellTree(List<ResourceListItem> resourceList, SelectionModel<ResourceListItem> selectionModel) {
        super(new CustomTreeModel(resourceList, selectionModel), null, (CellTree.Resources)GWT.create(CustomCellTreeResource.class));
        setDefaultNodeSize(10000);
        
        TreeNode treeNode = getRootTreeNode();
        for(int i = 0; i < treeNode.getChildCount(); i++) {
            treeNode.setChildOpen(i, true);
        }
    }
    
}
