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
package org.opennms.features.reporting.rest.internal;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.reporting.core.DeliveryOptions;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ReportDetailsBuilder {

    private final ReportDetails reportDetails = new ReportDetails();


    public ReportDetailsBuilder withFormats(List<ReportFormat> formats) {
        reportDetails.setFormats(formats);
        return this;
    }

    public ReportDetailsBuilder withReportId(final String reportId) {
        reportDetails.setReportId(reportId);
        return this;
    }

    public ReportDetailsBuilder withParameters(final ReportParameters reportParameters) {
        reportDetails.setParameters(reportParameters);
        return this;
    }

    public ReportDetailsBuilder withCategories(Collection<Category> categories) {
        reportDetails.setCategories(Lists.newArrayList(categories));
        return this;
    }


    public ReportDetailsBuilder withSurveillanceCategories(List<OnmsCategory> surveillanceCategories) {
        reportDetails.setSurveillanceCategories(surveillanceCategories);
        return this;
    }

    public ReportDetailsBuilder withDeliveryOptions(final DeliveryOptions deliveryOptions) {
        reportDetails.setDeliveryOptions(deliveryOptions);
        return this;
    }

    public ReportDetailsBuilder withCronExpression(final String cronExpression) {
        reportDetails.setCronExpression(cronExpression);
        return this;
    }

    public ReportDetailsBuilder withTimezones(List<String> timezones) {
        reportDetails.setTimezones(timezones);
        return this;
    }

    public ReportDetailsBuilder withDefaultTimezones() {
        final List<String> allAvailableIds = Lists.newArrayList(TimeZone.getAvailableIDs());
        allAvailableIds.removeAll(ZoneId.SHORT_IDS.keySet()); // these cannot be used via ZoneId.of(...) so we remove them
        return withTimezones((allAvailableIds));
    }

    public ReportDetails build() {
        if (Strings.isNullOrEmpty(reportDetails.getReportId())) {
            throw new IllegalStateException("ReportDetails must have a report Id");
        }
        return reportDetails;
    }
}
