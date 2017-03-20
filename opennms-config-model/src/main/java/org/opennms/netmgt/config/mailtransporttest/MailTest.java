/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.mailtransporttest;

  import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines a use case for testing mail. If both a send and a read
 * test are configured, then the use case (d) will be executed.
 */

@XmlRootElement(name="mail-test")
@XmlAccessorType(XmlAccessType.FIELD)
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
        if (readmailTest == null) {
            throw new IllegalArgumentException("'readmail-test' is a required element!");
        }
        m_readmailTest = readmailTest;
    }

    public SendmailTest getSendmailTest() {
        return m_sendmailTest;
    }

    public void setSendmailTest(final SendmailTest sendmailTest) {
        if (sendmailTest == null) {
            throw new IllegalArgumentException("'sendmail-test' is a required element!");
        }
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
