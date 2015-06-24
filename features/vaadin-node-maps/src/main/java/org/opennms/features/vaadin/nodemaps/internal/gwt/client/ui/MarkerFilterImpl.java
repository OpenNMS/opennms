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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.AlarmSeverity;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ComponentTracker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilterUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchStringSetEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchStringSetEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.shared.Util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class MarkerFilterImpl implements MarkerFilter, AlarmSeverityUpdatedEventHandler, SearchStringSetEventHandler {
    private static final Logger LOG = Logger.getLogger(MarkerFilterImpl.class.getName());
    private static final RegExp m_searchPattern = RegExp.compile("^\\s*(.*?)\\s*( in |\\=|\\:)\\s*(.*)\\s*$");

    private OpenNMSEventManager m_eventManager;
    private ComponentTracker m_componentTracker;

    String m_searchString = null;
    AlarmSeverity m_minimumSeverity = AlarmSeverity.NORMAL;

    public MarkerFilterImpl(final String searchString, final AlarmSeverity minimumSeverity, final OpenNMSEventManager eventManager, final ComponentTracker componentTracker) {
        m_searchString = searchString;
        m_minimumSeverity = minimumSeverity;
        m_eventManager = eventManager;
        m_componentTracker = componentTracker;
    }

    public void onLoad() {
        m_eventManager.addHandler(SearchStringSetEvent.TYPE, this);
        m_eventManager.addHandler(AlarmSeverityUpdatedEvent.TYPE, this);
        m_componentTracker.ready(getClass());
    }

    public void onUnload() {
        m_eventManager.removeHandler(AlarmSeverityUpdatedEvent.TYPE, this);
        m_eventManager.removeHandler(SearchStringSetEvent.TYPE, this);
    }

    public void setSearchString(final String searchString) {
        if (Util.hasChanged(m_searchString, searchString)) {
            LOG.info("MarkerFilterImpl.setSearchString(" + searchString + "): search string modified (old = '" + m_searchString + "')");
            m_searchString = searchString;
            sendFilterUpdatedEvent();
        } else {
            LOG.info("MarkerFilterImpl.setSearchString(" + searchString + "): search string unmodified.");
        }
    }

    public void setMinimumSeverity(final AlarmSeverity minimumSeverity) {
        if (Util.hasChanged(m_minimumSeverity, minimumSeverity)) {
            LOG.info("MarkerFilterImpl.setMinimumSeverity(" + minimumSeverity + "): minimum severity modified (old = '" + m_minimumSeverity + "'");
            m_minimumSeverity = minimumSeverity;
            sendFilterUpdatedEvent();
        } else {
            LOG.info("MarkerFilterImpl.setMinimumSeverity(" + minimumSeverity + "): minimum severity unmodified.");
        }
    }

    void sendFilterUpdatedEvent() {
        m_eventManager.fireEvent(new FilterUpdatedEvent());
    }

    @Override
    public boolean matches(final NodeMarker marker) {
        if (marker == null) return false;

        final AlarmSeverity severity;
        if (marker.getSeverity() == null) {
            severity = AlarmSeverity.NORMAL;
        } else {
            severity = AlarmSeverity.get(marker.getSeverity());
        }
        if (severity.isLessThan(m_minimumSeverity)) return false;
        if (m_searchString == null || "".equals(m_searchString)) return true;

        final String searchProperty;
        final MatchType matchType;
        final List<String> searchFor = new ArrayList<String>();

        final MatchResult m = m_searchPattern.exec(m_searchString);
        if (m != null) {
            searchProperty = m.getGroup(1);
            matchType = MatchType.fromToken(m.getGroup(2));

            final String searchCriteria = m.getGroup(3);
            if (matchType == MatchType.IN) {
                final String ignoreParens = searchCriteria.replaceAll("^\\s*\\(\\s*(.*)\\s*\\)\\s*$", "$1");
                for (final String s : ignoreParens.split("\\s*,\\s*")) {
                    searchFor.add(s);
                }
            } else {
                searchFor.add(searchCriteria);
            }
        } else {
            searchProperty = null;
            matchType = MatchType.SUBSTRING;
            searchFor.add(m_searchString);
        }

        final Map<String, String> markerProperties = marker.getProperties();

        if (searchProperty != null) {
            return matchProperty(matchType, searchProperty, searchFor, markerProperties);
        } else {
            for (final String key : markerProperties.keySet()) {
                if (matchProperty(matchType, key, searchFor, markerProperties)) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean matchProperty(final MatchType matchType, final String searchProperty, final List<String> searchFor, final Map<String, String> searchIn) {
        final String lowerSearchProperty = searchProperty.toLowerCase();

        if ("category".equals(lowerSearchProperty) || "categories".equals(lowerSearchProperty)) {
            return matchCategory(searchIn.get("categories"), matchType, searchFor);
        } else {
            final String value = searchIn.get(lowerSearchProperty);
            if (value == null) return false;
            for (final String searchEntry : searchFor) {
                final String lowerSearch = searchEntry.toLowerCase();
                final String lowerValue = value.toLowerCase();
                if (matchType == MatchType.EXACT || matchType == MatchType.IN) {
                    if (lowerValue.equals(lowerSearch)) {
                        return true;
                    }
                } else if (matchType == MatchType.SUBSTRING) {
                    if (lowerValue.contains(lowerSearch)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    boolean matchCategory(final String categories, final MatchType matchType, final List<String> searchFor) {
        if (categories == null) return false;

        for (final String category : categories.split("\\s*,\\s*")) {
            final String categoryLower = category.toLowerCase();
            for (final String searchEntry : searchFor) {
                if (matchType == MatchType.EXACT || matchType == MatchType.IN) {
                    if (category.equalsIgnoreCase(searchEntry)) {
                        return true;
                    }
                } else if (matchType == MatchType.SUBSTRING) {
                    if (categoryLower.contains(searchEntry.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onAlarmSeverityUpdated(final AlarmSeverityUpdatedEvent event) {
        LOG.info("MarkerFilterImpl.onAlarmSeverityUpdated(" + event.getSeverity() + ")");
        setMinimumSeverity(event.getSeverity());
    }

    @Override
    public void onSearchStringSet(final SearchStringSetEvent event) {
        LOG.info("MarkerFilterImpl.onSearchStringSet(" + event.getSearchString() + ")");
        setSearchString(event.getSearchString());
    }
}
