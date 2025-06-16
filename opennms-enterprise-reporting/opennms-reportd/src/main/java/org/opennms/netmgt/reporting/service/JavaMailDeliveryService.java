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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;

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

                MimeMessageHelper helper = new MimeMessageHelper(msg, true, sendmailProtocol.getCharSet());
                helper.setFrom(sendmailMessage.getFrom());
                if (!Strings.isNullOrEmpty(sendmailMessage.getReplyTo())) {
                    helper.setReplyTo(sendmailMessage.getReplyTo());
                }
                helper.setTo(report.getRecipients().toArray(new String[0]));
                helper.setSubject("OpenNMS Report: " + report.getReportName());
                if ("text/html".equals(sendmailProtocol.getMessageContentType().toLowerCase())) {
                    helper.setText(sendmailMessage.getBody().replaceAll("\\<[^>]*>",""), sendmailMessage.getBody());
                } else {
                    helper.setText(sendmailMessage.getBody());
                }
                helper.addAttachment(fileName, new File(fileName));
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
