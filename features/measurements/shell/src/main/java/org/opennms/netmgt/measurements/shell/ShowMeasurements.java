/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.shell;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.RrdGraphAttribute;

@Command(scope = "opennms-measurements", name = "show-measurements", description = "Measurements")
@Service
public class ShowMeasurements implements Action {

    @Reference
    ResourceDao resourceDao;

    @Reference
    MeasurementsService measurementsService;

    @Option(name = "-a", aliases = "--attribute", description = "Attribute", multiValued = true)
    List<String> attributesSelected;

    @Option(name = "-i", aliases = "--interval", description = "Requested step interval (ms)")
    long stepMs = TimeUnit.MINUTES.toMillis(5);

    @Option(name = "-t", aliases = "--raw-timestamps", description = "Display timestamps as milliseconds from epoch")
    boolean rawTimestamps = false;

    @Option(name = "-s", aliases = "--start", description = "Start Timestamp (epoch ms)")
    long start = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);

    @Option(name = "-e", aliases = "--end", description = "End Timestamp (epoch ms)")
    long end = System.currentTimeMillis();

    @Argument(index = 0, name = "resourceId", description = "Resource ID", required = true)
    String resourceId;

    @Override
    public Object execute() throws MeasurementException {
        final ResourceId resourceIdd = ResourceId.fromString(resourceId);
        final OnmsResource resource = resourceDao.getResourceById(resourceIdd);
        if (resource == null) {
            System.out.printf("No resource with ID '%s' found.\n", resourceId);
            return null;
        }
        // Jim, we found the resource.

        // What attributes i.e. ifHCInOctets are associated with this resource?
        final Map<String, RrdGraphAttribute> attrsByName = resource.getRrdGraphAttributes();
        System.out.printf("Resource with ID '%s' has attributes: %s\n", resourceId, attrsByName.keySet());

        // Filter the attribute name if 1+ were passed in as options
        Set<String> filteredAttrNames = attrsByName.keySet();
        if (attributesSelected != null && !attributesSelected.isEmpty()) {
            filteredAttrNames = attrsByName.keySet().stream()
                    .filter(a -> attributesSelected.contains(a))
                    .collect(Collectors.toSet());
            System.out.printf("Limiting attributes to: %s\n", filteredAttrNames);
        }

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setStart(start);
        queryRequest.setEnd(end);
        queryRequest.setStep(stepMs);
        for (String attrName : filteredAttrNames) {
            Source source = new Source();
            source.setAttribute(attrName);
            source.setLabel(attrName);
            source.setAggregation("AVERAGE");
            source.setResourceId(resourceId);
            queryRequest.getSources().add(source);
        }
        QueryResponse queryResponse = measurementsService.query(queryRequest);

        System.out.println();
        printResponseAsTable(queryResponse);
        return null;
    }

    private void printResponseAsTable(QueryResponse queryResponse) {
        final long[] timestamps = queryResponse.getTimestamps();
        final String[] labels = queryResponse.getLabels();
        final QueryResponse.WrappedPrimitive[] columns = queryResponse.getColumns();


        System.out.print("timestamp");
        for (String label : labels) {
            System.out.printf(",%s", label);
        }
        System.out.println();
        for (int i = 0; i < timestamps.length; i++) {
            if (rawTimestamps) {
                System.out.printf("%d", timestamps[i]);
            } else {
                System.out.printf("%s", new Date(timestamps[i]));
            }
            for (int k = 0; k < labels.length; k++) {
                System.out.printf(",%.4f", columns[k].getList()[i]);
            }
            System.out.println();
        }
    }
}
