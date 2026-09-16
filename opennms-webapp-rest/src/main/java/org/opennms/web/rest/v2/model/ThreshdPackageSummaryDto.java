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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "A threshd package without its address and service detail, for list views.")
public class ThreshdPackageSummaryDto {

    @Schema(description = "Unique package name.", example = "example1")
    private String name;

    @Schema(description = "Filter rule selecting the interfaces this package applies to.")
    private String filter;

    @Schema(description = "Number of services in the package.", example = "1")
    private Integer serviceCount;

    @Schema(description = "Names of scheduled outages applied to the package.")
    private List<String> outageCalendars;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }
    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(final Integer serviceCount) {
        this.serviceCount = serviceCount;
    }
    public List<String> getOutageCalendars() {
        return outageCalendars;
    }

    public void setOutageCalendars(final List<String> outageCalendars) {
        this.outageCalendars = outageCalendars;
    }
}
