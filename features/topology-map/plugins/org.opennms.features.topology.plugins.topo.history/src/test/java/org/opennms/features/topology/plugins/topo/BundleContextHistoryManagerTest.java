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

package org.opennms.features.topology.plugins.topo;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.support.HistoryAwareSearchProvider;
import org.opennms.features.topology.api.support.SavedHistory;
import org.opennms.features.topology.api.support.ServiceLocator;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.SimpleMetaTopologyProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.AlarmProvider;
import org.opennms.features.topology.app.internal.AlarmSearchProvider;
import org.opennms.features.topology.app.internal.CategoryProvider;
import org.opennms.features.topology.app.internal.CategorySearchProvider;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.opennms.features.topology.app.internal.IpInterfaceProvider;
import org.opennms.features.topology.app.internal.IpLikeSearchProvider;
import org.opennms.features.topology.app.internal.jung.CircleLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.AutoRefreshToggleOperation;
import org.opennms.features.topology.app.internal.operations.CircleLayoutOperation;
import org.opennms.features.topology.app.internal.operations.FRLayoutOperation;
import org.opennms.features.topology.app.internal.operations.ISOMLayoutOperation;
import org.opennms.features.topology.app.internal.operations.KKLayoutOperation;
import org.opennms.features.topology.app.internal.operations.ManualLayoutOperation;
import org.opennms.features.topology.app.internal.operations.RealUltimateLayoutOperation;
import org.opennms.features.topology.app.internal.operations.SimpleLayoutOperation;
import org.opennms.features.topology.app.internal.operations.SpringLayoutOperation;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.support.AlarmHopCriteria;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.features.topology.app.internal.support.IpLikeHopCriteria;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

public class BundleContextHistoryManagerTest  {

    private static final String searchQuery = "search query 1";
    private static final String idAlarm = "1";
    private static final String idCategory = "2";
    private static final String idIpLike = "3";
    private static final String labelAlarm = "alarmLabel";
    private static final String labelCategory = "categoryLabel";
    private static final String labelIpLike = "ipLikeLabel";

    private enum CriteriaTypes {
        category,
        ipLike,
        alarm
    }

    private static class TestVertex extends AbstractVertex {

        public TestVertex(String id) {
            super("test", id, id);
        }
    }

    private static final String DATA_FILE_NAME = BundleContextHistoryManager.DATA_FILE_NAME;

    private BundleContextHistoryManager historyManager;
    private GraphContainer graphContainerMock;
    private Graph graphMock;
    private GraphProvider graphProviderMock;
    private BundleContext bundleContextMock;
    private ServiceLocator serviceLocatorMock;
    private TopologyServiceClient topologyServiceClientMock;

    private Map<CriteriaTypes, SearchResult> startingSearchResults;
    private Map<CriteriaTypes, Criteria> startingCriteria;
    private Map<CriteriaTypes, SearchProvider> startingProviders;
    private List<Criteria> capturedCriteria;
    private List<Vertex> displayableVertices;
    private Layout selectedLayout;

    @Before
    public void setUp() {
        if (new File(DATA_FILE_NAME).exists()) {
            new File(DATA_FILE_NAME).delete();
        }

        serviceLocatorMock = Mockito.mock(ServiceLocator.class);
    	bundleContextMock = Mockito.mock(BundleContext.class);
    	graphContainerMock = Mockito.mock(GraphContainer.class);
    	topologyServiceClientMock = Mockito.mock(TopologyServiceClient.class);
        graphMock = Mockito.mock(Graph.class);
        graphProviderMock = Mockito.mock(GraphProvider.class);

        startingSearchResults = new HashMap<>();
        startingProviders = new HashMap<>();
        startingCriteria = new HashMap<>();
        capturedCriteria = new ArrayList<>();
        displayableVertices = new ArrayList<>();
        selectedLayout = new DefaultLayout();

        historyManager = new BundleContextHistoryManager(bundleContextMock, serviceLocatorMock);
        historyManager.onBind(new CircleLayoutOperation());
        historyManager.onBind(new ManualLayoutOperation(null));
        historyManager.onBind(new FRLayoutOperation());
        historyManager.onBind(new SimpleLayoutOperation());
        historyManager.onBind(new ISOMLayoutOperation());
        historyManager.onBind(new SpringLayoutOperation());
        historyManager.onBind(new RealUltimateLayoutOperation());
        historyManager.onBind(new KKLayoutOperation());
        historyManager.onBind(new AutoRefreshToggleOperation());

        setBehaviour(graphProviderMock);
        setBehaviour(topologyServiceClientMock);
        setBehaviour(bundleContextMock);
        setBehaviour(graphContainerMock);
        setBehaviour(graphMock);
        setBehaviour(serviceLocatorMock);

        initProvidersAndCriteria();
    }

