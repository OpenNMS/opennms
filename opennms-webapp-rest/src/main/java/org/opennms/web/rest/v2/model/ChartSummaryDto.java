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

@Schema(name = "ChartSummary", description = "A bar chart configured in chart-configuration.xml.")
public class ChartSummaryDto {

    private String name;
    private String title;
    private String subTitle;
    private String domainAxisLabel;
    private String rangeAxisLabel;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(final String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDomainAxisLabel() {
        return domainAxisLabel;
    }

    public void setDomainAxisLabel(final String domainAxisLabel) {
        this.domainAxisLabel = domainAxisLabel;
    }

    public String getRangeAxisLabel() {
        return rangeAxisLabel;
    }

    public void setRangeAxisLabel(final String rangeAxisLabel) {
        this.rangeAxisLabel = rangeAxisLabel;
    }
}
