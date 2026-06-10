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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A single auth definition. Describes how to obtain a token from an
 * authentication endpoint and how to extract that token from the response.
 * Once defined, the resulting token is referenced from elsewhere via the
 * {@code ${token:<name>}} metadata DSL placeholder.
 *
 * <p><b>Treat as immutable post-load.</b> The auth runtime computes a
 * fingerprint from this object's fields to key the token cache; if a
 * caller mutates header values, basic-auth credentials, etc. after
 * the {@link TokenAuthConfiguration} is loaded, the fingerprint shifts and
 * the cache silently forks, leaving the previously-cached token
 * orphaned. Setters are public for JAXB binding only.</p>
 */
@XmlRootElement(name = "token-auth")
@XmlAccessorType(XmlAccessType.FIELD)
public class TokenAuth implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_METHOD = "POST";

    /** Unique name used as the key in {@code ${token:<name>}}. */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /** Authentication endpoint URL. */
    @XmlElement(name = "url", required = true)
    private String url;

    /** HTTP method for the auth call. Defaults to POST. */
    @XmlElement(name = "method")
    private String method;

    /** Optional HTTP basic-auth shortcut for the auth call. */
    @XmlElement(name = "basic-auth")
    private BasicAuth basicAuth;

    /** Headers to send on the auth request. */
    @XmlElement(name = "header")
    private List<Header> headers = new ArrayList<>();

    /** Optional request body. */
    @XmlElement(name = "content")
    private Content content;

    /** Where the token is in the auth response. */
    @XmlElement(name = "token-from", required = true)
    private TokenFrom tokenFrom;

    /**
     * Optional proactive refresh interval in seconds. When set, the cache
     * fetches a new token shortly before this interval elapses. When unset,
     * the token is held until something invalidates it (for example, a 401
     * on a downstream call).
     */
    @XmlElement(name = "ttl-seconds")
    private Long ttlSeconds;

    /**
     * If true, disable SSL certificate verification for the auth call.
     * Useful for self-signed certs in lab environments. Default false.
     */
    @XmlElement(name = "disable-ssl-verification")
    private Boolean disableSslVerification;

    /**
     * If true, route the auth call through the JVM-configured proxy.
     * Default false.
     */
    @XmlElement(name = "use-system-proxy")
    private Boolean useSystemProxy;

    public TokenAuth() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getMethod() {
        return method == null ? DEFAULT_METHOD : method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public BasicAuth getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(final BasicAuth basicAuth) {
        this.basicAuth = basicAuth;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(final List<Header> headers) {
        this.headers = (headers == null) ? new ArrayList<>() : new ArrayList<>(headers);
    }

    public Content getContent() {
        return content;
    }

    public void setContent(final Content content) {
        this.content = content;
    }

    public TokenFrom getTokenFrom() {
        return tokenFrom;
    }

    public void setTokenFrom(final TokenFrom tokenFrom) {
        this.tokenFrom = tokenFrom;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(final Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public Boolean getDisableSslVerification() {
        return disableSslVerification;
    }

    public void setDisableSslVerification(final Boolean disableSslVerification) {
        this.disableSslVerification = disableSslVerification;
    }

    public boolean isDisableSslVerification() {
        return Boolean.TRUE.equals(disableSslVerification);
    }

    public Boolean getUseSystemProxy() {
        return useSystemProxy;
    }

    public void setUseSystemProxy(final Boolean useSystemProxy) {
        this.useSystemProxy = useSystemProxy;
    }

    public boolean isUseSystemProxy() {
        return Boolean.TRUE.equals(useSystemProxy);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenAuth)) return false;
        final TokenAuth other = (TokenAuth) o;
        return Objects.equals(name, other.name)
                && Objects.equals(url, other.url)
                && Objects.equals(method, other.method)
                && Objects.equals(basicAuth, other.basicAuth)
                && Objects.equals(headers, other.headers)
                && Objects.equals(content, other.content)
                && Objects.equals(tokenFrom, other.tokenFrom)
                && Objects.equals(ttlSeconds, other.ttlSeconds)
                && Objects.equals(disableSslVerification, other.disableSslVerification)
                && Objects.equals(useSystemProxy, other.useSystemProxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, method, basicAuth, headers, content, tokenFrom,
                ttlSeconds, disableSslVerification, useSystemProxy);
    }
}
