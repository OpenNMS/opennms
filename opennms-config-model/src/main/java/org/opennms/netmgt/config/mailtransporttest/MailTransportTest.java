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

import org.opennms.core.xml.ValidateUsing;

/**
 * Use this element to define a mail test with the
 * MailTransportMonitor. Supported
 *  use cases for the sequence are:
 *  
 *  a) Class will test that it can successfully send an email.
 *  b) Class will test that it can successfully connect to a mail
 * server and get mailbox contents.
 *  c) Class will test that it can successfully read a new email
 * message from a mail server.
 *  d) Class will test that it can send an email and read that same
 * email from a mail server.
 *  
 *  The sequence support a max of one send and one receive server.
 * If each are specified, the complete
 *  sequence is delivery of a message from one mail host to
 * another.
 */

@XmlRootElement(name="mail-transport-test")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class MailTransportTest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Defines a use case for testing mail. If both a send and a
     * read test are configured, then the use case (d) will be executed.
     *  
     */
    @XmlElement(name="mail-test")
    private MailTest m_mailTest;

    public MailTransportTest() {
    }

    public MailTest getMailTest() {
        return m_mailTest;
    }

    public void setMailTest(final MailTest mailTest) {
        if (mailTest == null) {
            throw new IllegalArgumentException("'mail-test' is a required element!");
        }
        m_mailTest = mailTest;
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
