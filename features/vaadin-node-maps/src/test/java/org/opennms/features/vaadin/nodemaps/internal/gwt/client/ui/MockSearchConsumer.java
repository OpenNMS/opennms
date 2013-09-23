package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;

public class MockSearchConsumer implements SearchConsumer {
    private String m_search;
    private int m_minimumSeverity;

    public MockSearchConsumer(final String search, final int minimumSeverity) {
        m_search = search;
        m_minimumSeverity = minimumSeverity;
    }

    @Override
    public String getSearchString() {
        return m_search;
    }

    @Override
    public void setSearchString(String searchString) {
        m_search = searchString;
    }

    @Override
    public int getMinimumSeverity() {
        return m_minimumSeverity;
    }

    @Override
    public void setMinimumSeverity(int minSeverity) {
        m_minimumSeverity = minSeverity;
    }

    @Override
    public boolean isSearching() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void clearSearch() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