    @After
    public void tearDown() {
        if (new File(DATA_FILE_NAME).exists()) {
            new File(DATA_FILE_NAME).delete();
        }
    }

    @Test
    public void testHistoryManager() throws IOException {
        String admin = "admin";
        String user1 = "user1";
        displayableVertices.add(new TestVertex("100"));
        
        // simple save
        String historyHash = historyManager.saveOrUpdateHistory(admin, graphContainerMock);
        SavedHistory savedHistory = historyManager.getHistoryByFragment(historyHash);
        Properties properties = loadProperties();
        Assert.assertNotNull(savedHistory);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash));
        Assert.assertNotNull(properties.get(admin));
        properties = null;  // no access to this field after this line!

        // save again (nothing should change)
        String historyHash2 = historyManager.saveOrUpdateHistory(admin, graphContainerMock);
        SavedHistory savedHistory2 = historyManager.getHistoryByFragment(historyHash2);
        properties = loadProperties();
        Assert.assertNotNull(savedHistory2);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash2));
        Assert.assertEquals(properties.get(admin), historyHash2);
        Assert.assertNotNull(properties.get(historyHash2));

        // change entry for user "admin"
        displayableVertices.add(new TestVertex("200"));
        String historyHash3 = historyManager.saveOrUpdateHistory(admin, graphContainerMock);
        SavedHistory savedHistory3 = historyManager.getHistoryByFragment(historyHash3);
        properties = loadProperties();
        Assert.assertNotNull(savedHistory3);
        Assert.assertNotNull(properties);
        Assert.assertEquals(3, properties.size());   // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash3));
        Assert.assertTrue(properties.containsKey(historyHash2)); // this should not be removed
        Assert.assertNotNull(properties.get(historyHash3));

        // create an entry for another user, but with same historyHash
        String historyHash4 = historyManager.saveOrUpdateHistory(user1, graphContainerMock);
        SavedHistory savedHistory4 = historyManager.getHistoryByFragment(historyHash3);
        properties = loadProperties();
        Assert.assertEquals(historyHash3, historyHash4);
        Assert.assertNotNull(savedHistory4);
        Assert.assertNotNull(properties);
        Assert.assertEquals(4, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(user1));
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash4));
        Assert.assertEquals(historyHash4, properties.get(admin));
        Assert.assertEquals(historyHash4, properties.get(user1));
        Assert.assertNotNull(properties.get(historyHash4));

        // change entry for user1
        displayableVertices.remove(0);
        String historyHash5 = historyManager.saveOrUpdateHistory(user1, graphContainerMock);
        SavedHistory savedHistory5 = historyManager.getHistoryByFragment(historyHash3);
        properties = loadProperties();
        Assert.assertNotNull(savedHistory5);
        Assert.assertNotNull(properties);
        Assert.assertEquals(5, properties.size());   // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(user1));
        Assert.assertTrue(properties.containsKey(historyHash4));
        Assert.assertTrue(properties.containsKey(historyHash5));
        Assert.assertNotNull(properties.get(historyHash5));
        Assert.assertEquals(historyHash4, properties.get(admin));
        Assert.assertEquals(historyHash5, properties.get(user1));
    }

    /**
     * <p>This method tests the correctness of {@link org.opennms.features.topology.api.support.HistoryAwareSearchProvider#buildCriteriaFromQuery(SearchResult, GraphContainer)} method.
     * The {@link SearchCriteria} objects, generated by this method, are compared to those created directly, with the use of a corresponding constructor</p>
     * <p>
     * Additionally, it checks whether the {@link SavedHistory} objects are saved / loaded correctly and whether there is any data loss / distortion
     * </p>
     */
    @Test
    public void verifySearchCriteriaPersistence() {
        // Test 1 - checking whether method buildCriteriaFromQuery is working correctly
        // Testing for CategoryHopCriteria & CategorySearchProvider
        Criteria criterionNew = ((HistoryAwareSearchProvider)this.startingProviders.get(CriteriaTypes.category)).buildCriteriaFromQuery(
                            this.startingSearchResults.get(CriteriaTypes.category), graphContainerMock);
        Assert.assertEquals(this.startingCriteria.get(CriteriaTypes.category), criterionNew);

        // Testing for IpLikeHopCriteria & IpLikeSearchProvider
        criterionNew = ((HistoryAwareSearchProvider)this.startingProviders.get(CriteriaTypes.ipLike)).buildCriteriaFromQuery(
                this.startingSearchResults.get(CriteriaTypes.ipLike), graphContainerMock);
        Assert.assertEquals(this.startingCriteria.get(CriteriaTypes.ipLike), criterionNew);

        // Testing for AlarmHopCriteria & AlarmSearchProvider
        criterionNew = ((HistoryAwareSearchProvider)this.startingProviders.get(CriteriaTypes.alarm)).buildCriteriaFromQuery(
                this.startingSearchResults.get(CriteriaTypes.alarm), graphContainerMock);
        Assert.assertEquals(this.startingCriteria.get(CriteriaTypes.alarm), criterionNew);

        // Testing saving/loading of history - checking whether the set of Criteria remains the same
        // Step 1 - providing GraphContainer with a starting Criteria set
        for (Criteria criteria : this.startingCriteria.values()) {
            graphContainerMock.addCriteria(criteria);
        }

        // Saving and then loading history
        String fragment = historyManager.saveOrUpdateHistory("admin", graphContainerMock);
        historyManager.applyHistory(fragment, graphContainerMock);

        Assert.assertNotNull(capturedCriteria);
        Assert.assertThat(startingCriteria.values(), containsInAnyOrder(capturedCriteria.toArray()));
    }

    /**
     * In this method all starting {@link SearchCriteria} and {@link SearchProvider} objects are initialized
     */
    private void initProvidersAndCriteria() {
        // Preparing SearchProviders
        CategoryProvider vertexProvider = new CategoryProvider() {
            @Override
            public Collection<OnmsCategory> getAllCategories() {
                return Lists.newArrayList(findCategoryByName("somename"));
            }

            @Override
            public OnmsCategory findCategoryByName(String m_categoryName) {
                OnmsCategory cat = new OnmsCategory("test", "test");
                cat.setId(Integer.valueOf(idCategory));
                return cat;
            }

            @Override
            public List<OnmsNode> findNodesForCategory(OnmsCategory category) {
                return new ArrayList<>();
            }
        };
        IpInterfaceProvider ipInterfaceProvider = new IpInterfaceProvider() {
            @Override
            public List<OnmsIpInterface> findMatching(org.opennms.core.criteria.Criteria criteria) {
                OnmsNode node = new OnmsNode();
                node.setId(Integer.valueOf(idIpLike));
                String ipAddr = "127.0.0.1";
                OnmsIpInterface ipInterface = new OnmsIpInterface(ipAddr, node);
                return Lists.newArrayList(ipInterface);
            }
        };
        AlarmProvider alarmProvider = new AlarmProvider() {
            @Override
            public List<OnmsAlarm> findMatchingAlarms(org.opennms.core.criteria.Criteria criteria) {
                Date eventTime = new Date();
                OnmsDistPoller distPoller = new OnmsDistPoller("pollerID");
                OnmsEvent event = new OnmsEvent();
                OnmsAlarm alarm = new OnmsAlarm(Integer.valueOf(idAlarm), "eventUI", distPoller, 2, 3, eventTime, event);
                return Lists.newArrayList(alarm);
            }
        };

        // Creating SearchResults to be used in testing
        SearchResult sResultCategory = new SearchResult(CategoryHopCriteria.NAMESPACE, idCategory, labelCategory, searchQuery, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
        SearchResult sResultAlarm = new SearchResult(AlarmHopCriteria.NAMESPACE, idAlarm, labelAlarm, searchQuery, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
        SearchResult sResultIpLike = new SearchResult(IpLikeHopCriteria.NAMESPACE, idIpLike, labelIpLike, searchQuery, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
        this.startingSearchResults.put(CriteriaTypes.alarm, sResultAlarm);
        this.startingSearchResults.put(CriteriaTypes.ipLike, sResultIpLike);
        this.startingSearchResults.put(CriteriaTypes.category, sResultCategory);

        // Initializing available (initial) SearchProviders
        final DefaultTopologyService topologyService = new DefaultTopologyService();
        topologyService.setServiceLocator(serviceLocatorMock);

        this.startingProviders.put(CriteriaTypes.category, new CategorySearchProvider(topologyService, vertexProvider));
        this.startingProviders.put(CriteriaTypes.ipLike, new IpLikeSearchProvider(ipInterfaceProvider));
        this.startingProviders.put(CriteriaTypes.alarm, new AlarmSearchProvider(alarmProvider));

        // Initializing available (initial) Criteria
        this.startingCriteria.put(CriteriaTypes.category, new CategoryHopCriteria(sResultCategory, vertexProvider, graphContainerMock));
        this.startingCriteria.put(CriteriaTypes.ipLike, new IpLikeHopCriteria(sResultIpLike, ipInterfaceProvider));
        this.startingCriteria.put(CriteriaTypes.alarm, new AlarmHopCriteria(new AlarmSearchProvider(alarmProvider).new AlarmSearchResult(sResultAlarm), alarmProvider));
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(DATA_FILE_NAME)));
        return props;
    }

    private void setBehaviour(GraphProvider graphProviderMock) {
        Mockito.when(graphProviderMock.getVertices(Matchers.any())).
                thenReturn(Lists.newArrayList());

        Mockito.when(graphProviderMock.getNamespace()).thenReturn("test");
    }

    private void setBehaviour(TopologyServiceClient topologyServiceClientMock) {
        Mockito.when(topologyServiceClientMock.getNamespace()).
                thenReturn("test");

        Mockito.when(topologyServiceClientMock.getGraphProviderBy("test")).
                thenReturn(this.graphProviderMock);
    }

    private void setBehaviour(ServiceLocator serviceLocator) {
        Mockito.when(serviceLocator.findServices(SearchProvider.class, null))
               .thenAnswer(invocationOnMock -> Lists.newArrayList(startingProviders.values()));

        Mockito.when(serviceLocator.findServices(MetaTopologyProvider.class, null))
               .thenAnswer(invocationOnMock -> Lists.newArrayList(new SimpleMetaTopologyProvider(graphProviderMock)));
    }

    private void setBehaviour(Graph graphMock) {
        Mockito.when(graphMock.getDisplayEdges()).
                thenReturn(new ArrayList<Edge>());

        Mockito.when(graphMock.getDisplayVertices()).
                thenReturn(displayableVertices);

        Mockito.when(graphMock.getLayout()).
                thenReturn(selectedLayout);
    }

    private void setBehaviour(GraphContainer graphContainerMock) {
    	MapViewManager mapViewManagerMock = Mockito.mock(MapViewManager.class);
        SelectionManager selectionManagerMock = Mockito.mock(SelectionManager.class);

        Mockito.when(mapViewManagerMock.getCurrentBoundingBox()).
                thenReturn(new BoundingBox(0,0,Integer.MAX_VALUE, Integer.MAX_VALUE));

        Mockito.when(selectionManagerMock.getSelectedVertexRefs()).
                thenReturn(new ArrayList<VertexRef>());

    	Mockito.when(graphContainerMock.getGraph()).
                thenReturn(graphMock);

        Mockito.when(graphContainerMock.getSemanticZoomLevel()).
                thenReturn(0);

        Mockito.when(graphContainerMock.getLayoutAlgorithm()).
                thenReturn(new CircleLayoutAlgorithm());
        
        Mockito.when(graphContainerMock.getMapViewManager()).
                thenReturn(mapViewManagerMock);
        
        Mockito.when(graphContainerMock.getSelectionManager()).
                thenReturn(selectionManagerMock);

        Mockito.when(graphContainerMock.getTopologyServiceClient()).
                thenReturn(topologyServiceClientMock);

        Mockito.doAnswer(invocationOnMock -> capturedCriteria.toArray(new Criteria[capturedCriteria.size()]))
                .when(graphContainerMock).getCriteria();

        Mockito.doAnswer(invocationOnMock -> {
			capturedCriteria.clear();
			return null;
		}).when(graphContainerMock).clearCriteria();

        Mockito.doAnswer(invocation -> {
			if (invocation.getArguments()[0] != null && invocation.getArguments()[0] instanceof Criteria) {
				capturedCriteria.add((Criteria) invocation.getArguments()[0]);
			}
			return null;
		}).when(graphContainerMock).addCriteria(Matchers.any(Criteria.class));
    }

    private void setBehaviour(BundleContext bundleContextMock) {
        Mockito.when(bundleContextMock.getDataFile(DATA_FILE_NAME)).
                thenReturn(new File(DATA_FILE_NAME));
    }
}
