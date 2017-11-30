/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;

import java.util.List;
import java.util.Map;

/**
 * This class represents a Rrd graph selection window.
 *
 * @author Christian Pape
 */
public class GraphSelectionWindow extends Window {
    /**
     * the tree component
     */
    private Tree m_tree;

    /**
     * Constructor for creating new instances.
     *
     * @param nodeDao        the node dao instance
     * @param rrdGraphHelper the rrd graph helper instance
     * @param rrdGraphEntry  the entry to be edited
     */
    public GraphSelectionWindow(final NodeDao nodeDao, final RrdGraphHelper rrdGraphHelper, final RrdGraphEntry rrdGraphEntry) {
        /**
         * Setting the title
         */
        super("Select RRD graph");

        /**
         * setting up the component
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(50, Unit.PERCENTAGE);
        setHeight(70, Unit.PERCENTAGE);

        /**
         * setting up the container
         */
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();

        hierarchicalContainer.addContainerProperty("id", String.class, null);
        hierarchicalContainer.addContainerProperty("label", String.class, null);
        hierarchicalContainer.addContainerProperty("type", String.class, null);

        hierarchicalContainer.addContainerProperty("nodeId", String.class, null);
        hierarchicalContainer.addContainerProperty("nodeLabel", String.class, null);
        hierarchicalContainer.addContainerProperty("resourceId", String.class, null);
        hierarchicalContainer.addContainerProperty("resourceLabel", String.class, null);
        hierarchicalContainer.addContainerProperty("resourceTypeId", String.class, null);
        hierarchicalContainer.addContainerProperty("resourceTypeLabel", String.class, null);
        hierarchicalContainer.addContainerProperty("graphId", String.class, null);
        hierarchicalContainer.addContainerProperty("graphLabel", String.class, null);
        hierarchicalContainer.addContainerProperty("graphUrl", String.class, null);

        /**
         * filling the container with node data
         */
        List<OnmsNode> onmsNodeList = nodeDao.findAll();

        for (OnmsNode onmsNode : onmsNodeList) {
            Item item = hierarchicalContainer.addItem(onmsNode.getId().toString());
            item.getItemProperty("label").setValue(onmsNode.getLabel() + " (" + onmsNode.getId() + ")");
            item.getItemProperty("id").setValue(onmsNode.getId().toString());
            item.getItemProperty("type").setValue("node");
            item.getItemProperty("nodeId").setValue(onmsNode.getId().toString());
        }

        /**
         * creating a panel for the tree component
         */
        Panel panel = new Panel();

        m_tree = new Tree();
        m_tree.setCaption("Graph");
        m_tree.setSizeFull();
        m_tree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        m_tree.setItemCaptionPropertyId("label");
        m_tree.setContainerDataSource(hierarchicalContainer);
        m_tree.setMultiSelect(false);
        m_tree.setNewItemsAllowed(false);
        m_tree.setImmediate(true);

        /**
         * adding en expand listener for lazy loading the resourceType and resource data
         */
        m_tree.addExpandListener(new Tree.ExpandListener() {
            @Override
            public void nodeExpand(Tree.ExpandEvent expandEvent) {
                String itemToExpandId = String.valueOf(expandEvent.getItemId());

                /**
                 * if the data has already been loaded, return
                 */
                if (m_tree.hasChildren(itemToExpandId)) {
                    return;
                }

                Item itemToExpand = m_tree.getItem(expandEvent.getItemId());
                String type = itemToExpand.getItemProperty("type").getValue().toString();

                /**
                 * a node is selected
                 */
                if ("node".equals(type)) {
                    Map<OnmsResourceType, List<OnmsResource>> resourceTypeMap = rrdGraphHelper.getResourceTypeMapForNodeId(String.valueOf(itemToExpand.getItemProperty("id").getValue()));

                    for (Map.Entry<OnmsResourceType, List<OnmsResource>> resourceTypeMapEntry : resourceTypeMap.entrySet()) {
                        String newResourceTypeItemId = "node[" + itemToExpandId + "]." + resourceTypeMapEntry.getKey().getName();

                        Item newResourceTypeItem = hierarchicalContainer.addItem(newResourceTypeItemId);

                        newResourceTypeItem.getItemProperty("label").setValue(resourceTypeMapEntry.getKey().getLabel());
                        newResourceTypeItem.getItemProperty("type").setValue("resourceType");
                        newResourceTypeItem.getItemProperty("nodeId").setValue(itemToExpandId);
                        newResourceTypeItem.getItemProperty("nodeLabel").setValue(itemToExpand.getItemProperty("label").getValue());
                        newResourceTypeItem.getItemProperty("resourceTypeId").setValue(newResourceTypeItemId);
                        newResourceTypeItem.getItemProperty("resourceTypeLabel").setValue(resourceTypeMapEntry.getKey().getLabel());

                        m_tree.setParent(newResourceTypeItemId, itemToExpandId);
                        m_tree.setChildrenAllowed(newResourceTypeItemId, true);

                        for (OnmsResource onmsResource : resourceTypeMapEntry.getValue()) {

                            String newResourceItemId = onmsResource.getId().toString();

                            Item newResourceItem = hierarchicalContainer.addItem(newResourceItemId);

                            newResourceItem.getItemProperty("label").setValue(onmsResource.getLabel());
                            newResourceItem.getItemProperty("type").setValue("resource");
                            newResourceItem.getItemProperty("nodeId").setValue(itemToExpandId);
                            newResourceItem.getItemProperty("nodeLabel").setValue(itemToExpand.getItemProperty("label").getValue());
                            newResourceItem.getItemProperty("resourceId").setValue(newResourceItemId);
                            newResourceItem.getItemProperty("resourceLabel").setValue(onmsResource.getLabel());
                            newResourceItem.getItemProperty("resourceTypeId").setValue(newResourceTypeItemId);
                            newResourceItem.getItemProperty("resourceTypeLabel").setValue(newResourceTypeItem.getItemProperty("label").getValue());

                            m_tree.setParent(newResourceItemId, newResourceTypeItemId);
                            m_tree.setChildrenAllowed(newResourceItemId, true);
                        }
                    }
                }

                /**
                 * a resource is selected
                 */
                if ("resource".equals(type)) {
                    final ResourceId resourceId = ResourceId.fromString(itemToExpandId);

                    Map<String, String> map = rrdGraphHelper.getGraphResultsForResourceId(resourceId);
                    Map<String, String> titleNameMapping = rrdGraphHelper.getGraphTitleNameMappingForResourceId(resourceId);

                    for (Map.Entry<String, String> entry : titleNameMapping.entrySet()) {
                        String newGraphItemId = itemToExpandId + "." + entry.getKey();
/*
                        if (hierarchicalContainer.containsId(newGraphItemId)) {
                            continue;
                        }
*/
                        Item newGraphItem = hierarchicalContainer.addItem(newGraphItemId);

                        newGraphItem.getItemProperty("label").setValue(entry.getKey());
                        newGraphItem.getItemProperty("type").setValue("graph");
                        newGraphItem.getItemProperty("nodeId").setValue(String.valueOf(itemToExpand.getItemProperty("nodeId").getValue()));
                        newGraphItem.getItemProperty("nodeLabel").setValue(String.valueOf(itemToExpand.getItemProperty("nodeLabel").getValue()));
                        newGraphItem.getItemProperty("resourceId").setValue(String.valueOf(itemToExpand.getItemProperty("resourceId").getValue()));
                        newGraphItem.getItemProperty("resourceLabel").setValue(String.valueOf(itemToExpand.getItemProperty("resourceLabel").getValue()));
                        newGraphItem.getItemProperty("resourceTypeId").setValue(String.valueOf(itemToExpand.getItemProperty("resourceTypeId").getValue()));
                        newGraphItem.getItemProperty("resourceTypeLabel").setValue(String.valueOf(itemToExpand.getItemProperty("resourceTypeLabel").getValue()));
                        newGraphItem.getItemProperty("graphId").setValue(newGraphItemId);
                        newGraphItem.getItemProperty("graphLabel").setValue(entry.getKey());
                        newGraphItem.getItemProperty("graphUrl").setValue(map.get(entry.getValue()));

                        m_tree.setParent(newGraphItemId, itemToExpandId);
                        m_tree.setChildrenAllowed(newGraphItemId, false);
                    }
                }
            }
        });

        /**
         * adding button to a horizontal layout
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("100%");

        final Button cancel = new Button("Cancel");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);

        buttonLayout.addComponent(cancel);
        buttonLayout.setExpandRatio(cancel, 1);
        buttonLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        final Button ok = new Button("Select");

        ok.setEnabled(false);

        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (m_tree.getValue() != null) {
                    /**
                     * saving the data
                     */
                    Item selectedItem = m_tree.getItem(m_tree.getValue());

                    rrdGraphEntry.setGraphId(String.valueOf(selectedItem.getItemProperty("graphId").getValue()));
                    rrdGraphEntry.setResourceTypeId(String.valueOf(selectedItem.getItemProperty("resourceTypeId").getValue()));
                    rrdGraphEntry.setResourceId(String.valueOf(selectedItem.getItemProperty("resourceId").getValue()));
                    rrdGraphEntry.setNodeId(String.valueOf(selectedItem.getItemProperty("nodeId").getValue()));

                    rrdGraphEntry.setGraphLabel(String.valueOf(selectedItem.getItemProperty("graphLabel").getValue()));
                    rrdGraphEntry.setResourceTypeLabel(String.valueOf(selectedItem.getItemProperty("resourceTypeLabel").getValue()));
                    rrdGraphEntry.setResourceLabel(String.valueOf(selectedItem.getItemProperty("resourceLabel").getValue()));
                    rrdGraphEntry.setNodeLabel(String.valueOf(selectedItem.getItemProperty("nodeLabel").getValue()));

                    rrdGraphEntry.setGraphUrl(String.valueOf(selectedItem.getItemProperty("graphUrl").getValue()));

                    rrdGraphEntry.update();
                }
                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);

        buttonLayout.addComponent(ok);

        /**
         * if data is available expand the required nodes
         */
        if (rrdGraphEntry.getNodeId() != null) {
            m_tree.expandItem(rrdGraphEntry.getNodeId());

            if (rrdGraphEntry.getResourceTypeId() != null) {
                m_tree.expandItem(rrdGraphEntry.getResourceTypeId());

                if (rrdGraphEntry.getResourceId() != null) {
                    m_tree.expandItem(rrdGraphEntry.getResourceId());

                    /**
                     * and select the specified entry
                     */
                    if (rrdGraphEntry.getGraphId() != null) {
                        m_tree.select(rrdGraphEntry.getGraphId());
                    }
                }
            }
        }

        /**
         * adding a value change listener that checks if leaf node is selected
         */
        m_tree.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (valueChangeEvent.getProperty().getValue() != null) {
                    Item selectedItem = m_tree.getItem(valueChangeEvent.getProperty().getValue());
                    Object object = selectedItem.getItemProperty("graphId").getValue();

                    ok.setEnabled(object != null);
                }
            }
        });

        /**
         * creating the layout and setting the content
         */
        panel.setContent(m_tree);
        panel.setCaption("Graph");
        panel.setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setMargin(true);
        verticalLayout.addComponent(panel);
        verticalLayout.setExpandRatio(panel, 1.0f);
        verticalLayout.addComponent(buttonLayout);

        setContent(verticalLayout);
    }
}
