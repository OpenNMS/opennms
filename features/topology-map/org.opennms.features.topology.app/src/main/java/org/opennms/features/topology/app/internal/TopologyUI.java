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

import static org.opennms.features.topology.api.support.VertexHopGraphProvider.getWrappedVertexHopCriteria;
import static org.opennms.features.topology.app.internal.operations.TopologySelectorOperation.createOperationForDefaultGraphProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HasExtraComponents;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.MapViewManagerListener;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.WidgetManager;
import org.opennms.features.topology.api.WidgetUpdateListener;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAwareTable;
import org.opennms.features.topology.api.info.InfoPanelItem;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultMetaInfo;
import org.opennms.features.topology.api.topo.MetaInfo;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.CommandManager.DefaultOperationContext;
import org.opennms.features.topology.app.internal.TopologyComponent.VertexUpdateListener;
import org.opennms.features.topology.app.internal.jung.TopoFRLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.RedoLayoutOperation;
import org.opennms.features.topology.app.internal.operations.TopologySelectorOperation;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.features.topology.app.internal.support.FontAwesomeIcons;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.opennms.features.topology.app.internal.ui.HudDisplay;
import org.opennms.features.topology.app.internal.ui.InfoPanel;
import org.opennms.features.topology.app.internal.ui.LastUpdatedLabel;
import org.opennms.features.topology.app.internal.ui.NoContentAvailableWindow;
import org.opennms.features.topology.app.internal.ui.SearchBox;
import org.opennms.osgi.EventConsumer;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextCreator;
import org.opennms.osgi.VaadinApplicationContextImpl;
import org.opennms.osgi.locator.OnmsServiceManagerLocator;
import org.opennms.web.api.OnmsHeaderProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wolfie.refresher.Refresher;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("topo_default")
@Title("OpenNMS Topology Map")
@PreserveOnRefresh
@StyleSheet(value = {
        "theme://font-awesome/css/font-awesome.min.css",
        "theme://ionicons/css/ionicons.min.css"
})
public class TopologyUI extends UI implements CommandUpdateListener, MenuItemUpdateListener, ContextMenuHandler, WidgetUpdateListener, WidgetContext, UriFragmentChangedListener, GraphContainer.ChangeListener, MapViewManagerListener, VertexUpdateListener, SelectionListener, VerticesUpdateManager.VerticesUpdateListener {

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

                final RedoLayoutOperation op = new RedoLayoutOperation();
                op.execute(getGraphContainer());

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

    private interface RequestParameterHandler {
        boolean handleRequest(VaadinRequest request);
    }

    /**
     * Class to handle Request Parameters, such as SZL, Vertices in Focus, Layout Selection, Graph Provider
     * Selection, Status Provider selection, etc...
     */
    private class TopologyUIRequestHandler implements RequestHandler {

        private final List<RequestParameterHandler> requestHandlerList;

        private static final String PARAMETER_LAYOUT = "layout";
        private static final String PARAMETER_FOCUS_NODES = "focusNodes";
        private static final String PARAMETER_FOCUS_VERTICES = "focus-vertices";
        private static final String PARAMETER_SEMANTIC_ZOOM_LEVEL = "szl";
        private static final String PARAMETER_GRAPH_PROVIDER = "provider";
        protected static final String PARAMETER_HISTORY_FRAGMENT = "ui-fragment";

        private TopologyUIRequestHandler() {
            requestHandlerList = Lists.newArrayList(
                    // The order matters
                    request -> loadHistoryFragment(request),
                    request -> loadGraphProvider(request),
                    request -> loadVertexHopCriteria(request),
                    request -> loadSemanticZoomLevel(request),
                    request -> loadLayout(request));
        }

        @Override
        public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
            handleRequestParameter(request);
            return false;
        }

        public void handleRequestParameter(VaadinRequest request) {
            boolean updateURL = false;
            for (RequestParameterHandler handler : requestHandlerList) {
                if (handler.handleRequest(request)) {
                    updateURL = true;
                }
            }
            // we redo the layout before we save the history
            m_graphContainer.redoLayout();

            // Close all open Windows/Dialogs if it is not the "NO VERTICES IN FOCUS"-Window
            for (Window eachWindow : getWindows()) {
                if (eachWindow != m_noContentWindow) {
                    eachWindow.close();
                }
            }

            if (updateURL) {
                // If we have a new location, we reload the page.
                // This needs to be done to set the fragment of the page correctly
                // Otherwise the Request-Parameters would still be there

                // we must overwrite the existing saved history for this user, otherwise updateURL does not work
                String fragment = m_historyManager.createHistory(m_applicationContext.getUsername(), m_graphContainer);
                LOG.info("Redirect user {} to topology fragment url with fragment {}", m_applicationContext.getUsername(), fragment);
                getPage().setLocation(String.format("%s#%s", ((VaadinServletRequest) request).getRequestURI(), fragment));
            }
        }

