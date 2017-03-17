/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class SendmailMessage.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="sendmail-message", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendmailMessage implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1343557787012712270L;

    /** The to. */
    @XmlAttribute(name="to")
    private String _to;

    /** The from. */
    @XmlAttribute(name="from")
    private String _from;

    /** The subject. */
    @XmlAttribute(name="subject")
    private String _subject;

    /** The body. */
    @XmlAttribute(name="body")
    private String _body;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new sendmail message.
     */
    public SendmailMessage() {
        super();
        setTo("root@localhost");
        setFrom("root@[127.0.0.1]");
        setSubject("OpenNMS Test Message");
        setBody("This is an OpenNMS test message.");
    }

    //-----------/
    //- Methods -/
    //-----------/

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SendmailMessage) {
            final SendmailMessage temp = (SendmailMessage)obj;
            return Objects.equals(temp._to, _to)
                    && Objects.equals(temp._from, _from)
                    && Objects.equals(temp._subject, _subject)
                    && Objects.equals(temp._body, _body);
        }
        return false;
    }

    /**
     * Returns the value of field 'body'.
     * 
     * @return the value of field 'Body'.
     */
    public String getBody() {
        return this._body == null ? "This is an OpenNMS test message." : this._body;
    }

    /**
     * Returns the value of field 'from'.
     * 
     * @return the value of field 'From'.
     */
    public String getFrom() {
        return this._from == null ? "root@[127.0.0.1]" : this._from;
    }

    /**
     * Returns the value of field 'subject'.
     * 
     * @return the value of field 'Subject'.
     */
    public String getSubject() {
        return this._subject == null ? "OpenNMS Test Message" : this._subject;
    }

    /**
     * Returns the value of field 'to'.
     * 
     * @return the value of field 'To'.
     */
    public String getTo() {
        return this._to == null ? "root@localhost" : this._to;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(_to, _from, _subject, _body);
    }

    /**
     * Sets the value of field 'body'.
     * 
     * @param body the value of field 'body'.
     */
    public void setBody(final String body) {
        this._body = body;
    }

    /**
     * Sets the value of field 'from'.
     * 
     * @param from the value of field 'from'.
     */
    public void setFrom(final String from) {
        this._from = from;
    }

    /**
     * Sets the value of field 'subject'.
     * 
     * @param subject the value of field 'subject'.
     */
    public void setSubject(final String subject) {
        this._subject = subject;
    }

    /**
     * Sets the value of field 'to'.
     * 
     * @param to the value of field 'to'.
     */
    public void setTo(final String to) {
        this._to = to;
    }

}
