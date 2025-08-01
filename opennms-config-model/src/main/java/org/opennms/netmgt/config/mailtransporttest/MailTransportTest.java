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

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Use this element to define a mail test with the
 * MailTransportMonitor. Supported
 *  use cases for the sequence are:
 *  
 *  a) Class will test that it can successfully send an email.
 *  b) Class will test that it can successfully connect to a mail server and get mailbox contents.
 *  c) Class will test that it can successfully read a new email message from a mail server.
 *  d) Class will test that it can send an email and read that same email from a mail server.
 *  
 *  The sequence support a max of one send and one receive server. If each are specified, the complete
 *  sequence is delivery of a message from one mail host to another.
 */

@XmlRootElement(name="mail-transport-test")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class MailTransportTest implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Defines a use case for testing mail. If both a send and a
     * read test are configured, then the use case (d) will be executed.
     *  
     */
    @XmlElement(name="mail-test", required=true)
    private MailTest m_mailTest;

    public MailTransportTest() {
    }

    public MailTest getMailTest() {
        return m_mailTest;
    }

    public void setMailTest(final MailTest mailTest) {
        m_mailTest = ConfigUtils.assertNotNull(mailTest, "mail-test");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_mailTest);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof MailTransportTest) {
            final MailTransportTest that = (MailTransportTest)obj;
            return Objects.equals(this.m_mailTest, that.m_mailTest);
        }
        return false;
    }

}
