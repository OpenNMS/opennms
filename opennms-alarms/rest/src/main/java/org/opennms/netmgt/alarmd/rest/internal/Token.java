/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.alarmd.rest.internal;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "Token")
@XmlRootElement(name = "Token")
public class Token {

    private String msg;
    private String error = "";
    private String userPrincipal;

    public Token() { }
    public Token(String message) { this.msg = message; }

    @XmlAttribute(name = "msg")
    public String getMsg() { return msg; }

    public void setMsg(String message) { this.msg = message; }

    @XmlAttribute(name = "timestamp")
    public String getTimestamp() { return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()); }

    public void setTimestamp(String timestamp) {  }

    @XmlElement(name = "Error", nillable = true)
    public String getError() { return error; }

    public Token setError(String error) {
        if (this.error == null || "".equals(this.error)) { this.error = "[" + error + "]"; }
        else { this.error += "  |  [ " + error + "]"; }
        return this;
    }

    @XmlElement(name = "UserPrincipal")
    public String getUserPrincipal() { return userPrincipal; }

    public void setUserPrincipal(String principal) { this.userPrincipal = principal; }
}
