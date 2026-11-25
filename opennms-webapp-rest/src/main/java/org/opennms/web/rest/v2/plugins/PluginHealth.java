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

import java.util.Objects;

/**
 * One health signal of a running plugin: an OpenNMS health check one of its
 * bundles registered, or a bundle whose blueprint or declarative container has
 * not come up. Statuses follow org.opennms.core.health.api.Status.
 */
public class PluginHealth {
    public static final String SUCCESS = "Success";
    public static final String STARTING = "Starting";
    public static final String FAILURE = "Failure";
    public static final String TIMEOUT = "Timeout";
    public static final String UNKNOWN = "Unknown";

    private String description;
    private String status;
    private String message;

    public PluginHealth() {
    }

    public PluginHealth(final String description, final String status, final String message) {
        this.description = description;
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    /** "description: status" plus the message when there is one; what events and the table title carry. */
    public String summary() {
        return description + ": " + status + (message == null || message.isBlank() ? "" : " (" + message.trim() + ")");
    }

    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(final String message) { this.message = message; }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof PluginHealth)) {
            return false;
        }
        final PluginHealth other = (PluginHealth) o;
        return Objects.equals(description, other.description) && Objects.equals(status, other.status) && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, status, message);
    }

    @Override
    public String toString() {
        return summary();
    }
}
