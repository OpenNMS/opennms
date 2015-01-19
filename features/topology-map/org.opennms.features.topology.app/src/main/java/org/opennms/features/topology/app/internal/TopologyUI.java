/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.*;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.CommandManager.DefaultOperationContext;
import org.opennms.features.topology.app.internal.TopologyComponent.VertexUpdateListener;
import org.opennms.features.topology.app.internal.jung.TopoFRLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.features.topology.app.internal.support.FontAwesomeIcons;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.opennms.features.topology.app.internal.ui.HudDisplay;
import org.opennms.features.topology.app.internal.ui.LastUpdatedLabel;
import org.opennms.features.topology.app.internal.ui.NoContentAvailableWindow;
import org.opennms.features.topology.app.internal.ui.SearchBox;
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
import java.net.URL;
import java.util.*;

@SuppressWarnings("serial")
@Theme("topo_default")
@PreserveOnRefresh
public class TopologyUI extends UI implements CommandUpdateListener, MenuItemUpdateListener, ContextMenuHandler, WidgetUpdateListener, WidgetContext, UriFragmentChangedListener, GraphContainer.ChangeListener, MapViewManagerListener, VertexUpdateListener, SelectionListener, VerticesUpdateManager.VerticesUpdateListener {

    public static final String PARAMETER_FOCUS_NODES = "focusNodes";
    private static final String PARAMETER_SEMANTIC_ZOOM_LEVEL = "szl";
    private static final String PARAMETER_GRAPH_PROVIDER = "provider";

    private class DynamicUpdateRefresher implements Refresher.RefreshListener {
        private final Object lockObject = new Object();
        private boolean m_refreshInProgress = false;
        private long m_lastUpdateTime = 0;


        @Override
        public void refresh(Refresher refresher) {
             if (needsRefresh()) {
                refreshUI();
            }
        }

        private void refreshUI() {
            synchronized (lockObject) {
                m_refreshInProgress = true;
                m_topologyComponent.blockSelectionEvents();

                getGraphContainer().getBaseTopology().refresh();
                getGraphContainer().setDirty(true);
                getGraphContainer().redoLayout();
                TopologyUI.this.markAsDirtyRecursive();

                m_lastUpdateTime = System.currentTimeMillis();
                updateTimestamp(m_lastUpdateTime);

                m_topologyComponent.unblockSelectionEvents();
                m_refreshInProgress = false;
            }
        }

        private boolean needsRefresh() {
            if (m_refreshInProgress) {
                return false;
            }

            if (!m_graphContainer.getAutoRefreshSupport().isEnabled()) {
                return false;
            }

            return true;
        }

    }

    private static final long serialVersionUID = 6837501987137310938L;
    private static final Logger LOG = LoggerFactory.getLogger(TopologyUI.class);

    private TopologyComponent m_topologyComponent;
    private Window m_noContentWindow;
    private final GraphContainer m_graphContainer;
    private SelectionManager m_selectionManager;
    private final CommandManager m_commandManager;
    private MenuBar m_menuBar;
    private TopoContextMenu m_contextMenu;
    private VerticalLayout m_layout;
    private VerticalLayout m_rootLayout;
    private final IconRepositoryManager m_iconRepositoryManager;
    private WidgetManager m_widgetManager;
    private AbsoluteLayout m_treeMapSplitPanel;
    private final TextField m_zoomLevelLabel = new TextField();
    private final HistoryManager m_historyManager;
    private String m_headerHtml;
    private boolean m_showHeader = true;
    private OnmsHeaderProvider m_headerProvider = null;
    private String m_userName;
    private OnmsServiceManager m_serviceManager;
    private VaadinApplicationContext m_applicationContext;
    private VerticesUpdateManager m_verticesUpdateManager;
    private Button m_panBtn;
    private Button m_selectBtn;
    private Button m_szlOutBtn;
    private LastUpdatedLabel m_lastUpdatedTimeLabel;
    private HudDisplay m_hudDisplay;
    int m_settingFragment = 0;
    private SearchBox m_searchBox;

    private String getHeader(HttpServletRequest request) throws Exception {
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
        FontAwesomeIcons.load(new ThemeResource("font-awesome/css/font-awesome.min.css"));

        try {
            URL pageUrl = Page.getCurrent().getLocation().toURL();
            m_headerHtml = getHeader(new HttpServletRequestVaadinImpl(request, pageUrl));
        } catch (final Exception e) {
            LOG.error("failed to get header HTML for request " + request.getPathInfo(), e.getCause());
        }

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

        // Add a request handler that parses incoming focusNode and szl query parameters
        getSession().addRequestHandler(new RequestHandler() {
            @Override
            public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
                loadGraphProvider(request);
                loadVertexHopCriteria(request, m_graphContainer);
                loadSemanticZoomLevel(request, m_graphContainer);
                m_graphContainer.redoLayout();
                return false; // No response was written
            }
        });

