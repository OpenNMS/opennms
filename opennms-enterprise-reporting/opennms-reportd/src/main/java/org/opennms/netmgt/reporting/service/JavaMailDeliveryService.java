/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.reporting.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The Class JavaMailDeliveryService.
 * 
 * @author ranger
 */
public class JavaMailDeliveryService implements ReportDeliveryService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JavaMailDeliveryService.class);

    /** The JavaMail configuration DAO. */
    JavaMailConfigurationDao m_JavamailConfigDao;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.reporting.service.ReportDeliveryService#deliverReport(org.opennms.netmgt.config.reportd.Report, java.lang.String)
     */
    @Override
    public void deliverReport(Report report, String fileName) throws ReportDeliveryException {
        try {
            SendmailConfig config = null;

            if (report.getMailer().isPresent()) {
                final String mailer = report.getMailer().get();
                LOG.debug("deliverReport with mailer={}", mailer);
                config = m_JavamailConfigDao.getSendMailConfig(mailer);
            } else {
                LOG.debug("deliverReport with default sendmail config");
                config = m_JavamailConfigDao.getDefaultSendmailConfig();
            }

            JavaSendMailer sm = new JavaSendMailer(config);
            MimeMessage msg = new MimeMessage(sm.getSession());

            if (config.getSendmailMessage() != null && config.getSendmailProtocol() != null) {
                final SendmailMessage sendmailMessage = config.getSendmailMessage();
                final SendmailProtocol sendmailProtocol = config.getSendmailProtocol();

                final String charset = sendmailProtocol.getCharSet();
                msg.setFrom(new InternetAddress(sendmailMessage.getFrom()));
                if (!Strings.isNullOrEmpty(sendmailMessage.getReplyTo())) {
                    msg.setReplyTo(InternetAddress.parse(sendmailMessage.getReplyTo()));
                }
                final List<InternetAddress> to = new ArrayList<>();
                for (final String recipient : report.getRecipients()) {
                    to.add(new InternetAddress(recipient));
                }
                msg.setRecipients(Message.RecipientType.TO, to.toArray(new InternetAddress[0]));
                msg.setSubject("OpenNMS Report: " + report.getReportName(), charset);

                final MimeBodyPart textPart = new MimeBodyPart();
                if ("text/html".equals(sendmailProtocol.getMessageContentType().toLowerCase())) {
                    // plain-text and HTML renditions of the body as a multipart/alternative
                    final MimeMultipart alternative = new MimeMultipart("alternative");
                    final MimeBodyPart plain = new MimeBodyPart();
                    plain.setText(sendmailMessage.getBody().replaceAll("\\<[^>]*>",""), charset);
                    alternative.addBodyPart(plain);
                    final MimeBodyPart html = new MimeBodyPart();
                    html.setContent(sendmailMessage.getBody(), "text/html; charset=" + charset);
                    alternative.addBodyPart(html);
                    textPart.setContent(alternative);
                } else {
                    textPart.setText(sendmailMessage.getBody(), charset);
                }

                final MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(new File(fileName))));
                attachmentPart.setFileName(fileName);
                attachmentPart.setDisposition(Part.ATTACHMENT);

                final MimeMultipart mixed = new MimeMultipart("mixed");
                mixed.addBodyPart(textPart);
                mixed.addBodyPart(attachmentPart);
                msg.setContent(mixed);
                sm.send(msg);
            } else {
                LOG.error("sendmail-message or sendmail-protocol is not configured!");
            }

        } catch (JavaMailerException e) {
            LOG.error("Problem with JavaMailer {}", e.getMessage(), e);
            throw new ReportDeliveryException("Caught JavaMailerException: " + e.getMessage());
        } catch (MessagingException e) {
            LOG.error("Problem with Messaging {}", e.getMessage(), e);
            throw new ReportDeliveryException("Caught MessagingException: " + e.getMessage());
        } catch (Throwable e) {
            LOG.error("Unexpected exception: {}",e.getMessage(), e);
            throw new ReportDeliveryException("Caught unexpected " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    /**
     * Gets the JavaMail configuration DAO.
     *
     * @return the JavaMail configuration DAO
     */
    public JavaMailConfigurationDao getJavamailConfigDao() {
        return m_JavamailConfigDao;
    }

    /**
     * Sets the JavaMail configuration DAO.
     *
     * @param javamailConfigDao the new JavaMail configuration DAO
     */
    public void setJavamailConfigDao(JavaMailConfigurationDao javamailConfigDao) {
        m_JavamailConfigDao = javamailConfigDao;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.reporting.service.ReportDeliveryService#reloadConfiguration()
     */
    public void reloadConfiguration() {
        m_JavamailConfigDao.reloadConfiguration();
    }

}
