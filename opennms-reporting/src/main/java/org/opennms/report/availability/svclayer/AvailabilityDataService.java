package org.opennms.report.availability.svclayer;

import java.util.List;

import org.opennms.report.datablock.Node;

public interface AvailabilityDataService {
    
    public List<Node> getNodes(org.opennms.netmgt.config.categories.Category category, long startTime, long endTime) throws AvailabilityDataServiceException;

}
