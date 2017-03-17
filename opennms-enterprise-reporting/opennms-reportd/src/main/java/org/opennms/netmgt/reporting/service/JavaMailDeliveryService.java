/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

            String mailer = report.getMailer();
            LOG.debug("deliverReport with mailer={}", mailer);
            SendmailConfig config = null;
            if (mailer != null && mailer.length() > 0) {
                config = m_JavamailConfigDao.getSendMailConfig(mailer);
            } else {
                config = m_JavamailConfigDao.getDefaultSendmailConfig();
            }
            JavaSendMailer sm = new JavaSendMailer(config);
            MimeMessage msg = new MimeMessage(sm.getSession());

            if (config.getSendmailMessage().isPresent() && config.getSendmailProtocol().isPresent()) {
                final SendmailMessage sendmailMessage = config.getSendmailMessage().get();
                final SendmailProtocol sendmailProtocol = config.getSendmailProtocol().get();

                MimeMessageHelper helper = new MimeMessageHelper(msg, true, sendmailProtocol.getCharSet());
                helper.setFrom(sendmailMessage.getFrom());
                helper.setTo(report.getRecipient());
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
