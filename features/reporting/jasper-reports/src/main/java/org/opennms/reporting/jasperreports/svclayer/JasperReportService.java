/*
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2009 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. Modifications: Created: March 30th,
 * 2010 jonathan@opennms.org Update: November 22nd, 2010 jonathan@opennms.org
 * Now supports parameters Copyright (C) 2010 The OpenNMS Group, Inc. All
 * rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have
 * received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place -
 * Suite 330, Boston, MA 02111-1307, USA. For more information contact:
 * OpenNMS Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 */
package org.opennms.reporting.jasperreports.svclayer;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
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
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.JasperReportConfigDao;

/**
 * <p>
 * JasperReportService class.
 * </p>
 * 
 * @author jonathan@opennms.org
 * @version $Id: $
 */
public class JasperReportService implements ReportService {

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private JasperReportConfigDao m_jasperReportConfigDao;

    private final ThreadCategory log;

    /**
     * <p>
     * Constructor for JasperReportService.
     * </p>
     */
    public JasperReportService() {
        String oldPrefix = ThreadCategory.getPrefix();
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(JasperReportService.class);
        ThreadCategory.setPrefix(oldPrefix);
    }

    /** {@inheritDoc} */
    public List<ReportFormat> getFormats(String reportId) {
        List<ReportFormat> formats = new ArrayList<ReportFormat>();
        formats.add(ReportFormat.PDF);
        return formats;
    }

