/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.tl1d;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Tl1Element.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "tl1-element")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tl1Element implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_USERID = "opennms";
    private static final String DEFAULT_PASSWORD = "opennms";
    private static final String DEFAULT_TL1_CLIENT_API = "org.opennms.netmgt.tl1d.Tl1ClientImpl";
    private static final String DEFAULT_TL1_MESSAGE_PARSER = "org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor";

    @XmlAttribute(name = "host", required = true)
    private String host;

    @XmlAttribute(name = "port")
    private Integer port;

    @XmlAttribute(name = "userid")
    private String userid;

    @XmlAttribute(name = "password")
    private String password;

    @XmlAttribute(name = "tl1-client-api")
    private String tl1ClientApi;

    @XmlAttribute(name = "tl1-message-parser")
    private String tl1MessageParser;

    @XmlAttribute(name = "reconnect-delay")
    private Long reconnectDelay;

    /**
     */
    public void deletePort() {
        this.port= null;
    }

    /**
     */
    public void deleteReconnectDelay() {
        this.reconnectDelay= null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Tl1Element) {
            Tl1Element temp = (Tl1Element)obj;
            boolean equals = Objects.equals(temp.host, host)
                && Objects.equals(temp.port, port)
                && Objects.equals(temp.userid, userid)
                && Objects.equals(temp.password, password)
                && Objects.equals(temp.tl1ClientApi, tl1ClientApi)
                && Objects.equals(temp.tl1MessageParser, tl1MessageParser)
                && Objects.equals(temp.reconnectDelay, reconnectDelay);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'host'.
     * 
     * @return the value of field 'Host'.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Returns the value of field 'password'.
     * 
     * @return the value of field 'Password'.
     */
    public String getPassword() {
        return this.password != null ? this.password : DEFAULT_PASSWORD;
    }

    /**
     * Returns the value of field 'port'.
     * 
     * @return the value of field 'Port'.
     */
    public Integer getPort() {
        return this.port != null ? this.port : Integer.valueOf("502");
    }

    /**
     * Returns the value of field 'reconnectDelay'.
     * 
     * @return the value of field 'ReconnectDelay'.
     */
    public Long getReconnectDelay() {
        return this.reconnectDelay != null ? this.reconnectDelay : Long.valueOf("30000");
    }

    /**
     * Returns the value of field 'tl1ClientApi'.
     * 
     * @return the value of field 'Tl1ClientApi'.
     */
    public String getTl1ClientApi() {
        return this.tl1ClientApi != null ? this.tl1ClientApi : DEFAULT_TL1_CLIENT_API;
    }

    /**
     * Returns the value of field 'tl1MessageParser'.
     * 
     * @return the value of field 'Tl1MessageParser'.
     */
    public String getTl1MessageParser() {
        return this.tl1MessageParser != null ? this.tl1MessageParser : DEFAULT_TL1_MESSAGE_PARSER;
    }

    /**
     * Returns the value of field 'userid'.
     * 
     * @return the value of field 'Userid'.
     */
    public String getUserid() {
        return this.userid != null ? this.userid : DEFAULT_USERID;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return this.port != null;
    }

    /**
     * Method hasReconnectDelay.
     * 
     * @return true if at least one ReconnectDelay has been added
     */
    public boolean hasReconnectDelay() {
        return this.reconnectDelay != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            host, 
            port, 
            userid, 
            password, 
            tl1ClientApi, 
            tl1MessageParser, 
            reconnectDelay);
        return hash;
    }

    /**
     * Sets the value of field 'host'.
     * 
     * @param host the value of field 'host'.
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Sets the value of field 'password'.
     * 
     * @param password the value of field 'password'.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the value of field 'port'.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(final Integer port) {
        this.port = port;
    }

    /**
     * Sets the value of field 'reconnectDelay'.
     * 
     * @param reconnectDelay the value of field 'reconnectDelay'.
     */
    public void setReconnectDelay(final Long reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    /**
     * Sets the value of field 'tl1ClientApi'.
     * 
     * @param tl1ClientApi the value of field 'tl1ClientApi'.
     */
    public void setTl1ClientApi(final String tl1ClientApi) {
        this.tl1ClientApi = tl1ClientApi;
    }

    /**
     * Sets the value of field 'tl1MessageParser'.
     * 
     * @param tl1MessageParser the value of field 'tl1MessageParser'.
     */
    public void setTl1MessageParser(final String tl1MessageParser) {
        this.tl1MessageParser = tl1MessageParser;
    }

    /**
     * Sets the value of field 'userid'.
     * 
     * @param userid the value of field 'userid'.
     */
    public void setUserid(final String userid) {
        this.userid = userid;
    }

}
