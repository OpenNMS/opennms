package org.opennms.netmgt.reporting.service;

import java.io.File;

import javax.mail.MessagingException;

import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.config.common.SendmailConfig;
import org.opennms.netmgt.config.common.SendmailMessage;
import org.opennms.netmgt.config.reportd.Report;
import org.springframework.mail.javamail.MimeMailMessage;

public class JavaMailDeliveryService implements ReportDeliveryService {

    SendmailConfig m_sendMailConfig = null;
    
    public SendmailConfig getSendMailConfig() {
        return m_sendMailConfig;
    }

    public void setSendMailConfig(SendmailConfig sendMailConfig) {
        m_sendMailConfig = sendMailConfig;
    }

    public void deliverReport(Report report, String fileName) {

        
        try {
            
            for(String recipient : report.getRecipientCollection()){
            JavaSendMailer sm = new JavaSendMailer(m_sendMailConfig);
            SendmailMessage sendmailMessage = new SendmailMessage();
            sendmailMessage.setSubject("OpenNMS Report: " + report.getReportName());
            sendmailMessage.setTo(recipient);
            
            MimeMailMessage mm = sm.buildMimeMessage(sendmailMessage);
            mm.getMimeMessageHelper().addAttachment(report.getReportOutput(), new File(fileName));
            sm.setMessage(mm);
            sm.send();
            }
            
        } catch (JavaMailerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    
    
}
