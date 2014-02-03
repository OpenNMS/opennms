package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;


public class MockSearchConsumer /*implements SearchConsumer */ {
    private String m_search;
    private int m_minimumSeverity;

    public MockSearchConsumer(final String search, final int minimumSeverity) {
        m_search = search;
        m_minimumSeverity = minimumSeverity;
    }

    public String getSearchString() {
        return m_search;
    }

    public void setSearchString(String searchString) {
        m_search = searchString;
    }

    public int getMinimumSeverity() {
        return m_minimumSeverity;
    }

    public void setMinimumSeverity(int minSeverity) {
        m_minimumSeverity = minSeverity;
    }

    public boolean isSearching() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void refresh() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void clearSearch() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
