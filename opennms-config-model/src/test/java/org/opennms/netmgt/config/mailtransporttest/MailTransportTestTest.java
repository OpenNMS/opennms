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
package org.opennms.netmgt.config.mailtransporttest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class MailTransportTestTest extends XmlTestNoCastor<MailTransportTest> {

    public MailTransportTestTest(final MailTransportTest sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final MailTransportTest mtt = new MailTransportTest();
        final MailTest mailTest = new MailTest();
        mtt.setMailTest(mailTest);

        final SendmailTest smt = new SendmailTest(3000l, true, false, false);
        mailTest.setSendmailTest(smt);

        smt.addJavamailProperty(new JavamailProperty("mail.smtp.userset", "false"));
        smt.addJavamailProperty("mail.smtp.ehlo", "true");

        smt.setSendmailHost("127.0.0.1", 25l);

        final SendmailProtocol sendmailProtocol = new SendmailProtocol();
        smt.setSendmailProtocol(sendmailProtocol);
        sendmailProtocol.setCharSet("us-ascii");
        sendmailProtocol.setMailer("smtpsend");
        sendmailProtocol.setMessageContentType("text/plain");
        sendmailProtocol.setMessageEncoding("7-bit");
        sendmailProtocol.setQuitWait(true);
        sendmailProtocol.setSslEnable(false);
        sendmailProtocol.setStartTls(false);
        sendmailProtocol.setTransport("smtp");

        final SendmailMessage message = new SendmailMessage();
        smt.setSendmailMessage(message);
        message.setTo("foo@gmail.com");
        message.setFrom("root@[127.0.0.1]");
        message.setSubject("OpenNMS Test Message ");
        message.setBody("This is an OpenNMS test message.");

        smt.setUserAuth(new UserAuth("opennms", "rulz"));

        final ReadmailTest rmt = new ReadmailTest(5000l, true, "INBOX", "OpenNMS Test Message", true);
        mailTest.setReadmailTest(rmt);

        rmt.addJavamailProperty(new JavamailProperty("mail.pop3.apop.enable", "false"));
        rmt.addJavamailProperty("mail.pop3.rsetbeforequit", "false");

        final ReadmailHost rmh = new ReadmailHost("pop.gmail.com", 995l);
        rmt.setReadmailHost(rmh);
        rmh.setReadmailProtocol(new ReadmailProtocol("pop3s", true, false));

        rmt.setUserAuth("bar", "foo");

        return Arrays.asList(new Object[][] {
                {
                    mtt,
                    new String(Files.readAllBytes(Paths.get("target/test-classes/mail-transport-test.xml"))),
                    "target/classes/xsds/mail-transport-test.xsd"
                }
        });
    }

}
