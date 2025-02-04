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
package org.opennms.systemreport;

import java.util.Map;

import org.springframework.core.io.Resource;

public interface SystemReportPlugin extends Comparable<SystemReportPlugin> {
    /**
     * Get the name of this report plugin.
     * @return the name
     */
    public String getName();

    /**
     * Get a short description of the plugin's operation.
     * @return the description
     */
    public String getDescription();
    
    /**
     * Get the priority of this plugin.  This will be used to sort the various plugins' output when creating an aggregate report.
     * 1-10: system-level plugins
     * 11-50: related to core system functionality (eg, events, alarms, notifications)
     * 51-98: related to non-essential system functionality (eg, UI, reporting)
     * 99: unknown priority
     * @return the priority, from 1 to 99
     */
    public int getPriority();

    /**
     * Get a map of key/value pairs of data exposed by the plugin.
     * @return the plugin's data
     */
    public Map<String,Resource> getEntries();

    public boolean getFullOutputOnly();

    public boolean getOutputsFiles();

    public boolean isVisible();
}
