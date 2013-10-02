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

package org.opennms.features.topology.plugins.topo;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.support.SavedHistory;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.opennms.features.topology.app.internal.jung.CircleLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.*;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BundleContextHistoryManagerTest  {

    private static class TestVertex extends AbstractVertex {

        public TestVertex(String id) {
            super("test", id, id);
        }
    }

    private static final String DATA_FILE_NAME = BundleContextHistoryManager.DATA_FILE_NAME;

    private BundleContextHistoryManager historyManager;

    private GraphContainer graphContainerMock;

    private Graph graphMock;

    private BundleContext bundleContextMock;
    private List<Vertex> displayableVertices;
    private Layout selectedLayout;

    @Before
    public void setUp() {
        if (new File(DATA_FILE_NAME).exists()) {
            new File(DATA_FILE_NAME).delete();
        }
        
    	bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
    	graphContainerMock = EasyMock.createNiceMock(GraphContainer.class);
        graphMock = EasyMock.createNiceMock(Graph.class);
    	
        displayableVertices = new ArrayList<Vertex>();
        selectedLayout = new DefaultLayout(graphContainerMock);

        historyManager = new BundleContextHistoryManager(bundleContextMock);
        historyManager.onBind(new CircleLayoutOperation());
        historyManager.onBind(new ManualLayoutOperation());
        historyManager.onBind(new FRLayoutOperation());
        historyManager.onBind(new SimpleLayoutOperation());
        historyManager.onBind(new ISOMLayoutOperation());
        historyManager.onBind(new SpringLayoutOperation());
        historyManager.onBind(new RealUltimateLayoutOperation());
        historyManager.onBind(new KKLayoutOperation());
        historyManager.onBind(new AutoRefreshToggleOperation());

        setBehaviour(bundleContextMock);
        setBehaviour(graphContainerMock);
        setBehaviour(graphMock);

        EasyMock.replay(graphContainerMock);
        EasyMock.replay(graphMock);
        EasyMock.replay(bundleContextMock);
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
        String historyHash = historyManager.createHistory(admin, graphContainerMock);
        SavedHistory savedHistory = historyManager.getHistory(admin, historyHash);
        Properties properties = loadProperties();
        Assert.assertNotNull(savedHistory);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash));
        Assert.assertNotNull(properties.get(admin));
        properties = null;  // no access to this field after this line!

        // save again (nothing should change)
        String historyHash2 = historyManager.createHistory(admin, graphContainerMock);
        SavedHistory savedHistory2 = historyManager.getHistory(admin, historyHash2);
        Properties properties2 = loadProperties();
        Assert.assertNotNull(savedHistory2);
        Assert.assertNotNull(properties2);
        Assert.assertEquals(2, properties2.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties2.containsKey(admin));
        Assert.assertTrue(properties2.containsKey(historyHash2));
        Assert.assertEquals(properties2.get(admin), historyHash2);
        Assert.assertNotNull(properties2.get(historyHash2));
        properties2 = null;   // no access to this field after this line!

        // change entry for user "admin"
        displayableVertices.add(new TestVertex("200"));
        String historyHash3 = historyManager.createHistory(admin, graphContainerMock);
        SavedHistory savedHistory3 = historyManager.getHistory(admin, historyHash3);
        Properties properties3 = loadProperties();
        Assert.assertNotNull(savedHistory3);
        Assert.assertNotNull(properties3);
        Assert.assertEquals(2, properties3.size());   // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties3.containsKey(admin));
        Assert.assertTrue(properties3.containsKey(historyHash3));
        Assert.assertFalse(properties3.containsKey(historyHash2)); // this should be removed
        Assert.assertNotNull(properties3.get(historyHash3));
        properties3 = null;  // no access to this field after this line!

        // create an entry for another user, but with same historyHash
        String historyHash4 = historyManager.createHistory(user1, graphContainerMock);
        SavedHistory savedHistory4 = historyManager.getHistory(admin, historyHash3);
        Properties properties4 = loadProperties();
        Assert.assertEquals(historyHash3, historyHash4);
        Assert.assertNotNull(savedHistory4);
        Assert.assertNotNull(properties4);
        Assert.assertEquals(3, properties4.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties4.containsKey(user1));
        Assert.assertTrue(properties4.containsKey(admin));
        Assert.assertTrue(properties4.containsKey(historyHash4));
        Assert.assertEquals(historyHash4, properties4.get(admin));
        Assert.assertEquals(historyHash4, properties4.get(user1));
        Assert.assertNotNull(properties4.get(historyHash4));
        properties4 = null;  // no access to this field after this line!

        // change entry for user1
        displayableVertices.remove(0);
        String historyHash5 = historyManager.createHistory(user1, graphContainerMock);
        SavedHistory savedHistory5 = historyManager.getHistory(user1, historyHash3);
        Properties properties5 = loadProperties();
        Assert.assertNotNull(savedHistory5);
        Assert.assertNotNull(properties5);
        Assert.assertEquals(4, properties5.size());   // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties5.containsKey(admin));
        Assert.assertTrue(properties5.containsKey(user1));
        Assert.assertTrue(properties5.containsKey(historyHash4));
        Assert.assertTrue(properties5.containsKey(historyHash5));
        Assert.assertNotNull(properties5.get(historyHash5));
        Assert.assertEquals(historyHash4, properties5.get(admin));
        Assert.assertEquals(historyHash5, properties5.get(user1));
        properties5 = null;  // no access to this field after this line!
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(DATA_FILE_NAME)));
        return props;
    }

    private void setBehaviour(Graph graphMock) {
        EasyMock
                .expect(graphMock.getDisplayEdges())
                .andReturn(new ArrayList<Edge>())
                .anyTimes();

        EasyMock
                .expect(graphMock.getDisplayVertices())
                .andReturn(displayableVertices)
                .anyTimes();
        
        EasyMock
        	.expect(graphMock.getLayout())
        	.andReturn(selectedLayout)
        	.anyTimes();
    }

    private void setBehaviour(GraphContainer graphContainerMock) {
    	MapViewManager mapViewManagerMock = EasyMock.createNiceMock(MapViewManager.class);
        EasyMock.expect(mapViewManagerMock.getCurrentBoundingBox()).andReturn(new BoundingBox(0,0,Integer.MAX_VALUE, Integer.MAX_VALUE)).anyTimes();
        EasyMock.replay(mapViewManagerMock);
        
        SelectionManager selectionManagerMock = EasyMock.createNiceMock(SelectionManager.class);
        EasyMock.expect(selectionManagerMock.getSelectedVertexRefs()).andReturn(new ArrayList<VertexRef>()).anyTimes();
        EasyMock.replay(selectionManagerMock);
    	
    	EasyMock
                .expect(graphContainerMock.getGraph())
                .andReturn(graphMock)
                .anyTimes();

        EasyMock
                .expect(graphContainerMock.getSemanticZoomLevel())
                .andReturn(0)
                .anyTimes();

        EasyMock
        		.expect(graphContainerMock.getLayoutAlgorithm())
    			.andReturn(new CircleLayoutAlgorithm())
    			.anyTimes();
        
        EasyMock
                .expect(graphContainerMock.getMapViewManager())
                .andReturn(mapViewManagerMock)
                .anyTimes();
        
        EasyMock
        		.expect(graphContainerMock.getSelectionManager())
        		.andReturn(selectionManagerMock)
        		.anyTimes();
    }

    private void setBehaviour(BundleContext bundleContextMock) {
        EasyMock
                .expect(bundleContextMock.getDataFile(DATA_FILE_NAME))
                .andReturn(new File(DATA_FILE_NAME))
                .anyTimes();
    }
}
