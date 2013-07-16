/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.reporting.jasperreports.svclayer;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.fill.JRParameterDefaultValuesEvaluator;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;

import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.*;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * JasperReportService class.
 * </p>
 *
 * @author jonathan@opennms.org
 * @version $Id: $
 */
public class JasperReportService implements ReportService {
    private static final Logger LOG = LoggerFactory.getLogger(JasperReportService.class);

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String STRING_INPUT_TYPE = "org.opennms.report.stringInputType";

    private GlobalReportRepository m_globalReportRepository;

    /**
     * <p>
     * Constructor for JasperReportService.
     * </p>
     */
    public JasperReportService() {
        // TODO this should wrap calls to this class
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportFormat> getFormats(String reportId) {
        List<ReportFormat> formats = new ArrayList<ReportFormat>();
        formats.add(ReportFormat.PDF);
        formats.add(ReportFormat.CSV);
        return formats;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ReportException
     */
    @Override
    public ReportParameters getParameters(String reportId)
            throws ReportException {

        ReportParameters reportParameters = new ReportParameters();
        ArrayList<ReportIntParm> intParms;
        ArrayList<ReportFloatParm> floatParms;
        ArrayList<ReportDoubleParm> doubleParms;
        ArrayList<ReportStringParm> stringParms;
        ArrayList<ReportDateParm> dateParms;

        JRParameter[] reportParms;

        JasperReport jasperReport = null;
        Map<?, ?> defaultValues = null;

        try {
            jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
            defaultValues = JRParameterDefaultValuesEvaluator.evaluateParameterDefaultValues(jasperReport,
                    null);
        } catch (JRException e) {
            LOG.error("unable to compile jasper report", e);
            throw new ReportException("unable to compile jasperReport", e);
        }

        reportParms = jasperReport.getParameters();

        intParms = new ArrayList<ReportIntParm>();
        reportParameters.setIntParms(intParms);
        floatParms = new ArrayList<ReportFloatParm>();
        reportParameters.setFloatParms(floatParms);
        doubleParms = new ArrayList<ReportDoubleParm>();
        reportParameters.setDoubleParms(doubleParms);
        stringParms = new ArrayList<ReportStringParm>();
        reportParameters.setStringParms(stringParms);
        dateParms = new ArrayList<ReportDateParm>();
        reportParameters.setDateParms(dateParms);

        for (JRParameter reportParm : reportParms) {

            if (reportParm.isSystemDefined() == false) {

                if (reportParm.isForPrompting() == false) {
                    LOG.debug("report parm {} is not for prompting - continuing", reportParm.getName());
                    continue;
                } else {
                    LOG.debug("found promptable report parm {}", reportParm.getName());

                }

                if (reportParm.getValueClassName().equals("java.lang.String")) {
                    LOG.debug("adding a string parm name {}", reportParm.getName());
                    ReportStringParm stringParm = new ReportStringParm();
                    if (reportParm.getDescription() != null) {
                        stringParm.setDisplayName(reportParm.getDescription());
                    } else {
                        stringParm.setDisplayName(reportParm.getName());
                    }
                    if (reportParm.getPropertiesMap().containsProperty(STRING_INPUT_TYPE)) {
                        stringParm.setInputType(reportParm.getPropertiesMap().getProperty(STRING_INPUT_TYPE));
                    }
                    stringParm.setName(reportParm.getName());
                    if (defaultValues.containsKey(reportParm.getName())
                            && (defaultValues.get(reportParm.getName()) != null)) {
                        stringParm.setValue((String) defaultValues.get(reportParm.getName()));
                    } else {
                        stringParm.setValue(new String());
                    }
                    stringParms.add(stringParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                    LOG.debug("adding a Integer parm name {}", reportParm.getName());
                    ReportIntParm intParm = new ReportIntParm();
                    if (reportParm.getDescription() != null) {
                        intParm.setDisplayName(reportParm.getDescription());
                    } else {
                        intParm.setDisplayName(reportParm.getName());
                    }
                    intParm.setName(reportParm.getName());
                    if (defaultValues.containsKey(reportParm.getName())
                            && (defaultValues.get(reportParm.getName()) != null)) {
                        intParm.setValue((Integer) defaultValues.get(reportParm.getName()));
                    } else {
                        intParm.setValue(new Integer(0));
                    }
                    intParms.add(intParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Float")) {
                    LOG.debug("adding a Float parm name {}", reportParm.getName());
                    ReportFloatParm floatParm = new ReportFloatParm();
                    if (reportParm.getDescription() != null) {
                        floatParm.setDisplayName(reportParm.getDescription());
                    } else {
                        floatParm.setDisplayName(reportParm.getName());
                    }
                    floatParm.setName(reportParm.getName());
                    if (defaultValues.containsKey(reportParm.getName())
                            && (defaultValues.get(reportParm.getName()) != null)) {
                        floatParm.setValue((Float) defaultValues.get(reportParm.getName()));
                    } else {
                        floatParm.setValue(new Float(0));
                    }
                    floatParms.add(floatParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Double")) {
                    LOG.debug("adding a Double parm name {}", reportParm.getName());
                    ReportDoubleParm doubleParm = new ReportDoubleParm();
                    if (reportParm.getDescription() != null) {
                        doubleParm.setDisplayName(reportParm.getDescription());
                    } else {
                        doubleParm.setDisplayName(reportParm.getName());
                    }
                    doubleParm.setName(reportParm.getName());
                    if (defaultValues.containsKey(reportParm.getName())
                            && (defaultValues.get(reportParm.getName()) != null)) {
                        doubleParm.setValue((Double) defaultValues.get(reportParm.getName()));
                    } else {
                        doubleParm.setValue(new Double(0));
                    }
                    doubleParms.add(doubleParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.util.Date")) {
                    LOG.debug("adding a java.util.Date parm name {}", reportParm.getName());
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
                    if (defaultValues.containsKey(reportParm.getName())
                            && (defaultValues.get(reportParm.getName()) != null)) {
                        dateParm.setDate((Date) defaultValues.get(reportParm.getName()));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateParm.getDate());
                        dateParm.setMinutes(cal.get(Calendar.MINUTE));
                        dateParm.setHours(cal.get(Calendar.HOUR_OF_DAY));
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        dateParm.setDate(cal.getTime());
                    }
                    dateParms.add(dateParm);
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Date")
                        || reportParm.getValueClassName().equals("java.sql.Timestamp")) {
                    LOG.debug("adding a java.sql.Date or Timestamp parm name {}", reportParm.getName());
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
                    if (defaultValues.containsKey(reportParm.getName())
                            && (defaultValues.get(reportParm.getName()) != null)) {
                        dateParm.setDate((Date) defaultValues.get(reportParm.getName()));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateParm.getDate());
                        dateParm.setMinutes(cal.get(Calendar.MINUTE));
                        dateParm.setHours(cal.get(Calendar.HOUR_OF_DAY));
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        dateParm.setDate(cal.getTime());
                    }
                    dateParms.add(dateParm);
                    continue;
                }

                throw new ReportException(
                        "Unsupported report parameter type "
                                + reportParm.getValueClassName());

            }
        }

        return reportParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(String ReportId, String location, ReportFormat format,
                       OutputStream outputStream) throws ReportException {
        try {

            JasperPrint jasperPrint = getJasperPrint(location);

            switch (format) {
                case PDF:
                    LOG.debug("rendering as PDF");
                    exportReportToPdf(jasperPrint, outputStream);
                    break;

                case CSV:
                    LOG.debug("rendering as CSV");
                    exportReportToCsv(jasperPrint, outputStream);
                    break;

                default:
                    LOG.debug("rendering as PDF as no valid format found");
                    exportReportToPdf(jasperPrint, outputStream);
            }
        } catch (JRException e) {
            LOG.error("unable to render report", e);
            throw new ReportException("unable to render report", e);
        }
    }

    private JasperPrint getJasperPrint(String location) throws JRException {
        if (location.contains("jrpxml")) {
            return JRPrintXmlLoader.load(location);
        } else {
            return (JasperPrint) JRLoader.loadObject(location);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String run(HashMap<String, Object> reportParms, String reportId)
            throws ReportException {
        String baseDir = System.getProperty("opennms.report.dir");
        JasperReport jasperReport = null;
        String outputFileName = null;

        // TODO TAK: What is about jrReportParms?
        HashMap<String, Object> jrReportParms;

        try {
            jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
        } catch (JRException e) {
            LOG.error("unable to compile jasper report", e);
            throw new ReportException("unable to compile jasperReport", e);
        }

        jrReportParms = buildJRparameters(reportParms,
                jasperReport.getParameters());

        // Find sub reports and provide sub reports as parameter
        jrReportParms.putAll(buildSubreport(reportId, jasperReport));

        outputFileName = new String(baseDir + "/" + jasperReport.getName()
                + new SimpleDateFormat("-MMddyyyy-HHmm").format(new Date())
                + ".jrprint");
        LOG.debug("jrprint output file: {}", outputFileName);

        if ("jdbc".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
            Connection connection;
            try {
                connection = DataSourceFactory.getDataSource().getConnection();
                JasperFillManager.fillReportToFile(jasperReport,
                        outputFileName,
                        reportParms, connection);

                connection.close();
            } catch (SQLException e) {
                LOG.error("sql exception getting or closing datasource ", e);
                throw new ReportException(
                        "sql exception getting or closing datasource",
                        e);
            } catch (JRException e) {
                LOG.error("jasper report exception ", e);
                throw new ReportException(
                        "unable to run emptyDataSource jasperReport",
                        e);
            }
        } else if (m_globalReportRepository.getEngine(reportId).equals("null")) {

            try {

                JasperFillManager.fillReportToFile(jasperReport,
                        outputFileName,
                        reportParms,
                        new JREmptyDataSource());
            } catch (JRException e) {
                LOG.error("jasper report exception ", e);
                throw new ReportException(
                        "unable to run emptyDataSource jasperReport",
                        e);
            }

        } else {
            throw new ReportException(
                    "no suitable datasource configured for reportId: "
                            + reportId);
        }

        return outputFileName;
    }


    /**
     * Method to find all sub reports as parameter. Compile sub reports and put all compile sub reports in a parameter map.
     * Returned map is compatible to common jasper report parameter map.
     *
     * @param mainReportId String for specific main report identified by a report id
     * @param mainReport   JasperReport a compiled main report
     * @return a sub report parameter map as {@link java.util.HashMap<String,Object>} object
     */
    private HashMap<String, Object> buildSubreport(String mainReportId, JasperReport mainReport) {
        String repositoryId = mainReportId.substring(0, mainReportId.indexOf("_"));
        HashMap<String, Object> subreportMap = new HashMap<String, Object>();

        // Filter parameter for sub reports
        for (JRParameter parameter : mainReport.getParameters()) {
            // We need only parameter for Sub reports and we *DON'T* need the default parameter JASPER_REPORT
            if ("net.sf.jasperreports.engine.JasperReport".equals(parameter.getValueClassName()) && !"JASPER_REPORT".equals(parameter.getName())) {
                subreportMap.put(parameter.getName(), parameter.getValueClassName());
            }
        }

        for (Map.Entry<String,Object> entry : subreportMap.entrySet()) {
            try {
                entry.setValue(JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(repositoryId + "_" + entry.getKey())));
            } catch (JRException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        for (Map.Entry<String,Object> entry : subreportMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " - " + "Value: " + entry.getValue());
        }
        return subreportMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runAndRender(HashMap<String, Object> reportParms,
                             String reportId, ReportFormat format, OutputStream outputStream)
            throws ReportException {

        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        HashMap<String, Object> jrReportParms;

        try {
            jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
        } catch (JRException e) {
            LOG.error("unable to compile jasper report", e);
            throw new ReportException("unable to compile jasperReport", e);
        }

        jrReportParms = buildJRparameters(reportParms,
                jasperReport.getParameters());
        jrReportParms.putAll(buildSubreport(reportId, jasperReport));

        if ("jdbc".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
            Connection connection;
            try {
                connection = DataSourceFactory.getDataSource().getConnection();
                jasperPrint = JasperFillManager.fillReport(jasperReport,
                        jrReportParms,
                        connection);
                exportReport(format, jasperPrint, outputStream);
                connection.close();
            } catch (SQLException e) {
                LOG.error("sql exception getting or closing datasource ", e);
                throw new ReportException(
                        "sql exception getting or closing datasource",
                        e);
            } catch (JRException e) {
                LOG.error("jasper report exception ", e);
                throw new ReportException(
                        "unable to run or render jdbc jasperReport",
                        e);
            }
        } else if ("null".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
            try {
                jasperPrint = JasperFillManager.fillReport(jasperReport,
                        jrReportParms,
                        new JREmptyDataSource());
                exportReport(format, jasperPrint, outputStream);
            } catch (JRException e) {
                LOG.error("jasper report exception ", e);
                throw new ReportException(
                        "unable to run or render emptyDataSource jasperReport",
                        e);
            }

        }

    }

    private void exportReport(ReportFormat format, JasperPrint jasperPrint,
                              OutputStream outputStream) throws JRException {
        switch (format) {
            case PDF:
                exportReportToPdf(jasperPrint, outputStream);
                break;

            case CSV:
                exportReportToCsv(jasperPrint, outputStream);
                break;

            default:
                break;
        }

    }

    private void exportReportToPdf(JasperPrint jasperPrint,
                                   OutputStream outputStream) throws JRException {
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
    }

    private void exportReportToCsv(JasperPrint jasperPrint,
                                   OutputStream outputStream) throws JRException {
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

        exporter.exportReport();
    }

    private HashMap<String, Object> buildJRparameters(
            HashMap<String, Object> onmsReportParms, JRParameter[] reportParms)
            throws ReportException {

        HashMap<String, Object> jrReportParms = new HashMap<String, Object>();

        for (JRParameter reportParm : reportParms) {
            LOG.debug("found report parm {} of class {}", reportParm.getValueClassName(), reportParm.getName());
            if (reportParm.isSystemDefined() == false) {

                String parmName = reportParm.getName();

                if (reportParm.isForPrompting() == false) {
                    LOG.debug("Required parameter {} is not for prompting - continuing", parmName);
                    continue;
                }

                if (onmsReportParms.containsKey(parmName) == false)
                    throw new ReportException("Required parameter "
                            + parmName
                            + " not supplied to JasperReports by OpenNMS");

                if (reportParm.getValueClassName().equals("java.lang.String")) {
                    jrReportParms.put(parmName,
                            new String(
                                    (String) onmsReportParms.get(parmName)));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                    jrReportParms.put(parmName,
                            new Integer(
                                    (Integer) onmsReportParms.get(parmName)));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Float")) {
                    jrReportParms.put(parmName,
                            new Float(
                                    (Float) onmsReportParms.get(parmName)));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Double")) {
                    jrReportParms.put(parmName,
                            new Double(
                                    (Double) onmsReportParms.get(parmName)));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(HashMap<String, Object> reportParms,
                            String reportId) {
        // returns true until we can take parameters
        return true;
    }

    public void setGlobalReportRepository(GlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}
