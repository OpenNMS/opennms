/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import org.hibernate.lob.ReaderInputStream;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Test for the mail monitor
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class MailTransportMonitorTest {

    MailTransportMonitor m_monitor;
    Map<String, Object> m_params;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        final Resource resource = new ClassPathResource("/etc/javamail-configuration.properties");
        final File homeDir = resource.getFile().getParentFile().getParentFile();

        System.out.println("homeDir: " + homeDir.getAbsolutePath());
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        m_monitor = new MailTransportMonitor();
        m_monitor.initialize(new HashMap<String,Object>());

        m_params = new HashMap<String, Object>();
        m_params.put("timeout", "3000");
        m_params.put("retries", "1");
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @Test
    @Ignore("requires real mail server")
    public void testEndToEnd() throws Exception {
        
        setupLocalhostSendGoogleRead2();

        final PollStatus status = m_monitor.poll(getMailService("127.0.0.1"), m_params);
        
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }
    
    /*
     * requires a gmail account that has a message in the INBOX subject: READTEST
     */
    @Test
    @Ignore("requires real mail server")
    public void readOnlyTest() throws Exception {
        m_params.put("timeout", "3000");
        m_params.put("retry", "1");
        m_params.put("strict-timeouts", "true");
        m_params.put("mail-transport-test", 
        "    <mail-transport-test >\n" + 
        "      <mail-test>\n" + 
        "        <readmail-test attempt-interval=\"2000\" mail-folder=\"INBOX\" subject-match=\"READTEST\" >\n" + 
        "          <readmail-host host=\"imap.gmail.com\" port=\"993\">\n" + 
        "            <readmail-protocol ssl-enable=\"true\" transport=\"imaps\" />\n" + 
        "          </readmail-host>\n" + 
        "          <user-auth user-name=\"username\" password=\"password\"/>\n" + 
        "        </readmail-test>\n" + 
        "      </mail-test>\n"+
        "    </mail-transport-test>\n");
        PollStatus status = m_monitor.poll(getMailService("127.0.0.1"), m_params);
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

    @Test
    public void testLoadXmlProperties() throws InvalidPropertiesFormatException, IOException {
        Properties props = new Properties();
        
        Reader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" + 
                "<properties>\n" + 
                "<comment>Hi</comment>\n" + 
                "<entry key=\"foo\">1</entry>\n" + 
                "<entry key=\"fu\">baz</entry>\n" + 
                "</properties>");
        InputStream stream = new ReaderInputStream(reader );
        props.loadFromXML(stream);
        assertEquals("1", props.get("foo"));
    }

    private void setupLocalhostSendGoogleRead2() {
        m_params.put("timeout", "3000");
        m_params.put("retry", "1");
        m_params.put("strict-timeouts", "true");
        m_params.put("mail-transport-test", "<mail-transport-test >\n" + 
        		"\n" + 
        		"<!--  Example end2end test sending to localhost and reading from gmail.  In an\n" + 
        		"      end2end test, mail is sent to the specified host and read from the specified host.\n" + 
        		"      If the host value is set to ${ipaddr}, then the IP address of the service being\n" + 
        		"      polled will be used.  And end2end test is configured when both a send and a read\n" + 
        		"      test are defined.  The subject in the send is used for the match and the subject is\n" + 
        		"      modified to have the current time in millis appended.  The subject-match attribute in\n" + 
        		"      the read test is ignored for the end2end test.-->\n" + 
        		"\n" + 
        		"  <mail-test>\n" + 
        		"    <sendmail-test attempt-interval=\"3000\" debug=\"true\" use-authentication=\"false\" use-jmta=\"false\">\n" + 
        		"    \n" + 
        		"      <!-- These 2 properties are passed directly to the javamailer class.  The will\n" + 
        		"           be overridden if they are the same properties that are derived based on the\n" + 
        		"           other configuration elements.  Mainly here for convenience allowing properties\n" + 
        		"           to be set that don\'t get set by the configuration. -->\n" + 
        		"      <javamail-property name=\"mail.smtp.userset\" value=\"false\"/>\n" + 
        		"      <javamail-property name=\"mail.smtp.ehlo\" value=\"true\"/>\n" + 
        		" \n" + 
        		"      <!-- Connect to local MTA and send... no auth required but the configuration\n" + 
        		"           requires auth be configured.  Disable with use-authentication attribute above. -->\n" + 
        		"      <sendmail-host host=\"cartman\" port=\"25\"/>\n" + 
        		"      <sendmail-protocol char-set=\"us-ascii\" \n" + 
        		"                         mailer=\"smtpsend\" \n" + 
        		"                         message-content-type=\"text/plain\" \n" + 
        		"                         message-encoding=\"7-bit\" \n" + 
        		"                         quit-wait=\"true\" \n" + 
        		"                         ssl-enable=\"false\" \n" + 
        		"                         start-tls=\"false\" \n" + 
        		"                         transport=\"smtp\" />\n" + 
        		"      <sendmail-message to=\"dhustace@gmail.com\" \n" + 
        		"                        from=\"david@opennms.org\" \n" + 
        		"                        subject=\"OpenNMS Test Message \"\n" + 
        		"                        body=\"This is an OpenNMS test message.\" />\n" + 
        		"      <user-auth user-name=\"opennms\" password=\"rulz\" />\n" + 
        		"    </sendmail-test>\n" + 
        		"\n" + 
        		"    <!-- Read portion of the test.  Check to see if local MTA has delivered mail to Google Gmail account.  The\n" + 
        		"         attempt interval gives a delay between send and read test as well as between each retry. -->    \n" + 
        		"    <readmail-test attempt-interval=\"5000\" debug=\"true\" mail-folder=\"INBOX\" subject-match=\"OpenNMS Test Message\" delete-all-mail=\"false\" >\n" + 
        		"    \n" + 
        		"      <!-- Sample properties that you may want to set... these examples are the javamail defaults. -->\n" + 
        		"      <javamail-property name=\"mail.pop3.apop.enable\" value=\"false\"/>\n" + 
        		"      <javamail-property name=\"mail.pop3.rsetbeforequit\" value=\"false\" />\n" + 
        		"      \n" + 
        		"      <readmail-host host=\"imap.gmail.com\" port=\"993\">\n" + 
        		"        <readmail-protocol ssl-enable=\"true\" start-tls=\"false\" transport=\"imaps\"/>\n" + 
        		"      </readmail-host>\n" + 
        		"      \n" + 
        		"      <user-auth user-name=\"dhustace\" password=\"abc\" />\n" + 
        		"    </readmail-test>\n" + 
        		"  </mail-test>\n" + 
        		"</mail-transport-test>\n" + 
        		"");
        
    }
        
    protected MonitoredService getMailService(String hostname) throws Exception {
        return getMailService(hostname, InetAddressUtils.addr(hostname));
    }
    
    protected MonitoredService getMailService(String hostname, InetAddress ip) throws Exception {
        MonitoredService svc = new MockMonitoredService(1, hostname, ip, "MAIL");
        m_monitor.initialize(svc);
        return svc;
    }


}
