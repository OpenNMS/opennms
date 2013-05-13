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

package org.opennms.report.configuration.svclayer;


import org.opennms.report.configuration.ConfigurationReportRunner;

/**
 * <p>DefaultConfigurationReportService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultConfigurationReportService implements ConfigurationReportService {

    ConfigurationReportRunner m_reportRunner;
    
    /**
     * <p>getReportRunner</p>
     *
     * @return a {@link org.opennms.report.configuration.ConfigurationReportRunner} object.
     */
    public ConfigurationReportRunner getReportRunner() {
        return m_reportRunner;
    }
    
    /**
     * <p>setReportRunner</p>
     *
     * @param reportRunner a {@link org.opennms.report.configuration.ConfigurationReportRunner} object.
     */
    public void setReportRunner(ConfigurationReportRunner reportRunner) {
        m_reportRunner = reportRunner;
    }
        
    /** {@inheritDoc} */
    @Override
    public boolean runReport(ConfigurationReportCriteria criteria){
    
          m_reportRunner.setUser(criteria.getUser());
          m_reportRunner.setTheDate(criteria.getTheDate());
          m_reportRunner.setReportEmail(criteria.getReportEmail());
          m_reportRunner.setReportFormat(criteria.getReportFormat());
          m_reportRunner.setReportRequestDate(criteria.getReportRequestDate());
            
          new Thread(m_reportRunner, m_reportRunner.getClass().getSimpleName()).start();                    
    
          return true;
    }   

}
