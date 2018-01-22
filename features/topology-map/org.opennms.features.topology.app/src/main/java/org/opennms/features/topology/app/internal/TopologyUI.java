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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultTopologyProviderInfo;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.TopologyComponent.VertexUpdateListener;
import org.opennms.features.topology.app.internal.menu.MenuUpdateListener;
import org.opennms.features.topology.app.internal.menu.OperationManager;
import org.opennms.features.topology.app.internal.menu.TopologyContextMenu;
import org.opennms.features.topology.app.internal.menu.TopologyMenuBar;
import org.opennms.features.topology.app.internal.operations.RedoLayoutOperation;
import org.opennms.features.topology.app.internal.service.NoSuchProviderException;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.features.topology.app.internal.ui.HudDisplay;
import org.opennms.features.topology.app.internal.ui.InfoPanel;
import org.opennms.features.topology.app.internal.ui.LayoutHintComponent;
import org.opennms.features.topology.app.internal.ui.NoContentAvailableWindow;
import org.opennms.features.topology.app.internal.ui.SearchBox;
import org.opennms.features.topology.app.internal.ui.ToolbarPanel;
import org.opennms.features.topology.app.internal.ui.ToolbarPanelController;
import org.opennms.features.topology.app.internal.ui.breadcrumbs.BreadcrumbComponent;
import org.opennms.features.topology.link.TopologyLinkBuilder;
import org.opennms.netmgt.vaadin.core.ConfirmationDialog;
import org.opennms.osgi.EventConsumer;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextCreator;
import org.opennms.osgi.VaadinApplicationContextImpl;
import org.opennms.osgi.locator.OnmsServiceManagerLocator;
import org.opennms.web.api.OnmsHeaderProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.github.wolfie.refresher.Refresher;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("topo_default")
@Title("OpenNMS Topology Map")
@PreserveOnRefresh
public class TopologyUI extends UI implements MenuUpdateListener, ContextMenuHandler, WidgetUpdateListener, WidgetContext, UriFragmentChangedListener, GraphContainer.ChangeListener, MapViewManagerListener, VertexUpdateListener, SelectionListener, VerticesUpdateManager.VerticesUpdateListener {

    private class DynamicUpdateRefresher implements Refresher.RefreshListener {
        private final Object lockObject = new Object();
        private boolean m_refreshInProgress = false;


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
    public class TopologyUIRequestHandler implements RequestHandler {

        private final List<RequestParameterHandler> requestHandlerList;

        private static final String PARAMETER_FOCUS_NODES = "focusNodes";

        public static final String PARAMETER_HISTORY_FRAGMENT = "ui-fragment";

        private TopologyUIRequestHandler() {
            requestHandlerList = Lists.newArrayList(
                    // The order matters
                    this::loadHistoryFragment,
                    this::loadGraphProvider,
                    this::loadVertexHopCriteria,
                    this::loadSemanticZoomLevel,
                    this::loadLayout);
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
            m_topologyComponent.getState().setPhysicalWidth(0);
            m_topologyComponent.getState().setPhysicalHeight(0);
            m_topologyComponent.markAsDirtyRecursive();

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
                String fragment = m_historyManager.saveOrUpdateHistory(m_applicationContext.getUsername(), m_graphContainer);
                LOG.info("Redirect user {} to topology fragment url with fragment {}", m_applicationContext.getUsername(), fragment);
                getPage().setLocation(String.format("%s#%s", ((VaadinServletRequest) request).getRequestURI(), fragment));
            }
        }

        private boolean loadLayout(VaadinRequest request) {
            String layoutName = request.getParameter(TopologyLinkBuilder.PARAMETER_LAYOUT);
            return executeOperationWithLabel(layoutName);
        }

        private boolean loadGraphProvider(VaadinRequest request) {
            String graphProviderName = request.getParameter(TopologyLinkBuilder.PARAMETER_GRAPH_PROVIDER);
            return executeOperationWithLabel(graphProviderName);
        }

