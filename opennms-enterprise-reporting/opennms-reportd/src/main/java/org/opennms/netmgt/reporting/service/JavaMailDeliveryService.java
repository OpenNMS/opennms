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

public class JavaMailDeliveryService implements ReportDeliveryService {

    JavaMailConfigurationDao m_JavamailConfigDao;

    public void deliverReport(Report report, String fileName) {
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
            LogUtils.errorf(this,"Problem with JavaMailer %s", e.getMessage());
        } catch (MessagingException e) {
            LogUtils.errorf(this,"Problem with Messaing %s", e.getMessage());
        } catch (Exception e) {
            LogUtils.errorf(this,"Non Specific Error: %s",e.getMessage());
        }
        
    }


    public JavaMailConfigurationDao getJavamailConfigDao() {
        return m_JavamailConfigDao;
    }

    
    public void setJavamailConfigDao(JavaMailConfigurationDao javamailConfigDao) {
        m_JavamailConfigDao = javamailConfigDao;
    }

}
