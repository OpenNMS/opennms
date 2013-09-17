package org.opennms.web.filter;

import java.util.ArrayList;
import java.util.List;

public class NormalizedQueryParameters implements QueryParameters {

    private String sortStyleShortName;
    private NormalizedAcknowledgeType ackType;
    private List<Filter> filters;
    private int limit;
    private int multiple;
    private String display;

    public NormalizedQueryParameters(QueryParameters queryParms) {
        this.sortStyleShortName = queryParms.getSortStyleShortName();
        this.ackType = queryParms.getAckType();
        this.display = queryParms.getDisplay();
        this.filters = new ArrayList<Filter>(queryParms.getFilters());
        this.limit = queryParms.getLimit();
        this.multiple = queryParms.getMultiple();
    }

    public NormalizedQueryParameters() {
        filters = new ArrayList<Filter>();
    }

    @Override
    public String getSortStyleShortName() {
        return sortStyleShortName;
    }

    @Override
    public NormalizedAcknowledgeType getAckType() {
        return ackType;
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getMultiple() {
        return multiple;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    public void setSortStyleShortName(String sortStyleShortName) {
        this.sortStyleShortName = sortStyleShortName;
    }

    public void setAckType(NormalizedAcknowledgeType ackType) {
        this.ackType = ackType;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
