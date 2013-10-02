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

import com.github.wolfie.refresher.Refresher;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;
import org.opennms.features.topology.app.internal.TopologyComponent.VertexUpdateListener;
import org.opennms.features.topology.app.internal.jung.FRLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.opennms.osgi.*;
import org.opennms.osgi.locator.OnmsServiceManagerLocator;
import org.opennms.web.api.OnmsHeaderProvider;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SuppressWarnings("serial")
@Theme("topo_default")
@JavaScript({
	"http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js",
	"chromeFrameCheck.js"
})
@PreserveOnRefresh
public class TopologyUI extends UI implements CommandUpdateListener, MenuItemUpdateListener, ContextMenuHandler, WidgetUpdateListener, WidgetContext, UriFragmentChangedListener, GraphContainer.ChangeListener, MapViewManagerListener, VertexUpdateListener, SelectionListener, VerticesUpdateManager.VerticesUpdateListener {

    public static final String PARAMETER_FOCUS_NODES = "focusNodes";

    private class DynamicUpdateRefresher implements Refresher.RefreshListener {
        private final Object lockObject = "lockObject";
        private boolean refreshInProgress = false;
        private long lastUpdateTime;

        @Override
        public void refresh(Refresher refresher) {
            if (needsRefresh()) {
                synchronized (lockObject) {
                    refreshInProgress = true;

                    m_log.debug("Refresh UI");
                    getGraphContainer().getBaseTopology().refresh();
                    getGraphContainer().redoLayout();
                    TopologyUI.this.markAsDirtyRecursive();

                    lastUpdateTime = System.currentTimeMillis();

                    refreshInProgress = false;
                }
            }
        }

        private boolean needsRefresh() {
            if (refreshInProgress) {
                return false;
            }
            if (!m_graphContainer.getAutoRefreshSupport().isEnabled()) {
                return false;
            }

            long updateDiff = System.currentTimeMillis() - lastUpdateTime;
            return updateDiff >= m_graphContainer.getAutoRefreshSupport().getInterval()*1000; // update or not
        }
    }

	private static final long serialVersionUID = 6837501987137310938L;

    private static Logger m_log = LoggerFactory.getLogger(TopologyUI.class);
	private static final String LABEL_PROPERTY = "label";
	private TopologyComponent m_topologyComponent;
	private VertexSelectionTree m_tree;
	private final GraphContainer m_graphContainer;
    private SelectionManager m_selectionManager;
    private final CommandManager m_commandManager;
	private MenuBar m_menuBar;
	private TopoContextMenu m_contextMenu;
	private VerticalLayout m_layout;
	private VerticalLayout m_rootLayout;
	private final IconRepositoryManager m_iconRepositoryManager;
	private WidgetManager m_widgetManager;
	private WidgetManager m_treeWidgetManager;
	private Accordion m_treeAccordion;
    private HorizontalSplitPanel m_treeMapSplitPanel;
    private final Label m_zoomLevelLabel = new Label("0"); 
    private final HistoryManager m_historyManager;
    private String m_headerHtml;
    private boolean m_showHeader = true;
    private OnmsHeaderProvider m_headerProvider = null;
    private String m_userName;
    private OnmsServiceManager m_serviceManager;
    private VaadinApplicationContext m_applicationContext;
    private VerticesUpdateManager m_verticesUpdateManager;

    private String getHeader(HttpServletRequest request) {
        if(m_headerProvider == null) {
            return "";
        } else {
            return m_headerProvider.getHeaderHtml(request);
        }
    }

    public TopologyUI(CommandManager commandManager, HistoryManager historyManager, GraphContainer graphContainer, IconRepositoryManager iconRepoManager, SelectionManager selectionManager) {
        // Ensure that selection changes trigger a history save operation
        m_commandManager = commandManager;
        m_historyManager = historyManager;
        m_iconRepositoryManager = iconRepoManager;

        // Create a per-session GraphContainer instance
        m_graphContainer = graphContainer;
        m_selectionManager = selectionManager;
        m_graphContainer.setSelectionManager(selectionManager);
    }

