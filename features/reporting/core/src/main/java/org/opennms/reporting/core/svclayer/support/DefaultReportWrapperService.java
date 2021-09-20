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

package org.opennms.reporting.core.svclayer.support;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

public class DefaultReportWrapperService implements ReportWrapperService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultReportWrapperService.class);

    private ReportServiceLocator m_reportServiceLocator;

    private ReportStoreService m_reportStoreService;

    private static final String LOG4J_CATEGORY = "reports";

    /** {@inheritDoc} */
    @Override
    public List<ReportFormat> getFormats(String reportId) {
        return getReportService(reportId).getFormats(reportId);
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
                    LOG.error("Failed to render report", e);
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

                    final Map<String, Object> reportParameters = parameters.getReportParms(mode);
                    if (!deliveryOptions.isPersist()) {
                        try {
                            getReportService(reportId).runAndRender(reportParameters, reportId, deliveryOptions.getFormat(), bout);
                            if (deliveryOptions.isSendMail() && deliveryOptions.getMailTo().length() != 0) {
                                mailReport(deliveryOptions, out);
                            }
                            if (deliveryOptions.isWebhook()) {
                                postReport(deliveryOptions, reportParameters, out, deliveryOptions.getInstanceId() + "." + deliveryOptions.getFormat().name().toLowerCase());
                            }
                        } catch (final ReportException reportException) {
                            logError(reportId, reportException);
                        }
                    } else {
                        final String outputPath = getReportService(reportId).run(reportParameters, reportId);
                        final ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
                        catalogEntry.setReportId(reportId);
                        catalogEntry.setTitle(deliveryOptions.getInstanceId());
                        catalogEntry.setLocation(outputPath);
                        catalogEntry.setDate(new Date());
                        m_reportStoreService.save(catalogEntry);

                        if (deliveryOptions.isSendMail() || deliveryOptions.isWebhook()) {
                            getReportService(reportId).render(reportId, outputPath, deliveryOptions.getFormat(), bout);
                            if (deliveryOptions.isSendMail() && deliveryOptions.getMailTo().length() != 0) {
                                mailReport(deliveryOptions, out);
                            }
                            if (deliveryOptions.isWebhook()) {
                                final String fileName = Paths.get(catalogEntry.getLocation()).getFileName().toString().replaceAll("jrprint", "");
                                postReport(deliveryOptions, reportParameters, out, fileName + deliveryOptions.getFormat().name().toLowerCase());
                            }
                        }
                    }
                } catch (final Exception e) {
                    logError(reportId, e);
                } finally {
                    IOUtils.closeQuietly(bout);
                    IOUtils.closeQuietly(out);
                }
            }
        });
    }

    private static void logError(final String reportId, final Exception exception) {
        LOG.error("failed to run or render report: {}", reportId, exception);
    }

    private static void postReport(final DeliveryOptions deliveryOptions, final Map<String, Object> reportParameters, final ByteArrayOutputStream outputStream, final String fileName) {
        final String url = deliveryOptions.getWebhookUrl();
        final String substitutedUrl = substituteUrl(url, deliveryOptions, reportParameters);
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
             final CloseableHttpClient client = HttpClients.createDefault();
        ) {
            // Build Request
            LOG.debug("Posting generated report with name {} to endpoint {} (input was {})", fileName, substitutedUrl, url);
            final HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", inputStream, ContentType.DEFAULT_BINARY, fileName)
                    .build();
            final HttpPost httpPost = new HttpPost(substitutedUrl);
            httpPost.setEntity(entity);

            // Execute request and ensure it succeeded
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                LOG.debug("Request performed. Received response: {}", response);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException("Expected status code of >= 200 and <= 299 but received: " + statusCode + ". Reason: " + response.getStatusLine().getReasonPhrase());
                }
            }
        } catch (IOException ex) {
            LOG.error("Error while posting data to endpoint {}: {}", url, ex.getMessage(), ex);
        }
    }

    protected static String substituteUrl(String url, DeliveryOptions deliveryOptions, Map<String, Object> reportParameters) {
        final Map<String, Object> parameters = new HashMap<>();
        reportParameters.entrySet().forEach(e -> parameters.put("parameter_" + e.getKey(), e.getValue()));
        parameters.put("instanceId", deliveryOptions.getInstanceId());
        parameters.put("format", deliveryOptions.getFormat().name());

        final String newUrl = substituteUrl(url, parameters);
        return newUrl;
    }

    protected static String substituteUrl(String url, Map<String, Object> parameters) {
        if (url.contains(":")) {
            String newUrl = url;
            for (Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    try {
                        newUrl = newUrl.replaceAll(":" + entry.getKey(), URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        LOG.error("Could not find encoder: {}", e.getMessage(), e);
                    }
                }
            }
            return newUrl;
        }
        return url;
    }

    private static void mailReport(final DeliveryOptions deliveryOptions, final ByteArrayOutputStream outputStream) {
        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            final JavaMailer jm = new JavaMailer();
            jm.setTo(deliveryOptions.getMailTo());
            jm.setSubject(deliveryOptions.getInstanceId());
            jm.setMessageText("Here is your report from the OpenNMS report service.");
            jm.setInputStream(inputStream);

            switch (deliveryOptions.getFormat()) {
            case PDF:
            case SVG:
                jm.setInputStreamName(deliveryOptions.getInstanceId()+ ".pdf");
                jm.setInputStreamContentType("application/pdf");
                break;
            case CSV:
                jm.setInputStreamName(deliveryOptions.getInstanceId()+ ".csv");
                jm.setInputStreamContentType("text/csv");
                break;
            case HTML:
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
    public void runAndRender(final ReportParameters parameters, final ReportMode mode, final OutputStream outputStream) throws ReportException {
        if (LOG.isDebugEnabled()) {
            final Map<String, Object> reportParms = parameters.getReportParms(mode);
            for (final Entry<String,Object> entry : reportParms.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                LOG.debug("param {} set {}", key, value == null? "NULL" : value);
            }
        }

        try {
            getReportService(parameters.getReportId()).runAndRender(parameters.getReportParms(mode), parameters.getReportId(), parameters.getFormat(), outputStream);
        } catch (ReportException reportException) {
            logError(parameters.getReportId(), reportException);
            throw reportException;
        }
    }

}
