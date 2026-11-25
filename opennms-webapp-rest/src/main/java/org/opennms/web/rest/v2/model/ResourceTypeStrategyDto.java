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

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A persistence selector or storage strategy of a resource type: a Java class and its parameters.")
public class ResourceTypeStrategyDto {

    @Schema(description = "Fully qualified name of the class implementing the strategy.",
            example = "org.opennms.netmgt.collection.support.IndexStorageStrategy", required = true)
    private String className;

    @Schema(description = "Parameters passed to the strategy, in order. A key may occur more than once.")
    private List<ParameterDto> parameters = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public List<ParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(final List<ParameterDto> parameters) {
        this.parameters = parameters;
    }
}
