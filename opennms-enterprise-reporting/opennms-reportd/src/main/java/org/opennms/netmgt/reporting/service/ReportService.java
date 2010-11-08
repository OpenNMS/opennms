package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;
    
/**
 * <p>ReportService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ReportService {

    /**
     * <p>runReport</p>
     *
     * @param report a {@link org.opennms.netmgt.config.reportd.Report} object.
     * @param reportDirectory a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws ReportRunException 
     */
    public String runReport(Report report,String reportDirectory) throws ReportRunException;

}