        private boolean loadLayout(VaadinRequest request) {
            String layoutName = request.getParameter(PARAMETER_LAYOUT);
            return executeOperationWithLabel(layoutName);
        }

        private boolean loadGraphProvider(VaadinRequest request) {
            String graphProviderName = request.getParameter(PARAMETER_GRAPH_PROVIDER);
            return executeOperationWithLabel(graphProviderName);
        }

        private boolean executeOperationWithLabel(String operationLabel) {
            final CheckedOperation operation = m_commandManager.findOperationByLabel(CheckedOperation.class, operationLabel);
            if (operation != null) {
                final DefaultOperationContext operationContext = new DefaultOperationContext(TopologyUI.this, m_graphContainer, DisplayLocation.MENUBAR);
                final List<VertexRef> targets = Collections.<VertexRef>emptyList();
                // CheckedOperations may toggle its state when execute is invoked.
                // We do not execute if already checked, as this would disable an already checked operation.
                if (!operation.isChecked(targets, operationContext)) {
                    operation.execute(targets, operationContext);
                    return true;
                }
            }
            return false;
        }

        private boolean loadHistoryFragment(VaadinRequest request) {
            String fragment = request.getParameter(PARAMETER_HISTORY_FRAGMENT);
            if (!Strings.isNullOrEmpty(fragment) && getPage() != null) {
                applyHistory(m_applicationContext.getUsername(), fragment);
                return true;
            }
            return false;
        }

        private boolean loadSemanticZoomLevel(VaadinRequest request) {
            String szl = request.getParameter(PARAMETER_SEMANTIC_ZOOM_LEVEL);
            if (szl != null) {
                try {
                    m_graphContainer.setSemanticZoomLevel(Integer.parseInt(szl));
                    return true;
                } catch (NumberFormatException e) {
                    LOG.warn("Invalid SZL found in {} parameter: {}", PARAMETER_SEMANTIC_ZOOM_LEVEL, szl);
                }
            }
            return false;
        }

