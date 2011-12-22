/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLocalReportsDao implements LocalReportsDao {

    private Logger logger = LoggerFactory.getLogger(LegacyLocalReportsDao.class);
    private final String LOCAL_REPORTS_CONFIG_XML = 
            System.getProperty("opennms.home") + 
            File.separator + 
            "etc" + 
            File.separator + 
            "local-reports.xml";
    
    private LegacyLocalReportsDefinition m_reports;
    
    public LegacyLocalReportsDao() {
        try {
            m_reports = JAXB.unmarshal(new File(LOCAL_REPORTS_CONFIG_XML), LegacyLocalReportsDefinition.class);
        }catch (Exception e) {
            // TODO Tak: fail safety
            logger.error("Unmarshal Failed for '{}'", LOCAL_REPORTS_CONFIG_XML);
            logger.error("Returning blank new LegacyLocalReportsDefinition");
            e.printStackTrace();
            m_reports = new LegacyLocalReportsDefinition();
        }
    }
    
    @Override
    public List<BasicReportDefinition> getReports() {
        ArrayList<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        for (BasicReportDefinition report : m_reports.getReportList()) {
            resultList.add(report);
        }
        return resultList;
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
       List<BasicReportDefinition> onlineReports = new ArrayList<BasicReportDefinition>();
       for (BasicReportDefinition report : m_reports.getReportList()) {
           if (report.getOnline()) {
               onlineReports.add(report);
           }
       }
       return onlineReports;
    }

    @Override
    public String getReportService(String id) {
        for (BasicReportDefinition report : m_reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getReportService();
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(String id) {
        for (BasicReportDefinition report : m_reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getDisplayName();
            }
        }
        return null;
    }
    
}
