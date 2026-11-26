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

@Schema(description = "How the document of a source is requested.")
public class XmlRequestDto {

    @Schema(description = "HTTP method.", example = "GET")
    private String method;

    @Schema(description = "Request parameters, e.g. timeout or retries.")
    private List<ParameterDto> parameters;

    @Schema(description = "HTTP headers sent with the request.")
    private List<ParameterDto> headers;

    @Schema(description = "Content type of the request body.", example = "application/json")
    private String contentType;

    @Schema(description = "Request body.")
    private String content;

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public List<ParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(final List<ParameterDto> parameters) {
        this.parameters = parameters;
    }

    public List<ParameterDto> getHeaders() {
        return headers;
    }

    public void setHeaders(final List<ParameterDto> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
