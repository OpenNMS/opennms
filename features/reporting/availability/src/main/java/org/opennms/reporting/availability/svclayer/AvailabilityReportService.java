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

package org.opennms.reporting.availability.svclayer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.dao.api.OnmsReportConfigDao;
import org.opennms.reporting.availability.AvailabilityCalculationException;
import org.opennms.reporting.availability.AvailabilityCalculator;
import org.opennms.reporting.availability.render.HTMLReportRenderer;
import org.opennms.reporting.availability.render.PDFReportRenderer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;
import org.opennms.reporting.core.svclayer.ParameterConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * <p>AvailabilityReportService class.</p>
 */
public class AvailabilityReportService implements ReportService {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityReportService.class);

    private AvailabilityCalculator m_classicCalculator;

    private AvailabilityCalculator m_calendarCalculator;

    private OnmsReportConfigDao m_configDao;

    private ParameterConversionService m_parameterConversionService;

    private static final String LOG4J_CATEGORY = "reports";

    private static final String CAL_TYPE = "calendar";


    /**
     * <p>Constructor for AvailabilityReportService.</p>
     */
    public AvailabilityReportService() {
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(HashMap<String, Object> reportParms,
            String reportID) {

        if (!reportParms.containsKey("endDate")) {
            LOG.error("report parameters should contain parameter endDate");
            return false;
        }

        if (!(reportParms.get("endDate") instanceof Date)) {
            LOG.error("report parameters 'endDate' should be a Date");
            return false;
        }

        if (!reportParms.containsKey("reportCategory")) {
            LOG.error("report parameters should contain parameter reportCategory");
            return false;
        }

        if (!(reportParms.get("reportCategory") instanceof String)) {
            LOG.error("report parameter 'reportCategory' should be a String");
            return false;
        }

        return true;

    }


    /** {@inheritDoc} */
    @Override
    public void render(String id, String location, ReportFormat format,
            OutputStream outputStream) {
        
        FileInputStream inputStream = null;
        
            try {
                inputStream = new FileInputStream(location);
                render(id, inputStream, format, outputStream);
            } catch (FileNotFoundException e) {
                LOG.error("could not open input file", e);
            }
    }
    
    private void render(String id, InputStream inputStream, ReportFormat format,
            OutputStream outputStream) {

        Resource xsltResource;
        ReportRenderer renderer;

        try {

            switch (format) {

            case HTML:
                LOG.debug("rendering as HTML");
                renderer = new HTMLReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getHtmlStylesheetLocation(id));
                break;
            case PDF:
                LOG.debug("rendering as PDF");
                renderer = new PDFReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getPdfStylesheetLocation(id));
                break;
            case SVG:
                LOG.debug("rendering as PDF with embedded SVG");
                renderer = new PDFReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getSvgStylesheetLocation(id));
                break;
            default:
                LOG.debug("rendering as HTML as no valid format found");
                renderer = new HTMLReportRenderer();
                xsltResource = new UrlResource(
                                               m_configDao.getHtmlStylesheetLocation(id));
            }

            String baseDir = System.getProperty("opennms.report.dir");
            renderer.setBaseDir(baseDir);
            renderer.render(inputStream, outputStream, xsltResource);
            outputStream.flush();

        } catch (MalformedURLException e) {
            LOG.error("Malformed URL for xslt template");
        } catch (ReportRenderException e) {
            LOG.error("unable to render report");
        } catch (IOException e) {
            LOG.error("IO exception flushing output stream ", e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public List<ReportFormat> getFormats(String id) {

        List<ReportFormat> formats = new ArrayList<ReportFormat>();

        if (m_configDao.getHtmlStylesheetLocation(id) != null)
            formats.add(ReportFormat.HTML);
        if (m_configDao.getPdfStylesheetLocation(id) != null)
            formats.add(ReportFormat.PDF);
        if (m_configDao.getSvgStylesheetLocation(id) != null)
            formats.add(ReportFormat.SVG);

        return formats;
    }

    // this new version needs the report wrapper to persist the entry
    
    /** {@inheritDoc} */
    @Override
    public String run(HashMap<String, Object> reportParms,
            String reportId) {
        
        AvailabilityCalculator calculator;
        String reportFileName = null;

        LOG.debug("running OpenNMS database report {}", reportId);

        if (m_configDao.getType(reportId).equalsIgnoreCase(CAL_TYPE)) {
            calculator = m_calendarCalculator;
            LOG.debug("Calendar report format selected");
        } else {
            calculator = m_classicCalculator;
            LOG.debug("Classic report format selected");
        }

        calculator.setCategoryName((String) reportParms.get("reportCategory"));
        
        LOG.debug("set availability calculator report category to: {}", calculator.getCategoryName());

        calculator.setCalendar(new GregorianCalendar());
        calculator.setPeriodEndDate((Date) reportParms.get("endDate"));
        
        LOG.debug("set availability calculator end date to: {}", calculator.getPeriodEndDate());

        calculator.setLogoURL(m_configDao.getLogo(reportId));

        // have the calculator calculate everything to enable any of the
        // templates to work
        // This has changed since the last version
        // This will have some performance impact.

        calculator.setReportFormat("all");

        LOG.debug("Starting Availability Report Calculations");
        try {
            calculator.calculate();
            reportFileName = calculator.writeXML();
        } catch (AvailabilityCalculationException ce) {
            LOG.error("Unable to calculate report data ", ce);
        }

        return reportFileName;

    }
    
    /** {@inheritDoc} */
    @Override
    public void runAndRender(HashMap<String, Object> reportParms,
            String reportId, ReportFormat format, OutputStream outputStream) {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedOutputStream bout = new BufferedOutputStream(out);
        
        AvailabilityCalculator calculator;

        LOG.debug("running OpenNMS database report {}", reportId);

        if (m_configDao.getType(reportId).equalsIgnoreCase(CAL_TYPE)) {
            calculator = m_calendarCalculator;
            LOG.debug("Calendar report format selected");
        } else {
            calculator = m_classicCalculator;
            LOG.debug("Classic report format selected");
        }

        calculator.setCategoryName((String) reportParms.get("reportCategory"));
        
        LOG.debug("set availability calculator report category to: {}", calculator.getCategoryName());


        calculator.setCalendar(new GregorianCalendar());
        calculator.setPeriodEndDate((Date) reportParms.get("endDate"));
        
        LOG.debug("set availability calculator end date to: {}", calculator.getPeriodEndDate());

        calculator.setLogoURL(m_configDao.getLogo(reportId));

        // have the calculator calculate everything to enable any of the
        // templates to work
        // This has changed since the last version
        // This will have some performance impact.

        calculator.setReportFormat("all");

        LOG.debug("Starting Availability Report Calculations");
        try {
            calculator.calculate();
            calculator.writeXML(bout);
            render(reportId,
                   new ByteArrayInputStream(out.toByteArray()),
                   format,
                   outputStream);
            outputStream.flush();
        } catch (AvailabilityCalculationException ce) {
            LOG.error("Unable to calculate report data ", ce);
        } catch (IOException e) {
            LOG.error("IO exception flushing output stream ", e);
        } 
        
    }

    
    /** {@inheritDoc} */
    @Override
    public ReportParameters getParameters(String ReportId) {
        return m_parameterConversionService.convert(m_configDao.getParameters(ReportId));
    }
    
    /**
     * <p>setCalendarCalculator</p>
     *
     * @param calculator a {@link org.opennms.reporting.availability.AvailabilityCalculator} object.
     */
    public void setCalendarCalculator(AvailabilityCalculator calculator) {
        m_calendarCalculator = calculator;
    }

    /**
     * <p>setClassicCalculator</p>
     *
     * @param calulator a {@link org.opennms.reporting.availability.AvailabilityCalculator} object.
     */
    public void setClassicCalculator(AvailabilityCalculator calulator) {
        m_classicCalculator = calulator;
    }

    /**
     * <p>setConfigDao</p>
     *
     * @param configDao a {@link org.opennms.netmgt.dao.api.OnmsReportConfigDao} object.
     */
    public void setConfigDao(OnmsReportConfigDao configDao) {
        m_configDao = configDao;
    }
    
    /**
     * <p>setParameterConversionService</p>
     *
     * @param parameterConversionService a {@link org.opennms.reporting.core.svclayer.ParameterConversionService} object.
     */
    public void setParameterConversionService(ParameterConversionService parameterConversionService) {
        m_parameterConversionService = parameterConversionService;
    }



}