        private boolean loadVertexHopCriteria(VaadinRequest request) {
            final String nodeIds = request.getParameter(PARAMETER_FOCUS_NODES);
            String vertexIdInFocus = request.getParameter(PARAMETER_FOCUS_VERTICES);
            if (nodeIds != null && vertexIdInFocus != null) {
                LOG.warn("Usage of parameter '{1}' and '{2}'. This is not supported. Skipping parameter '{2}'", PARAMETER_FOCUS_NODES, PARAMETER_FOCUS_VERTICES);
            }
            if (nodeIds != null) {
                LOG.warn("Usage of deprecated parameter '{}'. Please use '{}' instead.", PARAMETER_FOCUS_NODES, PARAMETER_FOCUS_VERTICES);
                vertexIdInFocus = nodeIds;
            }
            if (vertexIdInFocus != null) {
                // Build the VertexRef elements
                final TreeSet<VertexRef> refs = new TreeSet<>();
                for (String vertexId : vertexIdInFocus.split(",")) {
                    String namespace = m_graphContainer.getBaseTopology().getVertexNamespace();
                    Vertex vertex = m_graphContainer.getBaseTopology().getVertex(namespace, vertexId);
                    if (vertex == null) {
                        LOG.warn("Vertex with namespace {} and id {} do not exist in the selected Graph Provider {}",
                                namespace, vertexId, m_graphContainer.getBaseTopology().getClass().getSimpleName());
                    } else {
                        refs.add(vertex);
                    }
                }
                // We have to update the vertices in focus (in our case only nodes) only if the focus has changed
                VertexHopGraphProvider.VertexHopCriteria criteria = VertexHopGraphProvider.getWrappedVertexHopCriteria(m_graphContainer);
                if (!criteria.getVertices().equals(refs)) {
                    m_graphContainer.clearCriteria();
                    refs.forEach(vertexRef -> m_graphContainer.addCriteria(new VertexHopGraphProvider.DefaultVertexHopCriteria(vertexRef)));
                    m_graphContainer.setSemanticZoomLevel(1);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Helper class to load components to show in the info panel.
     */
    public class InfoPanelItemProvider implements SelectionListener, MenuItemUpdateListener, GraphContainer.ChangeListener {

        // Panel Item to visualize the selection context
        private final InfoPanelItem selectionContextPanelItem = new InfoPanelItem() {

            @Override
            public Component getComponent(GraphContainer container) {
                synchronized (m_currentHudDisplayLock) {
                    m_currentHudDisplay = new HudDisplay();
                    m_currentHudDisplay.setImmediate(true);
                    m_currentHudDisplay.setProvider(m_graphContainer.getBaseTopology().getVertexNamespace().equals("nodes") ? "Linkd" : m_graphContainer.getBaseTopology().getVertexNamespace());
                    m_currentHudDisplay.setVertexFocusCount(getFocusVertices(m_graphContainer));
                    m_currentHudDisplay.setEdgeFocusCount(0);
                    m_currentHudDisplay.setVertexSelectionCount(m_graphContainer.getSelectionManager().getSelectedVertexRefs().size());
                    m_currentHudDisplay.setEdgeSelectionCount(m_graphContainer.getSelectionManager().getSelectedEdgeRefs().size());
                    m_currentHudDisplay.setVertexContextCount(m_graphContainer.getGraph().getDisplayVertices().size());
                    m_currentHudDisplay.setEdgeContextCount(m_graphContainer.getGraph().getDisplayEdges().size());
                    m_currentHudDisplay.setVertexTotalCount(m_graphContainer.getBaseTopology().getVertexTotalCount());
                    m_currentHudDisplay.setEdgeTotalCount(m_graphContainer.getBaseTopology().getEdges().size());
                    return m_currentHudDisplay;
                }
            }

            @Override
            public boolean contributesTo(GraphContainer container) {
                // only show if no selection
                return container.getSelectionManager().getSelectedEdgeRefs().isEmpty()
                        && container.getSelectionManager().getSelectedVertexRefs().isEmpty();
            }

            @Override
            public String getTitle(GraphContainer container) {
                return "Selection Context";
            }

            @Override
            public int getOrder() {
                return 1;
            }
        };

        // Panel Item to visualize the meta info
        private final InfoPanelItem metaInfoPanelItem = new InfoPanelItem() {

            private MetaInfo getMetaInfo() {
                MetaInfo metaInfo = getGraphContainer().getBaseTopology().getMetaInfo();

                if (Objects.isNull(metaInfo)) {
                    metaInfo = new DefaultMetaInfo();
                }

                return metaInfo;
            }

            @Override
            public Component getComponent(GraphContainer container) {
                return new Label(getMetaInfo().getDescription());
            }

            @Override
            public boolean contributesTo(GraphContainer container) {
                // only show if no selection
                return container.getSelectionManager().getSelectedEdgeRefs().isEmpty()
                        && container.getSelectionManager().getSelectedVertexRefs().isEmpty();
            }

            @Override
            public String getTitle(GraphContainer container) {
                return getMetaInfo().getName();
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };

        private Component wrap(InfoPanelItem item) {
            return wrap(item.getComponent(m_graphContainer), item.getTitle(m_graphContainer));
        }

        /**
         * Wraps the provided component in order to fit it better in the Info Panel.
         * E.g. a caption is added to better difference between components.
         *
         * @param component The component to wrap.
         * @param title the title of the component to wrap.
         * @return The wrapped component.
         */
        private Component wrap(Component component, String title) {
            Label label = new Label();
            label.addStyleName("info-panel-item-label");
            if (title != null) {
                label.setValue(title);
            }

            VerticalLayout layout = new VerticalLayout();
            layout.addStyleName("info-panel-item");
            layout.addComponent(label);
            layout.addComponent(component);
            layout.setMargin(true);

            return layout;
        }

        private List<Component> getInfoPanelComponents() {
            final List<InfoPanelItem> infoPanelItems = findInfoPanelItems();
            infoPanelItems.add(selectionContextPanelItem); // manually add this, as it is not exposed via osgi
            infoPanelItems.add(metaInfoPanelItem); // same here
            return infoPanelItems.stream()
                    .filter(item -> {
                        try {
                            return item.contributesTo(m_graphContainer);
                        } catch (Throwable t) {
                            // See NMS-8394
                            LOG.error("An error occured while determining if info panel item {} should be displayed. "
                                    + "The component will not be displayed.", item.getClass(), t);
                            return false;
                        }
                    })
                    .sorted()
                    .map(item -> {
                        try {
                            return wrap(item);
                        } catch (Throwable t) {
                            // See NMS-8394
                            LOG.error("An error occured while retriveing the component from info panel item {}. "
                                    + "The component will not be displayed.", item.getClass(), t);
                            return null;
                        }
                    })
                    .filter(component -> component != null) // Skip any nulls from components with exceptions
                    .collect(Collectors.toList());
        }

        private List<InfoPanelItem> findInfoPanelItems() {
            try {
                return m_bundlecontext.getServiceReferences(InfoPanelItem.class, null).stream()
                        .map(eachRef -> m_bundlecontext.getService(eachRef))
                        .collect(Collectors.toList());
            } catch (InvalidSyntaxException e) {
                LOG.error(e.getMessage(), e);
                return Collections.emptyList();
            }
        }

        private void refreshInfoPanel() {
            List<Component> components = Lists.newArrayList();
            components.addAll(getInfoPanelComponents());
            m_infoPanel.setDynamicComponents(components);
        }

        @Override
        public void selectionChanged(SelectionContext selectionContext) {
            refreshInfoPanel();
        }

        @Override
        public void updateMenuItems() {
            refreshInfoPanel();
        }

        @Override
        public void graphChanged(GraphContainer graphContainer) {
            refreshInfoPanel();
        }
    }

    private static final long serialVersionUID = 6837501987137310938L;
    private static final Logger LOG = LoggerFactory.getLogger(TopologyUI.class);

    private TopologyComponent m_topologyComponent;
    private Window m_noContentWindow;
    private InfoPanel m_infoPanel;
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
    private OnmsServiceManager m_serviceManager;
    private VaadinApplicationContext m_applicationContext;
    private VerticesUpdateManager m_verticesUpdateManager;
    private Button m_panBtn;
    private Button m_selectBtn;
    private Button m_szlOutBtn;
    private LastUpdatedLabel m_lastUpdatedTimeLabel;
    int m_settingFragment = 0;
    private SearchBox m_searchBox;
    private TabSheet tabSheet;
    private BundleContext m_bundlecontext;
    private final Object m_currentHudDisplayLock = new Object();
    private HudDisplay m_currentHudDisplay;

    private String getHeader(HttpServletRequest request) throws Exception {
        if(m_headerProvider == null) {
            return "";
        } else {
            return m_headerProvider.getHeaderHtml(request);
        }
    }

    public TopologyUI(CommandManager commandManager, HistoryManager historyManager, GraphContainer graphContainer, IconRepositoryManager iconRepoManager) {
        // Ensure that selection changes trigger a history save operation
        m_commandManager = commandManager;
        m_historyManager = historyManager;
        m_iconRepositoryManager = iconRepoManager;

        // We set it programmatically, as we require a GraphContainer instance per Topology UI instance.
        // Scope Prototype would create too many GraphContainers, as scope singleton would create too few.
        m_selectionManager = new DefaultSelectionManager(graphContainer);

        // Create a per-session GraphContainer instance
        m_graphContainer = graphContainer;
        m_graphContainer.setSelectionManager(m_selectionManager);
        m_graphContainer.setIconManager(m_iconRepositoryManager);
    }

	@Override
    protected void init(final VaadinRequest request) {
        try {
            m_headerHtml = getHeader(((VaadinServletRequest) request).getHttpServletRequest());
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
        m_serviceManager.getEventRegistry().addPossibleEventConsumer(this, m_applicationContext);

        // Set the algorithm last so that the criteria and SZLs are
        // in place before we run the layout algorithm.
        m_graphContainer.setSessionId(m_applicationContext.getSessionId());
        m_graphContainer.setLayoutAlgorithm(new TopoFRLayoutAlgorithm());

        createLayouts();
        setupErrorHandler(); // Set up an error handler for UI-level exceptions
        setupAutoRefresher(); // Add an auto refresh handler to the GraphContainer

        loadUserSettings();
        // the layout must be created BEFORE loading the hop criteria and the semantic zoom level
        TopologyUIRequestHandler handler = new TopologyUIRequestHandler();
        getSession().addRequestHandler(handler); // Add a request handler that parses incoming focusNode and szl query parameters
        handler.handleRequestParameter(request); // deal with those in init case

        // Add the default criteria if we do not have already a criteria set
        if (getWrappedVertexHopCriteria(m_graphContainer).isEmpty() && noAdditionalFocusCriteria()) {
            m_graphContainer.addCriteria(m_graphContainer.getBaseTopology().getDefaultCriteria()); // set default
        }

        // If no Topology Provider was selected (due to loadUserSettings(), fallback to default
        if (m_graphContainer.getBaseTopology() == null
                || m_graphContainer.getBaseTopology() == MergingGraphProvider.NULL_PROVIDER) {
            TopologySelectorOperation defaultTopologySelectorOperation = createOperationForDefaultGraphProvider(m_bundlecontext, "(|(label=Enhanced Linkd)(label=Linkd))");
            Objects.requireNonNull(defaultTopologySelectorOperation, "No default GraphProvider found."); // no default found, abort
            defaultTopologySelectorOperation.execute(m_graphContainer);
        }

        // We set the listeners at the end, to not fire them all the time when initializing the UI
        setupListeners();

        // We force a reload of the topology provider as it may not have been initialized
        m_graphContainer.getBaseTopology().refresh();

        // We force a reload to trigger a fireGraphChanged()
        m_graphContainer.setDirty(true);
        m_graphContainer.redoLayout();

        // Trigger a selectionChanged
        m_selectionManager.selectionChanged(m_selectionManager);
    }

    private void setupListeners() {
        getPage().addUriFragmentChangedListener(this);
        m_selectionManager.addSelectionListener(this);
        m_graphContainer.addChangeListener(this);
        m_graphContainer.getMapViewManager().addListener(this);
        m_commandManager.addMenuItemUpdateListener(this);
        m_commandManager.addCommandUpdateListener(this);

        m_graphContainer.addChangeListener(m_searchBox);
        m_selectionManager.addSelectionListener(m_searchBox);

        m_graphContainer.addChangeListener(m_verticesUpdateManager);
        m_selectionManager.addSelectionListener(m_verticesUpdateManager);

        // Register the Info Panel to listen for certain events
        final InfoPanelItemProvider infoPanelItemProvider = new InfoPanelItemProvider();
        m_selectionManager.addSelectionListener(infoPanelItemProvider);
        m_commandManager.addMenuItemUpdateListener(infoPanelItemProvider);
        m_graphContainer.addChangeListener(infoPanelItemProvider);
    }

    private boolean noAdditionalFocusCriteria() {
        Criteria[] crits = m_graphContainer.getCriteria();
        for(Criteria criteria : crits){
            if (criteria instanceof CategoryHopCriteria) {
                return false;
            }
        }
        return true;
    }

    private void createLayouts() {
        m_rootLayout = new VerticalLayout();
        m_rootLayout.setSizeFull();
        m_rootLayout.addStyleName("root-layout");
        m_rootLayout.addStyleName("topo-root-layout");
        setContent(m_rootLayout);

        addHeader();

        addContentLayout();

        addNoContentWindow();
    }

    private void addNoContentWindow() {
        m_noContentWindow = new NoContentAvailableWindow(m_graphContainer);
        m_noContentWindow.setVisible(true);
        addWindow(m_noContentWindow);
    }

    private void setupErrorHandler() {
        setErrorHandler(new DefaultErrorHandler() {

            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Notification.show("An Exception Occurred: see karaf.log", Notification.Type.TRAY_NOTIFICATION);
                LOG.warn("An Exception Occurred: in the TopologyUI", event.getThrowable());

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

        //Don't create a horizontal Split container here, no need. Remove and use the absolute
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

    private Component createMapLayout() {
        final Property<Double> scale = m_graphContainer.getScaleProperty();

        m_lastUpdatedTimeLabel = new LastUpdatedLabel();
        m_lastUpdatedTimeLabel.setImmediate(true);

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
        m_panBtn.setIcon(FontAwesome.ARROWS);
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
        VerticalLayout toollayout = new VerticalLayout();
        toollayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        toollayout.setSpacing(true);

        toollayout.addComponent(historyCtrlLayout);
        toollayout.addComponent(locationToolLayout);
        toollayout.addComponent(showFocusVerticesBtn);
        toollayout.addComponent(sliderLayout);
        toollayout.addComponent(controlLayout);
        toollayout.addComponent(semanticCtrlLayout);

        AbsoluteLayout mapLayout = new AbsoluteLayout();
        mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
        mapLayout.addComponent(m_lastUpdatedTimeLabel, "top: 5px; right: 10px;");
        mapLayout.addComponent(toollayout, "top: 25px; right: 10px;");
        mapLayout.setSizeFull();

        m_infoPanel = new InfoPanel(m_searchBox, mapLayout);
        return m_infoPanel;
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

    // See if the history manager has an existing fragment stored for
    // this user. Do this before laying out the UI because the history
    // may change during layout.
    private void loadUserSettings() {
        applyHistory(m_applicationContext.getUsername(), m_historyManager.getHistoryHash(m_applicationContext.getUsername()));
        m_graphContainer.redoLayout();
    }

    private void applyHistory(String username, String fragment) {
        // If there was existing history, then restore that history snapshot.
        if (fragment != null) {
            LoggerFactory.getLogger(this.getClass()).info("Restoring history for user {}: {}", username, fragment);
            if (getPage() != null) {
                getPage().setUriFragment(fragment);
            }
            m_historyManager.applyHistory(username, fragment, m_graphContainer);
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
                    updateTabVisibility();
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
        
        tabSheet = new TabSheet();
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

    private void updateTabVisibility() {
        for (int i=0; i<tabSheet.getComponentCount(); i++) {
            TabSheet.Tab tab = tabSheet.getTab(i);
            if (tab.getComponent() instanceof SelectionAwareTable) {
                ContentType contentType = ((SelectionAwareTable) tab.getComponent()).getContentType();
                boolean visible = m_graphContainer.getBaseTopology().contributesTo(contentType);
                tab.setVisible(visible);
            }
        }
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

        // Add Menu Item to share the View with others
        m_menuBar.addItem("Share", FontAwesome.SHARE, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuItem selectedItem) {
                // create the share link
                String fragment = getPage().getLocation().getFragment();
                String url = getPage().getLocation().toString().replace("#" + getPage().getLocation().getFragment(), "");
                String shareLink = String.format("%s?%s=%s", url, TopologyUIRequestHandler.PARAMETER_HISTORY_FRAGMENT, fragment);

                // Create the Window
                Window shareWindow = new Window();
                shareWindow.setCaption("Share Link");
                shareWindow.setModal(true);
                shareWindow.setClosable(true);
                shareWindow.setResizable(false);
                shareWindow.setWidth(400, Unit.PIXELS);

                TextArea shareLinkField = new TextArea();
                shareLinkField.setValue(shareLink);
                shareLinkField.setReadOnly(true);
                shareLinkField.setRows(3);
                shareLinkField.setWidth(100, Unit.PERCENTAGE);

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
            }
        });

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
        m_historyManager.applyHistory(m_applicationContext.getUsername(), fragment, m_graphContainer);

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
            String fragment = m_historyManager.createHistory(m_applicationContext.getUsername(), m_graphContainer);
            if (getPage() != null) {
                getPage().setUriFragment(fragment, false);
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

        m_zoomLevelLabel.setValue(String.valueOf(graphContainer.getSemanticZoomLevel()));
        m_szlOutBtn.setEnabled(graphContainer.getSemanticZoomLevel() > 0);
        updateTabVisibility();
        updateTimestamp(System.currentTimeMillis());
        updateMenuItems();

        synchronized (m_currentHudDisplayLock) {
            if (m_currentHudDisplay != null) {
                m_currentHudDisplay.setVertexFocusCount(getFocusVertices(m_graphContainer));
            }
        }
    }

    private int getFocusVertices(GraphContainer graphContainer) {
        int count = 0;
        Criteria[] crits = graphContainer.getCriteria();
        for(Criteria criteria : crits){
            try{
                VertexHopCriteria catCrit = (VertexHopCriteria) criteria;
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
        synchronized (m_currentHudDisplayLock) {
            if (m_currentHudDisplay != null) {
                m_currentHudDisplay.setVertexSelectionCount(selectionContext.getSelectedVertexRefs().size());
                m_currentHudDisplay.setEdgeSelectionCount(selectionContext.getSelectedEdgeRefs().size());
            }
        }

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
        this.m_bundlecontext = bundleContext;
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