        private boolean executeOperationWithLabel(String operationLabel) {
            final CheckedOperation operation = m_operationManager.findOperationByLabel(operationLabel);
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
            String szl = request.getParameter(TopologyLinkBuilder.PARAMETER_SEMANTIC_ZOOM_LEVEL);
            if (szl != null) {
                try {
                    m_graphContainer.setSemanticZoomLevel(Integer.parseInt(szl));
                    return true;
                } catch (NumberFormatException e) {
                    LOG.warn("Invalid SZL found in {} parameter: {}", TopologyLinkBuilder.PARAMETER_SEMANTIC_ZOOM_LEVEL, szl);
                }
            }
            return false;
        }

        private boolean loadVertexHopCriteria(VaadinRequest request) {
            final String nodeIds = request.getParameter(PARAMETER_FOCUS_NODES);
            String vertexIdInFocus = request.getParameter(TopologyLinkBuilder.PARAMETER_FOCUS_VERTICES);
            if (nodeIds != null && vertexIdInFocus != null) {
                LOG.warn("Usage of parameter '{1}' and '{2}'. This is not supported. Skipping parameter '{2}'", PARAMETER_FOCUS_NODES, TopologyLinkBuilder.PARAMETER_FOCUS_VERTICES);
            }
            if (nodeIds != null) {
                LOG.warn("Usage of deprecated parameter '{}'. Please use '{}' instead.", PARAMETER_FOCUS_NODES, TopologyLinkBuilder.PARAMETER_FOCUS_VERTICES);
                vertexIdInFocus = nodeIds;
            }
            if (vertexIdInFocus != null) {
                // Build the VertexRef elements
                final TreeSet<VertexRef> refs = new TreeSet<>();
                for (String vertexId : vertexIdInFocus.split(",")) {
                    String namespace = m_graphContainer.getTopologyServiceClient().getNamespace();
                    Vertex vertex = m_graphContainer.getTopologyServiceClient().getVertex(namespace, vertexId);
                    if (vertex == null) {
                        LOG.warn("Vertex with namespace {} and id {} do not exist in the selected Graph Provider {}",
                                namespace, vertexId, m_graphContainer.getTopologyServiceClient().getClass().getSimpleName());
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
    public class InfoPanelItemManager implements SelectionListener, MenuUpdateListener, GraphContainer.ChangeListener {

        // Panel InfoPanelItem to visualize the selection context
        private final InfoPanelItemProvider selectionContextPanelItem = container -> {
            // Only contribute if no selection
            return (container.getSelectionManager().getSelectedEdgeRefs().isEmpty() &&
                    container.getSelectionManager().getSelectedVertexRefs().isEmpty())
                    ? Collections.<InfoPanelItem>singleton(new InfoPanelItem() {
                            @Override
                            public Component getComponent() {
                                m_currentHudDisplay = new HudDisplay();
                                m_currentHudDisplay.setImmediate(true);
                                m_currentHudDisplay.setProvider(m_graphContainer.getTopologyServiceClient().getNamespace().equals("nodes")
                                        ? "Linkd"
                                        : m_graphContainer.getTopologyServiceClient().getNamespace());
                                m_currentHudDisplay.setVertexFocusCount(getFocusVertices(m_graphContainer));
                                m_currentHudDisplay.setEdgeFocusCount(0);
                                m_currentHudDisplay.setVertexSelectionCount(m_graphContainer.getSelectionManager().getSelectedVertexRefs().size());
                                m_currentHudDisplay.setEdgeSelectionCount(m_graphContainer.getSelectionManager().getSelectedEdgeRefs().size());
                                m_currentHudDisplay.setVertexContextCount(m_graphContainer.getGraph().getDisplayVertices().size());
                                m_currentHudDisplay.setEdgeContextCount(m_graphContainer.getGraph().getDisplayEdges().size());
                                m_currentHudDisplay.setVertexTotalCount(m_graphContainer.getTopologyServiceClient().getVertexTotalCount());
                                m_currentHudDisplay.setEdgeTotalCount(m_graphContainer.getTopologyServiceClient().getEdgeTotalCount());
                                return m_currentHudDisplay;
                            }

                            @Override
                            public String getTitle() {
                                return "Selection Context";
                            }

                            @Override
                            public int getOrder() {
                                return 2;
                            }
                        })
                   : Collections.emptySet();
        };

        // Panel Item to visualize the meta info
        private final InfoPanelItemProvider topologyProviderInfoPanelItem = (container) -> {
            TopologyProviderInfo metaInfo = Optional.ofNullable(getGraphContainer().getTopologyServiceClient().getInfo())
                    .orElseGet(DefaultTopologyProviderInfo::new);

            // Only contribute if no selection
            return (container.getSelectionManager().getSelectedEdgeRefs().isEmpty() &&
                    container.getSelectionManager().getSelectedVertexRefs().isEmpty())
                    ? Collections.singleton(new DefaultInfoPanelItem()
                    .withOrder(0)
                    .withId("topologyInfo")
                    .withTitle(metaInfo.getName())
                    .withComponent(new Label(metaInfo.getDescription())))
                    : Collections.emptySet();
        };

        private Component wrap(final InfoPanelItem item) {
            return wrap(item.getComponent(), item.getTitle(), item.getId());
        }

        /**
         * Wraps the provided component in order to fit it better in the Info Panel.
         * E.g. a caption is added to better difference between components.
         *
         * @param component The component to wrap.
         * @param title the title of the component to wrap.
         * @param id the id of the wrapped component.
         * @return The wrapped component.
         */
        private Component wrap(Component component, String title, String id) {
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
            if (id != null) {
                layout.setId(id);
            }
            return layout;
        }

        private List<Component> getInfoPanelComponents() {
            final List<InfoPanelItemProvider> infoPanelItemProviders = findInfoPanelItems();
            infoPanelItemProviders.add(selectionContextPanelItem); // manually add this, as it is not exposed via osgi
            infoPanelItemProviders.add(topologyProviderInfoPanelItem); // same here
            return m_transactionOperations.execute(ts -> {
                return infoPanelItemProviders.stream()
                                             .flatMap(provider -> {
                                                 try {
                                                     return provider.getContributions(m_graphContainer).stream();
                                                 } catch (Throwable t) {
                                                     // See NMS-8394
                                                     LOG.error("An error occurred while retrieving the component from info panel item provider {}. "
                                                               + "The component will not be displayed.", provider.getClass(), t);
                                                     return null;
                                                 }
                                             })
                                             .filter(Objects::nonNull)
                                             .sorted()
                                             .map(this::wrap)
                                             .collect(Collectors.toList());
            });
        }

        private List<InfoPanelItemProvider> findInfoPanelItems() {
            try {
                return m_bundlecontext.getServiceReferences(InfoPanelItemProvider.class, null).stream()
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
        public void updateMenu() {
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
    private BreadcrumbComponent m_breadcrumbComponent;
    private LayoutHintComponent m_layoutHintComponent;
    private final GraphContainer m_graphContainer;
    private SelectionManager m_selectionManager;
    private final OperationManager m_operationManager;
    private TopologyContextMenu m_contextMenu;
    private TopologyMenuBar m_menuBar;
    private VerticalLayout m_layout;
    private VerticalLayout m_rootLayout;
    private final IconRepositoryManager m_iconRepositoryManager;
    private WidgetManager m_widgetManager;
    private Component m_mapLayout;
    private final HistoryManager m_historyManager;
    private String m_headerHtml;
    private boolean m_showHeader = true;
    private OnmsHeaderProvider m_headerProvider = null;
    private OnmsServiceManager m_serviceManager;
    private VaadinApplicationContext m_applicationContext;
    private VerticesUpdateManager m_verticesUpdateManager;
    private int m_settingFragment = 0;
    private SearchBox m_searchBox;
    private TabSheet tabSheet;
    private BundleContext m_bundlecontext;
    private HudDisplay m_currentHudDisplay;
    private ToolbarPanel m_toolbarPanel;
    private TransactionOperations m_transactionOperations;
    private final LayoutManager m_layoutManager;

    private String getHeader(HttpServletRequest request) throws Exception {
        if(m_headerProvider == null) {
            return "";
        } else {
            return m_headerProvider.getHeaderHtml(request);
        }
    }

    public TopologyUI(OperationManager operationManager, HistoryManager historyManager, GraphContainer graphContainer, IconRepositoryManager iconRepoManager, LayoutManager layoutManager, TransactionOperations transactionOperations) {
        // Ensure that selection changes trigger a history save operation
        m_operationManager = operationManager;
        m_historyManager = historyManager;
        m_iconRepositoryManager = iconRepoManager;
        m_layoutManager = layoutManager;
        m_transactionOperations = transactionOperations;

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
        // Register a cleanup
        request.getService().addSessionDestroyListener((SessionDestroyListener) event -> m_widgetManager.removeUpdateListener(TopologyUI.this));

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
        m_graphContainer.setApplicationContext(m_applicationContext);

        createLayouts();
        setupErrorHandler(); // Set up an error handler for UI-level exceptions
        setupAutoRefresher(); // Add an auto refresh handler to the GraphContainer
        loadUserSettings();

        // If no Topology Provider was selected (due to loadUserSettings(), fallback to default
        if (Strings.isNullOrEmpty(m_graphContainer.getMetaTopologyId())) {
            CheckedOperation defaultTopologySelectorOperation = getDefaultTopologySelectorOperation(m_bundlecontext);
            Objects.requireNonNull(defaultTopologySelectorOperation, "No default GraphProvider found."); // no default found, abort
            defaultTopologySelectorOperation.execute(Lists.newArrayList(), new DefaultOperationContext(TopologyUI.this, m_graphContainer, DisplayLocation.MENUBAR));
        }

        // Add a request handler that parses incoming focusNode and szl query parameters
        TopologyUIRequestHandler handler = new TopologyUIRequestHandler();
        getSession().addRequestHandler(handler);
        handler.handleRequestParameter(request); // deal with those in init case

        // Add the default criteria if we do not have already a criteria set
        if (getWrappedVertexHopCriteria(m_graphContainer).isEmpty() && noAdditionalFocusCriteria()) {
            List<Criteria> defaultCriteriaList = m_graphContainer.getTopologyServiceClient().getDefaults().getCriteria();
            if (defaultCriteriaList != null) {
                defaultCriteriaList.forEach(m_graphContainer::addCriteria); // set default
            }
        }

        // We set the listeners at the end, to not fire them all the time when initializing the UI
        setupListeners();

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
        m_menuBar.addMenuItemUpdateListener(this);
        m_contextMenu.addMenuItemUpdateListener(this);

        m_graphContainer.addChangeListener(m_searchBox);
        m_selectionManager.addSelectionListener(m_searchBox);

        m_graphContainer.addChangeListener(m_verticesUpdateManager);
        m_selectionManager.addSelectionListener(m_verticesUpdateManager);

        // Register the Info Panel to listen for certain events

        final InfoPanelItemManager infoPanelItemManager = new InfoPanelItemManager();
        m_selectionManager.addSelectionListener(infoPanelItemManager);
        m_menuBar.addMenuItemUpdateListener(infoPanelItemManager);
        m_contextMenu.addMenuItemUpdateListener(infoPanelItemManager);
        m_graphContainer.addChangeListener(infoPanelItemManager);

        // Register the Toolbar Panel
        m_graphContainer.addChangeListener(m_toolbarPanel);
        m_selectionManager.addSelectionListener(m_toolbarPanel);

        // Register the Breadcrumb Panel
        m_graphContainer.addChangeListener(m_breadcrumbComponent);

        // Register layout hint component
        m_graphContainer.addChangeListener(m_layoutHintComponent);
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
                Throwable t = findNoSuchProviderException(event.getThrowable());
                if (t instanceof NoSuchProviderException) {
                    final NoSuchProviderException exception = (NoSuchProviderException) t;
                    LOG.warn("Access to a graph/meta topology provider was made, which does not exist anymore: The error message was: {} Don't worry, I know what to do.", exception.getMessage());
                    new ConfirmationDialog()
                            .withCaption("Selected topology no longer available")
                            .withCancelButton(false)
                            .withDescription(() -> {
                                CheckedOperation defaultTopologySelectorOperation = getDefaultTopologySelectorOperation(m_bundlecontext);
                                if (defaultTopologySelectorOperation != null) {
                                    return "Clicking okay will switch to the default topology provider.";
                                } else {
                                    return "Please log out and log in again to resolve the issue.";
                                }
                            })
                            .withOkAction(window -> {
                                // If possible, select the default topology. Otherwise there is nothing we can do
                                CheckedOperation defaultTopologySelectorOperation = getDefaultTopologySelectorOperation(m_bundlecontext);
                                if (defaultTopologySelectorOperation != null) {
                                    defaultTopologySelectorOperation.execute(Lists.newArrayList(), new DefaultOperationContext(TopologyUI.this, m_graphContainer, DisplayLocation.MENUBAR));
                                } else {
                                    // Invalidate the ui state, which enforces the user to reload the ui
                                    // (hopefully she logged out as we suggested, otherwise an error is shown)
                                    UI.getCurrent().close();
                                }
                            })
                            .open();
                } else {
                    Notification.show("An Unexpected Exception Occurred: see karaf.log", Notification.Type.TRAY_NOTIFICATION);
                    LOG.warn("An Unexpected Exception Occurred: in the TopologyUI", event.getThrowable());
                }
            }

            private Throwable findNoSuchProviderException(Throwable t) {
                while (t != null && !(t instanceof NoSuchProviderException)) {
                    t = t.getCause();
                }
                return t;
            }
        });
    }

    private void setupAutoRefresher() {
        if (m_graphContainer.hasAutoRefreshSupport()) {
            Refresher refresher = new Refresher();
            refresher.setRefreshInterval((int) m_graphContainer.getAutoRefreshSupport().getInterval() * 1000); // ask every <interval> seconds for changes
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

        m_mapLayout = createMapLayout();
        m_mapLayout.setSizeFull();

        m_menuBar = new TopologyMenuBar();
        m_contextMenu = new TopologyContextMenu();
        updateMenu();
        if(m_widgetManager.widgetCount() != 0) {
            updateWidgetView(m_widgetManager);
        }else {
            m_layout.addComponent(m_mapLayout);
        }
        // Set expand ratio so that extra space is not allocated to this vertical component
        if (m_showHeader) {
            m_rootLayout.addComponent(m_menuBar, 1);
        } else {
            m_rootLayout.addComponent(m_menuBar, 0);
        }
    }

    private Component createMapLayout() {
        // Topology (Map) Component
        m_topologyComponent = new TopologyComponent(m_graphContainer, m_iconRepositoryManager, this);
        m_topologyComponent.setSizeFull();
        m_topologyComponent.addMenuItemStateListener(this);
        m_topologyComponent.addVertexUpdateListener(this);

        // Search Box
        m_searchBox = new SearchBox(m_serviceManager, new DefaultOperationContext(this, m_graphContainer, OperationContext.DisplayLocation.SEARCH));

        // Info Panel
        m_infoPanel = new InfoPanel(m_searchBox);

        // Breadcrumb
        m_breadcrumbComponent = new BreadcrumbComponent();

        // Layout hint
        m_layoutHintComponent = new LayoutHintComponent(m_layoutManager, m_graphContainer);

        // Toolbar
        m_toolbarPanel = new ToolbarPanel(new ToolbarPanelController() {
            @Override
            public void refreshUI() {
                new TopologyUI.DynamicUpdateRefresher().refreshUI();
            }

            @Override
            public void saveHistory() {
                TopologyUI.this.saveHistory();
            }

            @Override
            public void saveLayout() {
                m_graphContainer.saveLayout();
            }

            @Override
            public void setActiveTool(ActiveTool activeTool) {
                Objects.requireNonNull(activeTool);
                m_topologyComponent.setActiveTool(activeTool.name());
            }

            @Override
            public void showAllMap() {
                m_topologyComponent.showAllMap();
            }

            @Override
            public void centerMapOnSelection() {
                m_topologyComponent.centerMapOnSelection();
            }

            @Override
            public void toggleHighlightFocus() {
                m_topologyComponent.getState().setHighlightFocus(!m_topologyComponent.getState().isHighlightFocus());
                m_topologyComponent.updateGraph();
            }

            @Override
            public void setSemanticZoomLevel(int semanticZoomLevel) {
                m_graphContainer.setSemanticZoomLevel(semanticZoomLevel);
                m_graphContainer.redoLayout();
            }

            public int getSemanticZoomLevel() {
                return m_graphContainer.getSemanticZoomLevel();
            }

            @Override
            public Property<Double> getScaleProperty() {
                return m_graphContainer.getScaleProperty();
            }

            @Override
            public LayoutManager getLayoutManager() {
                return m_layoutManager;
            }
        });

        // Map Layout (we need to wrap it in an absolute layout otherwise it shows up twice on the topology map)
        AbsoluteLayout mapLayout = new AbsoluteLayout();
        mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
        mapLayout.addComponent(m_breadcrumbComponent, "top:10px; left: 50px");
        mapLayout.addComponent(m_layoutHintComponent, "bottom: 10px; left:20px");
        mapLayout.setSizeFull();

        HorizontalLayout layout = new HorizontalLayout();
        layout.addStyleName("map-layout");
        layout.addComponent(m_infoPanel);
        layout.addComponent(mapLayout);
        layout.addComponent(m_toolbarPanel);
        layout.setExpandRatio(mapLayout, 1);
        layout.setSizeFull();

        return layout;
    }

    // See if the history manager has an existing fragment stored for
    // this user. Do this before laying out the UI because the history
    // may change during layout.
    private void loadUserSettings() {
        applyHistory(m_applicationContext.getUsername(), m_historyManager.getHistoryFragment(m_applicationContext.getUsername()));
        m_graphContainer.redoLayout();
    }

    private void applyHistory(String username, String fragment) {
        // If there was existing history, then restore that history snapshot.
        if (fragment != null) {
            LoggerFactory.getLogger(this.getClass()).info("Restoring history for user {}: {}", username, fragment);
            if (getPage() != null) {
                getPage().setUriFragment(fragment);
            }
            m_historyManager.applyHistory(fragment, m_graphContainer);
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
        synchronized (m_layout) {
            m_layout.removeAllComponents();
            if(widgetManager.widgetCount() == 0) {
                m_layout.addComponent(m_mapLayout);
            } else {
                VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
                bottomLayoutBar.setFirstComponent(m_mapLayout);
                // Split the screen 70% top, 30% bottom
                bottomLayoutBar.setSplitPosition(70, Unit.PERCENTAGE);
                bottomLayoutBar.setSizeFull();
                bottomLayoutBar.setSecondComponent(getTabSheet(widgetManager, this));
                bottomLayoutBar.addSplitterClickListener((event) -> {
                    if (event.isDoubleClick()) {
                        if (bottomLayoutBar.getSplitPosition() == 100) {
                            bottomLayoutBar.setSplitPosition(70, Unit.PERCENTAGE);
                        } else {
                            bottomLayoutBar.setSplitPosition(100, Unit.PERCENTAGE);
                        }
                    }
                });
                m_layout.addComponent(bottomLayoutBar);
                updateTabVisibility();
            }
            m_layout.markAsDirty();
        }
        m_layout.markAsDirty();
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
            try {
                m_graphContainer.addChangeListener((GraphContainer.ChangeListener) view);
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
                boolean visible = m_graphContainer.getTopologyServiceClient().contributesTo(contentType);
                tab.setVisible(visible);
            }
        }
    }

	@Override
	public void updateMenu() {
        if(m_menuBar != null) {
            m_menuBar.updateMenu(m_graphContainer, this, m_operationManager);
        }
	}

	@Override
	public void showContextMenu(Object target, int left, int top) {
        Collection<VertexRef> selectedVertexRefs = getGraphContainer().getSelectionManager().getSelectedVertexRefs();
        List<VertexRef> targets;
        // If the user right-clicks on a vertex that is already selected...
        if(selectedVertexRefs.contains(target)) {
            // ... then use the entire selection as the target of the operation
            targets = Lists.newArrayList(selectedVertexRefs);
        } else{
            // Otherwise, just use the single vertex that was right-clicked on as the target
            targets = TopologyContextMenu.asVertexList(target);
        }

        // The target must be set before we update the operation context because the op context
        // operations are dependent on the target of the right-click
        // we have to generate the context menu here
        m_contextMenu.updateMenu(m_graphContainer, this, m_operationManager, targets);
        m_contextMenu.setAsContextMenuOf(this);
		m_contextMenu.open(left, top);
	}

    public WidgetManager getWidgetManager() {
        return m_widgetManager;
    }

    public void setWidgetManager(WidgetManager widgetManager) {
        m_widgetManager = widgetManager;
        m_widgetManager.addUpdateListener(this);
    }

    @Override
    public void widgetListUpdated(WidgetManager widgetManager) {
        if(isAttached()) {
            updateWidgetView(widgetManager);
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
        m_historyManager.applyHistory(fragment, m_graphContainer);

        m_graphContainer.redoLayout();

        m_settingFragment--;
    }

    private void saveHistory() {
        if (m_settingFragment == 0) {
            String fragment = m_historyManager.saveOrUpdateHistory(m_applicationContext.getUsername(), m_graphContainer);
            if (getPage() != null) {
                getPage().setUriFragment(fragment, false);
            }
        }
    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        // are there any vertices to display?
        boolean verticesAvailable = !graphContainer.getGraph().getDisplayVertices().isEmpty();
        boolean collapsibleCriteriaInFocus = hasCollapsibleCriteriaInFocus(graphContainer);

        // toggle view
        if (verticesAvailable || collapsibleCriteriaInFocus) {
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

        updateTabVisibility();
        updateMenu();

        if (m_currentHudDisplay != null) {
            m_currentHudDisplay.setVertexFocusCount(getFocusVertices(m_graphContainer));
        }
    }

    private boolean hasCollapsibleCriteriaInFocus(GraphContainer graphContainer) {
        for (Criteria criteria : graphContainer.getCriteria()) {
            if (criteria instanceof CollapsibleCriteria) {
                return true;
            }
        }
        return false;
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

    @Override
    public void boundingBoxChanged(MapViewManager viewManager) {
        saveHistory();
    }

    @Override
    public void onVertexUpdate() {
        saveHistory();
        // The vertex positions might be changed
        // We update the ui elements accordingly
        m_toolbarPanel.graphChanged(m_graphContainer);
        m_layoutHintComponent.graphChanged(m_graphContainer);
    }

    public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }

    /**
     * Parameter is a String because config has String values
     * @param boolVal
     */
    public void setShowHeader(String boolVal) {
        m_showHeader = Boolean.valueOf(boolVal);
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        if (m_currentHudDisplay != null) {
            m_currentHudDisplay.setVertexSelectionCount(selectionContext.getSelectedVertexRefs().size());
            m_currentHudDisplay.setEdgeSelectionCount(selectionContext.getSelectedEdgeRefs().size());
        }

        if(m_topologyComponent != null) m_topologyComponent.setActiveTool("pan");
        saveHistory();
    }

    @Override
    public void detach() {
        super.detach();
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

    private static CheckedOperation getDefaultTopologySelectorOperation(BundleContext bundleContext) {
        try {
            // Find all topology selector operations
            final Collection<ServiceReference<CheckedOperation>> serviceReferences = bundleContext.getServiceReferences(CheckedOperation.class, "(operation.label=*?group=topology)");

            // Filter for linkd
            final Optional<ServiceReference<CheckedOperation>> linkdTopologySelectorOperationOptional = serviceReferences.stream()
                    .filter(serviceReference -> {
                        String label = (String) serviceReference.getProperty("operation.label");
                        return label.toLowerCase().contains("linkd");
                    })
                    .findFirst();
            if (linkdTopologySelectorOperationOptional.isPresent()) {
                return bundleContext.getService(linkdTopologySelectorOperationOptional.get());
            }

            // We did not find linkd, we fall back to the first provider (sorted by label) we found, if any
            final Map<String, CheckedOperation> operationMap = serviceReferences.stream()
                    .collect(Collectors.toMap(reference -> (String) reference.getProperty("operation.label"), reference -> bundleContext.getService(reference)));
            final Optional<String> optionalLabel = operationMap.keySet().stream().sorted().findFirst();
            if (optionalLabel.isPresent()) {
                return operationMap.get(optionalLabel.get());
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Could not query BundleContext for services", e);
        }
        return null; // nothing found
    }
}
