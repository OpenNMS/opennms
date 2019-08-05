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

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.features.reporting.rest.ReportRestService;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.SchedulerMessage;
import org.opennms.web.svclayer.SchedulerMessageSeverity;
import org.opennms.web.svclayer.SchedulerRequestContext;
import org.opennms.web.svclayer.SchedulerService;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.opennms.web.svclayer.model.DatabaseReportDescription;
import org.opennms.web.svclayer.model.ReportRepositoryDescription;
import org.opennms.web.svclayer.model.TriggerDescription;

import com.google.common.base.Strings;

public class ReportRestServiceImpl implements ReportRestService {

    private final DatabaseReportListService databaseReportListService;
    private final ReportWrapperService reportWrapperService;
    private final CategoryDao categoryDao;
    private final CategoryConfigDao categoryConfigDao;
    private final ReportStoreService reportStoreService;
    private final SchedulerService schedulerService;

    public ReportRestServiceImpl(DatabaseReportListService databaseReportListService, ReportWrapperService reportWrapperService, CategoryDao categoryDao, CategoryConfigDao categoryConfigDao, ReportStoreService reportStoreService, SchedulerService schedulerService) {
        this.databaseReportListService = Objects.requireNonNull(databaseReportListService);
        this.reportWrapperService = Objects.requireNonNull(reportWrapperService);
        this.categoryDao = Objects.requireNonNull(categoryDao);
        this.categoryConfigDao = Objects.requireNonNull(categoryConfigDao);
        this.reportStoreService = Objects.requireNonNull(reportStoreService);
        this.schedulerService = Objects.requireNonNull(schedulerService);
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
            json.put("id", description.getId());
            json.put("name", description.getDisplayName());
            json.put("description", description.getDescription());
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
                jsonDateParm.put("date", new SimpleDateFormat("yyyy-MM-dd").format(dateParm.getDate()));
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

        // Create return object
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", reportId);
        jsonObject.put("name", parameters.getDisplayName());
        jsonObject.put("parameters", jsonParameters);
        jsonObject.put("formats", jsonFormats);
        jsonObject.put("categories", jsonCategories);
        jsonObject.put("surveillanceCategories", jsonSurveillanceCategories);

        // Apply delivery Options if user Id is provided
        if (userId != null) {
            final DeliveryOptions deliveryOptions = reportWrapperService.getDeliveryOptions(reportId, userId);
            jsonObject.put("deliveryOptions", new JSONObject(deliveryOptions));
        }

        return Response.ok(jsonObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response scheduleReport(final Map<String, Object> parameters) {
        final ReportParameters reportParameters = parseParameters(parameters);
        final DeliveryOptions deliveryOptions = parseDeliveryOptions(parameters);
        final SchedulerRequestContext requestContext = new DummyRequestContext();

        schedulerService.addCronTrigger(reportParameters.getReportId(), reportParameters, deliveryOptions, (String) parameters.get("cronExpression"), requestContext);
        final SchedulerMessage errorMessage = extractErrorMessage(requestContext);
        if (errorMessage != null) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(convert(errorMessage).toString()).build();
        }

        return Response.accepted().build();
    }

    @Override
    public Response deliverReport(final Map<String, Object> parameters) {
        final ReportParameters reportParameters = parseParameters(parameters);
        final DeliveryOptions deliveryOptions = parseDeliveryOptions(parameters);
        final SchedulerRequestContext requestContext = new DummyRequestContext();
        schedulerService.execute(reportParameters.getReportId(), reportParameters, deliveryOptions, requestContext);
        final SchedulerMessage errorMessage = extractErrorMessage(requestContext);
        if (errorMessage != null) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(convert(errorMessage).toString()).build();
        }
        return Response.accepted().build();
    }

