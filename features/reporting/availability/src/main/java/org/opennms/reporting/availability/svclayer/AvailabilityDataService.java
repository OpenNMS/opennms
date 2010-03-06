package org.opennms.reporting.availability.svclayer;

import java.util.List;

import org.opennms.reporting.datablock.Node;

public interface AvailabilityDataService {
    
    public List<Node> getNodes(org.opennms.netmgt.config.categories.Category category, long startTime, long endTime) throws AvailabilityDataServiceException;

}
