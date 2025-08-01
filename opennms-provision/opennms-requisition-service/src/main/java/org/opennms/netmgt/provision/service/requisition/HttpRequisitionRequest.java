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

    @XmlAttribute(name = "use-system-proxy")
    private Boolean useSystemProxy = Boolean.FALSE;

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
        useSystemProxy = Boolean.valueOf(parameters.get("use-system-proxy"));
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

    public Boolean getUseSystemProxy() {
        return useSystemProxy;
    }

    public void setUseSystemProxy(Boolean useSystemProxy) {
        this.useSystemProxy = useSystemProxy;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HttpRequisitionRequest)) {
            return false;
        }
        HttpRequisitionRequest castOther = (HttpRequisitionRequest) other;
        return Objects.equals(url, castOther.url) && Objects.equals(username, castOther.username)
                && Objects.equals(password, castOther.password) && Objects.equals(strictSsl, castOther.strictSsl)
                && Objects.equals(useSystemProxy, castOther.useSystemProxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, username, password, strictSsl, useSystemProxy);
    }

}
