package org.opennms.netmgt.alarmd.northbounder.email;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.castor.DefaultJavamailConfigurationDao;
import org.springframework.core.io.FileSystemResource;


public class Playground {

    SendmailConfig m_sendmail;
    
    @Test
    public void test() throws Exception {
        // Setup JavaMail configuration DAO
        System.setProperty("opennms.home", "/Users/agalue/Development/opennms/git/develop/target/opennms-17.0.1-SNAPSHOT");
        DefaultJavamailConfigurationDao javaMailDao = new DefaultJavamailConfigurationDao();
        javaMailDao.setConfigResource(new FileSystemResource(new File("/Users/agalue/Development/opennms/git/develop/target/opennms-17.0.1-SNAPSHOT/etc/javamail-configuration.xml")));
        javaMailDao.afterPropertiesSet();

        // Creating a local copy of the SendmailConfig object, to avoid potential thread contention issues.
        ByteArrayInputStream is = null;
        try {
            final SendmailConfig sendmail = javaMailDao.getSendMailConfig("icee-customer-email");
            if (sendmail != null) {
                final String sendmailText = CastorUtils.marshal(sendmail);
                is = new ByteArrayInputStream(sendmailText.getBytes());
                m_sendmail = CastorUtils.unmarshal(SendmailConfig.class, is);
            }
        } catch (Exception e) {
            Assert.fail();
        } finally {
            IOUtils.closeQuietly(is);
        }

        m_sendmail.getSendmailMessage().setSubject("Testing 123");
        m_sendmail.getSendmailMessage().setBody("Testing 123, from Alejandro");
        
        try {
            JavaSendMailer mailer = new JavaSendMailer(m_sendmail, false);
            mailer.send();
        } catch (JavaMailerException e) {
            Assert.fail();
        }

    }
}
