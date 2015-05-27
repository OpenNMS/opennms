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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.AlarmSeverity;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ComponentTracker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SimpleNodeMarker;

public class MarkerFilterImplTest {
    public class WrappedMarkerFilterImpl extends MarkerFilterImpl {
        private int m_filterUpdatedCalls = 0;

        public WrappedMarkerFilterImpl(final String searchString, final AlarmSeverity minimumSeverity, final OpenNMSEventManager eventManager, final ComponentTracker componentTracker) {
            super(searchString, minimumSeverity, eventManager, componentTracker);
        }

        protected void initHandlers() {
        }
        
        public int getFilterUpdatedCalls() {
            return m_filterUpdatedCalls;
        }

        void sendFilterUpdatedEvent() {
            m_filterUpdatedCalls++;
        }
    }

    private OpenNMSEventManager m_eventManager;
    private ComponentTracker m_componentTracker;

    @Before
    public void setUp() throws Exception {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINER);
        m_eventManager = new OpenNMSEventManager();
        m_componentTracker = new ComponentTracker(m_eventManager);
    }
    
    @Test
    public void testEmptySearch() {
        // empty searches should always match
        final WrappedMarkerFilterImpl filter = new WrappedMarkerFilterImpl(null, AlarmSeverity.NORMAL, m_eventManager, m_componentTracker);

        final NodeMarker marker = new SimpleNodeMarker();
        assertTrue(filter.matches(marker));

        filter.setSearchString("");
        assertTrue(filter.matches(marker));
        assertEquals(1, filter.getFilterUpdatedCalls());
    }

    @Test
    public void testSubstringMatch() {
        final MarkerFilterImpl filter = new WrappedMarkerFilterImpl("blah", AlarmSeverity.NORMAL, m_eventManager, m_componentTracker);

        final SimpleNodeMarker marker = new SimpleNodeMarker();

        marker.setNodeLabel("this has the string blah in it");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bla");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("blah");
        assertTrue(filter.matches(marker));

        filter.setSearchString("nodeLabel: blah");

        marker.setNodeLabel("this has the string blah in it");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bla");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("blah");
        assertTrue(filter.matches(marker));

        // now try categories
        filter.setSearchString("blah");
        marker.setNodeLabel("notMatching");
        marker.addCategory("bla");
        assertFalse(filter.matches(marker));

        marker.addCategory("blah");
        assertTrue(filter.matches(marker));

        marker.setCategoryList(new ArrayList<String>());
        assertFalse(filter.matches(marker));

        marker.addCategory("this has the string 'blah' in it too!");
        assertTrue(filter.matches(marker));

        filter.setSearchString("category: blah");
        marker.setNodeLabel("notMatching");
        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("bla");
        assertFalse(marker.getCategoryList() + " should contain blah", filter.matches(marker));

        marker.addCategory("blah");
        assertTrue(filter.matches(marker));

        marker.setCategoryList(new ArrayList<String>());
        assertFalse(filter.matches(marker));

        marker.addCategory("this has the string 'blah' in it too!");
        assertTrue(filter.matches(marker));
        
        marker.setForeignSource("sanJose");
        filter.setSearchString("foreignSource:sanJose");
        assertTrue(filter.matches(marker));
    }

    @Test
    public void testExactMatch() {
        final MarkerFilterImpl filter = new WrappedMarkerFilterImpl("nodeLabel=blah", AlarmSeverity.NORMAL, m_eventManager, m_componentTracker);
        final SimpleNodeMarker marker = new SimpleNodeMarker();

        marker.setNodeLabel("blah");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("ablah");
        assertFalse(filter.matches(marker));

        filter.setSearchString("category=blah");
        marker.setNodeLabel(null);
        marker.addCategory("ablah");
        assertFalse(filter.matches(marker));

        marker.addCategory("blah");
        assertTrue(filter.matches(marker));

        marker.setForeignSource("sanJose");
        filter.setSearchString("foreignSource=sanJose");
        assertTrue(filter.matches(marker));
    }

    @Test
    public void testInMatch() {
        final MarkerFilterImpl filter = new WrappedMarkerFilterImpl("nodeLabel in foo, bar, baz", AlarmSeverity.NORMAL, m_eventManager, m_componentTracker);
        final SimpleNodeMarker marker = new SimpleNodeMarker();

        marker.setNodeLabel("fo");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("foo");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bara");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("baz");
        assertTrue(filter.matches(marker));

        filter.setSearchString("nodeLabel in (foo, bar, baz)");

        marker.setNodeLabel("fo");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("foo");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bara");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("baz");
        assertTrue(filter.matches(marker));

        filter.setSearchString("category in foo, bar, baz");
        marker.setNodeLabel(null);
        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("ba");
        assertFalse(filter.matches(marker));

        marker.addCategory("bar");
        assertTrue(filter.matches(marker));

        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("baz");
        assertTrue(filter.matches(marker));

        filter.setSearchString("categories in foo, bar, baz");
        marker.setNodeLabel(null);
        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("ba");
        assertFalse(filter.matches(marker));

        marker.addCategory("bar");
        assertTrue(filter.matches(marker));

        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("baz");
        assertTrue(filter.matches(marker));
    }
}
