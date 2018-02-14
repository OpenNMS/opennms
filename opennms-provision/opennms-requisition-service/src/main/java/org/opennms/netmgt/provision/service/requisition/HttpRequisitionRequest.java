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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.requisition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.RequisitionRequest;

import java.util.Map;
import java.util.Objects;

@XmlRootElement(name = "http-requisition-request")
@XmlAccessorType(XmlAccessType.NONE)
public class HttpRequisitionRequest implements RequisitionRequest {

    @XmlAttribute(name = "url")
    private String url;

    @XmlAttribute(name = "username")
    private String username;

    @XmlAttribute(name = "password")
    private String password;

    @XmlAttribute(name = "strict-ssl")
    private Boolean strictSsl;

    public HttpRequisitionRequest() { }

    public HttpRequisitionRequest(Map<String, String> parameters) {
        url = parameters.get("url");
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("url is required.");
        }
        username = parameters.get("username");
        password = parameters.get("password");
        if (parameters.containsKey("strict-ssl")) {
            strictSsl = Boolean.parseBoolean(parameters.get("strict-ssl"));
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getStrictSsl() {
        return strictSsl;
    }

    public void setStrictSsl(Boolean strictSsl) {
        this.strictSsl = strictSsl;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HttpRequisitionRequest)) {
            return false;
        }
        HttpRequisitionRequest castOther = (HttpRequisitionRequest) other;
        return Objects.equals(url, castOther.url) && Objects.equals(username, castOther.username)
                && Objects.equals(password, castOther.password) && Objects.equals(strictSsl, castOther.strictSsl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, username, password, strictSsl);
    }

}
