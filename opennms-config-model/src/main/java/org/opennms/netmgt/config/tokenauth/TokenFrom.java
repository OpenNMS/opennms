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
package org.opennms.netmgt.config.tokenauth;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Where the token is in the auth response. Exactly one of {@code jsonpath},
 * {@code header}, or {@code bodyAsToken} must be set; this is enforced at
 * load time rather than in the schema.
 */
@XmlRootElement(name = "token-from")
@XmlAccessorType(XmlAccessType.FIELD)
public class TokenFrom implements Serializable {
    private static final long serialVersionUID = 1L;

    /** JXPath expression into a JSON response body. */
    @XmlAttribute(name = "jsonpath")
    private String jsonpath;

    /** Name of a response header that carries the token. */
    @XmlAttribute(name = "header")
    private String header;

    /** When true, the entire response body is the token. */
    @XmlAttribute(name = "body-as-token")
    private Boolean bodyAsToken;

    public TokenFrom() {
    }

    public String getJsonpath() {
        return jsonpath;
    }

    public void setJsonpath(final String jsonpath) {
        this.jsonpath = jsonpath;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    public Boolean getBodyAsToken() {
        return bodyAsToken;
    }

    public void setBodyAsToken(final Boolean bodyAsToken) {
        this.bodyAsToken = bodyAsToken;
    }

    /** Returns true when {@link #bodyAsToken} is non-null and set to true. */
    public boolean isBodyAsToken() {
        return Boolean.TRUE.equals(bodyAsToken);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenFrom)) return false;
        final TokenFrom other = (TokenFrom) o;
        return Objects.equals(jsonpath, other.jsonpath)
                && Objects.equals(header, other.header)
                && Objects.equals(bodyAsToken, other.bodyAsToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonpath, header, bodyAsToken);
    }
}
