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
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.ValidationException;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.ReportService;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.logging.Logging;
import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    /** {@inheritDoc} */
    @Override
    public DeliveryOptions getDeliveryOptions(final String reportId, final String userId) {
        final DeliveryOptions options = new DeliveryOptions();

        options.setFormat(ReportFormat.HTML);
        options.setPersist(true);
        options.setSendMail(false);

        Logging.withPrefix(LOG4J_CATEGORY, new Runnable() {
            @Override public void run() {
                UserManager userFactory = UserFactory.getInstance();

                try {
                    final String emailAddress = userFactory.getEmail(userId);
                    if (emailAddress != null && !emailAddress.isEmpty()) {
                        options.setMailTo(emailAddress);
                    }
                } catch (final ValidationException e) {
                    LOG.error("validation exception trying to set destination email address", e);
                } catch (final NullPointerException e) { // See NMS-5111 for more details.
                    LOG.warn("the user {} does not have any email configured.", userId);
                } catch (final Exception e) {
                    LOG.error("An error occurred while attempting to determine and set the destination email address for user {}", userId, e);
                }

                options.setInstanceId(reportId + " " + userId);

            }
        });
        return options;
    }

    /** {@inheritDoc} */
    @Override
    public List<ReportFormat> getFormats(String reportId) {
        return getReportService(reportId).getFormats(reportId);
    }

    /** {@inheritDoc} */
    @Override
    public ReportParameters getParameters(final String reportId) {
        try {
            return Logging.withPrefix(LOG4J_CATEGORY, new Callable<ReportParameters>() {
                @Override public ReportParameters call() throws Exception {
                    try {
                        return getReportService(reportId).getParameters(reportId);
                    } catch (final ReportException e) {
                        LOG.error("Report Exception when retrieving report parameters", e);
                    }
                    return null;
                }
            });
        } catch (final Exception e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Boolean hasParameters(final String reportId) {
        final Map<String, Object> reportParms = getParameters(reportId).getReportParms();
        if ((reportParms == null)||(reportParms.isEmpty())) {
            return false;
        } else {
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(final String reportId, final String location, final ReportFormat format, final OutputStream outputStream) {
        Logging.withPrefix(LOG4J_CATEGORY, new Runnable() {
            @Override public void run() {
                try {
                    getReportService(reportId).render(reportId, location, format, outputStream);
                } catch (final ReportException e) {
                    LOG.error("failed to render report", e);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void run(final ReportParameters parameters, final ReportMode mode, final DeliveryOptions deliveryOptions, final String reportId) {
        Logging.withPrefix(LOG4J_CATEGORY, new Runnable() {
            @Override public void run() {
                ByteArrayOutputStream out = null;
                BufferedOutputStream bout = null;

                try {
                    out = new ByteArrayOutputStream();
                    bout = new BufferedOutputStream(out);
                    if (!deliveryOptions.getPersist()) {
                        try {
                            getReportService(reportId).runAndRender(parameters.getReportParms(mode), reportId, deliveryOptions.getFormat(), bout);
                        } catch (final ReportException reportException) {
                            LOG.error("failed to run or render report: {}", reportId, reportException);
                        }
                        mailReport(deliveryOptions, out);
                    } else {
                        final String outputPath = getReportService(reportId).run(parameters.getReportParms(mode), reportId);
                        final ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
                        catalogEntry.setReportId(reportId);
                        catalogEntry.setTitle(deliveryOptions.getInstanceId());
                        catalogEntry.setLocation(outputPath);
                        catalogEntry.setDate(new Date());
                        m_reportStoreService.save(catalogEntry);
                        if (deliveryOptions.getMailTo().length() != 0) {
                            getReportService(reportId).render(reportId, outputPath, deliveryOptions.getFormat(), bout);
                            mailReport(deliveryOptions, out);
                        }
                    }
                } catch (final Exception e) {
                    LOG.error("failed to run or render report: {}", reportId, e);
                } finally {
                    IOUtils.closeQuietly(bout);
                    IOUtils.closeQuietly(out);
                }
            }
        });
    }

    private void mailReport(final DeliveryOptions deliveryOptions, final ByteArrayOutputStream outputStream) {
        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            final JavaMailer jm = new JavaMailer();
            jm.setTo(deliveryOptions.getMailTo());
            jm.setSubject(deliveryOptions.getInstanceId());
            jm.setMessageText("Here is your report from the OpenNMS report service.");
            jm.setInputStream(inputStream);

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
        } catch (final JavaMailerException e) {
            LOG.error("Caught JavaMailer exception sending report", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(final ReportParameters parameters, final String reportId) {
        return getReportService(reportId).validate(parameters.getReportParms(), reportId);
    }

    private ReportService getReportService(final String reportId) {
        return m_reportServiceLocator.getReportServiceForId(reportId);
    }

    /**
     * <p>setReportServiceLocator</p>
     *
     * @param reportServiceLocator a {@link org.opennms.reporting.core.svclayer.ReportServiceLocator} object.
     */
    public void setReportServiceLocator(final ReportServiceLocator reportServiceLocator) {
        m_reportServiceLocator = reportServiceLocator;
    }

    /**
     * <p>setReportStoreService</p>
     *
     * @param reportStoreService a {@link org.opennms.reporting.core.svclayer.ReportStoreService} object.
     */
    public void setReportStoreService(final ReportStoreService reportStoreService) {
        m_reportStoreService = reportStoreService;
    }

    /** {@inheritDoc} */

    @Override
    public void runAndRender(final ReportParameters parameters, final ReportMode mode, final OutputStream outputStream) {
        Logging.withPrefix(LOG4J_CATEGORY, new Runnable() {
            @Override public void run() {
                if (LOG.isDebugEnabled()) {
                    // TODO remove this debug code
                    final Map<String, Object> reportParms = parameters.getReportParms(mode);
                    for (final String key : reportParms.keySet()) {
                        final Object value = reportParms.get(key);
                        LOG.debug("param {} set {}", key, value == null? "NULL" : value);
                    }
                }

                try {
                    getReportService(parameters.getReportId()).runAndRender(parameters.getReportParms(mode), parameters.getReportId(), parameters.getFormat(), outputStream);
                } catch (final ReportException e) {
                    LOG.error("failed to run or render report: ", parameters.getReportId(), e);
                }

            }
        });
    }

}
