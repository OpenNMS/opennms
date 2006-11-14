package org.opennms.web.svclayer;

import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.ResourceId;

public interface GraphResultsService {
    public GraphResults findResults(ResourceId[] resources,
            String[] reports,
            long start, long end, String relativeTime);
}
