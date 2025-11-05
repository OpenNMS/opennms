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
package org.opennms.netmgt.measurements.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.QueryNode;
import org.opennms.netmgt.measurements.model.QueryResource;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.measurements.utils.Utils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;

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

        final List<QueryResource> resources = new ArrayList<>();
        final List<QueryResource> additionalResources = new ArrayList<>();

        final Map<Source, String> rrdsBySource = Maps.newHashMap();
        
        final Map<ResourceId, OnmsResource> resourceCache = new HashMap<>();

        for (final Source source : sources) {
            final ResourceId resourceId;
            try {
                resourceId = ResourceId.fromString(source.getResourceId());
            } catch (final IllegalArgumentException ex) {
                if (relaxed) continue;
                LOG.error("Ill-formed resource id: {}", source.getResourceId(), ex);
                resources.add(null);
                return null;
            }

            // Grab the resource
            final OnmsResource resource = resourceCache.computeIfAbsent(resourceId, r -> m_resourceDao.getResourceById(r));

            if (resource == null) {
                if (relaxed) {
                    // Attempt to get parent resource, e.g. the node, to put into response metadata
                    final OnmsResource parentResource = resourceCache.computeIfAbsent(resourceId, r -> m_resourceDao.getResourceById(r.getParent()));

                    if (parentResource != null) {
                        final QueryResource parentResourceInfo = getResourceInfo(parentResource, source);
                        additionalResources.add(parentResourceInfo);
                    }

                    continue;
                }

                LOG.error("No resource with id: {}", source.getResourceId());
                resources.add(null);
                return null;
            }

            final QueryResource resourceInfo = getResourceInfo(resource, source);
            resources.add(resourceInfo);

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

        if (!additionalResources.isEmpty()) {
            resources.addAll(additionalResources);
        }

        // Fetch
        return fetchMeasurements(start, end, step, maxrows, rrdsBySource, constants, sources, new QueryMetadata(resources), relaxed);
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
                                           List<Source> sources, QueryMetadata metadata, boolean relaxed) throws RrdException {
        // NMS-8665: Avoid making calls to XPORT with no definitions
        if (relaxed && rrdsBySource.isEmpty()) {
            return Utils.createEmtpyFetchResults(step, constants);
        }

        FetchResults fetchResults = fetchMeasurements(start, end, step, maxrows, rrdsBySource, constants, metadata);
        if (relaxed) {
            Utils.fillMissingValues(fetchResults, sources);
        }
        return fetchResults;
    }

    /**
     * Performs the actual retrieval of the values from the RRD/JRB files.
     */
    protected abstract FetchResults fetchMeasurements(long start, long end, long step, int maxrows,
            Map<Source, String> rrdsBySource, Map<String, Object> constants, QueryMetadata metadata) throws RrdException;

    private static QueryResource getResourceInfo(final OnmsResource resource, final Source source) {
        if (resource == null) return null;
        OnmsNode node = null;
        try {
            node = ResourceTypeUtils.getNodeFromResourceRoot(resource);
        } catch (final ObjectRetrievalFailureException e) {
            LOG.warn("Failed to get node info from resource: {}", resource, e);
        }
        return new QueryResource(
                                resource.getId().toString(),
                                resource.getParent() == null? null : resource.getParent().getId().toString(),
                                resource.getLabel(),
                                resource.getName(),
                                node == null? null : new QueryNode(node.getId(), node.getForeignSource(), node.getForeignId(), node.getLabel())
                );
    }
}
