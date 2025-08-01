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

/**
 * Defines a use case for testing mail. If both a send and a read
 * test are configured, then the use case (d) will be executed.
 */

@XmlRootElement(name="mail-test")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class MailTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name="sendmail-test")
    private SendmailTest m_sendmailTest;

    @XmlElement(name="readmail-test")
    private ReadmailTest m_readmailTest;

    public MailTest() {
    }

    public ReadmailTest getReadmailTest() {
        return m_readmailTest;
    }

    public void setReadmailTest(final ReadmailTest readmailTest) {
        m_readmailTest = readmailTest;
    }

    public SendmailTest getSendmailTest() {
        return m_sendmailTest;
    }

    public void setSendmailTest(final SendmailTest sendmailTest) {
        m_sendmailTest = sendmailTest;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_sendmailTest, m_readmailTest);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof MailTest) {
            final MailTest that = (MailTest)obj;
            return Objects.equals(this.m_sendmailTest, that.m_sendmailTest) &&
                    Objects.equals(this.m_readmailTest, that.m_readmailTest);
        }
        return false;
    }

}
