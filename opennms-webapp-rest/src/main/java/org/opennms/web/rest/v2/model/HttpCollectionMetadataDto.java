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

@Schema(description = "The values an HTTP collection editor can choose from.")
public class HttpCollectionMetadataDto {

    @Schema(description = "Selectable attribute types.")
    private List<String> attributeTypes;

    @Schema(description = "Selectable HTTP methods.")
    private List<String> methods;

    @Schema(description = "Selectable HTTP versions.")
    private List<String> httpVersions;

    @Schema(description = "Commonly used URL schemes.")
    private List<String> schemes;

    @Schema(description = "RRD settings suggested for a new collection.")
    private HttpRrdDto defaultRrd;

    public List<String> getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(final List<String> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(final List<String> methods) {
        this.methods = methods;
    }

    public List<String> getHttpVersions() {
        return httpVersions;
    }

    public void setHttpVersions(final List<String> httpVersions) {
        this.httpVersions = httpVersions;
    }

    public List<String> getSchemes() {
        return schemes;
    }

    public void setSchemes(final List<String> schemes) {
        this.schemes = schemes;
    }

    public HttpRrdDto getDefaultRrd() {
        return defaultRrd;
    }

    public void setDefaultRrd(final HttpRrdDto defaultRrd) {
        this.defaultRrd = defaultRrd;
    }
}
