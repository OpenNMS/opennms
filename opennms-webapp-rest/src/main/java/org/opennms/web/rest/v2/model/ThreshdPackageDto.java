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

@Schema(description = """
        A threshd package: a filter selecting the interfaces to threshold, plus the services to evaluate
        on them. Writes replace the whole package rather than patching it.""")
public class ThreshdPackageDto {

    @Schema(description = "Unique package name.", example = "example1", required = true)
    private String name;

    @Schema(description = "Filter rule selecting the interfaces this package applies to.",
            example = "IPADDR != '0.0.0.0'", required = true)
    private String filter;

    @Schema(description = "Individual IP addresses always included.")
    private List<String> specifics;

    @Schema(description = "Address ranges included.")
    private List<IpRangeDto> includeRanges;

    @Schema(description = "Address ranges excluded.")
    private List<IpRangeDto> excludeRanges;

    @Schema(description = "URLs of files listing further addresses to include.")
    private List<String> includeUrls;

    @Schema(description = "Services thresholded within this package.")
    private List<ThreshdServiceDto> services;

    @Schema(description = "Names of scheduled outages during which thresholding is suspended. Also "
            + "maintained by the scheduled outages API.")
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
    public List<String> getSpecifics() {
        return specifics;
    }

    public void setSpecifics(final List<String> specifics) {
        this.specifics = specifics;
    }
    public List<IpRangeDto> getIncludeRanges() {
        return includeRanges;
    }

    public void setIncludeRanges(final List<IpRangeDto> includeRanges) {
        this.includeRanges = includeRanges;
    }
    public List<IpRangeDto> getExcludeRanges() {
        return excludeRanges;
    }

    public void setExcludeRanges(final List<IpRangeDto> excludeRanges) {
        this.excludeRanges = excludeRanges;
    }
    public List<String> getIncludeUrls() {
        return includeUrls;
    }

    public void setIncludeUrls(final List<String> includeUrls) {
        this.includeUrls = includeUrls;
    }
    public List<ThreshdServiceDto> getServices() {
        return services;
    }

    public void setServices(final List<ThreshdServiceDto> services) {
        this.services = services;
    }
    public List<String> getOutageCalendars() {
        return outageCalendars;
    }

    public void setOutageCalendars(final List<String> outageCalendars) {
        this.outageCalendars = outageCalendars;
    }
}
