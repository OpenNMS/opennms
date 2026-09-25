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
package org.opennms.web.rest.v2.plugins;

/**
 * The one place that words how to restart OpenNMS after a plugin change, so
 * the install dialog and the page header say the same thing.
 */
public class RestartInstructions {
    public static final String PACKAGES = "sudo systemctl restart opennms";
    public static final String CONTAINER = "docker restart <container>, or kubectl rollout restart deployment/<name>";
    public static final String HEALTH_CHECK = "opennms:health-check in the Karaf shell, or GET /opennms/rest/health";
    public static final String NOTE = "Plugin features are only loaded at startup. The web UI is unavailable while OpenNMS restarts; wait for the health check to report success before using it again.";

    private String packages = PACKAGES;
    private String container = CONTAINER;
    private String healthCheck = HEALTH_CHECK;
    private String note = NOTE;

    public static RestartInstructions current() {
        return new RestartInstructions();
    }

    public String getPackages() { return packages; }
    public void setPackages(final String packages) { this.packages = packages; }
    public String getContainer() { return container; }
    public void setContainer(final String container) { this.container = container; }
    public String getHealthCheck() { return healthCheck; }
    public void setHealthCheck(final String healthCheck) { this.healthCheck = healthCheck; }
    public String getNote() { return note; }
    public void setNote(final String note) { this.note = note; }
}
