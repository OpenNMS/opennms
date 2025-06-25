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
package org.opennms.systemreport.formatters;

import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.systemreport.event.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.Map;

public class CsvSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(CsvSystemReportFormatter.class);

    @Override
    public String getName() {
        return "csv";
    }

    @Override
    public String getDescription() {
        return "Simple human-readable indented csv";
    }
    
    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getExtension() {
        return "csv";
    }

    @Override
    public boolean canStdout() {
        return true;
    }

    @Override
    public void write(final SystemReportPlugin plugin) {
        if (plugin.getFullOutputOnly()) return;

        LOG.debug("write({})", plugin.getName());
        try {

            Map<String, Resource> pluginData = plugin.getEntries();
            Resource csvHeaders = pluginData.get("csvHeaders");
            Resource csvData = pluginData.get("csvData");

            CsvUtils.writeDataToStream(csvHeaders,csvData,getOutputStream(),Boolean.TRUE);

        } catch (Throwable e) {
            LOG.error("Error writing plugin data.", e);
        }
    }
}
