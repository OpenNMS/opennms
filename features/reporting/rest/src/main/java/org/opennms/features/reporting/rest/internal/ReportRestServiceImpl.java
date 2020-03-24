/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.ReportParameterBuilder;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.features.reporting.rest.ReportRestService;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.DeliveryConfig;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.reporting.core.svclayer.ScheduleConfig;
import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.SchedulerService;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.opennms.web.svclayer.model.DatabaseReportDescription;
import org.opennms.web.svclayer.model.ReportRepositoryDescription;
import org.opennms.web.svclayer.model.TriggerDescription;
import org.opennms.web.svclayer.support.SchedulerContextException;
import org.opennms.web.svclayer.support.SchedulerException;
import org.opennms.web.utils.QueryParameters;
import org.opennms.web.utils.QueryParametersBuilder;
import org.opennms.web.utils.ResponseUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public class ReportRestServiceImpl implements ReportRestService {

    private final DatabaseReportListService databaseReportListService;
    private final ReportWrapperService reportWrapperService;
    private final CategoryDao categoryDao;
    private final CategoryConfigDao categoryConfigDao;
    private final ReportStoreService reportStoreService;
    private final SchedulerService schedulerService;
    private final ReportCatalogDao reportCatalogDao;

    public ReportRestServiceImpl(DatabaseReportListService databaseReportListService,
                                 ReportWrapperService reportWrapperService,
                                 CategoryDao categoryDao,
                                 CategoryConfigDao categoryConfigDao,
                                 ReportStoreService reportStoreService,
                                 SchedulerService schedulerService,
                                 ReportCatalogDao reportCatalogDao) {
        this.databaseReportListService = Objects.requireNonNull(databaseReportListService);
        this.reportWrapperService = Objects.requireNonNull(reportWrapperService);
        this.categoryDao = Objects.requireNonNull(categoryDao);
        this.categoryConfigDao = Objects.requireNonNull(categoryConfigDao);
        this.reportStoreService = Objects.requireNonNull(reportStoreService);
        this.schedulerService = Objects.requireNonNull(schedulerService);
        this.reportCatalogDao = Objects.requireNonNull(reportCatalogDao);
    }

    @Override
    public Response listReports() {
        final List<ReportRepositoryDescription> activeRepositories = databaseReportListService.getActiveRepositories();
        final List<DatabaseReportDescription> reportDescriptions = activeRepositories.stream()
                .flatMap(repositoryDescriptor -> databaseReportListService.getReportsByRepositoryId(repositoryDescriptor.getId()).stream())
                .collect(Collectors.toList());
        if (reportDescriptions.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray jsonArray = new JSONArray();
        for (DatabaseReportDescription description : reportDescriptions) {
            final JSONObject json = new JSONObject();
            json.put("id", description.getId().trim());
            json.put("name", description.getDisplayName().trim());
            json.put("description", Strings.isNullOrEmpty(description.getDescription()) ? "" : description.getDescription().trim());
            json.put("allowAccess", description.getAllowAccess());
            json.put("online", description.getIsOnline());
            json.put("repositoryId", description.getRepositoryId());
            jsonArray.put(json);
        }
        return Response.ok(jsonArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getReportDetails(String reportId, String userId) {
        final List<ReportFormat> formats = reportWrapperService.getFormats(reportId);
        final ReportParameters parameters = reportWrapperService.getParameters(reportId);
        final Collection<Category> categories = categoryConfigDao.findAll();
        final List<OnmsCategory> surveillanceCategories = categoryDao.findAll();
        final ReportDetailsBuilder reportDetailsBuilder = new ReportDetailsBuilder()
                .withReportId(reportId)
                .withFormats(formats)
                .withParameters(parameters)
                .withCategories(categories)
                .withSurveillanceCategories(surveillanceCategories)
                .withDefaultTimezones();

        // Apply delivery Options if user Id is provided
        if (userId != null) {
            final DeliveryOptions deliveryOptions = reportWrapperService.getDeliveryOptions(reportId, userId);
            reportDetailsBuilder.withDeliveryOptions(deliveryOptions);
        }

        // Convert to JSON
        final JSONObject jsonObject = reportDetailsBuilder.build().toJson();
        return Response.ok(jsonObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response scheduleReport(final Map<String, Object> parameters) {
        try {
            final ReportParameters reportParameters = parseParameters(parameters, ReportMode.SCHEDULED);
            final DeliveryOptions deliveryOptions = parseDeliveryOptions(parameters);
            final ScheduleConfig scheduleConfig = new ScheduleConfig(reportParameters, deliveryOptions, (String) parameters.get("cronExpression"));
            schedulerService.addCronTrigger(scheduleConfig);
        } catch (SchedulerContextException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex.getContext(), ex.getRawMessage()));
        } catch (SchedulerException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex));
        }
        return Response.accepted().build();
    }

    @Override
    public Response deliverReport(final Map<String, Object> parameters) {
        try {
            final ReportParameters reportParameters = parseParameters(parameters, ReportMode.IMMEDIATE);
            final DeliveryOptions deliveryOptions = parseDeliveryOptions(parameters);
            final DeliveryConfig deliveryConfig = new DeliveryConfig(reportParameters, deliveryOptions);
            schedulerService.execute(deliveryConfig);
        } catch (SchedulerContextException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex.getContext(), ex.getRawMessage()));
        } catch (SchedulerException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex));
        }
        return Response.accepted().build();
    }

    @Override
    public Response runReport(final String reportId, final Map<String, Object> inputParameters) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final ReportParameters parameters = parseParameters(inputParameters, ReportMode.IMMEDIATE);
            parameters.setReportId(reportId);
            reportWrapperService.runAndRender(parameters, ReportMode.IMMEDIATE, outputStream);
            if ((parameters.getFormat() == ReportFormat.PDF) || (parameters.getFormat() == ReportFormat.SVG)) {
                return Response.ok().type("application/pdf;charset=UTF-8")
                        .header("Content-disposition", "inline; filename=report.pdf")
                        .header("Pragma", "public")
                        .header("Cache-Control", "cache")
                        .header("Cache-Control", "must-revalidate")
                        .entity(outputStream.toByteArray()).build();
            }
            if (parameters.getFormat() == ReportFormat.CSV) {
                return Response.ok().type("text/csv;charset=UTF-8")
                        .header("Content-disposition", "inline; filename=report.csv")
                        .header("Cache-Control", "cache")
                        .header("Cache-Control", "must-revalidate")
                        .entity(outputStream.toByteArray()).build();
            }
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject("format", "Only PDF, SVG or CSV are supported"));
        } catch (SchedulerContextException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex.getContext(), ex.getRawMessage()));
        } catch (ReportException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex));
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public Response listPersistedReports(UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final List<ReportCatalogEntry> persistedReports = reportStoreService.getPage(queryParameters.getOffset(), queryParameters.getLimit());
        if (persistedReports.isEmpty()) {
            return Response.noContent().build();
        }
        final Map<String, Object> formatMap = reportStoreService.getFormatMap();
        final JSONArray jsonArray = new JSONArray();
        for (ReportCatalogEntry eachEntry : persistedReports) {
            final JSONObject jsonObject = new JSONObject(eachEntry);
            final List<ReportFormat> formats = (List<ReportFormat>)formatMap.get(eachEntry.getReportId());
            if (formats != null && !formats.isEmpty()) {
                jsonObject.put("formats", new JSONArray(formats));
            } else {
                // Special Reportd behaviour:
                // Reportd persists the reports as PDF/CSV and therefore formats is empty.
                // In that case the format is the format of the file

                final String format = eachEntry.getLocation().substring(eachEntry.getLocation().lastIndexOf(".") + 1);
                final JSONArray formatsArray = new JSONArray();
                formatsArray.put(format.toUpperCase());
                jsonObject.put("formats", formatsArray);
            }
            jsonArray.put(jsonObject);
        }
        return Response.ok()
                .header("Content-Range", ResponseUtils.getContentRange(jsonArray.length(), queryParameters.getOffset(), reportStoreService.countAll()))
                .entity(jsonArray.toString())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @Override
    public Response deletePersistedReports() {
        final Integer[] reportIdsToDelete = reportStoreService.getAll().stream().map(ReportCatalogEntry::getId).toArray(Integer[]::new);
        reportStoreService.delete(reportIdsToDelete);
        return Response.accepted().build();
    }

    @Override
    public Response deletePersistedReport(int id) {
        final Optional<ReportCatalogEntry> any = reportStoreService.getAll().stream().filter(r -> r.getId() != null && r.getId() == id).findAny();
        if (any.isPresent()) {
            reportStoreService.delete(any.get().getId());
            return Response.accepted().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response listScheduledReports(UriInfo uriInfo) {
        final List<TriggerDescription> triggerDescriptions = schedulerService.getTriggerDescriptions();
        if (triggerDescriptions.isEmpty()) {
            return Response.noContent().build();
        }
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final List<TriggerDescription> triggerDescriptionsForPage = queryParameters.getPage().apply(triggerDescriptions);

        final JSONArray scheduledReports = new JSONArray();
        for (TriggerDescription eachDescription : triggerDescriptionsForPage) {
            scheduledReports.put(new JSONObject(eachDescription));
        }
        return Response.ok()
                .header("Content-Range", ResponseUtils.getContentRange(scheduledReports.length(), queryParameters.getOffset(), triggerDescriptions.size()))
                .entity(scheduledReports.toString())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @Override
    public Response getSchedule(String triggerName) {
        final Optional<TriggerDescription> any = schedulerService.getTriggerDescriptions().stream()
                .filter(triggerDescription -> triggerDescription.getTriggerName().equals(triggerName))
                .findAny();
        if (any.isPresent()) {
            final TriggerDescription triggerDescription = any.get();
            final String reportId = triggerDescription.getReportId();
            final List<ReportFormat> formats = reportWrapperService.getFormats(reportId);
            final Collection<Category> categories = categoryConfigDao.findAll();
            final List<OnmsCategory> surveillanceCategories = categoryDao.findAll();
            final ReportParameters parameters = reportWrapperService.getParameters(reportId);
            final ReportParameters persistedParameters = triggerDescription.getReportParameters();
            parameters.apply(persistedParameters);

            final ReportDetails reportDetails = new ReportDetailsBuilder()
                    .withReportId(triggerDescription.getReportId())
                    .withFormats(formats)
                    .withParameters(parameters)
                    .withCategories(categories)
                    .withSurveillanceCategories(surveillanceCategories)
                    .withDeliveryOptions(triggerDescription.getDeliveryOptions())
                    .withCronExpression(triggerDescription.getCronExpression())
                    .withDefaultTimezones()
                    .build();

            return Response.ok(reportDetails.toJson().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response updateSchedule(final String triggerName, final Map<String, Object> parameters) {
        final Optional<TriggerDescription> any = schedulerService.getTriggerDescriptions().stream()
                .filter(triggerDescription -> triggerDescription.getTriggerName().equals(triggerName))
                .findAny();
        if (any.isPresent()) {
            final ReportParameters reportParameters = parseParameters(parameters, ReportMode.SCHEDULED);
            final DeliveryOptions deliveryOptions = parseDeliveryOptions(parameters);
            final ScheduleConfig scheduleConfig = new ScheduleConfig(reportParameters, deliveryOptions, (String) parameters.get("cronExpression"));
            try {
                schedulerService.updateCronTrigger(triggerName, scheduleConfig);
            } catch (SchedulerContextException ex) {
                return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex.getContext(), ex.getRawMessage()));
            } catch (SchedulerException ex) {
                return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex));
            }
            return Response.accepted().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    private static Response createErrorResponse(Status status, JSONObject errorObject) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(errorObject);
        return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(errorObject.toString()).build();
    }

    @Override
    public Response deleteScheduledReports() {
        final String[] triggersToDelete = schedulerService.getTriggerDescriptions().stream().map(TriggerDescription::getTriggerName).toArray(String[]::new);
        schedulerService.removeTriggers(triggersToDelete);
        return Response.accepted().build();
    }

    @Override
    public Response deleteScheduledReport(String triggerName) {
        if (schedulerService.exists(triggerName)) {
            schedulerService.removeTrigger(triggerName);
            return Response.accepted().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response downloadReport(final String format, final String locatorId) {
        if (Strings.isNullOrEmpty(locatorId)) {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(createErrorObject("entity", "Property 'locatorId' is null or empty").toString())
                    .build();
        }
        final Integer reportCatalogEntryId = WebSecurityUtils.safeParseInt(locatorId);
        final ReportCatalogEntry reportCatalogEntry = reportCatalogDao.get(reportCatalogEntryId);
        if (reportCatalogEntry == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        // Some reports are persisted as jrprint and must be reran to download/view
        boolean mustRender = reportCatalogEntry.getLocation().endsWith("jrprint") || reportCatalogEntry.getLocation().endsWith("xml");
        if (mustRender) { // the format should be set if we need to re-render the report
            if (Strings.isNullOrEmpty(format)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(createErrorObject("entity", "Property 'format' is null or empty").toString())
                        .build();
            }
        }
        try {
            final String suffix = reportCatalogEntry.getLocation().substring(reportCatalogEntry.getLocation().lastIndexOf(".") + 1).toLowerCase();
            final ReportFormat reportFormat = mustRender ? parseReportFormat(format) : parseReportFormat(suffix);
            final String filename = mustRender ? reportCatalogEntryId.toString() + "." + reportFormat.name().toLowerCase() : Paths.get(reportCatalogEntry.getLocation()).getFileName().toString();
            final StreamingOutput streamingOutput = mustRender
                    // Rerender
                    ?   outputStream -> {
                            reportStoreService.render(reportCatalogEntryId, reportFormat, outputStream);
                            outputStream.flush();
                        }
                    // Just download
                    :   outputStream -> {
                            try (FileInputStream input = new FileInputStream(new File(reportCatalogEntry.getLocation()))){
                                ByteStreams.copy(input, outputStream);
                            }
                            outputStream.flush();
                        };
            final Response.ResponseBuilder responseBuilder = Response.ok()
                    .header("Pragma", "public")
                    .header("Cache-Control", "cache")
                    .header("Cache-Control", "must-revalidate")
                    .entity(streamingOutput);
            if (ReportFormat.PDF == reportFormat || ReportFormat.SVG == reportFormat ) {
                return responseBuilder.type("application/pdf;charset=UTF-8")
                        .header("Content-disposition", "inline; filename=" + filename)
                        .build();
            }
            if (ReportFormat.HTML == reportFormat) {
                responseBuilder.type("text/html;charset=UTF-8")
                .header("Content-disposition", "inline; filename=" + filename);
            }
            if (ReportFormat.CSV == reportFormat) {
                responseBuilder.type("text/csv;charset=UTF-8")
                .header("Content-disposition", "inline; filename=" + filename);
            }
            return responseBuilder.build();
        } catch (NumberFormatException e) {
            return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(createErrorObject(e).toString()).build();
        } catch (SchedulerContextException ex) {
            return createErrorResponse(Status.BAD_REQUEST, createErrorObject(ex.getContext(), ex.getRawMessage()));
        }
    }

    private JSONObject createErrorObject(Exception exception) {
        return createErrorObject("entity", exception.getMessage());
    }

    private JSONObject createErrorObject(String context, String message) {
        final JSONObject errorObject = new JSONObject()
                .put("message", message)
                .put("context", context);
        return errorObject;
    }

    private ReportParameters parseParameters(final Map<String, Object> inputParameters, final ReportMode mode) {
        final String reportId = (String) inputParameters.get("id");
        final ReportParameters actualParameters = reportWrapperService.getParameters(reportId);
        final ReportFormat reportFormat = parseReportFormat((String) inputParameters.get("format"));
        actualParameters.setReportId(reportId);
        actualParameters.setFormat(reportFormat);

        // Determine the new values
        final JSONObject jsonInputParameters = new JSONObject(inputParameters);
        final ReportParameterBuilder reportParameterBuilder = new ReportParameterBuilder();
        final JSONArray jsonParameters = jsonInputParameters.getJSONArray("parameters");
        for (int i=0; i< jsonParameters.length(); i++) {
            final JSONObject jsonParameter = jsonParameters.getJSONObject(i);
            if (!jsonParameter.has("name") || !jsonParameter.has("type")) {
                continue;
            }
            final String parameterName = jsonParameter.getString("name");
            final String parameterType = jsonParameter.getString("type");
            final Object parameterValue = jsonParameter.has("value") ? jsonParameter.get("value") : null;
            if (parameterType.equals("string")) {
                if (!(parameterValue instanceof String)) {
                    throw new SchedulerContextException(parameterName, "Provided value ''{0}'' is not a string.", parameterValue);
                }
                reportParameterBuilder.withString(parameterName, jsonParameter.getString("value"));
            } else if (parameterType.equals("double")) {
                final Double doubleValue = parseDouble(parameterName, parameterValue);
                reportParameterBuilder.withDouble(parameterName, doubleValue);
            } else if (parameterType.equals("integer")) {
                final Integer integerValue = parseInteger(parameterName, parameterValue);
                reportParameterBuilder.withInteger(parameterName, integerValue);
            } else if (parameterType.equals("float")) {
                final Float floatValue = parseFloat(parameterName, parameterValue);
                reportParameterBuilder.withFloat(parameterName, floatValue);
            } else if (parameterType.equals("date")) {
                final int hours = jsonParameter.has("hours") ? parseInteger(parameterName + "Hours", jsonParameter.get("hours")) : 0;
                final int minutes = jsonParameter.has("minutes") ? parseInteger(parameterName + "Minutes", jsonParameter.get("minutes")) : 0;
                final ReportDateParm actualDateParm = actualParameters.getParameter(parameterName);
                if (actualDateParm.getUseAbsoluteDate() == true || mode == ReportMode.IMMEDIATE) {
                    if (jsonParameter.has("date")) {
                        final String dateString = jsonParameter.getString("date");
                        try {
                            final Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                            reportParameterBuilder.withDate(parameterName, parsedDate, hours, minutes);
                        } catch (ParseException e) {
                            throw new SchedulerContextException(parameterName, "The provided value ''{0}'' cannot be parsed as a date. Expected format is yyyy-MM-dd", dateString);
                        }
                    }
                } else {
                    final String interval = jsonParameter.getString("interval");
                    final int count = jsonParameter.getInt("count");
                    reportParameterBuilder.withDate(parameterName, interval, count, hours, minutes);
                }
            } else if(parameterType.equals("timezone")) {
                final ZoneId zoneId = parseTimezone(parameterName, jsonParameter.has("value") ? jsonParameter.getString("value") : "");
                reportParameterBuilder.withTimezone(parameterName, zoneId);
            } else {
                throw new SchedulerContextException(parameterName, "Unknown type ''{0}''. Supported types are: ''{1}''",
                        parameterType, Lists.newArrayList("string", "integer", "float", "double", "date"));
            }
        }

        // Finally apply the new values
        final ReportParameters mergeWithParameters = reportParameterBuilder.build();
        actualParameters.apply(mergeWithParameters);

        return actualParameters;
    }

    private DeliveryOptions parseDeliveryOptions(Map<String, Object> parameters) {
        final DeliveryOptions options = new DeliveryOptions();
        final JSONObject jsonParameters = new JSONObject(parameters);
        final JSONObject jsonOptions = jsonParameters.getJSONObject("deliveryOptions");
        options.setInstanceId(jsonOptions.getString("instanceId"));
        options.setSendMail(jsonOptions.getBoolean("sendMail"));
        if (options.isSendMail() && jsonOptions.has("mailTo")) {
            options.setMailTo(jsonOptions.getString("mailTo"));
        }
        if (jsonOptions.has("webhook")) {
            options.setWebhook(jsonOptions.getBoolean("webhook"));
        }
        if (options.isWebhook() && jsonOptions.has("webhookUrl")) {
            options.setWebhookUrl(jsonOptions.getString("webhookUrl"));
        }
        options.setPersist(jsonOptions.getBoolean("persist"));
        options.setFormat(parseReportFormat(jsonOptions.getString("format")));
        return options;
    }

    private ReportFormat parseReportFormat(String input) {
        if (Strings.isNullOrEmpty(input)) {
            throw new SchedulerContextException("format", "Please provide a value");
        }
        for (ReportFormat eachFormat : ReportFormat.values()) {
            if (eachFormat.name().equalsIgnoreCase(input)) {
                return eachFormat;
            }
        }
        throw new SchedulerContextException("format", "Provided format ''{0}'' is not supported", input);
    }

    private static Double parseDouble(String name, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException ex) {
                throw new SchedulerContextException(name, "Provided value ''{0}'' is not a floating number", value);
            }
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new SchedulerContextException(name, "Provided value ''{0}'' must be of type string or double");
    }

    private static Integer parseInteger(String name, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException ex) {
                throw new SchedulerContextException(name, "Provided value ''{0}'' is not an integer number", value);
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new SchedulerContextException(name, "Provided value ''{0}'' must be of type string or integer");
    }

    private static Float parseFloat(String name, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            try {
                return Float.valueOf((String) value);
            } catch (NumberFormatException ex) {
                throw new SchedulerContextException(name, "Provided value ''{0}'' is not a floating number", value);
            }
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new SchedulerContextException(name, "Provided value ''{0}'' must be of type string or float");
    }

    private static ZoneId parseTimezone(String name, String timezoneValue) {
        try {
            return ZoneId.of(timezoneValue);
        } catch (DateTimeException e) {
            throw new SchedulerContextException(name, "Provided timezone ''{0}'' could not be parsed: ''{1}''.", timezoneValue, e.getMessage());
        }
    }

}
