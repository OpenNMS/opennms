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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class SendmailProtocol.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="sendmail-protocol", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendmailProtocol implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -562952928846549040L;

    /** The char set. */
    @XmlAttribute(name="char-set")
    private String _charSet;

    /** The mailer. */
    @XmlAttribute(name="mailer")
    private String _mailer;

    /** The message content type. */
    @XmlAttribute(name="message-content-type")
    private String _messageContentType;

    /** The message encoding. */
    @XmlAttribute(name="message-encoding")
    private String _messageEncoding;

    /** The quit wait flag. */
    @XmlAttribute(name="quit-wait")
    private Boolean _quitWait;

    /** The transport. */
    @XmlAttribute(name="transport")
    private String _transport;

    /** The SSL enable flag. */
    @XmlAttribute(name="ssl-enable")
    private Boolean _sslEnable;

    /** The start TLS flag. */
    @XmlAttribute(name="start-tls")
    private Boolean _startTls;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new sendmail protocol.
     */
    public SendmailProtocol() {
        super();
        setCharSet("us-ascii");
        setMailer("smtpsend");
        setMessageContentType("text/plain");
        setMessageEncoding("7-bit");
        setTransport("smtp");
    }


    //-----------/
    //- Methods -/
    //-----------/

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        if (obj instanceof SendmailProtocol) {
            SendmailProtocol temp = (SendmailProtocol)obj;
            if (this._charSet != null) {
                if (temp._charSet == null) return false;
                else if (!(this._charSet.equals(temp._charSet))) 
                    return false;
            }
            else if (temp._charSet != null)
                return false;
            if (this._mailer != null) {
                if (temp._mailer == null) return false;
                else if (!(this._mailer.equals(temp._mailer))) 
                    return false;
            }
            else if (temp._mailer != null)
                return false;
            if (this._messageContentType != null) {
                if (temp._messageContentType == null) return false;
                else if (!(this._messageContentType.equals(temp._messageContentType))) 
                    return false;
            }
            else if (temp._messageContentType != null)
                return false;
            if (this._messageEncoding != null) {
                if (temp._messageEncoding == null) return false;
                else if (!(this._messageEncoding.equals(temp._messageEncoding))) 
                    return false;
            }
            else if (temp._messageEncoding != null)
                return false;
            if (this._quitWait != temp._quitWait)
                return false;
            if (this._transport != null) {
                if (temp._transport == null) return false;
                else if (!(this._transport.equals(temp._transport))) 
                    return false;
            }
            else if (temp._transport != null)
                return false;
            if (this._sslEnable != temp._sslEnable)
                return false;
            if (this._startTls != temp._startTls)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'charSet'.
     * 
     * @return the value of field 'CharSet'.
     */
    public String getCharSet() {
        return this._charSet == null ? "us-ascii" : this._charSet;
    }

    /**
     * Returns the value of field 'mailer'.
     * 
     * @return the value of field 'Mailer'.
     */
    public String getMailer() {
        return this._mailer == null ? "smtpsend" : this._mailer;
    }

    /**
     * Returns the value of field 'messageContentType'.
     * 
     * @return the value of field 'MessageContentType'.
     */
    public String getMessageContentType() {
        return this._messageContentType == null ? "text/plain" : this._messageContentType;
    }

    /**
     * Returns the value of field 'messageEncoding'.
     * 
     * @return the value of field 'MessageEncoding'.
     */
    public String getMessageEncoding() {
        return this._messageEncoding == null ? "7-bit" : this._messageEncoding;
    }

    /**
     * Returns the value of field 'quitWait'.
     * 
     * @return the value of field 'QuitWait'.
     */
    public Boolean isQuitWait() {
        return this._quitWait == null ? Boolean.TRUE : this._quitWait;
    }

    /**
     * Returns the value of field 'sslEnable'.
     * 
     * @return the value of field 'SslEnable'.
     */
    public Boolean isSslEnable() {
        return this._sslEnable == null ? Boolean.FALSE : this._sslEnable;
    }

    /**
     * Returns the value of field 'startTls'.
     * 
     * @return the value of field 'StartTls'.
     */
    public Boolean isStartTls() {
        return this._startTls == null ? Boolean.FALSE : this._startTls;
    }

    /**
     * Returns the value of field 'transport'.
     * 
     * @return the value of field 'Transport'.
     */
    public String getTransport() {
        return this._transport == null ? "smtp" : this._transport;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 17;
        if (_charSet != null) {
            result = 37 * result + _charSet.hashCode();
        }
        if (_mailer != null) {
            result = 37 * result + _mailer.hashCode();
        }
        if (_messageContentType != null) {
            result = 37 * result + _messageContentType.hashCode();
        }
        if (_messageEncoding != null) {
            result = 37 * result + _messageEncoding.hashCode();
        }
        result = 37 * result + (_quitWait?0:1);
        if (_transport != null) {
            result = 37 * result + _transport.hashCode();
        }
        result = 37 * result + (_sslEnable?0:1);
        result = 37 * result + (_startTls?0:1);
        return result;
    }

    /**
     * Sets the value of field 'charSet'.
     * 
     * @param charSet the value of field 'charSet'.
     */
    public void setCharSet(final String charSet) {
        this._charSet = charSet;
    }

    /**
     * Sets the value of field 'mailer'.
     * 
     * @param mailer the value of field 'mailer'.
     */
    public void setMailer(final String mailer) {
        this._mailer = mailer;
    }

    /**
     * Sets the value of field 'messageContentType'.
     * 
     * @param messageContentType the value of field 'messageContentType'.
     */
    public void setMessageContentType(final String messageContentType) {
        this._messageContentType = messageContentType;
    }

    /**
     * Sets the value of field 'messageEncoding'.
     * 
     * @param messageEncoding the value of field 'messageEncoding'.
     */
    public void setMessageEncoding(final String messageEncoding) {
        this._messageEncoding = messageEncoding;
    }

    /**
     * Sets the value of field 'quitWait'.
     * 
     * @param quitWait the value of field 'quitWait'.
     */
    public void setQuitWait(final Boolean quitWait) {
        this._quitWait = quitWait;
    }

    /**
     * Sets the value of field 'sslEnable'.
     * 
     * @param sslEnable the value of field 'sslEnable'.
     */
    public void setSslEnable(final Boolean sslEnable) {
        this._sslEnable = sslEnable;
    }

    /**
     * Sets the value of field 'startTls'.
     * 
     * @param startTls the value of field 'startTls'.
     */
    public void setStartTls(final Boolean startTls) {
        this._startTls = startTls;
    }

    /**
     * Sets the value of field 'transport'.
     * 
     * @param transport the value of field 'transport'.
     */
    public void setTransport(final String transport) {
        this._transport = transport;
    }

}
