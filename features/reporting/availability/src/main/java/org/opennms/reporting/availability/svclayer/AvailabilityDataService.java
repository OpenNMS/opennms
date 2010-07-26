package org.opennms.reporting.availability.svclayer;

import java.util.List;

import org.opennms.reporting.datablock.Node;

/**
 * <p>AvailabilityDataService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface AvailabilityDataService {
    
    /**
     * <p>getNodes</p>
     *
     * @param category a {@link org.opennms.netmgt.config.categories.Category} object.
     * @param startTime a long.
     * @param endTime a long.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.reporting.availability.svclayer.AvailabilityDataServiceException if any.
     */
    public List<Node> getNodes(org.opennms.netmgt.config.categories.Category category, long startTime, long endTime) throws AvailabilityDataServiceException;

}
