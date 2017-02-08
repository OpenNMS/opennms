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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.easymock.IAnswer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ComponentTracker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.impl.HistoryImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    NativeEvent.class,
    EventTarget.class,
    History.class,
    HistoryImpl.class,
    Scheduler.class,
    SchedulerImpl.class
})
@SuppressStaticInitializationFor({
    "com.google.gwt.dom.client.NativeEvent",
    "com.google.gwt.dom.client.EventTarget",
    "com.google.gwt.dom.client.InputElement",
    "com.google.gwt.user.client.History",
    "com.google.gwt.user.client.HistoryImpl",
    "com.google.gwt.core.client.Scheduler",
    "com.google.gwt.core.client.impl.SchedulerImpl"
})
public class SearchStateTest {
    final Logger LOG = Logger.getLogger(SearchStateTest.class.getName());

    private ValueItem m_mockSearchInput = new TestValueItem();
    private ValueItem m_mockHistory = new TestValueItem();
    private MockSearchStateManager m_searchManager;
    private OpenNMSEventManager m_eventManager = new OpenNMSEventManager();
    private ComponentTracker m_componentTracker = new ComponentTracker(m_eventManager);

    @BeforeClass
    public static void setUpClass() throws Exception {
        GWTMockUtilities.disarm();

        final SchedulerImpl scheduler = new TestSchedulerImpl();
        Whitebox.setInternalState(SchedulerImpl.class, "INSTANCE", scheduler);
    }

    @AfterClass
    public static void tearDownClass() {
        GWTMockUtilities.restore();
    }

    @Before
    public void setUp() throws Exception {
        m_mockSearchInput.setValue("");
        m_mockHistory.setValue("");
        m_searchManager = new MockSearchStateManager(m_mockSearchInput, m_mockHistory, m_eventManager, m_componentTracker);
    }

