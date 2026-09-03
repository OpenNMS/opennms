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

/**
 * Request body of PUT /api/v2/wsman-config: the whole document, plus the
 * version it was built from. A null password keeps the stored one (the
 * client never sees it); clearPassword removes it. A definition's
 * sourceIndex names the definition it was loaded from, so its stored
 * password follows it through edits and reordering.
 */
public class WsmanConfigUpdate {

    public static class SettingsUpdate {
        private Integer retry;
        private Integer timeout;
        private String username;
        private String password;
        private boolean clearPassword;
        private Integer port;
        private Integer maxElements;
        private Boolean ssl;
        private Boolean strictSsl;
        private String path;
        private String productVendor;
        private String productVersion;
        private Boolean gssAuth;

        public Integer getRetry() { return retry; }
        public void setRetry(Integer retry) { this.retry = retry; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isClearPassword() { return clearPassword; }
        public void setClearPassword(boolean clearPassword) { this.clearPassword = clearPassword; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public Integer getMaxElements() { return maxElements; }
        public void setMaxElements(Integer maxElements) { this.maxElements = maxElements; }
        public Boolean getSsl() { return ssl; }
        public void setSsl(Boolean ssl) { this.ssl = ssl; }
        public Boolean getStrictSsl() { return strictSsl; }
        public void setStrictSsl(Boolean strictSsl) { this.strictSsl = strictSsl; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getProductVendor() { return productVendor; }
        public void setProductVendor(String productVendor) { this.productVendor = productVendor; }
        public String getProductVersion() { return productVersion; }
        public void setProductVersion(String productVersion) { this.productVersion = productVersion; }
        public Boolean getGssAuth() { return gssAuth; }
        public void setGssAuth(Boolean gssAuth) { this.gssAuth = gssAuth; }
    }

    public static class RangeUpdate {
        private String begin;
        private String end;

        public String getBegin() { return begin; }
        public void setBegin(String begin) { this.begin = begin; }
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
    }

    public static class DefinitionUpdate extends SettingsUpdate {
        private List<RangeUpdate> ranges = new ArrayList<>();
        private List<String> specifics = new ArrayList<>();
        private List<String> ipMatches = new ArrayList<>();
        private Integer sourceIndex;
        private String requisition;

        public List<RangeUpdate> getRanges() { return ranges; }
        public void setRanges(List<RangeUpdate> ranges) { this.ranges = ranges == null ? new ArrayList<>() : ranges; }
        public List<String> getSpecifics() { return specifics; }
        public void setSpecifics(List<String> specifics) { this.specifics = specifics == null ? new ArrayList<>() : specifics; }
        public List<String> getIpMatches() { return ipMatches; }
        public void setIpMatches(List<String> ipMatches) { this.ipMatches = ipMatches == null ? new ArrayList<>() : ipMatches; }
        public Integer getSourceIndex() { return sourceIndex; }
        public void setSourceIndex(Integer sourceIndex) { this.sourceIndex = sourceIndex; }
        public String getRequisition() { return requisition; }
        public void setRequisition(String requisition) { this.requisition = requisition; }
    }

    private SettingsUpdate defaults;
    // null (absent) is rejected: removing every definition must be an explicit empty list
    private List<DefinitionUpdate> definitions;
    // the version the client loaded; the write is refused if the file changed since
    private String version;

    public SettingsUpdate getDefaults() { return defaults; }
    public void setDefaults(SettingsUpdate defaults) { this.defaults = defaults; }
    public List<DefinitionUpdate> getDefinitions() { return definitions; }
    public void setDefinitions(List<DefinitionUpdate> definitions) { this.definitions = definitions; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
