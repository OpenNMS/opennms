package org.opennms.web.outage.filter;

/** Convenience class to determine what sort of notices to include in a query. */
public interface Filter {
    public String getSql();
    public String getDescription();
    public String getTextDescription();
}



