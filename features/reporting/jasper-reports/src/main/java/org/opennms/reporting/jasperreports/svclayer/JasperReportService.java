/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.jasperreports.svclayer;

import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.fill.JRParameterDefaultValuesEvaluator;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;

import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.DBUtils;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String LOG4J_CATEGORY = "reports";

    private static final String STRING_INPUT_TYPE = "org.opennms.report.stringInputType";

    private GlobalReportRepository m_globalReportRepository;

    /**
     * <p>
     * Constructor for JasperReportService.
     * </p>
     */
    public JasperReportService() {
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
    public ReportParameters getParameters(final String reportId) throws ReportException {
        try {
            return Logging.withPrefix(LOG4J_CATEGORY, new Callable<ReportParameters>() {
                @Override public ReportParameters call() throws Exception {
                    final ReportParameters reportParameters = new ReportParameters();

                    JasperReport jasperReport = null;
                    Map<String, Object> defaultValues = null;

                    try {
                        jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
                        defaultValues = JRParameterDefaultValuesEvaluator.evaluateParameterDefaultValues(jasperReport, new HashMap<String, Object>());
                    } catch (final JRException e) {
                        LOG.error("unable to compile jasper report", e);
                        throw new ReportException("unable to compile jasperReport", e);
                    }

                    final JRParameter[] reportParms = jasperReport.getParameters();

                    final List<ReportIntParm> intParms = new ArrayList<ReportIntParm>();
                    reportParameters.setIntParms(intParms);

                    final List<ReportFloatParm> floatParms = new ArrayList<ReportFloatParm>();
                    reportParameters.setFloatParms(floatParms);

                    final List<ReportDoubleParm> doubleParms = new ArrayList<ReportDoubleParm>();
                    reportParameters.setDoubleParms(doubleParms);

                    final List<ReportStringParm> stringParms = new ArrayList<ReportStringParm>();
                    reportParameters.setStringParms(stringParms);

                    final List<ReportDateParm> dateParms = new ArrayList<ReportDateParm>();
                    reportParameters.setDateParms(dateParms);

                    for (final JRParameter reportParm : reportParms) {

                        if (reportParm.isSystemDefined() == false) {

                            if (reportParm.isForPrompting() == false) {
                                LOG.debug("report parm {} is not for prompting - continuing", reportParm.getName());
                                continue;
                            } else {
                                LOG.debug("found promptable report parm {}", reportParm.getName());

                            }

                            if (reportParm.getValueClassName().equals("java.lang.String")) {
                                LOG.debug("adding a string parm name {}", reportParm.getName());
                                final ReportStringParm stringParm = new ReportStringParm();
                                if (reportParm.getDescription() != null) {
                                    stringParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    stringParm.setDisplayName(reportParm.getName());
                                }
                                if (reportParm.getPropertiesMap().containsProperty(STRING_INPUT_TYPE)) {
                                    stringParm.setInputType(reportParm.getPropertiesMap().getProperty(STRING_INPUT_TYPE));
                                }
                                stringParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    stringParm.setValue((String) defaultValues.get(reportParm.getName()));
                                } else {
                                    stringParm.setValue("");
                                }
                                stringParms.add(stringParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                                LOG.debug("adding a Integer parm name {}", reportParm.getName());
                                final ReportIntParm intParm = new ReportIntParm();
                                if (reportParm.getDescription() != null) {
                                    intParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    intParm.setDisplayName(reportParm.getName());
                                }
                                intParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    intParm.setValue((Integer) defaultValues.get(reportParm.getName()));
                                } else {
                                    intParm.setValue(Integer.valueOf(0));
                                }
                                intParms.add(intParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.lang.Float")) {
                                LOG.debug("adding a Float parm name {}", reportParm.getName());
                                final ReportFloatParm floatParm = new ReportFloatParm();
                                if (reportParm.getDescription() != null) {
                                    floatParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    floatParm.setDisplayName(reportParm.getName());
                                }
                                floatParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    floatParm.setValue((Float) defaultValues.get(reportParm.getName()));
                                } else {
                                    floatParm.setValue(new Float(0));
                                }
                                floatParms.add(floatParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.lang.Double")) {
                                LOG.debug("adding a Double parm name {}", reportParm.getName());
                                final ReportDoubleParm doubleParm = new ReportDoubleParm();
                                if (reportParm.getDescription() != null) {
                                    doubleParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    doubleParm.setDisplayName(reportParm.getName());
                                }
                                doubleParm.setName(reportParm.getName());
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    doubleParm.setValue((Double) defaultValues.get(reportParm.getName()));
                                } else {
                                    doubleParm.setValue(new Double(0));
                                }
                                doubleParms.add(doubleParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.util.Date")) {
                                LOG.debug("adding a java.util.Date parm name {}", reportParm.getName());
                                final ReportDateParm dateParm = new ReportDateParm();
                                dateParm.setUseAbsoluteDate(false);
                                if (reportParm.getDescription() != null) {
                                    dateParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    dateParm.setDisplayName(reportParm.getName());
                                }
                                dateParm.setName(reportParm.getName());
                                dateParm.setCount(Integer.valueOf(1));
                                dateParm.setInterval("day");
                                dateParm.setHours(0);
                                dateParm.setMinutes(0);
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    dateParm.setDate((Date) defaultValues.get(reportParm.getName()));
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(dateParm.getDate());
                                    dateParm.setMinutes(cal.get(Calendar.MINUTE));
                                    dateParm.setHours(cal.get(Calendar.HOUR_OF_DAY));
                                } else {
                                    final Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);
                                    dateParm.setDate(cal.getTime());
                                }
                                dateParms.add(dateParm);
                                continue;
                            }

                            if (reportParm.getValueClassName().equals("java.sql.Date") || reportParm.getValueClassName().equals("java.sql.Timestamp")) {
                                LOG.debug("adding a java.sql.Date or Timestamp parm name {}", reportParm.getName());
                                final ReportDateParm dateParm = new ReportDateParm();
                                dateParm.setUseAbsoluteDate(false);
                                if (reportParm.getDescription() != null) {
                                    dateParm.setDisplayName(reportParm.getDescription());
                                } else {
                                    dateParm.setDisplayName(reportParm.getName());
                                }
                                dateParm.setName(reportParm.getName());
                                dateParm.setCount(Integer.valueOf(1));
                                dateParm.setInterval("day");
                                dateParm.setHours(0);
                                dateParm.setMinutes(0);
                                if (defaultValues.containsKey(reportParm.getName()) && (defaultValues.get(reportParm.getName()) != null)) {
                                    dateParm.setDate((Date) defaultValues.get(reportParm.getName()));
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(dateParm.getDate());
                                    dateParm.setMinutes(cal.get(Calendar.MINUTE));
                                    dateParm.setHours(cal.get(Calendar.HOUR_OF_DAY));
                                } else {
                                    final Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);
                                    dateParm.setDate(cal.getTime());
                                }
                                dateParms.add(dateParm);
                                continue;
                            }
                            throw new ReportException("Unsupported report parameter type " + reportParm.getValueClassName());
                        }
                    }
                    return reportParameters;                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final String reportId, final String location, final ReportFormat format, final OutputStream outputStream) throws ReportException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    try {
                        final JasperPrint jasperPrint = getJasperPrint(location);

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
                    } catch (final Exception e) {
                        LOG.error("Unable to render report {}", reportId, e);
                        throw new ReportException("Unable to render report " + reportId, e);
                    }

                    return null;
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException(e);
        }
    }

    private JasperPrint getJasperPrint(String location) throws JRException {
        if (location.contains("jrpxml")) {
            return JRPrintXmlLoader.load(location);
        } else {
            return (JasperPrint) JRLoader.loadObject(new File(location));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String run(final Map<String, Object> reportParms, final String reportId) throws ReportException {
        try {
            return Logging.withPrefix(LOG4J_CATEGORY, new Callable<String>() {
                @Override public String call() throws Exception {
                    final String baseDir = System.getProperty("opennms.report.dir");
                    JasperReport jasperReport = null;

                    final DBUtils db = new DBUtils();

                    try {
                        jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
                    } catch (JRException e) {
                        LOG.error("Unable to compile jasper report {}", reportId, e);
                        throw new ReportException("Unable to compile jasperReport " + reportId, e);
                    }

                    final Map<String, Object> jrReportParms = buildJRparameters(reportParms, jasperReport.getParameters());

                    // Find sub reports and provide sub reports as parameter
                    jrReportParms.putAll(buildSubreport(reportId, jasperReport));

                    final String outputFileName = new String(baseDir + "/" + jasperReport.getName() + new SimpleDateFormat("-MMddyyyy-HHmm").format(new Date()) + ".jrprint");
                    LOG.debug("jrprint output file: {}", outputFileName);

                    try {
                        if ("jdbc".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
                            try {
                                final Connection connection = DataSourceFactory.getInstance().getConnection();
                                db.watch(connection);
                                JasperFillManager.fillReportToFile(jasperReport, outputFileName, reportParms, connection);
                            } finally {
                                db.cleanUp();
                            }
                        } else if (m_globalReportRepository.getEngine(reportId).equals("null")) {
                            JasperFillManager.fillReportToFile(jasperReport, outputFileName, reportParms, new JREmptyDataSource());
                        } else {
                            throw new ReportException("No suitable datasource configured for report " + reportId);
                        }
                    } catch (final Exception e) {
                        LOG.warn("Failed to run report " + reportId, e);
                        if (e instanceof ReportException) throw (ReportException)e;
                        throw new ReportException(e);
                    }
                    return outputFileName;
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException("Failed to run Jasper report " + reportId, e);
        }

    }


    /**
     * Method to find all sub reports as parameter. Compile sub reports and put all compile sub reports in a parameter map.
     * Returned map is compatible to common jasper report parameter map.
     *
     * @param mainReportId String for specific main report identified by a report id
     * @param mainReport   JasperReport a compiled main report
     * @return a sub report parameter map as {@link java.util.HashMap<String,Object>} object
     */
    private Map<String, Object> buildSubreport(final String mainReportId, final JasperReport mainReport) {
        int idx = mainReportId.indexOf('_');
        String repositoryId = idx > -1 ? mainReportId.substring(0, idx) : "local";
        Map<String, Object> subreportMap = new HashMap<String, Object>();

        // Filter parameter for sub reports
        for (JRParameter parameter : mainReport.getParameters()) {
            // We need only parameter for Sub reports and we *DON'T* need the default parameter JASPER_REPORT
            if ("net.sf.jasperreports.engine.JasperReport".equals(parameter.getValueClassName()) && !"JASPER_REPORT".equals(parameter.getName())) {
                subreportMap.put(parameter.getName(), parameter.getValueClassName());
            }
        }

        for (final Map.Entry<String,Object> entry : subreportMap.entrySet()) {
            final String reportId = repositoryId + "_" + entry.getKey();
            try {
                entry.setValue(JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId)));
            } catch (final JRException e) {
                LOG.debug("failed to compile report {}", reportId, e);
            }
        }

        for (final Map.Entry<String,Object> entry : subreportMap.entrySet()) {
            LOG.debug("Key: {} - Value: {}", entry.getKey(), entry.getValue());
        }
        return subreportMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runAndRender(final Map<String, Object> reportParms, final String reportId, final ReportFormat format, final OutputStream outputStream) throws ReportException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    JasperReport jasperReport = null;

                    try {
                        jasperReport = JasperCompileManager.compileReport(m_globalReportRepository.getTemplateStream(reportId));
                    } catch (final JRException e) {
                        LOG.error("unable to compile jasper report", e);
                        throw new ReportException("unable to compile jasperReport", e);
                    }

                    final Map<String, Object> jrReportParms = buildJRparameters(reportParms, jasperReport.getParameters());
                    jrReportParms.putAll(buildSubreport(reportId, jasperReport));

                    if ("jdbc".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
                        final DBUtils db = new DBUtils();
                        try {
                            final Connection connection = DataSourceFactory.getInstance().getConnection();
                            db.watch(connection);

                            final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jrReportParms, connection);
                            exportReport(format, jasperPrint, outputStream);
                        } finally {
                            db.cleanUp();
                        }
                    } else if ("null".equalsIgnoreCase(m_globalReportRepository.getEngine(reportId))) {
                        final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jrReportParms, new JREmptyDataSource());
                        exportReport(format, jasperPrint, outputStream);

                    }

                    return null;
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportException) throw (ReportException)e;
            throw new ReportException("Failed to run Jasper report " + reportId, e);
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

    private void exportReportToPdf(final JasperPrint jasperPrint, final OutputStream outputStream) throws JRException {
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
    }

    private void exportReportToCsv(final JasperPrint jasperPrint, final OutputStream outputStream) throws JRException {
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

        exporter.exportReport();
    }

    private Map<String, Object> buildJRparameters(final Map<String, Object> onmsReportParms, final JRParameter[] reportParms) throws ReportException {
        final Map<String, Object> jrReportParms = new HashMap<String, Object>();

        for (final JRParameter reportParm : reportParms) {
            LOG.debug("found report parm {} of class {}", reportParm.getValueClassName(), reportParm.getName());
            if (reportParm.isSystemDefined() == false) {
                final String parmName = reportParm.getName();

                if (reportParm.isForPrompting() == false) {
                    LOG.debug("Required parameter {} is not for prompting - continuing", parmName);
                    continue;
                }

                if (onmsReportParms.containsKey(parmName) == false) {
                    throw new ReportException("Required parameter " + parmName + " not supplied to JasperReports by OpenNMS");
                }

                if (reportParm.getValueClassName().equals("java.lang.String")) {
                    jrReportParms.put(parmName, (String)onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Integer")) {
                    jrReportParms.put(parmName, (Integer) onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Float")) {
                    jrReportParms.put(parmName, (Float) onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.lang.Double")) {
                    jrReportParms.put(parmName, (Double) onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.util.Date")) {
                    jrReportParms.put(parmName, (Date)onmsReportParms.get(parmName));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Date")) {
                    final Date date = (Date)onmsReportParms.get(parmName);
                    jrReportParms.put(parmName, new java.sql.Date(date.getTime()));
                    continue;
                }

                if (reportParm.getValueClassName().equals("java.sql.Timestamp")) {
                    final Date date = (Date)onmsReportParms.get(parmName);
                    jrReportParms.put(parmName, new java.sql.Timestamp(date.getTime()));
                    continue;
                }

                throw new ReportException("Unsupported report parameter type " + reportParm.getValueClassName());
            }
        }

        return jrReportParms;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Map<String, Object> reportParms, final String reportId) {
        // returns true until we can take parameters
        return true;
    }

    public void setGlobalReportRepository(final GlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}
