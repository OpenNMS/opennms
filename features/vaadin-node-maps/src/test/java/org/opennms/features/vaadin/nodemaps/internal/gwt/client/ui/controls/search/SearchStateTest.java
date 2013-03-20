package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.SearchStateManager.State;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.impl.SchedulerImpl;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.junit.GWTMockUtilities;
import com.vaadin.terminal.gwt.client.ApplicationConfiguration;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Console;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ValueMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    NativeEvent.class,
    EventTarget.class,
    VConsole.class,
    Scheduler.class,
    SchedulerImpl.class
})
@SuppressStaticInitializationFor({
    "com.google.gwt.dom.client.NativeEvent",
    "com.google.gwt.dom.client.EventTarget",
    "com.google.gwt.dom.client.InputElement",
    "com.google.gwt.core.client.Scheduler",
    "com.google.gwt.core.client.impl.SchedulerImpl"
})
public class SearchStateTest {
    private ValueItem m_valueItem = new TestValueItem();

    @BeforeClass
    public static void setUp() throws Exception {
        GWTMockUtilities.disarm();
        final Console console = new TestConsole();
        Whitebox.setInternalState(VConsole.class, "impl", console);

        final SchedulerImpl scheduler = new TestSchedulerImpl();
        Whitebox.setInternalState(SchedulerImpl.class, "INSTANCE", scheduler);
    }

    @AfterClass
    public static void tearDown() {
        GWTMockUtilities.restore();
    }

    @Test
    public void testSimpleSearch() throws Exception {
        final MockSearchStateManager searchManager = new MockSearchStateManager(m_valueItem);

        assertNotNull(searchManager.getState());
        assertEquals(State.NOT_SEARCHING, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());

        // start typing
        typeCharacter(searchManager, 'a');
        searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());

