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
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.credentials.Range;
import org.opennms.netmgt.config.wsman.credentials.WsmanAgentConfig;
import org.opennms.netmgt.config.wsman.credentials.WsmanConfig;

/**
 * Read model of wsman-config.xml for the Manage WS-Man page: the agent
 * defaults plus every definition. Passwords are reported only as present or
 * absent; the value never leaves the server.
 */
public class WsmanConfigDto {

    public static class AgentSettings {
        private Integer retry;
        private Integer timeout;
        private String username;
        private boolean hasPassword;
        private Integer port;
        private Integer maxElements;
        private Boolean ssl;
        private Boolean strictSsl;
        private String path;
        private String productVendor;
        private String productVersion;
        private Boolean gssAuth;

        public Integer getRetry() { return retry; }
        public Integer getTimeout() { return timeout; }
        public String getUsername() { return username; }
        public boolean isHasPassword() { return hasPassword; }
        public Integer getPort() { return port; }
        public Integer getMaxElements() { return maxElements; }
        public Boolean getSsl() { return ssl; }
        public Boolean getStrictSsl() { return strictSsl; }
        public String getPath() { return path; }
        public String getProductVendor() { return productVendor; }
        public String getProductVersion() { return productVersion; }
        public Boolean getGssAuth() { return gssAuth; }
    }

    public static class RangeDto {
        private final String begin;
        private final String end;

        public RangeDto(final String begin, final String end) {
            this.begin = begin;
            this.end = end;
        }

        public String getBegin() { return begin; }
        public String getEnd() { return end; }
    }

    public static class DefinitionDto extends AgentSettings {
        private final List<RangeDto> ranges = new ArrayList<>();
        private final List<String> specifics = new ArrayList<>();
        private final List<String> ipMatches = new ArrayList<>();
        private String requisition;

        public String getRequisition() { return requisition; }
        public List<RangeDto> getRanges() { return ranges; }
        public List<String> getSpecifics() { return specifics; }
        public List<String> getIpMatches() { return ipMatches; }
    }

    private final AgentSettings defaults;
    private final List<DefinitionDto> definitions = new ArrayList<>();
    // content hash of the file this was read from; a PUT must present it
    private String version;

    private WsmanConfigDto(final AgentSettings defaults) {
        this.defaults = defaults;
    }

    public AgentSettings getDefaults() { return defaults; }
    public List<DefinitionDto> getDefinitions() { return definitions; }
    public String getVersion() { return version; }
    public void setVersion(final String version) { this.version = version; }

    public static WsmanConfigDto from(final WsmanConfig config) {
        Objects.requireNonNull(config, "config");
        final AgentSettings defaults = new AgentSettings();
        fill(defaults, config);

        final WsmanConfigDto dto = new WsmanConfigDto(defaults);
        for (final Definition def : config.getDefinition()) {
            final DefinitionDto d = new DefinitionDto();
            fill(d, def);
            for (final Range range : def.getRange()) {
                d.ranges.add(new RangeDto(range.getBegin(), range.getEnd()));
            }
            d.specifics.addAll(def.getSpecific());
            d.ipMatches.addAll(def.getIpMatch());
            d.requisition = def.getRequisition();
            dto.definitions.add(d);
        }
        return dto;
    }

    private static void fill(final AgentSettings target, final WsmanAgentConfig source) {
        target.retry = source.getRetry();
        target.timeout = source.getTimeout();
        target.username = source.getUsername();
        target.hasPassword = source.getPassword() != null && !source.getPassword().isEmpty();
        target.port = source.getPort();
        target.maxElements = source.getMaxElements();
        target.ssl = source.isSsl();
        target.strictSsl = source.isStrictSsl();
        target.path = source.getPath();
        target.productVendor = source.getProductVendor();
        target.productVersion = source.getProductVersion();
        target.gssAuth = source.isGssAuth();
    }
}
