package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilterUpdatedEvent;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class MarkerFilterImpl implements MarkerFilter {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(MarkerFilterImpl.class.getName());
    private static final RegExp m_searchPattern = RegExp.compile("^\\s*(.*?)\\s*( in |\\=|\\:)\\s*(.*)\\s*$");
    private String m_searchString;
    private int m_minimumSeverity = 0;

    public MarkerFilterImpl(final String searchString, final int minimumSeverity) {
        m_searchString = searchString;
        m_minimumSeverity = minimumSeverity;
    }

    @SuppressWarnings("unused")
    private boolean hasChanged(final String a, final String b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (b == null) {
                return false;
            } else {
                return a.equals(b);
            }
        }
    }

    public void setSearchString(final String searchString) {
        // if (hasChanged(m_searchString, searchString)) {
            m_searchString = searchString;
            DomEvent.send(FilterUpdatedEvent.createEvent());
        //}
    }
    
    public void setMinimumSeverity(final int minimumSeverity) {
        // if (m_minimumSeverity != minimumSeverity) {
            m_minimumSeverity = minimumSeverity;
            DomEvent.send(FilterUpdatedEvent.createEvent());
        // }
    }

    @Override
    public boolean matches(final NodeMarker marker) {
        if (marker.getSeverity() != null && marker.getSeverity() < m_minimumSeverity) return false;
        if (m_searchString == null || "".equals(m_searchString)) return true;

        final String searchProperty;
        final MatchType matchType;
        final List<String> searchFor = new ArrayList<String>();

        final MatchResult m = m_searchPattern.exec(m_searchString);
        if (m != null) {
            searchProperty = m.getGroup(1);
            matchType = MatchType.fromToken(m.getGroup(2));

            if (matchType == MatchType.IN) {
                for (final String s : m.getGroup(3).split("\\s*,\\s*")) {
                    searchFor.add(s);
                }
            } else {
                searchFor.add(m.getGroup(3));
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

    private boolean matchProperty(final MatchType matchType, final String searchProperty, final List<String> searchFor, final Map<String, String> searchIn) {
        final String lowerSearchProperty = searchProperty.toLowerCase();

        if ("category".equals(lowerSearchProperty) || "categories".equals(lowerSearchProperty)) {
            return matchCategory(searchIn.get("categories"), matchType, searchFor);
        } else {
            final String value = searchIn.get(lowerSearchProperty);
            if (value == null) return false;
            for (final String searchEntry : searchFor) {
                if (matchType == MatchType.EXACT || matchType == MatchType.IN) {
                    if (value.toLowerCase().equals(searchEntry)) {
                        return true;
                    }
                } else if (matchType == MatchType.SUBSTRING) {
                    if (value.toLowerCase().contains(searchEntry)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean matchCategory(final String categories, final MatchType matchType, final List<String> searchFor) {
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
}
