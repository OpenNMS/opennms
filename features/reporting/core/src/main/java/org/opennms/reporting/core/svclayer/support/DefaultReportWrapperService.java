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

package org.opennms.reporting.core.svclayer.support;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.reporting.core.svclayer.ReportWrapperService;

/**
 * <p>DefaultReportWrapperService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultReportWrapperService implements ReportWrapperService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultReportWrapperService.class);

    private ReportServiceLocator m_reportServiceLocator;

    private ReportStoreService m_reportStoreService;

    private static final String LOG4J_CATEGORY = "reports";

    /**
     * <p>Constructor for DefaultReportWrapperService.</p>
     */
    public DefaultReportWrapperService() {
        // TODO this should wrap the other methods
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    /** {@inheritDoc} */
    @Override
    public DeliveryOptions getDeliveryOptions(String reportId, String userId) {
        DeliveryOptions options = new DeliveryOptions();

        options.setFormat(ReportFormat.HTML);
        options.setPersist(true);
        options.setSendMail(false);

        UserManager userFactory = UserFactory.getInstance();

        try {
            String emailAddress = userFactory.getEmail(userId);
            if (emailAddress != null) {
                options.setMailTo(emailAddress);
            }
        } catch (MarshalException e) {
            LOG.error("marshal exception trying to set destination email address", e);
        } catch (ValidationException e) {
            LOG.error("validation exception trying to set destination email address", e);
        } catch (IOException e) {
            LOG.error("IO exception trying to set destination email address", e);
        } catch (NullPointerException e) { // See NMS-5111 for more details.
            LOG.warn("the user {} does not have any email configured.", userId);
        }

        options.setInstanceId(reportId + " " + userId);

        return options;
    }

    /** {@inheritDoc} */
    @Override
    public List<ReportFormat> getFormats(String reportId) {
        return getReportService(reportId).getFormats(reportId);
    }

    /** {@inheritDoc} */
    @Override
    public ReportParameters getParameters(String reportId) {
        try {
            return getReportService(reportId).getParameters(reportId);
        } catch (ReportException e) {
            LOG.error("Report Exception when retrieving report parameters", e);
        }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public Boolean hasParameters(String reportId) {
       
        Map<String, Object> reportParms = getParameters(reportId).getReportParms();
        if ((reportParms == null)||(reportParms.isEmpty())) {
            return false;
        } else {
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(String reportId, String location, ReportFormat format,
            OutputStream outputStream) {
        try {
            getReportService(reportId).render(reportId, location, format,
                                              outputStream);
        } catch (ReportException e) {
            LOG.error("failed to render report", e);
        }

    }
    
    /** {@inheritDoc} */
    @Override
    public void run(ReportParameters parameters,
            ReportMode mode,
            DeliveryOptions deliveryOptions,
            String reportId) {
        
        if (!deliveryOptions.getPersist()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedOutputStream bout = new BufferedOutputStream(out);
            try {
                getReportService(reportId).runAndRender(
                                                        parameters.getReportParms(mode),
                                                        reportId,
                                                        deliveryOptions.getFormat(),
                                                        bout);
            } catch (ReportException reportException) {
                LOG.error("failed to run or render report: {}", reportId, reportException);
            }
            mailReport(deliveryOptions, out);
        } else {
            String outputPath;
            try {
                outputPath = getReportService(reportId).run(
                                                                   parameters.getReportParms(mode),
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
                LOG.error("failed to run or render report: {}", reportId, reportException);
            }
        }

    }

    private void mailReport(DeliveryOptions deliveryOptions,
            ByteArrayOutputStream outputStream) {
        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(deliveryOptions.getMailTo());
            jm.setSubject(deliveryOptions.getInstanceId());
            jm.setMessageText("Here is your report from the OpenNMS report service.");
            jm.setInputStream(new ByteArrayInputStream(
                                                       outputStream.toByteArray()));
            switch (deliveryOptions.getFormat()) {

            case HTML:
                jm.setInputStreamName(deliveryOptions.getInstanceId() + ".htm");
                jm.setInputStreamContentType("text/html");
                break;
            case PDF:
                jm.setInputStreamName(deliveryOptions.getInstanceId() + ".pdf");
                jm.setInputStreamContentType("application/pdf");
                break;
            case SVG:
                jm.setInputStreamName(deliveryOptions.getInstanceId()+ ".pdf");
                jm.setInputStreamContentType("application/pdf");
                break;
            case CSV:
                jm.setInputStreamName(deliveryOptions.getInstanceId()+ ".csv");
                jm.setInputStreamContentType("text/csv");
                break;
            default:
                jm.setInputStreamName(deliveryOptions.getInstanceId() + ".htm");
                jm.setInputStreamContentType("text/html");

            }
            jm.mailSend();
        } catch (JavaMailerException e) {
            LOG.error("Caught JavaMailer exception sending report", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(ReportParameters parameters, String reportId) {
        return getReportService(reportId).validate(
                                                   parameters.getReportParms(),
                                                   reportId);
    }

    private ReportService getReportService(String reportId) {
        return m_reportServiceLocator.getReportServiceForId(reportId);
    }

    /**
     * <p>setReportServiceLocator</p>
     *
     * @param reportServiceLocator a {@link org.opennms.reporting.core.svclayer.ReportServiceLocator} object.
     */
    public void setReportServiceLocator(
            ReportServiceLocator reportServiceLocator) {
        m_reportServiceLocator = reportServiceLocator;
    }

    /**
     * <p>setReportStoreService</p>
     *
     * @param reportStoreService a {@link org.opennms.reporting.core.svclayer.ReportStoreService} object.
     */
    public void setReportStoreService(ReportStoreService reportStoreService) {
        m_reportStoreService = reportStoreService;
    }

    /** {@inheritDoc} */
   
    @Override
    public void runAndRender(ReportParameters parameters, ReportMode mode,
            OutputStream outputStream) {

        // TODO remove this debug code
        Map<String, Object> reportParms = parameters.getReportParms(mode);
        for (String key : reportParms.keySet()) {
            String value;
            if (reportParms.get(key) == null) {
                value = "NULL";
            } else {
                value = reportParms.get(key).toString();
            }
            LOG.debug("param {} set {}", value, key);
        }

        try {
            getReportService(parameters.getReportId()).runAndRender(
                                                                    parameters.getReportParms(mode),
                                                                    parameters.getReportId(),
                                                                    parameters.getFormat(),
                                                                    outputStream);
        } catch (ReportException reportException) {
            LOG.error("failed to run or render report: ", parameters.getReportId(), reportException);
        }

    }

}
