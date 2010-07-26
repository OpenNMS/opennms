package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.config.reportd.Report;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>ReportdConfigurationDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ReportdConfigurationDao {
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     */
    ReportdConfiguration getConfig();
    
    /**
     * <p>getReport</p>
     *
     * @param defName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.reportd.Report} object.
     */
    Report getReport(String defName);
    
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<Report> getReports();
        
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    void reloadConfiguration() throws DataAccessResourceFailureException;

    /**
     * <p>getPersistFlag</p>
     *
     * @return a boolean.
     */
    boolean  getPersistFlag();
    
    /**
     * <p>getStorageDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getStorageDirectory();
    
    /**
     * <p>deleteReport</p>
     *
     * @param reportName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean deleteReport(String reportName);
    
    
}
