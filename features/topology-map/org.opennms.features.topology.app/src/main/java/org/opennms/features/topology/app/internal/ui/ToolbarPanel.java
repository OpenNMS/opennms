/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.ui;

import static org.opennms.features.topology.app.internal.TopologyUI.TopologyUIRequestHandler.PARAMETER_HISTORY_FRAGMENT;
import static org.opennms.features.topology.app.internal.ui.ToolbarPanelController.ActiveTool;

import org.opennms.features.topology.api.Callbacks;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.GraphContainer.ChangeListener;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.app.internal.ManualLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.IonicIcons;
import org.opennms.features.topology.app.internal.support.LayoutManager;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Panel for the Toolbar in the Topology Map.
 * 
 * @author mvrueden
 */
public class ToolbarPanel extends CssLayout implements SelectionListener, ChangeListener {

    private static class Styles {
        private static final String SELECTED = "selected";
        private static final String TOOLBAR = "toolbar";
        private static final String EXPANDED = "expanded";
        private static final String LAYOUT = "layout";
    }
    
    private final Button m_panBtn;
    private final Button m_selectBtn;
    private final Button m_szlOutBtn;
    private final Label m_zoomLevelLabel = new Label();
    /**
     * The Button to toggle the visibility of the {@link #layerLayout}.
     */
    private final Button layerButton;

    /**
     * The Button to save the current vertex positions.
     */
    private final Button layerSaveButton;

    private final LayoutManager layoutManager;

    /**
     * The Layout which shows the available layers.
     */
    private final VerticalLayout layerLayout;

