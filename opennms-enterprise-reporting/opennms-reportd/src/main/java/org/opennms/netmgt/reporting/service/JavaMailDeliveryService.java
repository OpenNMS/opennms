package org.opennms.netmgt.reporting.service;

import java.io.File;

import javax.mail.MessagingException;

import org.opennms.core.utils.LogUtils;
import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.JavaMailConfigurationDao;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * <p>JavaMailDeliveryService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JavaMailDeliveryService implements ReportDeliveryService {

    JavaMailConfigurationDao m_JavamailConfigDao;

    /** {@inheritDoc} 
     * @throws ReportDeliveryException */
    public void deliverReport(Report report, String fileName) throws ReportDeliveryException {
        try {

            JavaSendMailer sm = new JavaSendMailer(m_JavamailConfigDao.getDefaultSendmailConfig());
            MimeMessageHelper helper = new MimeMessageHelper(sm.getMessage().getMimeMessage(),true);
                
            for(String recipient : report.getRecipientCollection()){
                helper.addTo(recipient);
            }
            
            helper.setSubject("OpenNMS Report: " + report.getReportName());
            helper.setText("OpenNMS Report: ");
                
            helper.addAttachment(fileName, new File(fileName));
                
            sm.setMessage(new MimeMailMessage(helper));
            sm.send();

        } catch (JavaMailerException e) {
            LogUtils.errorf(this, e, "Problem with JavaMailer %s", e.getMessage());
            throw new ReportDeliveryException("Caught JavaMailerException: " + e.getMessage());
        } catch (MessagingException e) {
            LogUtils.errorf(this, e, "Problem with Messaging %s", e.getMessage());
            throw new ReportDeliveryException("Caught MessagingException: " + e.getMessage());
        } catch (Exception e) {
            LogUtils.errorf(this, e, "Unexpected exception: %s",e.getMessage());
            throw new ReportDeliveryException("Caught unexpected " + e.getClass().getName() + ": " + e.getMessage());
        }
        
    }


    /**
     * <p>getJavamailConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.JavaMailConfigurationDao} object.
     */
    public JavaMailConfigurationDao getJavamailConfigDao() {
        return m_JavamailConfigDao;
    }

    
    /**
     * <p>setJavamailConfigDao</p>
     *
     * @param javamailConfigDao a {@link org.opennms.netmgt.dao.JavaMailConfigurationDao} object.
     */
    public void setJavamailConfigDao(JavaMailConfigurationDao javamailConfigDao) {
        m_JavamailConfigDao = javamailConfigDao;
    }

}
