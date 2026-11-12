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
package org.opennms.web.rest.v2.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The URL an HTTP collection URI requests, and how the response is matched. A null field takes its schema default.")
public class HttpUrlDto {

    @Schema(description = "HTTP method, `GET` or `POST`. Defaults to `GET`.", example = "GET")
    private String method;

    @Schema(description = "HTTP version, `1.0` or `1.1`. Defaults to `1.1`.", example = "1.1")
    private String httpVersion;

    @Schema(description = "User-Agent header to send.")
    private String userAgent;

    @Schema(description = "Host header to send, for name-based virtual hosts.")
    private String virtualHost;

    @Schema(description = "URL scheme. Defaults to `http`.", example = "http")
    private String scheme;

    @Schema(description = "User info of the URL (`user:password`) for basic authentication.")
    private String userInfo;

    @Schema(description = "Host to connect to. Defaults to `${ipaddr}`, the address of the interface being collected.", example = "${ipaddr}")
    private String host;

    @Schema(description = "Port to connect to. Defaults to 80.", example = "80")
    private Integer port;

    @Schema(description = "Path of the URL.", example = "/test/resources/httpcolltest.html", required = true)
    private String path;

    @Schema(description = "Query string of the URL, without the leading `?`.")
    private String query;

    @Schema(description = "Fragment of the URL.")
    private String fragment;

    @Schema(description = "Regular expression the response body is matched against; its groups provide the attribute values. Defaults to `(.*)`.", example = "(?s).*?Document Count:\\s+([0-9]+).*")
    private String matches;

    @Schema(description = "Range of acceptable HTTP status codes, such as `100-399` (the default).", example = "100-399")
    private String responseRange;

    @Schema(description = "Regular expression flag `canonicalEquivalence`. Defaults to false.")
    private Boolean canonicalEquivalence;

    @Schema(description = "Regular expression flag `caseInsensitive`. Defaults to false.")
    private Boolean caseInsensitive;

    @Schema(description = "Regular expression flag `comments`. Defaults to false.")
    private Boolean comments;

    @Schema(description = "Regular expression flag `dotall`. Defaults to false.")
    private Boolean dotall;

    @Schema(description = "Regular expression flag `literal`. Defaults to false.")
    private Boolean literal;

    @Schema(description = "Regular expression flag `multiline`. Defaults to false.")
    private Boolean multiline;

    @Schema(description = "Regular expression flag `unicodeCase`. Defaults to false.")
    private Boolean unicodeCase;

    @Schema(description = "Regular expression flag `unixLines`. Defaults to false.")
    private Boolean unixLines;

    @Schema(description = "Form parameters sent with a POST request.")
    private List<ParameterDto> parameters;

    @Schema(description = "Additional request headers.")
    private List<HttpHeaderDto> headers;

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(final String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(final String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(final String userInfo) {
        this.userInfo = userInfo;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(final String fragment) {
        this.fragment = fragment;
    }

    public String getMatches() {
        return matches;
    }

    public void setMatches(final String matches) {
        this.matches = matches;
    }

    public String getResponseRange() {
        return responseRange;
    }

    public void setResponseRange(final String responseRange) {
        this.responseRange = responseRange;
    }

    public Boolean getCanonicalEquivalence() {
        return canonicalEquivalence;
    }

    public void setCanonicalEquivalence(final Boolean canonicalEquivalence) {
        this.canonicalEquivalence = canonicalEquivalence;
    }

    public Boolean getCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(final Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public Boolean getComments() {
        return comments;
    }

    public void setComments(final Boolean comments) {
        this.comments = comments;
    }

    public Boolean getDotall() {
        return dotall;
    }

    public void setDotall(final Boolean dotall) {
        this.dotall = dotall;
    }

    public Boolean getLiteral() {
        return literal;
    }

    public void setLiteral(final Boolean literal) {
        this.literal = literal;
    }

    public Boolean getMultiline() {
        return multiline;
    }

    public void setMultiline(final Boolean multiline) {
        this.multiline = multiline;
    }

    public Boolean getUnicodeCase() {
        return unicodeCase;
    }

    public void setUnicodeCase(final Boolean unicodeCase) {
        this.unicodeCase = unicodeCase;
    }

    public Boolean getUnixLines() {
        return unixLines;
    }

    public void setUnixLines(final Boolean unixLines) {
        this.unixLines = unixLines;
    }

    public List<ParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(final List<ParameterDto> parameters) {
        this.parameters = parameters;
    }

    public List<HttpHeaderDto> getHeaders() {
        return headers;
    }

    public void setHeaders(final List<HttpHeaderDto> headers) {
        this.headers = headers;
    }
}
