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

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.api.reporting.parameter.ReportTimezoneParm;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.reporting.core.DeliveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ReportDetails {
	private static final Logger LOG = LoggerFactory.getLogger(ReportDetails.class);

    private String reportId;
    private ReportParameters parameters = new ReportParameters();
    private List<ReportFormat> formats = Lists.newArrayList();
    private List<Category> categories = Lists.newArrayList();
    private List<OnmsCategory> surveillanceCategories = Lists.newArrayList();
    private List<String> timezones = Lists.newArrayList();
    private DeliveryOptions deliveryOptions;
    private String cronExpression;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = Objects.requireNonNull(reportId);
    }

    public ReportParameters getParameters() {
        return parameters;
    }

    public void setParameters(ReportParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }

    public List<ReportFormat> getFormats() {
        return Collections.unmodifiableList(formats);
    }

    public void setFormats(List<ReportFormat> formats) {
        this.formats.clear();
        this.formats.addAll(formats);
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public void setCategories(List<Category> categories) {
        Objects.requireNonNull(categories);
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public List<OnmsCategory> getSurveillanceCategories() {
        return Collections.unmodifiableList(surveillanceCategories);
    }

    public void setSurveillanceCategories(List<OnmsCategory> surveillanceCategories) {
        Objects.requireNonNull(surveillanceCategories);
        this.surveillanceCategories.clear();
        this.surveillanceCategories.addAll(surveillanceCategories);
    }

    public void setDeliveryOptions(DeliveryOptions deliveryOptions) {
        this.deliveryOptions = deliveryOptions;
    }

    public DeliveryOptions getDeliveryOptions() {
        return deliveryOptions;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = Objects.requireNonNull(cronExpression);
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public List<String> getTimezones() {
        return timezones;
    }

    public void setTimezones(List<String> timezones) {
        this.timezones = Objects.requireNonNull(timezones);
    }

    public JSONObject toJson() {
        // Convert formats
        final JSONArray jsonFormats = new JSONArray();
        for (ReportFormat eachFormat : formats) {
            final JSONObject jsonFormat = new JSONObject();
            jsonFormat.put("ordinal", eachFormat.ordinal());
            jsonFormat.put("name", eachFormat.name());
            jsonFormats.put(jsonFormat);
        }

        // Convert parameters
        final JSONArray jsonParameters = new JSONArray();
        if (parameters.getDateParms() != null) {
            for (ReportDateParm dateParm : parameters.getDateParms()) {
                final JSONObject jsonDateParm = new JSONObject();
                jsonDateParm.put("type", "date");
                jsonDateParm.put("name", dateParm.getName());
                jsonDateParm.put("displayName", dateParm.getDisplayName());

                // This value is mostly false. Only Availability Reports can set this to true.
                // This also means, that Jasper Reports can never have an absolute date parameter when run scheduled.
                jsonDateParm.put("useAbsoluteDate", dateParm.getUseAbsoluteDate());

                // Relative date values
                jsonDateParm.put("count", dateParm.getCount());
                jsonDateParm.put("interval", dateParm.getInterval());
                jsonDateParm.put("hours", dateParm.getHours()); // also used for absolute dates
                jsonDateParm.put("minutes", dateParm.getMinutes()); // also used for absolute dates

                // Absolute date values
                if (dateParm.getDate() != null) {
                    final ZoneId zoneId = getZoneId();
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setTimeZone(TimeZone.getTimeZone(zoneId));
                    jsonDateParm.put("date", sdf.format(dateParm.getDate()));
                }
                jsonParameters.put(jsonDateParm);
            }
        }
        if (parameters.getDoubleParms() != null) {
            for (ReportDoubleParm doubleParm : parameters.getDoubleParms()) {
                final JSONObject jsonDoubleParm = new JSONObject();
                jsonDoubleParm.put("type", "double");
                jsonDoubleParm.put("name", doubleParm.getName());
                jsonDoubleParm.put("displayName", doubleParm.getDisplayName());
                jsonDoubleParm.put("value", doubleParm.getValue());
                jsonDoubleParm.put("inputType", doubleParm.getInputType());
                jsonParameters.put(jsonDoubleParm);
            }
        }
        if (parameters.getFloatParms() != null) {
            for (ReportFloatParm floatParm : parameters.getFloatParms()) {
                final JSONObject jsonFloatParm = new JSONObject();
                jsonFloatParm.put("type", "float");
                jsonFloatParm.put("name", floatParm.getName());
                jsonFloatParm.put("displayName", floatParm.getDisplayName());
                jsonFloatParm.put("value", floatParm.getValue());
                jsonFloatParm.put("inputType", floatParm.getInputType());
                jsonParameters.put(jsonFloatParm);
            }
        }
        if (parameters.getIntParms() != null) {
            for (ReportIntParm intParm : parameters.getIntParms()) {
                final JSONObject jsonIntParm = new JSONObject();
                jsonIntParm.put("type", "integer");
                jsonIntParm.put("name", intParm.getName());
                jsonIntParm.put("displayName", intParm.getDisplayName());
                jsonIntParm.put("value", intParm.getValue());
                jsonIntParm.put("inputType", intParm.getInputType());
                jsonParameters.put(jsonIntParm);
            }
        }
        if (parameters.getStringParms() != null) {
            for (ReportStringParm stringParm : parameters.getStringParms()) {
                final JSONObject jsonStringParm = new JSONObject();
                jsonStringParm.put("type", "string");
                jsonStringParm.put("name", stringParm.getName());
                jsonStringParm.put("displayName", stringParm.getDisplayName());
                jsonStringParm.put("value", stringParm.getValue());
                jsonStringParm.put("inputType", stringParm.getInputType());
                jsonParameters.put(jsonStringParm);
            }
        }
        if (parameters.getTimezoneParms() != null) {
            for (ReportTimezoneParm parm : parameters.getTimezoneParms()) {
                final JSONObject jsonTimezoneParm = new JSONObject();
                jsonTimezoneParm.put("type", "timezone");
                jsonTimezoneParm.put("name", parm.getName());
                jsonTimezoneParm.put("displayName", parm.getDisplayName());
                jsonTimezoneParm.put("value", parm.getValue());
                jsonParameters.put(jsonTimezoneParm);
            }
        }

        // Convert categories
        final JSONArray jsonCategories = new JSONArray();
        for (OnmsCategory eachCategory : surveillanceCategories) {
            jsonCategories.put(eachCategory.getName());
        }

        // Convert surveillanceCategories
        final JSONArray jsonSurveillanceCategories = new JSONArray();
        for (Category eachCategory : categories) {
            jsonSurveillanceCategories.put(eachCategory.getLabel());
        }

        // Convert timezones
        final JSONArray jsonTimezones = new JSONArray(timezones);

        // Create return object
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", reportId);
        jsonObject.put("parameters", jsonParameters);
        jsonObject.put("formats", jsonFormats);
        jsonObject.put("categories", jsonCategories);
        jsonObject.put("surveillanceCategories", jsonSurveillanceCategories);
        jsonObject.put("timezones", jsonTimezones);

        // Apply deliveryOptions if defined
        if (deliveryOptions != null) {
            jsonObject.put("deliveryOptions", new JSONObject(deliveryOptions));
        }
        // Apply cronExpression if defined
        if (!Strings.isNullOrEmpty(cronExpression)) {
            jsonObject.put("cronExpression", cronExpression);
        }
        return jsonObject;
    }

    private ZoneId getZoneId() {
        ZoneId zoneId = ZoneId.systemDefault();
        final List<ReportTimezoneParm> zones = parameters.getTimezoneParms();
        if (zones != null && zones.size() > 0) {
            try {
                zoneId = ZoneId.of(zones.get(0).getValue());
            } catch (final Exception e) {
                LOG.warn("Failed to determine timezone from: {}", zones.get(0));
            }
        }
        return zoneId;
    }
}
