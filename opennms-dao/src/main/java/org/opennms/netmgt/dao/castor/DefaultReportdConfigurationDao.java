/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.util.List;

import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.dao.api.ReportdConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>DefaultReportdConfigurationDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultReportdConfigurationDao extends AbstractCastorConfigDao<ReportdConfiguration, ReportdConfiguration> implements ReportdConfigurationDao {

    /**
     * <p>Constructor for DefaultReportdConfigurationDao.</p>
     */
    public DefaultReportdConfigurationDao() {
        super(ReportdConfiguration.class, "Reportd Configuration");
    }
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     */
    @Override
    public ReportdConfiguration getConfig() {
        return getContainer().getObject();
    }
    
    //@Override
    /**
     * <p>translateConfig</p>
     *
     * @param castorConfig a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     * @return a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     */
    @Override
    public ReportdConfiguration translateConfig(ReportdConfiguration castorConfig) {
        return castorConfig;
    }
    
    
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
    }
    
    /** {@inheritDoc} */
    @Override
    public Report getReport(String reportName) {
        for (Report report : getReports()) {
            if (report.getReportName().equals(reportName)) {
                return report;
            }
        }
        return null;
    }
    
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Report> getReports() {
        return getConfig().getReportCollection();
    }

    /**
     * <p>getPersistFlag</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean getPersistFlag() {
        
        //return  getConfig().getPersistReports();
        String strval = getConfig().getPersistReports();
        boolean retval=false;
        if(strval.equals("yes") || strval.equals("on") ){
            retval = true;
        }
        
        else if(strval.equals("off") ||strval.equals("no")){
          retval = false; 
        }
        
        return retval;
    }

    /**
     * <p>getStorageDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStorageDirectory() {
        return getConfig().getStorageLocation();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean deleteReport(String report){
        return getConfig().removeReport(getReport(report));
    }
    
        
}
