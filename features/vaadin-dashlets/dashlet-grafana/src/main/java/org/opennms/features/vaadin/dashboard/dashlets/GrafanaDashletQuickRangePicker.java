/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * This class implements a simple range picker for Grafna Quick Ranges.
 *
 * @author Christian Pape
 */
public class GrafanaDashletQuickRangePicker extends HorizontalLayout {
    /**
     * the number of columns
     */
    private final int quickRangeColumns = 4;
    /**
     * the quick range entries by column
     */
    private final Map<String, QuickRange> quickRangeColumn[] = new TreeMap[quickRangeColumns];
    /**
     * the listeners
     */
    private final List<QuickRangeListener> quickRangeListeners = new ArrayList<>();
    /**
     * mapping of buttons
     */
    private final Map<QuickRange, Button> buttonMap = new HashMap<>();
    /**
     * the currently selected button
     */
    private Button selectedButton;

    {
        for(int i = 0; i< quickRangeColumns; i++) {
            quickRangeColumn[i] = new TreeMap<>();
        }

        quickRangeColumn[0].put("Last 2 days", new QuickRange("now-2d", "now"));
        quickRangeColumn[0].put("Last 7 days", new QuickRange("now-7d", "now"));
        quickRangeColumn[0].put("Last 30 days", new QuickRange("now-30d", "now"));
        quickRangeColumn[0].put("Last 60 days", new QuickRange("now-60d", "now"));
        quickRangeColumn[0].put("Last 6 months", new QuickRange("now-6M", "now"));
        quickRangeColumn[0].put("Last 1 year", new QuickRange("now-1y", "now"));
        quickRangeColumn[0].put("Last 2 years", new QuickRange("now-2y", "now"));
        quickRangeColumn[0].put("Last 5 years", new QuickRange("now-5y", "now"));

        quickRangeColumn[1].put("Yesterday", new QuickRange("now-1d/d", "now-1d/d"));
        quickRangeColumn[1].put("Day before yesterday", new QuickRange("now-2d/d", "now-2d/d"));
        quickRangeColumn[1].put("This day last week", new QuickRange("now-7d/d", "now-7d/d"));
        quickRangeColumn[1].put("Previous week", new QuickRange("now-1w/w", "now-1w/w"));
        quickRangeColumn[1].put("Previous month", new QuickRange("now-1M/M", "now-1M/M"));
        quickRangeColumn[1].put("Previous year", new QuickRange("now-1y/y", "now-1y/y"));

        quickRangeColumn[2].put("Today", new QuickRange("now/d", "now/d"));
        quickRangeColumn[2].put("Today so far", new QuickRange("now/d", "now"));
        quickRangeColumn[2].put("This week", new QuickRange("now/w", "now/w"));
        quickRangeColumn[2].put("This week so far", new QuickRange("now/w", "now"));
        quickRangeColumn[2].put("This month", new QuickRange("now/M", "now/M"));
        quickRangeColumn[2].put("This year", new QuickRange("now/y", "now/y"));

        quickRangeColumn[3].put("Last 5 minutes", new QuickRange("now-5m", "now"));
        quickRangeColumn[3].put("Last 15 minutes", new QuickRange("now-15m", "now"));
        quickRangeColumn[3].put("Last 30 minutes", new QuickRange("now-30m", "now"));
        quickRangeColumn[3].put("Last 1 hour", new QuickRange("now-1h", "now"));
        quickRangeColumn[3].put("Last 3 hours", new QuickRange("now-3h", "now"));
        quickRangeColumn[3].put("Last 6 hours", new QuickRange("now-6h", "now"));
        quickRangeColumn[3].put("Last 12 hours", new QuickRange("now-12h", "now"));
        quickRangeColumn[3].put("Last 24 hours", new QuickRange("now-24h", "now"));
    }

    /**
     * Default constructor
     */
    public GrafanaDashletQuickRangePicker() {
        setMargin(true);
        setSpacing(true);
        for(int i = 0; i< quickRangeColumns; i++) {
            addComponent(createLayout(quickRangeColumn[i]));
        }
    }

    private VerticalLayout createLayout(Map<String, QuickRange> quickRanges) {
        VerticalLayout verticalLayout = new VerticalLayout();

        for (final Map.Entry<String, QuickRange> entry : quickRanges.entrySet()) {
            Button button = new Button(entry.getKey());
            button.addStyleName(BaseTheme.BUTTON_LINK);
            button.addClickListener(e -> {
                for (QuickRangeListener quickRangeListener : quickRangeListeners) {
                    quickRangeListener.quickRangeSelected(entry.getValue());
                }
                selectButton(button);
            });
            verticalLayout.addComponent(button);
            buttonMap.put(entry.getValue(), button);
        }
        return verticalLayout;
    }

    private void selectButton(Button button) {
        if (selectedButton != null) {
            selectedButton.removeStyleName("caption-bold");
        }
        selectedButton = button;
        if (selectedButton != null) {
            selectedButton.addStyleName("caption-bold");
        }
    }

    /**
     * Selects a quick range entry and alters the button style.
     *
     * @param from the from value
     * @param to the to value
     */
    public void selectQuickRange(String from, String to) {
        QuickRange quickRange = new QuickRange(from, to);
        selectButton(buttonMap.get(quickRange));
    }

    /**
     * Adds a listener for value changes.
     *
     * @param quickRangeListener the listener to be added
     */
    public void addQuickRangeListener(QuickRangeListener quickRangeListener) {
        if (quickRangeListener != null) {
            quickRangeListeners.add(quickRangeListener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param quickRangeListener the listener to be removed
     */
    public void removeQuickRangeListener(QuickRangeListener quickRangeListener) {
        if (quickRangeListener != null) {
            quickRangeListeners.remove(quickRangeListener);
        }
    }

    /**
     * Listener interface
     */
    public interface QuickRangeListener {
        void quickRangeSelected(QuickRange quickRange);
    }

    /**
     * Quick range entry
     */
    public static class QuickRange {
        private final String from, to;

        public QuickRange(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QuickRange that = (QuickRange) o;

            if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) return false;
            return getTo() != null ? getTo().equals(that.getTo()) : that.getTo() == null;
        }

        @Override
        public int hashCode() {
            int result = getFrom() != null ? getFrom().hashCode() : 0;
            result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
            return result;
        }
    }
}
