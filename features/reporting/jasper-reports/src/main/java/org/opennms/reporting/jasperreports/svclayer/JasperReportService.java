/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 30th, 2010 jonathan@opennms.org
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.reporting.jasperreports.svclayer;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;

import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.JasperReportConfigDao;

public class JasperReportService implements ReportService {

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private JasperReportConfigDao m_jasperReportConfigDao;

    private final ThreadCategory log;

    public JasperReportService() {
        String oldPrefix = ThreadCategory.getPrefix();
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(JasperReportService.class);
        ThreadCategory.setPrefix(oldPrefix);
    }

    public List<ReportFormat> getFormats(String reportId) {
        List<ReportFormat> formats = new ArrayList<ReportFormat>();
        formats.add(ReportFormat.PDF);
        return formats;
    }

    public ReportParameters getParameters(String ReportId) {
        return new ReportParameters();
    }

    public void render(String ReportId, String location, ReportFormat format,
            OutputStream outputStream) throws ReportException {
        try {
            JasperPrint jasperPrint = JRPrintXmlLoader.load(location);
            switch (format) {
            case PDF:
                log.debug("rendering as PDF");
                JasperExportManager.exportReportToPdfStream(jasperPrint,
                                                            outputStream);
                break;
            default:
                log.debug("rendering as PDF as no valid format found");
                JasperExportManager.exportReportToPdfStream(jasperPrint,
                                                            outputStream);
            }
        } catch (JRException e) {
            log.error("unable to render report", e);
            throw new ReportException("unable to render report", e);
        }

    }

    public String run(HashMap<String, Object> reportParms, String reportId)
            throws ReportException {
        String baseDir = System.getProperty("opennms.report.dir");
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        String outputFileName = null;
        String sourceFileName = m_jasperReportConfigDao.getTemplateLocation(reportId);
        if (sourceFileName != null) {

            try {
                jasperReport = JasperCompileManager.compileReport(System.getProperty("opennms.home")
                        + "/etc/report-templates/" + sourceFileName);
            } catch (JRException e) {
                log.error("unable to compile jasper report", e);
                throw new ReportException("unable to compile jasperReport", e);
            }
            outputFileName = new String(baseDir + "/"
                    + jasperReport.getName() + ".jrpxml");
            log.debug("jrpcml output file: " + outputFileName);
            if (m_jasperReportConfigDao.getEngine(reportId).equals("jdbc")) {
                Connection connection;
                try {
                    connection = DataSourceFactory.getDataSource().getConnection();
                    jasperPrint = JasperFillManager.fillReport(jasperReport,
                                                               reportParms,
                                                               connection);
                    JRXmlExporter exporter = new JRXmlExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                                          jasperPrint);
                    exporter.setParameter(
                                          JRExporterParameter.OUTPUT_FILE_NAME,
                                          outputFileName);
                    exporter.exportReport();
                    connection.close();
                } catch (SQLException e) {
                    log.error("sql exception getting or closing datasource ",
                              e);
                    throw new ReportException(
                                              "sql exception getting or closing datasource",
                                              e);
                } catch (JRException e) {
                    log.error("jasper report exception ", e);
                    throw new ReportException(
                                              "unable to run emptyDataSource jasperReport",
                                              e);
                }
            } else if (m_jasperReportConfigDao.getEngine(reportId).equals(
                                                                          "null")) {
                try {
                    jasperPrint = JasperFillManager.fillReport(
                                                               jasperReport,
                                                               reportParms,
                                                               new JREmptyDataSource());
                    JRXmlExporter exporter = new JRXmlExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                                          jasperPrint);
                    exporter.setParameter(
                                          JRExporterParameter.OUTPUT_FILE_NAME,
                                          outputFileName);
                    exporter.exportReport();
                } catch (JRException e) {
                    log.error("jasper report exception ", e);
                    throw new ReportException(
                                              "unable to run emptyDataSource jasperReport",
                                              e);
                }

            } else {
                throw new ReportException(
                                          "no suitable datasource configured for reportId: "
                                                  + reportId);
            }
        }

        return outputFileName;
    }

    public void runAndRender(HashMap<String, Object> reportParms,
            String reportId, ReportFormat format, OutputStream outputStream)
            throws ReportException {

        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        String sourceFileName = m_jasperReportConfigDao.getTemplateLocation(reportId);
        if (sourceFileName != null) {
            try {
                jasperReport = JasperCompileManager.compileReport(System.getProperty("opennms.home")
                        + "/etc/report-templates/" + sourceFileName);
            } catch (JRException e) {
                log.error("unable to compile jasper report", e);
                throw new ReportException("unable to compile jasperReport", e);
            }
            if (m_jasperReportConfigDao.getEngine(reportId).equals("jdbc")) {
                Connection connection;
                try {
                    connection = DataSourceFactory.getDataSource().getConnection();
                    jasperPrint = JasperFillManager.fillReport(jasperReport,
                                                               reportParms,
                                                               connection);
                    JasperExportManager.exportReportToPdfStream(jasperPrint,
                                                                outputStream);
                    connection.close();
                } catch (SQLException e) {
                    log.error("sql exception getting or closing datasource ",
                              e);
                    throw new ReportException(
                                              "sql exception getting or closing datasource",
                                              e);
                } catch (JRException e) {
                    log.error("jasper report exception ", e);
                    throw new ReportException(
                                              "unable to run or render jdbc jasperReport",
                                              e);
                }
            } else if (m_jasperReportConfigDao.getEngine(reportId).equals(
                                                                          "null")) {
                try {
                    jasperPrint = JasperFillManager.fillReport(
                                                               jasperReport,
                                                               reportParms,
                                                               new JREmptyDataSource());
                    JasperExportManager.exportReportToPdfStream(jasperPrint,
                                                                outputStream);
                } catch (JRException e) {
                    log.error("jasper report exception ", e);
                    throw new ReportException(
                                              "unable to run or render emptyDataSource jasperReport",
                                              e);
                }

            }

        }

    }

    public boolean validate(HashMap<String, Object> reportParms,
            String reportId) {
        // returns true until we can take parameters
        return true;
    }

    public void setConfigDao(JasperReportConfigDao jasperReportConfigDao) {
        m_jasperReportConfigDao = jasperReportConfigDao;
    }

}
