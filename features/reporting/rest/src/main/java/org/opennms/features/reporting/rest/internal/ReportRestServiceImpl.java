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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import org.opennms.features.reporting.rest.ReportRestService;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.opennms.web.svclayer.model.DatabaseReportDescription;
import org.opennms.web.svclayer.model.ReportRepositoryDescription;

public class ReportRestServiceImpl implements ReportRestService {

    private final DatabaseReportListService databaseReportListService;
    private final ReportWrapperService reportWrapperService;
    private final CategoryDao categoryDao;
    private final CategoryConfigDao categoryConfigDao;

    public ReportRestServiceImpl(DatabaseReportListService databaseReportListService, ReportWrapperService reportWrapperService, CategoryDao categoryDao, CategoryConfigDao categoryConfigDao) {
        this.databaseReportListService = Objects.requireNonNull(databaseReportListService);
        this.reportWrapperService = Objects.requireNonNull(reportWrapperService);
        this.categoryDao = Objects.requireNonNull(categoryDao);
        this.categoryConfigDao = Objects.requireNonNull(categoryConfigDao);
    }

    @Override
    public Response listReports() {
        final List<ReportRepositoryDescription> activeRepositories = databaseReportListService.getActiveRepositories();
        final List<DatabaseReportDescription> collect = activeRepositories.stream()
                .flatMap(repositoryDescriptor -> databaseReportListService.getReportsByRepositoryId(repositoryDescriptor.getId()).stream())
                .collect(Collectors.toList());
        final JSONArray jsonArray = new JSONArray();
        for (DatabaseReportDescription description : collect) {
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
    public Response getReportDetails(String reportId) {
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
        for (ReportDateParm dateParm : parameters.getDateParms()) {
            final JSONObject jsonDateParm = new JSONObject();
            jsonDateParm.put("type", "date");
            jsonDateParm.put("name", dateParm.getName());
            jsonDateParm.put("displayName", dateParm.getDisplayName());
            jsonDateParm.put("count", dateParm.getCount());
            jsonDateParm.put("date", new SimpleDateFormat("yyyy-MM-dd").format(dateParm.getDate()));
            jsonDateParm.put("value", dateParm.getValue(ReportMode.IMMEDIATE));
            jsonDateParm.put("hours", dateParm.getHours());
            jsonDateParm.put("interval", dateParm.getInterval());
            jsonDateParm.put("minutes", dateParm.getMinutes());
            jsonDateParm.put("useAbsoluteDate", dateParm.getUseAbsoluteDate());
            jsonParameters.put(jsonDateParm);
        }
        for (ReportDoubleParm doubleParm : parameters.getDoubleParms()) {
            final JSONObject jsonDoubleParm = new JSONObject();
            jsonDoubleParm.put("type", "double");
            jsonDoubleParm.put("name", doubleParm.getName());
            jsonDoubleParm.put("displayName", doubleParm.getDisplayName());
            jsonDoubleParm.put("value", doubleParm.getValue());
            jsonDoubleParm.put("inputType", doubleParm.getInputType());
            jsonParameters.put(jsonDoubleParm);
        }
        for (ReportFloatParm floatParm : parameters.getFloatParms()) {
            final JSONObject jsonFloatParm = new JSONObject();
            jsonFloatParm.put("type", "float");
            jsonFloatParm.put("name", floatParm.getName());
            jsonFloatParm.put("displayName", floatParm.getDisplayName());
            jsonFloatParm.put("value", floatParm.getValue());
            jsonFloatParm.put("inputType", floatParm.getInputType());
            jsonParameters.put(jsonFloatParm);
        }
        for (ReportIntParm intParm : parameters.getIntParms()) {
            final JSONObject jsonIntParm = new JSONObject();
            jsonIntParm.put("type", "integer");
            jsonIntParm.put("name", intParm.getName());
            jsonIntParm.put("displayName", intParm.getDisplayName());
            jsonIntParm.put("value", intParm.getValue());
            jsonIntParm.put("inputType", intParm.getInputType());
            jsonParameters.put(jsonIntParm);
        }
        for (ReportStringParm stringParm : parameters.getStringParms()) {
            final JSONObject jsonStringParm = new JSONObject();
            jsonStringParm.put("type", "string");
            jsonStringParm.put("name", stringParm.getName());
            jsonStringParm.put("displayName", stringParm.getDisplayName());
            jsonStringParm.put("value", stringParm.getValue());
            jsonStringParm.put("inputType", stringParm.getInputType());
            jsonParameters.put(jsonStringParm);
        }

        // Convert categories
        final JSONArray jsonCategories = new JSONArray();
        for (Category eachCategory : categories) {
            jsonCategories.put(eachCategory.getLabel());
        }

        // Convert surveillanceCategories
        final JSONArray jsonSurveillanceCategories = new JSONArray();
        for (OnmsCategory eachCategory : surveillanceCategories) {
            jsonSurveillanceCategories.put(eachCategory.getName());
        }

        // Create return object
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", reportId);
        jsonObject.put("name", parameters.getDisplayName());
        jsonObject.put("parameters", jsonParameters);
        jsonObject.put("formats", jsonFormats);
        jsonObject.put("categories", jsonCategories);
        jsonObject.put("surveillanceCategories", jsonSurveillanceCategories);

        return Response.ok(jsonObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response runReport(final String reportId, final Map<String, Object> inputParameters) {
        final JSONObject jsonParameters = new JSONObject(inputParameters);
        final ReportParameters parameters = new ReportParameters();
        parameters.setReportId(reportId);
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
            try {
                parm.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("date")));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            parm.setHours(jsonObject.getInt("hours"));
            parm.setInterval(jsonObject.getString("interval"));
            parm.setMinutes(jsonObject.getInt("minutes"));
            parm.setUseAbsoluteDate(jsonObject.getBoolean("useAbsoluteDate"));
            return parm;
        }));

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
}
