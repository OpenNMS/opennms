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

public class MarkerFilterImplTest {

    @Before
    public void setUp() {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINER);
    }

    @Test
    public void testEmptySearch() {
        // empty searches should always match
        final MockSearchConsumer consumer = new MockSearchConsumer(null, 0);
        final MarkerFilter filter = new MarkerFilterImpl(consumer);
        final NodeMarker marker = new SimpleNodeMarker();
        assertTrue(filter.matches(marker));

        consumer.setSearchString("");
        assertTrue(filter.matches(marker));
    }

    @Test
    public void testSubstringMatch() {
        final MockSearchConsumer consumer = new MockSearchConsumer("blah", 0);
        final MarkerFilter filter = new MarkerFilterImpl(consumer);
        final SimpleNodeMarker marker = new SimpleNodeMarker();

        marker.setNodeLabel("this has the string blah in it");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bla");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("blah");
        assertTrue(filter.matches(marker));

        consumer.setSearchString("nodeLabel: blah");

        marker.setNodeLabel("this has the string blah in it");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bla");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("blah");
        assertTrue(filter.matches(marker));

        // now try categories
        consumer.setSearchString("blah");
        marker.setNodeLabel("notMatching");
        marker.addCategory("bla");
        assertFalse(filter.matches(marker));

        marker.addCategory("blah");
        assertTrue(filter.matches(marker));

        marker.setCategoryList(new ArrayList<String>());
        assertFalse(filter.matches(marker));

        marker.addCategory("this has the string 'blah' in it too!");
        assertTrue(filter.matches(marker));

        consumer.setSearchString("category: blah");
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
        final MockSearchConsumer consumer = new MockSearchConsumer("nodeLabel=blah", 0);
        final MarkerFilter filter = new MarkerFilterImpl(consumer);
        final SimpleNodeMarker marker = new SimpleNodeMarker();

        marker.setNodeLabel("blah");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("ablah");
        assertFalse(filter.matches(marker));

        consumer.setSearchString("category=blah");
        marker.setNodeLabel(null);
        marker.addCategory("ablah");
        assertFalse(filter.matches(marker));

        marker.addCategory("blah");
        assertTrue(filter.matches(marker));
    }

    @Test
    public void testInMatch() {
        final MockSearchConsumer consumer = new MockSearchConsumer("nodeLabel in foo, bar, baz", 0);
        final MarkerFilter filter = new MarkerFilterImpl(consumer);
        final SimpleNodeMarker marker = new SimpleNodeMarker();

        marker.setNodeLabel("fo");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("foo");
        assertTrue(filter.matches(marker));

        marker.setNodeLabel("bara");
        assertFalse(filter.matches(marker));

        marker.setNodeLabel("baz");
        assertTrue(filter.matches(marker));

        consumer.setSearchString("category in foo, bar, baz");
        marker.setNodeLabel(null);
        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("ba");
        assertFalse(filter.matches(marker));

        marker.addCategory("bar");
        assertTrue(filter.matches(marker));

        marker.setCategoryList(new ArrayList<String>());
        marker.addCategory("baz");
        assertTrue(filter.matches(marker));

        consumer.setSearchString("categories in foo, bar, baz");
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