    @Test
    public void testSimpleSearch() throws Exception {
        assertNotNull(m_searchManager.getState());
        assertEquals(State.NOT_SEARCHING, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());

        // start typing
        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        typeCharacter(m_searchManager, 'b');
        m_searchManager.updateMatchCount(2);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        typeCharacter(m_searchManager, 'c');
        m_searchManager.updateMatchCount(0);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_HIDDEN, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        // hit enter
        hitEnterInInput(m_searchManager);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        // hit the cancel button
        hitCancelX(m_searchManager);
        assertEquals(State.NOT_SEARCHING, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
    }

    @Test
    public void testBackspace() throws Exception {
        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(20);
        typeCharacter(m_searchManager, 'b');
        m_searchManager.updateMatchCount(15);
        typeCharacter(m_searchManager, 'c');
        m_searchManager.updateMatchCount(12);
        typeCharacter(m_searchManager, 'd');
        m_searchManager.updateMatchCount(10);
        typeCharacter(m_searchManager, 'e');
        m_searchManager.updateMatchCount(1);
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_BACKSPACE);
        m_searchManager.updateMatchCount(10);
        assertEquals(4, m_mockSearchInput.getValue().length());
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
        
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_BACKSPACE);
        m_searchManager.updateMatchCount(12);
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_BACKSPACE);
        m_searchManager.updateMatchCount(15);
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_BACKSPACE);
        m_searchManager.updateMatchCount(20);
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_BACKSPACE);
        m_searchManager.updateMatchCount(100);
        assertEquals(0, m_mockSearchInput.getValue().length());
        assertEquals(State.NOT_SEARCHING, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
    }

    @Test
    public void testChangeSearchTextAfterFinishing() throws Exception {
        // start typing
        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
        assertEquals("a", m_mockSearchInput.getValue());

        typeCharacter(m_searchManager, 'b');
        m_searchManager.updateMatchCount(0);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_HIDDEN, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
        assertEquals("ab", m_mockSearchInput.getValue());
        
        // hit enter
        hitEnterInInput(m_searchManager);
        m_searchManager.updateMatchCount(0);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
        assertEquals("ab", m_mockSearchInput.getValue());

        // start typing
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_BACKSPACE);
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
        assertEquals("a", m_mockSearchInput.getValue());
    }

    @Test
    public void testAutocompleteKeyboardNavigation() throws Exception {
        // autocomplete shouldn't do anything even if we down-arrow, since we haven't started searching yet
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_DOWN);
        m_searchManager.updateMatchCount(100);
        assertEquals(State.NOT_SEARCHING, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        // start typing
        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        // hit down-arrow
        typeCharacter(m_searchManager, (char)KeyCodes.KEY_DOWN);
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_ACTIVE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(true, m_searchManager.isAutocompleteFocused());
        assertEquals(false, m_searchManager.isInputFocused());

        // hit enter
        hitEnterInInput(m_searchManager);
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
    }

    @Test
    public void testAutocompleteClick() throws Exception {
        // type something
        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(15);

        // then click on an entry in the autocomplete
        clickAutocompleteEntry(m_searchManager);
        m_searchManager.updateMatchCount(1);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        // hit enter, nothing should really happen
        hitEnterInInput(m_searchManager);
        m_searchManager.updateMatchCount(1);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
    }

    @Test
    public void testSelectingItemInAutocompleteBoxThenSearchingAgain() throws Exception {
        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(15);

        typeCharacter(m_searchManager, (char)KeyCodes.KEY_DOWN);
        m_searchManager.updateMatchCount(15);

        hitEnterInAutocompleteField(m_searchManager);
        m_searchManager.updateMatchCount(1);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());
        assertEquals(false, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());

        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(15);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
    }

    @Test
    public void testInitializingWithHistory() throws Exception {
        m_mockHistory.setValue("search/ae");
        m_searchManager = new MockSearchStateManager(m_mockSearchInput, m_mockHistory, m_eventManager, m_componentTracker);
        assertEquals(State.SEARCHING_FINISHED, m_searchManager.getState());

        typeCharacter(m_searchManager, 'a');
        m_searchManager.updateMatchCount(1);
        assertEquals(State.SEARCHING_AUTOCOMPLETE_VISIBLE, m_searchManager.getState());
        assertEquals(true, m_searchManager.isAutocompleteVisible());
        assertEquals(false, m_searchManager.isAutocompleteFocused());
        assertEquals(true, m_searchManager.isInputFocused());
        assertEquals("aea", m_mockSearchInput.getValue());
    }

    protected void clickAutocompleteEntry(final MockSearchStateManager searchManager) throws Exception {
        searchManager.handleAutocompleteEvent(createEvent("click", 0));
    }

    protected void hitEnterInAutocompleteField(final MockSearchStateManager searchManager) throws Exception {
        searchManager.handleAutocompleteEvent(createEvent("keydown", KeyCodes.KEY_ENTER));
    }

    protected void hitCancelX(final MockSearchStateManager searchManager) throws Exception {
        m_mockSearchInput.setValue("");
        searchManager.handleInputEvent(createEvent("search", 0));
    }

    protected void hitEnterInInput(final MockSearchStateManager searchManager) throws Exception {
        searchManager.handleInputEvent(createEvent("keydown", KeyCodes.KEY_ENTER));
    }

    protected void typeCharacter(final MockSearchStateManager searchManager, final char keyCode) throws Exception {
        final String value = m_mockSearchInput.getValue();
        if ("".equals(value) || value == null) {
            m_mockSearchInput.setValue("" + keyCode);
        } else if (keyCode == KeyCodes.KEY_BACKSPACE) {
            if (value.length() > 0) {
                m_mockSearchInput.setValue(value.substring(0, value.length() - 1));
                LOG.fine("backspace!  old=" + value + ", new=" + m_mockSearchInput.getValue());
            }
        } else {
            m_mockSearchInput.setValue(value + keyCode);
        }
        searchManager.handleInputEvent(createEvent("keydown", keyCode));
    }

    protected NativeEvent createEvent(final String type, final int keyCode) throws Exception {
        final NativeEvent event = PowerMock.createMock(NativeEvent.class);
        expect(event.getType()).andReturn(type).anyTimes();
        expect(event.getKeyCode()).andReturn(keyCode).anyTimes();
        event.stopPropagation();
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                LOG.info("stopPropagation() called on event(" + type + ", " + keyCode + ")");
                return null;
            }
        }).anyTimes();
        PowerMock.replay(event);
        return event;
    }

    private static final class TestValueItem implements ValueItem {
        private String m_value = "";

        @Override public String getValue() { return m_value; }
        @Override public void setValue(final String value) { m_value = value; }
        @Override public String toString() { return "TestValueItem [value=" + m_value + "]"; }
    }

    private static class MockSearchStateManager extends SearchStateManager {
        public MockSearchStateManager(final ValueItem searchString, final ValueItem history, final OpenNMSEventManager eventManager, final ComponentTracker componentTracker) {
            super(searchString, history, eventManager, componentTracker);
        }

        private boolean m_autocompleteVisible = false;
        private boolean m_autocompleteFocused = false;
        private boolean m_inputFocused;

        @Override public void refresh() {
            System.err.println("refreshing data!");
        }

        @Override public void focusInput() {
            System.err.println("input focused!");
            m_inputFocused = true;
            m_autocompleteFocused = false;
        }

        @Override public void focusAutocompleteWidget() {
            System.err.println("focusing autocomplete!");
            m_inputFocused = false;
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
        
        @Override protected void sendSearchStringSetEvent(final String searchString) {
            System.err.println("sending search string updated event: '" + searchString + "'");
        }

        public boolean isInputFocused() {
            return m_inputFocused;
        }

        public boolean isAutocompleteVisible() {
            return m_autocompleteVisible;
        }

        public Object isAutocompleteFocused() {
            return m_autocompleteFocused;
        }

    }

}
