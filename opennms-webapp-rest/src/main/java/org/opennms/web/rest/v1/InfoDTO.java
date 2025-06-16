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
package org.opennms.web.rest.v1;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.web.rest.v1.config.TicketerConfig;
import org.opennms.web.rest.v1.config.DatetimeformatConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@XmlRootElement(name="info")
public class InfoDTO {
    private String displayVersion;
    private String version;
    private String packageName;
    private String packageDescription;
    private TicketerConfig ticketerConfig;
    private DatetimeformatConfig datetimeformatConfig;
    private Map<String,String> services = Collections.emptyMap();

    public DatetimeformatConfig getDatetimeformatConfig() {
        return datetimeformatConfig;
    }

    public void setDatetimeformatConfig(DatetimeformatConfig datetimeformatConfig) {
        this.datetimeformatConfig = datetimeformatConfig;
    }

    public void setDisplayVersion(String displayVersion) {
        this.displayVersion = displayVersion;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageDescription(String packageDescription) {
        this.packageDescription = packageDescription;
    }

    public String getPackageDescription() {
        return packageDescription;
    }

    public void setTicketerConfig(TicketerConfig ticketerConfig) {
        this.ticketerConfig = ticketerConfig;
    }

    public TicketerConfig getTicketerConfig() {
        return ticketerConfig;
    }

    public void setServices(final Map<String,String> services) { this.services = services; }

    public Map<String,String> getServices() { return this.services; }
}
