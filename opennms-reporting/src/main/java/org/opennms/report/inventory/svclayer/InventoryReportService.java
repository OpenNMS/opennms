package org.opennms.report.inventory.svclayer;

/**
 * <p>InventoryReportService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface InventoryReportService {
    
    /**
     * <p>runReport</p>
     *
     * @param criteria a {@link org.opennms.report.inventory.svclayer.InventoryReportCriteria} object.
     * @return a boolean.
     */
    public boolean runReport(InventoryReportCriteria criteria);

}
