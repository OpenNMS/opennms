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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.TreeMap;

public class TopReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(TopReportPlugin.class);

    @Override
    public String getName() {
        return "Top";
    }

    @Override
    public String getDescription() {
        return "Output of the 'top' command (full output only)";
    }

    @Override
    public int getPriority() {
        return 11;
    }

    @Override
    public boolean getFullOutputOnly() {
        return true;
    }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public boolean getOutputsFiles() {
        return true;
    }

    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new TreeMap<String,Resource>();
        final String top = getResourceLocator().findBinary("top");

        String topOutput = null;

        if (top != null) {
            topOutput = getResourceLocator().slurpOutput(top + " -h", true);
            LOG.debug("top -h output: {}", topOutput);

            if (topOutput.contains("-b") && topOutput.contains("-n")) {
                final String topcmd = top + " -n 1 -b";
                LOG.trace("calling: {}", topcmd);
                topOutput = getResourceLocator().slurpOutput(topcmd, false);
            } else if (topOutput.contains("-l")) {
                final String topcmd = top + " -l 1";
                LOG.trace("calling: {}", topcmd);
                topOutput = getResourceLocator().slurpOutput(topcmd, false);
            } else if (topOutput.contains("-d count") && topOutput.contains("-J jail")) {
                final String topcmd = top + " -b -d 1";
                LOG.trace("calling: {}", topcmd);
                topOutput = getResourceLocator().slurpOutput(topcmd, false);
            } else {
                topOutput = null;
            }
        }

        if (topOutput != null) {
            map.put("Output", getResource(topOutput));
        }

        return map;
    }
}
