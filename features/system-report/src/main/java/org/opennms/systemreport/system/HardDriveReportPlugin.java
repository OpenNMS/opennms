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

public class HardDriveReportPlugin  extends AbstractSystemReportPlugin {


    @Override
    public String getName() {
        return "Hard Drive Stats";
    }

    @Override
    public String getDescription() { return "Hard Drive Capacity and Performance Information"; }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new TreeMap<String,Resource>();

        String[] dfCommand = {"bash", "-c", "df -h"};
        final String dfOutput = slurpCommand(dfCommand);
        if (dfOutput != null) {
            map.put("Hard Drive Capacity", getResource("\n"+dfOutput));
        }

        String[] ioStatCommand = {"bash", "-c", "iostat -d"};
        final String iostatOutput = slurpCommand(ioStatCommand);
        if (iostatOutput != null) {
            map.put("Hard Drive Performance", getResource("\n"+iostatOutput));
        }

        return map;
    }

}