    @Override
    public Response runReport(final String reportId, final Map<String, Object> inputParameters) {
        final ReportParameters parameters = parseParameters(inputParameters);
        parameters.setReportId(reportId);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            reportWrapperService.runAndRender(parameters, ReportMode.IMMEDIATE, outputStream);
            if ((parameters.getFormat() == ReportFormat.PDF) || (parameters.getFormat() == ReportFormat.SVG) ) {
                return Response.ok().type("application/pdf;charset=UTF-8")
                    .header("Content-disposition", "inline; filename=report.pdf")
                    .header("Pragma", "public")
                    .header("Cache-Control", "cache")
                    .header("Cache-Control", "must-revalidate")
                    .entity(outputStream.toByteArray()).build();
            }
            if(parameters.getFormat() == ReportFormat.CSV) {
                return Response.ok().type("text/csv;charset=UTF-8")
                    .header("Content-disposition", "inline; filename=report.csv")
                    .header("Cache-Control", "cache")
                    .header("Cache-Control", "must-revalidate")
                    .entity(outputStream.toByteArray()).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build(); // TODO MVR unsupported format
        } catch (ReportException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public Response listPersistedReports() {
        final List<ReportCatalogEntry> persistedReports = reportStoreService.getAll();
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
            }
            jsonArray.put(jsonObject);
        }
        return Response.ok().entity(jsonArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
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
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response listScheduledReports() {
        final List<TriggerDescription> triggerDescriptions = schedulerService.getTriggerDescriptions();
        if (triggerDescriptions.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray scheduledReports = new JSONArray();
        for (TriggerDescription eachDescription : triggerDescriptions) {
            scheduledReports.put(new JSONObject(eachDescription));
        }
        return Response.ok().entity(scheduledReports.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
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
                    jsonDateParm.put("date", new SimpleDateFormat("yyyy-MM-dd").format(dateParm.getDate()));
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

            // Create return object
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", reportId);
            jsonObject.put("name", parameters.getDisplayName());
            jsonObject.put("parameters", jsonParameters);
            jsonObject.put("formats", jsonFormats);
            jsonObject.put("categories", jsonCategories);
            jsonObject.put("surveillanceCategories", jsonSurveillanceCategories);

            final DeliveryOptions deliveryOptions = triggerDescription.getDeliveryOptions();
            jsonObject.put("deliveryOptions", new JSONObject(deliveryOptions));
            jsonObject.put("cronExpression", triggerDescription.getCronExpression());
            return Response.ok(jsonObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response updateSchedule(final String triggerName, final Map<String, Object> parameters) {
        final Optional<TriggerDescription> any = schedulerService.getTriggerDescriptions().stream()
                .filter(triggerDescription -> triggerDescription.getTriggerName().equals(triggerName))
                .findAny();
        if (any.isPresent()) {
            schedulerService.removeTrigger(triggerName);
            return scheduleReport(parameters);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
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
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response downloadReport(final String format, final String locatorId) {
        if (Strings.isNullOrEmpty(format)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(createErrorObject("entity", "Property 'format' is null or empty").toString())
                    .build();
        }
        if (Strings.isNullOrEmpty(locatorId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(createErrorObject("entity", "Property 'locatorId' is null or empty").toString())
                    .build();
        }
        try {
            final Integer reportCatalogEntryId = Integer.valueOf(WebSecurityUtils.safeParseInt(locatorId));
            final StreamingOutput streamingOutput = outputStream -> {
                reportStoreService.render(reportCatalogEntryId, ReportFormat.valueOf(format), outputStream);
                outputStream.flush();
            };
            final Response.ResponseBuilder responseBuilder = Response.ok()
                    .header("Pragma", "public")
                    .header("Cache-Control", "cache")
                    .header("Cache-Control", "must-revalidate")
                    .entity(streamingOutput);
            if ((ReportFormat.PDF == ReportFormat.valueOf(format)) || (ReportFormat.SVG == ReportFormat.valueOf(format)) ) {
                return responseBuilder.type("application/pdf;charset=UTF-8")
                        .header("Content-disposition", "inline; filename=" + reportCatalogEntryId.toString() + ".pdf")
                        .build();
            }
            if (ReportFormat.CSV == ReportFormat.valueOf(format)) {
                responseBuilder.type("text/csv;charset=UTF-8")
                .header("Content-disposition", "inline; filename=" + reportCatalogEntryId.toString() + ".csv");
            }
            return responseBuilder.build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(createErrorObject(e).toString()).build();
        }
    }

    // TODO MVR this is duplicated all over the place
    private JSONObject createErrorObject(Exception exception) {
        return createErrorObject("entity", exception.getMessage());
    }

    // TODO MVR this is duplicated all over the place
    private JSONObject createErrorObject(String message, String context) {
        final JSONObject errorObject = new JSONObject()
                .put("message", message)
                .put("context", context);
        return errorObject;
    }

    private static <T> List<T> parseParameters(JSONArray inputParameters, String type, Function<JSONObject, T> converter) {
        Objects.requireNonNull(inputParameters);
        Objects.requireNonNull(type);
        Objects.requireNonNull(converter);

        final List<T> parsedParameters = new ArrayList<>();
        for (int i=0; i<inputParameters.length(); i++) {
            final JSONObject eachInput = inputParameters.getJSONObject(i);
            if (eachInput.getString("type").equalsIgnoreCase(type)) {
                final T eachConvertedParameter = converter.apply(eachInput);
                if (eachConvertedParameter != null) {
                    parsedParameters.add(eachConvertedParameter);
                }
            }
        }
        return parsedParameters;
    }

    private JSONObject convert(SchedulerMessage message) {
        final JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", message.getSeverity().name());
        errorMessage.put("message", message.getText());
        return errorMessage;
    }

    private ReportParameters parseParameters(Map<String, Object> inputParameters) {
        final JSONObject jsonParameters = new JSONObject(inputParameters);
        final ReportParameters parameters = new ReportParameters();
        parameters.setReportId((String) inputParameters.get("id"));
        parameters.setFormat(ReportFormat.valueOf(jsonParameters.getString("format")));
        parameters.setStringParms(parseParameters(jsonParameters.getJSONArray("parameters"), "string", jsonObject -> {
            // TODO MVR this is not ideal, as we override name and such as well, should only apply the values
            final ReportStringParm parm = new ReportStringParm();
            if (jsonObject.has("inputType")) {
                parm.setInputType(jsonObject.getString("inputType"));
            }
            parm.setName(jsonObject.getString("name"));
            parm.setDisplayName(jsonObject.getString("displayName"));
            parm.setValue(jsonObject.getString("value"));
            return parm;
        }));
        parameters.setDoubleParms(parseParameters(jsonParameters.getJSONArray("parameters"), "double", jsonObject -> {
            // TODO MVR this is not ideal, as we override name and such as well, should only apply the values
            final ReportDoubleParm parm = new ReportDoubleParm();
            if (jsonObject.has("inputType")) {
                parm.setInputType(jsonObject.getString("inputType"));
            }
            parm.setName(jsonObject.getString("name"));
            parm.setDisplayName(jsonObject.getString("displayName"));
            parm.setValue(jsonObject.getDouble("value"));
            return parm;
        }));
        parameters.setIntParms(parseParameters(jsonParameters.getJSONArray("parameters"), "integer", jsonObject -> {
            // TODO MVR this is not ideal, as we override name and such as well, should only apply the values
            final ReportIntParm parm = new ReportIntParm();
            if (jsonObject.has("inputType")) {
                parm.setInputType(jsonObject.getString("inputType"));
            }
            parm.setName(jsonObject.getString("name"));
            parm.setDisplayName(jsonObject.getString("displayName"));
            parm.setValue(jsonObject.getInt("value"));
            return parm;
        }));
        parameters.setFloatParms(parseParameters(jsonParameters.getJSONArray("parameters"), "float", jsonObject -> {
            // TODO MVR this is not ideal, as we override name and such as well, should only apply the values
            final ReportFloatParm parm = new ReportFloatParm();
            if (jsonObject.has("inputType")) {
                parm.setInputType(jsonObject.getString("inputType"));
            }
            parm.setName(jsonObject.getString("name"));
            parm.setDisplayName(jsonObject.getString("displayName"));
            parm.setValue(jsonObject.getFloat("value"));
            return parm;
        }));
        parameters.setDateParms(parseParameters(jsonParameters.getJSONArray("parameters"), "date", jsonObject -> {
            // TODO MVR this is not ideal, as we override name and such as well, should only apply the values
            final ReportDateParm parm = new ReportDateParm();
            parm.setName(jsonObject.getString("name"));
            parm.setDisplayName(jsonObject.getString("displayName"));
            parm.setCount(jsonObject.getInt("count"));
            parm.setInterval(jsonObject.getString("interval"));
            if (jsonObject.has("date")) {
                try {
                    final String dateString = jsonObject.getString("date");
                    final Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                    parm.setDate(parsedDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            parm.setHours(jsonObject.getInt("hours"));
            parm.setMinutes(jsonObject.getInt("minutes"));
            parm.setUseAbsoluteDate(jsonObject.getBoolean("useAbsoluteDate")); // TODO MVR this is already known and should not be overriden
            return parm;
        }));
        return parameters;
    }

    private DeliveryOptions parseDeliveryOptions(Map<String, Object> parameters) {
        final DeliveryOptions options = new DeliveryOptions();
        final JSONObject jsonParameters = new JSONObject(parameters);
        final JSONObject jsonOptions = jsonParameters.getJSONObject("deliveryOptions");
        options.setInstanceId(jsonOptions.getString("instanceId"));
        options.setSendMail(jsonOptions.getBoolean("sendMail"));
        if (options.getSendMail()) {
            options.setMailTo(jsonOptions.getString("mailTo"));
        }
        options.setPersist(jsonOptions.getBoolean("persist"));
        options.setFormat(ReportFormat.valueOf(jsonOptions.getString("format")));
        return options;
    }

    private SchedulerMessage extractErrorMessage(SchedulerRequestContext requestContext) {
        return requestContext.getAllMessages().stream()
                .filter(message -> message.getSeverity() == SchedulerMessageSeverity.ERROR || message.getSeverity() == SchedulerMessageSeverity.FATAL)
                .findFirst().orElse(null);
    }
}
