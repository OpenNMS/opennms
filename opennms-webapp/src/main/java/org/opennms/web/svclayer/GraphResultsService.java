package org.opennms.web.svclayer;

import org.opennms.web.graph.GraphResults;

public interface GraphResultsService {
    public GraphResults findResults(String graphType,
            String parentResourceType, String parentResource,
            String resourceType, String resource, String[] reports,
            long start, long end, String relativeTime);
}
