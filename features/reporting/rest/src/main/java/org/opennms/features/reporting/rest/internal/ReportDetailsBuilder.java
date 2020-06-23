/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
