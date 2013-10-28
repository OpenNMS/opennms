package org.opennms.web.filter;

import java.util.List;

public interface QueryParameters {
    String getSortStyleShortName();
    NormalizedAcknowledgeType getAckType();
    List<Filter> getFilters();
    int getLimit();
    int getMultiple();
    String getDisplay();
}
