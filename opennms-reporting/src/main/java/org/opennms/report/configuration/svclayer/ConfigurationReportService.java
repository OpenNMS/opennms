package org.opennms.report.configuration.svclayer;

/**
 * <p>ConfigurationReportService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ConfigurationReportService {
        
    /**
     * <p>runReport</p>
     *
     * @param criteria a {@link org.opennms.report.configuration.svclayer.ConfigurationReportCriteria} object.
     * @return a boolean.
     */
    public boolean runReport(ConfigurationReportCriteria criteria);

}