	@Override
    protected void init(final VaadinRequest request) {
        m_headerHtml =  getHeader(new HttpServletRequestVaadinImpl(request));
        m_graphContainer.setLayoutAlgorithm(new FRLayoutAlgorithm());

        //create VaadinApplicationContext
        m_applicationContext = m_serviceManager.createApplicationContext(new VaadinApplicationContextCreator() {
            @Override
            public VaadinApplicationContext create(OnmsServiceManager manager) {
                VaadinApplicationContextImpl context = new VaadinApplicationContextImpl();
                context.setSessionId(request.getWrappedSession().getId());
                context.setUiId(getUIId());
                context.setUsername(request.getRemoteUser());
                return context;
            }
        });
        m_verticesUpdateManager = new OsgiVerticesUpdateManager(m_serviceManager, m_applicationContext);

        loadUserSettings(m_applicationContext);
        loadVertexHopCriteria(request, m_graphContainer);
        setupListeners();
        createLayouts();
        setupErrorHandler();
        setupAutoRefresher();

        // notifiy osgi-listeners, otherwise initialization would not work
        m_graphContainer.addChangeListener(m_verticesUpdateManager);
        m_selectionManager.addSelectionListener(m_verticesUpdateManager);
        m_verticesUpdateManager.selectionChanged(m_selectionManager);
        m_verticesUpdateManager.graphChanged(m_graphContainer);

        m_serviceManager.getEventRegistry().addPossibleEventConsumer(this, m_applicationContext);
    }

    private void setupListeners() {
        Page.getCurrent().addUriFragmentChangedListener(this);
        m_selectionManager.addSelectionListener(this);
        m_graphContainer.addChangeListener(this);
        m_graphContainer.getMapViewManager().addListener(this);
        m_commandManager.addMenuItemUpdateListener(this);
        m_commandManager.addCommandUpdateListener(this);
    }

    private static void loadVertexHopCriteria(VaadinRequest request, GraphContainer graphContainer) {
        String nodeIds = request.getParameter(PARAMETER_FOCUS_NODES);
        if (nodeIds == null) {
            return;
        }
        Collection<Integer> refs = new TreeSet<Integer>();
        for (String nodeId : nodeIds.split(",")) {
            try {
                refs.add(Integer.parseInt(nodeId));
            } catch (NumberFormatException e) {
                m_log.warn("Invalid node ID found in {} parameter: {}", PARAMETER_FOCUS_NODES, nodeId);
            }
        }
        // If we found valid node IDs in the list...
        if (refs.size() > 0) {
            VertexHopCriteria criteria = VertexHopGraphProvider.getVertexHopProviderForContainer(graphContainer);
            // Clear the exiting focus node list
            criteria.clear();
            for (Integer ref : refs) {
                // Add a new focus node reference to the VertexHopCriteria
                criteria.add(new AbstractVertexRef("nodes", String.valueOf(ref)));
            }
        } else {
            // Don't do anything... we didn't find any focus nodes in the parameter so don't alter
            // any existing VertexHopCriteria
        }
    }

    private void createLayouts() {
        m_rootLayout = new VerticalLayout();
        m_rootLayout.setSizeFull();
        setContent(m_rootLayout);

        addHeader();

        addContentLayout();
    }

