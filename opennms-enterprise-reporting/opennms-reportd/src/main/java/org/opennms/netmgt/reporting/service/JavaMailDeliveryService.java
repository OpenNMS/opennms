/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.reporting.service;

import java.io.File;

import javax.mail.MessagingException;

import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * <p>JavaMailDeliveryService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JavaMailDeliveryService implements ReportDeliveryService {
	
	
	private static final Logger LOG = LoggerFactory
			.getLogger(JavaMailDeliveryService.class);

    JavaMailConfigurationDao m_JavamailConfigDao;

    /** {@inheritDoc} 
     * @throws ReportDeliveryException */
    @Override
    public void deliverReport(Report report, String fileName) throws ReportDeliveryException {
        try {

            JavaSendMailer sm = null;
            String mailer = report.getMailer();
            LOG.debug("deliverReport with mailer={}", mailer);
            if (mailer != null && mailer.length() > 0) {
                sm = new JavaSendMailer(m_JavamailConfigDao.getSendMailConfig(mailer));
            } else {
                sm = new JavaSendMailer(m_JavamailConfigDao.getDefaultSendmailConfig());
            }
            MimeMessageHelper helper = new MimeMessageHelper(sm.getMessage().getMimeMessage(),true);
                
            helper.setTo(report.getRecipient());
            helper.setSubject("OpenNMS Report: " + report.getReportName());
            helper.setText("OpenNMS Report: "); // FIXME: sm.send() will override this.
                
            helper.addAttachment(fileName, new File(fileName));
                
            sm.setMessage(new MimeMailMessage(helper));
            sm.send();

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
     * <p>getJavamailConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.JavaMailConfigurationDao} object.
     */
    public JavaMailConfigurationDao getJavamailConfigDao() {
        return m_JavamailConfigDao;
    }

    
    /**
     * <p>setJavamailConfigDao</p>
     *
     * @param javamailConfigDao a {@link org.opennms.netmgt.dao.api.JavaMailConfigurationDao} object.
     */
    public void setJavamailConfigDao(JavaMailConfigurationDao javamailConfigDao) {
        m_JavamailConfigDao = javamailConfigDao;
    }

}
