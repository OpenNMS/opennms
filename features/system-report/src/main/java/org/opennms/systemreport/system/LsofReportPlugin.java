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
package org.opennms.systemreport.system;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.TreeMap;

public class LsofReportPlugin extends AbstractSystemReportPlugin {
    @Override
    public String getName() {
        return "lsof";
    }

    @Override
    public String getDescription() {
        return "Output of the 'lsof' command (full output only)";
    }

    @Override
    public int getPriority() {
        return 12;
    }

    @Override
    public boolean getFullOutputOnly() {
        return true;
    }

    @Override
    public boolean getOutputsFiles() {
        return true;
    }

    @Override
    public boolean isVisible() { return true; }
    @Override
    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new TreeMap<String,Resource>();
        String lsofOutput = null;

        final String lsof = getResourceLocator().findBinary("lsof");

        if (lsof != null) {
            lsofOutput = getResourceLocator().slurpOutput(lsof, false);
        }

        if (lsofOutput != null) {
            map.put("Output", getResource(lsofOutput));
        }

        return map;
    }
}
