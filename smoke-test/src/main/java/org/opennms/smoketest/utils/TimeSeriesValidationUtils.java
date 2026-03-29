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
package org.opennms.smoketest.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy-agnostic validation methods for the OpenNMS time series storage pipeline.
 * These assertions work regardless of the underlying TSS backend (RRD, Newts, Integration/Cortex).
 *
 * <p>Usage: call these methods from any smoke test that validates the data pipeline.</p>
 */
public final class TimeSeriesValidationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesValidationUtils.class);

    private TimeSeriesValidationUtils() {}

    /**
     * Validates that the resource tree for a node is populated with child resources
     * that have graph-ready attributes.
     *
     * @param client the REST client
     * @param nodeCriteria the node criteria (e.g. "selfmonitor:1")
     */
    public static void validateResourceTree(RestClient client, String nodeCriteria) {
        ResourceDTO resources = client.getResourcesForNode(nodeCriteria);
        assertThat("Resource tree should not be null", resources, notNullValue());

        List<ResourceDTO> children = resources.getChildren().getObjects();
        assertThat("Node should have child resources", children.size(), greaterThan(0));

        long attrCount = children.stream()
                .mapToLong(c -> c.getRrdGraphAttributes().size())
                .sum();
        assertThat("Child resources should have graph attributes", attrCount, greaterThan(0L));

        LOG.info("Resource tree validated: {} children, {} total graph attributes", children.size(), attrCount);
    }

    /**
     * Validates that the measurements API returns data for a given resource and attribute,
     * using the specified aggregation.
     *
     * @param client the REST client
     * @param resourceId the resource ID (e.g. "node[selfmonitor:1].nodeSnmp[]")
     * @param attribute the attribute name (e.g. "OnmsEventCount")
     * @param aggregation the aggregation function ("AVERAGE", "MAX", "MIN")
     * @return the query response for further inspection
     */
    public static QueryResponse validateMeasurements(RestClient client, String resourceId,
                                                      String attribute, String aggregation) {
        long now = System.currentTimeMillis();
        long tenMinAgo = now - (10 * 60 * 1000);

        Source source = new Source();
        source.setLabel("test");
        source.setResourceId(resourceId);
        source.setAttribute(attribute);
        source.setAggregation(aggregation);

        QueryRequest request = new QueryRequest();
        request.setStart(tenMinAgo);
        request.setEnd(now);
        request.setStep(300000L); // 5 minutes
        request.setRelaxed(true);
        request.setSources(Arrays.asList(source));

        QueryResponse response = client.getMeasurements(request);
        assertThat("Measurements response should not be null", response, notNullValue());
        assertThat("Response should have timestamps", response.getTimestamps().length, greaterThan(0));
        assertThat("Response should have columns", response.getColumns().length, greaterThan(0));

        double[] values = response.getColumns()[0].getList();
        long nonNullCount = Arrays.stream(values).filter(v -> !Double.isNaN(v)).count();
        assertThat("Should have non-NaN values with " + aggregation + " aggregation",
                nonNullCount, greaterThan(0L));

        LOG.info("Measurements validated: resourceId={}, attribute={}, aggregation={}, "
                + "timestamps={}, nonNullValues={}",
                resourceId, attribute, aggregation, response.getTimestamps().length, nonNullCount);

        return response;
    }

    /**
     * Validates that measurements metadata includes resource and node information.
     *
     * @param client the REST client
     * @param resourceId the resource ID
     * @param attribute the attribute name
     */
    public static void validateMeasurementsMetadata(RestClient client, String resourceId, String attribute) {
        long now = System.currentTimeMillis();

        Source source = new Source();
        source.setLabel("test");
        source.setResourceId(resourceId);
        source.setAttribute(attribute);
        source.setAggregation("AVERAGE");

        QueryRequest request = new QueryRequest();
        request.setStart(now - 300000L);
        request.setEnd(now);
        request.setStep(60000L);
        request.setRelaxed(true);
        request.setSources(Arrays.asList(source));

        QueryResponse response = client.getMeasurements(request);
        assertThat("Response should not be null", response, notNullValue());
        assertThat("Response should have metadata", response.getMetadata(), notNullValue());
        assertFalse("Metadata should have resources",
                response.getMetadata().getResources().isEmpty());
        assertFalse("Metadata should have nodes",
                response.getMetadata().getNodes().isEmpty());

        LOG.info("Measurements metadata validated for resourceId={}", resourceId);
    }

    /**
     * Validates all three supported aggregation functions (AVERAGE, MAX, MIN).
     *
     * @param client the REST client
     * @param resourceId the resource ID
     * @param attribute the attribute name
     */
    public static void validateAllAggregations(RestClient client, String resourceId, String attribute) {
        for (String agg : new String[]{"AVERAGE", "MAX", "MIN"}) {
            validateMeasurements(client, resourceId, attribute, agg);
        }
    }
}