    private void setupErrorHandler() {
        
        UI.getCurrent().setErrorHandler(new DefaultErrorHandler(){

            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Notification.show("An Exception Occurred: see karaf.log", Notification.Type.TRAY_NOTIFICATION);
                LoggerFactory.getLogger(this.getClass()).warn("An Exception Occured: in the TopologyUI", event.getThrowable());
                super.error(event);
            }
        });
    }

    private void setupAutoRefresher() {
        if (m_graphContainer.hasAutoRefreshSupport()) {
            Refresher refresher = new Refresher();
            refresher.setRefreshInterval(5000); // ask every 5 seconds for changes
            refresher.addListener(new DynamicUpdateRefresher());
            addExtension(refresher);
        }
    }

    private void addHeader() {
        if (m_headerHtml != null && m_showHeader) {
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(m_headerHtml.getBytes());
                final CustomLayout headerLayout = new CustomLayout(is);
                headerLayout.setWidth("100%");
                headerLayout.addStyleName("onmsheader");
                m_rootLayout.addComponent(headerLayout);
            } catch (final IOException e) {
                try {
                    is.close();
                } catch (final IOException closeE) {
                    m_log.debug("failed to close HTML input stream", closeE);
                }
                m_log.debug("failed to get header layout data", e);
            }
        }
    }

    private void addContentLayout() {
        m_layout = new VerticalLayout();
        m_layout.setSizeFull();

        // Set expand ratio so that all extra space is allocated to this vertical component
        m_rootLayout.addComponent(m_layout);
        m_rootLayout.setExpandRatio(m_layout, 1);

        m_treeMapSplitPanel = new HorizontalSplitPanel();
        m_treeMapSplitPanel.setFirstComponent(createWestLayout());
        m_treeMapSplitPanel.setSecondComponent(createMapLayout());
        m_treeMapSplitPanel.setSplitPosition(222, Unit.PIXELS);
        m_treeMapSplitPanel.setSizeFull();



        menuBarUpdated(m_commandManager);
        if(m_widgetManager.widgetCount() != 0) {
            updateWidgetView(m_widgetManager);
        }else {
            m_layout.addComponent(m_treeMapSplitPanel);
        }

        if(m_treeWidgetManager.widgetCount() != 0) {
            updateAccordionView(m_treeWidgetManager);
        }
    }

    private AbsoluteLayout createMapLayout() {
        final Property<Double> scale = m_graphContainer.getScaleProperty();

        m_topologyComponent = new TopologyComponent(m_graphContainer, m_iconRepositoryManager, this);
        m_topologyComponent.setSizeFull();
        m_topologyComponent.addMenuItemStateListener(this);
        m_topologyComponent.addVertexUpdateListener(this);

        final Slider slider = new Slider(0, 1);

        slider.setPropertyDataSource(scale);
        slider.setResolution(1);
        slider.setHeight("300px");
        slider.setOrientation(SliderOrientation.VERTICAL);

        slider.setImmediate(true);

        final Button zoomInBtn = new Button();
        zoomInBtn.setIcon(new ThemeResource("images/plus.png"));
        zoomInBtn.setDescription("Expand Semantic Zoom Level");
        zoomInBtn.setStyleName("semantic-zoom-button");
        zoomInBtn.addClickListener(new ClickListener() {

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
        zoomOutBtn.addClickListener(new ClickListener() {
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
        selectBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                selectBtn.setStyleName("toolbar-button down");
                panBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("select");
            }
        });

        panBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                panBtn.setStyleName("toolbar-button down");
                selectBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("pan");
            }
        });

        final Button historyBackBtn = new Button("<<");
        historyBackBtn.setDescription("Click to go back");
        historyBackBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                com.vaadin.ui.JavaScript.getCurrent().execute("window.history.back()");
            }
        });

        final Button historyForwardBtn = new Button(">>");
        historyForwardBtn.setDescription("Click to go forward");
        historyForwardBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                com.vaadin.ui.JavaScript.getCurrent().execute("window.history.forward()");
            }
        });

        VerticalLayout toolbar = new VerticalLayout();
        toolbar.setWidth("31px");
        toolbar.addComponent(panBtn);
        toolbar.addComponent(selectBtn);

        HorizontalLayout historyButtonLayout = new HorizontalLayout();
        historyButtonLayout.addComponent(historyBackBtn);
        historyButtonLayout.addComponent(historyForwardBtn);

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
        mapLayout.addComponent(historyButtonLayout, "top: 5px; right: 10px;");
        mapLayout.setSizeFull();

        return mapLayout;

    }

    private void loadUserSettings(VaadinApplicationContext context) {
        m_userName = context.getUsername();
        m_graphContainer.setUserName(m_userName);
        m_graphContainer.setSessionId(context.getSessionId());

        // See if the history manager has an existing fragment stored for
        // this user. Do this before laying out the UI because the history
        // may change during layout.
        String fragment = m_historyManager.getHistoryHash(m_userName);

        // If there was existing history, then restore that history snapshot.
        if (fragment != null) {
            LoggerFactory.getLogger(this.getClass()).info("Restoring history for user {}: {}", m_userName, fragment);
            Page.getCurrent().setUriFragment(fragment);
            m_historyManager.applyHistory(m_userName, fragment, m_graphContainer);
        }
    }

    /**
     * Update the Accordion View with installed widgets
     * @param treeWidgetManager
     */
    private void updateAccordionView(WidgetManager treeWidgetManager) {
        if (m_treeAccordion != null) {
            m_treeAccordion.removeAllComponents();
            
            m_treeAccordion.addTab(m_tree, m_tree.getTitle());
            for(IViewContribution widget : treeWidgetManager.getWidgets()) {
                if(widget.getIcon() != null) {
                    m_treeAccordion.addTab(widget.getView(m_applicationContext, this), widget.getTitle(), widget.getIcon());
                }else {
                    m_treeAccordion.addTab(widget.getView(m_applicationContext, this), widget.getTitle());
                }
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
        if (m_layout != null) {
            synchronized (m_layout) {
                m_layout.removeAllComponents();
                if(widgetManager.widgetCount() == 0) {
                    m_layout.addComponent(m_treeMapSplitPanel);
                } else {
                    VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
                    bottomLayoutBar.setFirstComponent(m_treeMapSplitPanel);
                    // Split the screen 70% top, 30% bottom
                    bottomLayoutBar.setSplitPosition(70, Unit.PERCENTAGE);
                    bottomLayoutBar.setSizeFull();
                    bottomLayoutBar.setSecondComponent(getTabSheet(widgetManager, this));
                    m_layout.addComponent(bottomLayoutBar);
                }
                m_layout.requestRepaint();
            }
        }
        // TODO: Integrate contextmenu with the Connector/Extension pattern
        if(m_contextMenu != null && m_contextMenu.getParent() == null) {
            //getMainWindow().addComponent(m_contextMenu);
        }
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
            final Component view = viewContrib.getView(m_applicationContext, widgetContext);
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
                    extraControls.setHeight(32, Unit.PIXELS);
                    extraControls.setSpacing(true);

                    // Add the extra controls to the layout
                    for (Component component : extras) {
                        extraControls.addComponent(component);
                        extraControls.setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                    }

                    // Add a TabSheet.SelectedTabChangeListener to show or hide the extra controls
                    tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
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
        bottomLayout.addComponent(tabSheet, "top: 0; left: 0; bottom: 0; right: 0;");

        return bottomLayout;
    }
    

    /**
     * Creates the west area layout including the
     * accordion and tree views.
     * 
     * @return
     */
	private Layout createWestLayout() {
        m_tree = createTree();
        
        final TextField filterField = new TextField("Filter");
        filterField.setTextChangeTimeout(200);
        
        final Button filterBtn = new Button("Filter");
        filterBtn.addClickListener(new ClickListener() {

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
	}

	private void updateContextMenuItems(Object target, List<TopoContextMenuItem> items) {
		for(TopoContextMenuItem contextItem : items) {
			if(contextItem.hasChildren()) {
				updateContextMenuItems(target, contextItem.getChildren());
			} else {
				m_commandManager.updateContextMenuItem(target, contextItem, m_graphContainer, this);
			}
		}
	}


	private void updateMenuItems(List<MenuItem> menuItems) {
		for(MenuItem menuItem : menuItems) {
			if(menuItem.hasChildren()) {
				updateMenuItems(menuItem.getChildren());
			}else {
				m_commandManager.updateMenuItem(menuItem, m_graphContainer, this);
			}
		}
	}

	@Override
	public void menuBarUpdated(CommandManager commandManager) {
		if(m_menuBar != null) {
			m_rootLayout.removeComponent(m_menuBar);
		}

		if(m_contextMenu != null) {
			m_contextMenu.detach();
		}

		m_menuBar = commandManager.getMenuBar(m_graphContainer, this);
		m_menuBar.setWidth(100, Unit.PERCENTAGE);
		// Set expand ratio so that extra space is not allocated to this vertical component
        if (m_showHeader) {
            m_rootLayout.addComponent(m_menuBar, 1);
        } else {
            m_rootLayout.addComponent(m_menuBar, 0);
        }


		m_contextMenu = commandManager.getContextMenu(m_graphContainer, this);
		m_contextMenu.setAsContextMenuOf(this);
		updateMenuItems();
	}
	
        @Override
	public void show(Object target, int left, int top) {
		updateContextMenuItems(target, m_contextMenu.getItems());
		m_contextMenu.setTarget(target);
		m_contextMenu.open(left, top);
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
        if(!isClosing()) {
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
    public void uriFragmentChanged(UriFragmentChangedEvent event) {
        m_settingFragment++;
        String fragment = event.getUriFragment();
        m_historyManager.applyHistory(m_userName, fragment, m_graphContainer);
        m_settingFragment--;
    }


    private void saveHistory() {
        if (m_settingFragment == 0) {
            String fragment = m_historyManager.createHistory(m_userName, m_graphContainer);
            Page.getCurrent().setUriFragment(fragment, false);
        }
    }


    @Override
    public void graphChanged(GraphContainer graphContainer) {
        m_zoomLevelLabel.setValue("" + graphContainer.getSemanticZoomLevel());
    }


    private void setSemanticZoomLevel(int szl) {
        m_zoomLevelLabel.setValue(String.valueOf(szl));
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

    public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }

    /**
     * Parameter is a String because config has String values
     * @param boolVal
     */
    //@Override
    public void setShowHeader(String boolVal) {
        m_showHeader = "true".equals(boolVal);
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        saveHistory();
    }

    @Override
    public void detach() {
        m_commandManager.removeCommandUpdateListener(this);
        m_commandManager.removeMenuItemUpdateListener(this);
        super.detach();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void setServiceManager(BundleContext bundleContext) {
        this.m_serviceManager = new OnmsServiceManagerLocator().lookup(bundleContext);
    }

    public VaadinApplicationContext getApplicationContext() {
        return m_applicationContext;
    }

    @Override
    @EventConsumer
    public void verticesUpdated(VerticesUpdateManager.VerticesUpdateEvent event) {

        Collection<VertexRef> selectedVertexRefs = m_selectionManager.getSelectedVertexRefs();
        Set<VertexRef> vertexRefs = event.getVertexRefs();
        if(!selectedVertexRefs.equals(vertexRefs) && !event.allVerticesSelected()){
            m_selectionManager.setSelectedVertexRefs(vertexRefs);
        }


    }
}
