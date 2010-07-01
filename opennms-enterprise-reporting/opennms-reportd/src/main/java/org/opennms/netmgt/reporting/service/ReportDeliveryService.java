package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;

/**
 * <p>ReportDeliveryService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ReportDeliveryService {
    
    /**
     * <p>deliverReport</p>
     *
     * @param report a {@link org.opennms.netmgt.config.reportd.Report} object.
     * @param fileName a {@link java.lang.String} object.
     */
    public void deliverReport(Report report,String fileName);

}
