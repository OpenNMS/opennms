package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SimpleNodeMarker;

import com.google.gwt.event.shared.HandlerManager;

public class MarkerFilterImplTest {

    private HandlerManager m_handlerManager;

    @Before
    public void setUp() {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINER);
        m_handlerManager = new HandlerManager(this);
    }

    @Test
    public void testEmptySearch() {
        // empty searches should always match
        final MarkerFilterImpl filter = new MarkerFilterImpl(null, 0);
        final NodeMarker marker = new SimpleNodeMarker();
        assertTrue(filter.matches(marker));

        filter.setSearchString("");
        assertTrue(filter.matches(marker));
    }

    @Test
    public void testSubstringMatch() {
        final MarkerFilterImpl filter = new MarkerFilterImpl("blah", 0);
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
    }

    @Test
    public void testExactMatch() {
        final MarkerFilterImpl filter = new MarkerFilterImpl("nodeLabel=blah", 0);
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
    }

    @Test
    public void testInMatch() {
        final MarkerFilterImpl filter = new MarkerFilterImpl("nodeLabel in foo, bar, baz", 0);
        final SimpleNodeMarker marker = new SimpleNodeMarker();

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
