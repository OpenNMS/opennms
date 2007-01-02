package org.opennms.web.svclayer;

import org.opennms.web.graph.GraphResults;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface GraphResultsService {
    public GraphResults findResults(String[] resources,
            String[] reports,
            long start, long end, String relativeTime);
}
