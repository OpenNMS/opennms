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

import java.io.File;
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
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;

import com.google.common.base.Optional;

@Command(scope = "opennms-measurements", name = "show-newts-samples", description = "Show the raw sample stored in Newts")
@Service
public class ShowNewtsSamples implements Action {

    @Reference(optional = true)
    SampleRepository sampleRepository;

    @Reference
    ResourceDao resourceDao;

    @Option(name = "-a", aliases = "--attribute", description = "Attribute", multiValued = true)
    List<String> attributesSelected;

    @Option(name = "-t", aliases = "--raw-timestamps", description = "Display timestamps as milliseconds from epoch")
    boolean rawTimestamps = false;

    @Option(name = "-s", aliases = "--start", description = "Start Timestamp (epoch ms)")
    long start = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);

    @Option(name = "-e", aliases = "--end", description = "End Timestamp (epoch ms)")
    long end = System.currentTimeMillis();

    @Argument(index = 0, name = "resourceId", description = "Resource ID", required = true)
    String resourceId;

    @Override
    public Object execute() {
        if (sampleRepository == null) {
            System.out.println("Newts must be enabled to use this command.");
            return null;
        }

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

        // The attributes for these resources may be spread across many Newts resource
        // Gather this list, so we may process each of these individually
        final Set<String> newtsResourceIds = filteredAttrNames.stream()
                .map(attrsByName::get)
                .map(attr -> {
                    // The Newts Resource ID is stored in the rrdFile attribute
                    String newtsResourceId = attr.getRrdRelativePath();
                    // Remove the file separator prefix, added by the RrdGraphAttribute class
                    if (newtsResourceId.startsWith(File.separator)) {
                        newtsResourceId = newtsResourceId.substring(File.separator.length());
                    }
                    return newtsResourceId;
                })
                .collect(Collectors.toSet());

        for (String newtsResourceId : newtsResourceIds) {
            System.out.printf("Fetching samples for Newts resource ID '%s'...\n", newtsResourceId);
            final Results<Sample> samples = sampleRepository.select(Context.DEFAULT_CONTEXT,
                    new Resource(newtsResourceId),
                    Optional.of(Timestamp.fromEpochMillis(start)),
                    Optional.of(Timestamp.fromEpochMillis(end)));

            // Render the samples
            for (Results.Row<Sample> sampleRow : samples.getRows()) {
                for (Sample sample : sampleRow.getElements()) {
                    if (!filteredAttrNames.contains(sample.getName())) {
                        continue;
                    }
                    final Date date = sample.getTimestamp().asDate();
                    System.out.printf("%s,%s,%.4f\n", rawTimestamps ? date.getTime() : date, sample.getName(), sample.getValue().doubleValue());
                }
            }
        }

        return null;
    }
}
