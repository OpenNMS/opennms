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
import org.opennms.systemreport.event.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

public class UserLoginsReportPlugin extends AbstractSystemReportPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(UserLoginsReportPlugin.class);
    @Override
    public String getName() {
        return "User Logins";
    }

    @Override
    public String getDescription() {
        return "User Logins during past 60 days";
    }

    @Override
    public int getPriority() {
        return 7;
    }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public String defaultFormat() { return ".csv"; }

    @Override
    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new HashMap<>();

        try {

            Resource csvHeaders = CsvUtils.readCsvHeaders(CsvUtils.USER_LOGINS_CSV_FILE_PATH);
            Resource csvData = CsvUtils.readCsvData(CsvUtils.USER_LOGINS_CSV_FILE_PATH,Boolean.TRUE);

            map.put("csvHeaders",csvHeaders);
            map.put("csvData",csvData);

        } catch (Exception e) {
            LOG.error("Unable to create temporary file!", e);
        }

        return map;
    }
}