    public ToolbarPanel(final ToolbarPanelController controller) {
        addStyleName(Styles.TOOLBAR);
        this.layoutManager = controller.getLayoutManager();

        final Property<Double> scale = controller.getScaleProperty();
        final Boolean[] eyeClosed = new Boolean[] {false};
        final Button showFocusVerticesBtn = new Button();
        showFocusVerticesBtn.setIcon(FontAwesome.EYE);
        showFocusVerticesBtn.setDescription("Toggle Highlight Focus Nodes");
        showFocusVerticesBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(eyeClosed[0]) {
                    showFocusVerticesBtn.setIcon(FontAwesome.EYE);
                } else {
                    showFocusVerticesBtn.setIcon(FontAwesome.EYE_SLASH);
                }
                eyeClosed[0] = !eyeClosed[0]; // toggle
                controller.toggleHighlightFocus();
            }
        });

        final Button magnifyBtn = new Button();
        magnifyBtn.setIcon(FontAwesome.PLUS);
        magnifyBtn.setDescription("Magnify");
        magnifyBtn.addClickListener((Button.ClickListener) event -> scale.setValue(Math.min(1, scale.getValue() + 0.25)));

        final Button demagnifyBtn = new Button();
        demagnifyBtn.setIcon(FontAwesome.MINUS);
        demagnifyBtn.setDescription("Demagnify");
        demagnifyBtn.addClickListener((Button.ClickListener) event -> scale.setValue(Math.max(0, scale.getValue() - 0.25)));

        m_szlOutBtn = new Button();
        m_szlOutBtn.setId("szlOutBtn");
        m_szlOutBtn.setIcon(FontAwesome.ANGLE_DOWN);
        m_szlOutBtn.setDescription("Decrease Semantic Zoom Level");
        m_szlOutBtn.setEnabled(controller.getSemanticZoomLevel() > 0);
        m_szlOutBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                int szl = controller.getSemanticZoomLevel();
                if (szl > 0) {
                    setSemanticZoomLevel(controller, szl - 1);
                    controller.saveHistory();
                }
            }
        });

        final Button szlInBtn = new Button();
        szlInBtn.setId("szlInBtn");
        szlInBtn.setIcon(FontAwesome.ANGLE_UP);
        szlInBtn.setDescription("Increase Semantic Zoom Level");
        szlInBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setSemanticZoomLevel(controller, controller.getSemanticZoomLevel() + 1);
                controller.saveHistory();
            }
        });

        m_zoomLevelLabel.setId("szlInputLabel");

        m_panBtn = new Button();
        m_panBtn.setIcon(FontAwesome.ARROWS);
        m_panBtn.setDescription("Pan Tool");
        m_panBtn.addStyleName(Styles.SELECTED);

        m_selectBtn = new Button();
        m_selectBtn.setIcon(IonicIcons.ANDROID_EXPAND);
        m_selectBtn.setDescription("Selection Tool");
        m_selectBtn.setStyleName("toolbar-button");
        m_selectBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                m_selectBtn.addStyleName(Styles.SELECTED);
                m_panBtn.removeStyleName(Styles.SELECTED);
                controller.setActiveTool(ActiveTool.select);
            }
        });

        m_panBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                m_panBtn.addStyleName(Styles.SELECTED);
                m_selectBtn.removeStyleName(Styles.SELECTED);
                controller.setActiveTool(ActiveTool.pan);
            }
        });

        Button showAllMapBtn = new Button();
        showAllMapBtn.setId("showEntireMapBtn");
        showAllMapBtn.setIcon(FontAwesome.GLOBE);
        showAllMapBtn.setDescription("Show Entire Map");
        showAllMapBtn.addClickListener((Button.ClickListener) event -> controller.showAllMap());

        Button centerSelectionBtn = new Button();
        centerSelectionBtn.setIcon(FontAwesome.LOCATION_ARROW);
        centerSelectionBtn.setDescription("Center On Selection");
        centerSelectionBtn.addClickListener((Button.ClickListener) event -> controller.centerMapOnSelection());

        Button shareButton = new Button("", FontAwesome.SHARE_SQUARE_O);
        shareButton.setDescription("Share");
        shareButton.addClickListener((x) -> {
            // Create the share link
            String fragment = getUI().getPage().getLocation().getFragment();
            String url = getUI().getPage().getLocation().toString().replace("#" + fragment, "");
            String shareLink = String.format("%s?%s=%s", url, PARAMETER_HISTORY_FRAGMENT, fragment);

            // Create the Window
            Window shareWindow = new Window();
            shareWindow.setCaption("Share Link");
            shareWindow.setModal(true);
            shareWindow.setClosable(true);
            shareWindow.setResizable(false);
            shareWindow.setWidth(400, Sizeable.Unit.PIXELS);

            TextArea shareLinkField = new TextArea();
            shareLinkField.setValue(shareLink);
            shareLinkField.setReadOnly(true);
            shareLinkField.setRows(3);
            shareLinkField.setWidth(100, Sizeable.Unit.PERCENTAGE);

            // Close Button
            Button close = new Button("Close");
            close.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
            close.addClickListener(event -> shareWindow.close());

            // Layout for Buttons
            HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setMargin(true);
            buttonLayout.setSpacing(true);
            buttonLayout.setWidth("100%");
            buttonLayout.addComponent(close);
            buttonLayout.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

            // Content Layout
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setMargin(true);
            verticalLayout.setSpacing(true);
            verticalLayout.addComponent(new Label("Please use the following link to share the current view with others."));
            verticalLayout.addComponent(shareLinkField);
            verticalLayout.addComponent(buttonLayout);

            shareWindow.setContent(verticalLayout);

            getUI().addWindow(shareWindow);
        });

        // Refresh Button
        Button refreshButton = new Button();
        refreshButton.setId("refreshNow");
        refreshButton.setIcon(FontAwesome.REFRESH);
        refreshButton.setDescription("Refresh Now");
        refreshButton.addClickListener((event) -> controller.refreshUI());

        // Layer Layout
        layerLayout = new VerticalLayout();
        layerLayout.setId("layerComponent");
        layerLayout.setSpacing(true);
        layerLayout.setMargin(true);

        // Layer Button
        layerButton = new Button();
        layerButton.setId("layerToggleButton");
        layerButton.setIcon(FontAwesome.BARS);
        layerButton.setDescription("Layers");
        layerButton.addClickListener((event) -> {
            boolean isCollapsed = layerButton.getStyleName().contains(Styles.EXPANDED);
            setLayerLayoutVisible(!isCollapsed);
        });

        // Save button
        layerSaveButton = new Button();
        layerSaveButton.setId("saveLayerButton");
        layerSaveButton.setIcon(FontAwesome.FLOPPY_O);
        layerSaveButton.addClickListener((event) -> controller.saveLayout());

        // Actual Layout for the Toolbar
        CssLayout contentLayout = new CssLayout();
        contentLayout.addStyleName("toolbar-component");
        contentLayout.addComponent(createGroup(szlInBtn, m_zoomLevelLabel, m_szlOutBtn));
        contentLayout.addComponent(createGroup(refreshButton, centerSelectionBtn, showAllMapBtn, layerButton, showFocusVerticesBtn));
        contentLayout.addComponent(createGroup(m_panBtn, m_selectBtn));
        contentLayout.addComponent(createGroup(magnifyBtn, demagnifyBtn));
        contentLayout.addComponent(createGroup(shareButton));
        contentLayout.addComponent(createGroup(layerSaveButton));

        // Toolbar
        addComponent(contentLayout);
    }

    private void setSemanticZoomLevel(ToolbarPanelController controller, int semanticZoomLevel) {
        controller.setSemanticZoomLevel(semanticZoomLevel);
        setSemanticZoomLevelLabel(semanticZoomLevel);
    }

    private void setLayerLayoutVisible(boolean show) {
        if (show) {
            layerButton.addStyleName(Styles.EXPANDED);
            layerButton.addStyleName(Styles.SELECTED);
            addComponent(layerLayout);
        } else {
            layerButton.removeStyleName(Styles.EXPANDED);
            layerButton.removeStyleName(Styles.SELECTED);
            removeComponent(layerLayout);
        }
    }

    public void setSemanticZoomLevelLabel(int semanticZoomLevel) {
        m_zoomLevelLabel.setValue(String.valueOf(semanticZoomLevel));
        m_szlOutBtn.setEnabled(semanticZoomLevel > 0);
    }

    private CssLayout createGroup(Component... components) {
        CssLayout group = new CssLayout();
        group.addStyleName("toolbar-component-group");
        group.setSizeFull();
        for (Component eachComponent : components) {
            eachComponent.setPrimaryStyleName("toolbar-group-item");
            group.addComponent(eachComponent);
        }
        return group;
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        // After selection always set the pantool back to active tool
        if(m_panBtn != null && !m_panBtn.getStyleName().contains(Styles.SELECTED)) {
            m_panBtn.addStyleName(Styles.SELECTED);
        }
        if(m_selectBtn != null && m_selectBtn.getStyleName().contains(Styles.SELECTED)) {
            m_selectBtn.removeStyleName(Styles.SELECTED);
        }
    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        setSemanticZoomLevelLabel(graphContainer.getSemanticZoomLevel());
        handleSaveButton(graphContainer);
        handleLayerButton(graphContainer);
    }

    private void handleLayerButton(GraphContainer graphContainer) {
        // Toggle layer button
        boolean enableLayerButton = graphContainer.getTopologyServiceClient().getGraphProviders().size() > 1;
        layerButton.setEnabled(enableLayerButton);

        // update the layer layout
        layerLayout.removeAllComponents();
        if (enableLayerButton) {
            graphContainer.getTopologyServiceClient().getGraphProviders().forEach(topologyProvider -> {
                boolean selected = topologyProvider.getNamespace().equals(graphContainer.getTopologyServiceClient().getNamespace());
                final TopologyProviderInfo topologyProviderInfo = topologyProvider.getTopologyProviderInfo();

                final Label nameLabel = new Label(topologyProviderInfo.getName());
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.addComponent(nameLabel);
                verticalLayout.addStyleName(Styles.LAYOUT);
                if (selected) {
                    verticalLayout.addStyleName(Styles.SELECTED);
                }
                verticalLayout.addLayoutClickListener((event) -> graphContainer.selectTopologyProvider(topologyProvider, Callbacks.applyDefaults()));
                layerLayout.addComponent(verticalLayout);
            });
        } else {
            setLayerLayoutVisible(false);
        }
    }

    private void handleSaveButton(GraphContainer graphContainer) {
        // Toggle save button for coordinates
        if (graphContainer.getLayoutAlgorithm() instanceof ManualLayoutAlgorithm) {
            // We only show the save button if we don't have a layout persisted, or the layout is not equal
            boolean showSave = layoutManager.loadLayout(graphContainer.getGraph()) == null
                    || !layoutManager.isPersistedLayoutEqualToCurrentLayout(graphContainer.getGraph());
            layerSaveButton.setEnabled(showSave);
            if (showSave) {
                layerSaveButton.setDescription("Save the current layout");
            } else {
                layerSaveButton.setDescription("Nothing to save");
            }
        } else {
            layerSaveButton.setEnabled(false);
            layerSaveButton.setDescription("Change to Manual Layout to enable saving");
        }
    }
}
