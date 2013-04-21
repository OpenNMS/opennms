package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

public interface SearchConsumer {
    public String getSearchString();
    public void setSearchString(final String searchString);
    public int getMinimumSeverity();
    public void setMinimumSeverity(final int minSeverity);
    public boolean isSearching();
    public void refresh();
    public void clearSearch();
}