        typeCharacter(searchManager, 'b');
        searchManager.updateMatchCount(2);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());

        typeCharacter(searchManager, 'c');
        searchManager.updateMatchCount(0);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_HIDDEN, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());

        // hit enter
        hitEnterInInput(searchManager);
        assertEquals(State.SEARCHING_FINISHED, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());

        // hit the cancel button
        hitCancelX(searchManager);
        assertEquals(State.NOT_SEARCHING, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
    }

    @Test
    public void testBackspace() throws Exception {
        final MockSearchStateManager searchManager = new MockSearchStateManager(m_valueItem);

        typeCharacter(searchManager, 'a');
        searchManager.updateMatchCount(20);
        typeCharacter(searchManager, 'b');
        searchManager.updateMatchCount(15);
        typeCharacter(searchManager, 'c');
        searchManager.updateMatchCount(12);
        typeCharacter(searchManager, 'd');
        searchManager.updateMatchCount(10);
        typeCharacter(searchManager, 'e');
        searchManager.updateMatchCount(1);
        typeCharacter(searchManager, (char)KeyCodes.KEY_BACKSPACE);
        searchManager.updateMatchCount(10);
        assertEquals(4, m_valueItem.getValue().length());
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());
    }

    @Test
    public void testChangeSearchTextAfterFinishing() throws Exception {
        final MockSearchStateManager searchManager = new MockSearchStateManager(m_valueItem);

        // start typing
        typeCharacter(searchManager, 'a');
        searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());
        assertEquals("a", m_valueItem.getValue());

        typeCharacter(searchManager, 'b');
        searchManager.updateMatchCount(0);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_HIDDEN, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
        assertEquals("ab", m_valueItem.getValue());
        
        // hit enter
        hitEnterInInput(searchManager);
        searchManager.updateMatchCount(0);
        assertEquals(State.SEARCHING_FINISHED, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
        assertEquals("ab", m_valueItem.getValue());

        // start typing
        typeCharacter(searchManager, (char)KeyCodes.KEY_BACKSPACE);
        searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());
        assertEquals("a", m_valueItem.getValue());
    }

    @Test
    public void testAutocompleteKeyboardNavigation() throws Exception {
        final MockSearchStateManager searchManager = new MockSearchStateManager(m_valueItem);

        // autocomplete shouldn't do anything even if we down-arrow, since we haven't started searching yet
        typeCharacter(searchManager, (char)KeyCodes.KEY_DOWN);
        searchManager.updateMatchCount(100);
        assertEquals(State.NOT_SEARCHING, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
        assertEquals(false, searchManager.isAutocompleteFocused());

        // start typing
        typeCharacter(searchManager, 'a');
        searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());
        assertEquals(false, searchManager.isAutocompleteFocused());

        // hit down-arrow
        typeCharacter(searchManager, (char)KeyCodes.KEY_DOWN);
        searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_ACTIVE, searchManager.getState());
        assertEquals(true, searchManager.isAutocompleteVisible());
        assertEquals(true, searchManager.isAutocompleteFocused());

        // hit enter
        hitEnterInInput(searchManager);
        searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_FINISHED, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
        assertEquals(false, searchManager.isAutocompleteFocused());
    }

    @Test
    public void testAutocompleteClick() throws Exception {
        final MockSearchStateManager searchManager = new MockSearchStateManager(m_valueItem);

        // type something
        typeCharacter(searchManager, 'a');
        searchManager.updateMatchCount(15);

        // then click on an entry in the autocomplete
        clickAutocompleteEntry(searchManager);
        searchManager.updateMatchCount(1);
        assertEquals(State.SEARCHING_FINISHED, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
        assertEquals(false, searchManager.isAutocompleteFocused());

        // hit enter, nothing should really happen
        hitEnterInInput(searchManager);
        searchManager.updateMatchCount(1);
        assertEquals(State.SEARCHING_FINISHED, searchManager.getState());
        assertEquals(false, searchManager.isAutocompleteVisible());
        assertEquals(false, searchManager.isAutocompleteFocused());
    }

    protected void clickAutocompleteEntry(final MockSearchStateManager searchManager) throws Exception {
        searchManager.handleAutocompleteEvent(createEvent("click", 0));
    }

    protected void hitEnterInAutocompleteField(final MockSearchStateManager searchManager) throws Exception {
        searchManager.handleAutocompleteEvent(createEvent("keydown", KeyCodes.KEY_ENTER));
    }

    protected void hitCancelX(final MockSearchStateManager searchManager) throws Exception {
        m_valueItem.setValue("");
        searchManager.handleInputEvent(createEvent("search", 0));
    }

    protected void hitEnterInInput(final MockSearchStateManager searchManager) throws Exception {
        searchManager.handleInputEvent(createEvent("keydown", KeyCodes.KEY_ENTER));
    }

    protected void typeCharacter(final MockSearchStateManager searchManager, final char keyCode) throws Exception {
        final String value = m_valueItem.getValue();
        if ("".equals(value) || value == null) {
            m_valueItem.setValue("" + keyCode);
        } else if (keyCode == KeyCodes.KEY_BACKSPACE) {
            if (value.length() > 0) {
                m_valueItem.setValue(value.substring(0, value.length() - 1));
                VConsole.log("backspace!  old=" + value + ", new=" + m_valueItem.getValue());
            }
        } else {
            m_valueItem.setValue(value + keyCode);
        }
        searchManager.handleInputEvent(createEvent("keydown", keyCode));
    }

    protected NativeEvent createEvent(final String type, final int keyCode) throws Exception {
        final NativeEvent event = PowerMock.createMock(NativeEvent.class);
        expect(event.getType()).andReturn(type).anyTimes();
        expect(event.getKeyCode()).andReturn(keyCode).anyTimes();
        PowerMock.replay(event);
        return event;
    }

    private static final class TestValueItem implements ValueItem {
        private String m_value = "";

        @Override public String getValue() { return m_value; }

        @Override public void setValue(final String value) { m_value = value; }
    }

    private static final class TestSchedulerImpl extends SchedulerImpl {
        @Override public void scheduleDeferred(final ScheduledCommand cmd) {
            cmd.execute();
        }

        @Override public void scheduleIncremental(final RepeatingCommand cmd) {
            while (cmd.execute()) {}
        }
    }

    private static final class TestConsole implements Console {
        @Override public void log(final String msg) {
            System.err.println(msg);
        }

        @Override public void log(final Throwable e) {
            e.printStackTrace(System.err);
        }

        @Override public void error(final Throwable e) {
            e.printStackTrace(System.err);
        }

        @Override public void error(final String msg) {
            System.err.println(msg);
        }

        @Override public void printObject(final Object msg) {
            System.err.println(msg);
        }

        @Override public void dirUIDL(ValueMap u, ApplicationConfiguration cnf) {}

        @Override public void printLayoutProblems(ValueMap meta, ApplicationConnection applicationConnection, Set<Paintable> zeroHeightComponents, Set<Paintable> zeroWidthComponents) {}

        @Override public void setQuietMode(boolean quietDebugMode) {}

        @Override public void init() {}
    }

    private static class MockSearchStateManager extends SearchStateManager {
        public MockSearchStateManager(final ValueItem valueItem) {
            super(valueItem);
        }

        private boolean m_autocompleteVisible = false;
        private boolean m_autocompleteFocused = false;

        @Override public void refresh() {
            System.err.println("refreshing data!");
        }

        @Override public void focusAutocomplete() {
            System.err.println("focusing autocomplete!");
            m_autocompleteFocused = true;
        }

        @Override public void hideAutocomplete() {
            System.err.println("hiding autocomplete!");
            m_autocompleteVisible = false;
            m_autocompleteFocused = false;
        }

        @Override public void showAutocomplete() {
            System.err.println("showing autocomplete!");
            m_autocompleteVisible = true;
        }

        @Override public void clearSearchInput() {
            System.err.println("clearing search input!");
        }

        @Override public void entrySelected() {
            System.err.println("current autocomplete entry selected!");
        }

        public boolean isAutocompleteVisible() {
            return m_autocompleteVisible;
        }

        public Object isAutocompleteFocused() {
            return m_autocompleteFocused;
        }

    }
}