        // Set the algorithm last so that the criteria and SZLs are
        // in place before we run the layout algorithm.
        m_graphContainer.setLayoutAlgorithm(new TopoFRLayoutAlgorithm());

        loadUserSettings(m_applicationContext);

        setupListeners();
        createLayouts();
        // Set up an error handler for UI-level exceptions
        setupErrorHandler();
        // Add an auto refresh handler to the GraphContainer
        setupAutoRefresher();

        // the layout must be created BEFORE loading the hop criteria and the semantic zoom level
        loadGraphProvider(request);
        loadVertexHopCriteria(request, m_graphContainer);
        loadSemanticZoomLevel(request, m_graphContainer);
        m_graphContainer.redoLayout();

        // notify OSGi listeners, otherwise initialization would not work
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
        FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer);

        if (nodeIds != null) {
            Collection<Integer> refs = new TreeSet<Integer>();
            for (String nodeId : nodeIds.split(",")) {
                try {
                    refs.add(Integer.parseInt(nodeId));
                } catch (NumberFormatException e) {
                    LOG.warn("Invalid node ID found in {} parameter: {}", PARAMETER_FOCUS_NODES, nodeId);
                }
            }
            // If we found valid node IDs in the list...
            if (refs.size() > 0) {
                if (criteria.size() == refs.size()) {
                    boolean criteriaChanged = false;
                    for (Integer ref : refs) {
                        if (!criteria.contains(new DefaultVertexRef("nodes", String.valueOf(ref)))) {
                            criteriaChanged = true;
                        }
                    }
                    // If all of the refs in the query string are already in the filter, then
                    // just return without altering it
                    if (!criteriaChanged) {
                        return;
                    }
                }

                // Clear the exiting focus node list
                criteria.clear();
                for (Integer ref : refs) {
                    // Add a new focus node reference to the VertexHopCriteria
                    criteria.add(new DefaultVertexRef("nodes", String.valueOf(ref)));
                }
                // Set the semantic zoom level to 1 by default
                graphContainer.setSemanticZoomLevel(1);
            } else {
                // Don't do anything... we didn't find any focus nodes in the parameter so don't alter
                // any existing VertexHopCriteria
            }
        }

        // check if we have a criteria set
        if (criteria.isEmpty() && noAdditionalFocusCriteria(graphContainer)) { // no criteria or nodes in focus, load defaults
            graphContainer.removeCriteria(criteria); // it is empty, so we don't need it
            graphContainer.addCriteria(graphContainer.getBaseTopology().getDefaultCriteria()); // set default
        }
    }

    private static boolean noAdditionalFocusCriteria(GraphContainer graphContainer) {
        Criteria[] crits = graphContainer.getCriteria();
        for(Criteria criteria : crits){
            try{
                CategoryHopCriteria catCrit = (CategoryHopCriteria) criteria;
                return false;
            } catch(ClassCastException e){}

        }
        return true;
    }

    private static void loadSemanticZoomLevel(VaadinRequest request, GraphContainer graphContainer) {
        String szl = request.getParameter(PARAMETER_SEMANTIC_ZOOM_LEVEL);
        if (szl != null) {
            try {
                graphContainer.setSemanticZoomLevel(Integer.parseInt(szl));
            } catch (NumberFormatException e) {
                LOG.warn("Invalid SZL found in {} parameter: {}", PARAMETER_SEMANTIC_ZOOM_LEVEL, szl);
            }
        }
    }

    private void loadGraphProvider(VaadinRequest request) {
        String providerName = request.getParameter(PARAMETER_GRAPH_PROVIDER);
        if (providerName != null) {
            List<HistoryOperation> operations = m_historyManager.getHistoryOperations();
            for (HistoryOperation operation : operations) {
                try {
                    // We have to cast to CheckedOperation here since that is the 
                    // interface that is used in the OSGi proxies
                    CheckedOperation selectOp = (CheckedOperation)operation;
                    if (providerName.equals(selectOp.getId())) {
                        selectOp.execute(Collections.<VertexRef>emptyList(), new DefaultOperationContext(this, m_graphContainer, DisplayLocation.MENUBAR));
                        // Updated the checked state of the menu items
                        updateMenuItems();
                        return;
                    }
                } catch (ClassCastException e) {}
            }
        }
        //Initialize the Base Topology provider just in case
        getGraphContainer().getBaseTopology().refresh();
    }

    private void createLayouts() {
        m_rootLayout = new VerticalLayout();
        m_rootLayout.setSizeFull();
        m_rootLayout.addStyleName("root-layout");
        m_rootLayout.addStyleName("topo-root-layout");
        setContent(m_rootLayout);

        addHeader();

        addContentLayout();
    }

    private void setupErrorHandler() {
        setErrorHandler(new DefaultErrorHandler() {

            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Notification.show("An Exception Occurred: see karaf.log", Notification.Type.TRAY_NOTIFICATION);
                LOG.warn("An Exception Occured: in the TopologyUI", event.getThrowable());

            }
        });
    }

    private void setupAutoRefresher() {
        if (m_graphContainer.hasAutoRefreshSupport()) {
            Refresher refresher = new Refresher();
            refresher.setRefreshInterval((int)m_graphContainer.getAutoRefreshSupport().getInterval() * 1000); // ask every 1 seconds for changes
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
                    LOG.debug("failed to close HTML input stream", closeE);
                }
                LOG.debug("failed to get header layout data", e);
            }
        }
    }

    private void addContentLayout() {
        m_layout = new VerticalLayout();
        m_layout.setSizeFull();

        // Set expand ratio so that all extra space is allocated to this vertical component
        m_rootLayout.addComponent(m_layout);
        m_rootLayout.setExpandRatio(m_layout, 1);

        //TODO: Don't create a horizontal Split container here, no need. Remove and use the absolute
        m_treeMapSplitPanel = new AbsoluteLayout();
        m_treeMapSplitPanel.addComponent(createMapLayout(), "top: 0px; left: 0px; right: 0px; bottom: 0px;");
        m_treeMapSplitPanel.setSizeFull();

        menuBarUpdated(m_commandManager);
        if(m_widgetManager.widgetCount() != 0) {
            updateWidgetView(m_widgetManager);
        }else {
            m_layout.addComponent(m_treeMapSplitPanel);
        }
    }

    private AbsoluteLayout createMapLayout() {
        final Property<Double> scale = m_graphContainer.getScaleProperty();

        m_lastUpdatedTimeLabel = new LastUpdatedLabel();
        m_lastUpdatedTimeLabel.setImmediate(true);

        m_hudDisplay = new HudDisplay();
        m_hudDisplay.setImmediate(true);

        m_zoomLevelLabel.setHeight(20, Unit.PIXELS);
        m_zoomLevelLabel.setWidth(22, Unit.PIXELS);
        m_zoomLevelLabel.addStyleName("center-text");
        m_zoomLevelLabel.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                try{
                    int zoomLevel = Integer.parseInt(event.getText());
                    setSemanticZoomLevel(zoomLevel);
                } catch(NumberFormatException e){
                    setSemanticZoomLevel(m_graphContainer.getSemanticZoomLevel());
                }

            }
        });

        m_topologyComponent = new TopologyComponent(m_graphContainer, m_iconRepositoryManager, this);
        m_topologyComponent.setSizeFull();
        m_topologyComponent.addMenuItemStateListener(this);
        m_topologyComponent.addVertexUpdateListener(this);

        final Slider slider = new Slider(0, 1);
        slider.setPropertyDataSource(scale);
        slider.setResolution(1);
        slider.setHeight("200px");
        slider.setOrientation(SliderOrientation.VERTICAL);

        slider.setImmediate(true);

        final NativeButton showFocusVerticesBtn = new NativeButton(FontAwesomeIcons.Icon.eye_open.variant());
        showFocusVerticesBtn.setDescription("Toggle Highlight Focus Nodes");
        showFocusVerticesBtn.setHtmlContentAllowed(true);
        showFocusVerticesBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if(showFocusVerticesBtn.getCaption().equals(FontAwesomeIcons.Icon.eye_close.variant())){
                    showFocusVerticesBtn.setCaption(FontAwesomeIcons.Icon.eye_open.variant());
                } else {
                    showFocusVerticesBtn.setCaption(FontAwesomeIcons.Icon.eye_close.variant());
                }
                m_topologyComponent.getState().setHighlightFocus(!m_topologyComponent.getState().isHighlightFocus());
                m_topologyComponent.updateGraph();
            }
        });

        final NativeButton magnifyBtn = new NativeButton();
        magnifyBtn.setHtmlContentAllowed(true);
        magnifyBtn.setCaption("<i class=\"" + FontAwesomeIcons.Icon.zoom_in.stylename() + "\" ></i>");
        magnifyBtn.setStyleName("icon-button");
        magnifyBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if(slider.getValue() < 1){
                    slider.setValue(Math.min(1, slider.getValue() + 0.25));
                }
            }
        });

        final NativeButton demagnifyBtn = new NativeButton();
        demagnifyBtn.setHtmlContentAllowed(true);
        demagnifyBtn.setCaption("<i class=\"" + FontAwesomeIcons.Icon.zoom_out.stylename() + "\" ></i>");
        demagnifyBtn.setStyleName("icon-button");
        demagnifyBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if(slider.getValue() != 0){
                    slider.setValue(Math.max(0, slider.getValue() - 0.25));
                }
            }
        });

        VerticalLayout sliderLayout = new VerticalLayout();
        sliderLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        sliderLayout.addComponent(magnifyBtn);
        sliderLayout.addComponent(slider);
        sliderLayout.addComponent(demagnifyBtn);

        m_szlOutBtn = new Button();
        m_szlOutBtn.setHtmlContentAllowed(true);
        m_szlOutBtn.setCaption(FontAwesomeIcons.Icon.arrow_down.variant());
        m_szlOutBtn.setDescription("Collapse Semantic Zoom Level");
        m_szlOutBtn.setEnabled(m_graphContainer.getSemanticZoomLevel() > 0);
        m_szlOutBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                int szl = m_graphContainer.getSemanticZoomLevel();
                if (szl > 0) {
                    szl--;
                    setSemanticZoomLevel(szl);
                    saveHistory();
                }
            }
        });

        final Button szlInBtn = new Button();
        szlInBtn.setHtmlContentAllowed(true);
        szlInBtn.setCaption(FontAwesomeIcons.Icon.arrow_up.variant());
        szlInBtn.setDescription("Expand Semantic Zoom Level");
        szlInBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                int szl = m_graphContainer.getSemanticZoomLevel();
                szl++;
                setSemanticZoomLevel(szl);
                saveHistory();
            }
        });


        m_panBtn = new Button();
        m_panBtn.setIcon(new ThemeResource("images/cursor_drag_arrow.png"));
        m_panBtn.setDescription("Pan Tool");
        m_panBtn.setStyleName("toolbar-button down");

        m_selectBtn = new Button();
        m_selectBtn.setIcon(new ThemeResource("images/selection.png"));
        m_selectBtn.setDescription("Selection Tool");
        m_selectBtn.setStyleName("toolbar-button");
        m_selectBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                m_selectBtn.setStyleName("toolbar-button down");
                m_panBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("select");
            }
        });

        m_panBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                m_panBtn.setStyleName("toolbar-button down");
                m_selectBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("pan");
            }
        });

        final Button historyBackBtn = new Button(FontAwesomeIcons.Icon.arrow_left.variant());
        historyBackBtn.setHtmlContentAllowed(true);
        historyBackBtn.setDescription("Click to go back");
        historyBackBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                com.vaadin.ui.JavaScript.getCurrent().execute("window.history.back()");
            }
        });

        final Button historyForwardBtn = new Button(FontAwesomeIcons.Icon.arrow_right.variant());
        historyForwardBtn.setHtmlContentAllowed(true);
        historyForwardBtn.setDescription("Click to go forward");
        historyForwardBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                com.vaadin.ui.JavaScript.getCurrent().execute("window.history.forward()");
            }
        });

        m_searchBox = new SearchBox(m_serviceManager, new CommandManager.DefaultOperationContext(this, m_graphContainer, OperationContext.DisplayLocation.SEARCH));
        m_selectionManager.addSelectionListener(m_searchBox);
        m_graphContainer.addChangeListener(m_searchBox);

        //History Button Layout
        HorizontalLayout historyButtonLayout = new HorizontalLayout();
        historyButtonLayout.setSpacing(true);
        historyButtonLayout.addComponent(historyBackBtn);
        historyButtonLayout.addComponent(historyForwardBtn);

        //Semantic Controls Layout
        HorizontalLayout semanticLayout = new HorizontalLayout();
        semanticLayout.setSpacing(true);
        semanticLayout.addComponent(szlInBtn);
        semanticLayout.addComponent(m_zoomLevelLabel);
        semanticLayout.addComponent(m_szlOutBtn);
        semanticLayout.setComponentAlignment(m_zoomLevelLabel, Alignment.MIDDLE_CENTER);

        VerticalLayout historyCtrlLayout = new VerticalLayout();
        historyCtrlLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        historyCtrlLayout.addComponent(historyButtonLayout);

        HorizontalLayout controlLayout = new HorizontalLayout();
        controlLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        controlLayout.addComponent(m_panBtn);
        controlLayout.addComponent(m_selectBtn);

        VerticalLayout semanticCtrlLayout = new VerticalLayout();
        semanticCtrlLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        semanticCtrlLayout.addComponent(semanticLayout);

        HorizontalLayout locationToolLayout = createLocationToolLayout();

        //Vertical Layout for all tools on right side
        VerticalLayout toolbar = new VerticalLayout();
        toolbar.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        toolbar.setSpacing(true);

        toolbar.addComponent(historyCtrlLayout);
        toolbar.addComponent(locationToolLayout);
        toolbar.addComponent(showFocusVerticesBtn);
        toolbar.addComponent(sliderLayout);
        toolbar.addComponent(controlLayout);
        toolbar.addComponent(semanticCtrlLayout);


        AbsoluteLayout mapLayout = new AbsoluteLayout();


        mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
        mapLayout.addComponent(m_lastUpdatedTimeLabel, "top: 5px; right: 10px;");
        mapLayout.addComponent(m_hudDisplay, "top: 445px; right: 10px");
        mapLayout.addComponent(toolbar, "top: 25px; right: 10px;");
        mapLayout.addComponent(m_searchBox, "top:5px; left:5px;");
        //mapLayout.addComponent(locationToolLayout, "top: 5px; left: 50%");
        mapLayout.setSizeFull();

        m_noContentWindow = new NoContentAvailableWindow(m_graphContainer);
        m_noContentWindow.setVisible(true);
        addWindow(m_noContentWindow);

        return mapLayout;
    }

    private HorizontalLayout createLocationToolLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        Button showAllMapBtn = new Button(FontAwesomeIcons.Icon.globe.variant());
        showAllMapBtn.setHtmlContentAllowed(true);
        showAllMapBtn.setDescription("Show Entire Map");
        showAllMapBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                m_topologyComponent.showAllMap();
            }
        });

        Button centerSelectionBtn = new Button(FontAwesomeIcons.Icon.location_arrow.variant());
        centerSelectionBtn.setHtmlContentAllowed(true);
        centerSelectionBtn.setDescription("Center On Selection");
        centerSelectionBtn.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                m_topologyComponent.centerMapOnSelection();
            }
        });

        layout.addComponent(centerSelectionBtn);
        layout.addComponent(showAllMapBtn);

        return layout;
    }

    private void loadUserSettings(VaadinApplicationContext context) {
        m_userName = context.getUsername();
        m_graphContainer.setSessionId(context.getSessionId());

        // See if the history manager has an existing fragment stored for
        // this user. Do this before laying out the UI because the history
        // may change during layout.
        String fragment = m_historyManager.getHistoryHash(m_userName);

        // If there was existing history, then restore that history snapshot.
        if (fragment != null) {
            LoggerFactory.getLogger(this.getClass()).info("Restoring history for user {}: {}", m_userName, fragment);
            Page page = Page.getCurrent();
            if (page != null) {
                page.setUriFragment(fragment);
            }
            m_historyManager.applyHistory(m_userName, fragment, m_graphContainer);
        }
    }

    /**
     * Updates the bottom widget area with the registered widgets
     * 
     * Any widget with the service property of 'location=bottom' are
     * included.
     * 
     * @param widgetManager The WidgetManager.
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
                m_layout.markAsDirty();
            }
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

    public void updateTimestamp(long updateTime) {
        m_lastUpdatedTimeLabel.setUpdateTime(updateTime);
    }

	@Override
	public void updateMenuItems() {
		updateMenuItems(m_menuBar.getItems());
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

		m_contextMenu = commandManager.getContextMenu(new DefaultOperationContext(this, m_graphContainer, DisplayLocation.CONTEXTMENU));
		m_contextMenu.setAsContextMenuOf(this);
		updateMenuItems();
	}

	@Override
	public void showContextMenu(Object target, int left, int top) {
		// The target must be set before we update the operation context because the op context
		// operations are dependent on the target of the right-click
		m_contextMenu.setTarget(target);
		m_contextMenu.updateOperationContext(new DefaultOperationContext(this, m_graphContainer, DisplayLocation.CONTEXTMENU));
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
            }
        }
    }

    @Override
    public GraphContainer getGraphContainer() {
        return m_graphContainer;
    }

    @Override
    public void uriFragmentChanged(UriFragmentChangedEvent event) {
        m_settingFragment++;
        String fragment = event.getUriFragment();
        m_historyManager.applyHistory(m_userName, fragment, m_graphContainer);

        // This is a hack to fix issue SPC-796 so that the display states of the 
        // TopologyComponent and NoContentAvailableWindow are reset correctly 
        // after a history operation
        graphChanged(m_graphContainer);

        //Manually trigger the searchbox to refresh
        m_searchBox.graphChanged(m_graphContainer);

        m_settingFragment--;
    }

    private void saveHistory() {
        if (m_settingFragment == 0) {
            String fragment = m_historyManager.createHistory(m_userName, m_graphContainer);
            Page page = Page.getCurrent();
            if (page != null) {
                page.setUriFragment(fragment, false);
            }
        }
    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        // are there any vertices to display?
        boolean verticesAvailable = !graphContainer.getGraph().getDisplayVertices().isEmpty();

        // toggle view
        if (verticesAvailable) {
            m_noContentWindow.setVisible(false);
            removeWindow(m_noContentWindow);
            m_topologyComponent.setEnabled(true);
        } else {
            m_topologyComponent.setEnabled(false);
            m_noContentWindow.setVisible(true);
            if(!m_noContentWindow.isAttached()){
                addWindow(m_noContentWindow);
            }

        }
        m_hudDisplay.setProvider(graphContainer.getBaseTopology().getVertexNamespace().equals("nodes") ? "Linkd" : graphContainer.getBaseTopology().getVertexNamespace());
        m_hudDisplay.setVertexFocusCount(getFocusVertices(graphContainer));
        m_hudDisplay.setEdgeFocusCount(0);
        m_hudDisplay.setVertexSelectionCount(graphContainer.getSelectionManager().getSelectedVertexRefs().size());
        m_hudDisplay.setEdgeSelectionCount(graphContainer.getSelectionManager().getSelectedEdgeRefs().size());
        m_hudDisplay.setVertexContextCount(graphContainer.getGraph().getDisplayVertices().size());
        m_hudDisplay.setEdgeContextCount(graphContainer.getGraph().getDisplayEdges().size());
        m_hudDisplay.setVertexTotalCount(graphContainer.getBaseTopology().getVertexTotalCount());
        m_hudDisplay.setEdgeTotalCount(graphContainer.getBaseTopology().getEdges().size());

        m_zoomLevelLabel.setValue(String.valueOf(graphContainer.getSemanticZoomLevel()));
        m_szlOutBtn.setEnabled(graphContainer.getSemanticZoomLevel() > 0);
        updateTimestamp(System.currentTimeMillis());
    }

    private int getFocusVertices(GraphContainer graphContainer) {
        int count = 0;
        Criteria[] crits = graphContainer.getCriteria();
        for(Criteria criteria : crits){
            try{
                VertexHopGraphProvider.VertexHopCriteria catCrit = (VertexHopGraphProvider.VertexHopCriteria) criteria;
                count += catCrit.getVertices().size();
            } catch(ClassCastException e){}

        }
        return count;
    }

    private void setSemanticZoomLevel(int semanticZoomLevel) {
        m_zoomLevelLabel.setValue(String.valueOf(semanticZoomLevel));
        m_szlOutBtn.setEnabled(semanticZoomLevel > 0);
        m_graphContainer.setSemanticZoomLevel(semanticZoomLevel);
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
        m_showHeader = Boolean.valueOf(boolVal);
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        m_hudDisplay.setVertexSelectionCount(selectionContext.getSelectedVertexRefs().size());
        m_hudDisplay.setEdgeSelectionCount(selectionContext.getSelectedEdgeRefs().size());

        //After selection always set the pantool back to active tool
        if(m_panBtn != null && !m_panBtn.getStyleName().equals("toolbar-button down")){
            m_panBtn.setStyleName("toolbar-button down");
        }
        if(m_selectBtn != null && m_selectBtn.getStyleName().equals("toolbar-button down")){
            m_selectBtn.setStyleName("toolbar-button");
        }
        if(m_topologyComponent != null) m_topologyComponent.setActiveTool("pan");
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
        if(!selectedVertexRefs.equals(vertexRefs) && !event.allVerticesSelected()) {
            m_selectionManager.setSelectedVertexRefs(vertexRefs);
        }
    }
}
