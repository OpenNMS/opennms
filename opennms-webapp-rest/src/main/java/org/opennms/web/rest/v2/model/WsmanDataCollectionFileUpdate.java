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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Request body of PUT /api/v2/wsman-config/data-collection/{file}: the whole
 * content of one source file (wsman-datacollection-config.xml or a drop-in
 * under wsman-datacollection.d), plus the version it was built from. Objects
 * in the other files are untouched but are consulted so names stay unique
 * and every reference still resolves.
 */
// read-only fields a client round-trips from a GET body (hasPassword, source) are ignored
@JsonIgnoreProperties(ignoreUnknown = true)
public class WsmanDataCollectionFileUpdate {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CollectionUpdate {
        private String name;
        private Integer rrdStep;
        private List<String> rras;
        private boolean includeAllSystemDefinitions;
        private List<String> includedSystemDefinitions;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getRrdStep() { return rrdStep; }
        public void setRrdStep(Integer rrdStep) { this.rrdStep = rrdStep; }
        public List<String> getRras() { return rras; }
        public void setRras(List<String> rras) { this.rras = rras; }
        public boolean isIncludeAllSystemDefinitions() { return includeAllSystemDefinitions; }
        public void setIncludeAllSystemDefinitions(boolean includeAllSystemDefinitions) { this.includeAllSystemDefinitions = includeAllSystemDefinitions; }
        public List<String> getIncludedSystemDefinitions() { return includedSystemDefinitions; }
        public void setIncludedSystemDefinitions(List<String> includedSystemDefinitions) { this.includedSystemDefinitions = includedSystemDefinitions; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributeUpdate {
        private String name;
        private String alias;
        private String type;
        private String indexOf;
        private String filter;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getIndexOf() { return indexOf; }
        public void setIndexOf(String indexOf) { this.indexOf = indexOf; }
        public String getFilter() { return filter; }
        public void setFilter(String filter) { this.filter = filter; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupUpdate {
        private String name;
        private String resourceType;
        private String resourceUri;
        private String dialect;
        private String filter;
        private List<AttributeUpdate> attributes;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public String getResourceUri() { return resourceUri; }
        public void setResourceUri(String resourceUri) { this.resourceUri = resourceUri; }
        public String getDialect() { return dialect; }
        public void setDialect(String dialect) { this.dialect = dialect; }
        public String getFilter() { return filter; }
        public void setFilter(String filter) { this.filter = filter; }
        public List<AttributeUpdate> getAttributes() { return attributes; }
        public void setAttributes(List<AttributeUpdate> attributes) { this.attributes = attributes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SystemDefinitionUpdate {
        private String name;
        private List<String> rules;
        private List<String> includedGroups;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getRules() { return rules; }
        public void setRules(List<String> rules) { this.rules = rules; }
        public List<String> getIncludedGroups() { return includedGroups; }
        public void setIncludedGroups(List<String> includedGroups) { this.includedGroups = includedGroups; }
    }

    private String version;
    private String rrdRepository;
    private List<CollectionUpdate> collections;
    private List<GroupUpdate> groups;
    private List<SystemDefinitionUpdate> systemDefinitions;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getRrdRepository() { return rrdRepository; }
    public void setRrdRepository(String rrdRepository) { this.rrdRepository = rrdRepository; }
    public List<CollectionUpdate> getCollections() { return collections; }
    public void setCollections(List<CollectionUpdate> collections) { this.collections = collections; }
    public List<GroupUpdate> getGroups() { return groups; }
    public void setGroups(List<GroupUpdate> groups) { this.groups = groups; }
    public List<SystemDefinitionUpdate> getSystemDefinitions() { return systemDefinitions; }
    public void setSystemDefinitions(List<SystemDefinitionUpdate> systemDefinitions) { this.systemDefinitions = systemDefinitions; }
}