    /** {@inheritDoc} */
    public ReportParameters getParameters(String reportId) {

        ReportParameters reportParameters = new ReportParameters();
        ArrayList<ReportIntParm> intParms;
        ArrayList<ReportFloatParm> floatParms;
        ArrayList<ReportStringParm> stringParms;
        ArrayList<ReportDateParm> dateParms;

        JRParameter[] reportParms;

        JasperReport jasperReport = null;

        String sourceFileName = m_jasperReportConfigDao.getTemplateLocation(reportId);
        if (sourceFileName != null) {
            try {
                jasperReport = JasperCompileManager.compileReport(System.getProperty("opennms.home")
                        + "/etc/report-templates/" + sourceFileName);
            } catch (JRException e) {
                log.error("unable to compile jasper report", e);
                // throw new ReportException("unable to compile jasperReport",
                // e);
            }
        }

        reportParms = jasperReport.getParameters();

        intParms = new ArrayList<ReportIntParm>();
        reportParameters.setIntParms(intParms);
        floatParms = new ArrayList<ReportFloatParm>();
        reportParameters.setFloatParms(floatParms);
        stringParms = new ArrayList<ReportStringParm>();
        reportParameters.setStringParms(stringParms);
        dateParms = new ArrayList<ReportDateParm>();
        reportParameters.setDateParms(dateParms);

        for (JRParameter reportParm : reportParms) {

            if (reportParm.isSystemDefined() == false) {

                if (reportParm.getValueClassName().equals("java.lang.String")) {
                    log.debug("adding a string parm name "
                            + reportParm.getName());
                    ReportStringParm stringParm = new ReportStringParm();
                    if (reportParm.getDescription() != null) {
                        stringParm.setDisplayName(reportParm.getDescription());
                    } else {
                        stringParm.setDisplayName(reportParm.getName());
                    }
                    stringParm.setName(reportParm.getName());
                    stringParm.setValue(new String());
                    stringParms.add(stringParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                    log.debug("adding a Integer parm name "
                            + reportParm.getName());
                    ReportIntParm intParm = new ReportIntParm();
                    if (reportParm.getDescription() != null) {
                        intParm.setDisplayName(reportParm.getDescription());
                    } else {
                        intParm.setDisplayName(reportParm.getName());
                    }
                    intParm.setName(reportParm.getName());
                    intParm.setValue(new Integer(0));
                    intParms.add(intParm);
                    continue;
                }
                
                if (reportParm.getValueClassName().equals("java.lang.Float")) {
                    log.debug("adding a Float parm name "
                            + reportParm.getName());
                    ReportFloatParm floatParm = new ReportFloatParm();
                    if (reportParm.getDescription() != null) {
                        floatParm.setDisplayName(reportParm.getDescription());
                    } else {
                        floatParm.setDisplayName(reportParm.getName());
                    }
                    floatParm.setName(reportParm.getName());
                    floatParm.setValue(new Float(0));
                    floatParms.add(floatParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.util.Date")) {
                    log.debug("adding a java.util.Date parm name "
                            + reportParm.getName());
                    ReportDateParm dateParm = new ReportDateParm();
                    dateParm.setUseAbsoluteDate(false);
                    if (reportParm.getDescription() != null) {
                        dateParm.setDisplayName(reportParm.getDescription());
                    } else {
                        dateParm.setDisplayName(reportParm.getName());
                    }
                    dateParm.setName(reportParm.getName());
                    dateParm.setCount(new Integer(1));
                    dateParm.setInterval("day");
                    dateParm.setHours(0);
                    dateParm.setMinutes(0);
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    dateParm.setDate(cal.getTime());
                    dateParms.add(dateParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Date") ||
                        reportParm.getValueClassName().equals("java.sql.Timestamp") ) {
                    log.debug("adding a java.sql.Date or Timestamp parm name "
                            + reportParm.getName());
                    ReportDateParm dateParm = new ReportDateParm();
                    dateParm.setUseAbsoluteDate(false);
                    if (reportParm.getDescription() != null) {
                        dateParm.setDisplayName(reportParm.getDescription());
                    } else {
                        dateParm.setDisplayName(reportParm.getName());
                    }
                    dateParm.setName(reportParm.getName());
                    dateParm.setCount(new Integer(1));
                    dateParm.setInterval("day");
                    dateParm.setHours(0);
                    dateParm.setMinutes(0);
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    dateParm.setDate(cal.getTime());
                    dateParms.add(dateParm);
                    continue;
                }

                // throw new
                // ReportException("Unsupported report parameter type "
                // + reportParm.getValueClassName());

            }
        }

        return reportParameters;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public String run(HashMap<String, Object> onmsReportParms, String reportId)
            throws ReportException {

        String baseDir = System.getProperty("opennms.report.dir");
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        String outputFileName = null;
        String sourceFileName = m_jasperReportConfigDao.getTemplateLocation(reportId);
        HashMap<String, Object> jrReportParms;

        if (sourceFileName != null) {

            try {
                jasperReport = JasperCompileManager.compileReport(System.getProperty("opennms.home")
                        + "/etc/report-templates/" + sourceFileName);
            } catch (JRException e) {
                log.error("unable to compile jasper report", e);
                throw new ReportException("unable to compile jasperReport", e);
            }

            jrReportParms = buildJRparameters(onmsReportParms,
                                              jasperReport.getParameters());

            outputFileName = new String(baseDir + "/"
                    + jasperReport.getName() + ".jrpxml");
            log.debug("jrpcml output file: " + outputFileName);
            if (m_jasperReportConfigDao.getEngine(reportId).equals("jdbc")) {
                Connection connection;
                try {
                    connection = DataSourceFactory.getDataSource().getConnection();
                    jasperPrint = JasperFillManager.fillReport(jasperReport,
                                                               jrReportParms,
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
                                                               jrReportParms,
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

    /** {@inheritDoc} */
    public void runAndRender(HashMap<String, Object> onmsReportParms,
            String reportId, ReportFormat format, OutputStream outputStream)
            throws ReportException {

        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        HashMap<String, Object> jrReportParms;

        String sourceFileName = m_jasperReportConfigDao.getTemplateLocation(reportId);
        if (sourceFileName != null) {
            try {
                jasperReport = JasperCompileManager.compileReport(System.getProperty("opennms.home")
                        + "/etc/report-templates/" + sourceFileName);
            } catch (JRException e) {
                log.error("unable to compile jasper report", e);
                throw new ReportException("unable to compile jasperReport", e);
            }

            jrReportParms = buildJRparameters(onmsReportParms,
                                              jasperReport.getParameters());

            if (m_jasperReportConfigDao.getEngine(reportId).equals("jdbc")) {
                Connection connection;
                try {
                    connection = DataSourceFactory.getDataSource().getConnection();
                    jasperPrint = JasperFillManager.fillReport(jasperReport,
                                                               jrReportParms,
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
                                                               jrReportParms,
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

    private HashMap<String, Object> buildJRparameters(
            HashMap<String, Object> onmsReportParms, JRParameter[] reportParms)
            throws ReportException {

        HashMap<String, Object> jrReportParms = new HashMap<String, Object>();

        for (JRParameter reportParm : reportParms) {
            log.debug("found report parm " + reportParm.getName()
                    + " of class " + reportParm.getValueClassName());
            if (reportParm.isSystemDefined() == false) {

                String parmName = reportParm.getName();

                if (onmsReportParms.containsKey(parmName) == false)
                    throw new ReportException("Required parameter "
                            + parmName
                            + " not supplied to JasperReports by OpenNMS");

                if (reportParm.getValueClassName().equals("java.lang.String")) {
                    jrReportParms.put(
                                      parmName,
                                      new String(
                                                 (String) onmsReportParms.get(parmName)));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                    jrReportParms.put(
                                      parmName,
                                      new Integer(
                                                  (Integer) onmsReportParms.get(parmName)));
                    continue;
                }
                
                if (reportParm.getValueClassName().equals("java.lang.Float")) {
                    jrReportParms.put(
                                      parmName,
                                      new Float(
                                                  (Float) onmsReportParms.get(parmName)));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.util.Date")) {
                    Date date = (Date) onmsReportParms.get(parmName);
                    jrReportParms.put(parmName, new Date(date.getTime()));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Date")) {
                    Date date = (Date) onmsReportParms.get(parmName);
                    jrReportParms.put(parmName,
                                      new java.sql.Date(date.getTime()));
                    continue;
                }
                
                if (reportParm.getValueClassName().equals("java.sql.Timestamp")) {
                    Date date = (Date) onmsReportParms.get(parmName);
                    jrReportParms.put(parmName,
                                      new java.sql.Timestamp(date.getTime()));
                    continue;
                }

                throw new ReportException(
                                          "Unsupported report parameter type "
                                                  + reportParm.getValueClassName());

            }
        }

        return jrReportParms;

    }

    /** {@inheritDoc} */
    public boolean validate(HashMap<String, Object> reportParms,
            String reportId) {
        // returns true until we can take parameters
        return true;
    }

    /**
     * <p>
     * setConfigDao
     * </p>
     * 
     * @param jasperReportConfigDao
     *            a {@link org.opennms.netmgt.dao.JasperReportConfigDao}
     *            object.
     */
    public void setConfigDao(JasperReportConfigDao jasperReportConfigDao) {
        m_jasperReportConfigDao = jasperReportConfigDao;
    }

}
