/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jrobin.core.RrdException;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.measurements.utils.Utils;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Used to fetch measurements from RRD files.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public abstract class AbstractRrdBasedFetchStrategy implements MeasurementFetchStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRrdBasedFetchStrategy.class);

    @Autowired
    private ResourceDao m_resourceDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public FetchResults fetch(long start, long end, long step, int maxrows,
                              Long interval, Long heartbeat, List<Source> sources, boolean relaxed) throws Exception {

        final Map<String, Object> constants = Maps.newHashMap();

        final Map<Source, String> rrdsBySource = Maps.newHashMap();
        
        for (final Source source : sources) {
            final ResourceId resourceId;
            try {
                resourceId = ResourceId.fromString(source.getResourceId());
            } catch (final IllegalArgumentException ex) {
                if (relaxed) continue;
                LOG.error("Ill-formed resource id: {}", source.getResourceId(), ex);
                return null;
            }

            // Grab the resource
            final OnmsResource resource = m_resourceDao.getResourceById(resourceId);
            if (resource == null) {
                if (relaxed) continue;
                LOG.error("No resource with id: {}", source.getResourceId());
                return null;
            }

            // Grab the attribute
            RrdGraphAttribute rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());

            if (rrdGraphAttribute == null && !Strings.isNullOrEmpty(source.getFallbackAttribute())) {
                LOG.error("No attribute with name '{}', using fallback-attribute with name '{}'", source.getAttribute(), source.getFallbackAttribute());
                source.setAttribute(source.getFallbackAttribute());
                source.setFallbackAttribute(null);
                rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());
            }

            if (rrdGraphAttribute == null) {
                if (relaxed) continue;
                LOG.error("No attribute with name: {}", source.getAttribute());
                return null;
            }

            // Gather the values from strings.properties
            Utils.convertStringAttributesToConstants(source.getLabel(), resource.getStringPropertyAttributes(), constants);

            // Build the path to the archive
            final String rrdFile = System.getProperty("rrd.base.dir")
                    + File.separator + rrdGraphAttribute.getRrdRelativePath();

            rrdsBySource.put(source, rrdFile);
        }

        // Fetch
        return fetchMeasurements(start, end, step, maxrows, rrdsBySource, constants, sources, relaxed);
    }

    /**
     *  Performs the actual retrieval of the values from the RRD/JRB files.
     *
     *  If relaxed is <code>true</code> an empty response will be generated if there
     *  are no RRD/JRB files to query.
     *
     *  If relaxed is <code>true</code> and one or more RRD/JRB files are present,
     *  then {@link FetchResults} will be populated with {@link Double#NaN} for all missing entries.
     */
    private FetchResults fetchMeasurements(long start, long end, long step, int maxrows,
                                           Map<Source, String> rrdsBySource, Map<String, Object> constants,
                                           List<Source> sources, boolean relaxed) throws RrdException {
        // NMS-8665: Avoid making calls to XPORT with no definitions
        if (relaxed && rrdsBySource.isEmpty()) {
            return Utils.createEmtpyFetchResults(step, constants);
        }

        FetchResults fetchResults = fetchMeasurements(start, end, step, maxrows, rrdsBySource, constants);
        if (relaxed) {
            Utils.fillMissingValues(fetchResults, sources);
        }
        return fetchResults;
    }

    /**
     * Performs the actual retrieval of the values from the RRD/JRB files.
     */
    protected abstract FetchResults fetchMeasurements(long start, long end, long step, int maxrows,
            Map<Source, String> rrdsBySource, Map<String, Object> constants) throws RrdException;

}
