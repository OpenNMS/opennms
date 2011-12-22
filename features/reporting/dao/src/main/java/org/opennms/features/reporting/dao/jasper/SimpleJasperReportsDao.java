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

package org.opennms.features.reporting.dao.jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.jasperreport.JasperReportDefinition;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportsDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJasperReportsDao implements LocalReportsDao, LocalJasperReportsDao {
    
    private Logger logger = LoggerFactory.getLogger(SimpleJasperReportsDao.class);
    
    private final String SIMPLE_JASPER_REPORTS_CONFIG_XML = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "simple-jasper-reports.xml";

    private SimpleJasperReportsDefinition reports;

    public SimpleJasperReportsDao() {
        try {
            reports = JAXB.unmarshal(new File(SIMPLE_JASPER_REPORTS_CONFIG_XML),
                                     SimpleJasperReportsDefinition.class);
            logger.debug("file '{}' unmarshalled: '{}' repotrs.", SIMPLE_JASPER_REPORTS_CONFIG_XML, reports.getReportList().size());
        } catch (Exception e) {
            logger.error("unmarshal of file '{}' faild: '{}'", SIMPLE_JASPER_REPORTS_CONFIG_XML, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getTemplateLocation(String id) {
        for (JasperReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getTemplate();
            }
        }
        return null;
    }

    @Override
    public String getEngine(String id) {
        String result = "null";
        for (JasperReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                result = report.getEngine();
            }
        }
        return result;
    }
    
    @Override
    public InputStream getTemplateStream(String id) {
        InputStream reportTemplateStream = null;
        for (JasperReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                try {
                    reportTemplateStream = new FileInputStream(new File(URI.create(report.getTemplate())));
                } catch (FileNotFoundException e) {
                    logger.error("getTemplateStream faild, file '{}' not fould: '{}'", report.getTemplate(), e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return reportTemplateStream;
    }

    @Override
    public List<BasicReportDefinition> getReports() {
        List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        resultList.addAll(reports.getReportList());
        return resultList;
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        for (BasicReportDefinition report : reports.getReportList()) {
            if (report.getOnline()) {
                resultList.add(report);
            }
        }
        return resultList;
    }

    @Override
    public String getReportService(String id) {
        String result = "";
        for (BasicReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                result = report.getReportService();
            }
        }
        return result;
    }

    @Override
    public String getDisplayName(String id) {
        String result = "";
        for (BasicReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                result = report.getDescription();
            }
        }
        return result;
    }

    public SimpleJasperReportsDefinition getSimpleJasperReportsDefinitionOnlineReports() {
        return reports;
    }

    public SimpleJasperReportsDefinition getSimpleJasperReportsDefinitionReports() {
        return reports;
    }
}
