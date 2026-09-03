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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.TimeSeries;
import org.opennms.netmgt.config.wsman.Attrib;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.Group;
import org.opennms.netmgt.config.wsman.SystemDefinition;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;

/**
 * Read model of the WS-Man data collection configuration: every collection,
 * group and system definition across wsman-datacollection-config.xml and the
 * wsman-datacollection.d drop-ins, each tagged with the file it came from.
 */
public class WsmanDataCollectionDto {

    public static class CollectionInfo {
        private final String name;
        private final String source;
        private final Integer rrdStep;
        private final List<String> rras = new ArrayList<>();
        private final boolean includeAllSystemDefinitions;
        private final List<String> includedSystemDefinitions = new ArrayList<>();

        CollectionInfo(final Collection c, final String source) {
            this.name = c.getName();
            this.source = source;
            this.rrdStep = c.getRrd() == null ? null : c.getRrd().getStep();
            if (c.getRrd() != null) {
                rras.addAll(c.getRrd().getRra());
            }
            this.includeAllSystemDefinitions = c.getIncludeAllSystemDefinitions() != null;
            includedSystemDefinitions.addAll(c.getIncludeSystemDefinition());
        }

        public String getName() { return name; }
        public String getSource() { return source; }
        public Integer getRrdStep() { return rrdStep; }
        public List<String> getRras() { return rras; }
        public boolean isIncludeAllSystemDefinitions() { return includeAllSystemDefinitions; }
        public List<String> getIncludedSystemDefinitions() { return includedSystemDefinitions; }
    }

    public static class AttributeInfo {
        private final String name;
        private final String alias;
        private final String type;
        private final String indexOf;
        private final String filter;

        AttributeInfo(final Attrib a) {
            this.name = a.getName();
            this.alias = a.getAlias();
            this.type = a.getType() == null ? null : a.getType().getName();
            this.indexOf = a.getIndexOf();
            this.filter = a.getFilter();
        }

        public String getName() { return name; }
        public String getAlias() { return alias; }
        public String getType() { return type; }
        public String getIndexOf() { return indexOf; }
        public String getFilter() { return filter; }
    }

    public static class GroupInfo {
        private final String name;
        private final String source;
        private final String resourceType;
        private final String resourceUri;
        private final String dialect;
        private final String filter;
        private final List<AttributeInfo> attributes = new ArrayList<>();

        GroupInfo(final Group g, final String source) {
            this.name = g.getName();
            this.source = source;
            this.resourceType = g.getResourceType();
            this.resourceUri = g.getResourceUri();
            this.dialect = g.getDialect();
            this.filter = g.getFilter();
            for (final Attrib a : g.getAttrib()) {
                attributes.add(new AttributeInfo(a));
            }
        }

        public String getName() { return name; }
        public String getSource() { return source; }
        public String getResourceType() { return resourceType; }
        public String getResourceUri() { return resourceUri; }
        public String getDialect() { return dialect; }
        public String getFilter() { return filter; }
        public List<AttributeInfo> getAttributes() { return attributes; }
    }

    public static class SystemDefinitionInfo {
        private final String name;
        private final String source;
        private final List<String> rules = new ArrayList<>();
        private final List<String> includedGroups = new ArrayList<>();

        SystemDefinitionInfo(final SystemDefinition s, final String source) {
            this.name = s.getName();
            this.source = source;
            rules.addAll(s.getRule());
            includedGroups.addAll(s.getIncludeGroup());
        }

        public String getName() { return name; }
        public String getSource() { return source; }
        public List<String> getRules() { return rules; }
        public List<String> getIncludedGroups() { return includedGroups; }
    }

    private String rrdRepository;
    // org.opennms.timeseries.strategy; rrdRepository and the RRAs only matter under "rrd"
    private final String timeseriesStrategy = System.getProperty(TimeSeries.TIMESERIES_STRATEGY_PROPERTY, TimeSeries.RRD_TIME_SERIES_STRATEGY_NAME);
    private final List<String> sources = new ArrayList<>();
    // content hash per source file; a PUT of that file must present it
    private final Map<String, String> versions = new LinkedHashMap<>();
    private final List<CollectionInfo> collections = new ArrayList<>();
    private final List<GroupInfo> groups = new ArrayList<>();
    private final List<SystemDefinitionInfo> systemDefinitions = new ArrayList<>();

    public String getRrdRepository() { return rrdRepository; }
    public String getTimeseriesStrategy() { return timeseriesStrategy; }
    public List<String> getSources() { return sources; }
    public Map<String, String> getVersions() { return versions; }
    public List<CollectionInfo> getCollections() { return collections; }
    public List<GroupInfo> getGroups() { return groups; }
    public List<SystemDefinitionInfo> getSystemDefinitions() { return systemDefinitions; }

    /** Folds one file in; files are added in the DAO's merge order, root file first. */
    public void addSource(final String source, final String version, final WsmanDatacollectionConfig config) {
        sources.add(source);
        versions.put(source, version);
        if (rrdRepository == null) {
            rrdRepository = config.getRrdRepository();
        }
        for (final Collection c : config.getCollection()) {
            collections.add(new CollectionInfo(c, source));
        }
        for (final Group g : config.getGroup()) {
            groups.add(new GroupInfo(g, source));
        }
        for (final SystemDefinition s : config.getSystemDefinition()) {
            systemDefinitions.add(new SystemDefinitionInfo(s, source));
        }
    }
}
