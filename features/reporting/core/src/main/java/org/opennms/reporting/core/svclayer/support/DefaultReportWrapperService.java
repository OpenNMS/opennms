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
 * Created: February 3rd 2010 Jonathan Sartin <jonathan@opennms.org>
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
package org.opennms.reporting.core.svclayer.support;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.reporting.core.svclayer.ReportWrapperService;

public class DefaultReportWrapperService implements ReportWrapperService {

    private ReportServiceLocator m_reportServiceLocator;

    private ThreadCategory log;

    private ReportStoreService m_reportStoreService;

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    public DefaultReportWrapperService() {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(DefaultReportWrapperService.class);

    }

    public DeliveryOptions getDeliveryOptions(String reportId, String userId) {
        DeliveryOptions options = new DeliveryOptions();

        options.setFormat(ReportFormat.HTML);
        options.setPersist(true);
        options.setSendMail(false);

        UserManager userFactory = UserFactory.getInstance();

        try {
            String emailAddress = userFactory.getEmail(userId);
            if (emailAddress != null) {
                options.setMailTo(userFactory.getEmail(userId));
            }
        } catch (MarshalException e) {
            log.error(
                      "marshal exception trying to set destination email address",
                      e);
        } catch (ValidationException e) {
            log.error(
                      "validation exception trying to set destination email address",
                      e);
        } catch (IOException e) {
            log.error("IO exception trying to set destination email address",
                      e);
        }

        options.setInstanceId(reportId + "_" + userId);

        return options;
    }

    public List<ReportFormat> getFormats(String reportId) {
        return getReportService(reportId).getFormats(reportId);
    }

    public ReportParameters getParameters(String reportId) {
        return getReportService(reportId).getParameters(reportId);
    }
    
    public Boolean hasParameters(String reportId) {
       
        Map<String, Object> reportParms = getParameters(reportId).getReportParms();
        if ((reportParms == null)||(reportParms.isEmpty())) {
            return false;
        } else {
            return true;
        }
    }

    public void render(String reportId, String location, ReportFormat format,
            OutputStream outputStream) {
        try {
            getReportService(reportId).render(reportId, location, format,
                                              outputStream);
        } catch (ReportException e) {
            log.error("failed to render report", e);
        }

    }

    public void run(ReportParameters parameters,
            DeliveryOptions deliveryOptions, String reportId) {
        if (!deliveryOptions.getPersist()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedOutputStream bout = new BufferedOutputStream(out);
            try {
                getReportService(reportId).runAndRender(
                                                        parameters.getReportParms(),
                                                        reportId,
                                                        deliveryOptions.getFormat(),
                                                        bout);
            } catch (ReportException reportException) {
                log.error("failed to run or render report: " + reportId, reportException);
            }
            mailReport(deliveryOptions, out);
        } else {
            String outputPath;
            try {
                outputPath = getReportService(reportId).run(
                                                                   parameters.getReportParms(),
                                                                   reportId);
                ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
                catalogEntry.setReportId(reportId);
                catalogEntry.setTitle(deliveryOptions.getInstanceId());
                catalogEntry.setLocation(outputPath);
                catalogEntry.setDate(new Date());
                m_reportStoreService.save(catalogEntry);
                if (deliveryOptions.getMailTo().length() != 0) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    BufferedOutputStream bout = new BufferedOutputStream(out);
                    getReportService(reportId).render(
                                                      reportId,
                                                      outputPath,
                                                      deliveryOptions.getFormat(),
                                                      bout);
                    mailReport(deliveryOptions, out);
                }
            } catch (ReportException reportException) {
                log.error("failed to run or render report: " + reportId, reportException);
            }
        }

    }

    private void mailReport(DeliveryOptions deliveryOptions,
            ByteArrayOutputStream outputStream) {
        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(deliveryOptions.getMailTo());
            jm.setSubject("OpenNMS Report");
            jm.setMessageText("Here is your report from the OpenNMS report service.");
            jm.setInputStream(new ByteArrayInputStream(
                                                       outputStream.toByteArray()));
            switch (deliveryOptions.getFormat()) {

            case HTML:
                jm.setInputStreamName("report.htm");
                jm.setInputStreamContentType("text/html");
                break;
            case PDF:
                jm.setInputStreamName("report.pdf");
                jm.setInputStreamContentType("application/pdf");
                break;
            case SVG:
                jm.setInputStreamName("svgreport.pdf");
                jm.setInputStreamContentType("application/pdf");
                break;
            default:
                jm.setInputStreamName("report.htm");
                jm.setInputStreamContentType("text/html");

            }
            jm.mailSend();
        } catch (JavaMailerException e) {
            log.error("Caught JavaMailer exception sending report", e);
        }
    }

    public boolean validate(ReportParameters parameters, String reportId) {
        return getReportService(reportId).validate(
                                                   parameters.getReportParms(),
                                                   reportId);
    }

    private ReportService getReportService(String reportId) {
        return m_reportServiceLocator.getReportServiceForId(reportId);
    }

    public void setReportServiceLocator(
            ReportServiceLocator reportServiceLocator) {
        m_reportServiceLocator = reportServiceLocator;
    }

    public void setReportStoreService(ReportStoreService reportStoreService) {
        m_reportStoreService = reportStoreService;
    }

    public void runAndRender(ReportParameters parameters,
            OutputStream outputStream) {

        // TODO remove this debug code
        Map<String, Object> reportParms = parameters.getReportParms();
        for (String key : reportParms.keySet()) {
            String value;
            if (reportParms.get(key) == null) {
                value = "NULL";
            } else {
                value = reportParms.get(key).toString();
            }
            log.debug("param " + key + " set " + value);
        }

        try {
            getReportService(parameters.getReportId()).runAndRender(
                                                                    parameters.getReportParms(),
                                                                    parameters.getReportId(),
                                                                    parameters.getFormat(),
                                                                    outputStream);
        } catch (ReportException reportException) {
            log.error("failed to run or render report: "
                    + parameters.getReportId(), reportException);
        }

    }

}
