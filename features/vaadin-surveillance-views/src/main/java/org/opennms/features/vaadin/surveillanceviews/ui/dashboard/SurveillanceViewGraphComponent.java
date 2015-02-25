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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurveillanceViewGraphComponent extends VerticalLayout implements SurveillanceViewDetail {
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewGraphComponent.class);

    private SurveillanceViewService m_surveillanceViewService;
    protected boolean m_enabled;
    private NativeSelect m_nodeSelect, m_resourceSelect, m_graphSelect;
    private HorizontalLayout m_imageLayout;

    public SurveillanceViewGraphComponent(SurveillanceViewService surveillanceViewService, boolean enabled) {

        m_surveillanceViewService = surveillanceViewService;
        m_enabled = enabled;

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setSpacing(false);
        horizontalLayout.setPrimaryStyleName("v-caption-surveillance-view");
        horizontalLayout.addComponent(new Label("Resource Graphs"));
        addComponent(horizontalLayout);

        m_nodeSelect = new NativeSelect();
        m_nodeSelect.setNullSelectionAllowed(false);

        m_nodeSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                OnmsNode onmsNode = (OnmsNode) m_nodeSelect.getValue();

                m_resourceSelect.removeAllItems();

                if (onmsNode != null) {
                    Map<OnmsResourceType, List<OnmsResource>> map = getSurveillanceViewService().getResourceTypeMapForNodeId(onmsNode.getId());

                    for (OnmsResourceType onmsResourceType : map.keySet()) {
                        for (OnmsResource onmsResource : map.get(onmsResourceType)) {
                            m_resourceSelect.addItem(onmsResource);
                            m_resourceSelect.setItemCaption(onmsResource, onmsResourceType.getLabel() + ": " + onmsResource.getLabel());
                        }
                    }

                    Iterator<?> i = m_resourceSelect.getItemIds().iterator();

                    if (i.hasNext()) {
                        m_resourceSelect.select(i.next());
                    }
                }
            }
        });

        m_resourceSelect = new NativeSelect();
        m_resourceSelect.setNullSelectionAllowed(false);

        m_resourceSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                OnmsResource onmsResource = (OnmsResource) m_resourceSelect.getValue();

                m_graphSelect.removeAllItems();

                if (onmsResource != null) {
                    Map<String, String> map = getSurveillanceViewService().getGraphResultsForResourceId(onmsResource.getId());

                    for (String string : map.keySet()) {
                        m_graphSelect.addItem(map.get(string));
                        m_graphSelect.setItemCaption(map.get(string), string);
                    }

                    Iterator<?> i = m_graphSelect.getItemIds().iterator();

                    if (i.hasNext()) {
                        m_graphSelect.select(i.next());
                    }
                }
            }
        });

        m_graphSelect = new NativeSelect();
        m_graphSelect.setNullSelectionAllowed(false);

        m_graphSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                String string = (String) m_graphSelect.getValue();

                if (string != null) {
                    replaceImage(getSurveillanceViewService().imageUrlForGraph(string, 1000, 200));
                } else {
                    replaceImage(null);
                }
            }
        });

        m_nodeSelect.setSizeFull();
        m_resourceSelect.setSizeFull();
        m_graphSelect.setSizeFull();

        m_imageLayout = new HorizontalLayout();
        m_imageLayout.setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(m_nodeSelect);
        verticalLayout.addComponent(m_resourceSelect);
        verticalLayout.addComponent(m_graphSelect);

        addComponent(verticalLayout);
        addComponent(m_imageLayout);
    }

    private void replaceImage(String url) {
        m_imageLayout.removeAllComponents();

        if (url != null) {
            Image image = new Image(null, new ExternalResource(url));
            image.setWidth(100, Unit.PERCENTAGE);

            m_imageLayout.addComponent(image);
        }
    }

    protected SurveillanceViewService getSurveillanceViewService() {
        return m_surveillanceViewService;
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        List<OnmsNode> nodes = getSurveillanceViewService().getNodesForCategories(rowCategories, colCategories);

        OnmsNode selectedNode = (OnmsNode) m_nodeSelect.getValue();
        OnmsResource selectedResource = (OnmsResource) m_resourceSelect.getValue();
        String selectedGraph = (String) m_graphSelect.getValue();

        LOG.debug("Saved selection={} / {} / {}", selectedNode == null ? "null" : selectedNode.getLabel(), selectedResource == null ? "null" : selectedResource.getLabel(), selectedGraph);

        m_nodeSelect.removeAllItems();

        if (nodes != null && !nodes.isEmpty()) {
            for (OnmsNode node : nodes) {
                m_nodeSelect.addItem(node);
                m_nodeSelect.setItemCaption(node, "Node: " + node.getLabel());
            }
        }

        if (selectedNode != null) {
            for (OnmsNode onmsNode : (Collection<OnmsNode>) m_nodeSelect.getItemIds()) {
                if (onmsNode.getId().equals(selectedNode.getId())) {
                    m_nodeSelect.select(onmsNode);
                    if (selectedResource != null) {
                        for (OnmsResource onmsResource : (Collection<OnmsResource>) m_resourceSelect.getItemIds()) {
                            if (onmsResource.getId().equals(selectedResource.getId())) {
                                m_resourceSelect.select(onmsResource);
                                if (selectedGraph != null) {
                                    m_graphSelect.select(selectedGraph);
                                }
                            }
                        }
                    }
                    return;
                }
            }
        }

        Iterator<?> i = m_nodeSelect.getItemIds().iterator();

        if (i.hasNext()) {
            m_nodeSelect.select(i.next());
        }
    }
}
