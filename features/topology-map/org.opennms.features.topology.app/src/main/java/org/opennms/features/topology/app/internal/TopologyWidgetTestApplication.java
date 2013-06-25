/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HasExtraComponents;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.MapViewManagerListener;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;
import org.opennms.features.topology.app.internal.TopologyComponent.VertexUpdateListener;
import org.opennms.features.topology.app.internal.jung.FRLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;


public class TopologyWidgetTestApplication extends Application implements CommandUpdateListener, MenuItemUpdateListener, ContextMenuHandler, WidgetUpdateListener, WidgetContext, FragmentChangedListener, GraphContainer.ChangeListener, MapViewManagerListener, VertexUpdateListener, SelectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyWidgetTestApplication.class);
	private static final long serialVersionUID = 6837501987137310938L;
	private static int HEADER_HEIGHT = 100;
	private static final int MENU_BAR_HEIGHT = 23;

	private static final String LABEL_PROPERTY = "label";
	private Window m_window;
	private TopologyComponent m_topologyComponent;
	private VertexSelectionTree m_tree;
	private final GraphContainer m_graphContainer;
	private final CommandManager m_commandManager;
	private MenuBar m_menuBar;
	private TopoContextMenu m_contextMenu;
	private AbsoluteLayout m_layout;
	private AbsoluteLayout m_rootLayout;
	private final IconRepositoryManager m_iconRepositoryManager;
	private WidgetManager m_widgetManager;
	private WidgetManager m_treeWidgetManager;
	private Accordion m_treeAccordion;
    private HorizontalSplitPanel m_treeMapSplitPanel;
    private final Label m_zoomLevelLabel = new Label("0"); 
    private UriFragmentUtility m_uriFragUtil;
    private final HistoryManager m_historyManager;
    private String m_headerHtml;
    private boolean m_showHeader = true;

    public TopologyWidgetTestApplication(CommandManager commandManager, HistoryManager historyManager, GraphContainer graphContainer, IconRepositoryManager iconRepoManager, SelectionManager selectionManager) {
        // Ensure that selection changes trigger a history save operation
        selectionManager.addSelectionListener(this);

        m_commandManager = commandManager;
        m_commandManager.addMenuItemUpdateListener(this);
        m_historyManager = historyManager;
        m_iconRepositoryManager = iconRepoManager;

        // Create a per-session GraphContainer instance
        m_graphContainer = graphContainer;
        m_graphContainer.setSelectionManager(selectionManager);
        m_graphContainer.addChangeListener(this);
        m_graphContainer.getMapViewManager().addListener(this);
        m_graphContainer.setUserName((String)this.getUser());
    }

	@SuppressWarnings("serial")
	@Override
	public void init() {
	    setTheme("topo_default");
	    HttpSession session = ((WebApplicationContext) this.getContext()).getHttpSession();
        m_graphContainer.setSessionId(session.getId());
	    
		// See if the history manager has an existing fragment stored for
		// this user. Do this before laying out the UI because the history
	    // may change during layout.
		String fragment = m_historyManager.getHistoryForUser((String)this.getUser());

	    m_rootLayout = new AbsoluteLayout();
	    m_rootLayout.setSizeFull();
	    
	    m_window = new Window("OpenNMS Topology");
        m_window.setContent(m_rootLayout);
        setMainWindow(m_window);


        
        m_uriFragUtil = new UriFragmentUtility();
        m_window.addComponent(m_uriFragUtil);
        m_uriFragUtil.addListener(this);
        
		m_layout = new AbsoluteLayout();
		m_layout.setSizeFull();
		m_rootLayout.addComponent(m_layout);
		
		if(m_showHeader) {
		    HEADER_HEIGHT = 100;
            m_rootLayout.addComponent(createHeader(), "top: 0px; left: 0px; right:0px;");
		} else {
		    HEADER_HEIGHT = 0;
		}
        
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(5000);
		getMainWindow().addComponent(refresher);

		m_graphContainer.setLayoutAlgorithm(new FRLayoutAlgorithm());

		m_topologyComponent = new TopologyComponent(m_graphContainer, m_iconRepositoryManager, this);
		m_topologyComponent.setSizeFull();
		m_topologyComponent.addMenuItemStateListener(this);
		m_topologyComponent.addVertexUpdateListener(this);
		
		final Property scale = m_graphContainer.getScaleProperty();
		final Slider slider = new Slider(0, 1);
		slider.setPropertyDataSource(scale);
		slider.setResolution(1);
		slider.setHeight("300px");
		slider.setOrientation(Slider.ORIENTATION_VERTICAL);
		slider.setImmediate(true);

		final Button zoomInBtn = new Button();
		zoomInBtn.setIcon(new ThemeResource("images/plus.png"));
		zoomInBtn.setDescription("Expand Semantic Zoom Level");
		zoomInBtn.setStyleName("semantic-zoom-button");
		zoomInBtn.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
				int szl = (Integer) m_graphContainer.getSemanticZoomLevel();
				szl++;
				m_graphContainer.setSemanticZoomLevel(szl);
				setSemanticZoomLevel(szl);
				saveHistory();
			}
		});

		Button zoomOutBtn = new Button();
		zoomOutBtn.setIcon(new ThemeResource("images/minus.png"));
		zoomOutBtn.setDescription("Collapse Semantic Zoom Level");
		zoomOutBtn.setStyleName("semantic-zoom-button");
		zoomOutBtn.addListener(new ClickListener() {

                        @Override
			public void buttonClick(ClickEvent event) {
				int szl = (Integer) m_graphContainer.getSemanticZoomLevel();
				if(szl > 0) {
				    szl--;
				    m_graphContainer.setSemanticZoomLevel(szl);
				    setSemanticZoomLevel(szl);
				    saveHistory();
				} 
				
			}
		});
		
		
		final Button panBtn = new Button();
		panBtn.setIcon(new ThemeResource("images/cursor_drag_arrow.png"));
		panBtn.setDescription("Pan Tool");
		panBtn.setStyleName("toolbar-button down");
		
		final Button selectBtn = new Button();
		selectBtn.setIcon(new ThemeResource("images/selection.png"));
		selectBtn.setDescription("Selection Tool");
		selectBtn.setStyleName("toolbar-button");
		selectBtn.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                selectBtn.setStyleName("toolbar-button down");
                panBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("select");
            }
        });
		
		panBtn.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                panBtn.setStyleName("toolbar-button down");
                selectBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("pan");
            }
        });
		
		VerticalLayout toolbar = new VerticalLayout();
		toolbar.setWidth("31px");
		toolbar.addComponent(panBtn);
		toolbar.addComponent(selectBtn);
		
		HorizontalLayout semanticLayout = new HorizontalLayout();
		semanticLayout.addComponent(zoomInBtn);
		semanticLayout.addComponent(m_zoomLevelLabel);
		semanticLayout.addComponent(zoomOutBtn);
		semanticLayout.setComponentAlignment(m_zoomLevelLabel, Alignment.MIDDLE_CENTER);
		
		AbsoluteLayout mapLayout = new AbsoluteLayout();

		mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
		mapLayout.addComponent(slider, "top: 5px; left: 20px; z-index:1000;");
		mapLayout.addComponent(toolbar, "top: 324px; left: 12px;");
		mapLayout.addComponent(semanticLayout, "top: 380px; left: 2px;");
		mapLayout.setSizeFull();

		m_treeMapSplitPanel = new HorizontalSplitPanel();
		m_treeMapSplitPanel.setFirstComponent(createWestLayout());
		m_treeMapSplitPanel.setSecondComponent(mapLayout);
		m_treeMapSplitPanel.setSplitPosition(222, Sizeable.UNITS_PIXELS);
		m_treeMapSplitPanel.setSizeFull();

		m_commandManager.addCommandUpdateListener(this);

		menuBarUpdated(m_commandManager);
		if(m_widgetManager.widgetCount() != 0) {
		    updateWidgetView(m_widgetManager);
		} else {
		    m_layout.addComponent(m_treeMapSplitPanel, getBelowMenuPosition());
		}
		
		if(m_treeWidgetManager.widgetCount() != 0) {
		    updateAccordionView(m_treeWidgetManager);
		}

		// If there was existing history, then restore that history snapshot.
		if (fragment != null) {
			LOG.info("Restoring history for user {}: {}", (String)this.getUser(), fragment);
			m_uriFragUtil.setFragment(fragment);
		}
	}

    private Panel createHeader() {
        final Panel header = new Panel("header");
        header.setCaption(null);
        header.setSizeUndefined();
        header.addStyleName("onmsheader");
        try {
            CustomLayout customLayout = new CustomLayout(getHeaderLayout());
            header.setContent(customLayout);
        } catch (IOException e) {
            LOG.error("Could not load header file", e);
        }
        return header;
    }


    /**
	 * Update the Accordion View with installed widgets
	 * @param treeWidgetManager
	 */
    private void updateAccordionView(WidgetManager treeWidgetManager) {
        m_treeAccordion.removeAllComponents();
        
        m_treeAccordion.addTab(m_tree, m_tree.getTitle());
        for(IViewContribution widget : treeWidgetManager.getWidgets()) {
            if(widget.getIcon() != null) {
                m_treeAccordion.addTab(widget.getView(this), widget.getTitle(), widget.getIcon());
            }else {
                m_treeAccordion.addTab(widget.getView(this), widget.getTitle());
            }
        }
    }

    /**
     * Updates the bottom widget area with the registered widgets
     * 
     * Any widget with the service property of 'location=bottom' are
     * included.
     * 
     * @param widgetManager
     */
    private void updateWidgetView(WidgetManager widgetManager) {
        synchronized (m_layout) {
            m_layout.removeAllComponents();
            if(widgetManager.widgetCount() == 0) {
                m_layout.addComponent(m_treeMapSplitPanel, getBelowMenuPosition());
            } else {
                VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
                bottomLayoutBar.setFirstComponent(m_treeMapSplitPanel);
                // Split the screen 70% top, 30% bottom
                bottomLayoutBar.setSplitPosition(70, Sizeable.UNITS_PERCENTAGE);
                bottomLayoutBar.setSizeFull();

                // Add the tabsheet to the layout
                bottomLayoutBar.addComponent(getTabSheet(widgetManager, this));

                m_layout.addComponent(bottomLayoutBar, getBelowMenuPosition());
            }
            m_layout.requestRepaint();
        }
        if(m_contextMenu != null && m_contextMenu.getParent() == null) {
            getMainWindow().addComponent(m_contextMenu);
        }
    }


    private String getBelowMenuPosition() {
        return "top: " + (HEADER_HEIGHT + MENU_BAR_HEIGHT) + "px; left: 0px; right:0px; bottom:0px;";
    }

    /**
     * Gets a {@link TabSheet} view for all widgets in this manager.
     * 
     * @return TabSheet
     */
    private Component getTabSheet(WidgetManager manager, WidgetContext widgetContext) {
        // Use an absolute layout for the bottom panel
        AbsoluteLayout bottomLayout = new AbsoluteLayout();
        bottomLayout.setSizeFull();
        
        final TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        for(IViewContribution viewContrib : manager.getWidgets()) {
            // Create a new view instance
            final Component view = viewContrib.getView(widgetContext);
            try {
                m_graphContainer.getSelectionManager().addSelectionListener((SelectionListener)view);
            } catch (ClassCastException e) {}
            try {
                ((SelectionNotifier)view).addSelectionListener(m_graphContainer.getSelectionManager());
            } catch (ClassCastException e) {}

            // Icon can be null
            tabSheet.addTab(view, viewContrib.getTitle(), viewContrib.getIcon());

            // If the component supports the HasExtraComponents interface, then add the extra 
            // components to the tab bar
            try {
                Component[] extras = ((HasExtraComponents)view).getExtraComponents();
                if (extras != null && extras.length > 0) {
                    // For any extra controls, add a horizontal layout that will float
                    // on top of the right side of the tab panel
                    final HorizontalLayout extraControls = new HorizontalLayout();
                    extraControls.setHeight(32, Sizeable.UNITS_PIXELS);
                    extraControls.setSpacing(true);

                    // Add the extra controls to the layout
                    for (Component component : extras) {
                        extraControls.addComponent(component);
                        extraControls.setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                    }

                    // Add a TabSheet.SelectedTabChangeListener to show or hide the extra controls
                    tabSheet.addListener(new SelectedTabChangeListener() {
                        private static final long serialVersionUID = 6370347645872323830L;

                        @Override
                        public void selectedTabChange(SelectedTabChangeEvent event) {
                            final TabSheet source = (TabSheet) event.getSource();
                            if (source == tabSheet) {
                                // Bizarrely enough, getSelectedTab() returns the contained
                                // Component, not the Tab itself.
                                //
                                // If the first tab was selected...
                                if (source.getSelectedTab() == view) {
                                    extraControls.setVisible(true);
                                } else {
                                    extraControls.setVisible(false);
                                }
                            }
                        }
                    });

                    // Place the extra controls on the absolute layout
                    bottomLayout.addComponent(extraControls, "top:0px;right:5px;z-index:100");
                }
            } catch (ClassCastException e) {}
            view.setSizeFull();
        }

        // Add the tabsheet to the layout
        bottomLayout.addComponent(tabSheet);

        return bottomLayout;
    }
    

    /**
     * Creates the west area layout including the
     * accordion and tree views.
     * 
     * @return
     */
    @SuppressWarnings("serial")
	private Layout createWestLayout() {
        m_tree = createTree();
        
        final TextField filterField = new TextField("Filter");
        filterField.setTextChangeTimeout(200);
        
        final Button filterBtn = new Button("Filter");
        filterBtn.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
            	GCFilterableContainer container = m_tree.getContainerDataSource();
                container.removeAllContainerFilters();
                
                String filterString = (String) filterField.getValue();
                if(!filterString.equals("") && filterBtn.getCaption().toLowerCase().equals("filter")) {
                    container.addContainerFilter(LABEL_PROPERTY, (String) filterField.getValue(), true, false);
                    filterBtn.setCaption("Clear");
                } else {
                    filterField.setValue("");
                    filterBtn.setCaption("Filter");
                }
                
            }
        });
        
        HorizontalLayout filterArea = new HorizontalLayout();
        filterArea.addComponent(filterField);
        filterArea.addComponent(filterBtn);
        filterArea.setComponentAlignment(filterBtn, Alignment.BOTTOM_CENTER);
        
        m_treeAccordion = new Accordion();
        m_treeAccordion.addTab(m_tree, m_tree.getTitle());
        m_treeAccordion.setWidth("100%");
        m_treeAccordion.setHeight("100%");
        
        AbsoluteLayout absLayout = new AbsoluteLayout();
        absLayout.setWidth("100%");
        absLayout.setHeight("100%");
        absLayout.addComponent(filterArea, "top: 25px; left: 15px;");
        absLayout.addComponent(m_treeAccordion, "top: 75px; left: 15px; right: 15px; bottom:25px;"); 
        
        return absLayout;
    }

    private VertexSelectionTree createTree() {
		VertexSelectionTree tree = new VertexSelectionTree("Nodes", m_graphContainer);
		tree.setMultiSelect(true);
		tree.setImmediate(true);
		tree.setItemCaptionPropertyId(LABEL_PROPERTY);

		for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
			Object item = it.next();
			tree.expandItemsRecursively(item);
		}
		
		m_graphContainer.getSelectionManager().addSelectionListener(tree);

		return tree;
	}

	@Override
	public void updateMenuItems() {
		updateMenuItems(m_menuBar.getItems());
		m_menuBar.requestRepaint();
	}

	private void updateContextMenuItems(Object target, List<TopoContextMenuItem> items) {
		for(TopoContextMenuItem contextItem : items) {
			if(contextItem.hasChildren()) {
				updateContextMenuItems(target, contextItem.getChildren());
			} else {
				m_commandManager.updateContextMenuItem(target, contextItem, m_graphContainer, getMainWindow());
			}
		}
	}


	private void updateMenuItems(List<MenuItem> menuItems) {
		for(MenuItem menuItem : menuItems) {
			if(menuItem.hasChildren()) {
				updateMenuItems(menuItem.getChildren());
			}else {
				m_commandManager.updateMenuItem(menuItem, m_graphContainer, getMainWindow());
			}
		}
	}

	@Override
	public void menuBarUpdated(CommandManager commandManager) {
		if(m_menuBar != null) {
			m_rootLayout.removeComponent(m_menuBar);
		}

		if(m_contextMenu != null) {
			getMainWindow().removeComponent(m_contextMenu);
		}

		m_menuBar = commandManager.getMenuBar(m_graphContainer, getMainWindow());
		m_menuBar.setWidth("100%");
		m_rootLayout.addComponent(m_menuBar, "top: " + HEADER_HEIGHT +"px; left: 0px; right:0px;");

		m_contextMenu = commandManager.getContextMenu(m_graphContainer, getMainWindow());
		getMainWindow().addComponent(m_contextMenu);
		updateMenuItems();
	}
	
        @Override
	public void show(Object target, int left, int top) {
		updateContextMenuItems(target, m_contextMenu.getItems());
		updateSubMenuDisplay(m_contextMenu.getItems());
		m_contextMenu.setTarget(target);
		m_contextMenu.show(left, top);
	}


	private void updateSubMenuDisplay(List<TopoContextMenuItem> items) {
		for (TopoContextMenuItem item : items) {
			if (!item.hasChildren()) continue;
			else updateSubMenuDisplay(item.getChildren());
			boolean shouldDisplay = false;
			for (TopoContextMenuItem child : item.getChildren()) {
				if (child.getItem().isVisible()) {
					shouldDisplay = true;
					break;
				}
			}
			item.getItem().setVisible(shouldDisplay);
		}
	}


    public WidgetManager getWidgetManager() {
        return m_widgetManager;
    }


    public void setWidgetManager(WidgetManager widgetManager) {
        if(m_widgetManager != null) {
            m_widgetManager.removeUpdateListener(this);
        }
        m_widgetManager = widgetManager;
        m_widgetManager.addUpdateListener(this);
    }


    @Override
    public void widgetListUpdated(WidgetManager widgetManager) {
        if(isRunning()) {
            if(widgetManager == m_widgetManager) {
                updateWidgetView(widgetManager);
            }else if(widgetManager == m_treeWidgetManager) {
                updateAccordionView(widgetManager);
            }
        }
    }


    public WidgetManager getTreeWidgetManager() {
        return m_treeWidgetManager;
    }


    public void setTreeWidgetManager(WidgetManager treeWidgetManager) {
        if(m_treeWidgetManager != null) {
            m_treeWidgetManager.removeUpdateListener(this);
        }
        
        m_treeWidgetManager = treeWidgetManager;
        m_treeWidgetManager.addUpdateListener(this);
    }


    @Override
    public GraphContainer getGraphContainer() {
        return m_graphContainer;
    }


    int m_settingFragment = 0;
    
    @Override
    public void fragmentChanged(FragmentChangedEvent source) {
        m_settingFragment++;
        String fragment = source.getUriFragmentUtility().getFragment();
        m_historyManager.applyHistory((String)getUser(), fragment, m_graphContainer);
        m_settingFragment--;
    }


    private void saveHistory() {
        if (m_settingFragment == 0) {
            String fragment = m_historyManager.create((String)getUser(), m_graphContainer);
            m_uriFragUtil.setFragment(fragment, false);
        }
    }


    @Override
    public void graphChanged(GraphContainer graphContainer) {
        m_zoomLevelLabel.setValue("" + graphContainer.getSemanticZoomLevel());
    }


    private void setSemanticZoomLevel(int szl) {
        m_zoomLevelLabel.setValue(szl);
        m_graphContainer.redoLayout();
    }


    @Override
    public void boundingBoxChanged(MapViewManager viewManager) {
        saveHistory();
    }


    @Override
    public void onVertexUpdate() {
        saveHistory();
    }
    
    private InputStream getHeaderLayout() {
        return new ByteArrayInputStream(m_headerHtml.getBytes());
    }


    public void setHeaderHtml(String headerHtml) {
        m_headerHtml = headerHtml;
    }
    
    /**
     * Parameter is a String because config has String values
     * @param boolVal
     */
    public void setShowHeader(String boolVal) {
        m_showHeader = "true".equals(boolVal);
    }

    @Override
    public void selectionChanged(SelectionContext selectionManager) {
        saveHistory();
    }
    
}
